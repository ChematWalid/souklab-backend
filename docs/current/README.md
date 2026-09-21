# Souklab documentation index

These documents describe the current layered modular monolith and its `/api/v1` contract. Historical phase notes are evidence, not API authority.

## Current authority

- [Frontend API handoff](frontend/API_HANDOFF.md) — TypeScript consumer guide.
- [API conventions](API_CONVENTIONS.md) — envelopes, statuses, pagination, and naming.
- [API specification](API_SPEC.md) — endpoint groups and authorization behavior.
- [Authorization matrix](AUTHORIZATION_MATRIX.md) — permission and ownership rules.
- [Architecture](ARCHITECTURE.md) — layer boundaries and package ownership.
- [Production configuration matrix](PRODUCTION_CONFIGURATION_MATRIX.md) — environment policy.
- [Deployment runbook](DEPLOYMENT_RUNBOOK.md) — operational procedures.
- [Database schema](DATABASE_SCHEMA.md) — entities and Flyway migrations.
- [Generated OpenAPI Markdown](API_OPENAPI.md) — human-readable endpoint and schema reference exported from the running application.

## Generated contract

When the application is running, Springdoc exposes the code-first contract at `/v3/api-docs` (or the configured `OPENAPI_PATH`). Export it with:

```bash
./scripts/export-openapi.sh http://localhost:8080/v3/api-docs docs/generated/openapi.json
./scripts/openapi-to-markdown.sh docs/generated/openapi.json docs/current/API_OPENAPI.md
```

The JSON export is intentionally not committed. The Markdown companion is committed as the reviewable API reference and should be regenerated whenever the live contract changes. CI should upload the JSON artifact and validate it before publishing a frontend build.

Phase reports, old release plans, and the generated Postman reference remain for traceability and are explicitly archival where they can describe superseded behavior.

## Controlled Chargily verification

The credential-free provider in `scripts/chargily-test-provider.py` implements
only `POST /checkouts` and is intended for local application verification. Set
`CHARGILY_BASE_URL=http://127.0.0.1:8787`, start it with
`CHARGILY_FAKE_MODE=success`, and keep `CHARGILY_ENABLED=true` only in the
local/test environment. `validation`, `rate_limit`, `provider`, and `malformed`
exercise negative provider responses; no money or external network is involved.

`scripts/chargily-sandbox-smoke.sh` is a separately controlled real-provider
check. It requires test-mode credentials and a test plan, creates one checkout,
prints only `PASS`, `BLOCKED_CREDENTIALS`, or `BLOCKED_PROVIDER`, and stops
before card entry, capture, refund, or webhook delivery. A public callback is
still required for real webhook delivery, which is not part of this smoke test.

For a repeatable application-level local lifecycle check, run
`scripts/verify-chargily-local-e2e.sh` with `SOUKLAB_ACCESS_TOKEN`,
`CHARGILY_TEST_PLAN_ID`, and the local `CHARGILY_SECRET_KEY`. It verifies
checkout metadata, idempotent replay, signed paid/duplicate delivery, invalid
signature, stale event, and unknown checkout handling. Optional
`CHARGILY_FAILED_ACCESS_TOKEN` and `CHARGILY_CANCELED_ACCESS_TOKEN` values run
the failed and canceled checkout paths with separate prepared test users.

`scripts/verify-live-http.sh` performs a repeatable status-only sweep of public
catalog/directory routes plus authenticated profile, notification, subscription,
payment, health, and OpenAPI routes when `SOUKLAB_ACCESS_TOKEN` is supplied.
It records only method, path, status, expected status, trace ID, and a sanitized
classification. It does not claim full business-journey coverage or replace a
STOMP/WebSocket client; the current local WebSocket evidence is the `/ws` and
`/ws/info` SockJS handshake plus the authenticated relay/integration tests.

`scripts/verify-live-stomp.py` is the repeatable client-side STOMP check. It
uses only Python's standard library, authenticates with the bearer token,
subscribes to the configured user destination, optionally sends one supplied
test frame, verifies a delivered `MESSAGE`, and disconnects. It prints only a
classification; missing credentials or an unavailable endpoint are reported as
blocked rather than treated as a pass.

Analytics activity outbox messages use canonical `eventId`, `eventType`, and
ISO-8601 `eventTime` fields. The consumer retains compatibility with legacy
array-shaped `LocalDateTime` values. The local Chargily application check also
covered checkout idempotency and a signed paid webhook; real sandbox access
remains explicitly classified by the smoke script when credentials/provider
access are unavailable.
