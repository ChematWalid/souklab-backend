#!/usr/bin/env python3
"""
Souklab Backend Exhaustive Verification - Domain 9: Admin & Moderation
Tests real administrative, moderation, user management, permissions, and audit log endpoints.
"""

import sys
import os
import json
import time
import subprocess
import urllib.request
import urllib.error
import urllib.parse
from typing import Any

BASE_URL = os.environ.get("SOUKLAB_BASE_URL", "http://127.0.0.1:8080")
DB_NAME = os.environ.get("VERIFY_DB_NAME", "souklab_test")
DB_USER = os.environ.get("VERIFY_DB_USERNAME", "souklab_test")
DB_PASS = os.environ.get("VERIFY_DB_PASSWORD", "souklab_test_password")
DB_PORT = os.environ.get("VERIFY_DB_PORT", "3307")
OUTPUT_DIR = ".agent-output"
REPORT_FILE = os.path.join(OUTPUT_DIR, "domain9-admin-report.json")

RUN_ID = f"d9-{int(time.time())}"

results = []

def record(scenario_id: str, description: str, passed: bool, expected: str, actual: str, payload: Any = None):
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
        "domain": "Domain 9: Admin & Moderation",
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

def db_execute(sql: str) -> str:
    cmd = [
        "mariadb",
        "-h", "127.0.0.1",
        "-P", str(DB_PORT),
        "-u", DB_USER,
        f"-p{DB_PASS}",
        "-D", DB_NAME,
        "-e", sql,
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, check=True)
    return res.stdout.strip()

def http_json(method: str, path: str, body: Any = None, token: str | None = None, query_params: dict[str, Any] | None = None):
    url = BASE_URL + path
    if query_params:
        url += "?" + urllib.parse.urlencode(query_params)
    data = json.dumps(body).encode("utf-8") if body is not None else None
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            raw = resp.read().decode("utf-8")
            return resp.status, dict(resp.headers), json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        try:
            return e.code, dict(e.headers), json.loads(raw) if raw else None
        except Exception:
            return e.code, dict(e.headers), raw
    except Exception as e:
        return 0, {}, str(e)

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

    db_execute(f"UPDATE users SET email_verified = 1, status = 'ACTIVE' WHERE email = '{email}';")
    out = db_execute(f"SELECT id FROM users WHERE email = '{email}'")
    lines = out.splitlines()
    uid = lines[1].strip() if len(lines) > 1 else lines[0].strip()

    st_l, _, resp_l = http_json("POST", "/api/v1/auth/login", {"email": email, "password": password})
    if st_l != 200 or not resp_l.get("success"):
        raise RuntimeError(f"Login failed ({st_l}): {resp_l}")
    token = resp_l["data"]["accessToken"]
    return token, uid


def main():
    print(f"=== Domain 9 Live Verification: Admin & Moderation (RUN_ID: {RUN_ID}) ===")

    # 1. Accounts Setup
    artisan_email = f"d9_artisan_{RUN_ID}@example.com"
    client_email = f"d9_client_{RUN_ID}@example.com"
    mod_target_email = f"d9_target_{RUN_ID}@example.com"
    pending_email = f"d9_pending_{RUN_ID}@example.com"

    token_art, id_art = register_user(artisan_email, "Password123!", "ARTISAN", "Artisan D9")
    token_cli, id_cli = register_user(client_email, "Password123!", "CLIENT", "Client D9")
    token_target, id_target = register_user(mod_target_email, "Password123!", "CLIENT", "Target User")

    # Register user and leave status as PENDING
    st_p, _, r_p = http_json("POST", "/api/v1/auth/register", {
        "email": pending_email,
        "password": "Password123!",
        "firstName": "Pending",
        "lastName": "User",
        "phone": "+213555999888",
        "accountType": "ARTISAN",
    })
    db_execute(f"UPDATE users SET email_verified = 1, status = 'PENDING' WHERE email = '{pending_email}';")
    out_p = db_execute(f"SELECT id FROM users WHERE email = '{pending_email}'")
    lines_p = out_p.splitlines()
    id_pending = lines_p[1].strip() if len(lines_p) > 1 else lines_p[0].strip()

    # Admin Login
    st_adm, _, resp_adm = http_json("POST", "/api/v1/auth/login", {"email": "4ce013@gmail.com", "password": "admin123"})
    if st_adm != 200:
        raise RuntimeError(f"Admin login failed: {st_adm} {resp_adm}")
    token_admin = resp_adm["data"]["accessToken"]

    print(f"✓ Accounts setup: Admin (OK), Artisan ({id_art}), Client ({id_cli}), Target ({id_target}), Pending ({id_pending})")

    # =========================================================================
    # Part 1: Access Control & Security Guards (d9-adm-01 to 08)
    # =========================================================================

    # d9-adm-01: Anonymous access to /api/v1/admin/users -> 401
    st, _, resp = http_json("GET", "/api/v1/admin/users")
    passed = (st == 401)
    record("d9-adm-01", "Anonymous access to /api/v1/admin/users rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d9-adm-02: Anonymous access to /api/v1/admin/users/audit-logs -> 401
    st, _, resp = http_json("GET", "/api/v1/admin/users/audit-logs")
    passed = (st == 401)
    record("d9-adm-02", "Anonymous access to /api/v1/admin/users/audit-logs rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d9-adm-03: Anonymous access to /api/v1/admin/users/{id}/permissions -> 401
    st, _, resp = http_json("GET", f"/api/v1/admin/users/{id_target}/permissions")
    passed = (st == 401)
    record("d9-adm-03", "Anonymous access to /api/v1/admin/users/{id}/permissions rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d9-adm-04: Client access to /api/v1/admin/users -> 403
    st, _, resp = http_json("GET", "/api/v1/admin/users", token=token_cli)
    passed = (st == 403)
    record("d9-adm-04", "Client access to /api/v1/admin/users rejection", passed, "403 FORBIDDEN", f"{st}", resp)

    # d9-adm-05: Artisan access to /api/v1/admin/users/audit-logs -> 403
    st, _, resp = http_json("GET", "/api/v1/admin/users/audit-logs", token=token_art)
    passed = (st == 403)
    record("d9-adm-05", "Artisan access to /api/v1/admin/users/audit-logs rejection", passed, "403 FORBIDDEN", f"{st}", resp)

    # d9-adm-06: Non-admin access to /api/v1/admin/formations/pending -> 403
    st, _, resp = http_json("GET", "/api/v1/admin/formations/pending", token=token_cli)
    passed = (st == 403)
    record("d9-adm-06", "Non-admin access to /api/v1/admin/formations/pending rejection", passed, "403 FORBIDDEN", f"{st}", resp)

    # d9-adm-07: Non-admin access to /api/v1/admin/feed/pending -> 403
    st, _, resp = http_json("GET", "/api/v1/admin/feed/pending", token=token_art)
    passed = (st == 403)
    record("d9-adm-07", "Non-admin access to /api/v1/admin/feed/pending rejection", passed, "403 FORBIDDEN", f"{st}", resp)

    # d9-adm-08: Non-admin access to /api/v1/admin/formateur-requests -> 403
    st, _, resp = http_json("GET", "/api/v1/admin/formateur-requests", token=token_cli)
    passed = (st == 403)
    record("d9-adm-08", "Non-admin access to /api/v1/admin/formateur-requests rejection", passed, "403 FORBIDDEN", f"{st}", resp)

    # =========================================================================
    # Part 2: User Listing, Search & Pagination (d9-adm-09 to 13)
    # =========================================================================

    # d9-adm-09: Admin lists users GET /api/v1/admin/users
    st, _, resp = http_json("GET", "/api/v1/admin/users", token=token_admin)
    p_users = resp.get("data", {}) if resp else {}
    passed = (st == 200 and resp.get("success") is True and p_users.get("totalElements", 0) > 0)
    record("d9-adm-09", "Admin lists users (paginated)", passed, "200 OK totalElements > 0", f"{st} count={p_users.get('totalElements')}", p_users)

    # d9-adm-10: Admin searches users GET /api/v1/admin/users?search=...
    st, _, resp = http_json("GET", "/api/v1/admin/users", query_params={"search": mod_target_email}, token=token_admin)
    p_search = resp.get("data", {}) if resp else {}
    s_items = p_search.get("content", [])
    found_target = next((u for u in s_items if u.get("id") == id_target), None)
    passed = (st == 200 and len(s_items) >= 1 and found_target is not None)
    record("d9-adm-10", "Admin searches users by email keyword", passed, "200 OK target found", f"{st} found={found_target is not None}", s_items)

    # d9-adm-11: Admin user pagination clamp / parameters
    st, _, resp = http_json("GET", "/api/v1/admin/users", query_params={"page": 0, "size": 2}, token=token_admin)
    p_page = resp.get("data", {}) if resp else {}
    passed = (st == 200 and len(p_page.get("content", [])) <= 2 and p_page.get("pageNumber") == 0)
    record("d9-adm-11", "Admin user list pagination parameters (size=2)", passed, "200 OK page=0 size=2", f"{st} count={len(p_page.get('content', []))}", p_page)

    # d9-adm-12: Admin lists audit logs GET /api/v1/admin/users/audit-logs
    st, _, resp = http_json("GET", "/api/v1/admin/users/audit-logs", token=token_admin)
    p_audit = resp.get("data", {}) if resp else {}
    passed = (st == 200 and resp.get("success") is True and isinstance(p_audit.get("content"), list))
    record("d9-adm-12", "Admin lists audit logs", passed, "200 OK paginated audit logs", f"{st} count={len(p_audit.get('content', []))}", p_audit)

    # d9-adm-13: Admin lists pending users GET /api/v1/admin/users/pending
    st, _, resp = http_json("GET", "/api/v1/admin/users/pending", token=token_admin)
    p_pending = resp.get("data", {}) if resp else {}
    pending_items = p_pending.get("content", [])
    found_pending = next((u for u in pending_items if u.get("id") == id_pending), None)
    passed = (st == 200 and found_pending is not None and (found_pending.get("status") == "PENDING" or found_pending.get("accountStatus") == "PENDING"))
    record("d9-adm-13", "Admin lists pending approval users", passed, "200 OK pending user present", f"{st} found={found_pending is not None}", pending_items)

    # =========================================================================
    # Part 3: Account Approval Workflow (d9-adm-14 to 17)
    # =========================================================================

    # d9-adm-14: Admin approves user in PENDING status
    st, _, resp = http_json("POST", f"/api/v1/admin/users/{id_pending}/approve", token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-14", "Admin approves pending user", passed, "200 OK", f"{st}", resp)

    # Verify status changed to ACTIVE in DB
    db_status = db_execute(f"SELECT status FROM users WHERE id = '{id_pending}'").splitlines()[-1].strip()
    passed = (db_status == "ACTIVE")
    record("d9-adm-14b", "Approved user status updated to ACTIVE in database", passed, "status=ACTIVE", f"status={db_status}")

    # d9-adm-15: Admin approves user already ACTIVE -> 400 BAD_REQUEST
    st, _, resp = http_json("POST", f"/api/v1/admin/users/{id_pending}/approve", token=token_admin)
    passed = (st == 400 and "not pending approval" in str(resp.get("message", "")))
    record("d9-adm-15", "Approve already ACTIVE user rejection", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d9-adm-16: Admin approves non-existent user -> 404 NOT_FOUND
    st, _, resp = http_json("POST", "/api/v1/admin/users/00000000-0000-0000-0000-000000000000/approve", token=token_admin)
    passed = (st == 404 and "User not found" in str(resp.get("message", "")))
    record("d9-adm-16", "Approve non-existent user rejection", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # d9-adm-17: Admin bulk approves users
    # Register another pending user
    bulk_email = f"d9_bulk_{RUN_ID}@example.com"
    http_json("POST", "/api/v1/auth/register", {
        "email": bulk_email,
        "password": "Password123!",
        "firstName": "Bulk",
        "lastName": "User",
        "phone": "+213555999777",
        "accountType": "ARTISAN",
    })
    db_execute(f"UPDATE users SET email_verified = 1, status = 'PENDING' WHERE email = '{bulk_email}';")
    out_b = db_execute(f"SELECT id FROM users WHERE email = '{bulk_email}'")
    id_bulk = out_b.splitlines()[-1].strip()

    st, _, resp = http_json("POST", "/api/v1/admin/users/approve-bulk", [id_bulk], token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-17", "Admin bulk approves users", passed, "200 OK", f"{st}", resp)

    # =========================================================================
    # Part 4: Moderation Sanctions: Timeout, Ban, Unban (d9-adm-18 to 24)
    # =========================================================================

    # d9-adm-18: Admin timeouts user POST /api/v1/admin/users/{id}/timeout
    timeout_req = {"minutes": 15, "reason": "Violation temporaire des règles de courtoisie"}
    st, _, resp = http_json("POST", f"/api/v1/admin/users/{id_target}/timeout", timeout_req, token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-18", "Admin timeouts user for 15 minutes", passed, "200 OK", f"{st}", resp)

    # d9-adm-19: Timed-out user attempted authenticated action rejection
    st, _, resp = http_json("GET", "/api/v1/auth/me", token=token_target)
    passed = (st in (401, 403))
    record("d9-adm-19", "Timed-out user authenticated action rejection", passed, "401 UNAUTHORIZED or 403 FORBIDDEN", f"{st}", resp)

    # d9-adm-20: Admin unbans/restores timed-out user POST /api/v1/admin/users/{id}/unban
    st, _, resp = http_json("POST", f"/api/v1/admin/users/{id_target}/unban", token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-20", "Admin unbans/restores timed-out user", passed, "200 OK", f"{st}", resp)

    # d9-adm-21: Restored user can now make authenticated requests
    st_l, _, r_l = http_json("POST", "/api/v1/auth/login", {"email": mod_target_email, "password": "Password123!"})
    token_target_fresh = r_l["data"]["accessToken"] if st_l == 200 else None
    st, _, resp = http_json("GET", "/api/v1/auth/me", token=token_target_fresh)
    passed = (st == 200 and (resp.get("data", {}).get("accountStatus") == "ACTIVE" or resp.get("data", {}).get("status") == "ACTIVE"))
    curr_st = resp.get("data", {}).get("accountStatus") or resp.get("data", {}).get("status") if resp else None
    record("d9-adm-21", "Restored user authenticated action success", passed, "200 OK status=ACTIVE", f"{st} status={curr_st}", resp)

    # d9-adm-22: Admin unbans already ACTIVE user -> 409 CONFLICT
    st, _, resp = http_json("POST", f"/api/v1/admin/users/{id_target}/unban", token=token_admin)
    passed = (st == 409 and "User is not suspended" in str(resp.get("message", "")))
    record("d9-adm-22", "Unban already ACTIVE user rejection", passed, "409 CONFLICT", f"{st} - {resp.get('message')}", resp)

    # d9-adm-23: Admin permanently bans user POST /api/v1/admin/users/{id}/ban
    ban_req = {"reason": "Infraction grave et répétée aux conditions d'utilisation"}
    st, _, resp = http_json("POST", f"/api/v1/admin/users/{id_target}/ban", ban_req, token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-23", "Admin permanently bans user", passed, "200 OK", f"{st}", resp)

    # d9-adm-24: Banned user login / access rejection
    st, _, resp = http_json("POST", "/api/v1/auth/login", {"email": mod_target_email, "password": "Password123!"})
    passed = (st == 403 or (st == 400 and "Account is" in str(resp.get("message", ""))))
    record("d9-adm-24", "Banned user login rejection", passed, "400/403 Account suspended", f"{st} - {resp.get('message')}", resp)

    # =========================================================================
    # Part 5: Direct Permission Management (d9-adm-25 to 27)
    # =========================================================================

    # d9-adm-25: Admin lists user permissions GET /api/v1/admin/users/{id}/permissions
    st, _, resp = http_json("GET", f"/api/v1/admin/users/{id_cli}/permissions", token=token_admin)
    perms = resp.get("data", []) if resp else []
    passed = (st == 200 and resp.get("success") is True and isinstance(perms, list))
    record("d9-adm-25", "Admin lists user permissions", passed, "200 OK permissions list", f"{st} count={len(perms)}", perms)

    # d9-adm-26: Admin grants permission to user POST /api/v1/admin/users/{id}/permissions
    perm_grant_req = {"permissionKey": "permission:artisan:formations"}
    st, _, resp = http_json("POST", f"/api/v1/admin/users/{id_cli}/permissions", perm_grant_req, token=token_admin)
    updated_perms = resp.get("data", []) if resp else []
    passed = (st == 200 and "permission:artisan:formations" in updated_perms)
    record("d9-adm-26", "Admin grants permission to user", passed, "200 OK permission included", f"{st} contains={('permission:artisan:formations' in updated_perms)}", updated_perms)

    # d9-adm-27: Admin revokes permission from user DELETE /api/v1/admin/users/{id}/permissions
    st, _, resp = http_json("DELETE", f"/api/v1/admin/users/{id_cli}/permissions", perm_grant_req, token=token_admin)
    revoked_perms = resp.get("data", []) if resp else []
    passed = (st == 200 and "permission:artisan:formations" not in revoked_perms)
    record("d9-adm-27", "Admin revokes permission from user", passed, "200 OK permission removed", f"{st} contains={('permission:artisan:formations' in revoked_perms)}", revoked_perms)

    # =========================================================================
    # Part 6: Administrative Moderation Queues (d9-adm-28 to 33)
    # =========================================================================

    # d9-adm-28: Admin formation moderation queue GET /api/v1/admin/formations/pending
    st, _, resp = http_json("GET", "/api/v1/admin/formations/pending", token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-28", "Admin formation moderation queue access", passed, "200 OK", f"{st}", resp)

    # d9-adm-29: Admin feed moderation queue GET /api/v1/admin/feed/pending
    st, _, resp = http_json("GET", "/api/v1/admin/feed/pending", token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-29", "Admin feed moderation queue access", passed, "200 OK", f"{st}", resp)

    # d9-adm-30: Admin formateur application queue GET /api/v1/admin/formateur-requests
    st, _, resp = http_json("GET", "/api/v1/admin/formateur-requests", token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-30", "Admin formateur requests queue access", passed, "200 OK", f"{st}", resp)

    # d9-adm-31: Admin lists reports GET /api/v1/admin/reports
    st, _, resp = http_json("GET", "/api/v1/admin/reports", token=token_admin)
    p_reports = resp.get("data", {}) if resp else {}
    passed = (st == 200 and resp.get("success") is True)
    record("d9-adm-31", "Admin reports queue access", passed, "200 OK", f"{st} total={p_reports.get('totalElements')}", resp)

    # d9-adm-32: User submits a content report and Admin resolves it
    report_req = {
        "targetType": "USER",
        "targetId": id_art,
        "reason": "Comportement inapproprié dans la bio",
    }
    st_r, _, resp_r = http_json("POST", "/api/v1/reports", report_req, token=token_cli)
    report_data = resp_r.get("data", {}) if resp_r else {}
    rep_id = report_data.get("id")
    passed = (st_r == 201 and bool(rep_id) and report_data.get("status") == "OPEN")
    record("d9-adm-32", "Client submits user report for moderation", passed, "201 CREATED status=OPEN", f"{st_r} id={rep_id}", resp_r)

    # d9-adm-33: Admin resolves report POST /api/v1/admin/reports/{id}/resolve
    resolve_req = {
        "action": "DISMISS",
        "note": "Aucune infraction constatée après vérification",
    }
    st, _, resp = http_json("POST", f"/api/v1/admin/reports/{rep_id}/resolve", resolve_req, token=token_admin)
    res_data = resp.get("data", {}) if resp else {}
    passed = (st == 200 and res_data.get("status") == "DISMISSED" and res_data.get("resolutionAction") == "DISMISS")
    record("d9-adm-33", "Admin resolves report with DISMISS", passed, "200 OK status=DISMISSED resolutionAction=DISMISS", f"{st} status={res_data.get('status')}", res_data)

    # =========================================================================
    # Part 7: Audit Trail & Integrity (d9-adm-34 to 35)
    # =========================================================================

    # d9-adm-34: Audit log contains recorded administrative actions
    st, _, resp = http_json("GET", "/api/v1/admin/users/audit-logs", query_params={"size": 50}, token=token_admin)
    audit_entries = resp.get("data", {}).get("content", []) if resp else []
    actions = [e.get("action") for e in audit_entries]
    has_ban_or_approve = any(a in ("BAN_USER", "APPROVE_USER", "TIMEOUT_USER", "UNBAN_USER", "PERMISSION_GRANTED", "PERMISSION_REVOKED") for a in actions)
    passed = (st == 200 and len(audit_entries) > 0 and has_ban_or_approve)
    record("d9-adm-34", "Audit log trail contains administrative moderation events", passed, "200 OK contains BAN_USER/APPROVE_USER", f"{st} count={len(audit_entries)} actions_sampled={actions[:3]}", audit_entries[:3])

    # d9-adm-35: Suspended user attempting admin action rejection
    st, _, resp = http_json("GET", "/api/v1/admin/users", token=token_target)
    passed = (st in (401, 403))
    record("d9-adm-35", "Banned/suspended user attempting admin action rejection", passed, "401 UNAUTHORIZED or 403 FORBIDDEN", f"{st}", resp)

    # =========================================================================
    # Summary & Output
    # =========================================================================
    total = len(results)
    passed_count = sum(1 for r in results if r["passed"])
    failed_count = sum(1 for r in results if not r["passed"])
    print("\n" + "=" * 80)
    print(f"DOMAIN 9 VERIFICATION COMPLETE: {passed_count}/{total} PASSED ({passed_count/total*100:.1f}%)")
    print("=" * 80)

    save_report()

if __name__ == "__main__":
    main()
