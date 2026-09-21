#!/usr/bin/env bash
set -euo pipefail

# Small repeatable HTTP contract sweep. It records status only, never tokens
# or response bodies. Full business journeys remain environment-specific.
base_url="${APP_BASE_URL:-http://localhost:8080}"
report="${LIVE_VERIFY_REPORT:-.agent-output/live-http-verification.tsv}"
mkdir -p "$(dirname "$report")"
printf 'method\tpath\tstatus\texpected\n' > "$report"

check() {
  local method="$1" path="$2" expected="$3" status
  status="$(curl --silent --output /dev/null --write-out '%{http_code}' \
    --request "$method" "$base_url$path" || true)"
  printf '%s\t%s\t%s\t%s\n' "$method" "$path" "$status" "$expected" >> "$report"
  [[ "$status" =~ ^($expected)$ ]]
}

check GET /actuator/health 200
check GET /v3/api-docs "401|403"
printf 'LIVE_HTTP_RESULT=PASS\n'
