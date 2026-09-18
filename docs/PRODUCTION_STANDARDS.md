# Production Engineering Standards

Status: approved baseline for the production-readiness audit

## Runtime baseline

- Java 21 is the supported runtime and compiler release.
- Spring Boot remains on the current 4.0.x line for this audit.
- MariaDB is the production database target. The MariaDB JDBC driver and dialect are authoritative.
- Flyway owns production schema changes. Production uses `ddl-auto=validate`; `update` is local-development only.
- Production deployments use UTC for persistence and explicit user-facing timezone conversion.

## Configuration

- `AppProperties` is the root for application policy configuration.
- Dedicated typed infrastructure classes may remain when they are bound and validated as part of the approved configuration surface; `StorageProperties` is the current example.
- Runtime values must come from typed configuration or framework infrastructure binding. Production code must not read `System.getenv`, `Environment`, or `@Value` directly for application policy.
- Production secrets have no usable defaults, are never logged, and are supplied by the deployment environment.
- Optional OAuth, mail, payment, and search features may be disabled explicitly. Production storage, antivirus scanning, RabbitMQ relay, and Elasticsearch are mandatory and fail startup or readiness when unsafe.
- Local `.env.example` values are safe development values only and are not production credentials.

## Code and architecture

- Constructor injection is required; field injection is prohibited.
- Controllers depend on services, and services own authorization and transaction boundaries. Controllers do not access repositories directly.
- API DTOs and persistence entities remain separate.
- Time-dependent code receives the shared `Clock`; persistence timestamps use UTC `Instant` or an explicitly documented compatibility type.
- New production code uses named classes instead of anonymous or inline implementation classes when behavior has lifecycle, error handling, or test significance.
- Production comments are limited to Javadocs for public contracts and non-obvious operational behavior. Stale or narrating comments are removed.
- Protocol constants may remain immutable code constants; operational policy values belong in typed configuration.
- Exceptions are translated at the API boundary without exposing secrets, provider details, SQL, or stack traces. Logs use structured context and exclude credentials and tokens.

## Persistence and transactions

- Read paths use read-only transactions where applicable.
- Long-running work and external calls do not run inside database transactions unless the boundary is deliberate and documented.
- Pageable queries must not fetch multiple collections. Use batch fetching, entity graphs for to-one relationships, or DTO projections.
- Soft-deleted records are excluded by repository query semantics, not by caller convention.
- New high-contention state transitions document their locking or idempotency strategy.
- Every production migration is forward-only, reviewed, and compatible with the supported MariaDB version.

## Security and operations

- Authorization is permission-based and enforced at service boundaries, including ownership and administrative capability checks.
- JWT secrets and external credentials are validated at startup and are never included in responses or logs.
- Uploads are size-, type-, path-, ownership-, and antivirus-validated. Antivirus failure is fail-closed in production.
- Actuator exposes only health/readiness/liveness and metrics required by the deployment platform; health details are not public.
- Production readiness must include database, storage, broker, search, and antivirus dependency policy, plus restart and outage behavior.
- Horizontal deployments require shared rate-limit state or an edge control; process-local caches are not a cluster-wide security boundary.

## Change policy

- Production fixes are committed in reviewable batches.
- Existing tests may be adjusted only for compatibility with a production behavior change during this audit. New test-hardening work is deferred to the backlog.
- API compatibility is preserved unless a security or correctness defect requires a contract change, which must be documented.
