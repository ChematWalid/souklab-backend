#!/usr/bin/env bash
set -euo pipefail

# Verify a provider-hosted callback after the operator completes Chargily's
# Test Mode UI. No credentials, raw webhook data, or identifiers are printed.
: "${VERIFY_DB_PASSWORD:?set VERIFY_DB_PASSWORD}"
: "${PAYMENT_ID:?set PAYMENT_ID}"
: "${SUBSCRIPTION_ID:?set SUBSCRIPTION_ID}"
: "${PROVIDER_CHECKOUT_ID:?set PROVIDER_CHECKOUT_ID}"
: "${CHARGILY_CALLBACK_EXPECTED:?set CHARGILY_CALLBACK_EXPECTED (paid or canceled)}"

for value in "$PAYMENT_ID" "$SUBSCRIPTION_ID" "$PROVIDER_CHECKOUT_ID"; do
  [[ "$value" =~ ^[A-Za-z0-9._:-]+$ ]] || { echo 'invalid identifier' >&2; exit 2; }
done
[[ "$CHARGILY_CALLBACK_EXPECTED" == paid || "$CHARGILY_CALLBACK_EXPECTED" == canceled ]] || {
  echo 'CHARGILY_CALLBACK_EXPECTED must be paid or canceled' >&2
  exit 2
}

db() {
  MYSQL_PWD="$VERIFY_DB_PASSWORD" mariadb --batch --skip-column-names --raw \
    -h "${VERIFY_DB_HOST:-127.0.0.1}" -P "${VERIFY_DB_PORT:-3307}" \
    -u "${VERIFY_DB_USER:-souklab_test}" "${VERIFY_DB_NAME:-souklab_test}" -e "$1"
}

contains_word() {
  case " $1 " in
    *" $2 "*) return 0 ;;
    *) return 1 ;;
  esac
}

payment_status="$(db "SELECT status FROM payments WHERE id='$PAYMENT_ID' AND provider_checkout_id='$PROVIDER_CHECKOUT_ID' AND deleted_at IS NULL LIMIT 1")"
subscription_status="$(db "SELECT status FROM client_subscriptions WHERE id='$SUBSCRIPTION_ID' AND deleted_at IS NULL UNION ALL SELECT status FROM artisan_subscriptions WHERE id='$SUBSCRIPTION_ID' AND deleted_at IS NULL LIMIT 1")"
webhook_count="$(db "SELECT COUNT(*) FROM payment_webhook_logs WHERE provider_checkout_id='$PROVIDER_CHECKOUT_ID' AND deleted_at IS NULL")"
webhook_types="$(db "SELECT GROUP_CONCAT(DISTINCT event_type SEPARATOR ' ') FROM payment_webhook_logs WHERE provider_checkout_id='$PROVIDER_CHECKOUT_ID' AND deleted_at IS NULL")"
webhook_statuses="$(db "SELECT GROUP_CONCAT(DISTINCT status SEPARATOR ' ') FROM payment_webhook_logs WHERE provider_checkout_id='$PROVIDER_CHECKOUT_ID' AND deleted_at IS NULL")"
notification_count="$(db "SELECT COUNT(*) FROM notifications WHERE target_id='$PAYMENT_ID' AND deleted_at IS NULL")"
notification_types="$(db "SELECT GROUP_CONCAT(DISTINCT type SEPARATOR ' ') FROM notifications WHERE target_id='$PAYMENT_ID' AND deleted_at IS NULL")"
audit_count="$(db "SELECT COUNT(*) FROM audit_logs WHERE (payment_id='$PAYMENT_ID' OR subscription_id='$SUBSCRIPTION_ID') AND deleted_at IS NULL")"
audit_actions="$(db "SELECT GROUP_CONCAT(DISTINCT action SEPARATOR ' ') FROM audit_logs WHERE (payment_id='$PAYMENT_ID' OR subscription_id='$SUBSCRIPTION_ID') AND deleted_at IS NULL")"
analytics_count="$(db "SELECT COUNT(*) FROM activity_events WHERE subject_id IN ('$PAYMENT_ID','$SUBSCRIPTION_ID') AND deleted_at IS NULL AND event_type IN ('PAYMENT_STATE_TRANSITION','SUBSCRIPTION_ACTIVATED','SUBSCRIPTION_CANCELED')")"
analytics_types="$(db "SELECT GROUP_CONCAT(DISTINCT event_type SEPARATOR ' ') FROM activity_events WHERE subject_id IN ('$PAYMENT_ID','$SUBSCRIPTION_ID') AND deleted_at IS NULL")"

if [[ "$CHARGILY_CALLBACK_EXPECTED" == paid ]]; then
  expected_payment=PAID expected_subscription=ACTIVE
  expected_webhook=checkout.paid expected_notification=PAYMENT_SUCCESS
  expected_audit='PAYMENT_PAID SUBSCRIPTION_ACTIVATED'
else
  expected_payment=CANCELED expected_subscription=CANCELED
  expected_webhook=checkout.canceled expected_notification=CHECKOUT_CANCELED
  expected_audit='PAYMENT_CANCELED SUBSCRIPTION_CANCELED'
fi

[[ "$payment_status" == "$expected_payment" ]] || { echo 'FAIL payment state'; exit 1; }
[[ "$subscription_status" == "$expected_subscription" ]] || { echo 'FAIL subscription state'; exit 1; }
[[ "$webhook_count" -ge 1 ]] || { echo 'FAIL webhook log'; exit 1; }
contains_word "$webhook_types" "$expected_webhook" || { echo 'FAIL webhook event type'; exit 1; }
contains_word "$webhook_statuses" PROCESSED || { echo 'FAIL webhook processing status'; exit 1; }
[[ "$notification_count" -ge 1 ]] || { echo 'FAIL notification'; exit 1; }
contains_word "$notification_types" "$expected_notification" || { echo 'FAIL notification type'; exit 1; }
[[ "$audit_count" -ge 2 ]] || { echo 'FAIL financial audit rows'; exit 1; }
for action in $expected_audit; do
  contains_word "$audit_actions" "$action" || { echo "FAIL audit action $action"; exit 1; }
done
[[ "$analytics_count" -ge 1 ]] || { echo 'FAIL analytics event'; exit 1; }
if [[ "$CHARGILY_CALLBACK_EXPECTED" == paid ]]; then
  contains_word "$analytics_types" SUBSCRIPTION_ACTIVATED || { echo 'FAIL analytics activation'; exit 1; }
else
  contains_word "$analytics_types" SUBSCRIPTION_CANCELED || { echo 'FAIL analytics cancellation'; exit 1; }
fi

echo "CHARGILY_CALLBACK_RESULT=PASS expected=$CHARGILY_CALLBACK_EXPECTED webhook=$expected_webhook notification=$expected_notification audit=$expected_audit"
