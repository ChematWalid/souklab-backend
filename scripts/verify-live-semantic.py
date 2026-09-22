#!/usr/bin/env python3
"""Create disposable role fixtures and run the complete live route matrix.

Only synthetic identities and status classifications are used.  Access tokens,
passwords, OTPs, request bodies containing credentials, and response bodies
never leave this process.
"""

from __future__ import annotations

import hashlib
import json
import os
import runpy
import subprocess
import urllib.error
import urllib.request
from uuid import uuid4
from pathlib import Path


BASE = os.getenv("APP_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
DB_PASSWORD = os.getenv("VERIFY_DB_PASSWORD", "")
ADMIN_EMAIL = os.getenv("APP_ADMIN_DEFAULT_EMAIL", "admin@souklab.test")
ADMIN_PASSWORD = os.getenv("APP_ADMIN_DEFAULT_PASSWORD", "local-test-password")
FIXTURE_REPORT = Path(os.getenv("STATUS_FIXTURE_REPORT", ".agent-output/live-status-fixtures.tsv"))
fixture_rows: list[tuple[str, str, str, str, str]] = []


def call(method: str, path: str, payload: object | None = None,
         access_token: str | None = None) -> tuple[int, object]:
    body = None if payload is None else json.dumps(payload).encode()
    headers = {"Accept": "application/json"}
    if body is not None:
        headers["Content-Type"] = "application/json"
    if access_token:
        headers["Authorization"] = "Bearer " + access_token
    request = urllib.request.Request(BASE + path, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            raw = response.read()
            return response.status, json.loads(raw) if raw else {}
    except urllib.error.HTTPError as exc:
        try:
            return exc.code, json.loads(exc.read())
        except (UnicodeDecodeError, json.JSONDecodeError):
            return exc.code, {}


def nested_value(value: object, *keys: str) -> object | None:
    if isinstance(value, dict):
        for key in keys:
            if key in value:
                return value[key]
        for child in value.values():
            found = nested_value(child, *keys)
            if found is not None:
                return found
    return None


def db(query: str) -> list[str]:
    if not DB_PASSWORD:
        raise RuntimeError("VERIFY_DB_PASSWORD is required")
    environment = os.environ.copy()
    environment["MYSQL_PWD"] = DB_PASSWORD
    command = ["mariadb", "--batch", "--skip-column-names", "--raw",
               "-h", os.getenv("VERIFY_DB_HOST", "127.0.0.1"),
               "-P", os.getenv("VERIFY_DB_PORT", "3307"),
               "-u", os.getenv("VERIFY_DB_USER", "souklab_test"),
               os.getenv("VERIFY_DB_NAME", "souklab_test"), "-e", query]
    output = subprocess.check_output(command, env=environment, text=True,
                                     stderr=subprocess.DEVNULL)
    return [line for line in output.splitlines() if line]


def verification_code(email: str) -> str:
    rows = db("SELECT vt.code_hash FROM verification_tokens vt JOIN users u ON u.id=vt.user_id "
               f"WHERE u.email='{email}' AND vt.type='EMAIL_VERIFICATION' "
               "AND vt.used_at IS NULL ORDER BY vt.created_at DESC LIMIT 1")
    if not rows:
        raise RuntimeError("verification token was not created")
    expected = rows[0]
    for number in range(1_000_000):
        candidate = f"{number:06d}"
        if hashlib.sha256(candidate.encode()).hexdigest() == expected:
            return candidate
    raise RuntimeError("verification hash did not match a six-digit code")


def create_account(account_type: str) -> tuple[str, str, str]:
    email = f"semantic-{account_type.lower()}-{uuid4().hex[:12]}@souklab.test"
    password = "synthetic-semantic-password-1"
    status, _ = call("POST", "/api/v1/auth/register", {
        "email": email, "password": password,
        "accountType": account_type, "firstName": "Semantic", "lastName": "Synthetic"
    })
    if status != 201:
        raise RuntimeError(f"registration failed for synthetic {account_type}: {status}")
    status, _ = call("POST", "/api/v1/auth/verify-email", {
        "email": email, "code": verification_code(email)
    })
    if status != 200:
        raise RuntimeError(f"verification failed for synthetic {account_type}: {status}")
    user_id = db(f"SELECT id FROM users WHERE email='{email}'")[0]
    return email, password, user_id


def login(email: str, password: str) -> str:
    status, response = call("POST", "/api/v1/auth/login",
                            {"email": email, "password": password})
    if status != 200:
        raise RuntimeError(f"synthetic login failed: {status}")
    access_token = nested_value(response, "accessToken")
    if not isinstance(access_token, str) or not access_token:
        raise RuntimeError("login response did not contain an access token")
    return access_token


def fixture(name: str, before: str, action: str, expected: str, actual: str) -> None:
    fixture_rows.append((name, before, action, expected, actual))


def expect_status(name: str, email: str, password: str, expected: int, before: str,
                  action: str) -> None:
    actual, _ = call("POST", "/api/v1/auth/login", {"email": email, "password": password})
    fixture(name, before, action, str(expected), str(actual))
    if actual != expected:
        raise RuntimeError(f"status fixture {name} expected {expected}, got {actual}")


def main() -> int:
    admin_token = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    client_email, client_password, _ = create_account("CLIENT")
    artisan_email, artisan_password, artisan_id = create_account("ARTISAN")
    second_client_email, second_client_password, _ = create_account("CLIENT")
    second_artisan_email, second_artisan_password, second_artisan_id = create_account("ARTISAN")
    pending_email, pending_password, _ = create_account("ARTISAN")
    rejected_email, rejected_password, rejected_id = create_account("CLIENT")
    timed_out_email, timed_out_password, timed_out_id = create_account("CLIENT")
    suspended_email, suspended_password, suspended_id = create_account("CLIENT")
    expect_status("pending-artisan-login", pending_email, pending_password, 403,
                  "PENDING", "login rejected before approval")
    db(f"UPDATE users SET status='REJECTED', ban_reason='synthetic rejection fixture' WHERE id='{rejected_id}'")
    expect_status("rejected-client-login", rejected_email, rejected_password, 403,
                  "REJECTED", "login rejected after database fixture assertion")
    status, _ = call("POST", f"/api/v1/admin/users/{timed_out_id}/timeout",
                      {"minutes": 5, "reason": "synthetic timeout fixture"}, admin_token)
    fixture("timed-out-client", "ACTIVE", "admin timeout", "200", str(status))
    if status != 200:
        raise RuntimeError(f"timeout fixture failed: {status}")
    expect_status("timed-out-client-login", timed_out_email, timed_out_password, 403,
                  "SUSPENDED", "login rejected during timeout")
    status, _ = call("POST", f"/api/v1/admin/users/{timed_out_id}/unban", access_token=admin_token)
    fixture("timed-out-client", "SUSPENDED", "admin unban", "200", str(status))
    if status != 200:
        raise RuntimeError(f"unban fixture failed: {status}")
    status, _ = call("POST", f"/api/v1/admin/users/{suspended_id}/ban",
                      {"reason": "synthetic suspension fixture"}, admin_token)
    fixture("suspended-client", "ACTIVE", "admin permanent suspension", "200", str(status))
    if status != 200:
        raise RuntimeError(f"suspension fixture failed: {status}")
    expect_status("suspended-client-login", suspended_email, suspended_password, 403,
                  "SUSPENDED", "login rejected while suspended")
    status, _ = call("POST", f"/api/v1/admin/users/{suspended_id}/unban", access_token=admin_token)
    fixture("suspended-client", "SUSPENDED", "admin unban", "200", str(status))
    if status != 200:
        raise RuntimeError(f"suspended unban fixture failed: {status}")
    status, _ = call("POST", f"/api/v1/admin/users/{artisan_id}/approve", access_token=admin_token)
    if status != 200:
        raise RuntimeError(f"artisan approval failed: {status}")
    status, _ = call("POST", f"/api/v1/admin/users/{second_artisan_id}/approve", access_token=admin_token)
    if status != 200:
        raise RuntimeError(f"second artisan approval failed: {status}")
    os.environ.update({
        "SOUKLAB_ACCESS_TOKEN": login(client_email, client_password),
        "SOUKLAB_ADMIN_TOKEN": admin_token,
        "SOUKLAB_CLIENT_TOKEN": login(client_email, client_password),
        "SOUKLAB_ARTISAN_TOKEN": login(artisan_email, artisan_password),
        "SOUKLAB_SECOND_CLIENT_TOKEN": login(second_client_email, second_client_password),
        "SOUKLAB_SECOND_ARTISAN_TOKEN": login(second_artisan_email, second_artisan_password),
        "API_ROUTE_REPORT": os.getenv("API_ROUTE_REPORT", ".agent-output/live-api-semantic-matrix.tsv"),
    })
    route_result = 0
    try:
        runpy.run_path("scripts/verify-api-routes.py", run_name="__main__")
    except SystemExit as exc:
        route_result = int(exc.code or 0)
    FIXTURE_REPORT.parent.mkdir(parents=True, exist_ok=True)
    with FIXTURE_REPORT.open("w", encoding="utf-8") as output:
        output.write("fixture\tinitial/status\taction\texpected\tactual\n")
        for row in fixture_rows:
            output.write("\t".join(row) + "\n")
    print(f"STATUS_FIXTURES={len(fixture_rows)} report={FIXTURE_REPORT}")
    return route_result


if __name__ == "__main__":
    raise SystemExit(main())
