#!/usr/bin/env python3
"""Small dependency-free SockJS/STOMP smoke client.

It authenticates a STOMP CONNECT, subscribes to a destination, optionally
sends one caller-supplied frame, and verifies one MESSAGE. It deliberately
prints only classifications and never prints the bearer token or frames.
"""

from __future__ import annotations

import base64
import hashlib
import json
import os
import secrets
import socket
import ssl
import sys
import time
import urllib.request
from urllib.parse import urlparse


class VerificationError(Exception):
    pass


def read_http_headers(sock: socket.socket) -> bytes:
    data = bytearray()
    while b"\r\n\r\n" not in data:
        chunk = sock.recv(4096)
        if not chunk:
            raise VerificationError("websocket handshake closed")
        data.extend(chunk)
        if len(data) > 16384:
            raise VerificationError("websocket headers too large")
    return bytes(data)


def ws_connect(url: str, timeout: float) -> socket.socket:
    parsed = urlparse(url)
    if parsed.scheme not in ("ws", "wss") or not parsed.hostname:
        raise VerificationError("STOMP_WS_URL must be ws:// or wss://")
    port = parsed.port or (443 if parsed.scheme == "wss" else 80)
    sock = socket.create_connection((parsed.hostname, port), timeout=timeout)
    sock.settimeout(timeout)
    if parsed.scheme == "wss":
        sock = ssl.create_default_context().wrap_socket(sock, server_hostname=parsed.hostname)
    key = base64.b64encode(secrets.token_bytes(16)).decode()
    path = parsed.path or "/"
    if parsed.query:
        path += "?" + parsed.query
    request = (
        f"GET {path} HTTP/1.1\r\nHost: {parsed.hostname}:{port}\r\n"
        "Upgrade: websocket\r\nConnection: Upgrade\r\n"
        f"Sec-WebSocket-Key: {key}\r\nSec-WebSocket-Version: 13\r\n\r\n"
    ).encode()
    sock.sendall(request)
    headers = read_http_headers(sock).decode("latin1")
    status = headers.split("\r\n", 1)[0]
    if " 101 " not in status:
        raise VerificationError(f"websocket handshake returned {status}")
    expected = base64.b64encode(hashlib.sha1((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").encode()).digest()).decode()
    if f"sec-websocket-accept: {expected.lower()}" not in headers.lower():
        raise VerificationError("websocket handshake response was invalid")
    return sock


def recv_frame(sock: socket.socket) -> tuple[int, bytes]:
    first = sock.recv(2)
    if len(first) != 2:
        raise VerificationError("websocket frame closed")
    opcode = first[0] & 0x0F
    length = first[1] & 0x7F
    if length == 126:
        length = int.from_bytes(sock.recv(2), "big")
    elif length == 127:
        length = int.from_bytes(sock.recv(8), "big")
    masked = first[1] & 0x80
    mask = sock.recv(4) if masked else b""
    payload = bytearray()
    while len(payload) < length:
        chunk = sock.recv(length - len(payload))
        if not chunk:
            raise VerificationError("websocket payload closed")
        payload.extend(chunk)
    if masked:
        payload = bytearray(value ^ mask[index % 4] for index, value in enumerate(payload))
    return opcode, bytes(payload)


def send_frame(sock: socket.socket, payload: bytes, opcode: int = 1) -> None:
    mask = secrets.token_bytes(4)
    encoded = bytes(value ^ mask[index % 4] for index, value in enumerate(payload))
    length = len(encoded)
    if length < 126:
        header = bytes([0x80 | opcode, 0x80 | length])
    elif length <= 0xFFFF:
        header = bytes([0x80 | opcode, 0x80 | 126]) + length.to_bytes(2, "big")
    else:
        header = bytes([0x80 | opcode, 0x80 | 127]) + length.to_bytes(8, "big")
    sock.sendall(header + mask + encoded)


def sockjs_send(sock: socket.socket, stomp: str) -> None:
    send_frame(sock, ("[" + json.dumps(stomp, separators=(",", ":")) + "]").encode())


def native_send(sock: socket.socket, stomp: str) -> None:
    send_frame(sock, stomp.encode())


def native_stomp_messages(sock: socket.socket, deadline: float):
    while time.monotonic() < deadline:
        opcode, payload = recv_frame(sock)
        if opcode == 8:
            return
        if opcode == 1:
            yield payload.decode()


def sockjs_stomp_messages(sock: socket.socket, deadline: float):
    while time.monotonic() < deadline:
        opcode, payload = recv_frame(sock)
        if opcode == 8:
            return
        if opcode != 1:
            continue
        text = payload.decode()
        if text == "o":
            yield None
            continue
        if text.startswith("a"):
            for message in json.loads(text[1:]):
                yield message
        elif text.startswith("c"):
            raise VerificationError("SockJS closed the connection")


def stomp_frame(command: str, headers: dict[str, str], body: str = "") -> str:
    lines = [command] + [f"{key}:{value}" for key, value in headers.items()]
    return "\n".join(lines) + "\n\n" + body + "\x00"


def verify_sockjs_info(url: str, timeout: float) -> None:
    """Verify the public SockJS metadata endpoint without authentication."""
    parsed = urlparse(url)
    scheme = "https" if parsed.scheme == "wss" else "http"
    path = parsed.path.rsplit("/", 1)[0] + "/info"
    info_url = f"{scheme}://{parsed.netloc}{path}"
    request = urllib.request.Request(info_url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(request, timeout=timeout) as response:
        if response.status != 200:
            raise VerificationError("SockJS /info did not return HTTP 200")
        body = json.loads(response.read().decode("utf-8"))
    if not isinstance(body, dict) or "websocket" not in body:
        raise VerificationError("SockJS /info response was not valid metadata")


def verify_unauthenticated_rejection(url: str, timeout: float) -> None:
    """CONNECT without a bearer token and require rejection, not CONNECTED."""
    sock = None
    try:
        sock = ws_connect(url, timeout)
        deadline = time.monotonic() + timeout
        native = url.rstrip("/").endswith("/websocket")
        send = native_send if native else sockjs_send
        messages = native_stomp_messages if native else sockjs_stomp_messages
        if not native and next(messages(sock, deadline), "not-open") is not None:
            raise VerificationError("SockJS open frame was not received")
        send(sock, stomp_frame("CONNECT", {
            "accept-version": "1.2",
            "host": urlparse(url).hostname or "localhost",
        }))
        for message in messages(sock, deadline):
            if message is None:
                continue
            if message.startswith("CONNECTED\n"):
                raise VerificationError("unauthenticated STOMP CONNECT was accepted")
            if message.startswith("ERROR\n"):
                return
        raise VerificationError("unauthenticated STOMP CONNECT received no rejection")
    except VerificationError as exc:
        message = str(exc)
        if "accepted" in message:
            raise
        if any(marker in message for marker in (
            "websocket handshake returned",
            "websocket frame closed",
            "websocket payload closed",
            "SockJS closed the connection",
        )):
            return
        raise
    finally:
        if sock is not None:
            sock.close()


def main() -> int:
    token = os.getenv("SOUKLAB_ACCESS_TOKEN", "")
    url = os.getenv("STOMP_WS_URL", "ws://localhost:8080/ws/websocket")
    destination = os.getenv("STOMP_SUBSCRIPTION", "/user/queue/notifications")
    send_destination = os.getenv("STOMP_SEND_DESTINATION", "")
    send_body = os.getenv("STOMP_SEND_BODY", "")
    timeout = float(os.getenv("STOMP_TIMEOUT_SECONDS", "10"))
    if not token:
        print("STOMP_VERIFY_RESULT=BLOCKED_AUTHENTICATION")
        return 2
    sock = None
    try:
        verify_sockjs_info(url, timeout)
        verify_unauthenticated_rejection(url, timeout)
        sock = ws_connect(url, timeout)
        deadline = time.monotonic() + timeout
        native = url.rstrip("/").endswith("/websocket")
        send = native_send if native else sockjs_send
        messages = native_stomp_messages if native else sockjs_stomp_messages
        if not native and next(messages(sock, deadline), "not-open") is not None:
            raise VerificationError("SockJS open frame was not received")
        send(sock, stomp_frame("CONNECT", {
            "accept-version": "1.2",
            "host": urlparse(url).hostname or "localhost",
            "Authorization": "Bearer " + token,
        }))
        connected = False
        for message in messages(sock, deadline):
            if message.startswith("CONNECTED\n"):
                connected = True
                break
            if message.startswith("ERROR\n"):
                raise VerificationError("STOMP authentication was rejected")
        if not connected:
            raise VerificationError("STOMP CONNECT did not complete")
        send(sock, stomp_frame("SUBSCRIBE", {"id": "souklab-verification", "destination": destination, "ack": "auto"}))
        if send_destination:
            send(sock, stomp_frame("SEND", {"destination": send_destination, "content-type": "application/json"}, send_body))
        if send_destination and os.getenv("STOMP_EXPECT_MESSAGE", "true").lower() == "true":
            if not any(message.startswith("MESSAGE\n") for message in messages(sock, deadline)):
                raise VerificationError("STOMP MESSAGE was not delivered")
        send(sock, stomp_frame("DISCONNECT", {"receipt": "souklab-verification-disconnect"}))
        print("STOMP_VERIFY_RESULT=PASS")
        return 0
    except (OSError, VerificationError, json.JSONDecodeError, StopIteration) as exc:
        print(f"STOMP_VERIFY_RESULT=BLOCKED_OR_FAILED ({type(exc).__name__})")
        return 1
    finally:
        if sock is not None:
            sock.close()


if __name__ == "__main__":
    sys.exit(main())
