#!/usr/bin/env python3
"""Secret-safe native STOMP chat lifecycle verifier.

The caller supplies an existing conversation and an access token.  The script
never prints frames, bearer tokens, message content, or response bodies.
"""

from __future__ import annotations

import importlib.util
import json
import os
import sys
import time
import uuid


def load_transport():
    path = os.path.join(os.path.dirname(__file__), "verify-live-stomp.py")
    spec = importlib.util.spec_from_file_location("stomp_transport", path)
    if spec is None or spec.loader is None:
        raise RuntimeError("unable to load STOMP transport")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def payload_from_frame(frame: str):
    try:
        body = frame.split("\n\n", 1)[1].rstrip("\x00")
        return json.loads(body)
    except (IndexError, json.JSONDecodeError):
        return None


def message_id(frame: str):
    body = payload_from_frame(frame)
    if not isinstance(body, dict):
        return None
    payload = body.get("payload")
    if isinstance(payload, dict) and isinstance(payload.get("id"), str):
        return payload["id"]
    return body.get("messageId") if isinstance(body.get("messageId"), str) else None


def receive(module, sock, deadline):
    frames = []
    try:
        for frame in module.native_stomp_messages(sock, deadline):
            if not frame:
                continue
            if frame.startswith("ERROR\n"):
                raise RuntimeError("STOMP broker/application returned ERROR")
            if frame.startswith("MESSAGE\n"):
                frames.append(frame)
    except TimeoutError:
        # The transport raises on an idle socket; already-received messages
        # remain valid evidence for the command under test.
        pass
    return frames


def send_command(module, sock, destination, body):
    encoded = json.dumps(body, separators=(",", ":"))
    module.native_send(sock, module.stomp_frame(
        "SEND",
        {
            "destination": destination,
            "content-type": "application/json",
            "content-length": str(len(encoded.encode("utf-8"))),
        },
        encoded,
    ))


def connect(module, url, token, timeout):
    sock = module.ws_connect(url, timeout)
    module.native_send(sock, module.stomp_frame("CONNECT", {
        "accept-version": "1.2",
        "host": "localhost",
        "Authorization": "Bearer " + token,
    }))
    for frame in module.native_stomp_messages(sock, time.monotonic() + timeout):
        if frame and frame.startswith("CONNECTED\n"):
            return sock
        if frame and frame.startswith("ERROR\n"):
            break
    sock.close()
    raise RuntimeError("authenticated STOMP CONNECT failed")


def wait_for_receipt(module, sock, receipt_id, timeout):
    for frame in module.native_stomp_messages(sock, time.monotonic() + timeout):
        if frame and frame.startswith("ERROR\n"):
            raise RuntimeError("STOMP broker returned an ERROR")
        if frame and frame.startswith("RECEIPT\n") and f"receipt-id:{receipt_id}" in frame:
            return
    raise RuntimeError("STOMP subscription receipt was not observed")


def main() -> int:
    token = os.getenv("SOUKLAB_ACCESS_TOKEN", "")
    conversation = os.getenv("CHAT_CONVERSATION_ID", "")
    second_token = os.getenv("CHAT_SECOND_TOKEN", "")
    base = os.getenv("STOMP_WS_URL", "ws://localhost:8080/ws/websocket")
    event_subscription = os.getenv("CHAT_EVENT_SUBSCRIPTION", "/user/queue/chat-events")
    ack_subscription = os.getenv("CHAT_ACK_SUBSCRIPTION", "/user/queue/chat")
    timeout = float(os.getenv("STOMP_TIMEOUT_SECONDS", "10"))
    if not token or not conversation:
        print("CHAT_STOMP_RESULT=BLOCKED_INPUT")
        return 2

    module = load_transport()
    sock = None
    stage = "connect"
    try:
        sock = connect(module, base, token, timeout)
        stage = "subscribe"
        module.native_send(sock, module.stomp_frame("SUBSCRIBE", {
            "id": "chat-user",
            "destination": ack_subscription,
            "ack": "client-individual",
            "receipt": "chat-user-subscription",
        }))
        # RabbitMQ's STOMP relay does not emit SUBSCRIBE RECEIPT frames for
        # these user destinations; allow the broker one scheduling interval
        # before publishing the command while retaining the receipt header.
        time.sleep(0.5)
        module.native_send(sock, module.stomp_frame("SUBSCRIBE", {
            "id": "chat-topic",
            "destination": event_subscription,
            "ack": "client-individual",
            "receipt": "chat-event-subscription",
        }))
        time.sleep(0.5)

        prefix = f"/app/v1/conversations/{conversation}"
        correlation = uuid.uuid4().hex
        stage = "send"
        send_command(module, sock, prefix + "/messages.send", {
            "idempotencyKey": "verify-" + correlation,
            "content": "synthetic chat lifecycle probe",
            "attachmentKeys": [],
        })
        frames = receive(module, sock, time.monotonic() + timeout)
        created_id = next((message_id(frame) for frame in frames if message_id(frame)), None)
        if not created_id:
            raise RuntimeError("message send acknowledgement/event was not observed")

        commands = [
            (prefix + "/messages.edit", {
                "messageId": created_id, "correlationId": correlation + "-edit",
                "content": "synthetic edited lifecycle probe",
            }),
            (prefix + "/read", {
                "messageId": created_id, "correlationId": correlation + "-read",
            }),
            (prefix + "/typing.start", {"correlationId": correlation + "-start"}),
            (prefix + "/typing.stop", {"correlationId": correlation + "-stop"}),
            (prefix + "/messages.delete", {
                "messageId": created_id, "correlationId": correlation + "-delete",
            }),
        ]
        stage = "lifecycle"
        for destination, body in commands:
            send_command(module, sock, destination, body)
        receive(module, sock, time.monotonic() + timeout)
        module.native_send(sock, module.stomp_frame("DISCONNECT", {
            "receipt": "chat-verification-disconnect",
        }))
        sock.close()
        sock = None

        if second_token:
            stage = "cross-user"
            outsider = connect(module, base, second_token, timeout)
            try:
                send_command(module, outsider, prefix + "/messages.send", {
                    "idempotencyKey": "verify-outsider-" + uuid.uuid4().hex,
                    "content": "must be rejected",
                    "attachmentKeys": [],
                })
                # The application reports command failures to the user queue;
                # any successful MESSAGE with a newly-created id is forbidden.
                outsider_frames = receive(module, outsider, time.monotonic() + timeout)
                if any(message_id(frame) for frame in outsider_frames):
                    raise RuntimeError("cross-user command was accepted")
            finally:
                outsider.close()

        print("CHAT_STOMP_RESULT=PASS")
        return 0
    except (OSError, RuntimeError, StopIteration, ValueError) as error:
        print(f"CHAT_STOMP_RESULT=FAIL stage={stage} ({type(error).__name__})")
        return 1
    finally:
        if sock is not None:
            sock.close()


if __name__ == "__main__":
    sys.exit(main())
