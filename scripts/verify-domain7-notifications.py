#!/usr/bin/env python3
"""
Souklab Backend Exhaustive Verification - Domain 7: Notifications
Tests real HTTP endpoints and STOMP WebSocket delivery against the running instance.
"""

import sys
import os
import json
import time
import uuid
import asyncio
import urllib.request
import urllib.error
import urllib.parse
import subprocess
import aiohttp

BASE_URL = os.environ.get("SOUKLAB_BASE_URL", "http://127.0.0.1:8080")
WS_URL = os.environ.get("SOUKLAB_WS_URL", "ws://127.0.0.1:8080/ws/websocket")
DB_NAME = os.environ.get("VERIFY_DB_NAME", "souklab_test")
DB_USER = os.environ.get("VERIFY_DB_USERNAME", "souklab_test")
DB_PASS = os.environ.get("VERIFY_DB_PASSWORD", "souklab_test_password")
DB_PORT = os.environ.get("VERIFY_DB_PORT", "3307")
OUTPUT_DIR = ".agent-output"
REPORT_FILE = os.path.join(OUTPUT_DIR, "domain7-notifications-report.json")

RUN_ID = f"d7-{int(time.time())}"

results = []

def record(scenario_id, description, passed, expected, actual, payload=None):
    entry = {
        "scenario_id": scenario_id,
        "description": description,
        "passed": bool(passed),
        "expected": str(expected),
        "actual": str(actual),
        "payload": payload,
    }
    results.append(entry)
    status = "PASS" if passed else "FAIL"
    print(f"[{status}] {scenario_id}: {description} | Expected: {expected} | Actual: {actual}")
    if not passed:
        print(f"  FAILED RESPONSE BODY: {json.dumps(payload, indent=2) if isinstance(payload, dict) else payload}")
        print("\nStopping immediately per verification protocol.")
        save_report()
        sys.exit(1)

def save_report():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    summary = {
        "domain": "Domain 7: Notifications",
        "run_id": RUN_ID,
        "total_scenarios": len(results),
        "passed": sum(1 for r in results if r["passed"]),
        "failed": sum(1 for r in results if not r["passed"]),
        "pass_percentage": round(sum(1 for r in results if r["passed"]) / max(1, len(results)) * 100, 2),
        "scenarios": results,
    }
    with open(REPORT_FILE, "w", encoding="utf-8") as f:
        json.dump(summary, f, indent=2)
    print(f"\nDetailed JSON report written to {REPORT_FILE}")

def http_json(method, path, body=None, token=None, query_params=None):
    url = BASE_URL + path
    if query_params:
        url += "?" + urllib.parse.urlencode(query_params)
    data = json.dumps(body).encode("utf-8") if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            raw = resp.read().decode("utf-8")
            return resp.status, resp.headers, json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        try:
            return e.code, e.headers, json.loads(raw) if raw else None
        except Exception:
            return e.code, e.headers, raw
    except Exception as e:
        return 0, {}, str(e)

def db_execute(sql: str) -> str:
    cmd = [
        "mariadb",
        "-h", "127.0.0.1",
        "-P", DB_PORT,
        "-u", DB_USER,
        f"-p{DB_PASS}",
        "-D", DB_NAME,
        "-e", sql,
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, check=True)
    return res.stdout.strip()

def register_user(email, password, account_type="CLIENT", name="Test User"):
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

    # Directly verify in DB to ensure immediate eligibility
    db_execute(f"UPDATE users SET email_verified = 1, status = 'ACTIVE' WHERE email = '{email}';")
    out = db_execute(f"SELECT id FROM users WHERE email = '{email}'")
    lines = out.splitlines()
    uid = lines[1].strip() if len(lines) > 1 else lines[0].strip()

    # Login to acquire fresh JWT
    st_l, _, resp_l = http_json("POST", "/api/v1/auth/login", {"email": email, "password": password})
    if st_l != 200 or not resp_l.get("success"):
        raise RuntimeError(f"Failed to login {email}: {st_l} {resp_l}")
    token = resp_l["data"]["accessToken"]
    return token, uid


# =============================================================================
# STOMP Helpers
# =============================================================================
class StompFrame:
    def __init__(self, command: str, headers: dict[str, str], body: str = ""):
        self.command = command
        self.headers = headers
        self.body = body

    @classmethod
    def parse(cls, raw: str) -> list["StompFrame"]:
        frames = []
        raw_frames = raw.split("\x00")
        for chunk in raw_frames:
            chunk = chunk.strip("\r\n")
            if not chunk:
                continue
            parts = chunk.split("\n\n", 1)
            if len(parts) < 2:
                parts = chunk.split("\r\n\r\n", 1)
            head_lines = parts[0].splitlines()
            if not head_lines:
                continue
            command = head_lines[0].strip()
            headers = {}
            for line in head_lines[1:]:
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

    def _matches(self, frame: StompFrame, command: str | None, destination: str | None, text_content: str | None) -> bool:
        if command and frame.command != command:
            return False
        if destination and frame.headers.get("destination") != destination:
            return False
        if text_content and text_content not in frame.body:
            return False
        return True

    async def expect_frame(
        self,
        command: str | None = None,
        destination: str | None = None,
        text_content: str | None = None,
        timeout: float = 4.0,
    ) -> StompFrame | None:
        start_time = time.time()
        while time.time() - start_time < timeout:
            for idx, frame in enumerate(self.received_frames):
                if self._matches(frame, command, destination, text_content):
                    return self.received_frames.pop(idx)
            self._event.clear()
            remaining = max(0.05, timeout - (time.time() - start_time))
            try:
                await asyncio.wait_for(self._event.wait(), timeout=remaining)
            except asyncio.TimeoutError:
                break
        for idx, frame in enumerate(self.received_frames):
            if self._matches(frame, command, destination, text_content):
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
# Main Verification Execution
# =============================================================================
async def main():
    print(f"=== Domain 7 Live Verification: Notifications (RUN_ID: {RUN_ID}) ===")

    # Setup accounts
    a_email = f"d7_user_a_{RUN_ID}@example.com"
    b_email = f"d7_user_b_{RUN_ID}@example.com"
    c_email = f"d7_user_c_{RUN_ID}@example.com"
    susp_email = f"d7_user_susp_{RUN_ID}@example.com"

    token_a, id_a = register_user(a_email, "Password123!", "CLIENT", "User Alpha")
    token_b, id_b = register_user(b_email, "Password123!", "CLIENT", "User Bravo")
    token_c, id_c = register_user(c_email, "Password123!", "CLIENT", "User Charlie")
    token_susp, id_susp = register_user(susp_email, "Password123!", "CLIENT", "User Suspended")
    db_execute(f"UPDATE users SET status = 'SUSPENDED' WHERE id = '{id_susp}';")

    print(f"✓ Accounts setup: User A ({id_a}), User B ({id_b}), User C ({id_c}), Suspended ({id_susp})")

    # =========================================================================
    # Part 1: Authentication & Baseline (d7-notif-01 to 07)
    # =========================================================================

    # d7-notif-01: Anonymous GET /api/v1/notifications
    st, _, resp = http_json("GET", "/api/v1/notifications")
    passed = (st == 401)
    record("d7-notif-01", "Anonymous GET /api/v1/notifications rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d7-notif-02: Anonymous GET /api/v1/notifications/unread-count
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count")
    passed = (st == 401)
    record("d7-notif-02", "Anonymous GET /api/v1/notifications/unread-count rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d7-notif-03: Anonymous PUT /api/v1/notifications/{id}/read
    st, _, resp = http_json("PUT", "/api/v1/notifications/dummy-id/read")
    passed = (st == 401)
    record("d7-notif-03", "Anonymous PUT /api/v1/notifications/{id}/read rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d7-notif-04: Anonymous PUT /api/v1/notifications/read-all
    st, _, resp = http_json("PUT", "/api/v1/notifications/read-all")
    passed = (st == 401)
    record("d7-notif-04", "Anonymous PUT /api/v1/notifications/read-all rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d7-notif-05: Anonymous DELETE /api/v1/notifications/{id}
    st, _, resp = http_json("DELETE", "/api/v1/notifications/dummy-id")
    passed = (st == 401)
    record("d7-notif-05", "Anonymous DELETE /api/v1/notifications/{id} rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d7-notif-06: Fresh user unread count baseline (raw integer 0)
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count", token=token_b)
    passed = (st == 200 and resp.get("success") is True and resp.get("data") == 0)
    record("d7-notif-06", "Fresh user unread count baseline (raw integer 0)", passed, "200 OK data=0", f"{st} data={resp.get('data')}", resp)

    # d7-notif-07: Fresh user empty notification list
    st, _, resp = http_json("GET", "/api/v1/notifications", token=token_b)
    paginated = resp.get("data", {}) if resp else {}
    passed = (st == 200 and resp.get("success") is True and paginated.get("totalElements") == 0 and len(paginated.get("content", [])) == 0)
    record("d7-notif-07", "Fresh user empty notification list", passed, "200 OK totalElements=0", f"{st} count={paginated.get('totalElements')}", resp)

    # =========================================================================
    # Part 2: Triggering Notifications & WebSocket Push (d7-notif-08 to 13)
    # =========================================================================

    # Setup WebSocket listener for User B on /user/queue/notifications
    async with aiohttp.ClientSession() as session:
        ws_b = await session.ws_connect(WS_URL)
        await ws_b.send_str(f"CONNECT\naccept-version:1.1,1.2\nAuthorization:Bearer {token_b}\n\n\x00")
        await ws_b.receive()
        client_b = StompClient(ws_b, name="User B Notifications")
        await client_b.send_frame("SUBSCRIBE", {"id": "sub-b-notifs", "destination": "/user/queue/notifications", "ack": "auto"})
        await asyncio.sleep(0.3)

        # d7-notif-08: Create conversation between User A and User B
        st_c, _, resp_c = http_json("POST", "/api/v1/conversations", {"recipientUserId": id_b}, token=token_a)
        conv_id = resp_c["data"]["id"]
        passed = (st_c == 201 and bool(conv_id))
        record("d7-notif-08", "Setup conversation A-B for notification triggers", passed, "201 CREATED", f"{st_c} id={conv_id}", resp_c)

        # d7-notif-09: User A sends message 1 to User B -> triggers notification
        msg1_key = f"k-notif-1-{RUN_ID}"
        st_m1, _, resp_m1 = http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {
            "idempotencyKey": msg1_key,
            "content": f"Notification trigger 1 {RUN_ID}",
        }, token=token_a)
        passed = (st_m1 == 201)
        record("d7-notif-09", "User A sends message 1 to trigger notification", passed, "201 CREATED", f"{st_m1}", resp_m1)

        # d7-notif-10: Live STOMP frame received by User B
        frame_notif = await client_b.expect_frame(command="MESSAGE", destination="/user/queue/notifications", timeout=4.0)
        passed = (frame_notif is not None and "New message from" in frame_notif.body)
        frame_body = json.loads(frame_notif.body) if frame_notif else {}
        record("d7-notif-10", "Live STOMP notification push on /user/queue/notifications", passed, "MESSAGE frame on /user/queue/notifications", f"Received={frame_notif is not None}", frame_body)

        await client_b.close()

    # d7-notif-11: User B unread count incremented to 1
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count", token=token_b)
    passed = (st == 200 and resp.get("data") == 1)
    record("d7-notif-11", "User B unread count incremented to 1", passed, "200 OK data=1", f"{st} data={resp.get('data')}", resp)

    # d7-notif-12: User B lists notifications (1 item, isRead=false, targetId=conv_id)
    st, _, resp = http_json("GET", "/api/v1/notifications", token=token_b)
    p_data = resp.get("data", {}) if resp else {}
    items = p_data.get("content", [])
    notif1 = items[0] if items else {}
    notif1_id = notif1.get("id")
    is_read_flag = notif1.get("read") if "read" in notif1 else notif1.get("isRead")
    passed = (st == 200 and len(items) == 1 and is_read_flag is False and notif1.get("targetId") == conv_id)
    record("d7-notif-12", "User B notification list details (read=false, targetId match)", passed, "200 OK 1 unread notification", f"{st} count={len(items)} read={is_read_flag}", notif1)

    # d7-notif-13: Isolation - User A lists notifications (must be 0)
    st, _, resp = http_json("GET", "/api/v1/notifications", token=token_a)
    p_a = resp.get("data", {}) if resp else {}
    passed = (st == 200 and p_a.get("totalElements") == 0)
    record("d7-notif-13", "User A isolation (sender receives zero notifications)", passed, "200 OK totalElements=0", f"{st} count={p_a.get('totalElements')}", resp)

    # d7-notif-14: Isolation - User C unread count is 0
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count", token=token_c)
    passed = (st == 200 and resp.get("data") == 0)
    record("d7-notif-14", "User C isolation (third party unread count is 0)", passed, "200 OK data=0", f"{st} data={resp.get('data')}", resp)

    # =========================================================================
    # Part 3: Multiple Notifications & Pagination (d7-notif-15 to 20)
    # =========================================================================

    # Trigger message 2 and 3
    http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": f"k-notif-2-{RUN_ID}", "content": "Trigger 2"}, token=token_a)
    time.sleep(0.05)
    http_json("POST", f"/api/v1/conversations/{conv_id}/messages", {"idempotencyKey": f"k-notif-3-{RUN_ID}", "content": "Trigger 3"}, token=token_a)

    # d7-notif-15: Unread count reaches 3
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count", token=token_b)
    passed = (st == 200 and resp.get("data") == 3)
    record("d7-notif-15", "Unread count increments to 3 after multiple triggers", passed, "200 OK data=3", f"{st} data={resp.get('data')}", resp)

    # d7-notif-16: Pagination size=2, page=0
    st, _, resp = http_json("GET", "/api/v1/notifications", query_params={"page": 0, "size": 2}, token=token_b)
    p_page0 = resp.get("data", {}) if resp else {}
    items_p0 = p_page0.get("content", [])
    passed = (st == 200 and len(items_p0) == 2 and p_page0.get("totalElements") == 3 and p_page0.get("totalPages") == 2 and p_page0.get("pageNumber") == 0)
    record("d7-notif-16", "Paginated notification list (page=0, size=2)", passed, "200 OK 2 items totalElements=3 totalPages=2", f"{st} count={len(items_p0)}", p_page0)

    # d7-notif-17: Pagination page=1, size=2
    st, _, resp = http_json("GET", "/api/v1/notifications", query_params={"page": 1, "size": 2}, token=token_b)
    p_page1 = resp.get("data", {}) if resp else {}
    items_p1 = p_page1.get("content", [])
    passed = (st == 200 and len(items_p1) == 1 and p_page1.get("pageNumber") == 1)
    record("d7-notif-17", "Paginated notification list (page=1, size=2)", passed, "200 OK 1 item on last page", f"{st} count={len(items_p1)}", p_page1)

    # d7-notif-18: Non-overlapping pages
    ids_p0 = {n["id"] for n in items_p0}
    ids_p1 = {n["id"] for n in items_p1}
    passed = (len(ids_p0.intersection(ids_p1)) == 0)
    record("d7-notif-18", "Non-overlapping paginated items", passed, "Zero overlap between pages", f"Overlap={len(ids_p0.intersection(ids_p1))}", {"page0": list(ids_p0), "page1": list(ids_p1)})

    # d7-notif-19: Sort order verification (newest first)
    st, _, resp = http_json("GET", "/api/v1/notifications", query_params={"size": 10}, token=token_b)
    all_notifs = resp.get("data", {}).get("content", []) if resp else []
    created_dates = [n.get("createdAt") for n in all_notifs if n.get("createdAt")]
    is_sorted_desc = all(created_dates[i] >= created_dates[i+1] for i in range(len(created_dates)-1))
    passed = (st == 200 and len(all_notifs) == 3 and is_sorted_desc)
    record("d7-notif-19", "Notification ordering (newest first)", passed, "200 OK descending createdAt", f"{st} sorted={is_sorted_desc}", created_dates)

    # Keep references to notification IDs
    # all_notifs is newest-first: index 0 is newest, index 2 is oldest (notif1)
    target_notif = all_notifs[0] # newest
    target_notif_id = target_notif["id"]

    # =========================================================================
    # Part 4: Mark As Read & Security Conventions (d7-notif-20 to 26)
    # =========================================================================

    # d7-notif-20: Mark non-existent notification read -> 404
    st, _, resp = http_json("PUT", "/api/v1/notifications/00000000-0000-0000-0000-000000000000/read", token=token_b)
    passed = (st == 404 and "Notification not found" in str(resp.get("message", "")))
    record("d7-notif-20", "Mark non-existent notification read rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d7-notif-21: Cross-user read rejection (anti-IDOR 404 convention)
    st, _, resp = http_json("PUT", f"/api/v1/notifications/{target_notif_id}/read", token=token_a)
    passed = (st == 404 and "Notification not found" in str(resp.get("message", "")))
    record("d7-notif-21", "Cross-user mark as read rejection (404 convention)", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d7-notif-22: User B marks notification as read
    st, _, resp = http_json("PUT", f"/api/v1/notifications/{target_notif_id}/read", token=token_b)
    updated_notif = resp.get("data", {}) if resp else {}
    up_read = updated_notif.get("read") if "read" in updated_notif else updated_notif.get("isRead")
    passed = (st == 200 and resp.get("success") is True and up_read is True and updated_notif.get("id") == target_notif_id)
    record("d7-notif-22", "User B marks target notification as read", passed, "200 OK read=true", f"{st} read={up_read}", updated_notif)

    # d7-notif-23: Idempotent mark as read
    st, _, resp = http_json("PUT", f"/api/v1/notifications/{target_notif_id}/read", token=token_b)
    up_read2 = resp.get("data", {}).get("read") if "read" in resp.get("data", {}) else resp.get("data", {}).get("isRead")
    passed = (st == 200 and up_read2 is True)
    record("d7-notif-23", "Idempotent mark as read on already-read notification", passed, "200 OK read=true", f"{st} read={up_read2}", resp)

    # d7-notif-24: Unread count decremented to 2
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count", token=token_b)
    passed = (st == 200 and resp.get("data") == 2)
    record("d7-notif-24", "Unread count decremented to 2", passed, "200 OK data=2", f"{st} data={resp.get('data')}", resp)

    # =========================================================================
    # Part 5: Deletion & Soft Delete Verification (d7-notif-25 to 30)
    # =========================================================================

    # Choose a second unread notification to delete
    del_target = all_notifs[1]
    del_target_id = del_target["id"]

    # d7-notif-25: Cross-user delete rejection (anti-IDOR 404 convention)
    st, _, resp = http_json("DELETE", f"/api/v1/notifications/{del_target_id}", token=token_a)
    passed = (st == 404 and "Notification not found" in str(resp.get("message", "")))
    record("d7-notif-25", "Cross-user delete notification rejection (404 convention)", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d7-notif-26: Delete non-existent notification
    st, _, resp = http_json("DELETE", "/api/v1/notifications/00000000-0000-0000-0000-000000000000", token=token_b)
    passed = (st == 404 and "Notification not found" in str(resp.get("message", "")))
    record("d7-notif-26", "Delete non-existent notification rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d7-notif-27: User B deletes notification
    st, _, resp = http_json("DELETE", f"/api/v1/notifications/{del_target_id}", token=token_b)
    passed = (st == 200 and resp.get("success") is True)
    record("d7-notif-27", "User B deletes unread notification", passed, "200 OK", f"{st}", resp)

    # d7-notif-28: Soft-deleted notification excluded from list
    st, _, resp = http_json("GET", "/api/v1/notifications", token=token_b)
    remaining_notifs = resp.get("data", {}).get("content", []) if resp else []
    found_del = next((n for n in remaining_notifs if n.get("id") == del_target_id), None)
    passed = (st == 200 and found_del is None and len(remaining_notifs) == 2)
    record("d7-notif-28", "Soft-deleted notification excluded from list", passed, "200 OK excluded from list count=2", f"{st} found={found_del is not None}", remaining_notifs)

    # d7-notif-29: Unread count reflects deleted unread notification (decreased to 1)
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count", token=token_b)
    passed = (st == 200 and resp.get("data") == 1)
    record("d7-notif-29", "Unread count reflects deleted unread notification (decreased to 1)", passed, "200 OK data=1", f"{st} data={resp.get('data')}", resp)

    # d7-notif-30: Second delete on already soft-deleted notification -> 404
    st, _, resp = http_json("DELETE", f"/api/v1/notifications/{del_target_id}", token=token_b)
    passed = (st == 404 and "Notification not found" in str(resp.get("message", "")))
    record("d7-notif-30", "Second delete on soft-deleted notification rejected", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d7-notif-31: Mark read on soft-deleted notification -> 404
    st, _, resp = http_json("PUT", f"/api/v1/notifications/{del_target_id}/read", token=token_b)
    passed = (st == 404 and "Notification not found" in str(resp.get("message", "")))
    record("d7-notif-31", "Mark read on soft-deleted notification rejected", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # =========================================================================
    # Part 6: Bulk Mark All As Read & Floor Stability (d7-notif-32 to 36)
    # =========================================================================

    # d7-notif-32: User B marks all as read
    st, _, resp = http_json("PUT", "/api/v1/notifications/read-all", token=token_b)
    passed = (st == 200 and resp.get("success") is True)
    record("d7-notif-32", "User B marks all notifications as read", passed, "200 OK", f"{st}", resp)

    # d7-notif-33: Unread count becomes 0
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count", token=token_b)
    passed = (st == 200 and resp.get("data") == 0)
    record("d7-notif-33", "Unread count becomes 0 after mark-all-as-read", passed, "200 OK data=0", f"{st} data={resp.get('data')}", resp)

    # d7-notif-34: Idempotent mark-all-as-read
    st, _, resp = http_json("PUT", "/api/v1/notifications/read-all", token=token_b)
    passed = (st == 200 and resp.get("success") is True)
    record("d7-notif-34", "Idempotent mark-all-as-read", passed, "200 OK", f"{st}", resp)

    # d7-notif-35: Unread count floor stability (never goes below 0)
    st, _, resp = http_json("GET", "/api/v1/notifications/unread-count", token=token_b)
    passed = (st == 200 and resp.get("data") == 0)
    record("d7-notif-35", "Unread count floor stability (data=0, never negative)", passed, "200 OK data=0", f"{st} data={resp.get('data')}", resp)

    # d7-notif-36: Suspended account access rejection
    st, _, resp = http_json("GET", "/api/v1/notifications", token=token_susp)
    passed = (st in (401, 403))
    record("d7-notif-36", "Suspended account notifications access rejection", passed, "401 UNAUTHORIZED or 403 FORBIDDEN", f"{st}", resp)

    # =========================================================================
    # Summary & Output
    # =========================================================================
    total = len(results)
    passed_count = sum(1 for r in results if r["passed"])
    failed_count = sum(1 for r in results if not r["passed"])
    print("\n" + "=" * 80)
    print(f"DOMAIN 7 VERIFICATION COMPLETE: {passed_count}/{total} PASSED ({passed_count/total*100:.1f}%)")
    print("=" * 80)

    save_report()

if __name__ == "__main__":
    asyncio.run(main())
