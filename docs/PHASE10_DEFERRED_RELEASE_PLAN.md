# Phase 10 Deferred Release Work Plan

## Purpose

This document is the handoff plan for the Phase 10 work that is intentionally
deferred from the current implementation pass. The backend implementation and
local Docker-backed acceptance work remain the baseline. A follow-up agent or
operator must execute this plan in an environment with the required CI,
staging/production, observability, and backup access.

The current implementation must not be declared production-approved merely
because the local test suite is green. Each item below needs the evidence
specified in this document before its corresponding release-checklist item is
marked complete.

## Explicitly deferred by the project owner

The following work is deferred for now and is not a blocker for the current
backend implementation handoff:

1. Hosted CI completion and the hosted dependency/security scan, including
   confirmation that the remaining Dependabot/dependency alert is cleared.
2. Production backup and restore evidence, including the encrypted backup
   artifact, systemd timer, restore drill, object inventory validation, and
   application smoke-flow record.

The remaining sections document the complete future release-readiness plan so
the next agent can execute all deferred work in one controlled pass.

## Current baseline

Before starting, the follow-up agent must confirm:

- the working tree and current branch are recorded;
- the reviewed commit is identified and pushed to the intended remote;
- `./scripts/check-source-hygiene.sh` passes;
- `./mvnw -q -DskipTests compile` passes;
- the local verification evidence in
  [`PHASE10_VERIFICATION.md`](PHASE10_VERIFICATION.md) is still accurate;
- no production secret, token, provider payload, or environment file is added
  to Git.

The existing enum/taxonomy rules remain mandatory during any follow-up fixes:

- import types at the top of Java files; do not use inline fully-qualified
  class names;
- use nested enums for meaningful compound event, permission, metric, audit,
  and filter taxonomy paths;
- do not introduce new inline or anonymous implementation classes;
- keep enum values at framework and persistence boundaries only where a JSON,
  SQL, Redis, message, or legacy wire representation requires a string;
- preserve existing persisted/API wire values unless a versioned migration and
  compatibility decision is approved.

## Work packages

### 1. Hosted CI and dependency security

This package is explicitly deferred and requires access to the repository's
hosted CI and security-alert systems.

#### Actions

1. Run the hosted workflow in
   [`.github/workflows/production-verification.yml`](../.github/workflows/production-verification.yml)
   from the reviewed commit.
2. Confirm compilation, source hygiene, Postman checks, production-manifest
   validation, Docker-backed integration verification, and image build all
   pass.
3. Run the configured OWASP dependency scan with the repository's approved NVD
   configuration. Do not suppress a finding without documenting the exact
   dependency, advisory, version, rationale, and expiry. The 2026-09-21 local
   run with Dependency-Check 13.0.0 and `-DfailBuildOnCVSS=7` failed on
   unresolved findings, including broad CPE matches for BOM-managed components.
   Treat this as an open release gate until each result is validated against
   the vendor advisory and a patched compatible version is selected or a
   reviewed false-positive record is approved.
4. Confirm `org.apache.tika:tika-core` resolves to `3.2.2` or newer and inspect
   the complete dependency tree for affected Tika modules, especially PDF
   parser modules.
5. Re-check the hosted Dependabot/security-alert page after the scan and record
   the alert identifier, resolved version, and resolution timestamp.
6. If a different critical/high finding remains, fix it in a separate coherent
   dependency commit, run the focused storage/document-processing tests, then
   rerun the full verification workflow.

#### Evidence to record

- hosted workflow URL and commit SHA;
- job names and pass status;
- dependency-scan report artifact or immutable report URL;
- dependency tree showing patched Tika version;
- remaining-alert status, if any, with an explicit owner and follow-up issue.

Do not mark “No unresolved critical/high dependency vulnerability” complete
from a local compile alone.

### 2. Production configuration and deployment identity

These checks establish that the deployed application is the reviewed artifact
and is configured safely.

#### Actions

1. Build the production image from the reviewed commit using the approved CI
   workflow or release builder.
2. Scan the image and record its immutable digest. Do not use a mutable tag as
   the release identity.
3. Verify the production environment file is secret-backed, has mode `600`, is
   absent from Git, and contains no test/default credentials.
4. Validate all required Phase 10 configuration values through the external
   environment-to-`application.properties`-to-typed-properties chain:
   analytics limits, job retention, rollup policy, RabbitMQ topology, OpenAPI
   policy, health policy, storage, and per-IP/per-user rate limits.
5. Run the configuration/startup validation with production-like values and
   capture failures for intentionally invalid values as a negative test.
6. Deploy the immutable image to the approved staging or production target.

#### Evidence to record

- reviewed commit SHA;
- image digest and registry location;
- redacted configuration validation output;
- proof of environment-file permissions and Git exclusion without exposing
  values;
- deployment identifier and timestamp.

### 3. Flyway upgrade and database acceptance

#### Actions

1. Run the forward migration path from a V5 schema through every Phase 10
   migration on a disposable MariaDB instance.
2. Run the opt-in
   `Phase10MariaDbMigrationTest` with
   `PHASE10_MARIADB_INTEGRATION=true` against the supported MariaDB version.
3. Test upgrade behavior on a representative database containing existing
   business records; confirm no destructive migration or invented historical
   activity events occur.
4. Verify the analytics permission is assigned only to approved administrators.
5. Record migration checksums and the resulting schema version.

#### Evidence to record

- migration command and sanitized output;
- database version and schema history;
- test report;
- permission-assignment query/result with user identifiers redacted as needed;
- explicit confirmation that the migration is forward-only and reversible only
  through the approved recovery procedure.

### 4. Elasticsearch bootstrap and normal schema validation

#### Actions

1. Start a fresh Elasticsearch data directory with the documented bootstrap
   profile.
2. Confirm the configured version, analyzer setup, index creation, and health
   indicator behavior.
3. Stop the bootstrap profile and start the normal application profile against
   the resulting schema.
4. Verify normal startup, search reads/writes, and readiness behavior.
5. Repeat with Elasticsearch unavailable to confirm the configured optional or
   mandatory readiness policy is respected.

#### Evidence to record

- fresh-bootstrap logs;
- index/analyzer listing;
- normal application startup result;
- readiness responses for available and unavailable Elasticsearch;
- no credentials or raw provider payloads in the evidence.

### 5. Production backup, encryption, and restore drill

This package is explicitly deferred and requires access to the production
backup host, encrypted object storage, and a disposable restore environment.

#### Actions

1. Run [`scripts/backup-preflight.sh`](../scripts/backup-preflight.sh) with
   production configuration and verify required tools, directories, secret
   references, storage access, and free space.
2. Run [`scripts/backup-production.sh`](../scripts/backup-production.sh) and
   confirm the MariaDB artifact is encrypted before upload or archival.
3. Validate backup retention and object-storage prefixes; ensure backup names
   do not expose credentials or unnecessary personal data.
4. Verify the daily backup systemd timer is installed, enabled, and points to
   the reviewed script and environment.
5. Execute [`scripts/restore-drill.sh`](../scripts/restore-drill.sh) in an
   isolated environment. Never restore over the live production database for a
   drill.
6. Restore MariaDB, validate Flyway/schema state, and validate the encrypted
   object inventory.
7. Start the application from the restored state and exercise a minimal smoke
   flow: authentication, an authorized analytics job submission/status/result,
   CSV authorization boundary, and a health/readiness check.
8. Record elapsed backup, restore, and recovery times and compare them with
   the operational targets.
9. If the drill fails, preserve the failure logs, open a remediation issue,
   and do not mark the backup/restore gates complete.

#### Evidence to record

- encrypted backup artifact identifier, size, checksum, and creation time;
- proof that the encryption key is externally managed, without recording the
  key;
- systemd timer status and last successful run;
- restore environment identifier;
- schema/data/object-inventory validation results;
- smoke-flow result and recovery times;
- retention cleanup result.

### 6. Deployed health, readiness, and Prometheus

#### Actions

1. Confirm public liveness and readiness probes expose only the intended
   minimal information.
2. Confirm detailed health, Prometheus metrics, OpenAPI JSON, and Swagger UI
   require authentication and the appropriate operator/admin authority.
3. Verify readiness returns HTTP 200 only when all enabled mandatory
   dependencies are healthy.
4. Disable each optional dependency feature one at a time and confirm it does
   not incorrectly fail readiness; enable it and confirm it becomes relevant.
5. Configure Prometheus to scrape the private metrics endpoint and verify the
   scrape target is up.
6. Confirm request, error, rate-limit rejection, analytics queue, and job
   counters are present without leaking credentials or personal payloads.

#### Evidence to record

- authenticated and unauthenticated HTTP results for each endpoint;
- readiness transition timeline during dependency outage/recovery;
- Prometheus target page or API result;
- representative metric names and labels;
- alert/rollback result for a deliberately bounded dependency failure.

### 7. Analytics REST, authorization, exports, and aliases

#### Actions

1. Run the admin API/Postman collection against the deployed application.
2. Verify every report family accepts the configured date range, bucket,
   filters, page size, sort field, and output format constraints.
3. Verify asynchronous job submission responds within the configured budget and
   status/result/download behavior is authenticated.
4. Verify owner isolation: a second administrator cannot inspect, download, or
   delete another administrator's job or artifact.
5. Verify financial report families require both analytics and financial
   permissions; verify non-financial families require only analytics permission.
6. Verify JSON pagination, CSV generation, export retention, and bounded row/
   byte limits.
7. Verify `/api/v1/admin/stats/jobs` is behaviorally equivalent to the
   analytics route alias.
8. Verify audit records contain actor, report, filters, range, job, permission
   scope, outcome, and timestamp without secrets or raw provider payloads.

#### Evidence to record

- sanitized Postman run/report;
- request IDs and job IDs;
- owner-isolation and financial-permission negative responses;
- JSON and CSV response samples with data redacted;
- audit-row verification;
- measured submission/result latency.

### 8. Event delivery, rollups, retention, and backfill

#### Actions

1. Confirm the durable analytics exchange, queue, retry policy, and dead-letter
   queue are present in RabbitMQ.
2. Exercise publisher confirms, broker outage/recovery, retry backoff, and
   dead-letter behavior.
3. Deliver a duplicate activity event and verify consumer idempotency.
4. Run bounded historical backfill only for metrics supported by historical
   source columns; record that unavailable historical login/interaction events
   were not invented.
5. Run a rollup rebuild twice for the same bounded window and compare results
   for idempotency.
6. Run retention cleanup with a test clock or isolated old records and confirm
   raw events, rollups, jobs, and artifacts use their configured retention
   policies.
7. Verify daily rollup scheduling, batch limits, and failure/retry behavior.

#### Evidence to record

- RabbitMQ topology and queue-depth snapshots;
- publish/consume/duplicate/retry/DLQ results;
- backfill window, source columns, row counts, and audit ID;
- before/after rollup checksums or aggregate comparison;
- cleanup counts and bounded execution time.

### 9. Cross-instance rate limiting

#### Actions

1. Run at least two application instances against the same Redis backend.
2. Verify unauthenticated keys are partitioned by client IP and endpoint class.
3. Verify authenticated keys are partitioned by user and endpoint class, not
   merely by IP.
4. Confirm analytics submission, result/download, CSV, authentication, public,
   admin, and webhook classes use their configured capacities/refill windows.
5. Confirm rejection counters and response envelopes are standardized.
6. Simulate Redis unavailability and verify the configured fallback/fail-closed
   behavior.

#### Evidence to record

- instance identifiers and Redis configuration (without secrets);
- request sequence and status counts;
- Redis key-prefix samples with user identifiers redacted;
- cross-instance enforcement result;
- outage/fallback result.

## Required follow-up commit and push discipline

The follow-up agent must commit related changes in batches, for example:

1. dependency/security remediation;
2. verification-script or test fixes;
3. deployment/health/readiness fixes;
4. analytics authorization/export fixes;
5. documentation and evidence updates.

Each batch must pass the relevant focused tests before it is pushed. Do not
combine unrelated formatting or speculative refactors with a release fix.

## Verification procedure after the follow-up agent finishes

The verifying agent must independently perform the following:

1. Inspect `git status`, commit history, remote branch, and the exact reviewed
   SHA.
2. Review every changed file and confirm no secrets, inline fully-qualified
   names, new anonymous implementation classes, or unjustified flat compound
   enum constants were introduced.
3. Run:

   ```text
   ./scripts/check-source-hygiene.sh
   ./mvnw -q -DskipTests compile
   ./mvnw test
   ```

4. Run the Docker-backed verification harness from the reviewed SHA.
5. Run the relevant opt-in MariaDB, RabbitMQ, Redis, storage, health, OpenAPI,
   and endpoint-security tests with their required environment flags.
6. Validate the deployed endpoint matrix and authorization boundaries manually
   or with the approved Postman collection.
7. Check the dependency tree and hosted security result independently.
8. Inspect backup encryption, timer status, restore output, checksums, and
   smoke-flow evidence independently; do not accept screenshots without the
   underlying command/API result.
9. Reconcile every item in `RELEASE_CHECKLIST.md` against an evidence artifact.
   Mark only proven items as complete. Leave explicitly deferred items
   unchecked and link this document from the checklist.
10. Push only after the verification report is complete and the working tree is
    clean.

## Completion criteria

The deferred release work is complete only when:

- all applicable hosted CI/security checks are green;
- the Tika advisory and any other critical/high findings have a recorded
  resolution;
- the deployed artifact is identified by immutable digest;
- Flyway, Elasticsearch, analytics APIs, authorization, exports, event
  delivery, rollups, retention, health, OpenAPI, Prometheus, and rate limiting
  have environment-appropriate evidence;
- the encrypted backup and restore drill have succeeded;
- every completed checklist item links to evidence;
- any remaining unchecked item has an explicit deferral owner and follow-up
  issue;
- the final verification passes without regressing the enum/taxonomy rules.
