# Production Readiness Audit

Audit date: 2026-09-16

This report is based on the current source tree, build configuration, migrations, container files, tests, and local verification commands. It is an engineering readiness review, not a compliance certification.

## Current recommendation

Do not expose the application to public production traffic yet. The authorization defects found during this pass are fixed, but the release process still lacks automated migration execution, CI gates, a supported test-runtime configuration, and production observability.

## Findings

### High priority

| Finding | Evidence | Recommendation |
| --- | --- | --- |
| Schema changes are manual artifacts | `docs/migrations` contains SQL scripts, while `pom.xml` has no Flyway or Liquibase integration. | Adopt one migration runner, execute migrations in CI/deploy, validate checksums, and document rollback/recovery procedures. |
| Test coverage regressed during authorization migration | The authorization commit removed 6,082 test lines while adding focused replacement tests. | Restore the behavioral tests and mechanically migrate their fixtures to permissions; require coverage thresholds in CI. |
| Runtime tests are not executable on the current JDK setup | Mockito inline mock-maker fails to self-attach on JDK 26; focused pure permission tests pass, Mockito-backed tests fail before assertions. | Standardize CI and local development on a supported JDK, or configure Mockito as an explicit test JVM agent. |
| No repository CI/release gate is present | No `.github` workflow or equivalent pipeline is tracked. | Add compile, test, migration validation, dependency scanning, image scanning, and artifact publication gates. |

### Medium priority

| Finding | Evidence | Recommendation |
| --- | --- | --- |
| No built-in health/readiness/metrics surface | No Actuator, Micrometer, or management endpoint is configured. | Add health/readiness probes, metrics, structured request correlation, and dependency dashboards before rollout. |
| Data seeding runs on every application startup | `DataSeeder` implements `CommandLineRunner` and also owns first-admin bootstrap. | Separate immutable reference-data migrations from an explicitly enabled bootstrap job; never email a bootstrap password in production. |
| Rate limiting is process-local | Bucket4j state is held in Caffeine caches. | Use a shared Redis or edge-gateway limiter for horizontally scaled deployments. |
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

## Verification evidence

- `./mvnw -DskipTests compile`: passed.
- `./mvnw -DskipTests test-compile`: passed.
- Permission and capability-isolation tests: 4 tests passed.
- `git diff --check`: passed.
- Production Java scan for `ROLE_*`, legacy role repositories, and inline project-class references: clean.
- Mockito-backed runtime tests: blocked by JDK 26 agent self-attachment in this environment.

## Next release gate

The next production milestone should restore the removed behavioral tests, add a migration runner and CI pipeline, configure a supported test JDK/Mockito agent, and expose health/readiness/metrics endpoints before any external deployment.
