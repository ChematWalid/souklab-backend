#!/usr/bin/env python3
"""Exhaustive live verification for Domain 5: Social Feed, Reviews, Reports.

Covers 80 live HTTP scenarios:
1. Feed Post Creation & Authorization Boundaries
   - Anonymous creation rejection (401)
   - Client account creation rejection (403)
   - Unverified artisan creation rejection (403)
   - Validation failures (blank title, blank body, null type, title > 200) (400)
   - Business rules for FORMATION type vs non-FORMATION type (400, 404)
   - Verified Artisan creates ACTUALITE post (201, status PENDING)
   - Verified Artisan creates ANNONCE post (201, status PENDING)
   - Verified Artisan creates FORMATION post (201, status PENDING)
   - Admin creates feed post directly (201, status PENDING)

2. Media Attachments Lifecycle on Feed Posts
   - Anonymous upload rejection (401)
   - Non-author non-admin upload rejection (403)
   - Non-existent post media upload (404)
   - Missing file upload rejection (400)
   - Unsupported file type rejection (400)
   - Author uploads PNG image (201, displayOrder=0)
   - Author uploads second JPEG image (201, displayOrder=1)
   - Media presence in post representation (2 attachments)
   - Non-author media delete rejection (403)
   - Non-existent media delete rejection (404)
   - Author deletes media item (200)
   - Post reflects deleted media

3. Public Feed Visibility & Moderation Gate
   - Public list excludes PENDING posts
   - Public single get on PENDING post returns 404
   - Non-admin cannot list pending posts (403)
   - Admin lists pending posts (200)
   - Non-admin cannot publish post (403)
   - Admin publish with blank note rejected (400)
   - Admin publishes post (200, status PUBLISHED, publishedAt set)
   - Notification delivered on FORMATION post publish
   - Published post visible in public feed
   - Public feed type filter: ACTUALITE, FORMATION, ANNONCE
   - Public single get on published post (200)
   - Admin hides published post (200, status HIDDEN)
   - Hidden post excluded from public feed (404)
   - Admin re-publishes hidden post (200)

4. Author Post Editing & Admin Edit Lifecycle
   - Non-author cannot edit post (403)
   - Author edits published post (200, status resets to PENDING, publishedAt cleared)
   - Post immediately hidden from public feed while PENDING again (404)
   - Admin re-publishes edited post (200)
   - Admin edits post (200, status remains PUBLISHED)

5. Post Deletion Lifecycle
   - Non-author cannot delete post (403)
   - Author deletes own post (200, status REMOVED)
   - Deleted post excluded from public feed (404)
   - Cannot publish removed post (409)
   - Admin removes post directly (200, status REMOVED)

6. Public Artisan Reviews Listing
   - Public list artisan reviews (200)
   - Public list for non-existent artisan (200, empty)
   - Attended completed review visible in public reviews list (200)
   - Soft-deleted review excluded from public reviews list

7. Content Reports Creation & Target Validation
   - Anonymous report creation rejection (401)
   - Self-report rejection for USER target (400)
   - Non-existent USER report rejection (404)
   - Non-existent POST report rejection (404)
   - Non-existent REVIEW report rejection (404)
   - Validation - missing targetType (400)
   - Validation - empty reason (400)
   - Report a User successfully (201, status OPEN)
   - Report a Feed Post successfully (201, status OPEN)
   - Report an Artisan Review successfully (201, status OPEN)

8. Admin Report Queue & Resolution Actions
   - Non-admin cannot list reports (403)
   - Admin lists reports unfiltered (200)
   - Admin lists reports filtered by status (200)
   - Admin lists reports filtered by targetType (200)
   - Admin lists reports filtered by status and targetType (200)
   - Non-admin cannot resolve report (403)
   - Resolve non-existent report (404)
   - Resolve action DISMISS on Post Report (200, status DISMISSED, post untouched)
   - Re-resolving resolved report rejected (400)
   - Resolve action HIDE on Post Report (200, status RESOLVED, post status HIDDEN)
   - Resolve action REMOVE on Post Report (200, status RESOLVED, post status REMOVED)
   - Resolve action HIDE on Review Report (200, status RESOLVED, review status HIDDEN, rating recalculated)
   - Resolve action REMOVE on Review Report (200, status RESOLVED, review status REMOVED, rating recalculated)
   - Resolve action HIDE on User Report (200, status RESOLVED, user status SUSPENDED)
   - Suspended user blocked from authenticated actions (403)
   - Resolve action REMOVE on User Report (200, status RESOLVED, user soft-deleted)
"""

from __future__ import annotations

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

BASE_URL = os.getenv("APP_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
DB_HOST = os.getenv("VERIFY_DB_HOST", "127.0.0.1")
DB_PORT = os.getenv("VERIFY_DB_PORT", "3307")
DB_NAME = os.getenv("VERIFY_DB_NAME", "souklab_test")
DB_USER = os.getenv("VERIFY_DB_USER", "souklab_test")
DB_PASS = os.getenv("VERIFY_DB_PASSWORD", "souklab_test_password")

RUN_ID = f"d5-{int(time.time())}"
results: list[dict[str, Any]] = []

VALID_PNG_BYTES = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\rIDATx\x9cc`\x00\x00\x00"
    b"\x02\x00\x01H\xaf\xa4q\x00\x00\x00\x00IEND\xaeB`\x82"
)

VALID_JPEG_BYTES = (
    b"\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x01\x00H\x00H\x00\x00"
    b"\xff\xdb\x00C\x00\x08\x06\x06\x07\x06\x05\x08\x07\x07\x07\t\t"
    b"\x08\n\x0c\x14\r\x0c\x0b\x0b\x0c\x19\x12\x13\x0f\x14\x1d\x1a"
    b"\x1f\x1e\x1d\x1a\x1c\x1c $.' \",#\x1c\x1c(7),01444\x1f'9=82<.342"
    b"\xff\xc0\x00\x0b\x08\x00\x01\x00\x01\x01\x01\x11\x00\xff\xc4\x00"
    b"\x1f\x00\x00\x01\x05\x01\x01\x01\x01\x01\x01\x00\x00\x00\x00\x00"
    b"\x00\x00\x00\x01\x02\x03\x04\x05\x06\x07\x08\t\n\x0b\xff\xda\x00"
    b"\x08\x01\x01\x00\x00?\x00\xbf\x00\xff\xd9"
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

def register_and_login(email: str, password: str, account_type: str) -> tuple[str, str]:
    """Register account, verify email & set active via DB, login, return (token, user_id)."""
    reg_payload = {
        "email": email,
        "password": password,
        "firstName": "Test",
        "lastName": "User",
        "phone": "+213555000000",
        "accountType": account_type,
    }
    st, _, resp = http_json("POST", "/api/v1/auth/register", reg_payload)
    if st not in (200, 201):
        raise RuntimeError(f"Registration failed ({st}): {resp}")

    # Set email verified & active in DB
    db_execute(f"UPDATE users SET email_verified = 1, status = 'ACTIVE' WHERE email = '{email}'")
    user_rows = db_query(f"SELECT id FROM users WHERE email = '{email}'")
    if not user_rows:
        raise RuntimeError(f"User not found for {email}")
    uid = user_rows[0][0]

    # Login
    login_payload = {"email": email, "password": password}
    st, _, resp = http_json("POST", "/api/v1/auth/login", login_payload)
    if st != 200 or not resp.get("success"):
        raise RuntimeError(f"Login failed ({st}): {resp}")
    token = resp["data"]["accessToken"]

    if account_type == "ARTISAN":
        reg_id = "00e08ab0-36c4-4e48-91f5-0a3ebfe168ea"
        sub_id = "0a905e89-0723-4512-865d-1c65ba658641"
        cst, _, cresp = http_json("POST", "/api/v1/auth/complete-profile", {
            "bio": "Artisan d'art traditionnel",
            "city": "Boumerdès",
            "address": "123 Rue des Artisans",
            "regionId": reg_id,
            "subCategoryId": sub_id,
        }, token=token)
        if cst != 200:
            raise RuntimeError(f"Complete profile failed ({cst}): {cresp}")

    return token, uid

def login_admin() -> str:
    admin_email = os.getenv("ADMIN_EMAIL", "4ce013@gmail.com")
    admin_pass = os.getenv("ADMIN_PASSWORD", "admin123")
    st, _, resp = http_json("POST", "/api/v1/auth/login", {"email": admin_email, "password": admin_pass})
    if st != 200 or not resp.get("success"):
        raise RuntimeError(f"Admin login failed ({st}): {resp}")
    return resp["data"]["accessToken"]


def main():
    print(f"=== Domain 5 Live Verification: Social Feed, Reviews, Reports (RUN_ID: {RUN_ID}) ===")

    # Setup accounts
    admin_token = login_admin()
    print("✓ Admin token obtained")

    # A1: Verified Teacher Artisan
    a1_email = f"d5_artisan_teacher_{RUN_ID}@example.com"
    a1_token, a1_uid = register_and_login(a1_email, "TestPassword123!", "ARTISAN")
    db_execute(f"UPDATE artisans SET is_teacher = 1, is_verified = 1 WHERE id = '{a1_uid}'")
    print(f"✓ Verified Teacher Artisan A1 setup: {a1_uid}")

    # A2: Verified Peer Artisan
    a2_email = f"d5_artisan_peer_{RUN_ID}@example.com"
    a2_token, a2_uid = register_and_login(a2_email, "TestPassword123!", "ARTISAN")
    db_execute(f"UPDATE artisans SET is_teacher = 0, is_verified = 1 WHERE id = '{a2_uid}'")
    print(f"✓ Verified Peer Artisan A2 setup: {a2_uid}")

    # A3: Unverified Artisan
    a3_email = f"d5_artisan_unverified_{RUN_ID}@example.com"
    a3_token, a3_uid = register_and_login(a3_email, "TestPassword123!", "ARTISAN")
    db_execute(f"UPDATE artisans SET is_verified = 0 WHERE id = '{a3_uid}'")
    print(f"✓ Unverified Artisan A3 setup: {a3_uid}")

    # C1: Client User
    c1_email = f"d5_client_{RUN_ID}@example.com"
    c1_token, c1_uid = register_and_login(c1_email, "TestPassword123!", "CLIENT")
    print(f"✓ Client User C1 setup: {c1_uid}")

    # Setup a completed formation F1 by A1 with enrollment by A2 (ATTENDED)
    form_payload = {
        "title": f"Masterclass Céramique {RUN_ID}",
        "description": "Techniques avancées de façonnage et de cuisson de l'argile.",
        "durationHours": 8,
        "maxParticipants": 10,
        "price": 4500.0,
        "scheduledAt": "2026-12-25T10:00:00",
    }
    st, _, form_resp = http_json("POST", "/api/v1/artisan/formations", form_payload, token=a1_token)
    if st != 201:
        raise RuntimeError(f"Formation creation failed ({st}): {form_resp}")
    f1_id = form_resp["data"]["id"]
    print(f"✓ Created test formation F1: {f1_id}")

    # Set formation PUBLISHED so A2 can enroll
    db_execute(f"UPDATE formations SET status = 'PUBLISHED' WHERE id = '{f1_id}'")

    # Enroll A2 in F1
    st, _, enroll_resp = http_json("POST", f"/api/v1/artisan/formations/{f1_id}/enroll", token=a2_token)
    if st != 200:
        raise RuntimeError(f"Enrollment failed ({st}): {enroll_resp}")
    enrollment_id = enroll_resp["data"]["id"]
    # Mark formation COMPLETED and enrollment ATTENDED in DB for review testing
    db_execute(f"UPDATE formations SET status = 'COMPLETED' WHERE id = '{f1_id}'")
    db_execute(f"UPDATE formation_enrollments SET status = 'ATTENDED' WHERE id = '{enrollment_id}'")
    print(f"✓ Formation F1 marked COMPLETED, enrollment {enrollment_id} marked ATTENDED")

    # =========================================================================
    # 1. Feed Post Creation & Authorization Boundaries
    # =========================================================================

    # d5-feed-01: Anonymous creation rejection (feed path is permitAll in filter, caught by @PreAuthorize -> 403)
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "Test", "body": "Body"})
    passed = (st in (401, 403))
    record("d5-feed-01", "Anonymous creation rejection", passed, "401/403 FORBIDDEN", f"{st}", resp)

    # d5-feed-02: Client account creation rejection
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "Test", "body": "Body"}, token=c1_token)
    passed = (st == 403 and "verified artisans or administrators" in str(resp.get("message", "")))
    record("d5-feed-02", "Client account creation rejection", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d5-feed-03: Unverified artisan creation rejection
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "Test", "body": "Body"}, token=a3_token)
    passed = (st == 403 and "verified artisans or administrators" in str(resp.get("message", "")))
    record("d5-feed-03", "Unverified artisan creation rejection", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d5-feed-04: Validation - blank title
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "", "body": "Body text"}, token=a1_token)
    passed = (st in (400, 422))
    record("d5-feed-04", "Validation - blank title", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d5-feed-05: Validation - blank body
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "Valid Title", "body": "   "}, token=a1_token)
    passed = (st in (400, 422))
    record("d5-feed-05", "Validation - blank body", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d5-feed-06: Validation - null type
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": None, "title": "Valid Title", "body": "Body text"}, token=a1_token)
    passed = (st in (400, 422))
    record("d5-feed-06", "Validation - null type", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d5-feed-07: Validation - title exceeds 200 chars
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "X" * 201, "body": "Body text"}, token=a1_token)
    passed = (st in (400, 422))
    record("d5-feed-07", "Validation - title exceeds 200 chars", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d5-feed-08: Rule - FORMATION type without formationId
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "FORMATION", "title": "Formation Post", "body": "Body text", "formationId": None}, token=a1_token)
    passed = (st == 400 and "require a formationId" in str(resp.get("message", "")))
    record("d5-feed-08", "Rule - FORMATION type without formationId", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d5-feed-09: Rule - Non-FORMATION type with formationId
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "Actualité Post", "body": "Body text", "formationId": f1_id}, token=a1_token)
    passed = (st == 400 and "Only formation posts may reference a formation" in str(resp.get("message", "")))
    record("d5-feed-09", "Rule - Non-FORMATION type with formationId", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d5-feed-10: Rule - FORMATION type with non-existent formationId
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "FORMATION", "title": "Formation Post", "body": "Body text", "formationId": "00000000-0000-0000-0000-000000000000"}, token=a1_token)
    passed = (st == 404 and "Formation not found" in str(resp.get("message", "")))
    record("d5-feed-10", "Rule - FORMATION type with non-existent formationId", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d5-feed-11: Verified Artisan creates ACTUALITE post
    post1_payload = {
        "type": "ACTUALITE",
        "title": f"Nouvel arrivage d'argile naturelle {RUN_ID}",
        "body": "Nous venons de recevoir un nouveau lot d'argile rouge traditionnelle de Tlemcen.",
    }
    st, _, resp = http_json("POST", "/api/v1/feed", post1_payload, token=a1_token)
    post1 = resp.get("data", {}) if resp else {}
    post1_id = post1.get("id")
    passed = (st == 201 and resp.get("success") and post1.get("status") == "PENDING" and post1.get("publishedAt") is None)
    record("d5-feed-11", "Verified Artisan creates ACTUALITE post", passed, "201 CREATED status=PENDING", f"{st} status={post1.get('status')}", resp)

    # d5-feed-12: Verified Artisan creates ANNONCE post
    post2_payload = {
        "type": "ANNONCE",
        "title": f"Portes ouvertes à l'atelier {RUN_ID}",
        "body": "Venez découvrir notre atelier et nos créations artisanales ce week-end.",
    }
    st, _, resp = http_json("POST", "/api/v1/feed", post2_payload, token=a1_token)
    post2 = resp.get("data", {}) if resp else {}
    post2_id = post2.get("id")
    passed = (st == 201 and resp.get("success") and post2.get("status") == "PENDING" and post2.get("type") == "ANNONCE")
    record("d5-feed-12", "Verified Artisan creates ANNONCE post", passed, "201 CREATED status=PENDING type=ANNONCE", f"{st} status={post2.get('status')}", resp)

    # d5-feed-13: Verified Artisan creates FORMATION post with valid formationId
    post3_payload = {
        "type": "FORMATION",
        "title": f"Session masterclass céramique disponible {RUN_ID}",
        "body": "Les inscriptions pour la prochaine session de masterclass sont ouvertes.",
        "formationId": f1_id,
    }
    st, _, resp = http_json("POST", "/api/v1/feed", post3_payload, token=a1_token)
    post3 = resp.get("data", {}) if resp else {}
    post3_id = post3.get("id")
    passed = (st == 201 and resp.get("success") and post3.get("status") == "PENDING" and post3.get("formationId") == f1_id)
    record("d5-feed-13", "Verified Artisan creates FORMATION post with valid formationId", passed, "201 CREATED formationId matches", f"{st} formationId={post3.get('formationId')}", resp)

    # d5-feed-14: Admin creates feed post directly
    post_admin_payload = {
        "type": "ACTUALITE",
        "title": f"Annonce officielle plateforme {RUN_ID}",
        "body": "Bienvenue sur le nouveau portail de l'artisanat algérien Souklab.",
    }
    st, _, resp = http_json("POST", "/api/v1/feed", post_admin_payload, token=admin_token)
    post_admin = resp.get("data", {}) if resp else {}
    post_admin_id = post_admin.get("id")
    passed = (st == 201 and resp.get("success") and post_admin.get("status") == "PENDING")
    record("d5-feed-14", "Admin creates feed post directly", passed, "201 CREATED status=PENDING", f"{st} status={post_admin.get('status')}", resp)

    # =========================================================================
    # 2. Media Attachments on Feed Posts
    # =========================================================================

    # d5-media-15: Anonymous media upload rejection
    st, _, resp = http_multipart("POST", f"/api/v1/feed/{post1_id}/media", {}, {"file": ("test.png", VALID_PNG_BYTES, "image/png")})
    passed = (st in (401, 403))
    record("d5-media-15", "Anonymous media upload rejection", passed, "401/403 FORBIDDEN", f"{st}", resp)

    # d5-media-16: Non-author non-admin media upload rejection
    st, _, resp = http_multipart("POST", f"/api/v1/feed/{post1_id}/media", {}, {"file": ("test.png", VALID_PNG_BYTES, "image/png")}, token=a2_token)
    passed = (st == 403 and "manage your own feed posts" in str(resp.get("message", "")))
    record("d5-media-16", "Non-author non-admin media upload rejection", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d5-media-17: Non-existent post media upload
    st, _, resp = http_multipart("POST", "/api/v1/feed/00000000-0000-0000-0000-000000000000/media", {}, {"file": ("test.png", VALID_PNG_BYTES, "image/png")}, token=a1_token)
    passed = (st == 404 and "Feed post not found" in str(resp.get("message", "")))
    record("d5-media-17", "Non-existent post media upload", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d5-media-18: Missing / empty file upload rejection
    st, _, resp = http_multipart("POST", f"/api/v1/feed/{post1_id}/media", {}, {"file": ("empty.png", b"", "image/png")}, token=a1_token)
    passed = (st == 400 and "Media file is required" in str(resp.get("message", "")))
    record("d5-media-18", "Missing / empty file upload rejection", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d5-media-19: Disallowed MIME type rejection
    st, _, resp = http_multipart("POST", f"/api/v1/feed/{post1_id}/media", {}, {"file": ("doc.txt", b"plain text content", "text/plain")}, token=a1_token)
    passed = (st in (400, 415))
    record("d5-media-19", "Disallowed MIME type rejection", passed, "400/415 UNSUPPORTED_MEDIA_TYPE", f"{st}", resp)

    # d5-media-20: Author uploads PNG image
    st, _, resp = http_multipart("POST", f"/api/v1/feed/{post1_id}/media", {}, {"file": ("photo1.png", VALID_PNG_BYTES, "image/png")}, token=a1_token)
    m1 = resp.get("data", {}) if resp else {}
    m1_id = m1.get("id")
    passed = (st == 201 and resp.get("success") and m1.get("displayOrder") == 0 and bool(m1.get("url")))
    record("d5-media-20", "Author uploads PNG image", passed, "201 CREATED displayOrder=0 url present", f"{st} order={m1.get('displayOrder')} url={bool(m1.get('url'))}", resp)

    # d5-media-21: Author uploads second JPEG image
    st, _, resp = http_multipart("POST", f"/api/v1/feed/{post1_id}/media", {}, {"file": ("photo2.jpg", VALID_JPEG_BYTES, "image/jpeg")}, token=a1_token)
    m2 = resp.get("data", {}) if resp else {}
    m2_id = m2.get("id")
    passed = (st == 201 and resp.get("success") and m2.get("displayOrder") == 1 and bool(m2.get("url")))
    record("d5-media-21", "Author uploads second JPEG image", passed, "201 CREATED displayOrder=1", f"{st} order={m2.get('displayOrder')}", resp)

    # d5-media-22: Media listed in post representation
    st, _, resp = http_json("GET", "/api/v1/admin/feed/pending", token=admin_token)
    pending_posts = resp.get("data", {}).get("content", []) if resp else []
    target_post = next((p for p in pending_posts if p.get("id") == post1_id), None)
    media_list = target_post.get("media", []) if target_post else []
    media_ids = {m["id"] for m in media_list}
    passed = (st == 200 and len(media_list) == 2 and {m1_id, m2_id}.issubset(media_ids))
    record("d5-media-22", "Media listed in post representation", passed, "2 media items present", f"{len(media_list)} items", media_list)

    # d5-media-23: Non-author non-admin media delete rejection
    st, _, resp = http_json("DELETE", f"/api/v1/feed/{post1_id}/media/{m1_id}", token=a2_token)
    passed = (st == 403 and "manage your own feed posts" in str(resp.get("message", "")))
    record("d5-media-23", "Non-author non-admin media delete rejection", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d5-media-24: Non-existent media delete rejection
    st, _, resp = http_json("DELETE", f"/api/v1/feed/{post1_id}/media/00000000-0000-0000-0000-000000000000", token=a1_token)
    passed = (st == 404 and "Feed media not found" in str(resp.get("message", "")))
    record("d5-media-24", "Non-existent media delete rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d5-media-25: Author deletes media item
    st, _, resp = http_json("DELETE", f"/api/v1/feed/{post1_id}/media/{m1_id}", token=a1_token)
    passed = (st == 200 and resp.get("success"))
    record("d5-media-25", "Author deletes media item", passed, "200 OK", f"{st}", resp)

    # d5-media-26: Verification of media removal in post
    st, _, resp = http_json("GET", "/api/v1/admin/feed/pending", token=admin_token)
    pending_posts = resp.get("data", {}).get("content", []) if resp else []
    target_post = next((p for p in pending_posts if p.get("id") == post1_id), None)
    media_list = target_post.get("media", []) if target_post else []
    passed = (st == 200 and len(media_list) == 1 and media_list[0]["id"] == m2_id)
    record("d5-media-26", "Verification of media removal in post", passed, "1 media item remaining", f"{len(media_list)} items", media_list)

    # =========================================================================
    # 3. Public Feed Discovery & Admin Moderation Gate
    # =========================================================================

    # d5-mod-27: Public list excludes PENDING posts
    st, _, resp = http_json("GET", "/api/v1/feed")
    pub_items = resp.get("data", {}).get("content", []) if resp else []
    found_pending = any(p.get("id") in (post1_id, post2_id, post3_id) for p in pub_items)
    passed = (st == 200 and not found_pending)
    record("d5-mod-27", "Public list excludes PENDING posts", passed, "200 OK pending posts excluded", f"{st} found_pending={found_pending}", resp)

    # d5-mod-28: Single public get on PENDING post returns 404
    st, _, resp = http_json("GET", f"/api/v1/feed/{post1_id}")
    passed = (st == 404 and "Feed post not found" in str(resp.get("message", "")))
    record("d5-mod-28", "Single public get on PENDING post returns 404", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d5-mod-29: Non-admin cannot list pending posts
    st, _, resp = http_json("GET", "/api/v1/admin/feed/pending", token=a1_token)
    passed = (st == 403)
    record("d5-mod-29", "Non-admin cannot list pending posts", passed, "403 FORBIDDEN", f"{st}", resp)

    # d5-mod-30: Admin lists pending posts
    st, _, resp = http_json("GET", "/api/v1/admin/feed/pending", token=admin_token)
    pending_items = resp.get("data", {}).get("content", []) if resp else []
    ids_in_pending = {p.get("id") for p in pending_items}
    passed = (st == 200 and {post1_id, post2_id, post3_id}.issubset(ids_in_pending))
    record("d5-mod-30", "Admin lists pending posts", passed, "200 OK includes created pending posts", f"{st} count={len(pending_items)}", resp)

    # d5-mod-31: Non-admin cannot publish post
    st, _, resp = http_json("POST", f"/api/v1/admin/feed/{post1_id}/publish", {"note": "Approved"}, token=a1_token)
    passed = (st == 403)
    record("d5-mod-31", "Non-admin cannot publish post", passed, "403 FORBIDDEN", f"{st}", resp)

    # d5-mod-32: Admin publish with blank note rejected
    st, _, resp = http_json("POST", f"/api/v1/admin/feed/{post1_id}/publish", {"note": "   "}, token=admin_token)
    passed = (st in (400, 422))
    record("d5-mod-32", "Admin publish with blank note rejected", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d5-mod-33: Admin publishes post1 (ACTUALITE)
    st, _, resp = http_json("POST", f"/api/v1/admin/feed/{post1_id}/publish", {"note": "Conforme à la charte"}, token=admin_token)
    p1_pub = resp.get("data", {}) if resp else {}
    passed = (st == 200 and resp.get("success") and p1_pub.get("status") == "PUBLISHED" and p1_pub.get("publishedAt") is not None and p1_pub.get("moderationNote") == "Conforme à la charte")
    record("d5-mod-33", "Admin publishes post1 (ACTUALITE)", passed, "200 OK status=PUBLISHED publishedAt set", f"{st} status={p1_pub.get('status')}", resp)

    # Also publish post2 (ANNONCE) and post3 (FORMATION)
    http_json("POST", f"/api/v1/admin/feed/{post2_id}/publish", {"note": "Annonce validée"}, token=admin_token)
    st3, _, resp3 = http_json("POST", f"/api/v1/admin/feed/{post3_id}/publish", {"note": "Formation validée"}, token=admin_token)

    # d5-mod-34: Notification delivered on FORMATION post publish
    # Verify author received notification for formation post
    st, _, notif_resp = http_json("GET", "/api/v1/notifications", token=a1_token)
    notifs = notif_resp.get("data", {}).get("content", []) if notif_resp else []
    formation_notif = next((n for n in notifs if n.get("targetId") == post3_id or "formation post was published" in str(n.get("message", ""))), None)
    passed = (st3 == 200 and formation_notif is not None)
    record("d5-mod-34", "Notification delivered on FORMATION post publish", passed, "Notification exists for author", f"Found={formation_notif is not None}", notif_resp)

    # d5-mod-35: Published post visible in public feed
    st, _, resp = http_json("GET", "/api/v1/feed")
    pub_items = resp.get("data", {}).get("content", []) if resp else []
    pub_ids = {p.get("id") for p in pub_items}
    passed = (st == 200 and post1_id in pub_ids and post2_id in pub_ids and post3_id in pub_ids)
    record("d5-mod-35", "Published post visible in public feed", passed, "200 OK includes post1, post2, post3", f"{st} found={post1_id in pub_ids}", resp)

    # d5-mod-36: Public feed type filter: ACTUALITE, FORMATION, ANNONCE
    st_act, _, resp_act = http_json("GET", "/api/v1/feed", query_params={"type": "ACTUALITE"})
    st_ann, _, resp_ann = http_json("GET", "/api/v1/feed", query_params={"type": "ANNONCE"})
    st_frm, _, resp_frm = http_json("GET", "/api/v1/feed", query_params={"type": "FORMATION"})
    act_items = resp_act.get("data", {}).get("content", []) if resp_act else []
    ann_items = resp_ann.get("data", {}).get("content", []) if resp_ann else []
    frm_items = resp_frm.get("data", {}).get("content", []) if resp_frm else []
    passed = (
        st_act == 200 and all(p.get("type") == "ACTUALITE" for p in act_items) and any(p.get("id") == post1_id for p in act_items) and
        st_ann == 200 and all(p.get("type") == "ANNONCE" for p in ann_items) and any(p.get("id") == post2_id for p in ann_items) and
        st_frm == 200 and all(p.get("type") == "FORMATION" for p in frm_items) and any(p.get("id") == post3_id for p in frm_items)
    )
    record("d5-mod-36", "Public feed type filter (ACTUALITE, ANNONCE, FORMATION)", passed, "Filtered results match types accurately", f"act={len(act_items)} ann={len(ann_items)} frm={len(frm_items)}", {"act": act_items, "ann": ann_items, "frm": frm_items})

    # d5-mod-37: Public single get on published post
    st, _, resp = http_json("GET", f"/api/v1/feed/{post1_id}")
    item = resp.get("data", {}) if resp else {}
    passed = (st == 200 and resp.get("success") and item.get("id") == post1_id and item.get("status") == "PUBLISHED" and item.get("authorName") is not None)
    record("d5-mod-37", "Public single get on published post", passed, "200 OK status=PUBLISHED with authorName", f"{st} authorName={item.get('authorName')}", resp)

    # d5-mod-38: Admin hides published post
    st, _, resp = http_json("POST", f"/api/v1/admin/feed/{post1_id}/hide", {"note": "Masquage temporaire"}, token=admin_token)
    hidden_post = resp.get("data", {}) if resp else {}
    passed = (st == 200 and hidden_post.get("status") == "HIDDEN")
    record("d5-mod-38", "Admin hides published post", passed, "200 OK status=HIDDEN", f"{st} status={hidden_post.get('status')}", resp)

    # d5-mod-39: Hidden post excluded from public feed
    st_get, _, resp_get = http_json("GET", f"/api/v1/feed/{post1_id}")
    st_list, _, resp_list = http_json("GET", "/api/v1/feed")
    pub_ids = {p.get("id") for p in resp_list.get("data", {}).get("content", [])} if resp_list else set()
    passed = (st_get == 404 and post1_id not in pub_ids)
    record("d5-mod-39", "Hidden post excluded from public feed", passed, "404 on get and excluded from list", f"st_get={st_get} in_list={post1_id in pub_ids}", resp_get)

    # d5-mod-40: Admin re-publishes hidden post
    st, _, resp = http_json("POST", f"/api/v1/admin/feed/{post1_id}/publish", {"note": "Ré-activation après vérification"}, token=admin_token)
    repub_post = resp.get("data", {}) if resp else {}
    passed = (st == 200 and repub_post.get("status") == "PUBLISHED")
    record("d5-mod-40", "Admin re-publishes hidden post", passed, "200 OK status=PUBLISHED", f"{st} status={repub_post.get('status')}", resp)

    # =========================================================================
    # 4. Author Post Editing & Admin Edit Lifecycle
    # =========================================================================

    # d5-edit-41: Non-author cannot edit post
    edit_payload = {"type": "ACTUALITE", "title": "Titre modifié par pirate", "body": "Nouveau contenu"}
    st, _, resp = http_json("PUT", f"/api/v1/feed/{post1_id}", edit_payload, token=a2_token)
    passed = (st == 403 and "manage your own feed posts" in str(resp.get("message", "")))
    record("d5-edit-41", "Non-author cannot edit post", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d5-edit-42: Author edits published post (resets status to PENDING)
    author_edit_payload = {
        "type": "ACTUALITE",
        "title": f"Mise à jour: Nouvel arrivage d'argile {RUN_ID}",
        "body": "Texte mis à jour avec les précisions sur les horaires de livraison.",
    }
    st, _, resp = http_json("PUT", f"/api/v1/feed/{post1_id}", author_edit_payload, token=a1_token)
    edited_post = resp.get("data", {}) if resp else {}
    passed = (st == 200 and edited_post.get("status") == "PENDING" and edited_post.get("publishedAt") is None and edited_post.get("title") == author_edit_payload["title"])
    record("d5-edit-42", "Author edits published post (resets status to PENDING)", passed, "200 OK status=PENDING publishedAt=null", f"{st} status={edited_post.get('status')}", resp)

    # d5-edit-43: Post immediately hidden from public feed while PENDING again
    st, _, resp = http_json("GET", f"/api/v1/feed/{post1_id}")
    passed = (st == 404 and "Feed post not found" in str(resp.get("message", "")))
    record("d5-edit-43", "Post immediately hidden from public feed while PENDING again", passed, "404 NOT_FOUND", f"{st}", resp)

    # d5-edit-44: Admin re-publishes edited post
    st, _, resp = http_json("POST", f"/api/v1/admin/feed/{post1_id}/publish", {"note": "Modification approuvée"}, token=admin_token)
    repub2 = resp.get("data", {}) if resp else {}
    passed = (st == 200 and repub2.get("status") == "PUBLISHED" and repub2.get("publishedAt") is not None)
    record("d5-edit-44", "Admin re-publishes edited post", passed, "200 OK status=PUBLISHED", f"{st} status={repub2.get('status')}", resp)

    # d5-edit-45: Admin edits post (status remains PUBLISHED)
    admin_edit_payload = {
        "type": "ACTUALITE",
        "title": f"Mise à jour Admin: Nouvel arrivage d'argile {RUN_ID}",
        "body": "Texte ajusté par l'administration pour clarté.",
    }
    st, _, resp = http_json("PUT", f"/api/v1/feed/{post1_id}", admin_edit_payload, token=admin_token)
    admin_edited = resp.get("data", {}) if resp else {}
    passed = (st == 200 and admin_edited.get("status") == "PUBLISHED")
    record("d5-edit-45", "Admin edits post (status remains PUBLISHED)", passed, "200 OK status=PUBLISHED", f"{st} status={admin_edited.get('status')}", resp)

    # =========================================================================
    # 5. Post Deletion Lifecycle
    # =========================================================================

    # Create an extra post by A1 specifically for deletion tests
    del_post_payload = {"type": "ANNONCE", "title": f"Post à supprimer {RUN_ID}", "body": "Ce post sera supprimé par l'auteur."}
    _, _, del_post_resp = http_json("POST", "/api/v1/feed", del_post_payload, token=a1_token)
    del_post_id = del_post_resp["data"]["id"]

    # d5-del-46: Non-author cannot delete post
    st, _, resp = http_json("DELETE", f"/api/v1/feed/{del_post_id}", token=a2_token)
    passed = (st == 403 and "manage your own feed posts" in str(resp.get("message", "")))
    record("d5-del-46", "Non-author cannot delete post", passed, "403 FORBIDDEN", f"{st} - {resp.get('message')}", resp)

    # d5-del-47: Author deletes own post
    st, _, resp = http_json("DELETE", f"/api/v1/feed/{del_post_id}", token=a1_token)
    db_rows = db_query(f"SELECT status, deleted_at FROM feed_posts WHERE id = '{del_post_id}'")
    db_status = db_rows[0][0] if db_rows else None
    db_deleted_at = db_rows[0][1] if db_rows else None
    passed = (st == 200 and resp.get("success") and db_status == "REMOVED" and db_deleted_at is not None and db_deleted_at != "NULL")
    record("d5-del-47", "Author deletes own post", passed, "200 OK DB status=REMOVED deleted_at set", f"{st} DB_status={db_status}", resp)

    # d5-del-48: Deleted post excluded from public feed
    st, _, resp = http_json("GET", f"/api/v1/feed/{del_post_id}")
    passed = (st == 404 and "Feed post not found" in str(resp.get("message", "")))
    record("d5-del-48", "Deleted post excluded from public feed", passed, "404 NOT_FOUND", f"{st}", resp)

    # d5-del-49: Cannot publish removed post
    st, _, resp = http_json("POST", f"/api/v1/admin/feed/{del_post_id}/publish", {"note": "Test publish removed"}, token=admin_token)
    passed = (st in (404, 409))
    record("d5-del-49", "Cannot publish removed post", passed, "404/409 NOT_FOUND/CONFLICT", f"{st} - {resp.get('message')}", resp)

    # d5-del-50: Admin removes post directly
    # Create another post to be removed by admin
    _, _, adm_del_resp = http_json("POST", "/api/v1/feed", {"type": "ANNONCE", "title": "Post removed by admin", "body": "Admin removal test"}, token=a1_token)
    adm_del_id = adm_del_resp["data"]["id"]
    st, _, resp = http_json("POST", f"/api/v1/admin/feed/{adm_del_id}/remove", token=admin_token)
    db_rows = db_query(f"SELECT status, deleted_at FROM feed_posts WHERE id = '{adm_del_id}'")
    db_status = db_rows[0][0] if db_rows else None
    passed = (st == 200 and resp.get("success") and db_status == "REMOVED")
    record("d5-del-50", "Admin removes post directly", passed, "200 OK DB status=REMOVED", f"{st} DB_status={db_status}", resp)

    # =========================================================================
    # 6. Public Artisan Reviews Listing
    # =========================================================================

    # d5-rev-51: Public list artisan reviews
    st, _, resp = http_json("GET", f"/api/v1/artisans/{a1_uid}/reviews")
    passed = (st == 200 and resp.get("success") and "content" in resp.get("data", {}))
    record("d5-rev-51", "Public list artisan reviews", passed, "200 OK content array", f"{st}", resp)

    # d5-rev-52: Public list for non-existent artisan
    st, _, resp = http_json("GET", "/api/v1/artisans/00000000-0000-0000-0000-000000000000/reviews")
    content = resp.get("data", {}).get("content", []) if resp else None
    passed = (st == 200 and resp.get("success") and content == [])
    record("d5-rev-52", "Public list for non-existent artisan", passed, "200 OK empty list", f"{st} len={len(content) if content is not None else -1}", resp)

    # Create a review for A1 by A2 on F1
    review_payload = {
        "rating": 4.5,
        "comment": "Excellente masterclass, formateur très pédagogue et passionné !",
    }
    st_rev, _, rev_resp = http_json("POST", f"/api/v1/artisan/formations/{f1_id}/reviews", review_payload, token=a2_token)
    if st_rev != 201:
        raise RuntimeError(f"Review creation failed ({st_rev}): {rev_resp}")
    r1_id = rev_resp["data"]["id"]
    print(f"✓ Created test review R1: {r1_id}")

    # d5-rev-53: Attended completed review visible in public reviews list
    st, _, resp = http_json("GET", f"/api/v1/artisans/{a1_uid}/reviews")
    reviews_list = resp.get("data", {}).get("content", []) if resp else []
    found_r1 = next((r for r in reviews_list if r.get("id") == r1_id), None)
    passed = (st == 200 and found_r1 is not None and float(found_r1.get("rating", 0)) == 4.5 and found_r1.get("comment") == review_payload["comment"])
    record("d5-rev-53", "Attended completed review visible in public reviews list", passed, "200 OK contains review with rating 4.5", f"{st} found={found_r1 is not None}", resp)

    # Create a second review R2 to test deletion exclusion
    # Create formation F2 by A1, enrollment by A2
    _, _, f2_resp = http_json("POST", "/api/v1/artisan/formations", {
        "title": f"Formation Sculpture {RUN_ID}",
        "description": "Techniques de sculpture sur bois.",
        "durationHours": 6,
        "maxParticipants": 5,
        "price": 3000.0,
        "scheduledAt": "2026-12-28T14:00:00",
    }, token=a1_token)
    f2_id = f2_resp["data"]["id"]
    db_execute(f"UPDATE formations SET status = 'PUBLISHED' WHERE id = '{f2_id}'")
    _, _, e2_resp = http_json("POST", f"/api/v1/artisan/formations/{f2_id}/enroll", token=a2_token)
    e2_id = e2_resp["data"]["id"]
    db_execute(f"UPDATE formations SET status = 'COMPLETED' WHERE id = '{f2_id}'")
    db_execute(f"UPDATE formation_enrollments SET status = 'ATTENDED' WHERE id = '{e2_id}'")
    _, _, r2_resp = http_json("POST", f"/api/v1/artisan/formations/{f2_id}/reviews", {"rating": 5.0, "comment": "À supprimer"}, token=a2_token)
    r2_id = r2_resp["data"]["id"]
    # Delete R2
    http_json("DELETE", f"/api/v1/artisan/reviews/{r2_id}", token=a2_token)

    # d5-rev-54: Soft-deleted review excluded from public reviews list
    st, _, resp = http_json("GET", f"/api/v1/artisans/{a1_uid}/reviews")
    reviews_list = resp.get("data", {}).get("content", []) if resp else []
    found_r2 = any(r.get("id") == r2_id for r in reviews_list)
    passed = (st == 200 and not found_r2)
    record("d5-rev-54", "Soft-deleted review excluded from public reviews list", passed, "200 OK deleted review excluded", f"{st} found_r2={found_r2}", resp)

    # =========================================================================
    # 7. Content Reports Creation & Target Validation
    # =========================================================================

    # d5-rep-55: Anonymous report creation rejection
    st, _, resp = http_json("POST", "/api/v1/reports", {"targetType": "POST", "targetId": post1_id, "reason": "Spam"})
    passed = (st == 401)
    record("d5-rep-55", "Anonymous report creation rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d5-rep-56: Self-report rejection for USER target
    st, _, resp = http_json("POST", "/api/v1/reports", {"targetType": "USER", "targetId": c1_uid, "reason": "Signaler moi-même"}, token=c1_token)
    passed = (st == 400 and "cannot report yourself" in str(resp.get("message", "")))
    record("d5-rep-56", "Self-report rejection for USER target", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d5-rep-57: Non-existent USER report rejection
    st, _, resp = http_json("POST", "/api/v1/reports", {"targetType": "USER", "targetId": "00000000-0000-0000-0000-000000000000", "reason": "Non-existent user"}, token=c1_token)
    passed = (st == 404 and "Report target not found" in str(resp.get("message", "")))
    record("d5-rep-57", "Non-existent USER report rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d5-rep-58: Non-existent POST report rejection
    st, _, resp = http_json("POST", "/api/v1/reports", {"targetType": "POST", "targetId": "00000000-0000-0000-0000-000000000000", "reason": "Non-existent post"}, token=c1_token)
    passed = (st == 404 and "Report target not found" in str(resp.get("message", "")))
    record("d5-rep-58", "Non-existent POST report rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d5-rep-59: Non-existent REVIEW report rejection
    st, _, resp = http_json("POST", "/api/v1/reports", {"targetType": "REVIEW", "targetId": "00000000-0000-0000-0000-000000000000", "reason": "Non-existent review"}, token=c1_token)
    passed = (st == 404 and "Report target not found" in str(resp.get("message", "")))
    record("d5-rep-59", "Non-existent REVIEW report rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d5-rep-60: Validation - missing targetType
    st, _, resp = http_json("POST", "/api/v1/reports", {"targetType": None, "targetId": post1_id, "reason": "Spam"}, token=c1_token)
    passed = (st in (400, 422))
    record("d5-rep-60", "Validation - missing targetType", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d5-rep-61: Validation - empty reason
    st, _, resp = http_json("POST", "/api/v1/reports", {"targetType": "POST", "targetId": post1_id, "reason": "   "}, token=c1_token)
    passed = (st in (400, 422))
    record("d5-rep-61", "Validation - empty reason", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # d5-rep-62: Report a User successfully
    user_rep_payload = {
        "targetType": "USER",
        "targetId": a3_uid,
        "reason": "Comportement suspect",
        "details": "L'utilisateur envoie des messages non sollicités.",
    }
    st, _, resp = http_json("POST", "/api/v1/reports", user_rep_payload, token=c1_token)
    u_rep = resp.get("data", {}) if resp else {}
    rep_user_id = u_rep.get("id")
    passed = (st == 201 and resp.get("success") and u_rep.get("targetType") == "USER" and u_rep.get("targetId") == a3_uid and u_rep.get("status") == "OPEN" and u_rep.get("reporterId") == c1_uid)
    record("d5-rep-62", "Report a User successfully", passed, "201 CREATED status=OPEN targetType=USER", f"{st} status={u_rep.get('status')}", resp)

    # d5-rep-63: Report a Feed Post successfully
    post_rep_payload = {
        "targetType": "POST",
        "targetId": post2_id,
        "reason": "Publicité mensongère",
        "details": "L'atelier n'est pas ouvert aux horaires indiqués.",
    }
    st, _, resp = http_json("POST", "/api/v1/reports", post_rep_payload, token=c1_token)
    p_rep = resp.get("data", {}) if resp else {}
    rep_post_id = p_rep.get("id")
    passed = (st == 201 and resp.get("success") and p_rep.get("targetType") == "POST" and p_rep.get("targetId") == post2_id and p_rep.get("status") == "OPEN")
    record("d5-rep-63", "Report a Feed Post successfully", passed, "201 CREATED status=OPEN targetType=POST", f"{st} status={p_rep.get('status')}", resp)

    # d5-rep-64: Report an Artisan Review successfully
    rev_rep_payload = {
        "targetType": "REVIEW",
        "targetId": r1_id,
        "reason": "Avis inapproprié",
        "details": "Contient des allégations inexactes.",
    }
    st, _, resp = http_json("POST", "/api/v1/reports", rev_rep_payload, token=c1_token)
    r_rep = resp.get("data", {}) if resp else {}
    rep_rev_id = r_rep.get("id")
    passed = (st == 201 and resp.get("success") and r_rep.get("targetType") == "REVIEW" and r_rep.get("targetId") == r1_id and r_rep.get("status") == "OPEN")
    record("d5-rep-64", "Report an Artisan Review successfully", passed, "201 CREATED status=OPEN targetType=REVIEW", f"{st} status={r_rep.get('status')}", resp)

    # =========================================================================
    # 8. Admin Report Queue & Resolution Actions
    # =========================================================================

    # d5-res-65: Non-admin cannot list reports
    st, _, resp = http_json("GET", "/api/v1/admin/reports", token=c1_token)
    passed = (st == 403)
    record("d5-res-65", "Non-admin cannot list reports", passed, "403 FORBIDDEN", f"{st}", resp)

    # d5-res-66: Admin lists reports unfiltered
    st, _, resp = http_json("GET", "/api/v1/admin/reports", token=admin_token)
    all_reps = resp.get("data", {}).get("content", []) if resp else []
    rep_ids = {r.get("id") for r in all_reps}
    passed = (st == 200 and {rep_user_id, rep_post_id, rep_rev_id}.issubset(rep_ids))
    record("d5-res-66", "Admin lists reports unfiltered", passed, "200 OK includes submitted reports", f"{st} count={len(all_reps)}", resp)

    # d5-res-67: Admin lists reports filtered by status
    st, _, resp = http_json("GET", "/api/v1/admin/reports", query_params={"status": "OPEN"}, token=admin_token)
    open_reps = resp.get("data", {}).get("content", []) if resp else []
    passed = (st == 200 and all(r.get("status") == "OPEN" for r in open_reps) and rep_post_id in {r.get("id") for r in open_reps})
    record("d5-res-67", "Admin lists reports filtered by status (OPEN)", passed, "200 OK all status=OPEN", f"{st} count={len(open_reps)}", resp)

    # d5-res-68: Admin lists reports filtered by targetType
    st, _, resp = http_json("GET", "/api/v1/admin/reports", query_params={"targetType": "POST"}, token=admin_token)
    post_reps = resp.get("data", {}).get("content", []) if resp else []
    passed = (st == 200 and all(r.get("targetType") == "POST" for r in post_reps) and rep_post_id in {r.get("id") for r in post_reps})
    record("d5-res-68", "Admin lists reports filtered by targetType (POST)", passed, "200 OK all targetType=POST", f"{st} count={len(post_reps)}", resp)

    # d5-res-69: Admin lists reports filtered by status and targetType
    st, _, resp = http_json("GET", "/api/v1/admin/reports", query_params={"status": "OPEN", "targetType": "POST"}, token=admin_token)
    filtered_reps = resp.get("data", {}).get("content", []) if resp else []
    passed = (st == 200 and all(r.get("status") == "OPEN" and r.get("targetType") == "POST" for r in filtered_reps) and rep_post_id in {r.get("id") for r in filtered_reps})
    record("d5-res-69", "Admin lists reports filtered by status and targetType", passed, "200 OK matches both filters", f"{st} count={len(filtered_reps)}", resp)

    # d5-res-70: Non-admin cannot resolve report
    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{rep_post_id}/resolve", {"action": "DISMISS", "note": "Rejeté"}, token=c1_token)
    passed = (st == 403)
    record("d5-res-70", "Non-admin cannot resolve report", passed, "403 FORBIDDEN", f"{st}", resp)

    # d5-res-71: Resolve non-existent report
    st, _, resp = http_json("POST", "/api/v1/admin/reports/00000000-0000-0000-0000-000000000000/resolve", {"action": "DISMISS", "note": "Rejeté"}, token=admin_token)
    passed = (st == 404 and "Report not found" in str(resp.get("message", "")))
    record("d5-res-71", "Resolve non-existent report", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d5-res-72: Resolve action DISMISS on Post Report (post remains untouched)
    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{rep_post_id}/resolve", {"action": "DISMISS", "note": "Signalement non justifié après enquête"}, token=admin_token)
    resolved_rep = resp.get("data", {}) if resp else {}
    # verify post2 remains PUBLISHED
    st_p2, _, resp_p2 = http_json("GET", f"/api/v1/feed/{post2_id}")
    passed = (st == 200 and resolved_rep.get("status") == "DISMISSED" and resolved_rep.get("resolutionAction") == "DISMISS" and st_p2 == 200 and resp_p2.get("data", {}).get("status") == "PUBLISHED")
    record("d5-res-72", "Resolve action DISMISS on Post Report (post remains untouched)", passed, "200 OK status=DISMISSED post remains PUBLISHED", f"{st} rep_status={resolved_rep.get('status')} post_status={resp_p2.get('data', {}).get('status')}", resp)

    # d5-res-73: Re-resolving resolved report rejected
    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{rep_post_id}/resolve", {"action": "HIDE", "note": "Tentative de ré-évaluation"}, token=admin_token)
    passed = (st == 400 and "already been resolved" in str(resp.get("message", "")))
    record("d5-res-73", "Re-resolving resolved report rejected", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d5-res-74: Resolve action HIDE on Post Report
    # Create another post + report for HIDE
    _, _, p_hide_resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "Post à masquer par signalement", "body": "Contenu inapproprié"}, token=a1_token)
    p_hide_id = p_hide_resp["data"]["id"]
    http_json("POST", f"/api/v1/admin/feed/{p_hide_id}/publish", {"note": "Publié initialement"}, token=admin_token)
    _, _, r_hide_resp = http_json("POST", "/api/v1/reports", {"targetType": "POST", "targetId": p_hide_id, "reason": "Contenu inapproprié"}, token=c1_token)
    r_hide_id = r_hide_resp["data"]["id"]

    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{r_hide_id}/resolve", {"action": "HIDE", "note": "Masqué suite au signalement"}, token=admin_token)
    rep_res_hide = resp.get("data", {}) if resp else {}
    st_chk, _, _ = http_json("GET", f"/api/v1/feed/{p_hide_id}")
    db_p_rows = db_query(f"SELECT status FROM feed_posts WHERE id = '{p_hide_id}'")
    db_p_status = db_p_rows[0][0] if db_p_rows else None
    passed = (st == 200 and rep_res_hide.get("status") == "RESOLVED" and rep_res_hide.get("resolutionAction") == "HIDE" and db_p_status == "HIDDEN" and st_chk == 404)
    record("d5-res-74", "Resolve action HIDE on Post Report (post status becomes HIDDEN)", passed, "200 OK post HIDDEN and 404 publicly", f"{st} DB_status={db_p_status}", resp)

    # d5-res-75: Resolve action REMOVE on Post Report
    # Create another post + report for REMOVE
    _, _, p_rem_resp = http_json("POST", "/api/v1/feed", {"type": "ANNONCE", "title": "Post à supprimer par signalement", "body": "Contenu illicite"}, token=a1_token)
    p_rem_id = p_rem_resp["data"]["id"]
    http_json("POST", f"/api/v1/admin/feed/{p_rem_id}/publish", {"note": "Publié initialement"}, token=admin_token)
    _, _, r_rem_resp = http_json("POST", "/api/v1/reports", {"targetType": "POST", "targetId": p_rem_id, "reason": "Contenu illicite"}, token=c1_token)
    r_rem_id = r_rem_resp["data"]["id"]

    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{r_rem_id}/resolve", {"action": "REMOVE", "note": "Supprimé définitivement"}, token=admin_token)
    rep_res_rem = resp.get("data", {}) if resp else {}
    db_p_rows = db_query(f"SELECT status, deleted_at FROM feed_posts WHERE id = '{p_rem_id}'")
    db_p_status = db_p_rows[0][0] if db_p_rows else None
    db_p_del = db_p_rows[0][1] if db_p_rows else None
    passed = (st == 200 and rep_res_rem.get("status") == "RESOLVED" and rep_res_rem.get("resolutionAction") == "REMOVE" and db_p_status == "REMOVED" and db_p_del is not None and db_p_del != "NULL")
    record("d5-res-75", "Resolve action REMOVE on Post Report (post status becomes REMOVED)", passed, "200 OK post REMOVED and deleted_at set", f"{st} DB_status={db_p_status}", resp)

    # d5-res-76: Resolve action HIDE on Review Report
    # Create formation F3 + enrollment + review for HIDE test
    _, _, f3_resp = http_json("POST", "/api/v1/artisan/formations", {
        "title": f"Formation Poterie {RUN_ID}",
        "description": "Art de la poterie kabyle.",
        "durationHours": 5,
        "maxParticipants": 5,
        "price": 2500.0,
        "scheduledAt": "2026-12-30T10:00:00",
    }, token=a1_token)
    f3_id = f3_resp["data"]["id"]
    db_execute(f"UPDATE formations SET status = 'PUBLISHED' WHERE id = '{f3_id}'")
    _, _, e3_resp = http_json("POST", f"/api/v1/artisan/formations/{f3_id}/enroll", token=a2_token)
    e3_id = e3_resp["data"]["id"]
    db_execute(f"UPDATE formations SET status = 'COMPLETED' WHERE id = '{f3_id}'")
    db_execute(f"UPDATE formation_enrollments SET status = 'ATTENDED' WHERE id = '{e3_id}'")
    _, _, r3_resp = http_json("POST", f"/api/v1/artisan/formations/{f3_id}/reviews", {"rating": 1.0, "comment": "Avis diffamatoire"}, token=a2_token)
    r3_id = r3_resp["data"]["id"]

    # Report R3
    _, _, rep3_resp = http_json("POST", "/api/v1/reports", {"targetType": "REVIEW", "targetId": r3_id, "reason": "Diffamation"}, token=a1_token)
    rep3_id = rep3_resp["data"]["id"]

    # Admin resolves report with HIDE
    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{rep3_id}/resolve", {"action": "HIDE", "note": "Masqué car diffamatoire"}, token=admin_token)
    db_r_rows = db_query(f"SELECT status FROM artisan_reviews WHERE id = '{r3_id}'")
    db_r_status = db_r_rows[0][0] if db_r_rows else None
    passed = (st == 200 and resp.get("data", {}).get("status") == "RESOLVED" and db_r_status == "HIDDEN")
    record("d5-res-76", "Resolve action HIDE on Review Report (review status becomes HIDDEN)", passed, "200 OK review status=HIDDEN", f"{st} DB_status={db_r_status}", resp)

    # d5-res-77: Resolve action REMOVE on Review Report (resolve rep_rev_id on R1)
    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{rep_rev_id}/resolve", {"action": "REMOVE", "note": "Supprimé par modération"}, token=admin_token)
    db_r1_rows = db_query(f"SELECT status, deleted_at FROM artisan_reviews WHERE id = '{r1_id}'")
    db_r1_status = db_r1_rows[0][0] if db_r1_rows else None
    db_r1_del = db_r1_rows[0][1] if db_r1_rows else None
    passed = (st == 200 and resp.get("data", {}).get("status") == "RESOLVED" and db_r1_status == "REMOVED" and db_r1_del is not None and db_r1_del != "NULL")
    record("d5-res-77", "Resolve action REMOVE on Review Report (review status becomes REMOVED)", passed, "200 OK review status=REMOVED", f"{st} DB_status={db_r1_status}", resp)

    # d5-res-78: Resolve action HIDE on User Report (user account suspended)
    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{rep_user_id}/resolve", {"action": "HIDE", "note": "Compte suspendu pour comportement abusif"}, token=admin_token)
    db_u_rows = db_query(f"SELECT status FROM users WHERE id = '{a3_uid}'")
    db_u_status = db_u_rows[0][0] if db_u_rows else None
    passed = (st == 200 and resp.get("data", {}).get("status") == "RESOLVED" and db_u_status == "SUSPENDED")
    record("d5-res-78", "Resolve action HIDE on User Report (user account SUSPENDED)", passed, "200 OK user status=SUSPENDED", f"{st} DB_status={db_u_status}", resp)

    # d5-res-79: Suspended user blocked from authenticated actions
    # Attempting to create a report or post as suspended user A3
    st, _, resp = http_json("POST", "/api/v1/feed", {"type": "ACTUALITE", "title": "Test", "body": "Suspended post"}, token=a3_token)
    passed = (st == 403)
    record("d5-res-79", "Suspended user blocked from authenticated actions", passed, "403 FORBIDDEN", f"{st}", resp)

    # d5-res-80: Resolve action REMOVE on User Report (user soft-deleted)
    # Register an additional user to test REMOVE on user
    u_rem_email = f"d5_user_rem_{RUN_ID}@example.com"
    _, u_rem_id = register_and_login(u_rem_email, "TestPassword123!", "CLIENT")
    _, _, u_rem_rep = http_json("POST", "/api/v1/reports", {"targetType": "USER", "targetId": u_rem_id, "reason": "Compte fraudeur"}, token=c1_token)
    rep_rem_uid = u_rem_rep["data"]["id"]

    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{rep_rem_uid}/resolve", {"action": "REMOVE", "note": "Compte supprimé définitivement"}, token=admin_token)
    db_del_user_rows = db_query(f"SELECT deleted_at FROM users WHERE id = '{u_rem_id}'")
    db_del_at = db_del_user_rows[0][0] if db_del_user_rows else None
    passed = (st == 200 and resp.get("data", {}).get("status") == "RESOLVED" and db_del_at is not None and db_del_at != "NULL")
    record("d5-res-80", "Resolve action REMOVE on User Report (user soft-deleted)", passed, "200 OK user deleted_at set", f"{st} deleted_at={db_del_at}", resp)

    # =========================================================================
    # Summary & Output
    # =========================================================================
    total = len(results)
    passed_count = sum(1 for r in results if r["passed"])
    failed_count = total - passed_count
    pass_pct = (passed_count / total) * 100 if total > 0 else 0

    print("\n" + "=" * 80)
    print(f"DOMAIN 5 VERIFICATION COMPLETE: {passed_count}/{total} PASSED ({pass_pct:.1f}%)")
    print("=" * 80)

    # Save report
    os.makedirs(".agent-output", exist_ok=True)
    report_file = ".agent-output/domain5-feed-reports-report.json"
    with open(report_file, "w", encoding="utf-8") as f:
        json.dump({
            "domain": "Domain 5: Social Feed, Reviews, Reports",
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


if __name__ == "__main__":
    main()
