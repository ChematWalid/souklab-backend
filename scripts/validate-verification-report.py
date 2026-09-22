#!/usr/bin/env python3
"""Validate the secret-safe Markdown artifact emitted by full-verification.sh."""

from __future__ import annotations

import re
import sys
from pathlib import Path


EXPECTED = [
    "Seq",
    "Prerequisite",
    "Actor/email",
    "Role/status/permissions",
    "Method/path/query",
    "Headers",
    "Payload",
    "Expected",
    "Actual/response",
    "DB/audit assertion",
    "Defect/fix/replay",
    "Result",
]

FORBIDDEN = (
    re.compile(r"\beyJ[a-zA-Z0-9_-]{10,}"),
    re.compile(r"\b(?:sk|pk)_(?:live|test)_[a-zA-Z0-9_-]{8,}"),
    re.compile(r"\b(?:access|refresh)[_-]?token\s*[:=]\s*[A-Za-z0-9._~-]{20,}", re.I),
    re.compile(r"\b(?:x-authorization|authorization)\s*:\s*bearer\s+[A-Za-z0-9._~-]{20,}", re.I),
    re.compile(r"\bsha256=[0-9a-f]{32,}\b", re.I),
)


def cells(line: str) -> list[str]:
    if not line.startswith("|") or not line.rstrip().endswith("|"):
        return []
    return [part.strip() for part in line.strip().strip("|").split("|")]


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: validate-verification-report.py REPORT.md", file=sys.stderr)
        return 2
    report = Path(sys.argv[1])
    text = report.read_text(encoding="utf-8")
    errors: list[str] = []
    lines = text.splitlines()
    headers = next((cells(line) for line in lines if cells(line) and cells(line)[0] == "Seq"), None)
    if headers != EXPECTED:
        errors.append(f"unexpected table header: {headers!r}")
    rows = [cells(line) for line in lines if cells(line) and cells(line)[0].isdigit()]
    if not rows:
        errors.append("report contains no verification rows")
    for index, row in enumerate(rows, 1):
        if len(row) != len(EXPECTED):
            errors.append(f"row {index} has {len(row)} cells, expected {len(EXPECTED)}")
        if row and row[-1] not in {"PASS", "FAIL", "SKIP"}:
            errors.append(f"row {index} has invalid result {row[-1]!r}")
    for pattern in FORBIDDEN:
        if pattern.search(text):
            errors.append(f"forbidden secret-like value matched {pattern.pattern!r}")
    if "## Summary" not in text or "- Passed:" not in text or "- Failed:" not in text:
        errors.append("summary section is incomplete")
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1
    print(f"verification report valid: {report} ({len(rows)} rows; secret-safe)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
