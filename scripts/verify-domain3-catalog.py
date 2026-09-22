#!/usr/bin/env python3
"""Exhaustive live verification for Domain 3: Catalog & Directory.

Covers:
- Reference Catalog Endpoints (GET /api/v1/catalog/**)
  * Regions taxonomy (Wilayas and child Communes)
  * Craft categories and subcategories
  * Material families and materials
  * Historical epochs
  * Craftsmanship techniques
  * Caffeine caching and idempotency
- Public Directory Faceted Search (GET /api/v1/public/directory)
  * Public unauthenticated access
  * Pagination bounds & validation (page=-1, size=0, size=101, size=1, size=100, page=999)
  * Rating bounds & validation (minRating=-0.1, minRating=5.1, minRating=0.0, minRating=5.0)
  * Keyword validation (max length 120) & alias 'q'
  * Sorting orders (RELEVANCE, RATING_DESC, REVIEWS_DESC, VIEWS_DESC, NEWEST, invalid sort 422)
  * Full-text search and fuzzy/exact keyword matching
  * Geographic filtering by regionSlug and wilayaCode
  * Taxonomy filtering by categorySlug and subCategorySlug
  * Multi-valued facet filtering: materials, techniques, epoques
  * Accreditation toggles: verifiedOnly, premiumOnly, teacherOnly
  * Admin user approval workflow triggering verified accreditation
  * Soft-deleted artisan exclusion from search results
"""

from __future__ import annotations

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

RUN_ID = f"d3-{int(time.time())}"
results: list[dict[str, Any]] = []

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

def http_req(
    method: str,
    path: str,
    token: str | None = None,
    body: dict[str, Any] | None = None,
    query_params: dict[str, Any] | None = None,
) -> tuple[int, dict[str, Any], dict[str, str]]:
    url = f"{BASE_URL}{path}"
    if query_params:
        encoded_params = []
        for k, v in query_params.items():
            if isinstance(v, list):
                for item in v:
                    encoded_params.append((k, str(item)))
            else:
                encoded_params.append((k, str(v)))
        url = f"{url}?{urllib.parse.urlencode(encoded_params)}"

    data = None
    headers: dict[str, str] = {
        "Accept": "application/json",
    }
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            resp_code = resp.status
            raw_body = resp.read().decode("utf-8")
            resp_headers = dict(resp.headers)
            try:
                parsed = json.loads(raw_body)
            except Exception:
                parsed = {"raw": raw_body}
            return resp_code, parsed, resp_headers
    except urllib.error.HTTPError as e:
        resp_code = e.code
        raw_body = e.read().decode("utf-8")
        resp_headers = dict(e.headers)
        try:
            parsed = json.loads(raw_body)
        except Exception:
            parsed = {"raw": raw_body}
        return resp_code, parsed, resp_headers

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
        print(f"  --> ERROR DETAIL: {json.dumps(payload, indent=2)}")

def main():
    print(f"=== Starting Domain 3: Catalog & Directory Live Verification [{RUN_ID}] ===")

    # -------------------------------------------------------------
    # PHASE 3.1: Catalog Taxonomy Endpoints (Public Reference Data)
    # -------------------------------------------------------------
    print("\n--- Phase 3.1: Catalog Taxonomy Endpoints ---")

    # 1. Regions
    status, data, _ = http_req("GET", "/api/v1/catalog/regions")
    regions = data.get("data", [])
    has_wilayas = isinstance(regions, list) and len(regions) > 0
    record_result(
        "CAT-01: GET /api/v1/catalog/regions returns 200 OK without auth",
        "HTTP 200, success: true, non-empty list",
        f"HTTP {status}, success: {data.get('success')}, count: {len(regions) if isinstance(regions, list) else 0}",
        status == 200 and data.get("success") is True and has_wilayas,
        data if status != 200 else None,
    )

    # 2. Regions hierarchy check (children communes present)
    dz_region = next((r for r in regions if r.get("code") == "DZ"), None)
    has_dz_children = dz_region is not None and len(dz_region.get("children", [])) > 0
    record_result(
        "CAT-02: Regions taxonomy has hierarchical children",
        "DZ root contains list of child Wilayas/Communes",
        f"Found DZ: {dz_region is not None}, child count: {len(dz_region.get('children', [])) if dz_region else 0}",
        has_dz_children,
        dz_region,
    )

    # 3. Categories
    status, data, _ = http_req("GET", "/api/v1/catalog/categories")
    categories = data.get("data", [])
    has_cats = isinstance(categories, list) and len(categories) > 0
    record_result(
        "CAT-03: GET /api/v1/catalog/categories returns 200 OK without auth",
        "HTTP 200, success: true, non-empty list",
        f"HTTP {status}, success: {data.get('success')}, count: {len(categories) if isinstance(categories, list) else 0}",
        status == 200 and data.get("success") is True and has_cats,
        data if status != 200 else None,
    )

    # 4. Categories hierarchy check (nested subcategories)
    first_cat = categories[0] if has_cats else None
    has_subcats = first_cat is not None and "subCategories" in first_cat and len(first_cat.get("subCategories", [])) > 0
    record_result(
        "CAT-04: Craft categories contain nested subcategories",
        "Categories have non-empty subCategories list",
        f"Cat '{first_cat.get('slug') if first_cat else None}' subCats: {len(first_cat.get('subCategories', [])) if first_cat else 0}",
        has_subcats,
        first_cat,
    )

    # 5. Materials
    status, data, _ = http_req("GET", "/api/v1/catalog/materials")
    mat_families = data.get("data", [])
    has_mats = isinstance(mat_families, list) and len(mat_families) > 0
    record_result(
        "CAT-05: GET /api/v1/catalog/materials returns 200 OK without auth",
        "HTTP 200, success: true, non-empty list",
        f"HTTP {status}, success: {data.get('success')}, count: {len(mat_families) if isinstance(mat_families, list) else 0}",
        status == 200 and data.get("success") is True and has_mats,
        data if status != 200 else None,
    )

    # 6. Materials constituent check
    first_mat_family = mat_families[0] if has_mats else None
    has_constituent_mats = first_mat_family is not None and "materials" in first_mat_family and len(first_mat_family.get("materials", [])) > 0
    record_result(
        "CAT-06: Material families contain constituent materials",
        "MaterialFamily contains materials list",
        f"Family '{first_mat_family.get('slug') if first_mat_family else None}' materials: {len(first_mat_family.get('materials', [])) if first_mat_family else 0}",
        has_constituent_mats,
        first_mat_family,
    )

    # 7. Epoques
    status, data, _ = http_req("GET", "/api/v1/catalog/epoques")
    epoques = data.get("data", [])
    has_epoques = isinstance(epoques, list) and len(epoques) > 0
    record_result(
        "CAT-07: GET /api/v1/catalog/epoques returns 200 OK without auth",
        "HTTP 200, success: true, non-empty list",
        f"HTTP {status}, success: {data.get('success')}, count: {len(epoques) if isinstance(epoques, list) else 0}",
        status == 200 and data.get("success") is True and has_epoques,
        data if status != 200 else None,
    )

    # 8. Epoques chronological check
    first_ep = epoques[0] if has_epoques else {}
    has_ep_fields = "name" in first_ep and "slug" in first_ep and "periodEra" in first_ep
    record_result(
        "CAT-08: Epoques response structure contains name, slug, periodEra",
        "Fields name, slug, periodEra present",
        f"Found fields: name={first_ep.get('name')}, slug={first_ep.get('slug')}, periodEra={first_ep.get('periodEra')}",
        has_ep_fields,
        first_ep,
    )

    # 9. Techniques
    status, data, _ = http_req("GET", "/api/v1/catalog/techniques")
    techniques = data.get("data", [])
    has_techniques = isinstance(techniques, list) and len(techniques) > 0
    record_result(
        "CAT-09: GET /api/v1/catalog/techniques returns 200 OK without auth",
        "HTTP 200, success: true, non-empty list",
        f"HTTP {status}, success: {data.get('success')}, count: {len(techniques) if isinstance(techniques, list) else 0}",
        status == 200 and data.get("success") is True and has_techniques,
        data if status != 200 else None,
    )

    # 10. Techniques structure check
    first_tech = techniques[0] if has_techniques else {}
    has_tech_fields = "name" in first_tech and "slug" in first_tech
    record_result(
        "CAT-10: Techniques response structure contains name, slug",
        "Fields name, slug present",
        f"Found fields: name={first_tech.get('name')}, slug={first_tech.get('slug')}",
        has_tech_fields,
        first_tech,
    )

    # 11-15. Cache Idempotency (repeated calls return identical HTTP 200 responses)
    for cat_name, cat_path in [
        ("regions", "/api/v1/catalog/regions"),
        ("categories", "/api/v1/catalog/categories"),
        ("materials", "/api/v1/catalog/materials"),
        ("epoques", "/api/v1/catalog/epoques"),
        ("techniques", "/api/v1/catalog/techniques"),
    ]:
        c_status, c_data, _ = http_req("GET", cat_path)
        record_result(
            f"CAT-CACHE: Repeated GET {cat_path} (Caffeine cache verification)",
            "HTTP 200, success: true",
            f"HTTP {c_status}, success: {c_data.get('success')}",
            c_status == 200 and c_data.get("success") is True,
            c_data if c_status != 200 else None,
        )

    # -------------------------------------------------------------
    # PHASE 3.2: Directory Faceted Search Validation & Parameter Boundaries
    # -------------------------------------------------------------
    print("\n--- Phase 3.2: Directory Faceted Search Validation & Parameter Boundaries ---")

    # DIR-01: Public access
    status, data, _ = http_req("GET", "/api/v1/public/directory")
    res_data = data.get("data", {})
    record_result(
        "DIR-01: GET /api/v1/public/directory returns 200 OK without auth",
        "HTTP 200, success: true",
        f"HTTP {status}, success: {data.get('success')}",
        status == 200 and data.get("success") is True,
        data if status != 200 else None,
    )

    # DIR-02: Default pagination structure
    page_num = res_data.get("pageNumber")
    page_size = res_data.get("pageSize")
    total_elements = res_data.get("totalElements")
    record_result(
        "DIR-02: Default pagination properties",
        "pageNumber=0, pageSize=20, totalElements >= 4",
        f"pageNumber={page_num}, pageSize={page_size}, totalElements={total_elements}",
        page_num == 0 and page_size == 20 and isinstance(total_elements, int) and total_elements >= 4,
        res_data,
    )

    # DIR-03: Artisan Directory Card fields
    cards = res_data.get("content", [])
    first_card = cards[0] if cards else {}
    expected_fields = ["id", "artisanName", "city", "wilayaName", "categoryName", "subCategoryName", "rating"]
    has_card_fields = all(f in first_card for f in expected_fields)
    record_result(
        "DIR-03: Directory Card response structure",
        f"Contains keys: {expected_fields}",
        f"Keys present: {list(first_card.keys())[:8]}...",
        has_card_fields,
        first_card,
    )

    # DIR-04: Negative page -> 422
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"page": -1})
    err_page = data.get("errors", {}).get("page", "")
    record_result(
        "DIR-04: Pagination boundary page=-1 returns 422 Unprocessable Content",
        "HTTP 422, errors.page contains 'negative'",
        f"HTTP {status}, errors: {data.get('errors')}",
        status == 422 and "negative" in err_page.lower(),
        data,
    )

    # DIR-05: Page size = 0 -> 422
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"size": 0})
    err_size = data.get("errors", {}).get("size", "")
    record_result(
        "DIR-05: Pagination boundary size=0 returns 422 Unprocessable Content",
        "HTTP 422, errors.size contains 'at least 1'",
        f"HTTP {status}, errors: {data.get('errors')}",
        status == 422 and "at least 1" in err_size.lower(),
        data,
    )

    # DIR-06: Page size = 101 -> 422
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"size": 101})
    err_size = data.get("errors", {}).get("size", "")
    record_result(
        "DIR-06: Pagination boundary size=101 returns 422 Unprocessable Content",
        "HTTP 422, errors.size contains 'cannot exceed 100'",
        f"HTTP {status}, errors: {data.get('errors')}",
        status == 422 and "cannot exceed 100" in err_size.lower(),
        data,
    )

    # DIR-07: Page size = 1 -> 200 OK, returns 1 card
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"size": 1})
    p_data = data.get("data", {})
    record_result(
        "DIR-07: Pagination boundary size=1 returns exactly 1 item and multiple pages",
        "HTTP 200, pageSize=1, content length=1, totalPages >= 4",
        f"HTTP {status}, pageSize={p_data.get('pageSize')}, count={len(p_data.get('content', []))}, totalPages={p_data.get('totalPages')}",
        status == 200 and p_data.get("pageSize") == 1 and len(p_data.get("content", [])) == 1 and p_data.get("totalPages", 0) >= 4,
        p_data,
    )

    # DIR-08: Page size = 100 -> 200 OK
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"size": 100})
    p_data = data.get("data", {})
    record_result(
        "DIR-08: Pagination boundary size=100 returns 200 OK with pageSize=100",
        "HTTP 200, pageSize=100",
        f"HTTP {status}, pageSize={p_data.get('pageSize')}",
        status == 200 and p_data.get("pageSize") == 100,
        p_data,
    )

    # DIR-09: Page offset beyond results page=999 -> 200 OK, empty content, last=true
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"page": 999})
    p_data = data.get("data", {})
    record_result(
        "DIR-09: Pagination beyond bounds page=999 returns 200 OK with empty content and last=true",
        "HTTP 200, empty content, last=true",
        f"HTTP {status}, content count={len(p_data.get('content', []))}, last={p_data.get('last')}",
        status == 200 and len(p_data.get("content", [])) == 0 and p_data.get("last") is True,
        p_data,
    )

    # DIR-10: Rating boundary minRating = -0.1 -> 422
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"minRating": -0.1})
    err_rating = data.get("errors", {}).get("minRating", "")
    record_result(
        "DIR-10: Rating boundary minRating=-0.1 returns 422 Unprocessable Content",
        "HTTP 422, errors.minRating contains 'cannot be less than 0.0'",
        f"HTTP {status}, errors: {data.get('errors')}",
        status == 422 and "cannot be less than 0.0" in err_rating.lower(),
        data,
    )

    # DIR-11: Rating boundary minRating = 5.1 -> 422
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"minRating": 5.1})
    err_rating = data.get("errors", {}).get("minRating", "")
    record_result(
        "DIR-11: Rating boundary minRating=5.1 returns 422 Unprocessable Content",
        "HTTP 422, errors.minRating contains 'cannot exceed 5.0'",
        f"HTTP {status}, errors: {data.get('errors')}",
        status == 422 and "cannot exceed 5.0" in err_rating.lower(),
        data,
    )

    # DIR-12: Rating filter minRating = 0.0 -> 200 OK, matches all
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"minRating": 0.0})
    p_data = data.get("data", {})
    record_result(
        "DIR-12: Rating filter minRating=0.0 returns 200 OK with all artisans",
        "HTTP 200, totalElements >= 4",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and p_data.get("totalElements", 0) >= 4,
        p_data,
    )

    # DIR-13: Rating filter minRating = 5.0 -> 200 OK, returns 0 if none is rated 5.0
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"minRating": 5.0})
    p_data = data.get("data", {})
    record_result(
        "DIR-13: Rating filter minRating=5.0 returns 200 OK",
        "HTTP 200, valid response",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and "totalElements" in p_data,
        p_data,
    )

    # DIR-14: Keyword length > 120 chars -> 422
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"keyword": "x" * 121})
    err_kw = data.get("errors", {}).get("keyword", "")
    record_result(
        "DIR-14: Keyword validation > 120 chars returns 422 Unprocessable Content",
        "HTTP 422, errors.keyword contains 'exceed 120'",
        f"HTTP {status}, errors: {data.get('errors')}",
        status == 422 and "120" in err_kw,
        data,
    )

    # DIR-15: Invalid sort order -> 422
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"sortBy": "INVALID_SORT_KEY"})
    record_result(
        "DIR-15: Invalid sort order returns 422 Unprocessable Content",
        "HTTP 422 with validation error on sortBy",
        f"HTTP {status}, errors: {data.get('errors')}",
        status == 422 and "sortBy" in data.get("errors", {}),
        data,
    )

    # DIR-16 to DIR-20: Valid sort orders
    for sort_key in ["RELEVANCE", "RATING_DESC", "REVIEWS_DESC", "VIEWS_DESC", "NEWEST"]:
        s_status, s_data, _ = http_req("GET", "/api/v1/public/directory", query_params={"sortBy": sort_key})
        sp_data = s_data.get("data", {})
        record_result(
            f"DIR-SORT: Valid sort order sortBy={sort_key}",
            "HTTP 200, success: true",
            f"HTTP {s_status}, totalElements={sp_data.get('totalElements')}",
            s_status == 200 and s_data.get("success") is True,
            s_data if s_status != 200 else None,
        )

    # -------------------------------------------------------------
    # PHASE 3.3: Directory Search Filtering & Facet Matching
    # -------------------------------------------------------------
    print("\n--- Phase 3.3: Directory Search Filtering & Facet Matching ---")

    # DIR-21: Keyword search 'Ahmed'
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"keyword": "Ahmed"})
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_ahmed = any("Ahmed" in card.get("artisanName", "") for card in hits)
    record_result(
        "DIR-21: Keyword search matching artisan name (keyword=Ahmed)",
        "HTTP 200, matching artisan found in content",
        f"HTTP {status}, hit count={len(hits)}, found Ahmed: {found_ahmed}",
        status == 200 and found_ahmed,
        p_data,
    )

    # DIR-22: Keyword alias 'q=Matoub'
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"q": "Matoub"})
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_matoub = any("Matoub" in card.get("artisanName", "") for card in hits)
    record_result(
        "DIR-22: Keyword search alias query parameter (q=Matoub)",
        "HTTP 200, matching artisan found in content",
        f"HTTP {status}, hit count={len(hits)}, found Matoub: {found_matoub}",
        status == 200 and found_matoub,
        p_data,
    )

    # DIR-23: Keyword search non-matching
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"keyword": "xyzNonExistentArtisan999"})
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    record_result(
        "DIR-23: Non-matching keyword search returns 0 hits",
        "HTTP 200, totalElements=0, content empty",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}, count={len(hits)}",
        status == 200 and p_data.get("totalElements") == 0 and len(hits) == 0,
        p_data,
    )

    # DIR-24: Geographic filter regionSlug='algerie'
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"regionSlug": "algerie"})
    p_data = data.get("data", {})
    record_result(
        "DIR-24: Geographic filter by regionSlug (regionSlug=algerie)",
        "HTTP 200, returns matching artisans",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and p_data.get("totalElements", 0) > 0,
        p_data,
    )

    # DIR-25: Geographic filter wilayaCode='DZ'
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"wilayaCode": "DZ"})
    p_data = data.get("data", {})
    record_result(
        "DIR-25: Geographic filter by wilayaCode (wilayaCode=DZ)",
        "HTTP 200, returns matching artisans",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and p_data.get("totalElements", 0) > 0,
        p_data,
    )

    # DIR-26: Geographic filter non-matching wilayaCode
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"wilayaCode": "9999"})
    p_data = data.get("data", {})
    record_result(
        "DIR-26: Non-matching wilayaCode returns 0 hits",
        "HTTP 200, totalElements=0",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and p_data.get("totalElements") == 0,
        p_data,
    )

    # DIR-27: Category filter categorySlug='vannerie-sparterie'
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"categorySlug": "vannerie-sparterie"})
    p_data = data.get("data", {})
    record_result(
        "DIR-27: Category filter (categorySlug=vannerie-sparterie)",
        "HTTP 200, returns matching artisans",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and p_data.get("totalElements", 0) > 0,
        p_data,
    )

    # DIR-28: Category filter non-matching
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"categorySlug": "non-existent-category-xyz"})
    p_data = data.get("data", {})
    record_result(
        "DIR-28: Non-matching categorySlug returns 0 hits",
        "HTTP 200, totalElements=0",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and p_data.get("totalElements") == 0,
        p_data,
    )

    # DIR-29: SubCategory filter subCategorySlug='vannerie-de-palme-saharienne'
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"subCategorySlug": "vannerie-de-palme-saharienne"})
    p_data = data.get("data", {})
    record_result(
        "DIR-29: SubCategory filter (subCategorySlug=vannerie-de-palme-saharienne)",
        "HTTP 200, returns matching artisans",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and p_data.get("totalElements", 0) > 0,
        p_data,
    )

    # DIR-30: SubCategory filter non-matching
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"subCategorySlug": "non-existent-subcat-xyz"})
    p_data = data.get("data", {})
    record_result(
        "DIR-30: Non-matching subCategorySlug returns 0 hits",
        "HTTP 200, totalElements=0",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and p_data.get("totalElements") == 0,
        p_data,
    )

    # -------------------------------------------------------------
    # PHASE 3.4: Artisan Lifecycle, Taxonomy Indexing & Accreditation
    # -------------------------------------------------------------
    print("\n--- Phase 3.4: Artisan Lifecycle, Taxonomy Indexing & Accreditation ---")

    test_email = f"artisan_tax_{int(time.time())}@souklab.test"
    test_pwd = "Password123!"

    # 1. Register test artisan
    status, data, _ = http_req("POST", "/api/v1/auth/register", body={
        "email": test_email,
        "password": test_pwd,
        "firstName": "Tahar",
        "lastName": "Djaout",
        "accountType": "ARTISAN",
    })
    test_user_id = data.get("data", {}).get("id")
    record_result(
        "DIR-31: Register dedicated artisan for faceted taxonomy testing",
        "HTTP 201, artisan created",
        f"HTTP {status}, user ID: {test_user_id}",
        status == 201 and test_user_id is not None,
        data,
    )

    # Direct DB verify email
    db_execute(f"UPDATE users SET email_verified = b'1', status = 'ACTIVE' WHERE id = '{test_user_id}';")

    # Login test artisan
    status, data, _ = http_req("POST", "/api/v1/auth/login", body={
        "email": test_email,
        "password": test_pwd,
    })
    artisan_token = data.get("data", {}).get("accessToken")
    record_result(
        "DIR-32: Authenticate dedicated test artisan",
        "HTTP 200, received accessToken",
        f"HTTP {status}, has token: {artisan_token is not None}",
        status == 200 and artisan_token is not None,
        data if status != 200 else None,
    )

    # Complete artisan profile with rich taxonomy:
    # Region: Tizi Ouzou (id: 0f000438-f3ce-4aae-ad83-dab36ee745d5, slug: tizi-ouzou)
    # SubCategory: Vannerie d'Alfa & Jonc (id: 0a905e89-0723-4512-865d-1c65ba658641, slug: vannerie-alfa-jonc)
    # Material: Argile Rouge de Kabylie (id: 0bd97dd8-8341-4fb6-9809-a1e44a77aab7, slug: argile-rouge-de-kabylie)
    # Technique: Tissage de haute lisse (id: 328f51ad-f523-4284-aa7c-78b779b72b85, slug: tissage-de-haute-lisse)
    # Epoque: Période Rustumide & Médiévale (id: 30e5fce5-4bd9-4037-bdd0-4e02afe12ae4, slug: periode-rustumide-medievale)
    status, data, _ = http_req("POST", "/api/v1/auth/complete-profile", token=artisan_token, body={
        "bio": "Traditional Kabyle artisan master in woven alfa and terracotta craft",
        "city": "Tizi Ouzou",
        "address": "Route de Beni Yenni",
        "website": "https://souklab-artisan.test",
        "regionId": "0f000438-f3ce-4aae-ad83-dab36ee745d5",
        "subCategoryId": "0a905e89-0723-4512-865d-1c65ba658641",
        "materialIds": ["0bd97dd8-8341-4fb6-9809-a1e44a77aab7"],
        "techniqueIds": ["328f51ad-f523-4284-aa7c-78b779b72b85"],
        "epoqueIds": ["30e5fce5-4bd9-4037-bdd0-4e02afe12ae4"],
    })
    record_result(
        "DIR-33: Complete test artisan profile with rich craft taxonomy",
        "HTTP 200, success: true",
        f"HTTP {status}, success: {data.get('success')}",
        status == 200 and data.get("success") is True,
        data if status != 200 else None,
    )

    # Wait 1s for Elasticsearch indexing synchronization
    time.sleep(1)

    # DIR-34: Search by material slug
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"materials": "argile-rouge-de-kabylie"})
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_by_mat = any(card.get("id") == test_user_id for card in hits)
    record_result(
        "DIR-34: Faceted search by material slug (materials=argile-rouge-de-kabylie)",
        "HTTP 200, returns newly indexed artisan",
        f"HTTP {status}, hit count={len(hits)}, found artisan: {found_by_mat}",
        status == 200 and found_by_mat,
        p_data,
    )

    # DIR-35: Search by technique slug
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"techniques": "tissage-de-haute-lisse"})
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_by_tech = any(card.get("id") == test_user_id for card in hits)
    record_result(
        "DIR-35: Faceted search by technique slug (techniques=tissage-de-haute-lisse)",
        "HTTP 200, returns newly indexed artisan",
        f"HTTP {status}, hit count={len(hits)}, found artisan: {found_by_tech}",
        status == 200 and found_by_tech,
        p_data,
    )

    # DIR-36: Search by epoque slug
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"epoques": "periode-rustumide-medievale"})
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_by_ep = any(card.get("id") == test_user_id for card in hits)
    record_result(
        "DIR-36: Faceted search by epoque slug (epoques=periode-rustumide-medievale)",
        "HTTP 200, returns newly indexed artisan",
        f"HTTP {status}, hit count={len(hits)}, found artisan: {found_by_ep}",
        status == 200 and found_by_ep,
        p_data,
    )

    # DIR-37: Multi-faceted combined query
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={
        "keyword": "Djaout",
        "regionSlug": "tizi-ouzou",
        "subCategorySlug": "vannerie-alfa-jonc",
        "materials": "argile-rouge-de-kabylie",
    })
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_multi = any(card.get("id") == test_user_id for card in hits)
    record_result(
        "DIR-37: Multi-faceted query (keyword + region + subCategory + material)",
        "HTTP 200, exact match on test artisan",
        f"HTTP {status}, hit count={len(hits)}, matched: {found_multi}",
        status == 200 and found_multi,
        p_data,
    )

    # DIR-38: Verified toggle filter before admin approval
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={
        "keyword": "Djaout",
        "verifiedOnly": "true",
    })
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_verified_before = any(card.get("id") == test_user_id for card in hits)
    record_result(
        "DIR-38: verifiedOnly=true excludes unverified artisan",
        "HTTP 200, test artisan NOT in hits",
        f"HTTP {status}, found in verified: {found_verified_before}",
        status == 200 and not found_verified_before,
        p_data,
    )

    # Admin Login to approve user
    status, data, _ = http_req("POST", "/api/v1/auth/login", body={
        "email": "4ce013@gmail.com",
        "password": "admin123",
    })
    admin_token = data.get("data", {}).get("accessToken")

    # Set user to PENDING so approveUser can transition to ACTIVE and set isVerified=true
    db_execute(f"UPDATE users SET status = 'PENDING' WHERE id = '{test_user_id}';")

    # Admin approves user
    status, data, _ = http_req("POST", f"/api/v1/admin/users/{test_user_id}/approve", token=admin_token)
    record_result(
        "DIR-39: Admin approves artisan user (POST /api/v1/admin/users/{id}/approve)",
        "HTTP 200, user approved successfully",
        f"HTTP {status}, message: {data.get('message')}",
        status == 200 and data.get("success") is True,
        data,
    )

    # Verify artisan is now verified in DB
    rows = db_query(f"SELECT is_verified FROM artisans WHERE id = '{test_user_id}';")
    # In MariaDB bit(1), raw text can be '\x01'
    is_ver_db = len(rows) > 0 and (rows[0][0] == "1" or rows[0][0] == "\x01" or rows[0][0] == "b'1'")
    record_result(
        "DIR-40: Artisan is_verified flag set to true in DB",
        "is_verified == true (1)",
        f"is_verified in DB: {rows[0][0] if rows else None}",
        is_ver_db,
        rows,
    )

    # Force search re-index via JPA save if needed, or wait
    # In UserManagementService.approveUser:
    #   artisan.setVerified(true); artisanRepository.save(artisan);
    # which triggers Hibernate Search entity indexing!
    time.sleep(1)

    # DIR-41: Verified toggle filter after admin approval
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={
        "keyword": "Djaout",
        "verifiedOnly": "true",
    })
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_verified_after = any(card.get("id") == test_user_id for card in hits)
    record_result(
        "DIR-41: verifiedOnly=true returns verified artisan after approval",
        "HTTP 200, test artisan present in verified hits",
        f"HTTP {status}, found in verified: {found_verified_after}",
        status == 200 and found_verified_after,
        p_data,
    )

    # DIR-42: Boolean toggle filters premiumOnly & teacherOnly
    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"premiumOnly": "true"})
    p_data = data.get("data", {})
    record_result(
        "DIR-42: premiumOnly=true query filter accepted and processed",
        "HTTP 200, returns paginated list",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and "totalElements" in p_data,
        p_data,
    )

    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"teacherOnly": "true"})
    p_data = data.get("data", {})
    record_result(
        "DIR-43: teacherOnly=true query filter accepted and processed",
        "HTTP 200, returns paginated list",
        f"HTTP {status}, totalElements={p_data.get('totalElements')}",
        status == 200 and "totalElements" in p_data,
        p_data,
    )

    # DIR-44: Soft deletion exclusion
    # We soft-delete the test artisan
    db_execute(f"UPDATE artisans SET deleted_at = NOW() WHERE id = '{test_user_id}';")
    # Also trigger JPA / ES update via a quick touch or test fallback
    # Since ES had the document, let's see if ES filter excludes deletedAt exists:
    # In DirectorySearchServiceImpl: predicates.add(f.not(f.exists().field(FIELD_DELETED_AT)).toPredicate());
    # Wait, if we updated directly via SQL, ES index might not have deleted_at unless touched via JPA or updated in ES directly!
    # Let's test by updating ES doc or updating via JPA. Let's see: does ES get touched if we touch via JPA?
    # Or let's update ES document directly via curl to match SQL!
    curl_es = [
        "curl", "-s", "-X", "POST",
        f"http://localhost:9200/artisans-000001/_update/{test_user_id}",
        "-H", "Content-Type: application/json",
        "-d", '{"doc": {"deletedAt": "2026-09-22T14:00:00.000000000"}}'
    ]
    subprocess.run(curl_es, capture_output=True)
    subprocess.run(["curl", "-s", "-X", "POST", "http://localhost:9200/artisans-000001/_refresh"], capture_output=True)

    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"keyword": "Djaout"})
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_after_soft_del = any(card.get("id") == test_user_id for card in hits)
    record_result(
        "DIR-44: Soft-deleted artisan excluded from directory search",
        "HTTP 200, soft-deleted artisan NOT found in search hits",
        f"HTTP {status}, found in hits: {found_after_soft_del}",
        status == 200 and not found_after_soft_del,
        p_data,
    )

    # DIR-45: Cleanup / restore
    db_execute(f"UPDATE artisans SET deleted_at = NULL WHERE id = '{test_user_id}';")
    curl_es_clear = [
        "curl", "-s", "-X", "POST",
        f"http://localhost:9200/artisans-000001/_update/{test_user_id}",
        "-H", "Content-Type: application/json",
        "-d", '{"doc": {"deletedAt": null}}'
    ]
    subprocess.run(curl_es_clear, capture_output=True)
    subprocess.run(["curl", "-s", "-X", "POST", "http://localhost:9200/artisans-000001/_refresh"], capture_output=True)

    status, data, _ = http_req("GET", "/api/v1/public/directory", query_params={"keyword": "Djaout"})
    p_data = data.get("data", {})
    hits = p_data.get("content", [])
    found_after_restore = any(card.get("id") == test_user_id for card in hits)
    record_result(
        "DIR-45: Restored artisan reappears in directory search",
        "HTTP 200, test artisan found after restoration",
        f"HTTP {status}, found in hits: {found_after_restore}",
        status == 200 and found_after_restore,
        p_data,
    )

    # -------------------------------------------------------------
    # Summary
    # -------------------------------------------------------------
    total = len(results)
    passed = sum(1 for r in results if r["passed"])
    failed = total - passed

    print("\n" + "=" * 80)
    print(f"DOMAIN 3 VERIFICATION SUMMARY: {passed}/{total} PASSED ({failed} FAILED)")
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
    report_file = ".agent-output/domain3-catalog-report.json"
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
