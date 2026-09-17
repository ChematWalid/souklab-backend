# Production Readiness Audit

Audit date: 2026-09-17

This report is based on the current source tree, build configuration, migrations, container files, tests, and local verification commands. It is an engineering readiness review, not a compliance certification.

## Current recommendation

The codebase is internally consistent and testable against the local dependency stack. Production rollout still requires the operational controls listed below, especially a CI/release gate, migration execution policy, and observability.

## Findings

### High priority

| Finding | Evidence | Recommendation |
| --- | --- | --- |
| Production schema changes require deployment discipline | Flyway is bundled and enabled by the production profile, while local development keeps it disabled. | Apply reviewed migrations before startup and document rollback/recovery ownership. |
| No tracked CI/release gate is present | No `.github` workflow or equivalent pipeline is tracked. | Add compile, test, migration, dependency, image, and artifact gates. |
| No application observability surface is configured | No Actuator or Micrometer management endpoint is configured. | Add dependency readiness, metrics, correlation IDs, and operational dashboards. |
| Rate limiting is process-local | Bucket4j state is held in Caffeine caches. | Use a shared limiter or edge gateway when horizontally scaling. |

### Medium priority

| Finding | Evidence | Recommendation |
| --- | --- | --- |
| Data seeding runs on every application startup | `DataSeeder` implements `CommandLineRunner` and also owns first-admin bootstrap. | Separate immutable reference-data migrations from an explicitly enabled bootstrap job; never email a bootstrap password in production. |
| S3 bucket creation is enabled by default | `storage.s3.auto-create-bucket` defaults to `true` in application configuration. | Default this to `false` in production and provision buckets/IAM policies outside the application. |
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

## Verification evidence

- `./mvnw -DskipTests compile` on Java 21: passed.
- Full Compose-backed suite: 828 tests, 0 failures, 0 errors, 0 skipped.
- JaCoCo report: generated successfully; 187 classes analyzed.
- `git diff --check`: passed.
- Production time scan: only `ClockConfig` creates the system clock; all production `now` calls use an injected clock.
- Architecture scans: no controller repository imports and no inline implementation classes.
- Native SonarLint executable/plugin: not installed or configured in this repository, so no native SonarLint result is available; local compiler, test, and source-hygiene checks are the available evidence.

## Next release gate

The next production milestone should add a CI pipeline, formalize migration and rollback ownership, and expose health/readiness/metrics endpoints before any external deployment.
