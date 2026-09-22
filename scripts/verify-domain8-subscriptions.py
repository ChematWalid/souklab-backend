#!/usr/bin/env python3
"""
Souklab Backend Exhaustive Verification - Domain 8: Subscriptions & Payments
(Regression confirmation pass - excludes external Chargily and OAuth login flows)
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
REPORT_FILE = os.path.join(OUTPUT_DIR, "domain8-subscriptions-report.json")

RUN_ID = f"d8-{int(time.time())}"

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
        "domain": "Domain 8: Subscriptions & Payments (Regression Confirmation)",
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

    # Verify email and set ACTIVE
    db_execute(f"UPDATE users SET email_verified = 1, status = 'ACTIVE' WHERE email = '{email}';")
    out = db_execute(f"SELECT id FROM users WHERE email = '{email}'")
    lines = out.splitlines()
    uid = lines[1].strip() if len(lines) > 1 else lines[0].strip()

    st_l, _, resp_l = http_json("POST", "/api/v1/auth/login", {"email": email, "password": password})
    if st_l != 200 or not resp_l.get("success"):
        raise RuntimeError(f"Login failed ({st_l}): {resp_l}")
    token = resp_l["data"]["accessToken"]

    reg_id = "00e08ab0-36c4-4e48-91f5-0a3ebfe168ea"
    if account_type == "ARTISAN":
        sub_id = "0a905e89-0723-4512-865d-1c65ba658641"
        http_json("POST", "/api/v1/auth/complete-profile", {
            "bio": "Artisan abonnement test",
            "city": "Boumerdès",
            "address": "123 Rue de l'Abonnement",
            "regionId": reg_id,
            "subCategoryId": sub_id,
        }, token=token)
    elif account_type == "CLIENT":
        http_json("POST", "/api/v1/auth/complete-profile", {
            "bio": "Client abonnement test",
            "city": "Alger",
            "address": "123 Rue Client",
            "regionId": reg_id,
        }, token=token)

    return token, uid


def main():
    print(f"=== Domain 8 Live Verification: Subscriptions & Payments (RUN_ID: {RUN_ID}) ===")

    # 1. Accounts Setup
    artisan_email = f"d8_artisan_{RUN_ID}@example.com"
    client_email = f"d8_client_{RUN_ID}@example.com"
    susp_email = f"d8_susp_{RUN_ID}@example.com"

    token_art, id_art = register_user(artisan_email, "Password123!", "ARTISAN", "Artisan Sub")
    token_cli, id_cli = register_user(client_email, "Password123!", "CLIENT", "Client Sub")
    token_susp, id_susp = register_user(susp_email, "Password123!", "CLIENT", "Suspended Sub")
    db_execute(f"UPDATE users SET status = 'SUSPENDED' WHERE id = '{id_susp}';")

    # Admin Login
    st_adm, _, resp_adm = http_json("POST", "/api/v1/auth/login", {"email": "4ce013@gmail.com", "password": "admin123"})
    if st_adm != 200:
        raise RuntimeError(f"Admin login failed: {st_adm} {resp_adm}")
    token_admin = resp_adm["data"]["accessToken"]

    print(f"✓ Accounts setup: Artisan ({id_art}), Client ({id_cli}), Suspended ({id_susp}), Admin (OK)")

    # =========================================================================
    # Part 1: Anonymous & Unauthorized Access Control (d8-sub-01 to 10)
    # =========================================================================

    # d8-sub-01: Public GET /api/v1/subscriptions/plans anonymous access -> 200 OK
    st, _, resp = http_json("GET", "/api/v1/subscriptions/plans")
    passed = (st == 200 and resp.get("success") is True and isinstance(resp.get("data"), list))
    record("d8-sub-01", "Public GET /api/v1/subscriptions/plans anonymous access", passed, "200 OK list returned", f"{st}", resp)

    # d8-sub-02: Anonymous GET /api/v1/subscriptions/current -> 401
    st, _, resp = http_json("GET", "/api/v1/subscriptions/current")
    passed = (st == 401)
    record("d8-sub-02", "Anonymous GET /api/v1/subscriptions/current rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d8-sub-03: Anonymous GET /api/v1/subscriptions -> 401
    st, _, resp = http_json("GET", "/api/v1/subscriptions")
    passed = (st == 401)
    record("d8-sub-03", "Anonymous GET /api/v1/subscriptions rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d8-sub-04: Anonymous POST /api/v1/subscriptions/{id}/cancel -> 401
    st, _, resp = http_json("POST", "/api/v1/subscriptions/dummy-id/cancel")
    passed = (st == 401)
    record("d8-sub-04", "Anonymous POST /api/v1/subscriptions/{id}/cancel rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d8-sub-05: Anonymous GET /api/v1/payments -> 401
    st, _, resp = http_json("GET", "/api/v1/payments")
    passed = (st == 401)
    record("d8-sub-05", "Anonymous GET /api/v1/payments rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d8-sub-06: Anonymous GET /api/v1/payments/{id} -> 401
    st, _, resp = http_json("GET", "/api/v1/payments/dummy-id")
    passed = (st == 401)
    record("d8-sub-06", "Anonymous GET /api/v1/payments/{id} rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d8-sub-07: Anonymous GET /api/v1/admin/subscription-plans -> 401
    st, _, resp = http_json("GET", "/api/v1/admin/subscription-plans")
    passed = (st == 401)
    record("d8-sub-07", "Anonymous GET /api/v1/admin/subscription-plans rejection", passed, "401 UNAUTHORIZED", f"{st}", resp)

    # d8-sub-08: Non-admin authenticated user GET /api/v1/admin/subscription-plans -> 403
    st, _, resp = http_json("GET", "/api/v1/admin/subscription-plans", token=token_art)
    passed = (st == 403)
    record("d8-sub-08", "Non-admin GET /api/v1/admin/subscription-plans rejection", passed, "403 FORBIDDEN", f"{st}", resp)

    # d8-sub-09: Non-admin authenticated user GET /api/v1/admin/subscriptions -> 403
    st, _, resp = http_json("GET", "/api/v1/admin/subscriptions", token=token_cli)
    passed = (st == 403)
    record("d8-sub-09", "Non-admin GET /api/v1/admin/subscriptions rejection", passed, "403 FORBIDDEN", f"{st}", resp)

    # d8-sub-10: Non-admin authenticated user POST /api/v1/admin/subscriptions/grant -> 403
    st, _, resp = http_json("POST", "/api/v1/admin/subscriptions/grant", {"accountId": id_art, "planId": "dummy", "reason": "test"}, token=token_art)
    passed = (st == 403)
    record("d8-sub-10", "Non-admin POST /api/v1/admin/subscriptions/grant rejection", passed, "403 FORBIDDEN", f"{st}", resp)

    # =========================================================================
    # Part 2: Fresh Accounts Baseline (d8-sub-11 to 13)
    # =========================================================================

    # d8-sub-11: Fresh Artisan account GET /api/v1/subscriptions/current -> 200 OK data=None
    st, _, resp = http_json("GET", "/api/v1/subscriptions/current", token=token_art)
    passed = (st == 200 and resp.get("success") is True and resp.get("data") is None)
    record("d8-sub-11", "Fresh Artisan account current subscription baseline (data=None)", passed, "200 OK data=None", f"{st} data={resp.get('data')}", resp)

    # d8-sub-12: Fresh Artisan account GET /api/v1/subscriptions history -> 200 OK data=[]
    st, _, resp = http_json("GET", "/api/v1/subscriptions", token=token_art)
    passed = (st == 200 and resp.get("success") is True and resp.get("data") == [])
    record("d8-sub-12", "Fresh Artisan account subscription history baseline (data=[])", passed, "200 OK data=[]", f"{st} data={resp.get('data')}", resp)

    # d8-sub-13: Fresh Artisan account GET /api/v1/payments -> 200 OK data=[]
    st, _, resp = http_json("GET", "/api/v1/payments", token=token_art)
    passed = (st == 200 and resp.get("success") is True and resp.get("data") == [])
    record("d8-sub-13", "Fresh Artisan account payments baseline (data=[])", passed, "200 OK data=[]", f"{st} data={resp.get('data')}", resp)

    # =========================================================================
    # Part 3: Plan Management by Admin (d8-sub-14 to 18)
    # =========================================================================

    # d8-sub-14: Admin creates a new Artisan Subscription Plan
    art_plan_req = {
        "subscriberType": "ARTISAN",
        "name": f"Plan Artisan Pro {RUN_ID}",
        "description": "Accès complet ateliers et visibilité prioritaire",
        "billingPeriod": "MONTHLY",
        "amount": 2500,
        "active": True,
        "entitlements": {"MAX_PRODUCTS": "100", "FEATURED_LISTING": "true"},
        "reason": "Création du plan artisan pro",
    }
    st, _, resp = http_json("POST", "/api/v1/admin/subscription-plans", art_plan_req, token=token_admin)
    art_plan_data = resp.get("data", {}) if resp else {}
    art_plan_id = art_plan_data.get("id")
    passed = (st in (200, 201) and bool(art_plan_id) and art_plan_data.get("subscriberType") == "ARTISAN")
    record("d8-sub-14", "Admin creates new Artisan Subscription Plan", passed, "200/201 CREATED id present", f"{st} id={art_plan_id}", resp)

    # d8-sub-15: Public GET /api/v1/subscriptions/plans lists newly created plan
    st, _, resp = http_json("GET", "/api/v1/subscriptions/plans")
    plans = resp.get("data", []) if resp else []
    found_art_plan = next((p for p in plans if p.get("id") == art_plan_id), None)
    passed = (st == 200 and found_art_plan is not None and found_art_plan.get("name") == art_plan_req["name"])
    record("d8-sub-15", "Public GET /api/v1/subscriptions/plans contains active artisan plan", passed, "200 OK plan included", f"{st} found={found_art_plan is not None}", found_art_plan)

    # d8-sub-16: Admin creates a new Client Subscription Plan
    cli_plan_req = {
        "subscriberType": "CLIENT",
        "name": f"Plan Client Club {RUN_ID}",
        "description": "Remises exclusives et réservations prioritaires",
        "billingPeriod": "YEARLY",
        "amount": 12000,
        "active": True,
        "entitlements": {"DISCOUNT_RATE": "10", "PRIORITY_BOOKING": "true"},
        "reason": "Création du plan club client",
    }
    st, _, resp = http_json("POST", "/api/v1/admin/subscription-plans", cli_plan_req, token=token_admin)
    cli_plan_data = resp.get("data", {}) if resp else {}
    cli_plan_id = cli_plan_data.get("id")
    passed = (st in (200, 201) and bool(cli_plan_id) and cli_plan_data.get("subscriberType") == "CLIENT")
    record("d8-sub-16", "Admin creates new Client Subscription Plan", passed, "200/201 CREATED id present", f"{st} id={cli_plan_id}", resp)

    # d8-sub-17: Admin lists all plans via GET /api/v1/admin/subscription-plans
    st, _, resp = http_json("GET", "/api/v1/admin/subscription-plans", token=token_admin)
    admin_plans = resp.get("data", []) if resp else []
    admin_ids = {p.get("id") for p in admin_plans}
    passed = (st == 200 and art_plan_id in admin_ids and cli_plan_id in admin_ids)
    record("d8-sub-17", "Admin lists all subscription plans", passed, "200 OK contains both created plans", f"{st} count={len(admin_plans)}", admin_plans)

    # d8-sub-18: Validation - Admin creates plan with invalid/blank name
    invalid_plan = dict(art_plan_req, name="")
    st, _, resp = http_json("POST", "/api/v1/admin/subscription-plans", invalid_plan, token=token_admin)
    passed = (st in (400, 422))
    record("d8-sub-18", "Admin create plan validation error (blank name)", passed, "400/422 BAD_REQUEST", f"{st}", resp)

    # =========================================================================
    # Part 4: Manual Subscription Grant & Business Logic (d8-sub-19 to 27)
    # =========================================================================

    # d8-sub-19: Admin manually grants subscription to Artisan
    grant_req_art = {
        "accountId": id_art,
        "planId": art_plan_id,
        "reason": "Attribution manuelle pour artisan certifié",
    }
    st, _, resp = http_json("POST", "/api/v1/admin/subscriptions/grant", grant_req_art, token=token_admin)
    grant_data_art = resp.get("data", {}) if resp else {}
    sub_art_id = grant_data_art.get("id")
    passed = (st in (200, 201) and bool(sub_art_id) and grant_data_art.get("status") == "ACTIVE")
    record("d8-sub-19", "Admin manually grants subscription to Artisan", passed, "200/201 CREATED status=ACTIVE", f"{st} subId={sub_art_id}", resp)

    # d8-sub-20: Duplicate grant rejection (already active)
    st, _, resp = http_json("POST", "/api/v1/admin/subscriptions/grant", grant_req_art, token=token_admin)
    passed = (st == 400 and "already has an active subscription" in str(resp.get("message", "")))
    record("d8-sub-20", "Duplicate active subscription grant rejection", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d8-sub-21: Mismatched subscriber type grant rejection (client plan to artisan)
    grant_mismatch = {
        "accountId": id_art,
        "planId": cli_plan_id,
        "reason": "Tentative invalide plan client",
    }
    st, _, resp = http_json("POST", "/api/v1/admin/subscriptions/grant", grant_mismatch, token=token_admin)
    passed = (st == 400 and "does not have a client profile" in str(resp.get("message", "")))
    record("d8-sub-21", "Mismatched subscriber type grant rejection", passed, "400 BAD_REQUEST", f"{st} - {resp.get('message')}", resp)

    # d8-sub-22: Artisan GET /api/v1/subscriptions/current reflects granted subscription
    st, _, resp = http_json("GET", "/api/v1/subscriptions/current", token=token_art)
    cur_sub = resp.get("data", {}) if resp else {}
    passed = (st == 200 and cur_sub.get("id") == sub_art_id and cur_sub.get("status") == "ACTIVE")
    record("d8-sub-22", "Artisan current subscription reflects ACTIVE granted plan", passed, "200 OK id matches status=ACTIVE", f"{st} id={cur_sub.get('id')}", cur_sub)

    # d8-sub-23: Artisan profile premium status set to true
    st, _, resp = http_json("GET", "/api/v1/auth/me", token=token_art)
    profile_data = resp.get("data", {}) if resp else {}
    passed = (st == 200 and profile_data.get("premium") is True)
    record("d8-sub-23", "Artisan profile reflects premium=true following grant", passed, "200 OK premium=true", f"{st} premium={profile_data.get('premium')}", profile_data)

    # d8-sub-24: Artisan GET /api/v1/subscriptions history contains 1 subscription
    st, _, resp = http_json("GET", "/api/v1/subscriptions", token=token_art)
    hist = resp.get("data", []) if resp else []
    passed = (st == 200 and len(hist) == 1 and hist[0].get("id") == sub_art_id)
    record("d8-sub-24", "Artisan subscription history contains granted record", passed, "200 OK 1 subscription", f"{st} count={len(hist)}", hist)

    # d8-sub-25: Artisan GET /api/v1/payments contains 1 manually granted payment record
    st, _, resp = http_json("GET", "/api/v1/payments", token=token_art)
    art_payments = resp.get("data", []) if resp else []
    first_payment = art_payments[0] if art_payments else {}
    pay_id = first_payment.get("id")
    passed = (st == 200 and len(art_payments) == 1 and first_payment.get("status") == "MANUALLY_GRANTED")
    record("d8-sub-25", "Artisan payments contains MANUALLY_GRANTED payment record", passed, "200 OK status=MANUALLY_GRANTED", f"{st} count={len(art_payments)} payId={pay_id}", first_payment)

    # d8-sub-26: Artisan GET /api/v1/payments/{paymentId} returns details
    st, _, resp = http_json("GET", f"/api/v1/payments/{pay_id}", token=token_art)
    single_pay = resp.get("data", {}) if resp else {}
    passed = (st == 200 and single_pay.get("id") == pay_id and single_pay.get("subscriptionId") == sub_art_id)
    record("d8-sub-26", "Artisan GET /api/v1/payments/{id} details", passed, "200 OK matches payment", f"{st} payId={single_pay.get('id')}", single_pay)

    # d8-sub-27: Cross-account payment privacy (Client attempts GET /api/v1/payments/{artisanPaymentId} -> 404)
    st, _, resp = http_json("GET", f"/api/v1/payments/{pay_id}", token=token_cli)
    passed = (st == 404 and "Payment not found" in str(resp.get("message", "")))
    record("d8-sub-27", "Cross-account payment details access rejection (404 convention)", passed, "404 NOT_FOUND", f"{st} - {resp.get('message')}", resp)

    # =========================================================================
    # Part 5: Subscription Cancellation & Client Lifecycle (d8-sub-28 to 34)
    # =========================================================================

    # d8-sub-28: Artisan cancels subscription POST /api/v1/subscriptions/{subId}/cancel -> 200 OK
    st, _, resp = http_json("POST", f"/api/v1/subscriptions/{sub_art_id}/cancel", token=token_art)
    passed = (st == 200 and resp.get("success") is True)
    record("d8-sub-28", "Artisan cancels own active subscription", passed, "200 OK", f"{st}", resp)

    # d8-sub-29: Admin manually grants subscription to Client
    grant_req_cli = {
        "accountId": id_cli,
        "planId": cli_plan_id,
        "reason": "Attribution manuelle client club VIP",
    }
    st, _, resp = http_json("POST", "/api/v1/admin/subscriptions/grant", grant_req_cli, token=token_admin)
    grant_data_cli = (resp.get("data") or {}) if resp else {}
    sub_cli_id = grant_data_cli.get("id")
    passed = (st in (200, 201) and bool(sub_cli_id) and grant_data_cli.get("status") == "ACTIVE")
    record("d8-sub-29", "Admin manually grants subscription to Client", passed, "200/201 CREATED status=ACTIVE", f"{st} subId={sub_cli_id}", resp)

    # d8-sub-30: Admin lists all subscriptions GET /api/v1/admin/subscriptions
    st, _, resp = http_json("GET", "/api/v1/admin/subscriptions", token=token_admin)
    all_admin_subs = resp.get("data", []) if resp else []
    admin_sub_ids = {s.get("id") for s in all_admin_subs}
    passed = (st == 200 and sub_art_id in admin_sub_ids and sub_cli_id in admin_sub_ids)
    record("d8-sub-30", "Admin lists all subscriptions across users", passed, "200 OK includes artisan and client subscriptions", f"{st} count={len(all_admin_subs)}", all_admin_subs)

    # d8-sub-31: Admin lists all payments GET /api/v1/admin/subscriptions/payments
    st, _, resp = http_json("GET", "/api/v1/admin/subscriptions/payments", token=token_admin)
    all_admin_pays = resp.get("data", []) if resp else []
    passed = (st == 200 and len(all_admin_pays) >= 2)
    record("d8-sub-31", "Admin lists all payments across platform", passed, "200 OK count >= 2", f"{st} count={len(all_admin_pays)}", all_admin_pays)

    # d8-sub-32: Admin lists webhook logs GET /api/v1/admin/subscriptions/webhooks
    st, _, resp = http_json("GET", "/api/v1/admin/subscriptions/webhooks", token=token_admin)
    passed = (st == 200 and isinstance(resp.get("data"), list))
    record("d8-sub-32", "Admin lists webhook logs (empty or array)", passed, "200 OK list returned", f"{st} count={len(resp.get('data', []))}", resp)

    # d8-sub-33: Admin revokes client subscription POST /api/v1/admin/subscriptions/{subId}/revoke
    st, _, resp = http_json("POST", f"/api/v1/admin/subscriptions/{sub_cli_id}/revoke", {"reason": "Annulation administrative par la modération"}, token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d8-sub-33", "Admin revokes Client subscription", passed, "200 OK", f"{st}", resp)

    # d8-sub-34: Client subscription reflects REVOKED status and current is null
    st, _, resp = http_json("GET", "/api/v1/subscriptions/current", token=token_cli)
    st_h, _, resp_h = http_json("GET", "/api/v1/subscriptions", token=token_cli)
    h_list = resp_h.get("data", []) if resp_h else []
    revoked_sub = next((s for s in h_list if s.get("id") == sub_cli_id), None)
    passed = (st == 200 and resp.get("data") is None and revoked_sub is not None and revoked_sub.get("status") == "REVOKED")
    record("d8-sub-34", "Client subscription reflects REVOKED status and current is null", passed, "200 OK current=null status=REVOKED", f"current={resp.get('data')} status={revoked_sub.get('status') if revoked_sub else None}", revoked_sub)

    # =========================================================================
    # Part 6: Plan Deactivation & Suspended Access (d8-sub-35 to 37)
    # =========================================================================

    # d8-sub-35: Admin deactivates subscription plan DELETE /api/v1/admin/subscription-plans/{planId}
    st, _, resp = http_json("DELETE", f"/api/v1/admin/subscription-plans/{cli_plan_id}", {"reason": "Fin de l'offre commerciale club"}, token=token_admin)
    passed = (st == 200 and resp.get("success") is True)
    record("d8-sub-35", "Admin deactivates subscription plan", passed, "200 OK", f"{st}", resp)

    # d8-sub-36: Public plan list excludes deactivated plan
    st, _, resp = http_json("GET", "/api/v1/subscriptions/plans")
    current_public_plans = resp.get("data", []) if resp else []
    found_deactivated = next((p for p in current_public_plans if p.get("id") == cli_plan_id), None)
    passed = (st == 200 and found_deactivated is None)
    record("d8-sub-36", "Public plan list excludes deactivated plan", passed, "200 OK deactivated plan excluded", f"{st} found={found_deactivated is not None}", current_public_plans)

    # d8-sub-37: Suspended user access rejection on subscriptions
    st, _, resp = http_json("GET", "/api/v1/subscriptions/current", token=token_susp)
    passed = (st in (401, 403))
    record("d8-sub-37", "Suspended user current subscription access rejection", passed, "401 UNAUTHORIZED or 403 FORBIDDEN", f"{st}", resp)

    # =========================================================================
    # Summary & Output
    # =========================================================================
    total = len(results)
    passed_count = sum(1 for r in results if r["passed"])
    failed_count = sum(1 for r in results if not r["passed"])
    print("\n" + "=" * 80)
    print(f"DOMAIN 8 VERIFICATION COMPLETE: {passed_count}/{total} PASSED ({passed_count/total*100:.1f}%)")
    print("=" * 80)

    save_report()

if __name__ == "__main__":
    main()
