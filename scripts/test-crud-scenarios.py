#!/usr/bin/env python3
"""Exhaustive curl-based scenario tester for Souklab single-resource CRUD and privacy endpoints.

Tests all possible scenarios:
- Missing and tampered auth
- Role-based authorization & privilege escalation
- Cross-tenant / IDOR isolation
- Nonexistent and fuzzed IDs (SQLi, XSS, traversal, long strings, null bytes)
- Inactive & soft-deleted states
- HTTP method tampering
- Client premium chat gating & artisan identity masking
"""

import hashlib
import json
import os
import subprocess
import sys
import time
import urllib.parse
from uuid import uuid4

BASE_URL = os.getenv("APP_BASE_URL", "http://localhost:8080").rstrip("/")
DB_PASSWORD = os.getenv("VERIFY_DB_PASSWORD", "souklab_test_password")
DB_USER = os.getenv("VERIFY_DB_USER", "souklab_test")
DB_HOST = os.getenv("VERIFY_DB_HOST", "127.0.0.1")
DB_PORT = os.getenv("VERIFY_DB_PORT", "3307")
DB_NAME = os.getenv("VERIFY_DB_NAME", "souklab_test")

ADMIN_EMAIL = os.getenv("APP_ADMIN_DEFAULT_EMAIL", "4ce013@gmail.com")
ADMIN_PASSWORD = os.getenv("APP_ADMIN_DEFAULT_PASSWORD", "admin123")

passed_count = 0
failed_count = 0
failures = []

def run_db(query: str) -> list[str]:
    env = os.environ.copy()
    env["MYSQL_PWD"] = DB_PASSWORD
    cmd = [
        "mariadb", "--batch", "--skip-column-names", "--raw",
        "-h", DB_HOST, "-P", DB_PORT, "-u", DB_USER, DB_NAME, "-e", query
    ]
    res = subprocess.run(cmd, env=env, text=True, capture_output=True)
    if res.returncode != 0:
        raise RuntimeError(f"DB error: {res.stderr}")
    return [line for line in res.stdout.splitlines() if line]

def curl(method: str, path: str, token: str = None, headers: dict = None, body: str = None, files: dict = None) -> tuple[int, dict | str]:
    cmd = ["curl", "-s", "-w", "\n%{http_code}", "-X", method, f"{BASE_URL}{path}"]
    if headers is None:
        headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    for k, v in headers.items():
        cmd.extend(["-H", f"{k}: {v}"])
    if files:
        for k, v in files.items():
            cmd.extend(["-F", f"{k}={v}"])
    elif body is not None:
        if "Content-Type" not in headers:
            cmd.extend(["-H", "Content-Type: application/json"])
        cmd.extend(["-d", body])

    res = subprocess.run(cmd, capture_output=True, text=True)
    out = res.stdout.strip()
    if not out:
        return 0, ""
    parts = out.rsplit("\n", 1)
    status_str = parts[-1]
    body_str = parts[0] if len(parts) > 1 else ""
    try:
        status_code = int(status_str)
    except ValueError:
        status_code = 0
    try:
        parsed_body = json.loads(body_str)
    except Exception:
        parsed_body = body_str
    return status_code, parsed_body

def record_test(name: str, expected_status: int | list[int], actual_status: int, details: str = ""):
    global passed_count, failed_count
    valid = (actual_status == expected_status) if isinstance(expected_status, int) else (actual_status in expected_status)
    if valid:
        passed_count += 1
        print(f"  [PASS] {name} -> {actual_status}")
    else:
        failed_count += 1
        msg = f"  [FAIL] {name} -> Expected {expected_status}, got {actual_status}. Details: {details}"
        print(msg)
        failures.append(msg)

def get_verification_code(email: str) -> str:
    rows = run_db(
        f"SELECT vt.code_hash FROM verification_tokens vt JOIN users u ON u.id=vt.user_id "
        f"WHERE u.email='{email}' AND vt.type='EMAIL_VERIFICATION' AND vt.used_at IS NULL "
        "ORDER BY vt.created_at DESC LIMIT 1;"
    )
    if not rows:
        raise RuntimeError(f"No verification token found for {email}")
    expected_hash = rows[0]
    for n in range(1_000_000):
        c = f"{n:06d}"
        if hashlib.sha256(c.encode()).hexdigest() == expected_hash:
            return c
    raise RuntimeError(f"Failed to crack 6-digit code for {email}")

def create_user(account_type: str, first_name: str, last_name: str, admin_token: str) -> tuple[str, str, str, str]:
    email = f"curl-{account_type.lower()}-{uuid4().hex[:8]}@example.com"
    password = "Password123!"
    status, _ = curl("POST", "/api/v1/auth/register", body=json.dumps({
        "email": email, "password": password, "accountType": account_type,
        "firstName": first_name, "lastName": last_name
    }))
    if status != 201:
        raise RuntimeError(f"Failed to register {email}: {status}")
    code = get_verification_code(email)
    status, _ = curl("POST", "/api/v1/auth/verify-email", body=json.dumps({"email": email, "code": code}))
    if status != 200:
        raise RuntimeError(f"Failed to verify {email}: {status}")
    user_id = run_db(f"SELECT id FROM users WHERE email='{email}';")[0]
    if account_type == "ARTISAN":
        status, _ = curl("POST", f"/api/v1/admin/users/{user_id}/approve", token=admin_token)
        if status != 200:
            raise RuntimeError(f"Failed to approve artisan {email}: {status}")
    status, res = curl("POST", "/api/v1/auth/login", body=json.dumps({"email": email, "password": password}))
    if status != 200:
        raise RuntimeError(f"Failed to login {email}: {status}")
    token = res.get("data", {}).get("accessToken")
    return email, password, user_id, token

def main():
    print("======================================================================")
    print("Starting Exhaustive Live CRUD Scenario & Resilience Test Suite")
    print(f"Target: {BASE_URL}")
    print("======================================================================")

    # 1. Login Admin
    print("\n[Phase 1] Authenticating Admin...")
    status, res = curl("POST", "/api/v1/auth/login", body=json.dumps({"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD}))
    if status != 200:
        print(f"FATAL: Admin login failed: {status}, {res}")
        sys.exit(1)
    admin_token = res["data"]["accessToken"]
    admin_id = res["data"]["user"]["id"]
    print(f"Admin logged in. ID: {admin_id}")

    # 2. Setup Test Identities
    print("\n[Phase 2] Seeding Test Identities...")
    artisan1_email, _, artisan1_id, artisan1_token = create_user("ARTISAN", "Kaddour", "Elfen", admin_token)
    artisan2_email, _, artisan2_id, artisan2_token = create_user("ARTISAN", "Brahim", "Sanai", admin_token)
    client1_email, _, client1_id, client1_token = create_user("CLIENT", "Walid", "Client", admin_token)
    client2_email, _, client2_id, client2_token = create_user("CLIENT", "Amine", "Premium", admin_token)
    print(f"Artisan 1: {artisan1_email} ({artisan1_id})")
    print(f"Artisan 2: {artisan2_email} ({artisan2_id})")
    print(f"Client 1 (Standard): {client1_email} ({client1_id})")
    print(f"Client 2 (Premium Target): {client2_email} ({client2_id})")

    # 3. Create Sample Artifacts (Certifications, Gallery Images, Formateur Requests, Plans, Notifications)
    print("\n[Phase 3] Seeding Domain Artifacts for Read Tests...")

    # Create temp files for uploads
    tmp_pdf = "/tmp/souklab_test_cert.pdf"
    with open(tmp_pdf, "wb") as f:
        f.write(b"%PDF-1.4\n%Fake PDF content for testing certifications\n%%EOF")

    # Tiny 1x1 valid PNG
    tmp_png = "/tmp/souklab_test_image.png"
    png_bytes = bytes.fromhex("89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c4890000000a49444154789c63000100000500010d0a2db40000000049454e44ae426082")
    with open(tmp_png, "wb") as f:
        f.write(png_bytes)

    # Artisan 1: Certification
    status, res = curl("POST", "/api/v1/artisan/certifications", token=artisan1_token,
                       files={"file": f"@{tmp_pdf};type=application/pdf", "title": "Master Potter", "issuer": "CAM Alger"})
    artisan1_cert_id = res.get("data", {}).get("id") if status == 201 else None
    print(f"Artisan 1 cert upload: {status}, ID: {artisan1_cert_id}")

    # Artisan 2: Certification
    status, res = curl("POST", "/api/v1/artisan/certifications", token=artisan2_token,
                       files={"file": f"@{tmp_pdf};type=application/pdf", "title": "Woodcraft Pro", "issuer": "CAM Oran"})
    artisan2_cert_id = res.get("data", {}).get("id") if status == 201 else None
    print(f"Artisan 2 cert upload: {status}, ID: {artisan2_cert_id}")

    # Artisan 1: Gallery image
    status, res = curl("POST", "/api/v1/artisan/gallery", token=artisan1_token,
                       files={"file": f"@{tmp_png};type=image/png", "title": "Ceramic Vase", "caption": "Handmade in Tizi"})
    artisan1_gallery_id = res.get("data", {}).get("id") if status == 201 else None
    print(f"Artisan 1 gallery upload: {status}, ID: {artisan1_gallery_id}")

    # Artisan 2: Gallery image
    status, res = curl("POST", "/api/v1/artisan/gallery", token=artisan2_token,
                       files={"file": f"@{tmp_png};type=image/png", "title": "Carved Table", "caption": "Solid cedar"})
    artisan2_gallery_id = res.get("data", {}).get("id") if status == 201 else None
    print(f"Artisan 2 gallery upload: {status}, ID: {artisan2_gallery_id}")

    # Artisan 1: Formateur request
    status, res = curl("POST", "/api/v1/artisan/formateur-request", token=artisan1_token,
                       body=json.dumps({"motivation": "I want to teach pottery masterclasses"}))
    artisan1_req_id = res.get("data", {}).get("id") if status == 201 else None
    print(f"Artisan 1 formateur request: {status}, ID: {artisan1_req_id}")

    # Artisan 2: Formateur request
    status, res = curl("POST", "/api/v1/artisan/formateur-request", token=artisan2_token,
                       body=json.dumps({"motivation": "Carpentry instructor application"}))
    artisan2_req_id = res.get("data", {}).get("id") if status == 201 else None
    print(f"Artisan 2 formateur request: {status}, ID: {artisan2_req_id}")

    # Complete Client 2 profile so they have a client entity
    status, res = curl("POST", "/api/v1/auth/complete-profile", token=client2_token,
                       body=json.dumps({"clientType": "INDIVIDUAL"}))
    print(f"Client 2 completed profile: {status}")

    # Admin: Subscription Plans (1 Active, 1 Inactive)
    active_plan_payload = {
        "subscriberType": "CLIENT", "name": f"Premium Client Plan {uuid4().hex[:4]}",
        "description": "Full access to artisan directory and chat",
        "billingPeriod": "MONTHLY", "amount": 1500, "active": True,
        "reason": "Test active plan", "entitlements": {"chat": "true", "contact_info": "true"}
    }
    status, res = curl("POST", "/api/v1/admin/subscription-plans", token=admin_token, body=json.dumps(active_plan_payload))
    active_plan_id = res.get("data", {}).get("id") if status == 200 else None
    print(f"Created active plan: {status}, ID: {active_plan_id}")

    inactive_plan_payload = {
        "subscriberType": "CLIENT", "name": f"Legacy Client Plan {uuid4().hex[:4]}",
        "description": "Deactivated plan",
        "billingPeriod": "MONTHLY", "amount": 900, "active": False,
        "reason": "Test inactive plan", "entitlements": {"chat": "false"}
    }
    status, res = curl("POST", "/api/v1/admin/subscription-plans", token=admin_token, body=json.dumps(inactive_plan_payload))
    inactive_plan_id = res.get("data", {}).get("id") if status == 200 else None
    print(f"Created inactive plan: {status}, ID: {inactive_plan_id}")

    # Admin: Manually grant active plan to Client 2 (making client2 Premium)
    grant_payload = {
        "accountId": client2_id, "planId": active_plan_id, "reason": "Test premium grant"
    }
    status, res = curl("POST", "/api/v1/admin/subscriptions/grant", token=admin_token, body=json.dumps(grant_payload))
    client2_sub_id = res.get("data", {}).get("id") if status == 200 else None
    print(f"Granted subscription to Client 2: {status}, Subscription ID: {client2_sub_id}")

    # Client 1: Upload avatar
    status, res = curl("POST", "/api/v1/users/me/avatars", token=client1_token,
                       files={"file": f"@{tmp_png};type=image/png"})
    client1_avatar_id = res.get("data", {}).get("id") if status == 201 else None
    print(f"Client 1 avatar upload: {status}, ID: {client1_avatar_id}")

    # Client 2: Upload avatar
    status, res = curl("POST", "/api/v1/users/me/avatars", token=client2_token,
                       files={"file": f"@{tmp_png};type=image/png"})
    client2_avatar_id = res.get("data", {}).get("id") if status == 201 else None
    print(f"Client 2 avatar upload: {status}, ID: {client2_avatar_id}")

    # Client 1: Read a notification
    status, res = curl("GET", "/api/v1/notifications", token=client1_token)
    items = res.get("data", {}).get("items", []) if isinstance(res, dict) else []
    client1_notif_id = items[0]["id"] if items else None
    if not client1_notif_id:
        notif_uuid = str(uuid4())
        run_db(f"INSERT INTO notifications (id, user_id, message, is_read, created_at, updated_at) VALUES ('{notif_uuid}', '{client1_id}', 'Welcome!', 0, NOW(), NOW());")
        client1_notif_id = notif_uuid
    print(f"Client 1 notification ID: {client1_notif_id}")

    # Client 2 notification
    notif2_uuid = str(uuid4())
    run_db(f"INSERT INTO notifications (id, user_id, message, is_read, created_at, updated_at) VALUES ('{notif2_uuid}', '{client2_id}', 'Premium active!', 0, NOW(), NOW());")
    client2_notif_id = notif2_uuid
    print(f"Client 2 notification ID: {client2_notif_id}")

    # Create Feed Post (Author Artisan 1)
    feed_payload = {"title": "Ceramics showcase", "body": "Discover authentic Algerian pottery", "type": "ACTUALITE"}
    status, res = curl("POST", "/api/v1/feed", token=artisan1_token, body=json.dumps(feed_payload))
    feed_post_id = res.get("data", {}).get("id") if status in (200, 201) else None
    print(f"Feed post created: {status}, ID: {feed_post_id}")

    # Seed Formation
    formation_id = str(uuid4())
    run_db(f"INSERT INTO formations (id, author_id, title, description, price, currency, duration_hours, is_online, max_participants, status, created_at, updated_at) "
           f"VALUES ('{formation_id}', '{artisan1_id}', 'Traditional Pottery Workshop', 'Hands-on class', 5000, 'DZD', 3, 0, 10, 'PENDING_REVIEW', NOW(), NOW());")
    print(f"Formation created: ID: {formation_id}")

    # ======================================================================
    # TEST SUITE: 12 CRUD ENDPOINTS & ALL POSSIBLE SCENARIOS
    # ======================================================================

    fuzz_ids = [
        ("00000000-0000-0000-0000-000000000000", "Zero-UUID"),
        ("nonexistent-random-id", "Random-String"),
        ("'%20OR%201=1%20--", "SQL-Injection-1"),
        ("1;DROP%20TABLE%20users;", "SQL-Injection-2"),
        ("..%2F..%2Fetc%2Fpasswd", "Path-Traversal"),
        ("%3Cscript%3Ealert(1)%3C%2Fscript%3E", "XSS-Vector"),
        ("a" * 500, "Long-String-500"),
        ("%20%20", "Whitespace-ID"),
        ("%00", "Null-Byte"),
    ]

    print("\n[Phase 4] Testing Endpoint 1: GET /api/v1/artisan/certifications/{id}...")
    s, _ = curl("GET", f"/api/v1/artisan/certifications/{artisan1_cert_id}")
    record_test("EP1: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/artisan/certifications/{artisan1_cert_id}", token="invalid.token.here")
    record_test("EP1: Invalid Bearer -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/artisan/certifications/{artisan1_cert_id}", token=client1_token)
    record_test("EP1: Client forbidden -> 403", 403, s)
    s, r = curl("GET", f"/api/v1/artisan/certifications/{artisan1_cert_id}", token=artisan1_token)
    record_test("EP1: Owner Artisan1 success -> 200", 200, s)
    s, _ = curl("GET", f"/api/v1/artisan/certifications/{artisan1_cert_id}", token=artisan2_token)
    record_test("EP1: IDOR Artisan2 viewing Artisan1 cert -> 404", 404, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/artisan/certifications/{f_id}", token=artisan1_token)
        record_test(f"EP1: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")
    if artisan1_cert_id:
        curl("DELETE", f"/api/v1/artisan/certifications/{artisan1_cert_id}", token=artisan1_token)
        s, _ = curl("GET", f"/api/v1/artisan/certifications/{artisan1_cert_id}", token=artisan1_token)
        record_test("EP1: Soft-deleted cert -> 404", 404, s)
    s, _ = curl("POST", f"/api/v1/artisan/certifications/{artisan2_cert_id}", token=artisan2_token)
    record_test("EP1: Method POST on resource -> 405", 405, s)

    print("\n[Phase 5] Testing Endpoint 2: GET /api/v1/artisan/gallery/{id}...")
    s, _ = curl("GET", f"/api/v1/artisan/gallery/{artisan1_gallery_id}")
    record_test("EP2: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/artisan/gallery/{artisan1_gallery_id}", token=client1_token)
    record_test("EP2: Client forbidden -> 403", 403, s)
    s, r = curl("GET", f"/api/v1/artisan/gallery/{artisan1_gallery_id}", token=artisan1_token)
    record_test("EP2: Owner Artisan1 success -> 200", 200, s)
    s, _ = curl("GET", f"/api/v1/artisan/gallery/{artisan1_gallery_id}", token=artisan2_token)
    record_test("EP2: IDOR Artisan2 viewing Artisan1 image -> 404", 404, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/artisan/gallery/{f_id}", token=artisan1_token)
        record_test(f"EP2: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")
    if artisan1_gallery_id:
        curl("DELETE", f"/api/v1/artisan/gallery/{artisan1_gallery_id}", token=artisan1_token)
        s, _ = curl("GET", f"/api/v1/artisan/gallery/{artisan1_gallery_id}", token=artisan1_token)
        record_test("EP2: Soft-deleted gallery -> 404", 404, s)
    s, _ = curl("POST", f"/api/v1/artisan/gallery/{artisan2_gallery_id}", token=artisan2_token)
    record_test("EP2: Method POST on resource -> 405", 405, s)

    print("\n[Phase 6] Testing Endpoint 3: GET /api/v1/subscriptions/plans/{id} (PUBLIC)...")
    s, r = curl("GET", f"/api/v1/subscriptions/plans/{active_plan_id}")
    record_test("EP3: Public unauthenticated active plan -> 200", 200, s)
    s, _ = curl("GET", f"/api/v1/subscriptions/plans/{inactive_plan_id}")
    record_test("EP3: Public unauthenticated inactive plan -> 404", 404, s)
    s, _ = curl("GET", f"/api/v1/subscriptions/plans/{active_plan_id}", token=client1_token)
    record_test("EP3: Client active plan -> 200", 200, s)
    s, _ = curl("GET", f"/api/v1/subscriptions/plans/{inactive_plan_id}", token=client1_token)
    record_test("EP3: Client inactive plan -> 404", 404, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/subscriptions/plans/{f_id}")
        record_test(f"EP3: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")
    s, _ = curl("POST", f"/api/v1/subscriptions/plans/{active_plan_id}")
    record_test("EP3: Unauthenticated Method POST -> 401", 401, s)
    s, _ = curl("DELETE", f"/api/v1/subscriptions/plans/{active_plan_id}")
    record_test("EP3: Unauthenticated Method DELETE -> 401", 401, s)
    s, _ = curl("POST", f"/api/v1/subscriptions/plans/{active_plan_id}", token=client1_token)
    record_test("EP3: Authenticated Method POST -> 405", 405, s)
    s, _ = curl("DELETE", f"/api/v1/subscriptions/plans/{active_plan_id}", token=client1_token)
    record_test("EP3: Authenticated Method DELETE -> 405", 405, s)

    print("\n[Phase 7] Testing Endpoint 4: GET /api/v1/admin/subscription-plans/{id} (ADMIN ONLY)...")
    s, _ = curl("GET", f"/api/v1/admin/subscription-plans/{active_plan_id}")
    record_test("EP4: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/admin/subscription-plans/{active_plan_id}", token=client1_token)
    record_test("EP4: Client forbidden -> 403", 403, s)
    s, _ = curl("GET", f"/api/v1/admin/subscription-plans/{active_plan_id}", token=artisan1_token)
    record_test("EP4: Artisan forbidden -> 403", 403, s)
    s, _ = curl("GET", f"/api/v1/admin/subscription-plans/{active_plan_id}", token=admin_token)
    record_test("EP4: Admin viewing active plan -> 200", 200, s)
    s, _ = curl("GET", f"/api/v1/admin/subscription-plans/{inactive_plan_id}", token=admin_token)
    record_test("EP4: Admin viewing inactive plan -> 200", 200, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/admin/subscription-plans/{f_id}", token=admin_token)
        record_test(f"EP4: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")

    print("\n[Phase 8] Testing Endpoint 5: GET /api/v1/admin/users/{id} (ADMIN ONLY)...")
    s, _ = curl("GET", f"/api/v1/admin/users/{client1_id}")
    record_test("EP5: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/admin/users/{client1_id}", token=client1_token)
    record_test("EP5: Client forbidden -> 403", 403, s)
    s, _ = curl("GET", f"/api/v1/admin/users/{client1_id}", token=artisan1_token)
    record_test("EP5: Artisan forbidden -> 403", 403, s)
    s, r = curl("GET", f"/api/v1/admin/users/{client1_id}", token=admin_token)
    record_test("EP5: Admin viewing user -> 200", 200, s)
    s, r = curl("GET", f"/api/v1/admin/users/{artisan1_id}", token=admin_token)
    record_test("EP5: Admin viewing artisan -> 200", 200, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/admin/users/{f_id}", token=admin_token)
        record_test(f"EP5: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")

    print("\n[Phase 9] Testing Endpoint 6: GET /api/v1/artisan/formateur-requests/{id} (ARTISAN ONLY)...")
    s, _ = curl("GET", f"/api/v1/artisan/formateur-requests/{artisan1_req_id}")
    record_test("EP6: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/artisan/formateur-requests/{artisan1_req_id}", token=client1_token)
    record_test("EP6: Client forbidden -> 403", 403, s)
    s, r = curl("GET", f"/api/v1/artisan/formateur-requests/{artisan1_req_id}", token=artisan1_token)
    record_test("EP6: Owner Artisan1 success -> 200", 200, s)
    s, _ = curl("GET", f"/api/v1/artisan/formateur-requests/{artisan1_req_id}", token=artisan2_token)
    record_test("EP6: IDOR Artisan2 viewing Artisan1 request -> 404", 404, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/artisan/formateur-requests/{f_id}", token=artisan1_token)
        record_test(f"EP6: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")

    print("\n[Phase 10] Testing Endpoint 7: GET /api/v1/admin/formateur-requests/{id} (ADMIN ONLY)...")
    s, _ = curl("GET", f"/api/v1/admin/formateur-requests/{artisan1_req_id}")
    record_test("EP7: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/admin/formateur-requests/{artisan1_req_id}", token=client1_token)
    record_test("EP7: Client forbidden -> 403", 403, s)
    s, _ = curl("GET", f"/api/v1/admin/formateur-requests/{artisan1_req_id}", token=artisan1_token)
    record_test("EP7: Artisan forbidden -> 403", 403, s)
    s, r = curl("GET", f"/api/v1/admin/formateur-requests/{artisan1_req_id}", token=admin_token)
    record_test("EP7: Admin viewing request -> 200", 200, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/admin/formateur-requests/{f_id}", token=admin_token)
        record_test(f"EP7: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")

    print("\n[Phase 11] Testing Endpoint 8: GET /api/v1/admin/formations/{id} (ADMIN ONLY)...")
    s, _ = curl("GET", f"/api/v1/admin/formations/{formation_id}")
    record_test("EP8: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/admin/formations/{formation_id}", token=client1_token)
    record_test("EP8: Client forbidden -> 403", 403, s)
    s, _ = curl("GET", f"/api/v1/admin/formations/{formation_id}", token=artisan1_token)
    record_test("EP8: Artisan forbidden -> 403", 403, s)
    s, r = curl("GET", f"/api/v1/admin/formations/{formation_id}", token=admin_token)
    record_test("EP8: Admin viewing formation -> 200", 200, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/admin/formations/{f_id}", token=admin_token)
        record_test(f"EP8: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")

    print("\n[Phase 12] Testing Endpoint 9: GET /api/v1/admin/subscriptions/{id} (ADMIN ONLY)...")
    s, _ = curl("GET", f"/api/v1/admin/subscriptions/{client2_sub_id}")
    record_test("EP9: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/admin/subscriptions/{client2_sub_id}", token=client1_token)
    record_test("EP9: Client forbidden -> 403", 403, s)
    s, _ = curl("GET", f"/api/v1/admin/subscriptions/{client2_sub_id}", token=artisan1_token)
    record_test("EP9: Artisan forbidden -> 403", 403, s)
    s, r = curl("GET", f"/api/v1/admin/subscriptions/{client2_sub_id}", token=admin_token)
    record_test("EP9: Admin viewing subscription -> 200", 200, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/admin/subscriptions/{f_id}", token=admin_token)
        record_test(f"EP9: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")

    print("\n[Phase 13] Testing Endpoint 10: GET /api/v1/users/me/avatars/{id} (AUTHENTICATED)...")
    s, _ = curl("GET", f"/api/v1/users/me/avatars/{client1_avatar_id}")
    record_test("EP10: No auth -> 401", 401, s)
    s, r = curl("GET", f"/api/v1/users/me/avatars/{client1_avatar_id}", token=client1_token)
    record_test("EP10: Owner Client1 success -> 200", 200, s)
    s, _ = curl("GET", f"/api/v1/users/me/avatars/{client1_avatar_id}", token=client2_token)
    record_test("EP10: IDOR Client2 viewing Client1 avatar -> 404", 404, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/users/me/avatars/{f_id}", token=client1_token)
        record_test(f"EP10: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")

    print("\n[Phase 14] Testing Endpoint 11: GET /api/v1/notifications/{id} (AUTHENTICATED)...")
    s, _ = curl("GET", f"/api/v1/notifications/{client1_notif_id}")
    record_test("EP11: No auth -> 401", 401, s)
    s, r = curl("GET", f"/api/v1/notifications/{client1_notif_id}", token=client1_token)
    record_test("EP11: Owner Client1 success -> 200", 200, s)
    s, _ = curl("GET", f"/api/v1/notifications/{client1_notif_id}", token=client2_token)
    record_test("EP11: IDOR Client2 viewing Client1 notification -> 404", 404, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/notifications/{f_id}", token=client1_token)
        record_test(f"EP11: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")
    if client1_notif_id:
        curl("DELETE", f"/api/v1/notifications/{client1_notif_id}", token=client1_token)
        s, _ = curl("GET", f"/api/v1/notifications/{client1_notif_id}", token=client1_token)
        record_test("EP11: Soft-deleted notification -> 404", 404, s)

    print("\n[Phase 15] Testing Endpoint 12: GET /api/v1/admin/feed/{id} (ADMIN ONLY)...")
    s, _ = curl("GET", f"/api/v1/admin/feed/{feed_post_id}")
    record_test("EP12: No auth -> 401", 401, s)
    s, _ = curl("GET", f"/api/v1/admin/feed/{feed_post_id}", token=client1_token)
    record_test("EP12: Client forbidden -> 403", 403, s)
    s, _ = curl("GET", f"/api/v1/admin/feed/{feed_post_id}", token=artisan2_token)
    record_test("EP12: Artisan forbidden -> 403", 403, s)
    s, r = curl("GET", f"/api/v1/admin/feed/{feed_post_id}", token=admin_token)
    record_test("EP12: Admin viewing feed post -> 200", 200, s)
    for f_id, f_desc in fuzz_ids:
        s, _ = curl("GET", f"/api/v1/admin/feed/{f_id}", token=admin_token)
        record_test(f"EP12: Fuzz ID ({f_desc}) -> 400|404", [400, 404], s, f"Status: {s}")

    # ======================================================================
    # PHASE 16: CHAT & PRIVACY INTEGRITY SCENARIOS
    # ======================================================================
    print("\n[Phase 16] Testing Chat Premium Gating & Artisan Privacy Masking...")
    # 1. Non-Premium Client1 initiates conversation with Artisan 1 -> MUST BE 403
    conv_req = {"recipientUserId": artisan1_id}
    s, r = curl("POST", "/api/v1/conversations", token=client1_token, body=json.dumps(conv_req))
    record_test("CHAT: Non-premium client initiate conversation -> 403", 403, s, f"Response: {r}")

    # 2. Premium Client2 initiates conversation with Artisan 1 -> MUST BE 201/200
    s, r = curl("POST", "/api/v1/conversations", token=client2_token, body=json.dumps(conv_req))
    record_test("CHAT: Premium client initiate conversation -> 201/200", [200, 201], s, f"Response: {r}")
    conv_id = r.get("data", {}).get("id") if isinstance(r, dict) else None
    print(f"Conversation created by Premium Client: ID {conv_id}")

    # 3. Premium Client2 sends a message in conversation -> MUST BE 201
    msg_req = {"idempotencyKey": str(uuid4()), "content": "Hello Artisan, I love your pottery work!"}
    s, r = curl("POST", f"/api/v1/conversations/{conv_id}/messages", token=client2_token, body=json.dumps(msg_req))
    record_test("CHAT: Premium client send message -> 201", 201, s, f"Response: {r}")

    # 4. Premium Client2 views conversation -> participantName MUST BE Artisan's real name ("Kaddour Elfen")
    s, r = curl("GET", f"/api/v1/conversations/{conv_id}", token=client2_token)
    p_name = (r.get("data") or {}).get("participantName", "") if isinstance(r, dict) else ""
    is_real_name = ("Kaddour" in p_name)
    record_test("CHAT: Premium client views unmasked artisan name", True, is_real_name, f"Observed name: {p_name}")

    # 5. Artisan 1 initiates conversation with Client 1 (Artisan can contact anyone) -> 200/201
    s, r = curl("POST", "/api/v1/conversations", token=artisan1_token, body=json.dumps({"recipientUserId": client1_id}))
    artisan_conv_id = (r.get("data") or {}).get("id") if isinstance(r, dict) else None
    record_test("CHAT: Artisan initiate conversation with client -> 201/200", [200, 201], s, f"Response: {r}")

    # 6. Non-premium Client 1 views conversation created by Artisan -> participantName MUST BE MASKED: "Artisan #XXXXX"
    if artisan_conv_id:
        s, r = curl("GET", f"/api/v1/conversations/{artisan_conv_id}", token=client1_token)
        p_name = (r.get("data") or {}).get("participantName", "") if isinstance(r, dict) else ""
        is_masked = p_name.startswith("Artisan #") and "Kaddour" not in p_name
        record_test("CHAT: Non-premium client views masked artisan name", True, is_masked, f"Observed name: {p_name}")

        # 7. Non-premium Client 1 attempts to reply/send message in this conversation -> MUST BE 403 Forbidden!
        s, r = curl("POST", f"/api/v1/conversations/{artisan_conv_id}/messages", token=client1_token, body=json.dumps({
            "idempotencyKey": str(uuid4()), "content": "Trying to bypass premium"
        }))
        record_test("CHAT: Non-premium client send message in existing conversation -> 403", 403, s, f"Response: {r}")

    # ======================================================================
    # FINAL SUMMARY
    # ======================================================================
    print("\n======================================================================")
    print("TEST SUITE SUMMARY")
    print("======================================================================")
    print(f"Total Scenarios Tested: {passed_count + failed_count}")
    print(f"Passed: {passed_count}")
    print(f"Failed: {failed_count}")
    if failures:
        print("\nFailures:")
        for f in failures:
            print(f)
    print("======================================================================")

    # Cleanup temp files
    try:
        os.remove(tmp_pdf)
        os.remove(tmp_png)
    except Exception:
        pass

    return 1 if failed_count > 0 else 0

if __name__ == "__main__":
    sys.exit(main())
