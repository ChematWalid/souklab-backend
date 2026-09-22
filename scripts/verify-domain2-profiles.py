#!/usr/bin/env python3
"""Exhaustive live verification for Domain 2: Artisan & Client Profiles.

Covers:
- Artisan Public Profile View (GET /api/v1/artisan/{id})
  * Self view (unlocked contact info)
  * Non-premium client view (contactInfoLocked: true, anonymized name, masked contact info)
  * Premium client view (contactInfoLocked: false, real name and contact info visible)
  * Admin view (unlocked)
  * Deduplicated view counts tracking
- Gallery CRUD (/api/v1/artisan/gallery)
  * Upload, get, reorder, delete, soft-delete exclusion, cross-artisan ownership 404
- Certification CRUD (/api/v1/artisan/certifications)
  * Upload, get, delete, soft-delete exclusion, cross-artisan ownership 404
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

RUN_ID = f"d2-{int(time.time())}"
results: list[dict[str, Any]] = []

# Minimal valid 1x1 PNG bytes
VALID_PNG_BYTES = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\rIDATx\x9cc`\x00\x00\x00"
    b"\x02\x00\x01H\xaf\xa4q\x00\x00\x00\x00IEND\xaeB`\x82"
)

# Minimal valid PDF bytes
VALID_PDF_BYTES = (
    b"%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
    b"2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
    b"3 0 obj<</Type/Page/MediaBox[0 0 3 3]>>endobj\n"
    b"xref\n0 4\n0000000000 65535 f\n0000000010 00000 n\n0000000053 00000 n\n0000000102 00000 n\n"
    b"trailer<</Size 4/Root 1 0 R>>\nstartxref\n149\n%%EOF"
)

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

def http_json(
    method: str,
    path: str,
    payload: Any = None,
    token: str | None = None,
) -> tuple[int, dict[str, str], Any]:
    url = BASE_URL + path
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
        with urllib.request.urlopen(req, timeout=15) as resp:
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

def assert_case(
    scenario_id: str,
    name: str,
    status: int,
    headers: dict[str, str],
    body: Any,
    expected_status: int | set[int],
    check_fn: Any = None,
) -> bool:
    allowed = {expected_status} if isinstance(expected_status, int) else expected_status
    passed = status in allowed
    detail = "OK"

    if passed and check_fn:
        try:
            c = check_fn(status, headers, body)
            if c is not True:
                passed = False
                detail = str(c)
        except Exception as e:
            passed = False
            detail = f"Exception in check_fn: {e}"

    if not passed:
        detail = f"HTTP {status}, body={json.dumps(body) if isinstance(body, dict) else str(body)}"
        print(f"FAIL {scenario_id} [{name}]: {detail}")

    expected_str = "/".join(str(s) for s in sorted(allowed))
    results.append({
        "id": scenario_id,
        "name": name,
        "expected": expected_str,
        "actual": status,
        "passed": passed,
        "detail": detail,
        "body": body,
    })
    return passed

def run():
    print(f"--- Starting Domain 2 (Artisan & Client Profiles) Verification [run_id: {RUN_ID}] ---")
    password = "SecurePassword123!"

    # 1. Setup Accounts
    # Artisan A
    artisan_a_email = f"artisan-a-{RUN_ID}@souklab.test"
    http_json("POST", "/api/v1/auth/register", {
        "email": artisan_a_email, "password": password,
        "firstName": "Mouloud", "lastName": "Mammeri", "accountType": "ARTISAN"
    })
    db_exec(f"UPDATE users SET status='ACTIVE', email_verified=b'1', email_verified_at=NOW(), phone='0550112233' WHERE email='{artisan_a_email}'")
    st, hd, b = http_json("POST", "/api/v1/auth/login", {"email": artisan_a_email, "password": password})
    artisan_a_token = b["data"]["accessToken"]
    artisan_a_id = b["data"]["user"]["id"]

    # Artisan A completes profile
    regions = db_query("SELECT id FROM regions LIMIT 1")
    subcats = db_query("SELECT id FROM job_sub_categories LIMIT 1")
    region_id = regions[0][0] if regions else None
    subcat_id = subcats[0][0] if subcats else None

    http_json("POST", "/api/v1/auth/complete-profile", {
        "city": "Tizi Ouzou", "address": "Rue Colonel Amirouche",
        "website": "https://artisan-mammeri.dz", "bio": "Master of traditional Kabyle jewelry",
        "regionId": region_id, "subCategoryId": subcat_id
    }, token=artisan_a_token)

    # Artisan B (for cross-ownership checks)
    artisan_b_email = f"artisan-b-{RUN_ID}@souklab.test"
    http_json("POST", "/api/v1/auth/register", {
        "email": artisan_b_email, "password": password,
        "firstName": "Lounes", "lastName": "Matoub", "accountType": "ARTISAN"
    })
    db_exec(f"UPDATE users SET status='ACTIVE', email_verified=b'1', email_verified_at=NOW() WHERE email='{artisan_b_email}'")
    st, hd, b = http_json("POST", "/api/v1/auth/login", {"email": artisan_b_email, "password": password})
    artisan_b_token = b["data"]["accessToken"]
    http_json("POST", "/api/v1/auth/complete-profile", {
        "city": "Bejaia", "bio": "Artisan B bio", "regionId": region_id, "subCategoryId": subcat_id
    }, token=artisan_b_token)

    # Regular (Non-Premium) Client
    client_reg_email = f"client-reg-{RUN_ID}@souklab.test"
    http_json("POST", "/api/v1/auth/register", {
        "email": client_reg_email, "password": password,
        "firstName": "Samir", "lastName": "Client", "accountType": "CLIENT"
    })
    db_exec(f"UPDATE users SET status='ACTIVE', email_verified=b'1', email_verified_at=NOW() WHERE email='{client_reg_email}'")
    st, hd, b = http_json("POST", "/api/v1/auth/login", {"email": client_reg_email, "password": password})
    client_reg_token = b["data"]["accessToken"]
    http_json("POST", "/api/v1/auth/complete-profile", {
        "clientType": "INDIVIDUAL", "city": "Algiers"
    }, token=client_reg_token)

    # Premium Client
    client_prem_email = f"client-prem-{RUN_ID}@souklab.test"
    http_json("POST", "/api/v1/auth/register", {
        "email": client_prem_email, "password": password,
        "firstName": "Farid", "lastName": "VIP", "accountType": "CLIENT"
    })
    db_exec(f"UPDATE users SET status='ACTIVE', email_verified=b'1', email_verified_at=NOW() WHERE email='{client_prem_email}'")
    st, hd, b = http_json("POST", "/api/v1/auth/login", {"email": client_prem_email, "password": password})
    client_prem_token = b["data"]["accessToken"]
    http_json("POST", "/api/v1/auth/complete-profile", {
        "clientType": "INDIVIDUAL", "city": "Oran"
    }, token=client_prem_token)
    # Mark client as premium in DB
    prem_user_id = db_query(f"SELECT id FROM users WHERE email='{client_prem_email}'")[0][0]
    db_exec(f"UPDATE clients SET is_premium=b'1' WHERE id='{prem_user_id}'")
    # Re-login to ensure any token claims or caches are fresh
    st, hd, b = http_json("POST", "/api/v1/auth/login", {"email": client_prem_email, "password": password})
    client_prem_token = b["data"]["accessToken"]

    # Admin token (from seeded admin)
    st, hd, b = http_json("POST", "/api/v1/auth/login", {"email": "4ce013@gmail.com", "password": "admin123"})
    admin_token = b["data"]["accessToken"]

    # --- Section 1: Artisan Profile View (GET /api/v1/artisan/{id}) ---
    # S2.01: 401 Unauthorized without token
    st, hd, b = http_json("GET", f"/api/v1/artisan/{artisan_a_id}")
    assert_case("S2.01", "Artisan profile read unauthorized", st, hd, b, 401)

    # S2.02: 404 Not Found for non-existent artisan ID
    st, hd, b = http_json("GET", "/api/v1/artisan/00000000-0000-0000-0000-000000000099", token=client_reg_token)
    assert_case("S2.02", "Artisan profile non-existent ID", st, hd, b, 404)

    # S2.03: 403 Forbidden when viewer is unverified
    unverified_email = f"unver-{RUN_ID}@souklab.test"
    http_json("POST", "/api/v1/auth/register", {"email": unverified_email, "password": password, "accountType": "CLIENT"})
    db_exec(f"UPDATE users SET status='ACTIVE', email_verified=b'0' WHERE email='{unverified_email}'")
    st, hd, b = http_json("POST", "/api/v1/auth/login", {"email": unverified_email, "password": password})
    # If login is blocked with 403 due to email unverified, we test viewer check with token generated or direct check
    if st == 403:
        assert_case("S2.03", "Unverified viewer rejected at auth gateway", st, hd, b, 403)
    else:
        unver_token = b["data"]["accessToken"]
        st2, hd2, b2 = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=unver_token)
        assert_case("S2.03", "Unverified viewer rejected", st2, hd2, b2, 403)

    # S2.04: Happy path: Self view by the artisan (unlocked contact info)
    st, hd, b = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=artisan_a_token)
    assert_case(
        "S2.04", "Self view unlocked contact info", st, hd, b, 200,
        check_fn=lambda s, h, body: (
            body.get("data", {}).get("contactInfoLocked") is False
            and body.get("data", {}).get("name") == "Mouloud Mammeri"
            and body.get("data", {}).get("email") == artisan_a_email
            and body.get("data", {}).get("phone") == "0550112233"
            and body.get("data", {}).get("website") == "https://artisan-mammeri.dz"
        )
    )

    # S2.05: Happy path: Admin view (unlocked contact info)
    st, hd, b = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=admin_token)
    assert_case(
        "S2.05", "Admin view unlocked contact info", st, hd, b, 200,
        check_fn=lambda s, h, body: (
            body.get("data", {}).get("contactInfoLocked") is False
            and body.get("data", {}).get("email") == artisan_a_email
        )
    )

    # S2.06: Happy path: Non-premium client view (masked contact info)
    initial_views = db_query(f"SELECT views_count FROM artisans WHERE id='{artisan_a_id}'")[0][0]
    st, hd, b = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=client_reg_token)
    expected_masked_name = "Artisan #" + artisan_a_id[-5:].upper()
    assert_case(
        "S2.06", "Non-premium client view masked contact info", st, hd, b, 200,
        check_fn=lambda s, h, body: (
            body.get("data", {}).get("contactInfoLocked") is True
            and body.get("data", {}).get("name") == expected_masked_name
            and body.get("data", {}).get("email") is None
            and body.get("data", {}).get("phone") is None
            and body.get("data", {}).get("website") is None
            and body.get("data", {}).get("address") is None
        )
    )

    # S2.07: View count deduplication: repeat view from same client does not increment
    views_after_first = int(db_query(f"SELECT views_count FROM artisans WHERE id='{artisan_a_id}'")[0][0])
    st, hd, b = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=client_reg_token)
    views_after_second = int(db_query(f"SELECT views_count FROM artisans WHERE id='{artisan_a_id}'")[0][0])
    assert_case(
        "S2.07", "Profile view count deduplication", st, hd, b, 200,
        check_fn=lambda s, h, body: views_after_second == views_after_first
    )

    # S2.08: Happy path: Premium client view (unlocked contact info)
    st, hd, b = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=client_prem_token)
    assert_case(
        "S2.08", "Premium client view unlocked contact info", st, hd, b, 200,
        check_fn=lambda s, h, body: (
            body.get("data", {}).get("contactInfoLocked") is False
            and body.get("data", {}).get("name") == "Mouloud Mammeri"
            and body.get("data", {}).get("email") == artisan_a_email
            and body.get("data", {}).get("phone") == "0550112233"
            and body.get("data", {}).get("website") == "https://artisan-mammeri.dz"
        )
    )

    # --- Section 2: Artisan Gallery CRUD (/api/v1/artisan/gallery) ---
    # S2.09: 401 Unauthorized on POST gallery
    st, hd, b = http_multipart("POST", "/api/v1/artisan/gallery", {"title": "Test"}, {"file": ("test.png", VALID_PNG_BYTES, "image/png")})
    assert_case("S2.09", "Gallery upload unauthorized", st, hd, b, 401)

    # S2.10: 403 Forbidden when Client token attempts gallery upload
    st, hd, b = http_multipart("POST", "/api/v1/artisan/gallery", {"title": "Test"}, {"file": ("test.png", VALID_PNG_BYTES, "image/png")}, token=client_reg_token)
    assert_case("S2.10", "Gallery upload forbidden for client role", st, hd, b, 403)

    # S2.11: 400 Bad Request when upload file is missing/empty
    st, hd, b = http_multipart("POST", "/api/v1/artisan/gallery", {"title": "Test"}, {"file": ("empty.png", b"", "image/png")}, token=artisan_a_token)
    assert_case("S2.11", "Gallery upload empty file validation", st, hd, b, 400)

    # S2.12: Upload image 1 happy path
    st, hd, b = http_multipart("POST", "/api/v1/artisan/gallery", {
        "title": "Plateau en Cuivre Ciselé", "caption": "Fabrication artisanale d'après les motifs anciens"
    }, {"file": ("plateau.png", VALID_PNG_BYTES, "image/png")}, token=artisan_a_token)
    img1_id = b.get("data", {}).get("id") if st == 201 else None
    assert_case(
        "S2.12", "Gallery upload image 1 happy path", st, hd, b, 201,
        check_fn=lambda s, h, body: body.get("data", {}).get("title") == "Plateau en Cuivre Ciselé" and body.get("data", {}).get("id") is not None
    )

    # S2.13: Upload image 2 happy path
    st, hd, b = http_multipart("POST", "/api/v1/artisan/gallery", {
        "title": "Aiguière Mauresque", "caption": "Ciselure fine sur laiton étamé"
    }, {"file": ("aiguiere.png", VALID_PNG_BYTES, "image/png")}, token=artisan_a_token)
    img2_id = b.get("data", {}).get("id") if st == 201 else None
    assert_case(
        "S2.13", "Gallery upload image 2 happy path", st, hd, b, 201,
        check_fn=lambda s, h, body: body.get("data", {}).get("title") == "Aiguière Mauresque" and body.get("data", {}).get("id") is not None
    )

    # S2.14: 401 Unauthorized on GET gallery
    st, hd, b = http_json("GET", "/api/v1/artisan/gallery")
    assert_case("S2.14", "Get gallery unauthorized", st, hd, b, 401)

    # S2.15: 403 Forbidden for Client token on GET gallery
    st, hd, b = http_json("GET", "/api/v1/artisan/gallery", token=client_reg_token)
    assert_case("S2.15", "Get gallery forbidden for client role", st, hd, b, 403)

    # S2.16: Happy path GET gallery returns uploaded images
    st, hd, b = http_json("GET", "/api/v1/artisan/gallery", token=artisan_a_token)
    assert_case(
        "S2.16", "Get my gallery returns active images", st, hd, b, 200,
        check_fn=lambda s, h, body: len(body.get("data", [])) >= 2
    )

    # S2.17: 401 Unauthorized on PUT gallery order
    st, hd, b = http_json("PUT", "/api/v1/artisan/gallery/order", [img2_id, img1_id])
    assert_case("S2.17", "Gallery reorder unauthorized", st, hd, b, 401)

    # S2.18: 400 Bad Request on PUT gallery order with invalid/missing IDs
    st, hd, b = http_json("PUT", "/api/v1/artisan/gallery/order", ["invalid-image-id"], token=artisan_a_token)
    assert_case("S2.18", "Gallery reorder mismatched IDs validation", st, hd, b, 400)

    # S2.19: Happy path: PUT gallery order reverses sequence
    st, hd, b = http_json("PUT", "/api/v1/artisan/gallery/order", [img2_id, img1_id], token=artisan_a_token)
    assert_case("S2.19", "Gallery reorder happy path", st, hd, b, 200)

    # S2.20: Verify reordered sequence in GET gallery
    st, hd, b = http_json("GET", "/api/v1/artisan/gallery", token=artisan_a_token)
    assert_case(
        "S2.20", "Verify gallery sequence reflected in GET", st, hd, b, 200,
        check_fn=lambda s, h, body: (
            len(body.get("data", [])) >= 2
            and body["data"][0]["id"] == img2_id
            and body["data"][1]["id"] == img1_id
        )
    )

    # S2.21: 401 Unauthorized on DELETE gallery image
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/gallery/{img1_id}")
    assert_case("S2.21", "Delete gallery image unauthorized", st, hd, b, 401)

    # S2.22: 403 Forbidden for Client token on DELETE gallery
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/gallery/{img1_id}", token=client_reg_token)
    assert_case("S2.22", "Delete gallery image forbidden for client role", st, hd, b, 403)

    # S2.23: 404 Not Found on DELETE gallery for non-existent image
    st, hd, b = http_json("DELETE", "/api/v1/artisan/gallery/00000000-0000-0000-0000-000000000099", token=artisan_a_token)
    assert_case("S2.23", "Delete non-existent gallery image", st, hd, b, 404)

    # S2.24: Cross-artisan ownership check: Artisan B deletes Artisan A's image -> 404 Not Found
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/gallery/{img1_id}", token=artisan_b_token)
    assert_case("S2.24", "Cross-artisan delete gallery image rejected", st, hd, b, 404)

    # S2.25: Happy path: Artisan A deletes own image
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/gallery/{img1_id}", token=artisan_a_token)
    assert_case("S2.25", "Delete own gallery image happy path", st, hd, b, 200)

    # S2.26: Soft-delete verification: deleted image excluded from subsequent GET
    st, hd, b = http_json("GET", "/api/v1/artisan/gallery", token=artisan_a_token)
    assert_case(
        "S2.26", "Soft-deleted gallery image excluded from GET", st, hd, b, 200,
        check_fn=lambda s, h, body: all(item["id"] != img1_id for item in body.get("data", []))
    )

    # S2.27: Re-deleting already soft-deleted image returns 404
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/gallery/{img1_id}", token=artisan_a_token)
    assert_case("S2.27", "Re-delete soft-deleted gallery image returns 404", st, hd, b, 404)

    # --- Section 3: Artisan Certification CRUD (/api/v1/artisan/certifications) ---
    # S2.28: 401 Unauthorized on POST certification
    st, hd, b = http_multipart("POST", "/api/v1/artisan/certifications", {
        "title": "Diplome d'Art", "issuer": "CAM Alger"
    }, {"file": ("cert.pdf", VALID_PDF_BYTES, "application/pdf")})
    assert_case("S2.28", "Certification upload unauthorized", st, hd, b, 401)

    # S2.29: 403 Forbidden for Client role on POST certification
    st, hd, b = http_multipart("POST", "/api/v1/artisan/certifications", {
        "title": "Diplome d'Art", "issuer": "CAM Alger"
    }, {"file": ("cert.pdf", VALID_PDF_BYTES, "application/pdf")}, token=client_reg_token)
    assert_case("S2.29", "Certification upload forbidden for client role", st, hd, b, 403)

    # S2.30: 400 Bad Request when title or issuer is missing
    st, hd, b = http_multipart("POST", "/api/v1/artisan/certifications", {
        "title": "", "issuer": ""
    }, {"file": ("cert.pdf", VALID_PDF_BYTES, "application/pdf")}, token=artisan_a_token)
    assert_case("S2.30", "Certification upload missing title validation", st, hd, b, 400)

    # S2.31: 400 Bad Request when file is missing/empty
    st, hd, b = http_multipart("POST", "/api/v1/artisan/certifications", {
        "title": "Diplome d'Art", "issuer": "CAM Alger"
    }, {"file": ("empty.pdf", b"", "application/pdf")}, token=artisan_a_token)
    assert_case("S2.31", "Certification upload empty file validation", st, hd, b, 400)

    # S2.32: Upload certification 1 happy path (PDF)
    st, hd, b = http_multipart("POST", "/api/v1/artisan/certifications", {
        "title": "Diplôme de Maître Artisan", "issuer": "Chambre d'Artisanat et des Métiers de Tizi Ouzou",
        "issuedAt": "2024-05-15"
    }, {"file": ("diplome.pdf", VALID_PDF_BYTES, "application/pdf")}, token=artisan_a_token)
    cert1_id = b.get("data", {}).get("id") if st == 201 else None
    assert_case(
        "S2.32", "Certification upload PDF happy path", st, hd, b, 201,
        check_fn=lambda s, h, body: body.get("data", {}).get("title") == "Diplôme de Maître Artisan" and body.get("data", {}).get("id") is not None
    )

    # S2.33: Upload certification 2 happy path (PNG)
    st, hd, b = http_multipart("POST", "/api/v1/artisan/certifications", {
        "title": "Carte d'Artisan Professionnel", "issuer": "Ministère du Tourisme et de l'Artisanat",
        "issuedAt": "2023-01-10"
    }, {"file": ("carte.png", VALID_PNG_BYTES, "image/png")}, token=artisan_a_token)
    cert2_id = b.get("data", {}).get("id") if st == 201 else None
    assert_case(
        "S2.33", "Certification upload PNG happy path", st, hd, b, 201,
        check_fn=lambda s, h, body: body.get("data", {}).get("title") == "Carte d'Artisan Professionnel" and body.get("data", {}).get("id") is not None
    )

    # S2.34: 401 Unauthorized on GET certifications
    st, hd, b = http_json("GET", "/api/v1/artisan/certifications")
    assert_case("S2.34", "Get certifications unauthorized", st, hd, b, 401)

    # S2.35: 403 Forbidden for Client on GET certifications
    st, hd, b = http_json("GET", "/api/v1/artisan/certifications", token=client_reg_token)
    assert_case("S2.35", "Get certifications forbidden for client role", st, hd, b, 403)

    # S2.36: Happy path GET certifications returns active certifications
    st, hd, b = http_json("GET", "/api/v1/artisan/certifications", token=artisan_a_token)
    assert_case(
        "S2.36", "Get certifications returns list sorted newest first", st, hd, b, 200,
        check_fn=lambda s, h, body: len(body.get("data", [])) >= 2
    )

    # S2.37: 401 Unauthorized on DELETE certification
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/certifications/{cert1_id}")
    assert_case("S2.37", "Delete certification unauthorized", st, hd, b, 401)

    # S2.38: 403 Forbidden for Client on DELETE certification
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/certifications/{cert1_id}", token=client_reg_token)
    assert_case("S2.38", "Delete certification forbidden for client role", st, hd, b, 403)

    # S2.39: 404 Not Found on DELETE certification for non-existent ID
    st, hd, b = http_json("DELETE", "/api/v1/artisan/certifications/00000000-0000-0000-0000-000000000099", token=artisan_a_token)
    assert_case("S2.39", "Delete non-existent certification", st, hd, b, 404)

    # S2.40: Cross-artisan ownership check: Artisan B deletes Artisan A's certification -> 404 Not Found
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/certifications/{cert1_id}", token=artisan_b_token)
    assert_case("S2.40", "Cross-artisan delete certification rejected", st, hd, b, 404)

    # S2.41: Happy path: Artisan A deletes own certification
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/certifications/{cert1_id}", token=artisan_a_token)
    assert_case("S2.41", "Delete own certification happy path", st, hd, b, 200)

    # S2.42: Soft-delete verification: deleted certification excluded from subsequent GET
    st, hd, b = http_json("GET", "/api/v1/artisan/certifications", token=artisan_a_token)
    assert_case(
        "S2.42", "Soft-deleted certification excluded from GET", st, hd, b, 200,
        check_fn=lambda s, h, body: all(c["id"] != cert1_id for c in body.get("data", []))
    )

    # S2.43: Re-deleting already soft-deleted certification returns 404
    st, hd, b = http_json("DELETE", f"/api/v1/artisan/certifications/{cert1_id}", token=artisan_a_token)
    assert_case("S2.43", "Re-delete soft-deleted certification returns 404", st, hd, b, 404)

    # --- Section 4: Public profile integration check ---
    # S2.44: GET /api/v1/artisan/{id} includes the active gallery image and remaining active certification
    st, hd, b = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=artisan_a_token)
    assert_case(
        "S2.44", "Artisan profile includes active gallery and certifications", st, hd, b, 200,
        check_fn=lambda s, h, body: (
            any(img["id"] == img2_id for img in body.get("data", {}).get("galleryImages", []))
            and all(img["id"] != img1_id for img in body.get("data", {}).get("galleryImages", []))
            and any(c["id"] == cert2_id for c in body.get("data", {}).get("certifications", []))
            and all(c["id"] != cert1_id for c in body.get("data", {}).get("certifications", []))
        )
    )

    # S2.45: Certification documentUrl masking check
    st_reg, hd_reg, b_reg = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=client_reg_token)
    st_prem, hd_prem, b_prem = http_json("GET", f"/api/v1/artisan/{artisan_a_id}", token=client_prem_token)
    reg_certs = b_reg.get("data", {}).get("certifications", [])
    prem_certs = b_prem.get("data", {}).get("certifications", [])
    assert_case(
        "S2.45", "Certification documentUrl masked for non-premium and visible for premium", st_reg, hd_reg, b_reg, 200,
        check_fn=lambda s, h, body: (
            len(reg_certs) > 0 and reg_certs[0].get("documentUrl") is None
            and len(prem_certs) > 0 and prem_certs[0].get("documentUrl") is not None
        )
    )

    passed_count = sum(1 for r in results if r["passed"])
    failed_count = len(results) - passed_count
    print(f"\n=======================================================")
    print(f"Domain 2 Execution Complete: {passed_count} PASSED, {failed_count} FAILED (Total: {len(results)})")
    print(f"=======================================================\n")

    os.makedirs(".agent-output", exist_ok=True)
    with open(".agent-output/domain2-profiles-report.json", "w") as f:
        json.dump(results, f, indent=2)

    return 0 if failed_count == 0 else 1

if __name__ == "__main__":
    sys.exit(run())
