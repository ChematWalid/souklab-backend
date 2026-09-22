#!/usr/bin/env python3
"""Fail when the hand-authored API guide and the live OpenAPI contract diverge.

Only method/path pairs are compared here. Schemas and prose remain useful
human-authored documentation, while this check prevents a route silently
disappearing from the guide.
"""

from __future__ import annotations

import json
import os
import re
import sys
import urllib.request
from pathlib import Path

METHODS = {"get", "post", "put", "patch", "delete", "head", "options", "trace"}
DOC_PATH = re.compile(r"^### `([^`]+)`$")
DOC_METHOD = re.compile(r"^#### (GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS|TRACE) — ")


def load_spec(source: str, token: str) -> dict:
    if source.startswith(("http://", "https://")):
        request = urllib.request.Request(source)
        if token:
            request.add_header("Authorization", "Bearer " + token)
        with urllib.request.urlopen(request, timeout=20) as response:
            return json.load(response)
    return json.loads(Path(source).read_text(encoding="utf-8"))


def spec_operations(spec: dict) -> set[tuple[str, str]]:
    return {
        (method.upper(), path)
        for path, item in spec.get("paths", {}).items()
        for method in item
        if method.lower() in METHODS
    }


def documented_operations(path: Path) -> set[tuple[str, str]]:
    result: set[tuple[str, str]] = set()
    current_path: str | None = None
    for line in path.read_text(encoding="utf-8").splitlines():
        path_match = DOC_PATH.match(line)
        if path_match:
            current_path = path_match.group(1)
            continue
        method_match = DOC_METHOD.match(line)
        if method_match and current_path:
            result.add((method_match.group(1), current_path))
    return result


def main() -> int:
    source = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080/v3/api-docs"
    docs = Path(sys.argv[2]) if len(sys.argv) > 2 else Path("docs/API_OPENAPI.md")
    spec = spec_operations(load_spec(source, os.environ.get("SOUKLAB_ACCESS_TOKEN", "")))
    documented = documented_operations(docs)
    missing = sorted(spec - documented)
    stale = sorted(documented - spec)
    if missing or stale:
        if missing:
            print("Undocumented OpenAPI operations:", file=sys.stderr)
            for method, path in missing:
                print(f"  {method} {path}", file=sys.stderr)
        if stale:
            print("Stale documented operations:", file=sys.stderr)
            for method, path in stale:
                print(f"  {method} {path}", file=sys.stderr)
        return 1
    print(f"API documentation is synchronized: {len(spec)} operations")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
