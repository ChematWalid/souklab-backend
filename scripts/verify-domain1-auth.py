#!/usr/bin/env python3
"""Exhaustive live verification for Domain 1: Auth & Identity.

Tests all endpoints in AuthController against a live Spring Boot instance.
Uses direct DB access to resolve OTP hashes and verify state transitions.
"""

from __future__ import annotations

import hashlib
import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from typing import Any

BASE_URL = os.getenv("APP_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
DB_HOST = os.getenv("VERIFY_DB_HOST", "127.0.0.1")
DB_PORT = os.getenv("VERIFY_DB_PORT", "3307")
DB_NAME = os.getenv("VERIFY_DB_NAME", "souklab_test")
DB_USER = os.getenv("VERIFY_DB_USER", "souklab_test")
DB_PASS = os.getenv("VERIFY_DB_PASSWORD", "souklab_test_password")

RUN_ID = f"d1-{int(time.time())}"

results: list[dict[str, Any]] = []

def db_query(sql: str) -> list[list[str]]:
    env = os.environ.copy()
    env["MYSQL_PWD"] = DB_PASS
    cmd = [
        "mariadb", "--batch", "--skip-column-names", "--raw",
        "-h", DB_HOST, "-P", DB_PORT, "-u", DB_USER, DB_NAME, "-e", sql
    ]
    res = subprocess.run(cmd, env=env, text=True, capture_output=True, check=True)
    return [line.split("\t") for line in res.stdout.splitlines() if line]

def db_exec(sql: str):
    env = os.environ.copy()
    env["MYSQL_PWD"] = DB_PASS
    cmd = [
        "mariadb",
        "-h", DB_HOST, "-P", DB_PORT, "-u", DB_USER, DB_NAME, "-e", sql
    ]
    subprocess.run(cmd, env=env, text=True, capture_output=True, check=True)

def find_otp(email: str, token_type: str) -> str:
    rows = db_query(
        f"SELECT vt.code_hash FROM verification_tokens vt "
        f"JOIN users u ON u.id = vt.user_id "
        f"WHERE u.email = '{email.lower()}' AND vt.type = '{token_type}' AND vt.used_at IS NULL "
        f"ORDER BY vt.created_at DESC LIMIT 1"
    )
    if not rows:
        raise RuntimeError(f"No active {token_type} token found for {email}")
    target_hash = rows[0][0]
    for val in range(1_000_000):
        code = f"{val:06d}"
        if hashlib.sha256(code.encode()).hexdigest() == target_hash:
            return code
    raise RuntimeError(f"Could not crack 6-digit OTP for hash {target_hash}")

def http_request(
    method: str,
    path: str,
    payload: Any = None,
    raw_body: bytes | None = None,
    token: str | None = None,
    content_type: str | None = "application/json",
) -> tuple[int, dict[str, str], Any]:
    url = BASE_URL + path
    headers = {"Accept": "application/json"}
    if content_type and (payload is not None or raw_body is not None):
        headers["Content-Type"] = content_type
    if token:
        headers["Authorization"] = f"Bearer {token}"
    
    if raw_body is not None:
        body = raw_body
    elif payload is not None:
        body = json.dumps(payload).encode("utf-8")
    else:
        body = None

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

def assert_scenario(
    scenario_id: str,
    name: str,
    method: str,
    path: str,
    expected_status: int | set[int],
    payload: Any = None,
    raw_body: bytes | None = None,
    token: str | None = None,
    content_type: str | None = "application/json",
    check_fn: Any = None,
) -> tuple[int, Any, bool]:
    status, headers, body = http_request(method, path, payload, raw_body, token, content_type)
    allowed = {expected_status} if isinstance(expected_status, int) else expected_status
    passed = status in allowed
    detail = ""

    if passed and check_fn:
        try:
            check_res = check_fn(status, headers, body)
            if check_res is not True:
                passed = False
                detail = str(check_res)
        except Exception as e:
            passed = False
            detail = f"Custom check exception: {e}"

    if not passed:
        detail_msg = f"HTTP {status}, body={json.dumps(body) if isinstance(body, dict) else str(body)}"
    else:
        detail_msg = "OK"

    expected_str = "/".join(str(s) for s in sorted(allowed))
    results.append({
        "id": scenario_id,
        "name": name,
        "method": method,
        "path": path,
        "expected": expected_str,
        "actual": status,
        "passed": passed,
        "detail": detail_msg if not passed else "OK",
        "body": body
    })
    return status, body, passed

def run():
    print(f"--- Starting Domain 1 (Auth & Identity) Verification [run_id: {RUN_ID}] ---")
    
    # 1. Registration tests
    client_email = f"client-{RUN_ID}@souklab.test"
    artisan_email = f"artisan-{RUN_ID}@souklab.test"
    password = "SecurePassword123!"
    
    # S1.01: Happy path client registration
    st, body, ok = assert_scenario(
        "S1.01", "Client registration", "POST", "/api/v1/auth/register", 201,
        {"email": client_email, "password": password, "firstName": "Karim", "lastName": "Client", "accountType": "CLIENT"},
        check_fn=lambda s, h, b: b.get("success") is True and b.get("data", {}).get("email") == client_email
    )
    if not ok: print(f"FAIL S1.01 body: {body}")

    # S1.02: Happy path artisan registration
    st, body, ok = assert_scenario(
        "S1.02", "Artisan registration", "POST", "/api/v1/auth/register", 201,
        {"email": artisan_email, "password": password, "firstName": "Ahmed", "lastName": "Artisan", "accountType": "ARTISAN"},
        check_fn=lambda s, h, b: b.get("success") is True and b.get("data", {}).get("accountStatus") == "PENDING"
    )
    if not ok: print(f"FAIL S1.02 body: {body}")

    # S1.03: Duplicate registration conflict
    st, body, ok = assert_scenario(
        "S1.03", "Duplicate registration conflict", "POST", "/api/v1/auth/register", 409,
        {"email": client_email, "password": password, "accountType": "CLIENT"},
        check_fn=lambda s, h, b: b.get("success") is False and b.get("code") == 409
    )
    if not ok: print(f"FAIL S1.03 body: {body}")

    # S1.04: Missing required fields
    st, body, ok = assert_scenario(
        "S1.04", "Registration empty body validation", "POST", "/api/v1/auth/register", 422,
        {},
        check_fn=lambda s, h, b: b.get("success") is False and b.get("code") == 422 and isinstance(b.get("errors"), (list, dict))
    )
    if not ok: print(f"FAIL S1.04 body: {body}")

    # S1.05: Invalid email format
    st, body, ok = assert_scenario(
        "S1.05", "Registration invalid email validation", "POST", "/api/v1/auth/register", 422,
        {"email": "notanemail", "password": password, "accountType": "CLIENT"},
        check_fn=lambda s, h, b: any("email" in str(e).lower() for e in b.get("errors", []))
    )
    if not ok: print(f"FAIL S1.05 body: {body}")

    # S1.06: Password too short
    st, body, ok = assert_scenario(
        "S1.06", "Registration short password validation", "POST", "/api/v1/auth/register", 422,
        {"email": f"shortpass-{RUN_ID}@souklab.test", "password": "short", "accountType": "CLIENT"},
        check_fn=lambda s, h, b: any("password" in str(e).lower() for e in b.get("errors", []))
    )
    if not ok: print(f"FAIL S1.06 body: {body}")

    # S1.07: Missing accountType
    st, body, ok = assert_scenario(
        "S1.07", "Registration missing accountType validation", "POST", "/api/v1/auth/register", 422,
        {"email": f"noacct-{RUN_ID}@souklab.test", "password": password},
        check_fn=lambda s, h, b: any("account" in str(e).lower() or "type" in str(e).lower() for e in b.get("errors", []))
    )
    if not ok: print(f"FAIL S1.07 body: {body}")

    # S1.08: Malformed JSON
    st, body, ok = assert_scenario(
        "S1.08", "Registration malformed JSON syntax", "POST", "/api/v1/auth/register", 400,
        raw_body=b'{"email": "badjson',
        check_fn=lambda s, h, b: b.get("code") == 400 or b.get("status") == 400
    )
    if not ok: print(f"FAIL S1.08 body: {body}")

    # 2. Email Verification tests
    # S1.09: Invalid verification code format (not 6 digits)
    st, body, ok = assert_scenario(
        "S1.09", "Verify email invalid code format", "POST", "/api/v1/auth/verify-email", 422,
        {"email": client_email, "code": "123"}
    )
    if not ok: print(f"FAIL S1.09 body: {body}")

    # S1.10: Invalid email format in verify
    st, body, ok = assert_scenario(
        "S1.10", "Verify email invalid email format", "POST", "/api/v1/auth/verify-email", 422,
        {"email": "bademail", "code": "123456"}
    )
    if not ok: print(f"FAIL S1.10 body: {body}")

    # S1.11: Verification with wrong OTP
    st, body, ok = assert_scenario(
        "S1.11", "Verify email wrong OTP code", "POST", "/api/v1/auth/verify-email", 400,
        {"email": client_email, "code": "999999"}
    )
    if not ok: print(f"FAIL S1.11 body: {body}")

    # S1.12: Happy path verification with valid OTP
    client_otp = find_otp(client_email, "EMAIL_VERIFICATION")
    st, body, ok = assert_scenario(
        "S1.12", "Verify email valid OTP", "POST", "/api/v1/auth/verify-email", 200,
        {"email": client_email, "code": client_otp},
        check_fn=lambda s, h, b: b.get("success") is True
    )
    if not ok: print(f"FAIL S1.12 body: {body}")
    # Verify DB flag
    verified_rows = db_query(f"SELECT email_verified FROM users WHERE email='{client_email}'")
    if not verified_rows or verified_rows[0][0] not in ('1', '\x01'):
        print(f"FAIL S1.12: DB email_verified is not 1 (actual: {verified_rows})")

    # S1.13: Replay attack with already used OTP
    st, body, ok = assert_scenario(
        "S1.13", "Verify email replay attack", "POST", "/api/v1/auth/verify-email", 400,
        {"email": client_email, "code": client_otp}
    )
    if not ok: print(f"FAIL S1.13 body: {body}")

    # S1.14: Verification for non-existent user
    st, body, ok = assert_scenario(
        "S1.14", "Verify email non-existent user", "POST", "/api/v1/auth/verify-email", {400, 404},
        {"email": f"nosuch-{RUN_ID}@souklab.test", "code": "123456"}
    )
    if not ok: print(f"FAIL S1.14 body: {body}")

    # 3. Resend Verification tests
    # Register an unverified client for resend testing
    unverified_email = f"unverified-{RUN_ID}@souklab.test"
    http_request("POST", "/api/v1/auth/register", {"email": unverified_email, "password": password, "accountType": "CLIENT"})
    old_unverified_otp = find_otp(unverified_email, "EMAIL_VERIFICATION")

    # S1.15: Resend verification happy path
    st, body, ok = assert_scenario(
        "S1.15", "Resend verification happy path", "POST", "/api/v1/auth/resend-verification", 200,
        {"email": unverified_email},
        check_fn=lambda s, h, b: b.get("success") is True
    )
    if not ok: print(f"FAIL S1.15 body: {body}")
    new_unverified_otp = find_otp(unverified_email, "EMAIL_VERIFICATION")
    # Verify old token was invalidated or new token was created
    if old_unverified_otp == new_unverified_otp:
        print("Note: OTP matched old OTP or regenerated same value")

    # S1.16: Resend verification anti-enumeration
    st, body, ok = assert_scenario(
        "S1.16", "Resend verification non-existent email", "POST", "/api/v1/auth/resend-verification", 200,
        {"email": f"ghost-{RUN_ID}@souklab.test"}
    )
    if not ok: print(f"FAIL S1.16 body: {body}")

    # S1.17: Resend verification invalid email
    st, body, ok = assert_scenario(
        "S1.17", "Resend verification invalid email format", "POST", "/api/v1/auth/resend-verification", 422,
        {"email": "not-valid-email"}
    )
    if not ok: print(f"FAIL S1.17 body: {body}")

    # S1.18: Resend verification malformed JSON
    st, body, ok = assert_scenario(
        "S1.18", "Resend verification malformed JSON", "POST", "/api/v1/auth/resend-verification", 400,
        raw_body=b'{"email": '
    )
    if not ok: print(f"FAIL S1.18 body: {body}")

    # 4. Login tests
    # S1.19: Login verified active client
    st, body, ok = assert_scenario(
        "S1.19", "Login verified active client", "POST", "/api/v1/auth/login", 200,
        {"email": client_email, "password": password},
        check_fn=lambda s, h, b: "accessToken" in b.get("data", {}) and "refreshToken" in b.get("data", {})
    )
    if not ok: print(f"FAIL S1.19 body: {body}")
    client_access_token = body.get("data", {}).get("accessToken") if ok else None
    client_refresh_token = body.get("data", {}).get("refreshToken") if ok else None

    # S1.20: Login unverified client
    st, body, ok = assert_scenario(
        "S1.20", "Login unverified client rejected", "POST", "/api/v1/auth/login", 403,
        {"email": unverified_email, "password": password},
        check_fn=lambda s, h, b: b.get("success") is False
    )
    if not ok: print(f"FAIL S1.20 body: {body}")

    # S1.21: Login pending artisan rejected
    st, body, ok = assert_scenario(
        "S1.21", "Login pending artisan rejected", "POST", "/api/v1/auth/login", 403,
        {"email": artisan_email, "password": password},
        check_fn=lambda s, h, b: b.get("success") is False
    )
    if not ok: print(f"FAIL S1.21 body: {body}")

    # S1.22: Login incorrect password
    st, body, ok = assert_scenario(
        "S1.22", "Login incorrect password", "POST", "/api/v1/auth/login", 401,
        {"email": client_email, "password": "WrongPassword!"}
    )
    if not ok: print(f"FAIL S1.22 body: {body}")

    # S1.23: Login non-existent email
    st, body, ok = assert_scenario(
        "S1.23", "Login non-existent email", "POST", "/api/v1/auth/login", 401,
        {"email": f"nobody-{RUN_ID}@souklab.test", "password": password}
    )
    if not ok: print(f"FAIL S1.23 body: {body}")

    # S1.24: Login missing password
    st, body, ok = assert_scenario(
        "S1.24", "Login missing password validation", "POST", "/api/v1/auth/login", 422,
        {"email": client_email}
    )
    if not ok: print(f"FAIL S1.24 body: {body}")

    # S1.25: Login missing email & username
    st, body, ok = assert_scenario(
        "S1.25", "Login missing identifier validation", "POST", "/api/v1/auth/login", {400, 422},
        {"password": password}
    )
    if not ok: print(f"FAIL S1.25 body: {body}")

    # S1.26: Login malformed JSON
    st, body, ok = assert_scenario(
        "S1.26", "Login malformed JSON syntax", "POST", "/api/v1/auth/login", 400,
        raw_body=b'{"email":'
    )
    if not ok: print(f"FAIL S1.26 body: {body}")

    # S1.27: Account lockout after 5 failed attempts
    lockout_email = f"lockout-{RUN_ID}@souklab.test"
    http_request("POST", "/api/v1/auth/register", {"email": lockout_email, "password": password, "accountType": "CLIENT"})
    l_otp = find_otp(lockout_email, "EMAIL_VERIFICATION")
    http_request("POST", "/api/v1/auth/verify-email", {"email": lockout_email, "code": l_otp})
    
    for i in range(5):
        http_request("POST", "/api/v1/auth/login", {"email": lockout_email, "password": "BadPassword!"})
    
    st, body, ok = assert_scenario(
        "S1.27", "Account lockout on 6th failed attempt", "POST", "/api/v1/auth/login", 403,
        {"email": lockout_email, "password": password},
        check_fn=lambda s, h, b: "locked" in str(b).lower()
    )
    if not ok: print(f"FAIL S1.27 body: {body}")

    # 5. Token Refresh tests
    # S1.28: Happy path refresh rotation
    st, body, ok = assert_scenario(
        "S1.28", "Refresh token rotation happy path", "POST", "/api/v1/auth/refresh", 200,
        {"refreshToken": client_refresh_token},
        check_fn=lambda s, h, b: "accessToken" in b.get("data", {}) and "refreshToken" in b.get("data", {})
    )
    if not ok: print(f"FAIL S1.28 body: {body}")
    new_client_access = body.get("data", {}).get("accessToken") if ok else client_access_token
    new_client_refresh = body.get("data", {}).get("refreshToken") if ok else client_refresh_token

    # S1.29: Replay attack on rotated refresh token
    st, body, ok = assert_scenario(
        "S1.29", "Replay rotated refresh token rejected", "POST", "/api/v1/auth/refresh", {401, 403},
        {"refreshToken": client_refresh_token}
    )
    if not ok: print(f"FAIL S1.29 body: {body}")

    # S1.30: Refresh with forged / invalid token
    st, body, ok = assert_scenario(
        "S1.30", "Refresh with forged token rejected", "POST", "/api/v1/auth/refresh", {401, 403},
        {"refreshToken": "invalid-token-uuid-12345"}
    )
    if not ok: print(f"FAIL S1.30 body: {body}")

    # S1.31: Refresh missing refreshToken
    st, body, ok = assert_scenario(
        "S1.31", "Refresh missing refreshToken validation", "POST", "/api/v1/auth/refresh", 422,
        {}
    )
    if not ok: print(f"FAIL S1.31 body: {body}")

    # S1.32: Refresh malformed JSON
    st, body, ok = assert_scenario(
        "S1.32", "Refresh malformed JSON syntax", "POST", "/api/v1/auth/refresh", 400,
        raw_body=b'{"refreshToken": '
    )
    if not ok: print(f"FAIL S1.32 body: {body}")

    # 6. Logout tests
    # S1.33: Logout happy path
    st, body, ok = assert_scenario(
        "S1.33", "Logout with active refresh token", "POST", "/api/v1/auth/logout", 200,
        {"refreshToken": new_client_refresh},
        token=new_client_access
    )
    if not ok: print(f"FAIL S1.33 body: {body}")

    # S1.34: Post-logout refresh rejected
    st, body, ok = assert_scenario(
        "S1.34", "Post-logout refresh rejected", "POST", "/api/v1/auth/refresh", {401, 403},
        {"refreshToken": new_client_refresh}
    )
    if not ok: print(f"FAIL S1.34 body: {body}")

    # S1.35: Logout without body
    st, body, ok = assert_scenario(
        "S1.35", "Logout without body handled gracefully", "POST", "/api/v1/auth/logout", 200,
        token=new_client_access
    )
    if not ok: print(f"FAIL S1.35 body: {body}")

    # Log client back in to get fresh tokens for remaining tests
    st, headers, body = http_request("POST", "/api/v1/auth/login", {"email": client_email, "password": password})
    client_access_token = body.get("data", {}).get("accessToken")
    client_refresh_token = body.get("data", {}).get("refreshToken")

    # 7. Forgot Password tests
    # S1.36: Forgot password happy path
    st, body, ok = assert_scenario(
        "S1.36", "Forgot password happy path", "POST", "/api/v1/auth/forgot-password", 200,
        {"email": client_email}
    )
    if not ok: print(f"FAIL S1.36 body: {body}")

    # S1.37: Forgot password non-existent email
    st, body, ok = assert_scenario(
        "S1.37", "Forgot password anti-enumeration", "POST", "/api/v1/auth/forgot-password", 200,
        {"email": f"notfound-{RUN_ID}@souklab.test"}
    )
    if not ok: print(f"FAIL S1.37 body: {body}")

    # S1.38: Forgot password invalid email format
    st, body, ok = assert_scenario(
        "S1.38", "Forgot password invalid email validation", "POST", "/api/v1/auth/forgot-password", 422,
        {"email": "bad-email"}
    )
    if not ok: print(f"FAIL S1.38 body: {body}")

    # S1.39: Forgot password malformed JSON
    st, body, ok = assert_scenario(
        "S1.39", "Forgot password malformed JSON syntax", "POST", "/api/v1/auth/forgot-password", 400,
        raw_body=b'{"email": '
    )
    if not ok: print(f"FAIL S1.39 body: {body}")

    # 8. Reset Password tests
    reset_otp = find_otp(client_email, "PASSWORD_RESET")
    new_password = "BrandNewPassword123!"

    # S1.40: Reset password wrong OTP
    st, body, ok = assert_scenario(
        "S1.40", "Reset password invalid OTP code", "POST", "/api/v1/auth/reset-password", 400,
        {"email": client_email, "code": "999999", "newPassword": new_password}
    )
    if not ok: print(f"FAIL S1.40 body: {body}")

    # S1.41: Reset password short password validation
    st, body, ok = assert_scenario(
        "S1.41", "Reset password short password validation", "POST", "/api/v1/auth/reset-password", 422,
        {"email": client_email, "code": reset_otp, "newPassword": "short"}
    )
    if not ok: print(f"FAIL S1.41 body: {body}")

    # S1.42: Reset password invalid code format
    st, body, ok = assert_scenario(
        "S1.42", "Reset password invalid code format validation", "POST", "/api/v1/auth/reset-password", 422,
        {"email": client_email, "code": "12", "newPassword": new_password}
    )
    if not ok: print(f"FAIL S1.42 body: {body}")

    # S1.43: Reset password happy path
    st, body, ok = assert_scenario(
        "S1.43", "Reset password valid OTP happy path", "POST", "/api/v1/auth/reset-password", 200,
        {"email": client_email, "code": reset_otp, "newPassword": new_password}
    )
    if not ok: print(f"FAIL S1.43 body: {body}")

    # S1.44: Login with old password rejected
    st, body, ok = assert_scenario(
        "S1.44", "Login with old password rejected after reset", "POST", "/api/v1/auth/login", 401,
        {"email": client_email, "password": password}
    )
    if not ok: print(f"FAIL S1.44 body: {body}")

    # S1.45: Login with new reset password succeeds
    st, body, ok = assert_scenario(
        "S1.45", "Login with new password succeeds after reset", "POST", "/api/v1/auth/login", 200,
        {"email": client_email, "password": new_password}
    )
    if not ok: print(f"FAIL S1.45 body: {body}")
    client_access_token = body.get("data", {}).get("accessToken")
    password = new_password

    # S1.46: Reset password replay attack
    st, body, ok = assert_scenario(
        "S1.46", "Reset password replay attack rejected", "POST", "/api/v1/auth/reset-password", 400,
        {"email": client_email, "code": reset_otp, "newPassword": "AnotherPassword123!"}
    )
    if not ok: print(f"FAIL S1.46 body: {body}")

    # 9. Change Password tests
    # S1.47: 401 Unauthorized - Missing Authorization header
    st, body, ok = assert_scenario(
        "S1.47", "Change password unauthorized without token", "POST", "/api/v1/auth/change-password", 401,
        {"oldPassword": password, "newPassword": "AnotherPassword123!"}
    )
    if not ok: print(f"FAIL S1.47 body: {body}")

    # S1.48: 401 Unauthorized - Malformed token
    st, body, ok = assert_scenario(
        "S1.48", "Change password unauthorized malformed token", "POST", "/api/v1/auth/change-password", 401,
        {"oldPassword": password, "newPassword": "AnotherPassword123!"},
        token="not.a.valid.jwt"
    )
    if not ok: print(f"FAIL S1.48 body: {body}")

    # S1.49: 422 Validation - Same old and new password (@DifferentPasswords)
    st, body, ok = assert_scenario(
        "S1.49", "Change password same passwords validation", "POST", "/api/v1/auth/change-password", 422,
        {"oldPassword": password, "newPassword": password},
        token=client_access_token
    )
    if not ok: print(f"FAIL S1.49 body: {body}")

    # S1.50: 422 Validation - New password < 8 chars
    st, body, ok = assert_scenario(
        "S1.50", "Change password short new password validation", "POST", "/api/v1/auth/change-password", 422,
        {"oldPassword": password, "newPassword": "short"},
        token=client_access_token
    )
    if not ok: print(f"FAIL S1.50 body: {body}")

    # S1.51: Wrong old password
    st, body, ok = assert_scenario(
        "S1.51", "Change password wrong old password", "POST", "/api/v1/auth/change-password", {400, 401},
        {"oldPassword": "IncorrectPassword!", "newPassword": "UpdatedPassword123!"},
        token=client_access_token
    )
    if not ok: print(f"FAIL S1.51 body: {body}")

    # S1.52: Happy path change password
    final_password = "FinalSecurePassword123!"
    st, body, ok = assert_scenario(
        "S1.52", "Change password happy path", "POST", "/api/v1/auth/change-password", 200,
        {"oldPassword": password, "newPassword": final_password},
        token=client_access_token
    )
    if not ok: print(f"FAIL S1.52 body: {body}")
    password = final_password

    # S1.53: Login with updated password succeeds
    st, body, ok = assert_scenario(
        "S1.53", "Login succeeds with changed password", "POST", "/api/v1/auth/login", 200,
        {"email": client_email, "password": password}
    )
    if not ok: print(f"FAIL S1.53 body: {body}")
    client_access_token = body.get("data", {}).get("accessToken")

    # 10. GET /api/v1/auth/me tests
    # S1.54: 401 Missing token
    st, body, ok = assert_scenario(
        "S1.54", "Profile read unauthorized missing token", "GET", "/api/v1/auth/me", 401
    )
    if not ok: print(f"FAIL S1.54 body: {body}")

    # S1.55: 401 Malformed token
    st, body, ok = assert_scenario(
        "S1.55", "Profile read unauthorized malformed token", "GET", "/api/v1/auth/me", 401,
        token="invalid.token.here"
    )
    if not ok: print(f"FAIL S1.55 body: {body}")

    # S1.56: Happy path client reads profile
    st, body, ok = assert_scenario(
        "S1.56", "Client reads own profile", "GET", "/api/v1/auth/me", 200,
        token=client_access_token,
        check_fn=lambda s, h, b: b.get("success") is True and b.get("data", {}).get("email") == client_email
    )
    if not ok: print(f"FAIL S1.56 body: {body}")

    # For artisan testing: Approve artisan via DB so they are ACTIVE and verified
    db_exec(f"UPDATE users SET status='ACTIVE', email_verified=b'1', email_verified_at=NOW() WHERE email='{artisan_email}'")
    st, headers, body = http_request("POST", "/api/v1/auth/login", {"email": artisan_email, "password": "SecurePassword123!"})
    artisan_access_token = body.get("data", {}).get("accessToken")

    # S1.57: Happy path artisan reads profile
    st, body, ok = assert_scenario(
        "S1.57", "Artisan reads own profile", "GET", "/api/v1/auth/me", 200,
        token=artisan_access_token,
        check_fn=lambda s, h, b: b.get("success") is True and b.get("data", {}).get("email") == artisan_email
    )
    if not ok: print(f"FAIL S1.57 body: {body}")

    # 11. Complete Profile tests
    # S1.58: Complete profile unauthorized
    st, body, ok = assert_scenario(
        "S1.58", "Complete profile unauthorized without token", "POST", "/api/v1/auth/complete-profile", 401,
        {"clientType": "INDIVIDUAL", "city": "Algiers"}
    )
    if not ok: print(f"FAIL S1.58 body: {body}")

    # S1.59: Client completes profile happy path
    st, body, ok = assert_scenario(
        "S1.59", "Client completes profile happy path", "POST", "/api/v1/auth/complete-profile", 200,
        {"clientType": "INDIVIDUAL", "city": "Algiers", "bio": "Art collector and enthusiast"},
        token=client_access_token,
        check_fn=lambda s, h, b: b.get("success") is True and b.get("data", {}).get("city") == "Algiers"
    )
    if not ok: print(f"FAIL S1.59 body: {body}")

    # Fetch reference IDs from DB for artisan completion
    subcats = db_query("SELECT id FROM job_sub_categories LIMIT 1")
    regions = db_query("SELECT id FROM regions LIMIT 1")
    subcat_id = subcats[0][0] if subcats else None
    region_id = regions[0][0] if regions else None

    # S1.60: Artisan completes profile happy path
    st, body, ok = assert_scenario(
        "S1.60", "Artisan completes profile happy path", "POST", "/api/v1/auth/complete-profile", 200,
        {"city": "Tizi Ouzou", "bio": "Traditional Kabyle silversmith", "subCategoryId": subcat_id, "regionId": region_id},
        token=artisan_access_token,
        check_fn=lambda s, h, b: b.get("success") is True and b.get("data", {}).get("city") == "Tizi Ouzou"
    )
    if not ok: print(f"FAIL S1.60 body: {body}")

    # S1.61: Artisan completes profile with invalid subCategoryId -> 404
    st, body, ok = assert_scenario(
        "S1.61", "Artisan complete profile non-existent subCategory", "POST", "/api/v1/auth/complete-profile", 404,
        {"subCategoryId": "00000000-0000-0000-0000-000000000099"},
        token=artisan_access_token
    )
    if not ok: print(f"FAIL S1.61 body: {body}")

    # 12. PATCH /api/v1/auth/me tests
    # S1.62: Patch unauthorized
    st, body, ok = assert_scenario(
        "S1.62", "Patch profile unauthorized without token", "PATCH", "/api/v1/auth/me", 401,
        {"city": "Oran"}
    )
    if not ok: print(f"FAIL S1.62 body: {body}")

    # S1.63: Client patches profile happy path
    st, body, ok = assert_scenario(
        "S1.63", "Client patches city and bio", "PATCH", "/api/v1/auth/me", 200,
        {"city": "Constantine", "bio": "Patched bio for client"},
        token=client_access_token,
        check_fn=lambda s, h, b: b.get("data", {}).get("city") == "Constantine"
    )
    if not ok: print(f"FAIL S1.63 body: {body}")

    # S1.64: Artisan patches profile happy path
    st, body, ok = assert_scenario(
        "S1.64", "Artisan patches website and address", "PATCH", "/api/v1/auth/me", 200,
        {"website": "https://artisan-kabyle.dz", "address": "Village Beni Yenni"},
        token=artisan_access_token,
        check_fn=lambda s, h, b: b.get("data", {}).get("website") == "https://artisan-kabyle.dz"
    )
    if not ok: print(f"FAIL S1.64 body: {body}")

    # Print summary
    passed_count = sum(1 for r in results if r["passed"])
    failed_count = len(results) - passed_count
    print(f"\n=======================================================")
    print(f"Domain 1 Execution Complete: {passed_count} PASSED, {failed_count} FAILED (Total: {len(results)})")
    print(f"=======================================================\n")
    
    # Write report JSON
    os.makedirs(".agent-output", exist_ok=True)
    with open(".agent-output/domain1-auth-report.json", "w") as f:
        json.dump(results, f, indent=2)

    return 0 if failed_count == 0 else 1

if __name__ == "__main__":
    sys.exit(run())
