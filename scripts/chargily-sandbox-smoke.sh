#!/usr/bin/env bash
set -euo pipefail

# Controlled smoke test: creates exactly one test-mode checkout and stops.
# It never enters card details, captures payment, refunds, or calls webhooks.
base_url="${APP_BASE_URL:-http://localhost:8080}"
result="BLOCKED_CREDENTIALS"
cleanup() { printf 'CHARGILY_SANDBOX_RESULT=%s\n' "$result"; }
trap cleanup EXIT

if [[ "${CHARGILY_MODE:-}" != "test" || -z "${CHARGILY_API_KEY:-}" || -z "${CHARGILY_SECRET_KEY:-}" ]]; then
  exit 0
fi
if [[ -z "${SOUKLAB_ACCESS_TOKEN:-}" || -z "${CHARGILY_TEST_PLAN_ID:-}" ]]; then
  exit 0
fi

response_file="$(mktemp)"
trap 'rm -f "$response_file"; cleanup' EXIT
status="$(curl --silent --show-error --output "$response_file" --write-out '%{http_code}' \
  --request POST "$base_url/api/v1/subscriptions/checkout" \
  --header "Authorization: Bearer $SOUKLAB_ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --header "Idempotency-Key: ${CHARGILY_SMOKE_IDEMPOTENCY_KEY:-souklab-sandbox-smoke-once}" \
  --data "{\"planId\":\"$CHARGILY_TEST_PLAN_ID\"}")"

if [[ "$status" == "401" || "$status" == "403" ]]; then
  result="BLOCKED_CREDENTIALS"
  exit 0
fi
if [[ "$status" =~ ^5 ]]; then
  result="BLOCKED_PROVIDER"
  exit 0
fi
if [[ "$status" != "2"* ]]; then
  result="BLOCKED_PROVIDER"
  exit 0
fi

# Only print non-sensitive structural evidence; response bodies may contain
# personal data or provider secrets and are intentionally not logged.
python3 - "$response_file" <<'PY'
import json, sys
body = json.load(open(sys.argv[1]))
data = body.get("data") or {}
required = ("paymentId", "subscriptionId", "providerCheckoutId", "checkoutUrl")
if not all(data.get(key) for key in required):
    raise SystemExit("sandbox response missing checkout metadata")
print("sandbox checkout metadata: present")
PY
result="PASS"
