# Souklab exhaustive verification coverage

Run date: 2026-09-25. Evidence is sanitized; credentials, tokens, provider
secrets, signatures, and raw webhook bodies are excluded.

| Plan requirement | Current evidence | Status |
|---|---|---|
| Fresh retained dependency environment and isolated compose project | Final integration run plus unique `COMPOSE_PROJECT_NAME` in `scripts/full-verification.sh`; 19 Flyway migrations through v18, MariaDB, RabbitMQ, Redis, MinIO, Elasticsearch, and ClamAV phases passed | Verified |
| Synthetic accounts, verification-code recovery, status and ownership fixtures | `scripts/verify-live-semantic.py`; `.agent-output/live-status-fixtures-final.tsv`; 8 status assertions passed | Verified |
| Registration, verification, login boundaries, lockout, refresh, logout, password and profile lifecycle | `.agent-output/auth-workflow-current.md`; 24 semantic authentication cases passed | Verified |
| Administration, approvals, rejection, timeout, suspension, unban and permissions | `.agent-output/live-status-fixtures-final.tsv`; live semantic matrix; focused administration tests | Verified |
| Catalog, directory, profiles, feeds, reports and ownership boundaries | 143-operation live semantic matrix and focused controller/service tests | Verified |
| Uploads, MIME/size/antivirus validation and cleanup | Focused upload/storage/antivirus tests; multipart attachment contract replay passed | Verified |
| Formation lifecycle, enrollment, cancellation, download and reviews | Focused formation/enrollment suites and live route boundary replay | Verified |
| Conversations, messages, attachments, notifications and isolation | Native/SockJS STOMP replay, focused chat/notification suites, and semantic matrix | Verified |
| Formateur request lifecycle | Focused formateur approval/rejection/cooldown tests and live route replay | Verified |
| Subscription, checkout, idempotency, cancellation, grants, corrections and refunds | Focused subscription/refund suites plus local Chargily paid/failed/canceled replay | Verified locally |
| Analytics jobs, polling, downloads, rebuild/backfill and authorization | Focused analytics suites and live semantic authorization replay | Verified |
| Health, OpenAPI, malformed/unsupported/conflict/not-found and dependency boundaries | 143 published operations; 1,232 semantic cases; 0 5xx/transport failures; integration phases passed | Verified |
| Admin audit-log endpoint and database reconciliation | `GET /api/v1/admin/users/audit-logs`, OpenAPI/API guide, controller tests, and MariaDB reconciliation | Verified |
| WebSocket/STOMP relay, receipts, acknowledgements, typing, edit/delete/read and reconnect boundaries | Native/SockJS verifier plus RabbitMQ relay and cross-user authorization replay | Verified |
| Chargily Test Mode authentication and checkout creation | Official Test Mode API smoke passed | Verified |
| Chargily hosted cancellation | Hosted Test Mode cancellation completed and provider state checked | Verified |
| Chargily hosted approval and provider-originated paid callback | Hosted Test Mode approval completed; `checkout.paid` reached Souklab and payment/subscription/notification/audit state reconciled | Verified |
| Chargily Pro v1 mobile-top-up/voucher API | Separate product/API, not used by Souklab's Pay v2 integration | Not applicable |
| Local failed-payment, malformed, duplicate, stale, signature and idempotency cases | Local provider suite and webhook-focused tests passed | Verified locally |
| Full regression and security gates | 1,439 tests: 0 failures/errors, 10 environment-dependent skips; OWASP: 265 scanned, 0 vulnerabilities; source/API/diff checks passed | Verified |

## Aggregate evidence

- Full Maven suite with all Phase 9/10 integration gates enabled: 1,439 tests,
  0 failures, 0 errors, 10 environment-dependent skips.
- Live semantic matrix: 1,232 cases, 0 5xx responses, 0 transport failures.
- Hosted Chargily Test Mode approval and cancellation, provider callbacks, and
  payment/subscription/audit reconciliation were completed manually. Live-mode
  financial operations remain intentionally out of scope.
