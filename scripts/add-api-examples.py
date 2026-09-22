#!/usr/bin/env python3
"""Add stable curl and TypeScript examples to each API operation section."""

from __future__ import annotations

import re
from pathlib import Path

DOC = Path("docs/API_OPENAPI.md")
OPERATION = re.compile(r"(?m)^### `([^`]+)`\n\n#### (GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS|TRACE) — ([^\n]+)\n")
MARKER = "#### HTTP example"


def metadata(operation_id: str, path: str, method: str) -> str:
    public = path.startswith("/api/v1/auth/") or path.startswith("/api/v1/public/") or path.startswith("/api/v1/catalog/") or path.endswith("/integrations/chargily/webhook")
    authorization = "Public or authentication-flow operation; no bearer token is required unless the live contract declares security." if public else "Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary."
    return (
        "#### Purpose and authorization\n\n"
        f"Purpose: `{operation_id}` performs the `{method}` operation for `{path}`.\n\n"
        f"Authorization: {authorization}\n\n"
        "Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.\n\n"
    )


def example(path: str, method: str) -> str:
    shell_path = path
    for parameter in re.findall(r"\{([^}]+)\}", shell_path):
        shell_path = shell_path.replace("{" + parameter + "}", "${" + parameter.upper() + "}")
    auth = "" if path.startswith("/api/v1/auth/") or path.endswith("/integrations/chargily/webhook") else " --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'"
    body = ""
    content_type = ""
    if method in {"POST", "PUT", "PATCH"} and not path.endswith("/webhook"):
        content_type = " --header 'Content-Type: application/json'"
        body = " --data '{}'"
    curl = f"curl --fail-with-body --request {method} \"${{SOUKLAB_BASE_URL:-http://localhost:8080}}{shell_path}\"{auth}{content_type}{body}"
    ts_headers = "{ Authorization: `Bearer ${accessToken}` }" if auth else "{}"
    ts_body = ", body: JSON.stringify({})" if body else ""
    ts = f"const response = await fetch(`${{baseUrl}}{path}`, {{ method: \"{method}\", headers: {ts_headers}{ts_body} }});"
    return (
        f"{MARKER}\n\n"
        "The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.\n\n"
        "```bash\n"
        f"{curl}\n"
        "```\n\n"
        "```ts\n"
        "const baseUrl = import.meta.env.VITE_API_BASE_URL ?? \"http://localhost:8080\";\n"
        "const accessToken = \"<access-token>\";\n"
        f"{ts}\n"
        "const payload = await response.json();\n"
        "```\n\n"
    )


def main() -> None:
    text = DOC.read_text(encoding="utf-8")
    matches = list(OPERATION.finditer(text))
    for match in reversed(matches):
        start = match.end()
        next_heading = text.find("\n### ", start)
        end = len(text) if next_heading < 0 else next_heading + 1
        section = text[start:end]
        if "#### Purpose and authorization" not in section:
            text = text[:start] + metadata(match.group(2), match.group(1), match.group(2)) + text[start:]
        if MARKER not in section:
            text = text[:end] + example(match.group(1), match.group(2)) + text[end:]
    DOC.write_text(text, encoding="utf-8")
    print(f"Added examples to {len(matches)} operations")


if __name__ == "__main__":
    main()
