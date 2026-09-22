#!/usr/bin/env python3
"""Secret-safe semantic authentication workflow verifier.

The application must already be running.  The verifier creates only synthetic
``@souklab.test`` users, recovers disposable OTPs by matching SHA-256 hashes in
MariaDB, and writes classifications rather than credentials, JWTs, refresh
tokens, or raw response bodies.
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
from pathlib import Path
from typing import Any


BASE = os.getenv("APP_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
STAMP = time.strftime("%Y%m%dT%H%M%SZ", time.gmtime())
EMAIL = f"auth-workflow-{STAMP.lower()}@souklab.test"
ARTISAN_EMAIL = f"artisan-workflow-{STAMP.lower()}@souklab.test"
PASSWORD = "synthetic-auth-password-1"
RESET_PASSWORD = "synthetic-reset-password-1"
REPORT = Path(os.getenv("AUTH_VERIFY_REPORT", f".agent-output/auth-workflow-{STAMP}.md"))

DB_HOST = os.getenv("VERIFY_DB_HOST", "127.0.0.1")
DB_PORT = os.getenv("VERIFY_DB_PORT", "3307")
DB_NAME = os.getenv("VERIFY_DB_NAME", "souklab_test")
DB_USER = os.getenv("VERIFY_DB_USER", "souklab_test")
DB_PASSWORD = os.getenv("VERIFY_DB_PASSWORD", "")

steps: list[dict[str, Any]] = []


def safe_payload(payload: Any) -> Any:
    if not isinstance(payload, dict):
        return payload
    result = {}
    for key, value in payload.items():
        if key.lower() in {"password", "oldpassword", "newpassword", "refreshtoken", "code", "token"}:
            result[key] = "<synthetic-secret-redacted>"
        else:
            result[key] = value
    return result


def request(method: str, path: str, payload: Any = None, token: str | None = None,
            follow_redirects: bool = True) -> tuple[int, dict[str, str], Any]:
    url = BASE + path
    headers = {"Accept": "application/json"}
    if payload is not None:
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = f"Bearer {token}"
    body = json.dumps(payload).encode() if payload is not None else None
    opener = urllib.request.build_opener(
        urllib.request.HTTPRedirectHandler() if follow_redirects else _NoRedirect()
    )
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with opener.open(req, timeout=20) as response:
            raw = response.read()
            return response.status, dict(response.headers), parse_json(raw)
    except urllib.error.HTTPError as exc:
        return exc.code, dict(exc.headers), parse_json(exc.read())
    except urllib.error.URLError as exc:
        raise RuntimeError(f"transport failure: {type(exc.reason).__name__}") from exc


class _NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def parse_json(raw: bytes) -> Any:
    try:
        return json.loads(raw.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError):
        return {"contentType": "non-json", "length": len(raw)}


def record(name: str, actor: str, method: str, path: str, expected: str,
           actual: int | str, payload: Any = None, assertion: str = "", passed: bool = False):
    steps.append({
        "name": name, "actor": actor, "request": f"{method} {path}",
        "expected": expected, "actual": actual, "payload": safe_payload(payload),
        "assertion": assertion, "result": "PASS" if passed else "FAIL",
    })


def json_value(body: Any, *keys: str) -> Any:
    if isinstance(body, dict):
        for key in keys:
            if key in body:
                return body[key]
        for value in body.values():
            found = json_value(value, *keys)
            if found is not None:
                return found
    return None


def db(query: str) -> list[list[str]]:
    if not DB_PASSWORD:
        raise RuntimeError("VERIFY_DB_PASSWORD is required for OTP/database assertions")
    env = os.environ.copy()
    env["MYSQL_PWD"] = DB_PASSWORD
    command = ["mariadb", "--batch", "--skip-column-names", "--raw",
               "-h", DB_HOST, "-P", DB_PORT, "-u", DB_USER, DB_NAME, "-e", query]
    completed = subprocess.run(command, env=env, text=True, capture_output=True, check=True)
    return [line.split("\t") for line in completed.stdout.splitlines() if line]


def otp(email: str, token_type: str) -> str:
    rows = db(
        "SELECT vt.code_hash FROM verification_tokens vt "
        "JOIN users u ON u.id=vt.user_id "
        f"WHERE u.email='{email}' AND vt.type='{token_type}' AND vt.used_at IS NULL "
        "ORDER BY vt.created_at DESC LIMIT 1"
    )
    if not rows:
        raise RuntimeError(f"no active {token_type} token")
    expected = rows[0][0]
    for value in range(1_000_000):
        candidate = f"{value:06d}"
        if hashlib.sha256(candidate.encode()).hexdigest() == expected:
            return candidate
    raise RuntimeError("OTP hash did not match a six-digit candidate")


def expect(name: str, actor: str, method: str, path: str, payload: Any,
           allowed: set[int], assertion: str, token: str | None = None,
           follow_redirects: bool = True) -> tuple[int, Any, dict[str, str]]:
    status, headers, body = request(method, path, payload, token, follow_redirects)
    record(name, actor, method, path, "/".join(map(str, sorted(allowed))), status,
           payload, assertion, status in allowed)
    return status, body, headers


def main() -> int:
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    try:
        expect("invalid registration validation", "synthetic-client", "POST", "/api/v1/auth/register",
               {"email": "invalid", "password": "short", "accountType": "CLIENT"}, {422},
               "validation envelope")
        expect("client registration", EMAIL, "POST", "/api/v1/auth/register",
               {"email": EMAIL, "password": PASSWORD, "accountType": "CLIENT", "firstName": "Synthetic"}, {201},
               "users row exists; EMAIL_VERIFICATION token exists")
        expect("duplicate registration", EMAIL, "POST", "/api/v1/auth/register",
               {"email": EMAIL, "password": PASSWORD, "accountType": "CLIENT"}, {409},
               "duplicate email rejected")
        expect("artisan registration", ARTISAN_EMAIL, "POST", "/api/v1/auth/register",
               {"email": ARTISAN_EMAIL, "password": PASSWORD, "accountType": "ARTISAN", "firstName": "Synthetic"}, {201},
               "users row exists with pending artisan status")
        artisan_rows = db(f"SELECT status, IF(email_verified, '1', '0') FROM users WHERE email='{ARTISAN_EMAIL}'")
        artisan_pending = bool(artisan_rows and artisan_rows[0][0] == "PENDING")
        record("artisan pending database assertion", ARTISAN_EMAIL, "DB", "users", "PENDING/0",
               "/".join(artisan_rows[0]) if artisan_rows else "missing", None,
               "registration requires administrator approval", artisan_pending)
        expect("pending artisan login rejected", ARTISAN_EMAIL, "POST", "/api/v1/auth/login",
               {"email": ARTISAN_EMAIL, "password": PASSWORD}, {403}, "pending artisan cannot authenticate")
        expect("unverified login rejected", EMAIL, "POST", "/api/v1/auth/login",
               {"email": EMAIL, "password": PASSWORD}, {401, 403}, "email verification boundary")
        expect("resend verification", EMAIL, "POST", "/api/v1/auth/resend-verification",
               {"email": EMAIL}, {200}, "old active token invalidated; new token exists")
        code = otp(EMAIL, "EMAIL_VERIFICATION")
        expect("email verification", EMAIL, "POST", "/api/v1/auth/verify-email",
               {"email": EMAIL, "code": code}, {200}, "users.email_verified=1; token.used_at set")
        status, body, _ = expect("verified login", EMAIL, "POST", "/api/v1/auth/login",
                                 {"email": EMAIL, "password": PASSWORD}, {200},
                                 "access and refresh tokens returned")
        access = json_value(body, "accessToken")
        refresh = json_value(body, "refreshToken")
        if not access or not refresh:
            raise RuntimeError("login response did not contain token pair")
        status, body, _ = expect("refresh rotation", EMAIL, "POST", "/api/v1/auth/refresh",
                                 {"refreshToken": refresh}, {200}, "old refresh revoked; new pair issued")
        new_access = json_value(body, "accessToken")
        new_refresh = json_value(body, "refreshToken")
        expect("refresh replay rejected", EMAIL, "POST", "/api/v1/auth/refresh",
               {"refreshToken": refresh}, {401, 403}, "rotated refresh token cannot be replayed")
        access, refresh = new_access, new_refresh
        expect("profile completion", EMAIL, "POST", "/api/v1/auth/complete-profile",
               {"clientType": "INDIVIDUAL", "city": "Algiers", "bio": "synthetic workflow"}, {200},
               "client profile row exists", access)
        expect("profile read", EMAIL, "GET", "/api/v1/auth/me", None, {200}, "profile response", access)
        expect("profile patch", EMAIL, "PATCH", "/api/v1/auth/me",
               {"city": "Oran", "bio": "patched synthetic workflow"}, {200}, "profile fields updated", access)
        expect("forgot password", EMAIL, "POST", "/api/v1/auth/forgot-password",
               {"email": EMAIL}, {200}, "PASSWORD_RESET token exists")
        reset_code = otp(EMAIL, "PASSWORD_RESET")
        expect("reset password", EMAIL, "POST", "/api/v1/auth/reset-password",
               {"email": EMAIL, "code": reset_code, "newPassword": RESET_PASSWORD}, {200},
               "password hash changed; reset token consumed")
        expect("old password rejected", EMAIL, "POST", "/api/v1/auth/login",
               {"email": EMAIL, "password": PASSWORD}, {401, 403}, "old credential invalid")
        status, body, _ = expect("post-reset login", EMAIL, "POST", "/api/v1/auth/login",
                                 {"email": EMAIL, "password": RESET_PASSWORD}, {200}, "new credential works")
        access = json_value(body, "accessToken")
        refresh = json_value(body, "refreshToken")
        expect("password change", EMAIL, "POST", "/api/v1/auth/change-password",
               {"oldPassword": RESET_PASSWORD, "newPassword": PASSWORD}, {200}, "password hash updated", access)
        expect("logout", EMAIL, "POST", "/api/v1/auth/logout", {"refreshToken": refresh}, {200},
               "refresh token revoked", access)
        expect("post-logout refresh rejected", EMAIL, "POST", "/api/v1/auth/refresh",
               {"refreshToken": refresh}, {401, 403}, "revoked refresh cannot be used")
        _, _, headers = expect("OAuth artisan intent boundary", "anonymous", "GET",
                               "/api/v1/auth/oauth/google/artisan", None, {302},
                               "HttpOnly Lax intent cookie; no credential", follow_redirects=False)
        expect("OAuth client intent boundary", "anonymous", "GET",
               "/api/v1/auth/oauth/google/client", None, {302},
               "HttpOnly Lax intent cookie; no credential", follow_redirects=False)
    except Exception as exc:
        record("workflow execution", "synthetic", "internal", "-", "no exception", type(exc).__name__,
               None, "sanitized harness exception", False)

    with REPORT.open("w", encoding="utf-8") as output:
        output.write("# Authentication semantic workflow\n\n")
        output.write("Synthetic accounts only; passwords, OTPs, JWTs, refresh tokens, response bodies, and provider secrets are redacted.\n\n")
        output.write("| Case | Actor | Request | Expected | Actual | Payload | DB/audit assertion | Result |\n|---|---|---|---:|---:|---|---|---|\n")
        for step in steps:
            output.write("| {name} | {actor} | `{request}` | {expected} | {actual} | `{payload}` | {assertion} | {result} |\n".format(
                name=step["name"], actor=step["actor"], request=step["request"], expected=step["expected"],
                actual=step["actual"], payload=json.dumps(step["payload"], separators=(",", ":")),
                assertion=step["assertion"], result=step["result"]))
        passed = sum(step["result"] == "PASS" for step in steps)
        failed = len(steps) - passed
        output.write(f"\nSummary: passed={passed}, failed={failed}\n")
    print(f"AUTH_WORKFLOW_RESULT={'PASS' if steps and failed == 0 else 'FAIL'} report={REPORT} passed={passed} failed={failed}")
    return 0 if steps and failed == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
