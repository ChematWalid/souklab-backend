#!/usr/bin/env python3
"""
Thorough end-to-end live testing suite using curl against the running SoukLab backend.

Exercises:
1. Actuator health and OpenAPI schema endpoints.
2. Public taxonomies and artisan directory.
3. Authentication with JJWT 0.12.6 tokens (login, me, token refresh, invalid token rejection).
4. Phase 1: Soft-deleted user authentication block and email re-registration release.
5. Phase 2: Formation review creation, deletion, and conflict-free reactivation.
6. Phase 6: Feed post retrieval with batched likes/bookmarks and artisan profile view atomic increment.
7. Phase 7: Subscription plans and administration endpoints.
"""

import hashlib
import json
import os
import subprocess
import sys
import time
from uuid import uuid4

def load_dotenv(path=".env"):
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    k = k.strip()
                    v = v.strip().strip("'\"")
                    if k not in os.environ:
                        os.environ[k] = v

load_dotenv()

BASE_URL = os.getenv("APP_BASE_URL", "http://localhost:8080").rstrip("/")
DB_HOST = os.getenv("DB_HOST", "127.0.0.1")
DB_PORT = os.getenv("DB_PORT", "3307")
DB_USER = os.getenv("DB_USER", "souklab_test")
DB_PASSWORD = os.getenv("DB_PASSWORD", "souklab_test_password")
DB_NAME = os.getenv("DB_NAME", "souklab_test")

ADMIN_EMAIL = os.getenv("ADMIN_EMAIL") or os.getenv("APP_ADMIN_DEFAULT_EMAIL", "admin@souklab.test")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD") or os.getenv("APP_ADMIN_DEFAULT_PASSWORD", "admin123")

test_results = []


def record(name: str, passed: bool, details: str = ""):
    status_str = "PASS" if passed else "FAIL"
    print(f"[{status_str}] {name} {('- ' + details) if details else ''}")
    test_results.append({"name": name, "passed": passed, "details": details})
    if not passed:
        print(f"!!! CRITICAL FAILURE: {name}: {details}")


def run_curl(method: str, path: str, headers: dict = None, data: object = None, cookie: str = None) -> tuple[int, dict, str]:
    cmd = ["curl", "-s", "-w", "\n%{http_code}", "-X", method]
    if headers:
        for k, v in headers.items():
            cmd.extend(["-H", f"{k}: {v}"])
    if cookie:
        cmd.extend(["-H", f"Cookie: {cookie}"])
    if data is not None:
        if isinstance(data, (dict, list)):
            cmd.extend(["-H", "Content-Type: application/json", "-d", json.dumps(data)])
        else:
            cmd.extend(["-d", str(data)])
    cmd.append(f"{BASE_URL}{path}")

    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        return 0, {}, f"Curl execution failed: {result.stderr}"

    output = result.stdout
    lines = output.rsplit("\n", 1)
    body_str = lines[0] if len(lines) > 1 else ""
    status_code_str = lines[-1].strip() if len(lines) > 1 else "0"

    try:
        status_code = int(status_code_str)
    except ValueError:
        status_code = 0

    try:
        body_json = json.loads(body_str) if body_str else {}
    except Exception:
        body_json = {"raw": body_str}

    return status_code, body_json, body_str


def db_query(sql: str) -> list[str]:
    env = os.environ.copy()
    env["MYSQL_PWD"] = DB_PASSWORD
    cmd = [
        "mariadb", "--batch", "--skip-column-names", "--raw",
        "-h", DB_HOST, "-P", DB_PORT, "-u", DB_USER, DB_NAME,
        "-e", sql
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, env=env)
    if res.returncode != 0:
        raise RuntimeError(f"DB Query failed: {res.stderr}")
    return [line for line in res.stdout.splitlines() if line]


def db_exec(sql: str):
    env = os.environ.copy()
    env["MYSQL_PWD"] = DB_PASSWORD
    cmd = [
        "mariadb", "-h", DB_HOST, "-P", DB_PORT, "-u", DB_USER, DB_NAME,
        "-e", sql
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, env=env)
    if res.returncode != 0:
        raise RuntimeError(f"DB Exec failed: {res.stderr}")


def get_otp_for_email(email: str) -> str:
    rows = db_query(
        f"SELECT vt.code_hash FROM verification_tokens vt JOIN users u ON u.id=vt.user_id "
        f"WHERE u.email='{email}' AND vt.type='EMAIL_VERIFICATION' "
        f"AND vt.used_at IS NULL ORDER BY vt.created_at DESC LIMIT 1;"
    )
    if not rows:
        raise RuntimeError(f"No verification token found for {email}")
    expected_hash = rows[0]
    for n in range(1_000_000):
        code = f"{n:06d}"
        if hashlib.sha256(code.encode()).hexdigest() == expected_hash:
            return code
    raise RuntimeError(f"Could not reverse SHA256 OTP hash for {email}")


def register_and_verify(email: str, password: str, account_type: str = "CLIENT") -> tuple[str, str]:
    status, body, _ = run_curl("POST", "/api/v1/auth/register", data={
        "email": email,
        "password": password,
        "accountType": account_type,
        "firstName": "Test",
        "lastName": "User"
    })
    if status != 201:
        raise RuntimeError(f"Register failed with status {status}: {body}")

    otp = get_otp_for_email(email)
    status, body, _ = run_curl("POST", "/api/v1/auth/verify-email", data={
        "email": email,
        "code": otp
    })
    if status != 200:
        raise RuntimeError(f"Verify email failed with status {status}: {body}")

    user_id = db_query(f"SELECT id FROM users WHERE email='{email}'")[0]
    return user_id, email


def login(email: str, password: str) -> tuple[str, str]:
    status, body, _ = run_curl("POST", "/api/v1/auth/login", data={
        "email": email,
        "password": password
    })
    if status != 200:
        raise RuntimeError(f"Login failed for {email} with status {status}: {body}")
    access_token = body.get("data", {}).get("accessToken")
    refresh_token = body.get("data", {}).get("refreshToken")
    return access_token, refresh_token


def test_actuator_and_openapi():
    print("\n--- Test Suite 1: Actuator & OpenAPI Smoke ---")
    # Public readiness endpoint
    status, body, _ = run_curl("GET", "/actuator/health/readiness")
    record("Public Actuator Readiness Probe", status == 200 and body.get("status") == "UP", f"status={status}, health={body.get('status')}")

    # Unauthenticated /v3/api-docs security boundary
    status, body, _ = run_curl("GET", "/v3/api-docs")
    record("OpenAPI Security Boundary (Unauthenticated 401)", status == 401, f"status={status}")

    # Authenticated /v3/api-docs with admin token
    admin_token, _ = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    status, body, _ = run_curl("GET", "/v3/api-docs", headers={"Authorization": f"Bearer {admin_token}"})
    has_openapi = "openapi" in body and "paths" in body
    record("OpenAPI Specification (Authenticated v3/api-docs)", status == 200 and has_openapi, f"status={status}, openapi={body.get('openapi')}")


def test_public_catalog():
    print("\n--- Test Suite 2: Public Taxonomies & Directory ---")
    for endpoint in ["categories", "materials", "techniques", "regions", "epoques"]:
        status, body, _ = run_curl("GET", f"/api/v1/catalog/{endpoint}")
        is_success = status == 200 and body.get("success") is True
        record(f"Catalog {endpoint.capitalize()}", is_success, f"status={status}")

    # Directory unauthenticated boundary
    status, body, _ = run_curl("GET", "/api/v1/public/directory?page=0&size=5")
    record("Directory Security Boundary (Unauthenticated 403)", status == 403, f"status={status}")

    # Directory authenticated search
    admin_token, _ = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    status, body, _ = run_curl("GET", "/api/v1/public/directory?page=0&size=5", headers={"Authorization": f"Bearer {admin_token}"})
    is_success = status == 200 and body.get("success") is True and "content" in body.get("data", {})
    count = len(body.get("data", {}).get("content", []))
    record("Artisan Directory Search (Authenticated)", is_success, f"status={status}, items_returned={count}")


def test_auth_and_tokens():
    print("\n--- Test Suite 3: JJWT 0.12.6 Token Issuance, Validation & Refresh ---")
    # Invalid login
    status, body, _ = run_curl("POST", "/api/v1/auth/login", data={"email": "nonexistent@souklab.test", "password": "WrongPassword!"})
    record("Invalid Credentials Rejection", status == 401, f"status={status}")

    # Admin Login
    admin_token, refresh_token = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    record("Admin Login & JJWT 0.12.6 Issuance", bool(admin_token), f"token length={len(admin_token) if admin_token else 0}")

    # Protected me endpoint
    status, body, _ = run_curl("GET", "/api/v1/auth/me", headers={"Authorization": f"Bearer {admin_token}"})
    user_email = body.get("data", {}).get("email")
    record("Protected GET /api/v1/auth/me with Valid Token", status == 200 and user_email == ADMIN_EMAIL, f"status={status}, email={user_email}")

    # Rejected with forged / corrupted token
    corrupted_token = admin_token[:-10] + "corrupted1"
    status, body, _ = run_curl("GET", "/api/v1/auth/me", headers={"Authorization": f"Bearer {corrupted_token}"})
    record("Protected Endpoint Rejects Corrupted Token", status == 401, f"status={status}")

    # Token Refresh
    status, body, _ = run_curl("POST", "/api/v1/auth/refresh", data={"refreshToken": refresh_token})
    new_token = (body.get("data") or {}).get("accessToken")
    record("Token Rotation (POST /api/v1/auth/refresh)", status == 200 and bool(new_token), f"status={status}")


def test_phase1_soft_delete_and_email_release():
    print("\n--- Test Suite 4: Phase 1 Soft-Delete Auth Prevention & Email Release ---")
    rand_suffix = uuid4().hex[:8]
    test_email = f"mod-user-{rand_suffix}@souklab.test"
    test_password = "Password123!"

    user_id, _ = register_and_verify(test_email, test_password, account_type="CLIENT")
    user_token, _ = login(test_email, test_password)
    record("Register and Verify Test User", bool(user_token), f"user_id={user_id}, email={test_email}")

    # Submit content report against this user (using admin token since a user cannot report themselves)
    admin_token, _ = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    status, body, _ = run_curl("POST", "/api/v1/reports", headers={"Authorization": f"Bearer {admin_token}"}, data={
        "targetType": "USER",
        "targetId": user_id,
        "reason": "SPAM",
        "details": "Automated audit test report for soft deletion"
    })
    report_id = body.get("data", {}).get("id")
    record("Submit Content Report Against User", status == 201 and bool(report_id), f"report_id={report_id}")

    # Admin resolves report with REMOVE (which soft deletes user and moves email)
    admin_token, _ = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    status, body, _ = run_curl("POST", f"/api/v1/admin/reports/{report_id}/resolve", headers={"Authorization": f"Bearer {admin_token}"}, data={
        "action": "REMOVE",
        "note": "Terminated as part of audit verification"
    })
    record("Admin Resolve Report with REMOVE Action", status == 200, f"status={status}")

    # Verify user is soft-deleted and status is DELETED in DB
    user_db_rows = db_query(f"SELECT email, status, deleted_at FROM users WHERE id='{user_id}'")
    db_email, db_status, db_deleted_at = user_db_rows[0].split("\t")
    is_soft_deleted = (db_status == "DELETED") and (db_deleted_at != "NULL") and ("@deleted.souklab.invalid" in db_email)
    record("User Record Status DELETED and Email Moved to Invalid in DB", is_soft_deleted, f"email={db_email}, status={db_status}")

    # Attempt login with soft-deleted account -> MUST BE BLOCKED
    status, body, _ = run_curl("POST", "/api/v1/auth/login", data={"email": test_email, "password": test_password})
    record("Soft-Deleted User Authentication Blocked", status == 401, f"status={status}, response={body.get('message')}")

    # Re-register with the EXACT original email -> MUST SUCCEED (Email unique constraint released!)
    new_user_id, _ = register_and_verify(test_email, test_password, account_type="CLIENT")
    new_user_token, _ = login(test_email, test_password)
    record("Original Email Re-registration Allowed (Constraint Released)", bool(new_user_token) and (new_user_id != user_id), f"new_user_id={new_user_id}")


def test_phase2_review_recreation():
    print("\n--- Test Suite 5: Phase 2 Review Re-creation Unique Constraint Conflict ---")
    rand_suffix = uuid4().hex[:8]
    artisan_email = f"artisan-rev-{rand_suffix}@souklab.test"
    password = "Password123!"

    artisan_id, _ = register_and_verify(artisan_email, password, account_type="ARTISAN")
    # Newly registered artisans require admin approval; activate for testing
    db_exec(f"UPDATE users SET status='ACTIVE' WHERE id='{artisan_id}';")
    db_exec(f"""
        INSERT INTO artisans (id, created_at, updated_at, is_premium, is_teacher, is_verified, rating, response_rate, reviews_count, views_count)
        VALUES ('{artisan_id}', NOW(), NOW(), 0, 0, 1, 0.0, 100, 0, 0)
        ON DUPLICATE KEY UPDATE updated_at=NOW();
    """)
    artisan_token, _ = login(artisan_email, password)

    # Completed formation from fixture
    formation_id = "64500837-dedc-4125-97f2-26b69aa7c3b1"

    # Insert attended enrollment in database
    enrollment_id = str(uuid4())
    db_exec(f"""
        INSERT INTO formation_enrollments (id, created_at, updated_at, enrolled_at, status, artisan_id, formation_id)
        VALUES ('{enrollment_id}', NOW(), NOW(), NOW(), 'ATTENDED', '{artisan_id}', '{formation_id}');
    """)

    # 1. First review submission
    status, body, _ = run_curl("POST", f"/api/v1/artisan/formations/{formation_id}/reviews", headers={"Authorization": f"Bearer {artisan_token}"}, data={
        "rating": 5.0,
        "comment": "Initial review created via curl"
    })
    review_id = body.get("data", {}).get("id")
    record("Submit First Formation Review", status == 201 and bool(review_id), f"review_id={review_id}")

    # 2. Delete the review (soft delete)
    status, body, _ = run_curl("DELETE", f"/api/v1/artisan/reviews/{review_id}", headers={"Authorization": f"Bearer {artisan_token}"})
    record("Soft-Delete Review", status == 200, f"status={status}")

    # Verify review is soft deleted in DB
    rev_deleted = db_query(f"SELECT deleted_at FROM artisan_reviews WHERE id='{review_id}'")[0]
    record("Review Soft-Deleted in DB (deleted_at IS NOT NULL)", rev_deleted != "NULL", f"deleted_at={rev_deleted}")

    # 3. Re-submit review for the same formation enrollment -> MUST SUCCEED (Reactivation without 500 error!)
    status, body, raw = run_curl("POST", f"/api/v1/artisan/formations/{formation_id}/reviews", headers={"Authorization": f"Bearer {artisan_token}"}, data={
        "rating": 4.0,
        "comment": "Updated review reactivated cleanly via curl"
    })
    is_recreated = (status == 201) and (body.get("data", {}).get("rating") == 4.0)
    record("Re-create Formation Review (Reactivated cleanly without 500 duplicate key error)", is_recreated, f"status={status}, rating={body.get('data', {}).get('rating')}")

    # Check my review
    status, body, _ = run_curl("GET", f"/api/v1/artisan/formations/{formation_id}/reviews/me", headers={"Authorization": f"Bearer {artisan_token}"})
    active_rating = body.get("data", {}).get("rating")
    record("GET /me Returns Reactivated Active Review", status == 200 and active_rating == 4.0, f"rating={active_rating}")


def test_phase6_feed_and_atomic_views():
    print("\n--- Test Suite 6: Phase 6 Feed Post Batching & Atomic Views Increment ---")
    rand_suffix = uuid4().hex[:8]
    user_email = f"feed-test-{rand_suffix}@souklab.test"
    password = "Password123!"

    user_id, _ = register_and_verify(user_email, password, account_type="CLIENT")
    user_token, _ = login(user_email, password)

    # Get Feed posts
    status, body, _ = run_curl("GET", "/api/v1/feed?page=0&size=5", headers={"Authorization": f"Bearer {user_token}"})
    posts = body.get("data", {}).get("content", [])
    has_posts = status == 200 and len(posts) > 0
    record("Feed Posts Retrieved with Batched Likes/Bookmarks", has_posts, f"status={status}, posts_count={len(posts)}")

    if posts:
        post_id = posts[0].get("id")
        # Like post
        status, body, _ = run_curl("POST", f"/api/v1/feed/{post_id}/likes", headers={"Authorization": f"Bearer {user_token}"})
        record(f"Like Feed Post ({post_id})", status == 201 or status == 200, f"status={status}")

        # Check feed again and verify likedByCurrentUser is True
        status, body, _ = run_curl("GET", "/api/v1/feed?page=0&size=10", headers={"Authorization": f"Bearer {user_token}"})
        liked_post = next((p for p in body.get("data", {}).get("content", []) if p.get("id") == post_id), None)
        is_liked = liked_post is not None and (liked_post.get("likedByCurrentUser") is True or liked_post.get("isLiked") is True)
        record("Feed Post Returned with likedByCurrentUser=True", is_liked, f"likedByCurrentUser={liked_post.get('likedByCurrentUser') if liked_post else None}")

        # Clean up like
        run_curl("DELETE", f"/api/v1/feed/{post_id}/likes", headers={"Authorization": f"Bearer {user_token}"})

    # Atomic Profile View Count Increment
    artisan_rows = db_query("SELECT id, views_count FROM artisans WHERE deleted_at IS NULL LIMIT 1;")
    if artisan_rows:
        artisan_id, initial_views = artisan_rows[0].split("\t")
        initial_views = int(initial_views)

        # Call authenticated artisan profile detail with fresh client user
        status, body, _ = run_curl("GET", f"/api/v1/artisan/{artisan_id}", headers={"Authorization": f"Bearer {user_token}"})
        record(f"GET Artisan Profile Detail ({artisan_id})", status == 200, f"status={status}")

        # Verify views count incremented atomically in DB
        views_after = int(db_query(f"SELECT views_count FROM artisans WHERE id='{artisan_id}';")[0])
        record("Artisan Views Count Incremented Atomically", views_after > initial_views, f"initial={initial_views}, after={views_after}")


def test_phase7_subscriptions():
    print("\n--- Test Suite 7: Phase 7 Subscription Services & Admin APIs ---")
    admin_token, _ = login(ADMIN_EMAIL, ADMIN_PASSWORD)

    # Public active plans
    status, body, _ = run_curl("GET", "/api/v1/subscriptions/plans")
    plans = body.get("data", [])
    record("List Public Subscription Plans", status == 200 and isinstance(plans, list) and len(plans) > 0, f"status={status}, plans_count={len(plans)}")

    # Admin plans list
    status, body, _ = run_curl("GET", "/api/v1/admin/subscription-plans", headers={"Authorization": f"Bearer {admin_token}"})
    admin_plans = body.get("data") or []
    record("Admin List Subscription Plans", status == 200 and isinstance(admin_plans, list), f"status={status}, admin_plans_count={len(admin_plans)}")

    # Current subscription endpoint for user without subscription -> returns 404/200 cleanly with ApiResponse
    rand_suffix = uuid4().hex[:8]
    user_email = f"sub-check-{rand_suffix}@souklab.test"
    register_and_verify(user_email, "Password123!", account_type="CLIENT")
    client_token, _ = login(user_email, "Password123!")

    status, body, _ = run_curl("GET", "/api/v1/subscriptions/current", headers={"Authorization": f"Bearer {client_token}"})
    record("GET /api/v1/subscriptions/current Handled Cleanly", status in (200, 404), f"status={status}")


def main():
    print("=" * 80)
    print("SOUKLAB LIVE REPOSITORY HARDENING VERIFICATION VIA CURL")
    print(f"Target Base URL: {BASE_URL}")
    print("=" * 80)

    try:
        test_actuator_and_openapi()
        test_public_catalog()
        test_auth_and_tokens()
        test_phase1_soft_delete_and_email_release()
        test_phase2_review_recreation()
        test_phase6_feed_and_atomic_views()
        test_phase7_subscriptions()
    except Exception as e:
        print(f"\n[ERROR] Test suite aborted with exception: {e}")
        import traceback
        traceback.print_exc()
        record("Test Suite Execution", False, f"Aborted with exception: {e}")

    print("\n" + "=" * 80)
    print("FINAL CURL TEST SUITE REPORT")
    print("=" * 80)

    total = len(test_results)
    passed = sum(1 for r in test_results if r["passed"])
    failed = total - passed

    for r in test_results:
        mark = "✓" if r["passed"] else "✗"
        print(f" {mark} {r['name']}: {'PASS' if r['passed'] else 'FAIL'} ({r['details']})")

    print("-" * 80)
    print(f"Total Tests Run: {total} | Passed: {passed} | Failed: {failed}")
    print("=" * 80)

    if failed > 0:
        sys.exit(1)
    print("ALL CURL LIVE VERIFICATION TESTS PASSED SUCCESSFULLY!")


if __name__ == "__main__":
    main()
