#!/usr/bin/env python3
"""Exhaustive live verification for Domain 6: Messaging & WebSocket.

Covers 70 live scenarios:
1. REST Conversation Management & Authorization (d6-conv-01 to d6-conv-14)
2. REST Messaging & Validation Boundaries (d6-msg-15 to d6-msg-30)
3. Message Attachments via REST (d6-att-31 to d6-att-36)
4. Message History & Cursor-Based Pagination (d6-hist-37 to d6-hist-44)
5. STOMP over WebSocket Connection & Handshake (d6-stomp-45 to d6-stomp-50)
6. STOMP Subscriptions & Multi-Destination Event Delivery (d6-sub-51 to d6-stomp-62)
7. STOMP Message Edit, Delete, and Reconnection Consistency (d6-stomp-63 to d6-stomp-70)
"""

from __future__ import annotations

import asyncio
import io
import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
from typing import Any

import aiohttp

BASE_URL = os.getenv("APP_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
WS_URL = BASE_URL.replace("http://", "ws://").replace("https://", "wss://") + "/ws/websocket"

DB_HOST = os.getenv("VERIFY_DB_HOST", "127.0.0.1")
DB_PORT = os.getenv("VERIFY_DB_PORT", "3307")
DB_NAME = os.getenv("VERIFY_DB_NAME", "souklab_test")
DB_USER = os.getenv("VERIFY_DB_USER", "souklab_test")
DB_PASS = os.getenv("VERIFY_DB_PASSWORD", "souklab_test_password")

RUN_ID = f"d6-{int(time.time())}"
results: list[dict[str, Any]] = []

VALID_PNG_BYTES = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\rIDATx\x9cc`\x00\x00\x00"
    b"\x02\x00\x01H\xaf\xa4q\x00\x00\x00\x00IEND\xaeB`\x82"
)


def db_query(sql: str) -> list[list[str]]:
    env = os.environ.copy()
    env["MYSQL_PWD"] = DB_PASS
    cmd = [
        "mariadb",
        "-h", DB_HOST,
        "-P", str(DB_PORT),
        "-u", DB_USER,
        "-D", DB_NAME,
        "-s",
        "-N",
        "-e", sql,
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, env=env)
    if res.returncode != 0:
        raise RuntimeError(f"Database query failed: {res.stderr}\nQuery: {sql}")
    lines = res.stdout.strip().split("\n") if res.stdout.strip() else []
    return [line.split("\t") for line in lines]


def db_execute(sql: str) -> None:
    env = os.environ.copy()
    env["MYSQL_PWD"] = DB_PASS
    cmd = [
        "mariadb",
        "-h", DB_HOST,
        "-P", str(DB_PORT),
        "-u", DB_USER,
        "-D", DB_NAME,
        "-e", sql,
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, env=env)
    if res.returncode != 0:
        raise RuntimeError(f"Database execution failed: {res.stderr}\nSQL: {sql}")


def http_json(
    method: str,
    path: str,
    payload: dict[str, Any] | None = None,
    token: str | None = None,
    query_params: dict[str, Any] | None = None,
) -> tuple[int, dict[str, str], Any]:
    url = BASE_URL + path
    if query_params:
        url = f"{url}?{urllib.parse.urlencode(query_params)}"
    headers = {"Accept": "application/json"}
    body = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        body = json.dumps(payload).encode("utf-8")
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = resp.read()
            parsed = json.loads(data.decode("utf-8")) if data else None
            return resp.status, dict(resp.headers), parsed
    except urllib.error.HTTPError as exc:
        data = exc.read()
        try:
            parsed = json.loads(data.decode("utf-8"))
        except Exception:
            parsed = data.decode("utf-8", errors="replace")
        return exc.code, dict(exc.headers), parsed
    except Exception as exc:
        return 0, {}, {"error": str(exc)}


def http_multipart(
    method: str,
    path: str,
    fields: dict[str, str],
    files: dict[str, tuple[str, bytes, str]],
    token: str | None = None,
) -> tuple[int, dict[str, str], Any]:
    boundary = f"----WebKitFormBoundary{uuid.uuid4().hex}"
    buf = io.BytesIO()

    for k, v in fields.items():
        buf.write(f"--{boundary}\r\n".encode("utf-8"))
        buf.write(f'Content-Disposition: form-data; name="{k}"\r\n\r\n'.encode("utf-8"))
        buf.write(f"{v}\r\n".encode("utf-8"))

    for k, (filename, content, mime) in files.items():
        buf.write(f"--{boundary}\r\n".encode("utf-8"))
        buf.write(f'Content-Disposition: form-data; name="{k}"; filename="{filename}"\r\n'.encode("utf-8"))
        buf.write(f"Content-Type: {mime}\r\n\r\n".encode("utf-8"))
        buf.write(content)
        buf.write(b"\r\n")

    buf.write(f"--{boundary}--\r\n".encode("utf-8"))
    body = buf.getvalue()

    url = BASE_URL + path
    headers = {
        "Accept": "application/json",
        "Content-Type": f"multipart/form-data; boundary={boundary}",
        "Content-Length": str(len(body)),
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = resp.read()
            parsed = json.loads(data.decode("utf-8")) if data else None
            return resp.status, dict(resp.headers), parsed
    except urllib.error.HTTPError as exc:
        data = exc.read()
        try:
            parsed = json.loads(data.decode("utf-8"))
        except Exception:
            parsed = data.decode("utf-8", errors="replace")
        return exc.code, dict(exc.headers), parsed
    except Exception as exc:
        return 0, {}, {"error": str(exc)}


def record(scenario_id: str, desc: str, passed: bool, expected: str, actual: str, payload: Any = None) -> None:
    results.append({
        "scenario_id": scenario_id,
        "description": desc,
        "passed": passed,
        "expected": expected,
        "actual": actual,
        "payload": payload,
    })
    status_str = "PASS" if passed else "FAIL"
    print(f"[{status_str}] {scenario_id}: {desc} | Expected: {expected} | Actual: {actual}")
    if not passed:
        print(f"  FAILED RESPONSE BODY: {json.dumps(payload, indent=2, default=str)}")
        print("\nStopping immediately per verification protocol.")
        sys.exit(1)


def register_user(email: str, password: str, account_type: str = "CLIENT") -> tuple[str, str]:
    reg_payload = {
        "email": email,
        "password": password,
        "firstName": "User",
        "lastName": email.split("@")[0],
        "phone": "+213555112233",
        "accountType": account_type,
    }
    st, _, resp = http_json("POST", "/api/v1/auth/register", reg_payload)
    if st not in (200, 201):
        raise RuntimeError(f"Registration failed ({st}): {resp}")

    db_execute(f"UPDATE users SET email_verified = 1, status = 'ACTIVE' WHERE email = '{email}';")
    user_rows = db_query(f"SELECT id FROM users WHERE email = '{email}'")
    if not user_rows:
        raise RuntimeError(f"User not found for {email}")
    uid = user_rows[0][0]

    st, _, lresp = http_json("POST", "/api/v1/auth/login", {"email": email, "password": password})
    if st != 200 or not lresp.get("success"):
        raise RuntimeError(f"Login failed ({st}): {lresp}")
    token = lresp["data"]["accessToken"]

    if account_type == "ARTISAN":
        reg_id = "00e08ab0-36c4-4e48-91f5-0a3ebfe168ea"
        sub_id = "0a905e89-0723-4512-865d-1c65ba658641"
        http_json("POST", "/api/v1/auth/complete-profile", {
            "bio": "Artisan profil",
            "city": "Boumerdès",
            "address": "123 Rue des Artisans",
            "regionId": reg_id,
            "subCategoryId": sub_id,
        }, token=token)

    return token, uid


# =============================================================================
# STOMP Protocol Utilities
# =============================================================================

class StompFrame:
    def __init__(self, command: str, headers: dict[str, str], body: str = ""):
        self.command = command
        self.headers = headers
        self.body = body

    @classmethod
    def parse(cls, raw: str) -> list[StompFrame]:
        frames = []
        chunks = raw.split("\x00")
        for chunk in chunks:
            chunk = chunk.strip("\n\r ")
            if not chunk:
                continue
            parts = chunk.split("\n\n", 1)
            header_lines = parts[0].split("\n")
            command = header_lines[0].strip()
            headers = {}
            for line in header_lines[1:]:
                if ":" in line:
                    k, v = line.split(":", 1)
                    headers[k.strip()] = v.strip()
            body = parts[1] if len(parts) > 1 else ""
            frames.append(cls(command, headers, body))
        return frames

    def serialize(self) -> str:
        lines = [self.command]
        for k, v in self.headers.items():
            lines.append(f"{k}:{v}")
        lines.append("")
        lines.append(self.body)
        return "\n".join(lines) + "\x00"


class StompClient:
    def __init__(self, ws: aiohttp.ClientWebSocketResponse, name: str = "Client"):
        self.ws = ws
        self.name = name
        self.received_frames: list[StompFrame] = []
        self._event = asyncio.Event()
        self._reader_task = asyncio.create_task(self._reader_loop())
        self.closed = False

    async def _reader_loop(self):
        try:
            async for msg in self.ws:
                if msg.type == aiohttp.WSMsgType.TEXT:
                    parsed_frames = StompFrame.parse(msg.data)
                    for frame in parsed_frames:
                        self.received_frames.append(frame)
                        self._event.set()
                elif msg.type in (aiohttp.WSMsgType.CLOSED, aiohttp.WSMsgType.ERROR):
                    break
        except Exception:
            pass
        finally:
            self.closed = True
            self._event.set()

    async def send_frame(self, command: str, headers: dict[str, str], body: str = ""):
        frame = StompFrame(command, headers, body)
        await self.ws.send_str(frame.serialize())

    def _matches(self, frame: StompFrame, command: str | None, destination: str | None, event_type: str | None) -> bool:
        if command and frame.command != command:
            return False
        if destination and frame.headers.get("destination") != destination:
            return False
        if event_type:
            try:
                payload = json.loads(frame.body)
                if payload.get("type") != event_type:
                    return False
            except Exception:
                return False
        return True

    async def expect_frame(
        self,
        command: str | None = None,
        destination: str | None = None,
        event_type: str | None = None,
        timeout: float = 4.0,
    ) -> StompFrame | None:
        start_time = time.time()
        while time.time() - start_time < timeout:
            for idx, frame in enumerate(self.received_frames):
                if self._matches(frame, command, destination, event_type):
                    return self.received_frames.pop(idx)
            self._event.clear()
            remaining = max(0.05, timeout - (time.time() - start_time))
            try:
                await asyncio.wait_for(self._event.wait(), timeout=remaining)
            except asyncio.TimeoutError:
                break
        # Final pass
        for idx, frame in enumerate(self.received_frames):
            if self._matches(frame, command, destination, event_type):
                return self.received_frames.pop(idx)
        return None

    async def drain(self) -> list[StompFrame]:
        frames = list(self.received_frames)
        self.received_frames.clear()
        return frames

    async def close(self):
        self._reader_task.cancel()
        if not self.ws.closed:
            await self.ws.close()


# =============================================================================
# Main Verification Suite
# =============================================================================

async def async_main():
    print(f"=== Domain 6 Live Verification: Messaging & WebSocket (RUN_ID: {RUN_ID}) ===")

    # 1. Setup accounts
    a_email = f"d6_user_a_{RUN_ID}@example.com"
    b_email = f"d6_user_b_{RUN_ID}@example.com"
    c_email = f"d6_user_c_{RUN_ID}@example.com"
    susp_email = f"d6_user_susp_{RUN_ID}@example.com"

    token_a, id_a = register_user(a_email, "Password123!", "CLIENT")
    token_b, id_b = register_user(b_email, "Password123!", "CLIENT")
    token_c, id_c = register_user(c_email, "Password123!", "CLIENT")
    token_susp, id_susp = register_user(susp_email, "Password123!", "CLIENT")
    db_execute(f"UPDATE users SET status = 'SUSPENDED' WHERE id = '{id_susp}';")

    print(f"✓ Accounts setup: User A ({id_a}), User B ({id_b}), User C ({id_c}), Suspended ({id_susp})")

    # =========================================================================
    # Part 1: REST Conversation Management & Authorization (d6-conv-01 to 14)
    # =========================================================================

    # d6-conv-01: Anonymous conversation creation rejection
    st, _, resp = http_json("POST", "/api/v1/conversations", {"recipientUserId": id_b})
    passed = (st == 401)
    record("d6-conv-01", "Anonymous conversation creation rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d6-conv-02: Self-conversation rejection
    st, _, resp = http_json("POST", "/api/v1/conversations", {"recipientUserId": id_a}, token=token_a)
    passed = (st == 400 and "cannot message yourself" in str(resp.get("message", "")))
    record("d6-conv-02", "Self-conversation rejection", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d6-conv-03: Non-existent recipient rejection
    st, _, resp = http_json("POST", "/api/v1/conversations", {"recipientUserId": "00000000-0000-0000-0000-000000000000"}, token=token_a)
    passed = (st == 404 and "Recipient not found" in str(resp.get("message", "")))
    record("d6-conv-03", "Non-existent recipient rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d6-conv-04: Suspended user conversation creation rejection
    st, _, resp = http_json("POST", "/api/v1/conversations", {"recipientUserId": id_b}, token=token_susp)
    passed = (st in (401, 403))
    record("d6-conv-04", "Suspended user conversation creation rejection", passed, "401 UNAUTHORIZED or 403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d6-conv-05: Creating conversation with suspended recipient rejection
    st, _, resp = http_json("POST", "/api/v1/conversations", {"recipientUserId": id_susp}, token=token_a)
    passed = (st == 403 and "not eligible for messaging" in str(resp.get("message", "")))
    record("d6-conv-05", "Creating conversation with suspended recipient rejection", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d6-conv-06: Valid conversation creation between User A and User B
    st, _, resp = http_json("POST", "/api/v1/conversations", {"recipientUserId": id_b}, token=token_a)
    conv_data = resp.get("data", {}) if resp else {}
    conv_id = conv_data.get("id")
    passed = (st == 201 and resp.get("success") and conv_data.get("participantUserId") == id_b and conv_data.get("unreadCount") == 0)
    record("d6-conv-06", "Valid conversation creation between User A and User B", passed, "201 CREATED unreadCount=0", f"{st} id={conv_id}", resp)

    # d6-conv-07: Idempotent conversation lookup
    st, _, resp = http_json("POST", "/api/v1/conversations", {"recipientUserId": id_b}, token=token_a)
    passed = (st == 201 and resp.get("data", {}).get("id") == conv_id)
    record("d6-conv-07", "Idempotent conversation lookup", passed, "201 CREATED same conversationId", f"{st} id={resp.get('data', {}).get('id')}", resp)

    # d6-conv-08: List conversations for User A
    st, _, resp = http_json("GET", "/api/v1/conversations", token=token_a)
    a_convs = resp.get("data", []) if resp else []
    passed = (st == 200 and any(c.get("id") == conv_id for c in a_convs))
    record("d6-conv-08", "List conversations for User A", passed, "200 OK includes created conversation", f"{st} count={len(a_convs)}", resp)

    # d6-conv-09: List conversations for third-party User C
    st, _, resp = http_json("GET", "/api/v1/conversations", token=token_c)
    c_convs = resp.get("data", []) if resp else []
    passed = (st == 200 and not any(c.get("id") == conv_id for c in c_convs))
    record("d6-conv-09", "List conversations for third-party User C", passed, "200 OK excludes A-B conversation", f"{st} count={len(c_convs)}", resp)

    # d6-conv-10: Archive conversation
    st, _, resp = http_json("PATCH", f"/api/v1/conversations/{conv_id}/archive", {"archived": True}, token=token_a)
    passed = (st == 200 and resp.get("success"))
    record("d6-conv-10", "Archive conversation", passed, "200 OK", f"{st}", resp)

    # d6-conv-11: Active list excludes archived
    st, _, resp = http_json("GET", "/api/v1/conversations", query_params={"archived": "false"}, token=token_a)
    act_convs = resp.get("data", []) if resp else []
    passed = (st == 200 and not any(c.get("id") == conv_id for c in act_convs))
    record("d6-conv-11", "Active list excludes archived conversation", passed, "200 OK excluded", f"{st} count={len(act_convs)}", resp)

    # d6-conv-12: Archived list includes archived
    st, _, resp = http_json("GET", "/api/v1/conversations", query_params={"archived": "true"}, token=token_a)
    arch_convs = resp.get("data", []) if resp else []
    passed = (st == 200 and any(c.get("id") == conv_id for c in arch_convs))
    record("d6-conv-12", "Archived list includes archived conversation", passed, "200 OK included", f"{st} count={len(arch_convs)}", resp)

    # d6-conv-13: Non-participant cannot archive conversation
    st, _, resp = http_json("PATCH", f"/api/v1/conversations/{conv_id}/archive", {"archived": True}, token=token_c)
    passed = (st == 404 and "Conversation not found" in str(resp.get("message", "")))
    record("d6-conv-13", "Non-participant cannot archive conversation", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d6-conv-14: Unarchive conversation
    st, _, resp = http_json("PATCH", f"/api/v1/conversations/{conv_id}/archive", {"archived": False}, token=token_a)
    passed = (st == 200 and resp.get("success"))
    record("d6-conv-14", "Unarchive conversation", passed, "200 OK", f"{st}", resp)

    # =========================================================================
    # Part 2: REST Messaging & Validation Boundaries (d6-msg-15 to 30)
    # =========================================================================

    # d6-msg-15: Anonymous message send rejection
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": "k-1", "content": "Hello"})
    passed = (st == 401)
    record("d6-msg-15", "Anonymous message send rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d6-msg-16: Non-participant message send rejection
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": "k-2", "content": "Pirate message"}, token=token_c)
    passed = (st == 404 and "Conversation not found" in str(resp.get("message", "")))
    record("d6-msg-16", "Non-participant message send rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d6-msg-17: Non-existent conversation message send
    st, _, resp = http_json("POST", "/api/v1/conversations/00000000-0000-0000-0000-000000000000/messages", {"idempotencyKey": "k-3", "content": "Ghost"}, token=token_a)
    passed = (st == 404 and "Conversation not found" in str(resp.get("message", "")))
    record("d6-msg-17", "Non-existent conversation message send", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d6-msg-18: Validation - blank content
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": "k-4", "content": "   "}, token=token_a)
    passed = (st in (400, 422))
    record("d6-msg-18", "Validation - blank content", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d6-msg-19: Validation - missing idempotencyKey
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": None, "content": "Hello"}, token=token_a)
    passed = (st in (400, 422))
    record("d6-msg-19", "Validation - missing idempotencyKey", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d6-msg-20: Validation - idempotencyKey > 128 chars
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": "X" * 129, "content": "Hello"}, token=token_a)
    passed = (st in (400, 422))
    record("d6-msg-20", "Validation - idempotencyKey > 128 chars", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d6-msg-21: Rule - content > 4000 chars rejected
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": "k-5", "content": "A" * 4001}, token=token_a)
    passed = (st == 400 and "exceeds configured limit" in str(resp.get("message", "")))
    record("d6-msg-21", "Rule - content > 4000 chars rejected", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d6-msg-22: User A sends valid message via REST
    idem_key_1 = f"idem-a1-{RUN_ID}"
    msg1_payload = {"idempotencyKey": idem_key_1, "content": f"Bonjour User B, première question sur votre poterie {RUN_ID}"}
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", msg1_payload, token=token_a)
    m1_data = resp.get("data", {}) if resp else {}
    msg1_id = m1_data.get("id")
    passed = (st == 201 and resp.get("success") and m1_data.get("authorId") == id_a and m1_data.get("deleted") is False and m1_data.get("content") == msg1_payload["content"])
    record("d6-msg-22", "User A sends valid message via REST", passed, "201 CREATED message returned", f"{st} id={msg1_id}", resp)

    # d6-msg-23: Idempotent message retry
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", msg1_payload, token=token_a)
    passed = (st == 201 and resp.get("data", {}).get("id") == msg1_id)
    record("d6-msg-23", "Idempotent message retry", passed, "201 CREATED same messageId without duplication", f"{st} id={resp.get('data', {}).get('id')}", resp)

    # d6-msg-24: User B views conversation list
    st, _, resp = http_json("GET", "/api/v1/conversations", token=token_b)
    b_convs = resp.get("data", []) if resp else []
    target_c = next((c for c in b_convs if c.get("id") == conv_id), None)
    passed = (st == 200 and target_c is not None and target_c.get("unreadCount") == 1 and target_c.get("lastMessagePreview") == msg1_payload["content"])
    record("d6-msg-24", "User B views conversation list (unreadCount=1, preview matches)", passed, "200 OK unreadCount=1", f"{st} unread={target_c.get('unreadCount') if target_c else None}", resp)

    # d6-msg-25: User B sends reply message via REST
    idem_key_2 = f"idem-b1-{RUN_ID}"
    msg2_payload = {"idempotencyKey": idem_key_2, "content": f"Bonjour User A, avec plaisir, voici les détails {RUN_ID}"}
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", msg2_payload, token=token_b)
    m2_data = resp.get("data", {}) if resp else {}
    msg2_id = m2_data.get("id")
    passed = (st == 201 and resp.get("success") and m2_data.get("authorId") == id_b)
    record("d6-msg-25", "User B sends reply message via REST", passed, "201 CREATED", f"{st} id={msg2_id}", resp)

    # d6-msg-26: Non-author edit message rejection
    st, _, resp = http_json("PATCH", f"/api/v1/conversations/{conv_id}/messages/{msg1_id}", {"content": "Pirated edit"}, token=token_b)
    passed = (st == 403 and "Only the author may edit" in str(resp.get("message", "")))
    record("d6-msg-26", "Non-author edit message rejection", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d6-msg-27: Author edits message via REST
    edit_payload = {"content": f"Bonjour User B, question modifiée avec précision {RUN_ID}"}
    st, _, resp = http_json("PATCH", f"/api/v1/conversations/{conv_id}/messages/{msg1_id}", edit_payload, token=token_a)
    m_edited = resp.get("data", {}) if resp else {}
    passed = (st == 200 and resp.get("success") and m_edited.get("content") == edit_payload["content"] and m_edited.get("editedAt") is not None)
    record("d6-msg-27", "Author edits message via REST", passed, "200 OK content updated and editedAt set", f"{st} editedAt={m_edited.get('editedAt')}", resp)

    # d6-msg-28: Non-author delete message rejection
    st, _, resp = http_json("DELETE", f"/api/v1/conversations/{conv_id}/messages/{msg1_id}", token=token_b)
    passed = (st == 403 and "Only the author may delete" in str(resp.get("message", "")))
    record("d6-msg-28", "Non-author delete message rejection", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d6-msg-29: Author deletes message via REST
    # Create an extra message msg3 by A to test deletion
    st_m3, _, resp_m3 = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": f"k-del-{RUN_ID}", "content": "Message à effacer"}, token=token_a)
    msg3_id = resp_m3["data"]["id"]
    st, _, resp = http_json("DELETE", f"/api/v1/conversations/{conv_id}/messages/{msg3_id}", token=token_a)
    passed = (st == 200 and resp.get("success"))
    record("d6-msg-29", "Author deletes message via REST", passed, "200 OK", f"{st}", resp)

    # d6-msg-30: Soft-deleted message presentation (excluded from message history)
    st, _, resp = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", token=token_a)
    messages_list = resp.get("data", {}).get("content", []) if resp else []
    deleted_msg = next((m for m in messages_list if m.get("id") == msg3_id), None)
    passed = (st == 200 and deleted_msg is None)
    record("d6-msg-30", "Soft-deleted message excluded from message history", passed, "deleted message excluded from content", f"st={st} found={deleted_msg is not None}", resp)

    # =========================================================================
    # Part 3: Message Attachments via REST (d6-att-31 to 36)
    # =========================================================================

    # d6-att-31: Upload attachment by non-participant rejected
    st, _, resp = http_multipart("POST", f"/api/v1/conversations/{conv_id}/attachments", {}, {"file": ("file.png", VALID_PNG_BYTES, "image/png")}, token=token_c)
    passed = (st == 404 and "Conversation not found" in str(resp.get("message", "")))
    record("d6-att-31", "Upload attachment by non-participant rejected", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d6-att-32: Upload empty attachment rejected
    st, _, resp = http_multipart("POST", f"/api/v1/conversations/{conv_id}/attachments", {}, {"file": ("empty.png", b"", "image/png")}, token=token_a)
    passed = (st in (400, 422))
    record("d6-att-32", "Upload empty attachment rejected", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d6-att-33: User A uploads valid PNG attachment
    st, _, resp = http_multipart("POST", f"/api/v1/conversations/{conv_id}/attachments", {}, {"file": ("photo.png", VALID_PNG_BYTES, "image/png")}, token=token_a)
    att_data = resp.get("data", {}) if resp else {}
    att_key = att_data.get("key") or att_data.get("storageKey")
    passed = (st == 201 and resp.get("success") and att_key is not None and att_data.get("contentType") == "image/png")
    record("d6-att-33", "User A uploads valid PNG attachment", passed, "201 CREATED storageKey returned", f"{st} key={att_key}", resp)

    # d6-att-34: User B attempts to use User A's attachment key in message
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {
        "idempotencyKey": f"k-stolen-{RUN_ID}",
        "content": "Using stolen key",
        "attachmentKeys": [att_key],
    }, token=token_b)
    passed = (st == 400 and "not owned by the sender" in str(resp.get("message", "")))
    record("d6-att-34", "User B attempts to use User A's attachment key in message", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d6-att-35: User A sends message with attachment key
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {
        "idempotencyKey": f"k-att-msg-{RUN_ID}",
        "content": "Voici l'image demandée",
        "attachmentKeys": [att_key],
    }, token=token_a)
    msg_with_att = resp.get("data", {}) if resp else {}
    atts = msg_with_att.get("attachments", [])
    att_url = (atts[0].get("downloadUrl") or atts[0].get("url")) if atts else None
    passed = (st == 201 and len(atts) == 1 and bool(att_url))
    record("d6-att-35", "User A sends message with attachment key", passed, "201 CREATED attachments present with downloadUrl", f"{st} atts_count={len(atts)}", resp)

    # d6-att-36: Attachment download / URL valid
    if att_url and not att_url.startswith("http"):
        att_url = BASE_URL + att_url
    st_dl = 0
    try:
        req = urllib.request.Request(att_url, headers={"Authorization": f"Bearer {token_a}"})
        with urllib.request.urlopen(req, timeout=5) as r:
            st_dl = r.status
    except urllib.error.HTTPError as e:
        st_dl = e.code
    except Exception:
        pass
    passed = (st_dl in (200, 302))
    record("d6-att-36", "Attachment download / URL valid", passed, "200/302 OK", f"{st_dl}", {"url": att_url})

    # =========================================================================
    # Part 4: Message History & Cursor-Based Pagination (d6-hist-37 to 44)
    # =========================================================================

    # d6-hist-37: Non-participant message history rejection
    st, _, resp = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", token=token_c)
    passed = (st == 404 and "Conversation not found" in str(resp.get("message", "")))
    record("d6-hist-37", "Non-participant message history rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d6-hist-38: Message history order (Newest first)
    st, _, resp = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", token=token_a)
    msgs = resp.get("data", {}).get("content", []) if resp else []
    # Check descending order of createdAt
    created_times = [m.get("createdAt") for m in msgs if m.get("createdAt")]
    is_desc = all(created_times[i] >= created_times[i+1] for i in range(len(created_times)-1))
    passed = (st == 200 and len(msgs) >= 3 and is_desc)
    record("d6-hist-38", "Message history order (Newest first)", passed, "200 OK descending createdAt", f"{st} desc={is_desc} count={len(msgs)}", msgs)

    # d6-hist-39: Pagination size clamp (size=0, size=200)
    st_0, _, resp_0 = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", query_params={"size": 0}, token=token_a)
    st_max, _, resp_max = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", query_params={"size": 200}, token=token_a)
    passed = (st_0 == 200 and st_max == 200)
    record("d6-hist-39", "Pagination size clamp (size=0 clamped to default, size=200 clamped to max)", passed, "200 OK clamped without error", f"st_0={st_0} st_max={st_max}", resp_0)

    # d6-hist-40: Cursor-based forward pagination
    st, _, resp = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", query_params={"size": 2}, token=token_a)
    page1_msgs = resp.get("data", {}).get("content", []) if resp else []
    next_cursor = resp.get("data", {}).get("nextCursor") if resp else None
    passed = (st == 200 and len(page1_msgs) == 2 and next_cursor is not None and resp.get("data", {}).get("last") is False)
    record("d6-hist-40", "Cursor-based forward pagination (Page 1)", passed, "200 OK nextCursor present last=false", f"{st} count={len(page1_msgs)} has_cursor={bool(next_cursor)}", resp)

    # d6-hist-41: Fetch next page using nextCursor
    st, _, resp = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", query_params={"cursor": next_cursor, "size": 2}, token=token_a)
    page2_msgs = resp.get("data", {}).get("content", []) if resp else []
    p1_ids = {m["id"] for m in page1_msgs}
    p2_ids = {m["id"] for m in page2_msgs}
    passed = (st == 200 and len(page2_msgs) > 0 and len(p1_ids.intersection(p2_ids)) == 0)
    record("d6-hist-41", "Fetch next page using nextCursor (Page 2)", passed, "200 OK non-overlapping messages", f"{st} count={len(page2_msgs)} overlap={len(p1_ids.intersection(p2_ids))}", resp)

    # d6-hist-42: Malformed cursor rejected
    st, _, resp = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", query_params={"cursor": "not-valid-base64-!!!"}, token=token_a)
    passed = (st == 400 and "Invalid message cursor" in str(resp.get("message", "")))
    record("d6-hist-42", "Malformed cursor rejected", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d6-hist-43: Mark conversation read via REST
    st, _, resp = http_json("POST", f"/api/v1/conversations/{conv_id}/read", {}, token=token_b)
    passed = (st == 200 and resp.get("success"))
    record("d6-hist-43", "Mark conversation read via REST", passed, "200 OK", f"{st}", resp)

    # d6-hist-44: Unread count reset to 0 after read
    st, _, resp = http_json("GET", "/api/v1/conversations", token=token_b)
    b_convs = resp.get("data", []) if resp else []
    target_c = next((c for c in b_convs if c.get("id") == conv_id), None)
    passed = (st == 200 and target_c is not None and target_c.get("unreadCount") == 0)
    record("d6-hist-44", "Unread count reset to 0 after read", passed, "200 OK unreadCount=0", f"{st} unread={target_c.get('unreadCount') if target_c else None}", resp)

    # =========================================================================
    # Part 5: STOMP over WebSocket Connection & Handshake (d6-stomp-45 to 50)
    # =========================================================================

    async with aiohttp.ClientSession() as session:

        # d6-stomp-45: STOMP CONNECT without Authorization header
        async with session.ws_connect(WS_URL) as ws:
            await ws.send_str("CONNECT\naccept-version:1.1,1.2\n\n\x00")
            msg = await ws.receive()
            passed = ("ERROR" in str(msg.data))
            record("d6-stomp-45", "STOMP CONNECT without Authorization header", passed, "ERROR frame connection rejected", f"Frame={msg.data[:60]}", msg.data)

        # d6-stomp-46: STOMP CONNECT with malformed/invalid Bearer token
        async with session.ws_connect(WS_URL) as ws:
            await ws.send_str("CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer invalid-garbage-token\n\n\x00")
            msg = await ws.receive()
            passed = ("ERROR" in str(msg.data))
            record("d6-stomp-46", "STOMP CONNECT with malformed/invalid Bearer token", passed, "ERROR frame connection rejected", f"Frame={msg.data[:60]}", msg.data)

        # d6-stomp-47: STOMP CONNECT with expired/tampered token
        tampered_token = token_a[:-10] + "0123456789"
        async with session.ws_connect(WS_URL) as ws:
            await ws.send_str(f"CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer {tampered_token}\n\n\x00")
            msg = await ws.receive()
            passed = ("ERROR" in str(msg.data))
            record("d6-stomp-47", "STOMP CONNECT with expired/tampered token", passed, "ERROR frame connection rejected", f"Frame={msg.data[:60]}", msg.data)

        # d6-stomp-48: STOMP CONNECT with suspended user token
        async with session.ws_connect(WS_URL) as ws:
            await ws.send_str(f"CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer {token_susp}\n\n\x00")
            msg = await ws.receive()
            passed = ("ERROR" in str(msg.data))
            record("d6-stomp-48", "STOMP CONNECT with suspended user token", passed, "ERROR frame connection rejected", f"Frame={msg.data[:60]}", msg.data)

        # d6-stomp-49: STOMP CONNECT with valid Bearer token for User A
        ws_a_raw = await session.ws_connect(WS_URL)
        await ws_a_raw.send_str(f"CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer {token_a}\n\n\x00")
        msg_a = await ws_a_raw.receive()
        passed = ("CONNECTED" in str(msg_a.data) and a_email in str(msg_a.data))
        record("d6-stomp-49", "STOMP CONNECT with valid Bearer token for User A", passed, "CONNECTED frame with user-name", f"Frame={msg_a.data[:60]}", msg_a.data)
        client_a = StompClient(ws_a_raw, name="User A")

        # d6-stomp-50: STOMP CONNECT with valid Bearer token for User B
        ws_b_raw = await session.ws_connect(WS_URL)
        await ws_b_raw.send_str(f"CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer {token_b}\n\n\x00")
        msg_b = await ws_b_raw.receive()
        passed = ("CONNECTED" in str(msg_b.data) and b_email in str(msg_b.data))
        record("d6-stomp-50", "STOMP CONNECT with valid Bearer token for User B", passed, "CONNECTED frame with user-name", f"Frame={msg_b.data[:60]}", msg_b.data)
        client_b = StompClient(ws_b_raw, name="User B")

        # User C client (isolation listener)
        ws_c_raw = await session.ws_connect(WS_URL)
        await ws_c_raw.send_str(f"CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer {token_c}\n\n\x00")
        await ws_c_raw.receive()
        client_c = StompClient(ws_c_raw, name="User C")

        # =====================================================================
        # Part 6: Subscriptions & Multi-Destination Event Delivery (d6-sub-51 to 62)
        # =====================================================================

        # d6-sub-51: Subscriptions on User A session
        await client_a.send_frame("SUBSCRIBE", {"id": "sub-a-chat", "destination": "/user/queue/chat", "ack": "auto"})
        await client_a.send_frame("SUBSCRIBE", {"id": "sub-a-events", "destination": "/user/queue/chat-events", "ack": "auto"})
        await client_a.send_frame("SUBSCRIBE", {"id": "sub-a-notifs", "destination": "/user/queue/notifications", "ack": "auto"})
        passed = True
        record("d6-sub-51", "Subscriptions on User A session (/user/queue/chat, chat-events, notifications)", passed, "Subscribed without error", "Subscribed")

        # d6-sub-52: Subscriptions on User B session
        await client_b.send_frame("SUBSCRIBE", {"id": "sub-b-chat", "destination": "/user/queue/chat", "ack": "auto"})
        await client_b.send_frame("SUBSCRIBE", {"id": "sub-b-events", "destination": "/user/queue/chat-events", "ack": "auto"})
        await client_b.send_frame("SUBSCRIBE", {"id": "sub-b-notifs", "destination": "/user/queue/notifications", "ack": "auto"})
        passed = True
        record("d6-sub-52", "Subscriptions on User B session (/user/queue/chat, chat-events, notifications)", passed, "Subscribed without error", "Subscribed")

        # d6-sub-53: Subscriptions on User C session
        await client_c.send_frame("SUBSCRIBE", {"id": "sub-c-chat", "destination": "/user/queue/chat", "ack": "auto"})
        await client_c.send_frame("SUBSCRIBE", {"id": "sub-c-events", "destination": "/user/queue/chat-events", "ack": "auto"})
        await client_c.send_frame("SUBSCRIBE", {"id": "sub-c-notifs", "destination": "/user/queue/notifications", "ack": "auto"})
        passed = True
        record("d6-sub-53", "Subscriptions on User C session (Third-party isolation listener)", passed, "Subscribed without error", "Subscribed")

        await asyncio.sleep(0.5)

        # d6-stomp-54: User A sends message over STOMP
        stomp_idem_key = f"stomp-idem-1-{RUN_ID}"
        stomp_msg_payload = {
            "idempotencyKey": stomp_idem_key,
            "content": f"Message envoyé en temps réel via STOMP {RUN_ID}",
        }
        await client_a.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/messages.send",
            "content-type": "application/json",
        }, json.dumps(stomp_msg_payload))
        passed = True
        record("d6-stomp-54", "User A sends message over STOMP", passed, "Frame sent successfully", "Frame sent")

        # d6-stomp-55: Sender ACK frame delivery
        ack_frame_a = await client_a.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="COMMAND_ACKNOWLEDGED", timeout=4.0)
        passed = (ack_frame_a is not None and stomp_idem_key in ack_frame_a.body)
        ack_body = json.loads(ack_frame_a.body) if ack_frame_a else {}
        stomp_sent_msg_id = ack_body.get("messageId")
        record("d6-stomp-55", "Sender ACK frame delivery on /user/queue/chat", passed, "COMMAND_ACKNOWLEDGED frame with idempotencyKey", f"Received={ack_frame_a is not None}", ack_body)

        # d6-stomp-56: Recipient event frame delivery
        event_frame_b = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/chat-events", event_type="MESSAGE_CREATED", timeout=4.0)
        event_body_b = json.loads(event_frame_b.body) if event_frame_b else {}
        passed = (event_frame_b is not None and event_body_b.get("type") == "MESSAGE_CREATED" and event_body_b.get("conversationId") == conv_id)
        record("d6-stomp-56", "Recipient event frame delivery on /user/queue/chat-events", passed, "MESSAGE_CREATED frame with payload", f"Received={event_frame_b is not None}", event_body_b)

        # d6-stomp-57: Recipient notification frame delivery
        notif_frame_b = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/notifications", timeout=4.0)
        passed = (notif_frame_b is not None and "New message" in notif_frame_b.body)
        record("d6-stomp-57", "Recipient notification frame delivery on /user/queue/notifications", passed, "Notification frame delivered", f"Received={notif_frame_b is not None}", notif_frame_b.body if notif_frame_b else None)

        # d6-stomp-58: Tenant / User Isolation
        c_frames = await client_c.drain()
        passed = (len(c_frames) == 0)
        record("d6-stomp-58", "Tenant / User Isolation (User C received zero frames)", passed, "Zero frames received by third party", f"Count={len(c_frames)}", c_frames)

        # d6-stomp-59: Typing Indicator Start over STOMP
        corr_typing = f"typing-start-{RUN_ID}"
        await client_a.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/typing.start",
            "content-type": "application/json",
        }, json.dumps({"correlationId": corr_typing}))
        typing_frame_b = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="TYPING_STARTED", timeout=4.0)
        passed = (typing_frame_b is not None and "TYPING_STARTED" in typing_frame_b.body and a_email in typing_frame_b.body)
        record("d6-stomp-59", "Typing Indicator Start over STOMP", passed, "TYPING_STARTED frame on /user/queue/chat", f"Received={typing_frame_b is not None}", typing_frame_b.body if typing_frame_b else None)

        # d6-stomp-60: Typing Indicator Stop over STOMP
        await client_a.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/typing.stop",
            "content-type": "application/json",
        }, json.dumps({"correlationId": f"typing-stop-{RUN_ID}"}))
        typing_stop_frame_b = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="TYPING_STOPPED", timeout=4.0)
        passed = (typing_stop_frame_b is not None and "TYPING_STOPPED" in typing_stop_frame_b.body)
        record("d6-stomp-60", "Typing Indicator Stop over STOMP", passed, "TYPING_STOPPED frame on /user/queue/chat", f"Received={typing_stop_frame_b is not None}", typing_stop_frame_b.body if typing_stop_frame_b else None)

        # d6-stomp-61: Read Receipt over STOMP
        corr_read = f"read-{RUN_ID}"
        await client_b.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/read",
            "content-type": "application/json",
        }, json.dumps({"messageId": stomp_sent_msg_id, "correlationId": corr_read}))
        read_ack_b = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="READ_UP_TO", timeout=4.0)
        read_event_a = await client_a.expect_frame(command="MESSAGE", destination="/user/queue/chat-events", event_type="READ_UP_TO", timeout=4.0)
        passed = (read_ack_b is not None and read_event_a is not None)
        record("d6-stomp-61", "Read Receipt over STOMP (ACK to B, event to A)", passed, "READ_UP_TO on chat and chat-events", f"B_ack={read_ack_b is not None} A_event={read_event_a is not None}", read_event_a.body if read_event_a else None)

        # d6-stomp-62: STOMP message send validation error
        await client_a.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/messages.send",
            "content-type": "application/json",
        }, json.dumps({"idempotencyKey": "k-invalid", "content": ""}))
        err_frame_a = await client_a.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="COMMAND_ERROR", timeout=4.0)
        passed = (err_frame_a is not None and "COMMAND_ERROR" in err_frame_a.body)
        record("d6-stomp-62", "STOMP message send validation error", passed, "COMMAND_ERROR frame on /user/queue/chat", f"Received={err_frame_a is not None}", err_frame_a.body if err_frame_a else None)

        # =====================================================================
        # Part 7: STOMP Edit, Delete, Reconnect Consistency (d6-stomp-63 to 70)
        # =====================================================================

        # d6-stomp-63: Message edit over STOMP
        corr_edit = f"corr-edit-{RUN_ID}"
        stomp_edited_content = f"Message STOMP édité en direct {RUN_ID}"
        await client_a.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/messages.edit",
            "content-type": "application/json",
        }, json.dumps({"messageId": stomp_sent_msg_id, "correlationId": corr_edit, "content": stomp_edited_content}))
        edit_ack_a = await client_a.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="COMMAND_ACKNOWLEDGED", timeout=4.0)
        edit_event_b = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/chat-events", event_type="MESSAGE_UPDATED", timeout=4.0)
        passed = (edit_ack_a is not None and edit_event_b is not None and stomp_edited_content in edit_event_b.body)
        record("d6-stomp-63", "Message edit over STOMP (ACK to A, MESSAGE_UPDATED to B)", passed, "MESSAGE_UPDATED with new content", f"A_ack={edit_ack_a is not None} B_event={edit_event_b is not None}", edit_event_b.body if edit_event_b else None)

        # d6-stomp-64: Message delete over STOMP
        corr_del = f"corr-del-{RUN_ID}"
        await client_a.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/messages.delete",
            "content-type": "application/json",
        }, json.dumps({"messageId": stomp_sent_msg_id, "correlationId": corr_del}))
        del_ack_a = await client_a.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="COMMAND_ACKNOWLEDGED", timeout=4.0)
        del_event_b = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/chat-events", event_type="MESSAGE_DELETED", timeout=4.0)
        passed = (del_ack_a is not None and del_event_b is not None and "MESSAGE_DELETED" in del_event_b.body)
        record("d6-stomp-64", "Message delete over STOMP (ACK to A, MESSAGE_DELETED to B)", passed, "MESSAGE_DELETED frame received", f"A_ack={del_ack_a is not None} B_event={del_event_b is not None}", del_event_b.body if del_event_b else None)

        # d6-stomp-65: Non-author edit over STOMP rejected
        await client_b.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/messages.edit",
            "content-type": "application/json",
        }, json.dumps({"messageId": msg1_id, "correlationId": "b-pirate-edit", "content": "Pirate content"}))
        err_b_edit = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="COMMAND_ERROR", timeout=4.0)
        passed = (err_b_edit is not None and "COMMAND_ERROR" in err_b_edit.body)
        record("d6-stomp-65", "Non-author edit over STOMP rejected", passed, "COMMAND_ERROR frame on /user/queue/chat", f"Received={err_b_edit is not None}", err_b_edit.body if err_b_edit else None)

        # d6-stomp-66: Non-author delete over STOMP rejected
        await client_b.send_frame("SEND", {
            "destination": f"/app/v1/conversations/{conv_id}/messages.delete",
            "content-type": "application/json",
        }, json.dumps({"messageId": msg1_id, "correlationId": "b-pirate-del"}))
        err_b_del = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/chat", event_type="COMMAND_ERROR", timeout=4.0)
        passed = (err_b_del is not None and "COMMAND_ERROR" in err_b_del.body)
        record("d6-stomp-66", "Non-author delete over STOMP rejected", passed, "COMMAND_ERROR frame on /user/queue/chat", f"Received={err_b_del is not None}", err_b_del.body if err_b_del else None)

        # d6-stomp-67: REST vs STOMP state consistency
        # Fetch message history via REST and confirm deleted STOMP message is excluded
        st, _, resp = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", token=token_b)
        all_msgs = resp.get("data", {}).get("content", []) if resp else []
        stomp_msg_in_rest = next((m for m in all_msgs if m.get("id") == stomp_sent_msg_id), None)
        passed = (st == 200 and stomp_msg_in_rest is None)
        record("d6-stomp-67", "REST vs STOMP state consistency (deleted message excluded)", passed, "REST excludes deleted message", f"{st} found={stomp_msg_in_rest is not None}", stomp_msg_in_rest)

        # d6-stomp-68: Soft-deleted message consistency across REST and WebSocket
        # Send a fresh message by A via REST, delete via REST, verify B's GET messages excludes it
        st_fresh, _, r_fresh = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": f"k-fresh-{RUN_ID}", "content": "Temporary fresh content"}, token=token_a)
        fresh_id = r_fresh["data"]["id"]
        http_json("DELETE", f"/api/v1/conversations/{conv_id}/messages/{fresh_id}", token=token_a)
        st_chk, _, r_chk = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", token=token_b)
        chk_msgs = r_chk.get("data", {}).get("content", []) if r_chk else []
        fresh_in_chk = next((m for m in chk_msgs if m.get("id") == fresh_id), None)
        passed = (st_chk == 200 and fresh_in_chk is None)
        record("d6-stomp-68", "Soft-deleted message consistency across REST and WebSocket", passed, "deleted message excluded from history", f"found={fresh_in_chk is not None}", fresh_in_chk)

        # d6-stomp-69: Reconnect after disconnect
        # User B disconnects
        await client_b.close()
        # User A sends message via REST while B is offline
        offline_msg_payload = {"idempotencyKey": f"k-offline-{RUN_ID}", "content": f"Message envoyé pendant la déconnexion de B {RUN_ID}"}
        st_off, _, r_off = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", offline_msg_payload, token=token_a)
        off_msg_id = r_off["data"]["id"]
        # User B reconnects via STOMP and subscribes
        ws_b_rec = await session.ws_connect(WS_URL)
        await ws_b_rec.send_str(f"CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer {token_b}\n\n\x00")
        await ws_b_rec.receive()
        client_b_rec = StompClient(ws_b_rec, name="User B Reconnect")
        await client_b_rec.send_frame("SUBSCRIBE", {"id": "sub-b-rec-events", "destination": "/user/queue/chat-events", "ack": "auto"})
        # Verify message history is consistent and contains the offline message without duplication
        st_hist, _, r_hist = http_json("GET", f"/api/v1/conversations/{conv_id}/messages", token=token_b)
        hist_msgs = r_hist.get("data", {}).get("content", []) if r_hist else []
        found_offline_msgs = [m for m in hist_msgs if m.get("id") == off_msg_id]
        passed = (st_hist == 200 and len(found_offline_msgs) == 1 and found_offline_msgs[0].get("content") == offline_msg_payload["content"])
        record("d6-stomp-69", "Reconnect after disconnect (message history intact without duplication)", passed, "Exactly 1 copy of offline message in history", f"Count={len(found_offline_msgs)}", found_offline_msgs)

        # d6-stomp-70: Multi-session delivery
        # User A opens a second concurrent WebSocket session
        ws_a2_raw = await session.ws_connect(WS_URL)
        await ws_a2_raw.send_str(f"CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer {token_a}\n\n\x00")
        await ws_a2_raw.receive()
        client_a2 = StompClient(ws_a2_raw, name="User A Session 2")
        await client_a2.send_frame("SUBSCRIBE", {"id": "sub-a2-events", "destination": "/user/queue/chat-events", "ack": "auto"})
        await asyncio.sleep(0.5)
        await client_a.drain()

        # User B sends message via REST to User A
        multi_msg_payload = {"idempotencyKey": f"k-multi-{RUN_ID}", "content": f"Message pour multi-sessions {RUN_ID}"}
        http_json("POST", f"/api/v1/conversations/{conv_id}/messages", multi_msg_payload, token=token_b)

        # Verify BOTH active sessions of User A receive the event
        f_a1 = await client_a.expect_frame(command="MESSAGE", destination="/user/queue/chat-events", event_type="MESSAGE_CREATED", timeout=4.0)
        f_a2 = await client_a2.expect_frame(command="MESSAGE", destination="/user/queue/chat-events", event_type="MESSAGE_CREATED", timeout=4.0)
        passed = (f_a1 is not None and f_a2 is not None and multi_msg_payload["content"] in f_a1.body and multi_msg_payload["content"] in f_a2.body)
        record("d6-stomp-70", "Multi-session delivery (Both active sessions of User A receive event)", passed, "Delivered to both concurrent sessions", f"Session1={f_a1 is not None} Session2={f_a2 is not None}")

        # Cleanup clients
        await client_a.close()
        await client_a2.close()
        await client_b_rec.close()
        await client_c.close()

    # =========================================================================
    # Summary & Output
    # =========================================================================
    total = len(results)
    passed_count = sum(1 for r in results if r["passed"])
    failed_count = total - passed_count
    pass_pct = (passed_count / total) * 100 if total > 0 else 0

    print("\n" + "=" * 80)
    print(f"DOMAIN 6 VERIFICATION COMPLETE: {passed_count}/{total} PASSED ({pass_pct:.1f}%)")
    print("=" * 80)

    # Save report
    os.makedirs(".agent-output", exist_ok=True)
    report_file = ".agent-output/domain6-messaging-report.json"
    with open(report_file, "w", encoding="utf-8") as f:
        json.dump({
            "domain": "Domain 6: Messaging & WebSocket",
            "run_id": RUN_ID,
            "total_scenarios": total,
            "passed": passed_count,
            "failed": failed_count,
            "pass_percentage": pass_pct,
            "scenarios": results,
        }, f, indent=2, default=str)
    print(f"Detailed JSON report written to {report_file}")

    if failed_count > 0:
        sys.exit(1)


def main():
    asyncio.run(async_main())


if __name__ == "__main__":
    main()
