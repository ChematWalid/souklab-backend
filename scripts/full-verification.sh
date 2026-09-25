#!/usr/bin/env bash
set -euo pipefail

# Deterministic, secret-safe verification orchestrator. It composes the
# repository's focused checks and records classifications only; response
# bodies, bearer tokens, passwords, provider keys, signatures, and webhook
# payloads are never copied into the report.

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
report="${FULL_VERIFY_REPORT:-.agent-output/full-verification-${timestamp}.md}"
export COMPOSE_PROJECT_NAME="${FULL_VERIFY_COMPOSE_PROJECT:-souklab-full-verification-${timestamp,,}}"
work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT
mkdir -p "$(dirname "$report")"

cat >"$report" <<EOF
# Souklab full verification

- Run: ${timestamp}
- Scope: local retained dependencies, application contract, WebSocket/STOMP,
  local provider, and optional official Test Mode smoke checks.
- Compose project: ${COMPOSE_PROJECT_NAME} (unique per run unless explicitly overridden)
- Redaction: credentials, tokens, passwords, signatures, webhook bodies, URLs
  containing secrets, and personal data are excluded.

| Seq | Prerequisite | Actor/email | Role/status/permissions | Method/path/query | Headers | Payload | Expected | Actual/response | DB/audit assertion | Defect/fix/replay | Result |
|---:|---|---|---|---|---|---|---|---|---|---|---|
EOF

sequence=0
passed=0
failed=0
skipped=0

record() {
  local prerequisite="$1" actor="$2" check="$3" expected="$4" actual="$5" assertion="$6" result="$7"
  sequence=$((sequence + 1))
  case "$result" in PASS) passed=$((passed + 1));; FAIL) failed=$((failed + 1));; SKIP) skipped=$((skipped + 1));; esac
  printf '| %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s |\n' \
    "$sequence" "$prerequisite" "$actor" "synthetic/see actor label" "$check" \
    "sanitized; no tokens/signatures" "<redacted or not applicable>" "$expected" \
    "$actual" "$assertion" "none; replay recorded by command/report" "$result" >>"$report"
}

run_check() {
  local prerequisite="$1" actor="$2" check="$3" expected="$4" assertion="$5"
  shift 5
  local output status actual
  output="$work_dir/check-${sequence}.out"
  if "$@" >"$output" 2>&1; then
    status=PASS
    actual="passed"
  else
    status=FAIL
    actual="failed (sanitized command output retained only in process logs)"
  fi
  record "$prerequisite" "$actor" "$check" "$expected" "$actual" "$assertion" "$status"
  [[ "$status" == PASS ]]
}

run_check "none" "system/static" "source hygiene and migration checks" "pass" "migration/source invariants" ./scripts/check-source-hygiene.sh || true
run_check "none" "system/static" "API contract checks" "pass" "controller/documentation invariants" ./scripts/check-api-contract.sh || true
run_check "none" "system/static" "Maven package" "pass" "compiled artifact" ./mvnw -q -DskipTests package || true

if [[ "${FULL_VERIFY_RUN_INTEGRATION:-false}" == "true" ]]; then
  run_check "static checks" "synthetic test roles" "fresh retained dependency integration suite" "0 failures/errors" "MariaDB/RabbitMQ/Redis/MinIO/ClamAV/Elasticsearch" ./scripts/verify-local-integration.sh || true
else
  record "static checks" "synthetic test roles" "fresh dependency integration suite" "0 failures/errors" "not requested in this invocation" "see verify-local-integration.sh" SKIP
fi

if [[ -n "${APP_BASE_URL:-}" && -n "${VERIFY_DB_PASSWORD:-}" ]]; then
  run_check "retained runtime and disposable database" "synthetic admin/client/artisan/second-owner fixtures" \
    "deterministic multi-role semantic matrix for every OpenAPI operation" \
    "all published operations, boundary cases, and no 5xx/transport failures" \
    "${API_ROUTE_REPORT:-.agent-output/live-api-semantic-matrix.tsv}" \
    env APP_BASE_URL="$APP_BASE_URL" VERIFY_DB_PASSWORD="$VERIFY_DB_PASSWORD" \
      API_ROUTE_REPORT="${API_ROUTE_REPORT:-.agent-output/live-api-semantic-matrix.tsv}" \
      python3 scripts/verify-live-semantic.py || true
else
  record "retained runtime and database credentials" "synthetic role fixtures" \
    "deterministic multi-role semantic matrix" "all published operations pass" \
    "missing APP_BASE_URL or VERIFY_DB_PASSWORD" "see verify-live-semantic.py" SKIP
fi

if [[ -n "${APP_BASE_URL:-}" && -n "${SOUKLAB_ACCESS_TOKEN:-}" ]]; then
  run_check "running application and access token" "admin/client synthetic accounts" "authenticated HTTP/OpenAPI sweep" "all operations, no 5xx/transport failures" "runtime state and route report" ./scripts/verify-live-http.sh || true
  sleep 3
  run_check "running application and access token" "authenticated synthetic user" "native/SockJS STOMP verification" "CONNECT/SUBSCRIBE boundary passes" "broker relay delivery" python3 scripts/verify-live-stomp.py || true
else
  record "running application and access token" "synthetic roles" "authenticated HTTP/STOMP checks" "pass" "missing APP_BASE_URL or SOUKLAB_ACCESS_TOKEN" "not applicable" SKIP
fi

if [[ -n "${APP_BASE_URL:-}" && -n "${SOUKLAB_ACCESS_TOKEN:-}" && -n "${CHARGILY_TEST_PLAN_ID:-}" && -n "${CHARGILY_SECRET_KEY:-}" ]]; then
  run_check "authenticated client profile and test plan" "synthetic client/active/verified" "local Chargily checkout and webhook suite" "checkout, idempotency, paid/failed/canceled and validation pass" "payment/subscription/webhook rows" ./scripts/verify-chargily-local-e2e.sh || true
else
  record "authenticated client profile and test plan" "synthetic client" "local Chargily suite" "pass" "missing runtime/provider test variables" "not applicable" SKIP
fi

cat >>"$report" <<EOF

## Summary

- Passed: ${passed}
- Failed: ${failed}
- Skipped: ${skipped}
- Report is intentionally evidence-classified rather than a response dump.
EOF

python3 scripts/validate-verification-report.py "$report"

printf 'FULL_VERIFY_RESULT=%s report=%s passed=%s failed=%s skipped=%s\n' \
  "$([[ "$failed" -eq 0 ]] && echo PASS || echo FAIL)" "$report" "$passed" "$failed" "$skipped"
[[ "$failed" -eq 0 ]]
