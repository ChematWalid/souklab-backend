# Souklab verification report

Run date: 2026-09-22

This report contains sanitized evidence only. Tokens, provider keys, webhook
bodies, checkout URLs, and personal data are intentionally excluded. Disposable
synthetic `@souklab.test` identities may appear in local workflow artifacts.

Requirement-by-requirement coverage is summarized in
`docs/VERIFICATION_COVERAGE_MATRIX.md`.

## Passed

### Continuation run (2026-09-21)

- Retained Docker dependencies were healthy and the full Maven suite produced
  1,228 test reports with 0 failures, 0 errors, and 0 skips when all Phase 9/10
  integration gates were enabled.
  The Surefire fork then required forced termination while waiting for the
  Spring STOMP relay shutdown future; no test failure or error was reported.
- Maven package compilation passed.
- Authenticated runtime health returned 200 against MariaDB and RabbitMQ.
- Live OpenAPI contained the admin audit route and synchronized 143 operations.
- Authenticated live HTTP sweep covered 143 operations with 0 failures.
- Native/SockJS STOMP verification passed.
- `GET /api/v1/admin/users/audit-logs` returned the documented pagination and
  financial audit fields; its result reconciled with MariaDB `audit_logs`.
- Local Chargily-compatible checkout, idempotent replay, paid/failed/canceled
  webhook transitions, duplicate delivery, and validation cases passed.
- Official Chargily Test Mode authentication and checkout creation returned
  successful responses with checkout metadata.
- The official hosted Test Mode cancellation control was exercised through its
  Livewire action and the provider checkout was verified as `canceled` through
  the Test Mode API.
- A compatible headless browser session also loaded the official hosted page,
  exercised the visible `Annuler` control, and reached Chargily's cancellation
  confirmation page. The visible `Payer` control was exercised with synthetic
  billing data; Chargily rejected the attempt at its Google reCAPTCHA gate.
- A signed webhook callback reached the application through a temporary ngrok
  tunnel and returned 200.
- Semantic authentication workflow replay passed 24 cases with 0 failures, including client and artisan registration, pending-state/login boundaries, duplicate/validation cases, OTP verification, refresh rotation/replay, password reset/change, profile lifecycle, logout, and OAuth intent cookies. The generated report was verified as clean UTF-8 text with no NUL bytes.
- Fresh semantic authentication replay (2026-09-22) also passed all 24 cases
  with 0 failures against the built jar and retained MariaDB; report:
  `.agent-output/auth-workflow-current.md`.
- Focused content workflow tests passed 26 cases with 0 failures across catalog, public directory validation, feed ownership/moderation boundaries, and content reports.
- Focused lifecycle/security tests passed 111 cases with 0 failures across formations/enrollment, conversations and STOMP controllers, notifications, formateur approval/rejection, analytics authorization, subscriptions, Chargily webhooks, and refund handling.
- Focused administration/upload/security tests passed 108 cases with 0 failures across user management, permissions, avatars, gallery/certification uploads, rate limits, S3 lifecycle, and real ClamAV clean/EICAR detection.
- The replay found and fixed an unverified-active-client login defect; the corrected JAR replay passed the boundary and the targeted `AuthServiceTest` passed.
- Dependency security verification passed with OWASP Dependency-Check 13.0.0
  using the cached NVD data: 265 dependencies scanned, 0 vulnerable
  dependencies, 0 vulnerabilities found, and 2 documented suppressions. The
  Elasticsearch REST client was upgraded from 9.3.8 to 9.4.5 before the final
  build.
- JaCoCo coverage from the current post-fix verification artifacts: 80.30% line
  coverage and 72.76% branch coverage.
- The reusable STOMP verifier was strengthened to require SockJS `/info`, an
  unauthenticated CONNECT rejection, and authenticated CONNECT/SUBSCRIBE
  behavior; syntax, source-hygiene, API-contract, and diff checks pass after
  the change.

- Isolated Docker dependencies: MariaDB, Flyway MariaDB, RabbitMQ/STOMP,
  Redis, MinIO, Elasticsearch, and ClamAV healthy.
- Full Maven/integration suite: 1,228 reported tests, 0 failures, 0 errors.
  A verifier-controlled clean-exit run with the four disposable-container
  phases disabled completed with Maven `BUILD SUCCESS` in 10:09, producing
  all 1,228 reports with 0 failures and 0 errors; the four phase tests were
  intentionally skipped in that run.
- Isolated Docker-backed integration exits: `Phase9MariaDbMigrationTest`,
  `Phase10MariaDbMigrationTest`, `Phase10RabbitMqIntegrationTest`, and
  `Phase10RedisRateLimitIntegrationTest` each passed with Maven exit code 0.
  The MariaDB tests applied all 15 migrations through v14 and verified their
  production uniqueness/invariant assertions. These four phase tests were
  rerun together against the current post-upgrade build and each reported
  1 test, 0 failures, 0 errors, and 0 skips.
- Live OpenAPI contract: 143 operations synchronized with the hand-authored
  guide.
- Live HTTP route sweep: 143 operations, 0 5xx failures, 0 transport failures.
  Evidence: `.agent-output/live-api-routes.tsv`.
- Fresh retained-runtime replay (2026-09-22): authenticated health, API
  documentation, and all 143 currently published OpenAPI operations passed
  with 0 route failures; evidence is `.agent-output/live-http-current.tsv`
  and `.agent-output/live-http-current-all-routes.tsv`.
- Fresh deterministic multi-role semantic boundary replay (2026-09-22): all
  143 published operations produced 1,232 sanitized cases across anonymous,
  admin, client, artisan, second-owner, malformed-body, and repeated-
  idempotency requests; 0 5xx responses and 0 transport failures. Evidence:
  `.agent-output/live-api-semantic-matrix-final.tsv`. The matrix includes
  290 explicitly labeled `wrong-role` cases and 143 ownership-boundary cases;
  it records method, path/query, safe headers, expected result, payload
  classification, status, response media type and size, actor role, and
  classification without recording credentials or response bodies.
- The same deterministic harness created and asserted all required account
  status fixtures: pending artisan login rejection, rejected client login
  rejection, timed-out client rejection/unban, and permanently suspended client
  rejection/unban. All 8 fixture assertions passed; evidence:
  `.agent-output/live-status-fixtures-final.tsv`.
- The clean high-capacity replay after the attachment contract fix passed all
  1,232 cases with 0 5xx responses, including multipart conversation
  attachments. Final evidence: `.agent-output/live-api-semantic-matrix-final.tsv`;
  status fixtures: `.agent-output/live-status-fixtures-final.tsv`.
- Conversation attachment documentation now declares the required multipart
  `file` part and 201 response, matching the controller contract.
- `scripts/full-verification.sh` now assigns a unique compose project name per
  run by default, preserving disposable-volume isolation while allowing an
  explicit project override for retained verification.
- The full-verification Markdown artifact now has explicit columns for sequence,
  prerequisite, actor/email, role/status/permissions, method/path/query,
  sanitized headers and payload, expected/actual response, DB/audit assertion,
  defect/fix/replay, and result. Its credential-free smoke run passed 3 checks
  with 4 clearly classified environment-dependent skips. The reusable report
  validator now enforces this schema, summary, result values, and secret-like
  value redaction; the final schema artifact passed validation.
- Authenticated native STOMP CONNECT/SUBSCRIBE probe: passed.
- Invalid STOMP authentication: rejected.
- SockJS `/ws/info`: reachable.
- Fresh retained-runtime STOMP replay (2026-09-22): `STOMP_VERIFY_RESULT=PASS`
  with authenticated and rejected-connect boundaries.
- Fresh native STOMP chat replay (2026-09-22): `CHAT_STOMP_RESULT=PASS` for
  authenticated send acknowledgement, message event delivery, edit, read,
  typing start/stop, delete, and cross-user rejection. The verifier was fixed
  to retain already-received MESSAGE frames when an otherwise-idle socket
  timed out; no application defect remained.
- Fresh admin boundary replay (2026-09-22): synthetic bootstrapped admin login,
  pending-user listing, user listing, and paginated audit-log reads returned
  200; the synthetic client received 403 and anonymous access received 401.
- Admin transition replay: synthetic artisan approval (200), client timeout
  (200), blocked login during timeout (403), unban (200), restored login (200),
  permission revoke/grant (200), and audit rows for `APPROVE_USER`,
  `TIMEOUT_USER`, `UNBAN_USER`, `PERMISSION_REVOKED`, and
  `PERMISSION_GRANTED` were reconciled in MariaDB.
- Public ngrok webhook callback: signed POST reached the local application.
- Local fake Chargily checkout: success and idempotent replay passed.
- Local signed webhooks: `checkout.paid`, duplicate paid, `checkout.failed`,
  and `checkout.canceled` passed.
- Fresh local terminal-state replay (2026-09-22): paid, failed, and canceled
  synthetic-client checkouts all passed with idempotency and signed webhook
  handling. MariaDB reconciled `PAYMENT_PAID`/`SUBSCRIPTION_ACTIVATED`,
  `PAYMENT_FAILED`/`SUBSCRIPTION_CANCELED`, and
  `PAYMENT_CANCELED`/`SUBSCRIPTION_CANCELED` audit rows, each carrying the
  associated payment and subscription IDs.
- Fixed a discovered audit defect: provider webhook state transitions were
  previously visible only through analytics; they now create admin-visible
  financial audit records. The focused webhook suite passed 4/4 after the
  fix, including the audit assertion.
- Fixed a second database-backed audit defect found during completion audit:
  the MariaDB `audit_logs.action` enum did not contain the financial webhook
  actions emitted by the service. Migration v14 adds those values; both real
  MariaDB migration tests passed with enum metadata assertions, and the full
  post-fix suite passed.
- Added `scripts/validate-audit-action-schema.py` to the source-hygiene gate;
  it currently confirms all 34 typed Java audit actions are represented in
  the v14 database enum and all 25 typed notification values are represented
  in the v5 notification enum.
- The Phase 9 MariaDB regression now inserts `PAYMENT_PAID` and
  `SUBSCRIPTION_ACTIVATED` rows into `audit_logs`; it passed with 1 test,
  0 failures, and 0 errors, proving the database accepts the service-emitted
  financial actions.
- Webhook validation: invalid signature, stale event, unknown checkout,
  malformed JSON, oversized body, unsupported event, and missing signature were
  exercised with expected validation responses.
- Both plain hexadecimal and `sha256=` HMAC-SHA256 signature formats passed.
- Official Chargily test checkout: passed; checkout metadata was returned.
- The exact browser/operator procedure for the hosted approval, cancellation,
  and callback check is documented in `docs/CHARGILY_MANUAL_TEST.md`.
- Added `scripts/verify-chargily-callback.sh` for the operator's final step;
  it checks payment/subscription state plus webhook, notification, analytics,
  and audit records while printing only sanitized classifications.
- Final hosted Chargily Test Mode approval completed on 2026-09-22. The
  provider callback reached the local application through ngrok and the
  callback verifier passed with `checkout.paid`, `PAID` payment,
  `ACTIVE` subscription, `PAYMENT_SUCCESS`, `PAYMENT_PAID`, and
  `SUBSCRIPTION_ACTIVATED`.
- Final hosted Chargily Test Mode cancellation completed on 2026-09-22. The
  callback verifier passed with `checkout.canceled`, `CANCELED` payment,
  `CANCELED` subscription, `CHECKOUT_CANCELED`, `PAYMENT_CANCELED`, and
  `SUBSCRIPTION_CANCELED`.
- Post-callback-fix Maven gate (2026-09-22) passed with 1,228 tests, 0
  failures, 0 errors, and 10 environment-dependent integration skips before
  the final all-gates run below.
- Independent MariaDB reconciliation confirmed four financial audit rows for
  the hosted Test Mode runs: `PAYMENT_PAID`, `SUBSCRIPTION_ACTIVATED`,
  `PAYMENT_CANCELED`, and `SUBSCRIPTION_CANCELED`, all linked to their
  payment and subscription records.
- Source hygiene, API contract checks, compilation, and targeted regression
  tests passed.
- Final clean verification replay after the attachment, webhook-audit,
  database-enum, and dependency security fixes (2026-09-22): Maven `BUILD
  SUCCESS` at 03:07 with 1,228
  tests, 0 failures, 0 errors, and
  4 intentional skips;
  the four disposable dependency phases were then rerun separately against
  the v14 build and each passed with 1 test, 0 failures, 0 errors, and 0 skips.

### Coverage map

The current evidence is split between live semantic replays and focused
controller/service suites. Catalog, directory, feeds, reports, artisan
profiles, gallery/certification, formation lifecycle and enrollment,
conversations/STOMP, notifications, formateur requests, analytics
authorization, subscriptions, Chargily webhooks, refunds, administration,
permissions, upload validation/antivirus, rate limits, and dependency
integration tests all have dedicated passing test classes. The live route
sweep is contract/boundary evidence for all 143 published operations; it is
not represented as proof that every operation completed a full business-data
scenario. The hosted Test Mode provider callbacks were completed manually;
live-mode financial operations remain intentionally outside this run.

## Externally dependent

- The hosted Chargily Test Mode approval and cancellation controls were
  completed manually. No live funds were used. A separate automated approval
  attempt may still encounter Google reCAPTCHA; that limitation does not
  invalidate the successful manual Test Mode run.
- Production payment capture, refunds, and production callbacks remain outside
  this disposable verification environment.

## Not applicable

- Live-mode payment capture, refunds, and production callbacks were not
  performed by design; the hosted approval and cancellation above were isolated
  Chargily Test Mode operations.
- Chargily Pro v1 (`pro.chargily.net`, `X-Authorization`, mobile top-ups, and
  vouchers) is a separate product/API and was not added to or exercised by
  this Pay v2 application.

## Reproduction commands

Final local consistency check (2026-09-22): `bash -n scripts/full-verification.sh`,
source-hygiene, API-contract, `git diff --check`, and the retained Surefire
reports all passed; the latest all-gates aggregate is 1,228 tests, 0 failures,
0 errors, and 0 skips. The previously skipped container phases passed in that
same run.
- Final all-gates integration run (2026-09-22) enabled every Phase 9/10
  environment flag and completed with 1,228 tests, 0 failures, 0 errors, and
  0 skips. The four disposable-container phases passed in the same Maven run.
Offline OWASP Dependency-Check also passed with 0 vulnerable dependencies and
0 vulnerabilities after the Elasticsearch REST client upgrade.
It was rerun against the current worktree after the final verification-artifact
changes: 265 dependencies scanned (152 unique), 0 vulnerabilities found, and
2 documented suppressions.
The deterministic harness smoke run also passed with 3 checks passed
and 3 intentionally skipped because no runtime secrets were supplied.

```bash
./scripts/check-source-hygiene.sh
./scripts/check-api-contract.sh
./scripts/verify-local-integration.sh
python3 scripts/validate-api-docs.py "$APP_BASE_URL/v3/api-docs" docs/API_OPENAPI.md
python3 scripts/verify-api-routes.py
python3 scripts/verify-live-stomp.py
APP_BASE_URL=http://127.0.0.1:8080 \
VERIFY_DB_PASSWORD='<disposable-db-password>' \
./scripts/verify-auth-workflow.py
scripts/chargily-sandbox-smoke.sh
scripts/verify-chargily-local-e2e.sh
```

Use ephemeral environment variables for `CHARGILY_API_KEY`,
`CHARGILY_SECRET_KEY`, `SOUKLAB_ACCESS_TOKEN`, and `CHARGILY_TEST_PLAN_ID`.
Never place their values in this report or in committed files.
