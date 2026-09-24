# Production release checklist

Local Phase 10 acceptance evidence is recorded in
[`PHASE10_VERIFICATION.md`](dev/PHASE10_VERIFICATION.md). The boxes below remain
release gates and must be checked only when the corresponding production or CI
evidence exists.

The currently deferred production/CI work is documented in
[`PHASE10_DEFERRED_RELEASE_PLAN.md`](dev/PHASE10_DEFERRED_RELEASE_PLAN.md). In
particular, hosted CI/dependency scanning and production backup/restore
evidence are intentionally deferred and must remain unchecked until a future
agent or operator produces the required evidence.

- [ ] Hosted CI is green, including integration tests and dependency scan.
- [ ] Working tree is clean and image is built from the reviewed commit.
- [ ] Flyway migrations reviewed and upgrade path verified.
- [ ] Opt-in `Phase10MariaDbMigrationTest` passes with `PHASE10_MARIADB_INTEGRATION=true`.
- [ ] Fresh Elasticsearch bootstrap profile completed, then normal app schema validation verified.
- [ ] No unresolved critical/high dependency vulnerability.
- [ ] Production env file is secret-backed, mode 600, and absent from Git.
- [ ] Backup artifact exists and is encrypted; latest restore drill is recorded.
- [ ] Daily backup systemd timer is installed, enabled, and has a successful recent run.
- [ ] Restore drill restored MariaDB and validated the encrypted object inventory; application smoke flow results recorded.
- [ ] Immutable image digest recorded.
- [ ] Readiness returns HTTP 200 after deployment.
- [ ] Prometheus scrape is up and critical flows pass.
- [ ] V0-V16 Flyway migrations applied and analytics and catalog admin permissions assigned only to approved administrators.
- [ ] Client favorite artisan endpoints, pessimistic locking, and directory card contact masking verified.
- [ ] Analytics job owner isolation, financial permission split, CSV download, and `/admin/stats` alias verified.
- [ ] Activity-event retention and bounded backfill/rollup evidence recorded before enabling historical exports.
- [ ] RabbitMQ analytics exchange/queue/DLQ delivery, persisted retry backoff, publisher confirms, and duplicate-event idempotency verified with the feature enabled.
- [ ] Private `/v3/api-docs` and Swagger UI authenticated access verified.
- [ ] Per-IP and authenticated per-user rate-limit behavior verified across application instances.
- [x] Credential-free local Chargily provider and bounded real sandbox checkout smoke script are repository-owned; real credentials/provider availability remain explicitly classified by the smoke script.
