# Chargily Test Mode manual approval check

This is the provider-hosted Test Mode procedure. Use only Chargily Test Mode,
synthetic Souklab accounts, and a disposable test plan. Do not use live keys or
real card data.

Chargily Pro v1 (`pro.chargily.net`, `X-Authorization`, mobile top-ups, and
vouchers) is a separate product/API and is out of scope. Souklab integrates
Chargily Pay v2 checkout through the Test Mode Pay API only.

1. Start Souklab with the test-mode callback URL reachable through ngrok:

   `https://<ngrok-host>/api/v1/integrations/chargily/webhook`

   Confirm that `CHARGILY_MODE=test`, the Test Mode API base is
   `https://pay.chargily.net/test/api/v2`, and the provider webhook secret is
   configured. Keep the ngrok URL and provider keys out of screenshots and
   reports.

2. Using an active, verified synthetic client token, create one checkout:

   ```bash
   curl --fail-with-body --request POST "$APP_BASE_URL/api/v1/subscriptions/checkout" \
     --header "Authorization: Bearer $SOUKLAB_ACCESS_TOKEN" \
     --header 'Content-Type: application/json' \
     --header 'Idempotency-Key: manual-chargily-paid-1' \
     --data '{"planId":"<test-plan-id>"}'
   ```

   Copy only the returned `checkoutUrl` into a browser, and retain the
   `paymentId`, `subscriptionId`, and `providerCheckoutId` locally for the
   post-browser checker. Never paste the access token, provider key, full
   response, or identifiers into a ticket, screenshot, or report.

3. In the hosted Chargily Test Mode page, choose the simulated approval path.
   Complete the provider's test form and click `Payer`. If Google reCAPTCHA
   blocks the action, complete it interactively in the browser; synthetic
   automation cannot solve that gate.

4. After the browser returns, verify through the Souklab API and database that
   the payment is `PAID`, the subscription is `ACTIVE`, a notification and
   analytics event exist, the webhook log is accepted, and the admin audit
   endpoint contains `PAYMENT_PAID` and `SUBSCRIPTION_ACTIVATED`.

   The sanitized checker can assert all of those states without printing
   identifiers, tokens, keys, signatures, or webhook bodies:

   ```bash
   VERIFY_DB_PASSWORD='<disposable-db-password>' \
   PAYMENT_ID='<payment-id>' SUBSCRIPTION_ID='<subscription-id>' \
   PROVIDER_CHECKOUT_ID='<provider-checkout-id>' \
   CHARGILY_CALLBACK_EXPECTED=paid \
   ./scripts/verify-chargily-callback.sh
   ```

5. Create a second checkout with a new idempotency key, open its hosted URL,
   choose `Annuler`, and confirm cancellation. Verify `PAYMENT_CANCELED`, the
   subscription transition, webhook-log entry, notification, and audit row.
   Run the same checker with `CHARGILY_CALLBACK_EXPECTED=canceled` for this
   second checkout.

6. Record only statuses, synthetic actor labels, payment/subscription IDs
   truncated to a safe suffix if needed, timestamps, and database assertions.
   Never record JWTs, refresh tokens, keys, raw webhook bodies, or signatures.

Provider refund is not applicable to this Pay v2 checkout test if the provider
does not expose a Test Mode refund operation. Souklab's application refund and
audit workflow is already covered by the local verification suite.
