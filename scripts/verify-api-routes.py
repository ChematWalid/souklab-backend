#!/usr/bin/env python3
"""Secret-safe semantic/boundary sweep for every published OpenAPI operation.

The sweep uses placeholder resources and records statuses, not response bodies.
Business-data workflows are exercised by focused live and integration checks.
"""

from __future__ import annotations

import json
import os
import re
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any


BASE = os.getenv("APP_BASE_URL", "http://localhost:8080").rstrip("/")
REPORT = Path(os.getenv("API_ROUTE_REPORT", ".agent-output/api-route-sweep.tsv"))
JSON_METHODS = {"POST", "PUT", "PATCH"}
METHODS = {"GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"}


def token(name: str) -> str:
    return os.getenv(name, "")


def concrete_path(path: str) -> str:
    return re.sub(r"\{[^}]+\}", "00000000-0000-0000-0000-000000000000", path)


def query_for(operation: dict[str, Any]) -> str:
    values: list[tuple[str, str]] = []
    for parameter in operation.get("parameters", []):
        if parameter.get("in") != "query":
            continue
        name = parameter.get("name", "")
        schema = parameter.get("schema", {})
        if name.lower() in {"page", "offset"}:
            value = "0"
        elif name.lower() in {"size", "limit"}:
            value = "1"
        elif schema.get("type") == "boolean":
            value = "false"
        elif schema.get("type") in {"integer", "number"}:
            value = "1"
        else:
            value = "verification-placeholder"
        values.append((name, value))
    return urllib.parse.urlencode(values)


def body_for(operation: dict[str, Any], malformed: bool) -> tuple[bytes | None, str | None, str]:
    if operation.get("requestBody") is None:
        return None, None, "<none>"
    content = operation.get("requestBody", {}).get("content", {})
    if "multipart/form-data" in content:
        boundary = "----souklab-verification"
        if malformed:
            return b"{", "application/json", "<malformed-json>"
        return (f"--{boundary}--\r\n").encode(), f"multipart/form-data; boundary={boundary}", "<empty-multipart>"
    if malformed:
        return b"{", "application/json", "<malformed-json>"
    return b"{}", "application/json", "{}"


def roles_for() -> list[tuple[str, str, str]]:
    candidates = [("admin", "ADMIN", "SOUKLAB_ADMIN_TOKEN"),
                  ("client", "CLIENT", "SOUKLAB_CLIENT_TOKEN"),
                  ("artisan", "ARTISAN", "SOUKLAB_ARTISAN_TOKEN"),
                  ("second-client", "CLIENT", "SOUKLAB_SECOND_CLIENT_TOKEN"),
                  ("second-artisan", "ARTISAN", "SOUKLAB_SECOND_ARTISAN_TOKEN")]
    result = [(actor, role, token(variable)) for actor, role, variable in candidates
              if token(variable)]
    if not result and token("SOUKLAB_ACCESS_TOKEN"):
        result.append(("primary-synthetic", "UNKNOWN", token("SOUKLAB_ACCESS_TOKEN")))
    return result


def request(base: str, method: str, path: str, access_token: str,
            payload: bytes | None, content_type: str | None,
            idempotency: bool) -> tuple[str, int, int, str]:
    headers = {"Accept": "application/json", "X-Requested-With": "verification"}
    if access_token:
        headers["Authorization"] = "Bearer " + access_token
    if payload is not None:
        headers["Content-Type"] = content_type or "application/json"
    if idempotency:
        headers["Idempotency-Key"] = "verification-idempotency-key"
    req = urllib.request.Request(base + path, data=payload, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as response:
            body = response.read()
            return "PASS", response.status, len(body), response.headers.get_content_type()
    except urllib.error.HTTPError as exc:
        body = exc.read()
        return ("BOUNDARY" if exc.code < 500 else "FAIL_5XX", exc.code, len(body),
                exc.headers.get_content_type())
    except (urllib.error.URLError, TimeoutError, OSError) as exc:
        return "TRANSPORT_FAILURE", 0, 0, type(exc).__name__


def main() -> int:
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    spec_headers = {}
    if token("SOUKLAB_ACCESS_TOKEN"):
        spec_headers["Authorization"] = "Bearer " + token("SOUKLAB_ACCESS_TOKEN")
    with urllib.request.urlopen(urllib.request.Request(BASE + "/v3/api-docs", headers=spec_headers), timeout=20) as response:
        spec = json.load(response)
    operations = [(method.upper(), path, item[method])
                  for path, item in spec.get("paths", {}).items()
                  for method in item if method.upper() in METHODS]
    roles = roles_for()
    rows: list[dict[str, Any]] = []
    failures = 0

    def is_wrong_role(path: str, role: str) -> bool:
        if "/admin/" in path:
            return role != "ADMIN"
        if "/artisan/" in path or "formateur" in path:
            return role == "CLIENT"
        if "/client/" in path:
            return role == "ARTISAN"
        return False

    def expected_for(case: str) -> str:
        return {
            "unauthenticated": "public 2xx or 401/403",
            "authenticated": "no 5xx/transport",
            "wrong-role": "403/404 or no 5xx/transport",
            "ownership-boundary": "403/404 or no 5xx/transport",
            "malformed-body": "400/401/403/422",
            "idempotency-replay-1": "2xx/4xx, no 5xx",
            "idempotency-replay-2": "same boundary, no 5xx",
        }[case]

    def add(method: str, path: str, operation: dict[str, Any], case: str,
            actor: str, role: str, supplied_token: str, malformed: bool = False,
            idempotency: bool = False) -> None:
        nonlocal failures
        concrete = concrete_path(path)
        query = query_for(operation)
        if query:
            concrete += "?" + query
        payload, content_type, payload_label = body_for(operation, malformed)
        classification, status, length, response_type = request(
            BASE, method, concrete, supplied_token, payload, content_type, idempotency
        )
        if classification in {"FAIL_5XX", "TRANSPORT_FAILURE"}:
            failures += 1
        headers = "Accept,X-Requested-With"
        if content_type:
            headers += ",Content-Type"
        if idempotency:
            headers += ",Idempotency-Key:<synthetic>"
        rows.append({"case": case, "actor": actor, "role": role,
                     "expected": expected_for(case), "method": method,
                     "path": concrete, "headers": headers, "payload": payload_label,
                     "status": status, "response": f"{response_type};{length} bytes",
                     "classification": classification})

    for method, path, operation in sorted(operations):
        add(method, path, operation, "unauthenticated", "anonymous", "NONE", "")
        for actor, role, supplied in roles:
            case = "wrong-role" if is_wrong_role(path, role) else "authenticated"
            add(method, path, operation, case, actor, role, supplied)
        if method in JSON_METHODS:
            add(method, path, operation, "malformed-body", "anonymous", "NONE", "", True)
            if roles:
                add(method, path, operation, "idempotency-replay-1", roles[0][0], roles[0][1], roles[0][2], False, True)
                add(method, path, operation, "idempotency-replay-2", roles[0][0], roles[0][1], roles[0][2], False, True)
        if len(roles) >= 2:
            add(method, path, operation, "ownership-boundary", roles[-1][0], roles[-1][1], roles[-1][2])

    with REPORT.open("w", encoding="utf-8") as output:
        output.write("case\tactor\trole\texpected\tmethod\tpath\theaders\tpayload\tstatus\tresponse\tclassification\n")
        for row in rows:
            output.write("\t".join(str(row[key]) for key in
                                    ("case", "actor", "role", "expected", "method", "path", "headers",
                                     "payload", "status", "response", "classification")) + "\n")
    print(f"API_ROUTE_SWEEP operations={len(operations)} cases={len(rows)} failures={failures} report={REPORT}")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
