#!/usr/bin/env bash
set -euo pipefail

# Bounded local Chargily application verification. This script talks only to
# APP_BASE_URL and never enters card details or moves money. Registration and
# email verification are intentionally outside this script because the local
# SMTP code is operator-controlled.
base_url="${APP_BASE_URL:-http://localhost:8080}"
access_token="${SOUKLAB_ACCESS_TOKEN:-}"
plan_id="${CHARGILY_TEST_PLAN_ID:-}"
secret_key="${CHARGILY_SECRET_KEY:-}"
report="${CHARGILY_E2E_REPORT:-.agent-output/chargily-local-e2e.tsv}"

[[ -n "$access_token" && -n "$plan_id" && -n "$secret_key" ]] || {
  echo 'CHARGILY_LOCAL_E2E_RESULT=BLOCKED_CONFIGURATION'
  exit 2
}

mkdir -p "$(dirname "$report")"
printf 'case\tstatus\texpected\tresult\n' > "$report"

record() {
  printf '%s\t%s\t%s\t%s\n' "$1" "$2" "$3" "$4" >> "$report"
}

post_checkout() {
  local token="$1" key="$2" response_file="$3" status
  status="$(curl --silent --show-error --output "$response_file" --write-out '%{http_code}' \
    --request POST "$base_url/api/v1/subscriptions/checkout" \
    --header "Authorization: Bearer $token" \
    --header 'Content-Type: application/json' \
    --header "Idempotency-Key: $key" \
    --data "{\"planId\":\"$plan_id\"}")"
  [[ "$status" == 200 ]] || { record "checkout:$key" "$status" 200 FAIL; return 1; }
  record "checkout:$key" "$status" 200 PASS
}

signed_webhook() {
  local name="$1" token="$2" event_type="$3" provider_id="$4" event_id="$5" created_at="$6" response_file="$7" payload signature status
  payload="$(jq -cn --arg id "$event_id" --arg type "$event_type" --arg checkout "$provider_id" \
    --argjson created "$created_at" '{id:$id,type:$type,created_at:$created,data:{id:$checkout}}')"
  signature="$(printf '%s' "$payload" | openssl dgst -sha256 -hmac "$secret_key" -hex | awk '{print $2}')"
  status="$(curl --silent --show-error --output "$response_file" --write-out '%{http_code}' \
    --request POST "$base_url/api/v1/integrations/chargily/webhook" \
    --header "signature: $signature" --header 'Content-Type: application/json' --data-binary "$payload")"
  [[ "$status" == 200 ]] || { record "$name" "$status" 200 FAIL; return 1; }
  record "$name" "$status" 200 PASS
}

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

first="$tmp_dir/first.json"
second="$tmp_dir/second.json"
idempotency_key="souklab-local-e2e-$(date +%s)"
post_checkout "$access_token" "$idempotency_key" "$first"
checkout_key="$(jq -r '.data.paymentId // empty' "$first")"
provider_id="$(jq -r '.data.providerCheckoutId // empty' "$first")"
subscription_id="$(jq -r '.data.subscriptionId // empty' "$first")"
checkout_url="$(jq -r '.data.checkoutUrl // empty' "$first")"
[[ -n "$checkout_key" && -n "$provider_id" && -n "$subscription_id" && "$checkout_url" == http* ]] || {
  echo 'CHARGILY_LOCAL_E2E_RESULT=FAIL_MISSING_CHECKOUT_METADATA'
  exit 1
}

post_checkout "$access_token" "$idempotency_key" "$second"
replay_id="$(jq -r '.data.paymentId // empty' "$second")"
[[ "$replay_id" == "$checkout_key" ]] || { record idempotent-replay "$replay_id" "$checkout_key" FAIL; exit 1; }
record idempotent-replay 200 200 PASS

now="$(date +%s)"
signed_webhook paid "$access_token" checkout.paid "$provider_id" "souklab-local-paid-$checkout_key" "$now" "$tmp_dir/paid.out"
signed_webhook duplicate-paid "$access_token" checkout.paid "$provider_id" "souklab-local-paid-$checkout_key" "$now" "$tmp_dir/duplicate.out"

invalid_status="$(curl --silent --output /dev/null --write-out '%{http_code}' \
  --request POST "$base_url/api/v1/integrations/chargily/webhook" \
  --header 'signature: invalid' --header 'Content-Type: application/json' \
  --data '{"id":"souklab-local-invalid","type":"checkout.paid","created_at":1,"data":{"id":"unknown"}}')"
[[ "$invalid_status" == 403 ]] || { record invalid-signature "$invalid_status" 403 FAIL; exit 1; }
record invalid-signature "$invalid_status" 403 PASS

stale_payload="$(jq -cn --arg id souklab-local-stale --arg checkout unknown --argjson created "$(date -d '365 days ago' +%s)" \
  '{id:$id,type:"checkout.paid",created_at:$created,data:{id:$checkout}}')"
stale_signature="$(printf '%s' "$stale_payload" | openssl dgst -sha256 -hmac "$secret_key" -hex | awk '{print $2}')"
stale_status="$(curl --silent --output /dev/null --write-out '%{http_code}' --request POST "$base_url/api/v1/integrations/chargily/webhook" \
  --header "signature: $stale_signature" --header 'Content-Type: application/json' --data-binary "$stale_payload")"
[[ "$stale_status" == 400 ]] || { record stale-event "$stale_status" 400 FAIL; exit 1; }
record stale-event "$stale_status" 400 PASS

unknown_payload="$(jq -cn --arg id souklab-local-unknown --arg checkout unknown --argjson created "$now" \
  '{id:$id,type:"checkout.paid",created_at:$created,data:{id:$checkout}}')"
unknown_signature="$(printf '%s' "$unknown_payload" | openssl dgst -sha256 -hmac "$secret_key" -hex | awk '{print $2}')"
unknown_status="$(curl --silent --output /dev/null --write-out '%{http_code}' --request POST "$base_url/api/v1/integrations/chargily/webhook" \
  --header "signature: $unknown_signature" --header 'Content-Type: application/json' --data-binary "$unknown_payload")"
[[ "$unknown_status" == 200 ]] || { record unknown-checkout "$unknown_status" 200 FAIL; exit 1; }
record unknown-checkout "$unknown_status" 200 PASS

run_optional_terminal_case() {
  local label="$1" token="$2" event_type="$3" response_file provider event_id
  [[ -n "$token" ]] || return 0
  response_file="$tmp_dir/$label.json"
  post_checkout "$token" "souklab-local-e2e-$label-$(date +%s)" "$response_file"
  provider="$(jq -r '.data.providerCheckoutId // empty' "$response_file")"
  [[ -n "$provider" ]] || { echo "CHARGILY_LOCAL_E2E_RESULT=FAIL_${label}_CHECKOUT"; exit 1; }
  event_id="souklab-local-$label-$provider"
  signed_webhook "$label-webhook" "$token" "$event_type" "$provider" "$event_id" "$now" "$tmp_dir/$label.out"
}

run_optional_terminal_case failed "${CHARGILY_FAILED_ACCESS_TOKEN:-}" checkout.failed
run_optional_terminal_case canceled "${CHARGILY_CANCELED_ACCESS_TOKEN:-}" checkout.canceled

printf 'CHARGILY_LOCAL_E2E_RESULT=PASS\n'
