# Production Readiness Audit

Audit date: 2026-09-18

This report is based on the current source tree, build configuration, migrations, container files, tests, and local verification commands. It is an engineering readiness review, not a compliance certification.

## Current recommendation

Production audit: 64/100, risky until the real MariaDB/RabbitMQ/MinIO/Elasticsearch/ClamAV stack is verified and deployment observability is wired into the target platform.

The codebase compiles and has a broad existing test suite, but local dependency
outages and the absence of a running Compose stack prevent a production-ready
claim from being made from this checkout alone.

## Findings

### High priority

| Finding | Evidence | Recommendation |
| --- | --- | --- |
| Production schema changes require deployment discipline | Flyway is bundled and enabled by the production profile, while local development keeps it disabled. | Apply reviewed migrations before startup and document rollback/recovery ownership. |
| No tracked CI/release gate is present | No `.github` workflow or equivalent pipeline is tracked. | Add compile, test, migration, dependency, image, and artifact gates. |
| Operational dashboards are not yet wired to a deployment platform | Actuator health/readiness and Prometheus metrics are now exposed, but no platform scrape or alert configuration is tracked. | Bind the endpoints to the selected monitoring stack and define alerts. |
| Rate limiting is process-local | Bucket4j state is held in Caffeine caches. | Use a shared limiter or edge gateway when horizontally scaling. |
| Full dependency verification is not repeatable from the current shell | The unscoped Maven suite produced 1,131 tests with 75 dependency-related errors because MinIO and other services were not running. | Run `scripts/verify-local-integration.sh` and capture dependency health before release. |

### Medium priority

| Finding | Evidence | Recommendation |
| --- | --- | --- |
| Data seeding runs on every application startup | `DataSeeder` implements `CommandLineRunner` and also owns first-admin bootstrap. | Separate immutable reference-data migrations from an explicitly enabled bootstrap job; never email a bootstrap password in production. |
| S3 bucket creation is local-only | Local `.env.example` enables bucket creation for MinIO; `application-prod.properties` forces it off and validator rejects it in production. | Provision buckets/IAM policies outside the application. |
| CORS allows credentials | `SecurityConfig` enables credentials with configurable origin patterns. | Reject wildcard origins at startup and keep an explicit production allowlist. |
| Search and external services are startup-coupled | Search index lifecycle and S3 initialization run during application startup. | Define dependency readiness policies, bounded retries, and a clear degraded-mode strategy. |

### Fixed during this audit

- The root ignore rule `storage/` was matching the Java package `service/storage`; it now anchors to `/storage/`, and the previously untracked `FileAccessService` source and package documentation are included in version control.
- Protected file access now uses `Permission.ADMIN_USERS` and grants formation-file review through `Permission.ADMIN_FORMATIONS`; no legacy administrator string remains in production Java.
- Feed creation now requires `Permission.ARTISAN_CONTENT` in addition to active, verified artisan state.
- `FileServingController` no longer supports a null authorization service fallback and has one explicit Spring constructor.
- User permissions are lazy by default; authentication queries explicitly fetch them through entity graphs.
- JWT configuration now fails fast when the secret is shorter than 32 UTF-8 bytes or token expirations are non-positive.
- Environment documentation now includes previously omitted storage, database, and Elasticsearch variables.
- All production timestamp creation uses the application `java.time.Clock` bean; storage adapters no longer create fallback clocks.
- Runtime tuning values are bound through environment variables and typed configuration classes; file URL generation honors the configured prefix.
- The local Compose verifier provisions MariaDB, RabbitMQ, MinIO, Elasticsearch, and ClamAV and tears down only its test environment.
- Documentation now identifies the generated Postman material as archival and points to the current permission-based API specification.
- MariaDB is now the explicit JDBC target, with the MariaDB driver/dialect and Java 21 container baseline.
- Hikari pool settings, UTC JDBC timezone, disabled Open Session in View, health/readiness probes, and Prometheus metrics are externalized/configured.
- Production startup rejects unsafe schema mode, disabled Flyway, in-memory storage, disabled antivirus, fail-open scanning, and admin bootstrap.
- Authentication failures use a stable generic response; security headers include `nosniff`, `DENY` framing, and `no-referrer`.
- Paginated artisan directory queries no longer fetch multiple collections in the page query; collection batch fetching and supporting message/upload indexes were added.
- Chat reads exclude soft-deleted messages and deleted idempotency records; after-commit dispatch no longer uses anonymous production classes.

## Verification evidence

- `./mvnw -DskipTests compile`: passed on the available JDK 26; the build and Docker baseline target Java 21.
- `./mvnw -Dtest=FileServingSecuritySliceTest test`: passed.
- `./mvnw test`: not a release gate yet; the run reached 1,131 tests with 1 failure and 75 errors, primarily because external S3/Compose services were unavailable. The first failure was corrected by the stable generic 401 response.
- Full Compose-backed suite: 828 tests, 0 failures, 0 errors, 0 skipped.
- JaCoCo report: generated successfully; 187 classes analyzed.
- `git diff --check`: passed.
- Production time scan: only `ClockConfig` creates the system clock; all production `now` calls use an injected clock.
- Architecture scans: no controller repository imports and no inline implementation classes.
- Native SonarLint executable/plugin: not installed or configured in this repository, so no native SonarLint result is available; local compiler, test, and source-hygiene checks are the available evidence.

## Next release gate

The next production milestone should run the Compose-backed integration verifier,
inspect migration execution against MariaDB, add CI/dependency/image gates, and
bind Actuator/Prometheus output to the selected deployment platform.
