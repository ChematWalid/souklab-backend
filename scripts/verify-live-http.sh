#!/usr/bin/env bash
set -euo pipefail

# Repeatable HTTP contract sweep. It records status and trace identifiers only;
# tokens, request bodies, and response bodies are never written to the report.
base_url="${APP_BASE_URL:-http://localhost:8080}"
report="${LIVE_VERIFY_REPORT:-.agent-output/live-http-verification.tsv}"
token="${SOUKLAB_ACCESS_TOKEN:-}"
require_auth="${LIVE_VERIFY_REQUIRE_AUTH:-false}"
curl_auth_args=()
if [[ -n "$token" ]]; then
  curl_auth_args+=(--header "Authorization: Bearer $token")
fi
mkdir -p "$(dirname "$report")"
printf 'method\tpath\tstatus\texpected\ttrace_id\tclassification\n' > "$report"

check() {
  local method="$1" path="$2" expected="$3" status trace_id classification headers
  headers="$(mktemp)"
  status="$(curl --silent --output /dev/null --write-out '%{http_code}' \
    --dump-header "$headers" --request "$method" \
    "${curl_auth_args[@]}" "$base_url$path" || true)"
  trace_id="$(awk 'BEGIN{IGNORECASE=1} /^x-trace-id:/{sub("\r$", "", $2); print $2}' "$headers" | tail -1)"
  if [[ "$status" =~ ^2 ]]; then classification="PASS"; else classification="BOUNDARY_OR_FAILURE"; fi
  printf '%s\t%s\t%s\t%s\t%s\t%s\n' "$method" "$path" "$status" "$expected" "${trace_id:--}" "$classification" >> "$report"
  rm -f "$headers"
  [[ "$status" =~ ^($expected)$ ]]
}

# These operational/documentation endpoints are private by default. A 2xx
# result is valid only when the caller has supplied an authenticated setup;
# unauthenticated verification must observe the security boundary.
check GET /actuator/health "200|401|403"
check GET /v3/api-docs "200|401|403"

check GET /api/v1/catalog/categories "200|401|403"
check GET /api/v1/catalog/materials "200|401|403"
check GET /api/v1/catalog/techniques "200|401|403"
check GET /api/v1/catalog/regions "200|401|403"
check GET /api/v1/catalog/epoques "200|401|403"
check GET /api/v1/public/directory "200|401|403"

if [[ -n "$token" ]]; then
  check GET /v3/api-docs "200"
  check GET /api/v1/auth/me "200"
  check GET /api/v1/notifications "200"
  check GET /api/v1/notifications/unread-count "200"
  check GET /api/v1/subscriptions/plans "200"
  check GET /api/v1/subscriptions/current "200|404"
  check GET /api/v1/payments "200"
elif [[ "$require_auth" == "true" ]]; then
  echo 'LIVE_HTTP_RESULT=BLOCKED_AUTHENTICATION' >&2
  exit 2
fi

printf 'LIVE_HTTP_RESULT=PASS\n'
