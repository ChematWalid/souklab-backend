# Step-by-Step Implementation Roadmap

This roadmap breaks down the development of the **Souklab** production Spring Boot backend into atomic, verifiable steps.

> [!IMPORTANT]
> **Strategic Execution Sequence for Upcoming Phases**:
> **Phase 3 (Catalog & Heritage Taxonomy)** ➔ **Phase 4 (Artisan Profile Taxonomy Wiring & Gallery)** ➔ **Phase 5 (Elasticsearch Public Directory Search)**.
> 
> *Architectural Rationale*: Phase 5 public directory multi-facet search depends directly on reference taxonomy entities (Wilayas, Craft Categories, Materials, Eras, Techniques) and their join tables (`artisan_materials`, `artisan_techniques`, `artisan_epoques`), which must be established in Phase 3 before wiring into artisan profile queries in Phase 4.

---

## 📍 Phase 1: Build Infrastructure & Core Foundation (COMPLETED)
- [x] **Step 1.1**: Clean up and optimize `pom.xml` (Spring Boot 4.0.x, Java 21, Spring Data JPA, Spring Security, MySQL Connector, JJWT, Bucket4j / Caffeine rate limiting, ClamAV antivirus client, Jakarta Validation).
- [x] **Step 1.2**: Configure `application.properties` and environment-driven properties (Datasource connection pool, Hibernate DDL, JWT secret, CORS policies, AppProperties hierarchy).
- [x] **Step 1.3**: Implement Core Utilities & Foundation (`BaseEntity`, `ApiResponse<T>`, `PaginatedResponse<T>`, `AppException`, `GlobalExceptionHandler`).
- [x] **Step 1.4**: Compile and verify base setup with `./mvnw clean compile`.

---

## 📍 Phase 2: Security, Authentication & Identity Management (COMPLETED)
- [x] **Step 2.1**: Implement `User`, `AuthorizationPermission`, `RefreshToken`, and `VerificationToken` JPA entities and repositories.
- [x] **Step 2.2**: Implement `JwtUtils` (deterministic with injected `Clock`), `JwtAuthenticationFilter`, `RateLimitFilter` (Caffeine bounded cache), and `CustomUserDetailsService`.
- [x] **Step 2.3**: Configure `SecurityConfig` (stateless session, route whitelisting, CORS bean, method security with standard 403 error envelopes).
- [x] **Step 2.4**: Implement `AuthService` and `AuthController` (`/api/v1/auth/register`, `/login`, `/refresh`, `/logout`, `/verify-email`, `/resend-verification`, `/forgot-password`, `/reset-password`, `/change-password`, `/me`, `PATCH /me`, `/complete-profile`).
- [x] **Step 2.5**: Write comprehensive unit, slice, and integration tests for authentication workflows (AuthControllerTest, AuthServiceTest, JwtUtilsTest, RefreshTokenServiceTest).

---

## 📍 Phase D: Dedicated File Storage & Avatar Pipeline (COMPLETED)
- [x] **Step D.1**: Build pluggable `StorageService` abstraction with `StorageResource`, in-memory stub provider, and S3/MinIO compatible provider (`S3StorageService`).
- [x] **Step D.2**: Implement ClamAV daemon antivirus stream scanning (`ClamdInstreamScanner`) with fail-open/fail-closed configuration guards.
- [x] **Step D.3**: Implement multi-tier avatar image processing (`ThumbnailatorImageProcessingService` and `ResolutionTier`) generating `original` (max 2000px), `medium` (max 500px), and `thumbnail` (max 150px) variants.
- [x] **Step D.4**: Implement `UserAvatar` entity, gallery management (10-avatar quota, list, activate, delete), and `AvatarController`.
- [x] **Step D.5**: Implement streaming file serving endpoint (`GET /api/v1/files/{key}`) with immutable HTTP caching and dedicated avatar rate-limiting (`AvatarUploadRateLimitFilter`).

---

## 📍 Phase 3: Catalog, Taxonomy & Heritage Reference Data (COMPLETED)
- [x] **Step 3.1**: Create JPA entities: `Region` (hierarchical wilayas/communes with self-referencing parent-child), `JobCategory`, `JobSubCategory`, `MaterialFamily`, `Material`, `Epoque`, `Technique` with unique slug constraints and display order weights.
- [x] **Step 3.2**: Create Spring Data repositories with caching annotations for high-read, low-write taxonomy queries.
- [x] **Step 3.3**: Implement `CatalogService` & `CatalogController` (`/api/v1/catalog/**`) for public reference discovery.
- [x] **Step 3.4**: Extend `DataSeeder` with complete official 58 Algerian wilayas, traditional craft categories, material families, and historical periods.

---

## 📍 Phase 4: Artisan & Client Profiles (COMPLETED)
- [x] **Step 4.1a**: Implement base `Artisan` and `Client` JPA entities linked to `User`.
- [x] **Step 4.2a**: Implement `/api/v1/auth/complete-profile` for artisans (bio, city, address, website).
- [x] **Step 4.3a**: Implement `ArtisanController` (`GET /api/v1/artisan/{id}`) with deduplicated profile view tracking and premium-gated contact info masking.
- [x] **Step 4.1b**: Implement `ArtisanGalleryImage`, `ArtisanCertification`, `ArtisanAchievement`, `ArtisanSocialLink`, and join tables (`artisan_materials`, `artisan_techniques`, `artisan_epoques`).
- [x] **Step 4.2b**: Implement multi-step `/api/v1/auth/complete-profile` for clients (client type, company name).
- [x] **Step 4.3b**: Implement `ArtisanService` portfolio and certification management endpoints.

---

## 📍 Phase 5: Elasticsearch Indexing & Public Directory Search (COMPLETED)
- [x] **Step 5.1**: Add Hibernate Search annotations (`@Indexed`, `@FullTextField`, `@KeywordField`) on `Artisan` aggregate root and linked taxonomy entities; configure Elasticsearch 8 analyzers with edge n-grams and ASCII folding; configure asynchronous `SearchIndexInitializer` runner.
- [x] **Step 5.2**: Implement `DirectorySearchService` using Hibernate Search 8 DSL with adaptive fuzziness, bitset-cached facet filters, and resilient relational JPA Criteria fallback; implement `DirectoryController` (`GET /api/v1/public/directory`).
- [x] **Step 5.3**: Live database integration test suite (`DirectoryIntegrationTest`), Postman Batch 7 collection suite (requests 11.1 through 11.9), and complete API reference documentation.
- [x] **Step 5.4**: Verified contact data masking logic for free vs. premium client access in profile and directory card contracts.

---

## 📍 Phase 6: Formations (Workshops / Masterclasses) (COMPLETED)
- [x] **Step 6.0**: **Formateur Governance Subsystem (COMPLETED)**:
  - Artisan teacher eligibility (`isTeacher` flag, application submission `POST /api/v1/artisan/formateur-request`).
  - Formateur state machine (`PENDING`, `APPROVED`, `REJECTED`).
  - 14-day reapplication cooldown enforcement and permanent block flag (`canReapply = false`).
  - Admin governance endpoints (`GET /api/v1/admin/formateur-requests`, approve, reject, lift cooldown, direct grant, direct revoke).
- [x] **Step 6.1**: Implement `Formation`, `FormationFile`, `FormationEnrollment`, `FormationReview` entities, configuration pipeline, and repository layer.
- [x] **Step 6.2**: Implement formation authoring pipeline, media uploads via `StorageService` with compensating rollback, admin review moderation, and dual notification integration.
- [x] **Step 6.3**: Implement peer artisan workshop discovery, enrollment with capacity bounds, cancellation deadline cutoff (24h), protected course file downloads, and enrollment history.
- [x] **Step 6.4**: End-to-end integration test suite (`FormationIntegrationTest`), Postman Batch 8 (16 requests covering authoring, review, enrollment, protected files, and client boundary guards), and comprehensive API reference documentation.

---

## 📍 Phase 7: Social Feed, Reviews & Moderation
- [x] **Step 7.1**: Implement `FeedPost` (Actualité, Formation, Annonce) with moderated multi-image attachments.
- [x] **Step 7.2**: Implement decimal `ArtisanReview` ratings for attended formations with automatic artisan aggregate recalculation.
- [x] **Step 7.3**: Implement `ContentReport` workflow for users, posts, and reviews with explicit administrator resolution actions.

---

## 📍 Phase 8: Realtime Direct Messaging & Notifications
- [x] **Step 8.5**: **In-App Notification Engine (COMPLETED)**:
  - Notification persistence store (`Notification` entity & repository).
  - Unread badge counter with atomic decrement semantics (on read, soft-delete, bulk read) and floor-stability.
  - Soft-delete exclusion and query-scoped isolation (404 on cross-user manipulation).
  - Dual-channel delivery via Spring STOMP backed by RabbitMQ external broker relay with deferred push (`TransactionSynchronizationManager.afterCommit()`).
- [x] **Step 8.1**: Configure Spring STOMP WebSocket chat channels (`/ws`, `/topic`, `/queue`, `/app`, `/user`).
- [x] **Step 8.2**: Implement `WebSocketAuthInterceptor` for messaging channels.
- [x] **Step 8.3**: Implement private one-to-one `Conversation`, `ConversationParticipant`, `Message`, and attachment persistence with REST history and lifecycle APIs.
- [x] **Step 8.4**: Implement message service behavior with idempotent sends, read-up-to receipts, edits, soft deletes, typing events, presence tracking, and realtime STOMP dispatching.

---

## 📍 Phase 9: Monetization, Subscriptions & Chargily Pay V2
- [ ] **Step 9.1**: Implement `SubscriptionPricing`, `ArtisanSubscription`, `ClientSubscription`, `Payment`, `PaymentWebhookLog`.
- [ ] **Step 9.2**: Re-enable `WebClientConfig` and build `ChargilyClient` for checkout session creation.
- [ ] **Step 9.3**: Implement secure webhook handler (`POST /api/v1/subscription/webhook`) with HMAC-SHA256 signature verification and idempotency.
- [ ] **Step 9.4**: Implement automated subscription lifecycle manager (renewal, expiry cron job).

---

## 📍 Phase 10: Admin Dashboard & Production Hardening
- [x] **Step 10.1**: **User & Formateur Moderation (COMPLETED)**:
  - User listing with default and custom pagination/sorting (`GET /api/v1/admin/users`).
  - Pending user approval queue (`GET /api/v1/admin/users/pending`, `POST /approve`, `POST /approve-bulk`).
  - Permanent ban (`POST /ban`) setting `bannedUntil = null` and login lockout.
  - Temporary timeout (`POST /timeout`) with automatic expiration lapse.
  - Effective `ACTIVE` status resolution in listing and search before user login.
  - Manual unban endpoint (`POST /api/v1/admin/users/{id}/unban`) with conflict guards and `ACCOUNT_REINSTATED` notification.
- [x] **Step 10.4**: **Regression & Integration Suite (IMPLEMENTED)**:
  - Unit and MVC slice tests are present across domain services and controllers.
  - Postman collections document the endpoint verification scenarios.
  - Execute the Maven test profile with a supported JDK and the required external services before release; CI results are the authoritative pass count.
- [ ] **Step 10.2**: Implement platform analytics & KPI aggregation (`/api/v1/admin/stats`).
- [ ] **Step 10.3**: Actuator monitoring, health checks, rate-limit fine-tuning, and Swagger/OpenAPI documentation.
