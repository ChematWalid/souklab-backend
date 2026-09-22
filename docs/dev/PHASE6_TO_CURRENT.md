# SoukLab backend: Phase 6 to current

This document summarizes the work delivered from Phase 6 through the current security and production-hardening work. The source code, migration scripts, API documentation, and tests remain the authoritative implementation references.

## Timeline

| Period | Scope | Result |
| --- | --- | --- |
| Phase 6 | Formateur governance and formations | Completed |
| Phase 7 | Social feed, artisan reviews, and content moderation | Completed |
| Phase 8 | In-app notifications, direct messaging, and WebSocket delivery | Completed |
| Post-Phase 8 | Authorization migration, production audit, and hardening | Implemented through commit `32de354` |

## Phase 6: Formateur governance and formations

### Formateur governance

- Added artisan teacher eligibility through the `isTeacher` state.
- Added the formateur request lifecycle: `PENDING`, `APPROVED`, and `REJECTED`.
- Added the artisan request endpoint and administrator governance endpoints.
- Added a configurable reapplication cooldown and permanent ineligibility through `canReapply = false`.
- Added direct administrator grant and revoke operations.
- Added notifications and email communication for governance events.
- Replaced ambient time calls with an injected `Clock`, making cooldown behavior deterministic and testable.

### Formation domain

- Added `Formation`, `FormationFile`, `FormationEnrollment`, and `FormationReview` entities and repositories.
- Added formation authoring, update, submission, moderation, approval, rejection, and soft-delete workflows.
- Added thumbnail and course-file uploads through the provider-independent `StorageService` abstraction.
- Added MIME validation, size limits, antivirus scanning hooks, quota enforcement, and compensating storage deletion when database persistence fails.
- Added peer-artisan catalog discovery and formation detail views.
- Added enrollment capacity enforcement, author self-enrollment protection, enrollment history, and cancellation cutoffs.
- Added protected course-file downloads with author, enrolled-participant, and administrator access rules.
- Added dual notification delivery for relevant formation and governance events.

### Phase 6 verification and documentation

- Added the end-to-end `FormationIntegrationTest` suite.
- Added Postman Batch 8 coverage for authoring, moderation, enrollment, protected files, and client-boundary checks.
- Updated the API specification, roadmap, data model, architecture notes, and package READMEs.

## Phase 7: Social feed, reviews, and moderation

### Social feed

- Added `FeedPost`, `FeedPostMedia`, and their repositories and DTOs.
- Added post types for `ACTUALITE`, `FORMATION`, and `ANNONCE`.
- Added authoring, update, deletion, publication, hiding, removal, and moderation workflows.
- Added multi-image attachments with file validation, storage coordination, media ordering, and cleanup behavior.
- Added public feed listing and detail endpoints with filtering and pagination.
- Added administrator moderation queues and actions.
- Added author ownership checks and verified-artisan/account-state checks for artisan content.

### Reviews

- Added `ArtisanReview`, review status handling, DTOs, repository support, and API endpoints.
- Restricted review creation to the appropriate completed/attended formation workflow.
- Enforced one review per eligible enrollment and ownership checks for update/delete operations.
- Added decimal ratings from `0.00` to `5.00`.
- Added automatic artisan rating and review-count recalculation after review changes and moderation.
- Added public visible-review browsing.

### Content reports

- Added `ContentReport`, report target types, statuses, resolution actions, DTOs, and repositories.
- Added reporting for users, feed posts, and reviews.
- Added administrator report queues and explicit `DISMISS`, `HIDE`, and `REMOVE` resolution actions.
- Added target validation, duplicate/invalid report handling, moderation side effects, and auditability.

### Phase 7 verification and documentation

- Added controller, service, repository, and integration coverage for feed, review, and report workflows.
- Added notification coverage for new reviews and new reports.
- Added the Phase 7 schema migration artifact for social tables.
- Updated API, roadmap, notification, data-model, architecture, Postman, and package-level documentation.

## Phase 8: Direct messaging, notifications, and realtime delivery

- Added the persisted `Notification` entity and repository.
- Added user-scoped notification feeds, unread counts, mark-read operations, bulk mark-read behavior, and soft deletion.
- Enforced query-level user ownership so cross-user notification operations return the appropriate not-found behavior.
- Added atomic unread-count handling with floor stability.
- Added WebSocket/STOMP delivery and deferred push after successful transaction commit.
- Added support for externally configured STOMP/RabbitMQ relay destinations.
- Added WebSocket authentication interception for bearer-token messaging connections.
- Added private one-to-one conversations, participants, messages, attachments, REST history, cursor pagination, idempotent sends, edits, soft deletes, read-up-to receipts, typing events, and presence tracking.
- Added message authorization, attachment upload tracking, and after-commit realtime events for direct messages.

## Authorization migration

Authorization was migrated from role-oriented/string checks to database-backed capabilities.

### Permission model

The persisted permission set currently contains 12 capabilities:

1. `permission:admin:users`
2. `permission:admin:formations`
3. `permission:admin:feed`
4. `permission:admin:reports`
5. `permission:artisan:formations`
6. `permission:artisan:content`
7. `permission:artisan:reviews`
8. `permission:profile:read`
9. `permission:profile:write`
10. `permission:report:create`
11. `permission:file:read`
12. `permission:message:send`

`Permission` is the type-safe Java source of truth, while the `permissions` and `user_permissions` tables are the database source of truth. Account type is onboarding metadata used to select initial permissions; it is not itself an authorization authority.

### Enforcement rules

- `AccessControlService` centralizes coarse capability predicates used by method security.
- Domain services retain ownership, account-status, email-verification, workflow-state, enrollment, and moderation checks.
- Formation operations use `ARTISAN_FORMATIONS`; gallery, certifications, formateur requests, and eligible feed creation use `ARTISAN_CONTENT`; review mutations use `ARTISAN_REVIEWS`.
- Profile reads and writes use `PROFILE_READ` and `PROFILE_WRITE`.
- Report submission uses `REPORT_CREATE`; administrator report actions use `ADMIN_REPORTS`.
- Protected file access requires `FILE_READ` and then applies ownership, enrollment, or administrator rules.
- Administrator capabilities are intentionally separate, so possessing one administrative capability does not imply all other administrative capabilities unless the user is explicitly seeded or assigned those capabilities.
- Permission loading uses repository/entity-graph paths so lazy permission relationships are available during authentication.
- Legacy `ROLE_*`, `hasRole(...)`, role repositories, and role-string compatibility authorities are not part of the authorization contract.

The complete endpoint/service mapping is maintained in [`AUTHORIZATION_MATRIX.md`](../AUTHORIZATION_MATRIX.md).

## Configuration and runtime policy

Runtime and business policy values now follow this flow:

```text
.env / deployment environment
        ↓
application.properties
        ↓
AppProperties / StorageProperties / dedicated configuration types
        ↓
services and filters
```

The following policies were moved out of duplicated Java defaults and into configuration:

- authentication lockout and verification-code limits;
- JWT token lifetimes;
- administrator bootstrap and operational reasons;
- OAuth cookie lifetime;
- artisan gallery and certification quotas, file sizes, and MIME lists;
- formation file and thumbnail policies;
- pagination defaults and maximum page size;
- multipart upload limits;
- storage rate limits and cache policies;
- storage provider, S3 settings, virus-scanning settings, and bucket creation policy;
- CORS origins and credentials behavior;
- search and indexing operational settings.

Protocol and implementation invariants remain in code, including route paths, cache names, JWT claim names, permission authority format, migration identifiers, MIME field identifiers, and algorithm constants.

### Configuration validation

`ConfigurationPolicyValidator` now fails fast for invalid policy combinations, including:

- non-positive limits and durations;
- empty or invalid MIME allowlists;
- invalid avatar and storage quotas;
- wildcard CORS origins combined with credentials;
- unsafe production S3 bucket auto-creation;
- invalid production storage configuration.

`StorageConfiguration` also requires S3 credentials, bucket, and region when the S3 provider is selected. No Java fallback silently replaces a missing configured region.

## Storage and file-serving hardening

- Removed the fail-open `FileServingController` behavior that treated a missing authorization service as authorized.
- Kept one explicit constructor for file serving.
- Preserved public/private cache semantics while requiring authentication at the file route.
- Added `FILE_READ` enforcement for protected certification and formation files.
- Kept public media discoverable through metadata while applying ownership/enrollment rules to private objects.
- Preserved storage rollback behavior after database failures.
- Kept storage providers behind the `StorageService` interface with in-memory and S3-compatible implementations.

## Service boundaries

- Refactored `AvatarController` to depend only on `AvatarService` and transport concerns.
- Added `CurrentUserProvider` so principal lookup and user-entity resolution are owned by the service layer.
- Moved avatar authentication lookup, upload validation, quota checks, persistence, storage coordination, activation, listing, and deletion behind service methods.
- Refactored logout so authenticated-user resolution occurs in `AuthService`, not in the controller.
- Scanned controllers for direct repository, entity-manager, storage-properties, and authenticated-user access; no persistence bypass remains in the controller packages.

## Database migrations and seeding

- Added Flyway dependencies and versioned migration resources.
- Added `V1__phase7_social.sql` for feed, media, reviews, and reports.
- Added `V2__authorization_permissions.sql` for direct permissions and user-permission links.
- Kept Flyway disabled by default for local development and enabled it in the production profile.
- Kept production Hibernate schema handling in validation mode so schema drift is detected rather than silently altered.
- Separated reference permission seeding from privileged administrator bootstrap.
- Gated admin bootstrap behind `app.admin.bootstrap-enabled`; production defaults do not require admin bootstrap credentials on every startup.

Migration details and operational constraints are documented in [`migrations/README.md`](migrations/README.md).

## Documentation updates

The documentation was reconciled with the current source across:

- root README and project structure;
- API conventions and API specification;
- architecture, data model, roadmap, notifications, and production audit;
- authorization matrix;
- migration documentation;
- configuration, security, storage, authentication, user, profile, formation, feed, review, report, controller, DTO, DAO, model, and service package READMEs.

Package README inventories were rescanned against the Java source so implemented classes are represented and stale role-oriented descriptions were corrected.

## Verification performed

### Test-verifiability audit

The runtime investigation recorded the following environment and dependency
facts:

- The default shell runtime is OpenJDK `26.0.2.1` on Arch Linux; Maven Wrapper
  is Apache Maven `3.9.15` and the project compiles with `--release 17`.
- Spring Boot dependency management resolves Mockito `5.20.0` and Byte Buddy
  `1.17.8`.
- The project uses `mockito-core`; it does not declare `mockito-inline`.
- The initial JDK 26 run reproduced the runtime incompatibility through JaCoCo
  instrumentation of JDK 26 class-file versions. The historical Mockito
  self-attachment failure was not reproducible in the current checkout because
  the explicit agent was already configured; the run produced no Mockito
  self-attachment error.
- The chosen minimal build fix is the Mockito 5 javaagent in Surefire's
  `argLine`, combined with JDK 21 for the supported reproducible test runtime.
  This keeps production code unchanged. JDK 21 removes the JaCoCo class-file
  version errors and Mockito-backed tests execute normally.

The current test inventory contains 124 Surefire report files after the full run.
Forty-three test source files use Mockito annotations or APIs. Twenty-six source
files exercise persisted permission authorities or permission-based fixtures. Pure
tests include DTO/model/configuration and storage-validation checks; Mockito-
backed tests include service, controller, filter, security, and mapper tests.

The authorization migration commit was `09d325c`. It removed 6,082 test lines
across 94 files. The only test class deleted outright was the obsolete
`RolePermissionMapperTest`; the role-specific portions of other tests were
replaced with permission capabilities. The following business-logic suites were
therefore restored and expanded from the pre-migration assertion source:

- `AuthServiceTest` — 2,211 lines removed;
- `ProfileServiceTest` — 1,435 lines removed;
- `UserManagementServiceTest` — 769 lines removed;
- `ArtisanProfileServiceTest` — 438 lines removed;
- `ProfileResponseMapperTest` — 359 lines removed;
- `UserRepositoryTest` — 341 lines removed;
- `CustomUserDetailsServiceTest` — 107 lines removed.

The deleted business behavior has been restored in the seven named suites above,
with obsolete role-only assertions removed and permission-combination,
inactive-user, ownership-boundary, administrator-capability, and lazy-loading
regressions added. `RolePermissionMapperTest` remains deleted because it tested
removed protocol behavior.

Successful checks included:

- Maven main-source compilation;
- focused configuration-property tests;
- focused avatar and authentication controller tests;
- file-serving controller and security-slice tests;
- formateur permission-based controller tests;
- artisan gallery and certification service tests;
- `git diff --check`;
- controller scan for direct persistence access;
- scan for explicit hardcoded pageable sizes;
- environment-placeholder versus `.env.example` reconciliation.
- focused configuration, formation, verification-token, feed, artisan, formateur,
  and administrative-formation tests: 128 tests, 0 failures, 0 errors;
- test-fixture reconciliation after policy externalization: formation defaults,
  verification-token limits, formateur cooldowns, file-rate-limit cache settings,
  and default pagination now receive explicit configured values in tests;
- permission-message assertions updated to the canonical `Permission.authority()`
  values.

The complete clean Maven test run was executed with the Maven Wrapper, configured
Mockito javaagent, and the isolated Phase 8 Compose environment. The current run
covered the restored authorization/business-logic suites, messaging suites, full
application contexts, repository slices, and dependency verification tests:

```text
Tests run: 1085, Failures: 0, Errors: 0, Skipped: 0
```

The same run verified MariaDB-backed application/repository contexts, RabbitMQ
STOMP connectivity, MinIO/S3 behavior, Elasticsearch-backed startup readiness,
and ClamAV clean/infected scanning. The run completed with no failures, errors,
or skipped tests. Expected framework/deprecation warnings remain documented in
the verification logs; they are not test failures.

The latest clean JaCoCo report records 98.10% instruction, 98.41% line, 88.63%
branch, 98.02% method, and 100% class coverage. Class coverage is complete, but
the requested 100% line/branch coverage is not yet achieved; uncovered executable
paths remain and must not be represented as complete coverage.

The coverage recovery consists of restored pre-migration business behavior in
authentication, profiles, user management, artisan profiles, response mapping,
repositories, and security-detail loading; new permission-combination,
ownership-boundary, inactive-account, administrator-boundary, and lazy-loading
regressions; and Phase 8 notification/messaging/realtime behavior. The deleted
`RolePermissionMapperTest` remains intentionally absent because it tested the
removed role protocol rather than current business behavior.

The isolated verification also proved the real MariaDB, RabbitMQ/STOMP, MinIO/S3,
Elasticsearch, and ClamAV paths. ClamAV was tested with both a clean payload and
the EICAR signature. The verification script removed only its own containers,
network, and named volumes after completion.

## Current repository state

- Branch: `main`
- Latest commit: `32de354 refactor(security): harden configuration and permissions`
- Remote: `origin/main`
- The current Phase 8 implementation, test, and documentation edits are
  intentionally uncommitted.
- GitHub reported one dependency vulnerability on the default branch after the push; dependency review should be handled as a separate release-security task.

## Remaining roadmap and release work

The following are not claimed as complete by this document:

- a full CI pipeline with compilation, tests, migrations, formatting/static analysis, dependency scanning, and configuration validation;
- health/readiness/metrics endpoints and external dependency dashboards;
- shared distributed rate limiting for horizontally scaled deployments;
- a fully supported JDK/Maven matrix for all integration tests;
- final remediation of the GitHub-reported dependency vulnerability;
- raising line and branch coverage to the requested 100% with behavior-level
  assertions rather than coverage exclusions;
- production CI wiring for the same isolated dependency verification.
