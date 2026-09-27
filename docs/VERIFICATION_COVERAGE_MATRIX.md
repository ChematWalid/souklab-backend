# Souklab exhaustive verification coverage

Run date: 2026-09-27. Evidence is sanitized; credentials, tokens, provider
secrets, signatures, and raw webhook bodies are excluded.

| Plan requirement | Current evidence | Status |
|---|---|---|
| Fresh retained dependency environment and isolated compose project | Final integration run plus unique `COMPOSE_PROJECT_NAME` in `scripts/full-verification.sh`; 22 Flyway migrations through V21, MariaDB, RabbitMQ, Redis, MinIO, Elasticsearch, and ClamAV phases passed | Verified |
| Synthetic accounts, verification-code recovery, status and ownership fixtures | `scripts/verify-live-semantic.py`; `.agent-output/live-status-fixtures.tsv`; 8 status assertions passed | Verified |
| Registration, verification, login boundaries, lockout, refresh, logout, password and profile lifecycle | `.agent-output/auth-workflow-current.md`; 24 semantic authentication cases passed | Verified |
| Administration, approvals, rejection, timeout, suspension, unban and permissions | `.agent-output/live-status-fixtures.tsv`; live semantic matrix; focused administration tests | Verified |
| Catalog, directory, profiles, feeds, reports and ownership boundaries | 232-operation live semantic matrix and focused controller/service tests | Verified |
| Uploads, MIME/size/antivirus validation and cleanup | Focused upload/storage/antivirus tests; multipart attachment contract replay passed | Verified |
| Formation lifecycle, enrollment, cancellation, download and reviews | Focused formation/enrollment suites and live route boundary replay | Verified |
| Conversations, messages, attachments, notifications and isolation | Native/SockJS STOMP replay, focused chat/notification suites, and semantic matrix | Verified |
| Formateur request lifecycle | Focused formateur approval/rejection/cooldown tests and live route replay | Verified |
| Subscription, checkout, idempotency, cancellation, grants, corrections and refunds | Focused subscription/refund suites plus local Chargily paid/failed/canceled replay | Verified locally |
| Analytics jobs, polling, downloads, rebuild/backfill and authorization | Focused analytics suites and live semantic authorization replay | Verified |
| Health, OpenAPI, malformed/unsupported/conflict/not-found and dependency boundaries | 232 published operations; 1,987 semantic cases; 0 5xx/transport failures; integration phases passed | Verified |
| Admin audit-log endpoint and database reconciliation | `GET /api/v1/admin/users/audit-logs`, OpenAPI/API guide, controller tests, and MariaDB reconciliation across 56 audit action types | Verified |
| WebSocket/STOMP relay, receipts, acknowledgements, typing, edit/delete/read and reconnect boundaries | Native/SockJS verifier plus RabbitMQ relay and cross-user authorization replay | Verified |
| Chargily Test Mode authentication and checkout creation | Official Test Mode API smoke passed | Verified |
| Chargily hosted cancellation | Hosted Test Mode cancellation completed and provider state checked | Verified |
| Chargily hosted approval and provider-originated paid callback | Hosted Test Mode approval completed; `checkout.paid` reached Souklab and payment/subscription/notification/audit state reconciled | Verified |
| Chargily Pro v1 mobile-top-up/voucher API | Separate product/API, not used by Souklab's Pay v2 integration | Not applicable |
| Local failed-payment, malformed, duplicate, stale, signature and idempotency cases | Local provider suite and webhook-focused tests passed | Verified locally |
| Single-resource CRUD symmetry & chat privacy/resilience suite | 173 live curl scenarios: auth boundaries, RBAC isolation, IDOR, SQLi/XSS/path-traversal/null-byte fuzzing across 12 single-read endpoints, client premium chat enforcement, and artisan identity masking (`scripts/test-crud-scenarios.py`) | Verified |
| Full regression and security gates | 1,481 tests: 0 failures/errors, 10 environment-dependent skips; OWASP: 265 scanned, 0 vulnerabilities; source/API/diff checks passed | Verified |

## Aggregate evidence

- Full Maven suite with all Phase 9/10 integration gates enabled: 1,481 tests,
  0 failures, 0 errors, 10 environment-dependent skips.
- Multi-role live semantic matrix: 1,987 cases across all 232 operations, 0 5xx responses, 0 transport failures (`scripts/verify-live-semantic.py`).
- Live HTTP contract route sweep: 827 cases across all 232 operations (`scripts/verify-live-http.sh`).
- Live CRUD & Chat Security Resilience Suite (`scripts/test-crud-scenarios.py`): 173 live curl scenarios, 173 passed, 0 failed.
- Semantic authentication workflow replay (`scripts/verify-auth-workflow.py`): 24 cases, 24 passed, 0 failed.
- Local Chargily Pay V2 E2E checkout & webhooks (`scripts/verify-chargily-local-e2e.sh`): checkout, idempotency, signature, transitions passed.
- WebSocket STOMP Relay verification (`scripts/verify-live-stomp.py`): native & SockJS verification passed.
- Hosted Chargily Test Mode approval and cancellation, provider callbacks, and
  payment/subscription/audit reconciliation were completed manually. Live-mode
  financial operations remain intentionally out of scope.
