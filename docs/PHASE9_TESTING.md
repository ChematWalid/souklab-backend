# Phase 9 Testing Plan

Phase 9 testing is split into deterministic local verification and a later real-provider check.

## 1. Local Docker-mocked testing

Use the existing WireMock-based contract tests, optionally run from Docker, to simulate Chargily Pay V2.

The local mock verifies:

- Chargily authorization headers and checkout request payloads;
- amount, currency, configured URLs, fee allocation, locale, and metadata;
- successful checkout responses;
- provider responses for 400, 401, 403, 404, 429, and selected 5xx errors;
- timeout and retry behavior;
- response parsing and provider-error sanitization;
- signed webhook delivery;
- duplicate and concurrent webhook handling;
- payment-to-subscription activation;
- failed, canceled, expired, stale, malformed, and invalidly signed events;
- idempotency-key reuse and conflict handling;
- premium-flag synchronization and lifecycle expiry behavior.

This is the default CI path and must remain credential-free and deterministic.

## 2. Real Chargily test-mode verification

After local tests pass, run one end-to-end checkout with Chargily test-mode credentials.

Chargily cannot call a private `localhost` endpoint. Use one of these public HTTPS options:

- a Cloudflare Tunnel forwarding to the local backend;
- another secure tunnel provider;
- a publicly deployed staging backend.

The webhook URL must point to:

```text
https://<public-host>/api/v1/integrations/chargily/webhook
```

For local development, success and failure browser redirects may still target the local frontend. The webhook URL must be
publicly reachable because subscription activation occurs only after the verified webhook.

Recommended test-mode sequence:

1. Create a temporary public HTTPS tunnel or use staging.
2. Set `CHARGILY_MODE=test`, the test API credentials, and the public `CHARGILY_WEBHOOK_URL` in the ignored `.env` file.
3. Set `CHARGILY_ENABLED=true` and `SUBSCRIPTION_ENABLED=true`.
4. Restart the backend and create a checkout through the application.
5. Complete the test payment in Chargily’s test environment.
6. Verify the signed webhook reaches the backend and returns HTTP 200.
7. Confirm the payment becomes `PAID`, the subscription becomes `ACTIVE`, and the corresponding premium flag is synchronized.
8. Repeat with a failed or canceled test checkout and confirm that no entitlement is activated.

Never commit API keys, webhook secrets, or encrypted-payload keys. If a secret is exposed, regenerate it before continuing.

## Acceptance boundary

Passing the Docker-mocked suite proves application/provider-contract behavior. Passing the real test-mode sequence proves the
external Chargily account, credentials, network ingress, and webhook delivery are configured correctly. Both checks are required
before production activation.
