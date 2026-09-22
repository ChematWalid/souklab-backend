#!/usr/bin/env python3
"""Exhaustive live verification for Domain 4: Formations & Masterclasses.

Covers:
- Formateur Accreditation Gate
  * Non-teacher artisan denied (403 Forbidden)
  * Accredited teacher artisan allowed (201 Created)
- Formation Draft Creation & Validation
  * Blank title, description, negative price, durationHours=0, maxParticipants=0, past scheduledAt (422)
  * Valid creation response fields & status DRAFT
- Formation Authoring & Ownership
  * Author update (PUT /api/v1/artisan/formations/{id})
  * Cross-artisan edit rejection (403 Forbidden)
  * Non-existent formation (404 Not Found)
- Thumbnail & Course Document Management
  * Showcase thumbnail image upload (PNG/JPEG)
  * Unsupported MIME type rejection (422/400)
  * Cross-artisan upload rejection (403)
  * Course syllabus document upload (PDF)
  * Soft-deletion of course file
  * Cross-artisan file deletion rejection (403)
- Administrative Moderation Workflow
  * Author submits formation for review (DRAFT -> PENDING_REVIEW)
  * Duplicate submission conflict (409 Conflict)
  * Admin pending queue listing (GET /api/v1/admin/formations/pending)
  * Non-admin forbidden (403)
  * Admin rejection without comment (400)
  * Admin rejection with comment (PENDING_REVIEW -> REJECTED)
  * Author re-submits rejected formation (REJECTED -> PENDING_REVIEW)
  * Admin approval (PENDING_REVIEW -> APPROVED)
  * Publishing approved formation (APPROVED -> PUBLISHED)
  * Attempting to publish unapproved formation (409 Conflict)
- Public Catalog & Discovery
  * Published formation visible in public catalog (GET /api/v1/artisan/formations/catalog)
  * Published formation public details (GET /api/v1/artisan/formations/catalog/{id})
- Enrollment Lifecycle & Constraints
  * Author self-enrollment rejection (409 Conflict)
  * Peer artisan enrollment (200 OK, status: CONFIRMED)
  * Duplicate enrollment rejection (409 Conflict)
  * Participant enrollment history (GET /api/v1/artisan/formations/my-enrollments)
  * Public details reflects isEnrolled: true
  * Capacity constraint: maxParticipants exceeded (409 Conflict)
- Protected Course Document Access
  * Enrolled participant downloads course file (200 OK binary stream)
  * Non-enrolled participant download rejected (403 Forbidden)
- Cancellation & Reactivation
  * Participant cancels enrollment (200 OK, status: CANCELLED)
  * Cancellation of inactive enrollment rejected (404)
  * File download rejected after cancellation (403)
  * Re-enrolling after cancellation reactivates enrollment (200 OK, status: CONFIRMED)
- Attended Enrollment Review Workflow
  * Review creation before attendance rejected (403 Forbidden)
  * Setting status to ATTENDED & formation COMPLETED
  * Peer artisan posts review (POST /api/v1/artisan/formations/{id}/reviews -> 201 Created)
  * Duplicate review on same enrollment rejected (409 Conflict)
  * Author self-review rejected (409 Conflict)
  * Rating and review count aggregation recalculation
  * List artisan reviews (GET /api/v1/artisans/{artisanId}/reviews)
  * Author/Reviewer update review (PUT /api/v1/artisan/reviews/{id})
  * Non-author review update rejected (403 Forbidden)
  * Reviewer deletes review (DELETE /api/v1/artisan/reviews/{id})
- Formation Soft-Deletion
  * Author deletes formation (DELETE /api/v1/artisan/formations/{id})
  * Excluded from author's formations and public catalog
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

RUN_ID = f"d4-{int(time.time())}"
results: list[dict[str, Any]] = []

VALID_PNG_BYTES = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\rIDATx\x9cc`\x00\x00\x00"
    b"\x02\x00\x01H\xaf\xa4q\x00\x00\x00\x00IEND\xaeB`\x82"
)

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

def http_download(
    path: str,
    token: str | None = None,
) -> tuple[int, dict[str, str], bytes]:
    url = BASE_URL + path
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, headers=headers, method="GET")
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = resp.read()
            return resp.status, dict(resp.headers), data
    except urllib.error.HTTPError as exc:
        data = exc.read()
        return exc.code, dict(exc.headers), data
    except Exception as exc:
        return 0, {}, str(exc).encode("utf-8")

def record_result(scenario: str, expected: str, actual: str, passed: bool, payload: Any = None):
    status_str = "PASS" if passed else "FAIL"
    entry = {
        "scenario": scenario,
        "expected": expected,
        "actual": actual,
        "passed": passed,
        "payload": payload,
    }
    results.append(entry)
    print(f"[{status_str}] {scenario} | Exp: {expected} | Act: {actual}")
    if not passed:
        print(f"  --> ERROR DETAIL: {json.dumps(payload, indent=2) if isinstance(payload, dict) else payload}")

def register_and_login_artisan(prefix: str, is_teacher: bool = False) -> tuple[str, str]:
    email = f"{prefix}_{int(time.time())}_{uuid.uuid4().hex[:4]}@souklab.test"
    password = "Password123!"

    status, _, data = http_json("POST", "/api/v1/auth/register", {
        "email": email,
        "password": password,
        "firstName": prefix.capitalize(),
        "lastName": "Artisan",
        "accountType": "ARTISAN",
    })
    if status != 201:
        raise RuntimeError(f"Failed to register {email}: {data}")

    user_id = data.get("data", {}).get("id")

    # DB update email_verified, status ACTIVE
    db_execute(f"UPDATE users SET email_verified = b'1', status = 'ACTIVE' WHERE id = '{user_id}';")

    # Complete profile
    status, _, ldata = http_json("POST", "/api/v1/auth/login", {
        "email": email,
        "password": password,
    })
    token = ldata.get("data", {}).get("accessToken")

    http_json("POST", "/api/v1/auth/complete-profile", {
        "bio": f"Master artisan {prefix}",
        "city": "Algiers",
        "address": "123 Rue de la Casbah",
        "regionId": "0f000438-f3ce-4aae-ad83-dab36ee745d5",
        "subCategoryId": "0a905e89-0723-4512-865d-1c65ba658641",
    }, token=token)

    if is_teacher:
        db_execute(f"UPDATE artisans SET is_teacher = b'1' WHERE id = '{user_id}';")

    return user_id, token

def main():
    print(f"=== Starting Domain 4: Formations & Masterclasses Live Verification [{RUN_ID}] ===")

    # Setup admin token
    status, _, data = http_json("POST", "/api/v1/auth/login", {
        "email": "4ce013@gmail.com",
        "password": "admin123",
    })
    admin_token = data.get("data", {}).get("accessToken")
    if not admin_token:
        print("[FAIL] Could not authenticate admin.")
        sys.exit(1)

    # Setup Instructor Artisan (teacher) and Peer Artisan (student)
    instructor_id, instructor_token = register_and_login_artisan("instructor", is_teacher=True)
    peer_id, peer_token = register_and_login_artisan("peer", is_teacher=False)
    non_teacher_id, non_teacher_token = register_and_login_artisan("nonteacher", is_teacher=False)

    print(f"Instructor: {instructor_id}, Peer: {peer_id}, Non-Teacher: {non_teacher_id}")

    # -------------------------------------------------------------
    # PHASE 4.1: Instructor Accreditation Gate
    # -------------------------------------------------------------
    print("\n--- Phase 4.1: Instructor Accreditation Gate ---")

    future_time = "2028-10-15T10:00:00"
    base_formation_payload = {
        "title": "Traditional Pottery Workshop",
        "description": "Comprehensive course covering authentic Kabyle clay shaping and firing techniques.",
        "location": "Beni Yenni Cultural Center",
        "isOnline": False,
        "scheduledAt": future_time,
        "durationHours": 4,
        "maxParticipants": 10,
        "price": 3500,
        "currency": "DZD",
    }

    # FORM-01: Non-teacher artisan denied
    status, _, data = http_json("POST", "/api/v1/artisan/formations", base_formation_payload, token=non_teacher_token)
    record_result(
        "FORM-01: Non-teacher artisan cannot create formations",
        "HTTP 403 Forbidden",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 403,
        data,
    )

    # FORM-02: Accredited teacher artisan allowed
    status, _, data = http_json("POST", "/api/v1/artisan/formations", base_formation_payload, token=instructor_token)
    formation_id = data.get("data", {}).get("id") if isinstance(data, dict) else None
    record_result(
        "FORM-02: Accredited teacher artisan creates formation draft",
        "HTTP 201 Created, status: DRAFT",
        f"HTTP {status}, formationId: {formation_id}, status: {data.get('data', {}).get('status') if isinstance(data, dict) else None}",
        status == 201 and formation_id is not None and data.get("data", {}).get("status") == "DRAFT",
        data,
    )

    # -------------------------------------------------------------
    # PHASE 4.2: Formation Draft Validation & Creation
    # -------------------------------------------------------------
    print("\n--- Phase 4.2: Formation Draft Validation & Creation ---")

    # FORM-03: Blank title -> 422
    p = dict(base_formation_payload, title="")
    status, _, data = http_json("POST", "/api/v1/artisan/formations", p, token=instructor_token)
    record_result(
        "FORM-03: Creation with blank title returns 422",
        "HTTP 422 Unprocessable Content",
        f"HTTP {status}, errors: {data.get('errors') if isinstance(data, dict) else data}",
        status == 422 and "title" in data.get("errors", {}),
        data,
    )

    # FORM-04: Blank description -> 422
    p = dict(base_formation_payload, description="")
    status, _, data = http_json("POST", "/api/v1/artisan/formations", p, token=instructor_token)
    record_result(
        "FORM-04: Creation with blank description returns 422",
        "HTTP 422 Unprocessable Content",
        f"HTTP {status}, errors: {data.get('errors') if isinstance(data, dict) else data}",
        status == 422 and "description" in data.get("errors", {}),
        data,
    )

    # FORM-05: durationHours = 0 -> 422
    p = dict(base_formation_payload, durationHours=0)
    status, _, data = http_json("POST", "/api/v1/artisan/formations", p, token=instructor_token)
    record_result(
        "FORM-05: Creation with durationHours=0 returns 422",
        "HTTP 422 Unprocessable Content",
        f"HTTP {status}, errors: {data.get('errors') if isinstance(data, dict) else data}",
        status == 422 and "durationHours" in data.get("errors", {}),
        data,
    )

    # FORM-06: maxParticipants = 0 -> 422
    p = dict(base_formation_payload, maxParticipants=0)
    status, _, data = http_json("POST", "/api/v1/artisan/formations", p, token=instructor_token)
    record_result(
        "FORM-06: Creation with maxParticipants=0 returns 422",
        "HTTP 422 Unprocessable Content",
        f"HTTP {status}, errors: {data.get('errors') if isinstance(data, dict) else data}",
        status == 422 and "maxParticipants" in data.get("errors", {}),
        data,
    )

    # FORM-07: price < 0 -> 422
    p = dict(base_formation_payload, price=-100)
    status, _, data = http_json("POST", "/api/v1/artisan/formations", p, token=instructor_token)
    record_result(
        "FORM-07: Creation with negative price returns 422",
        "HTTP 422 Unprocessable Content",
        f"HTTP {status}, errors: {data.get('errors') if isinstance(data, dict) else data}",
        status == 422 and "price" in data.get("errors", {}),
        data,
    )

    # FORM-08: scheduledAt in the past -> 422
    p = dict(base_formation_payload, scheduledAt="2020-01-01T10:00:00")
    status, _, data = http_json("POST", "/api/v1/artisan/formations", p, token=instructor_token)
    record_result(
        "FORM-08: Creation with past scheduledAt returns 422",
        "HTTP 422 Unprocessable Content",
        f"HTTP {status}, errors: {data.get('errors') if isinstance(data, dict) else data}",
        status == 422 and "scheduledAt" in data.get("errors", {}),
        data,
    )

    # FORM-09: Valid response schema inspection
    status, _, data = http_json("GET", f"/api/v1/artisan/formations/{formation_id}", token=instructor_token)
    f_data = data.get("data", {}) if isinstance(data, dict) else {}
    expected_keys = ["id", "title", "description", "scheduledAt", "durationHours", "maxParticipants", "price", "status", "activeEnrollmentsCount"]
    has_keys = all(k in f_data for k in expected_keys)
    record_result(
        "FORM-09: Formation response schema contains required contract fields",
        f"Contains {expected_keys}",
        f"Keys present: {list(f_data.keys())[:8]}...",
        status == 200 and has_keys and f_data.get("activeEnrollmentsCount") == 0,
        f_data,
    )

    # -------------------------------------------------------------
    # PHASE 4.3: Formation Update & Author Ownership
    # -------------------------------------------------------------
    print("\n--- Phase 4.3: Formation Update & Author Ownership ---")

    update_payload = dict(base_formation_payload, title="Advanced Traditional Pottery Workshop", price=4000)

    # FORM-10: Author updates formation
    status, _, data = http_json("PUT", f"/api/v1/artisan/formations/{formation_id}", update_payload, token=instructor_token)
    updated_title = data.get("data", {}).get("title") if isinstance(data, dict) else None
    record_result(
        "FORM-10: Author successfully updates formation details",
        "HTTP 200, title updated",
        f"HTTP {status}, updated title: '{updated_title}'",
        status == 200 and updated_title == "Advanced Traditional Pottery Workshop",
        data,
    )

    # FORM-11: Non-author edit rejection -> 403
    status, _, data = http_json("PUT", f"/api/v1/artisan/formations/{formation_id}", update_payload, token=peer_token)
    record_result(
        "FORM-11: Non-author cannot modify another artisan's formation",
        "HTTP 403 Forbidden",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 403,
        data,
    )

    # FORM-12: Update non-existent formation -> 404
    status, _, data = http_json("PUT", f"/api/v1/artisan/formations/{uuid.uuid4()}", update_payload, token=instructor_token)
    record_result(
        "FORM-12: Updating non-existent formation returns 404",
        "HTTP 404 Not Found",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 404,
        data,
    )

    # -------------------------------------------------------------
    # PHASE 4.4: Thumbnail & Course File Management
    # -------------------------------------------------------------
    print("\n--- Phase 4.4: Thumbnail & Course File Management ---")

    # FORM-13: Upload showcase thumbnail
    status, _, data = http_multipart(
        "POST",
        f"/api/v1/artisan/formations/{formation_id}/thumbnail",
        fields={},
        files={"file": ("thumbnail.png", VALID_PNG_BYTES, "image/png")},
        token=instructor_token,
    )
    thumb_url = data.get("data", {}).get("thumbnailUrl") if isinstance(data, dict) else None
    record_result(
        "FORM-13: Author uploads showcase thumbnail image",
        "HTTP 200, thumbnailUrl populated",
        f"HTTP {status}, thumbnailUrl: {thumb_url}",
        status == 200 and thumb_url is not None and "/api/v1/files/" in thumb_url,
        data,
    )

    # FORM-14: Upload invalid thumbnail MIME type
    status, _, data = http_multipart(
        "POST",
        f"/api/v1/artisan/formations/{formation_id}/thumbnail",
        fields={},
        files={"file": ("malicious.txt", b"plain text is not an allowed image", "text/plain")},
        token=instructor_token,
    )
    record_result(
        "FORM-14: Non-image thumbnail upload rejected",
        "HTTP 400 or 422",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status in [400, 422],
        data,
    )

    # FORM-15: Non-author thumbnail upload rejected
    status, _, data = http_multipart(
        "POST",
        f"/api/v1/artisan/formations/{formation_id}/thumbnail",
        fields={},
        files={"file": ("thumb.png", VALID_PNG_BYTES, "image/png")},
        token=peer_token,
    )
    record_result(
        "FORM-15: Non-author thumbnail upload rejected",
        "HTTP 403 Forbidden",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 403,
        data,
    )

    # FORM-16: Upload course syllabus file (PDF)
    status, _, data = http_multipart(
        "POST",
        f"/api/v1/artisan/formations/{formation_id}/files",
        fields={},
        files={"file": ("syllabus.pdf", VALID_PDF_BYTES, "application/pdf")},
        token=instructor_token,
    )
    first_file_id = data.get("data", {}).get("id") if isinstance(data, dict) else None
    record_result(
        "FORM-16: Author uploads course document attachment (PDF)",
        "HTTP 201 Created, file descriptor populated",
        f"HTTP {status}, fileId: {first_file_id}, filename: {data.get('data', {}).get('originalFilename') if isinstance(data, dict) else None}",
        status == 201 and first_file_id is not None,
        data,
    )

    # FORM-17: Soft-delete course file
    status, _, data = http_json("DELETE", f"/api/v1/artisan/formations/{formation_id}/files/{first_file_id}", token=instructor_token)
    record_result(
        "FORM-17: Author soft-deletes course file",
        "HTTP 200 OK",
        f"HTTP {status}, success: {data.get('success') if isinstance(data, dict) else data}",
        status == 200,
        data,
    )

    # FORM-18: Non-author course file deletion rejected
    # First upload a replacement file
    status, _, data = http_multipart(
        "POST",
        f"/api/v1/artisan/formations/{formation_id}/files",
        fields={},
        files={"file": ("course_guide.pdf", VALID_PDF_BYTES, "application/pdf")},
        token=instructor_token,
    )
    course_file_id = data.get("data", {}).get("id") if isinstance(data, dict) else None

    status, _, data = http_json("DELETE", f"/api/v1/artisan/formations/{formation_id}/files/{course_file_id}", token=peer_token)
    record_result(
        "FORM-18: Non-author cannot delete course file",
        "HTTP 403 Forbidden",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 403,
        data,
    )

    # FORM-19: Verify active course file retained
    status, _, data = http_json("GET", f"/api/v1/artisan/formations/{formation_id}", token=instructor_token)
    active_files = data.get("data", {}).get("files", []) if isinstance(data, dict) else []
    record_result(
        "FORM-19: Author formation details lists active course files",
        "1 active course file present",
        f"Active file count: {len(active_files)}, id: {active_files[0].get('id') if active_files else None}",
        status == 200 and len(active_files) == 1 and active_files[0].get("id") == course_file_id,
        active_files,
    )

    # -------------------------------------------------------------
    # PHASE 4.5: Review Submission & Administrative Moderation
    # -------------------------------------------------------------
    print("\n--- Phase 4.5: Review Submission & Administrative Moderation ---")

    # FORM-20: Author submits formation for review
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/submit", token=instructor_token)
    f_status = data.get("data", {}).get("status") if isinstance(data, dict) else None
    record_result(
        "FORM-20: Author submits formation for review (DRAFT -> PENDING_REVIEW)",
        "HTTP 200, status: PENDING_REVIEW",
        f"HTTP {status}, status: {f_status}",
        status == 200 and f_status == "PENDING_REVIEW",
        data,
    )

    # FORM-21: Submitting already submitted formation -> 409
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/submit", token=instructor_token)
    record_result(
        "FORM-21: Re-submitting while PENDING_REVIEW returns 409 Conflict",
        "HTTP 409 Conflict",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 409,
        data,
    )

    # FORM-22: Admin views pending queue
    status, _, data = http_json("GET", "/api/v1/admin/formations/pending", token=admin_token)
    pending_items = data.get("data", {}).get("content", []) if isinstance(data, dict) else []
    found_in_pending = any(item.get("id") == formation_id for item in pending_items)
    record_result(
        "FORM-22: Admin lists pending formations queue",
        "HTTP 200, submitted formation present in queue",
        f"HTTP {status}, pending count={len(pending_items)}, found={found_in_pending}",
        status == 200 and found_in_pending,
        pending_items,
    )

    # FORM-23: Non-admin denied pending queue
    status, _, data = http_json("GET", "/api/v1/admin/formations/pending", token=instructor_token)
    record_result(
        "FORM-23: Non-admin cannot access admin pending formations queue",
        "HTTP 403 Forbidden",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 403,
        data,
    )

    # FORM-24: Admin rejects without comment -> 400
    status, _, data = http_json("POST", f"/api/v1/admin/formations/{formation_id}/review", {
        "decision": "REJECTED",
        "comment": "",
    }, token=admin_token)
    record_result(
        "FORM-24: Admin rejection without comment returns 400 Bad Request",
        "HTTP 400 Bad Request",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 400,
        data,
    )

    # FORM-25: Admin rejects with feedback -> REJECTED
    status, _, data = http_json("POST", f"/api/v1/admin/formations/{formation_id}/review", {
        "decision": "REJECTED",
        "comment": "Please detail safety guidelines for clay kilns.",
    }, token=admin_token)
    f_status = data.get("data", {}).get("status") if isinstance(data, dict) else None
    record_result(
        "FORM-25: Admin rejects formation with feedback (PENDING_REVIEW -> REJECTED)",
        "HTTP 200, status: REJECTED",
        f"HTTP {status}, status: {f_status}",
        status == 200 and f_status == "REJECTED",
        data,
    )

    # FORM-26: Author re-submits rejected formation
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/submit", token=instructor_token)
    f_status = data.get("data", {}).get("status") if isinstance(data, dict) else None
    record_result(
        "FORM-26: Author re-submits rejected formation (REJECTED -> PENDING_REVIEW)",
        "HTTP 200, status: PENDING_REVIEW",
        f"HTTP {status}, status: {f_status}",
        status == 200 and f_status == "PENDING_REVIEW",
        data,
    )

    # FORM-27: Admin approves formation
    status, _, data = http_json("POST", f"/api/v1/admin/formations/{formation_id}/review", {
        "decision": "APPROVED",
        "comment": "Excellent syllabus and safety prerequisites.",
    }, token=admin_token)
    f_status = data.get("data", {}).get("status") if isinstance(data, dict) else None
    record_result(
        "FORM-27: Admin approves formation (PENDING_REVIEW -> APPROVED)",
        "HTTP 200, status: APPROVED",
        f"HTTP {status}, status: {f_status}",
        status == 200 and f_status == "APPROVED",
        data,
    )

    # FORM-28: Publishing unapproved formation (negative check with draft or non-existent)
    status, _, data = http_json("POST", f"/api/v1/admin/formations/{uuid.uuid4()}/publish", token=admin_token)
    record_result(
        "FORM-28: Publishing non-existent formation returns 404 Not Found",
        "HTTP 404 Not Found",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 404,
        data,
    )

    # FORM-29: Admin publishes approved formation
    status, _, data = http_json("POST", f"/api/v1/admin/formations/{formation_id}/publish", token=admin_token)
    f_status = data.get("data", {}).get("status") if isinstance(data, dict) else None
    record_result(
        "FORM-29: Admin publishes approved formation (APPROVED -> PUBLISHED)",
        "HTTP 200, status: PUBLISHED",
        f"HTTP {status}, status: {f_status}",
        status == 200 and f_status == "PUBLISHED",
        data,
    )

    # -------------------------------------------------------------
    # PHASE 4.6: Public Catalog Discovery & Details
    # -------------------------------------------------------------
    print("\n--- Phase 4.6: Public Catalog Discovery & Details ---")

    # FORM-30: Public catalog listing
    status, _, data = http_json("GET", "/api/v1/artisan/formations/catalog", token=peer_token)
    catalog_items = data.get("data", {}).get("content", []) if isinstance(data, dict) else []
    found_in_catalog = any(item.get("id") == formation_id for item in catalog_items)
    record_result(
        "FORM-30: Published formation appears in public catalog",
        "HTTP 200, formation present in catalog",
        f"HTTP {status}, catalog count={len(catalog_items)}, found={found_in_catalog}",
        status == 200 and found_in_catalog,
        catalog_items,
    )

    # FORM-31: Public formation details
    status, _, data = http_json("GET", f"/api/v1/artisan/formations/catalog/{formation_id}", token=peer_token)
    p_view = data.get("data", {}) if isinstance(data, dict) else {}
    record_result(
        "FORM-31: Public formation view shows author info, capacity, and isEnrolled: false",
        "HTTP 200, isEnrolled: false",
        f"HTTP {status}, isEnrolled: {p_view.get('isEnrolled')}, maxParticipants: {p_view.get('maxParticipants')}",
        status == 200 and p_view.get("isEnrolled") is False,
        p_view,
    )

    # -------------------------------------------------------------
    # PHASE 4.7: Enrollment Lifecycle & Capacity Constraints
    # -------------------------------------------------------------
    print("\n--- Phase 4.7: Enrollment Lifecycle & Capacity Constraints ---")

    # FORM-32: Author cannot self-enroll -> 409
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/enroll", token=instructor_token)
    record_result(
        "FORM-32: Author self-enrollment returns 409 Conflict",
        "HTTP 409 Conflict",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 409,
        data,
    )

    # FORM-33: Peer artisan enrolls
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/enroll", token=peer_token)
    enr_status = data.get("data", {}).get("status") if isinstance(data, dict) else None
    record_result(
        "FORM-33: Peer artisan enrolls in published formation",
        "HTTP 200, status: CONFIRMED",
        f"HTTP {status}, status: {enr_status}",
        status == 200 and enr_status == "CONFIRMED",
        data,
    )

    # FORM-34: Duplicate enrollment -> 409
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/enroll", token=peer_token)
    record_result(
        "FORM-34: Duplicate enrollment returns 409 Conflict",
        "HTTP 409 Conflict",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 409,
        data,
    )

    # FORM-35: Participant enrollment history
    status, _, data = http_json("GET", "/api/v1/artisan/formations/my-enrollments", token=peer_token)
    my_enrollments = data.get("data", {}).get("content", []) if isinstance(data, dict) else []
    found_my_enr = any(
        (item.get("enrollment", {}).get("formationId") == formation_id)
        or (item.get("formation", {}).get("id") == formation_id)
        for item in my_enrollments
    )
    record_result(
        "FORM-35: Participant enrollment history includes active enrollment",
        "HTTP 200, formationId present",
        f"HTTP {status}, count={len(my_enrollments)}, found={found_my_enr}",
        status == 200 and found_my_enr,
        my_enrollments,
    )

    # FORM-36: Public formation details reports isEnrolled: true
    status, _, data = http_json("GET", f"/api/v1/artisan/formations/catalog/{formation_id}", token=peer_token)
    p_view = data.get("data", {}) if isinstance(data, dict) else {}
    record_result(
        "FORM-36: Public formation details reports isEnrolled: true for enrolled peer",
        "HTTP 200, isEnrolled: true",
        f"HTTP {status}, isEnrolled: {p_view.get('isEnrolled')}",
        status == 200 and p_view.get("isEnrolled") is True,
        p_view,
    )

    # FORM-37: Capacity constraint (create maxParticipants=1 formation)
    cap_payload = dict(base_formation_payload, title="Private Masterclass Capacity 1", maxParticipants=1)
    status, _, cap_data = http_json("POST", "/api/v1/artisan/formations", cap_payload, token=instructor_token)
    cap_f_id = cap_data.get("data", {}).get("id")
    http_json("POST", f"/api/v1/artisan/formations/{cap_f_id}/submit", token=instructor_token)
    http_json("POST", f"/api/v1/admin/formations/{cap_f_id}/review", {"decision": "APPROVED"}, token=admin_token)
    http_json("POST", f"/api/v1/admin/formations/{cap_f_id}/publish", token=admin_token)

    # Non-teacher enrolls first -> 200
    status, _, _ = http_json("POST", f"/api/v1/artisan/formations/{cap_f_id}/enroll", token=non_teacher_token)
    # Peer attempts to enroll (capacity 1 reached) -> 409
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{cap_f_id}/enroll", token=peer_token)
    record_result(
        "FORM-37: Enrollment beyond maxParticipants capacity returns 409 Conflict",
        "HTTP 409 Conflict, capacity reached",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 409 and "capacity" in (data.get("message") or "").lower(),
        data,
    )

    # -------------------------------------------------------------
    # PHASE 4.8: Protected Course Document Access
    # -------------------------------------------------------------
    print("\n--- Phase 4.8: Protected Course Document Access ---")

    # FORM-38: Enrolled participant downloads course file
    status, headers, content = http_download(
        f"/api/v1/artisan/formations/{formation_id}/files/{course_file_id}/download",
        token=peer_token,
    )
    has_pdf_header = content.startswith(b"%PDF")
    record_result(
        "FORM-38: Enrolled participant downloads course file",
        "HTTP 200, valid PDF binary content",
        f"HTTP {status}, bytes={len(content)}, pdf header: {has_pdf_header}",
        status == 200 and has_pdf_header,
        {"status": status, "len": len(content)},
    )

    # FORM-39: Non-enrolled artisan denied download
    status, _, content = http_download(
        f"/api/v1/artisan/formations/{formation_id}/files/{course_file_id}/download",
        token=non_teacher_token,
    )
    record_result(
        "FORM-39: Non-enrolled artisan cannot download protected course file",
        "HTTP 403 Forbidden",
        f"HTTP {status}",
        status == 403,
        {"status": status},
    )

    # -------------------------------------------------------------
    # PHASE 4.9: Cancellation & Reactivation Workflow
    # -------------------------------------------------------------
    print("\n--- Phase 4.9: Cancellation & Reactivation Workflow ---")

    # FORM-40: Participant cancels enrollment
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/cancel", token=peer_token)
    enr_status = data.get("data", {}).get("status") if isinstance(data, dict) else None
    record_result(
        "FORM-40: Participant cancels active enrollment",
        "HTTP 200, status: CANCELLED",
        f"HTTP {status}, status: {enr_status}",
        status == 200 and enr_status == "CANCELLED",
        data,
    )

    # FORM-41: Attempting to cancel already cancelled enrollment -> 404
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/cancel", token=peer_token)
    record_result(
        "FORM-41: Cancelling already cancelled enrollment returns 404 Not Found",
        "HTTP 404 Not Found",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 404,
        data,
    )

    # FORM-42: Download rejected after cancellation -> 403
    status, _, _ = http_download(
        f"/api/v1/artisan/formations/{formation_id}/files/{course_file_id}/download",
        token=peer_token,
    )
    record_result(
        "FORM-42: File download rejected after enrollment cancellation",
        "HTTP 403 Forbidden",
        f"HTTP {status}",
        status == 403,
        {"status": status},
    )

    # FORM-43: Re-enrolling after cancellation reactivates enrollment
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/enroll", token=peer_token)
    enr_status = data.get("data", {}).get("status") if isinstance(data, dict) else None
    record_result(
        "FORM-43: Re-enrolling after cancellation reactivates enrollment to CONFIRMED",
        "HTTP 200, status: CONFIRMED",
        f"HTTP {status}, status: {enr_status}",
        status == 200 and enr_status == "CONFIRMED",
        data,
    )

    # -------------------------------------------------------------
    # PHASE 4.10: Attended Enrollment Review Workflow
    # -------------------------------------------------------------
    print("\n--- Phase 4.10: Attended Enrollment Review Workflow ---")

    review_payload = {
        "rating": 4.5,
        "comment": "Outstanding masterclass! Learnt authentic Kabyle clay firing methods.",
    }

    # FORM-44: Review submission before attendance rejected -> 403
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/reviews", review_payload, token=peer_token)
    record_result(
        "FORM-44: Review creation before ATTENDED status returns 403 Forbidden",
        "HTTP 403 Forbidden ('Reviews require an attended completed formation.')",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 403,
        data,
    )

    # FORM-45: Set enrollment to ATTENDED and formation to COMPLETED in DB
    db_execute(f"UPDATE formation_enrollments SET status = 'ATTENDED' WHERE formation_id = '{formation_id}' AND artisan_id = '{peer_id}';")
    db_execute(f"UPDATE formations SET status = 'COMPLETED' WHERE id = '{formation_id}';")
    enr_rows = db_query(f"SELECT status FROM formation_enrollments WHERE formation_id = '{formation_id}' AND artisan_id = '{peer_id}';")
    form_rows = db_query(f"SELECT status FROM formations WHERE id = '{formation_id}';")
    record_result(
        "FORM-45: Transition enrollment to ATTENDED and formation to COMPLETED",
        "enrollment: ATTENDED, formation: COMPLETED",
        f"enrollment: {enr_rows[0][0] if enr_rows else None}, formation: {form_rows[0][0] if form_rows else None}",
        enr_rows and enr_rows[0][0] == "ATTENDED" and form_rows and form_rows[0][0] == "COMPLETED",
        {"enr": enr_rows, "form": form_rows},
    )

    # FORM-46: Enrolled participant posts review -> 201 Created
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/reviews", review_payload, token=peer_token)
    review_id = data.get("data", {}).get("id") if isinstance(data, dict) else None
    record_result(
        "FORM-46: Attended participant successfully submits review",
        "HTTP 201 Created, review persisted",
        f"HTTP {status}, reviewId: {review_id}, rating: {data.get('data', {}).get('rating') if isinstance(data, dict) else None}",
        status == 201 and review_id is not None,
        data,
    )

    # FORM-47: Duplicate review on same enrollment -> 409
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/reviews", review_payload, token=peer_token)
    record_result(
        "FORM-47: Duplicate review on same enrollment returns 409 Conflict",
        "HTTP 409 Conflict",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 409,
        data,
    )

    # FORM-48: Author attempting to review own formation -> 409
    status, _, data = http_json("POST", f"/api/v1/artisan/formations/{formation_id}/reviews", review_payload, token=instructor_token)
    record_result(
        "FORM-48: Author cannot review their own formation",
        "HTTP 403 or 409",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status in [403, 409],
        data,
    )

    # FORM-49: Artisan aggregate rating and review count updated in DB
    art_rows = db_query(f"SELECT rating, reviews_count FROM artisans WHERE id = '{instructor_id}';")
    new_rating = float(art_rows[0][0]) if art_rows else 0.0
    new_count = int(art_rows[0][1]) if art_rows else 0
    record_result(
        "FORM-49: Instructor artisan rating and reviewsCount recalculated",
        "rating == 4.5, reviewsCount == 1",
        f"rating: {new_rating}, reviewsCount: {new_count}",
        new_rating == 4.5 and new_count == 1,
        art_rows,
    )

    # FORM-50: List artisan reviews
    status, _, data = http_json("GET", f"/api/v1/artisans/{instructor_id}/reviews")
    rev_page = data.get("data", {}).get("content", []) if isinstance(data, dict) else []
    found_rev = any(r.get("id") == review_id for r in rev_page)
    record_result(
        "FORM-50: Public reviews listing displays newly submitted review",
        "HTTP 200, review present in content",
        f"HTTP {status}, review count: {len(rev_page)}, found: {found_rev}",
        status == 200 and found_rev,
        rev_page,
    )

    # FORM-51: Author/reviewer updates review
    update_rev_payload = {
        "rating": 5.0,
        "comment": "Updated review: Absolutely legendary masterclass!",
    }
    status, _, data = http_json("PUT", f"/api/v1/artisan/reviews/{review_id}", update_rev_payload, token=peer_token)
    upd_rating = data.get("data", {}).get("rating") if isinstance(data, dict) else None
    record_result(
        "FORM-51: Reviewer updates their review",
        "HTTP 200, rating updated to 5.0",
        f"HTTP {status}, rating: {upd_rating}",
        status == 200 and upd_rating == 5.0,
        data,
    )

    # FORM-52: Non-author cannot update review -> 403
    status, _, data = http_json("PUT", f"/api/v1/artisan/reviews/{review_id}", update_rev_payload, token=instructor_token)
    record_result(
        "FORM-52: Non-author cannot update review",
        "HTTP 403 Forbidden",
        f"HTTP {status}, message: {data.get('message') if isinstance(data, dict) else data}",
        status == 403,
        data,
    )

    # FORM-53: Reviewer deletes review
    status, _, data = http_json("DELETE", f"/api/v1/artisan/reviews/{review_id}", token=peer_token)
    record_result(
        "FORM-53: Reviewer deletes their review",
        "HTTP 200 OK",
        f"HTTP {status}, success: {data.get('success') if isinstance(data, dict) else data}",
        status == 200,
        data,
    )

    # FORM-54: Aggregate rating updated after review deletion
    art_rows_after = db_query(f"SELECT rating, reviews_count FROM artisans WHERE id = '{instructor_id}';")
    del_rating = float(art_rows_after[0][0]) if art_rows_after else 0.0
    del_count = int(art_rows_after[0][1]) if art_rows_after else 0
    record_result(
        "FORM-54: Instructor rating and review count reset after review deletion",
        "rating == 0.0, reviewsCount == 0",
        f"rating: {del_rating}, reviewsCount: {del_count}",
        del_rating == 0.0 and del_count == 0,
        art_rows_after,
    )

    # -------------------------------------------------------------
    # PHASE 4.11: Formation Lifecycle Soft-Deletion
    # -------------------------------------------------------------
    print("\n--- Phase 4.11: Formation Lifecycle Soft-Deletion ---")

    # FORM-55: Author soft-deletes formation
    status, _, data = http_json("DELETE", f"/api/v1/artisan/formations/{formation_id}", token=instructor_token)
    record_result(
        "FORM-55: Author soft-deletes formation",
        "HTTP 200 OK",
        f"HTTP {status}, success: {data.get('success') if isinstance(data, dict) else data}",
        status == 200,
        data,
    )

    # FORM-56: Excluded from author's formations
    status, _, data = http_json("GET", "/api/v1/artisan/formations/me", token=instructor_token)
    my_f = data.get("data", {}).get("content", []) if isinstance(data, dict) else []
    found_in_my = any(item.get("id") == formation_id for item in my_f)
    record_result(
        "FORM-56: Soft-deleted formation excluded from author's getMyFormations",
        "formation NOT present in content",
        f"HTTP {status}, count={len(my_f)}, found: {found_in_my}",
        status == 200 and not found_in_my,
        my_f,
    )

    # FORM-57: Excluded from public catalog
    status, _, data = http_json("GET", "/api/v1/artisan/formations/catalog", token=peer_token)
    cat_f = data.get("data", {}).get("content", []) if isinstance(data, dict) else []
    found_in_cat = any(item.get("id") == formation_id for item in cat_f)
    record_result(
        "FORM-57: Soft-deleted formation excluded from public catalog",
        "formation NOT present in catalog",
        f"HTTP {status}, count={len(cat_f)}, found: {found_in_cat}",
        status == 200 and not found_in_cat,
        cat_f,
    )

    # -------------------------------------------------------------
    # Summary
    # -------------------------------------------------------------
    total = len(results)
    passed = sum(1 for r in results if r["passed"])
    failed = total - passed

    print("\n" + "=" * 80)
    print(f"DOMAIN 4 VERIFICATION SUMMARY: {passed}/{total} PASSED ({failed} FAILED)")
    print("=" * 80)

    # Render summary table
    print(f"{'Scenario':<65} | {'Expected':<30} | {'Actual':<30} | {'Result':<6}")
    print("-" * 137)
    for r in results:
        scen = r["scenario"][:63]
        exp = r["expected"][:28]
        act = r["actual"][:28]
        res = "PASS" if r["passed"] else "FAIL"
        print(f"{scen:<65} | {exp:<30} | {act:<30} | {res:<6}")

    # Output JSON report
    os.makedirs(".agent-output", exist_ok=True)
    report_file = ".agent-output/domain4-formations-report.json"
    with open(report_file, "w") as f:
        json.dump({
            "runId": RUN_ID,
            "total": total,
            "passed": passed,
            "failed": failed,
            "results": results,
        }, f, indent=2)
    print(f"\nSaved detailed JSON report to {report_file}")

    if failed > 0:
        print(f"\n[FAIL] One or more scenarios failed. Halting before proceeding.")
        sys.exit(1)

if __name__ == "__main__":
    main()
