# Step-by-Step Implementation Roadmap

This roadmap breaks down the development of the **Souklab** production Spring Boot backend into atomic, verifiable steps.

> [!IMPORTANT]
> **Strategic Execution Sequence for Upcoming Phases**:
> **Phase 3 (Catalog & Heritage Taxonomy)** ➔ **Phase 4 (Artisan Profile Taxonomy Wiring & Gallery)** ➔ **Phase 5 (Elasticsearch Public Directory Search)**.
> 
> *Architectural Rationale*: Phase 5 public directory multi-facet search depends directly on reference taxonomy entities (Wilayas, Craft Categories, Materials, Eras, Techniques) and their join tables (`artisan_materials`, `artisan_techniques`, `artisan_epoques`), which must be established in Phase 3 before wiring into artisan profile queries in Phase 4.

---

## 📍 Phase 1: Build Infrastructure & Core Foundation (COMPLETED)
- [x] **Step 1.1**: Clean up and optimize `pom.xml` (Spring Boot 3.3.x, Java 21, Spring Data JPA, Spring Security, MySQL Connector, JJWT, Bucket4j / Caffeine rate limiting, ClamAV antivirus client, Jakarta Validation).
- [x] **Step 1.2**: Configure `application.properties` and environment-driven properties (Datasource connection pool, Hibernate DDL, JWT secret, CORS policies, AppProperties hierarchy).
- [x] **Step 1.3**: Implement Core Utilities & Foundation (`BaseEntity`, `ApiResponse<T>`, `PaginatedResponse<T>`, `AppException`, `GlobalExceptionHandler`).
- [x] **Step 1.4**: Compile and verify base setup with `./mvnw clean compile`.

---

## 📍 Phase 2: Security, Authentication & Identity Management (COMPLETED)
- [x] **Step 2.1**: Implement `User`, `Role`, `RefreshToken`, and `VerificationToken` JPA entities and repositories.
- [x] **Step 2.2**: Implement `JwtUtils` (deterministic with injected `Clock`), `JwtAuthenticationFilter`, `RateLimitFilter` (Caffeine bounded cache), and `CustomUserDetailsService`.
- [x] **Step 2.3**: Configure `SecurityConfig` (stateless session, route whitelisting, CORS bean, method security with standard 403 error envelopes).
- [x] **Step 2.4**: Implement `AuthService` and `AuthController` (`/api/v1/auth/register`, `/login`, `/refresh`, `/logout`, `/verify-email`, `/resend-verification`, `/forgot-password`, `/reset-password`, `/change-password`, `/me`, `PATCH /me`, `/complete-profile`).
- [x] **Step 2.5**: Write comprehensive unit, slice, and integration tests for authentication workflows (AuthControllerTest, AuthServiceTest, JwtUtilsTest, RefreshTokenServiceTest).

---

## 📍 Phase D: Dedicated File Storage & Avatar Pipeline (COMPLETED)
- [x] **Step D.1**: Build pluggable `StorageService` abstraction with `StorageResource`, in-memory stub provider, and S3/MinIO compatible provider (`S3StorageService`).
- [x] **Step D.2**: Implement ClamAV daemon antivirus stream scanning (`ClamAvScanner`) with fail-open/fail-closed configuration guards.
- [x] **Step D.3**: Implement multi-tier avatar image processing (`AvatarImageProcessor`) generating `original` (max 2000px), `medium` (max 500px), and `thumbnail` (max 150px) variants.
- [x] **Step D.4**: Implement `UserAvatar` entity, gallery management (10-avatar quota, list, activate, delete), and `AvatarController`.
- [x] **Step D.5**: Implement streaming file serving endpoint (`GET /api/v1/files/{key}`) with immutable HTTP caching and dedicated avatar rate-limiting (`AvatarUploadRateLimitFilter`).

---

## 📍 Phase 3: Catalog, Taxonomy & Heritage Reference Data (IN PROGRESS / ACTIVE TARGET)
- [ ] **Step 3.1**: Create JPA entities: `Region` (hierarchical wilayas/communes with self-referencing parent-child), `JobCategory`, `JobSubCategory`, `MaterialFamily`, `Material`, `Epoque`, `Technique` with unique slug constraints and display order weights.
- [ ] **Step 3.2**: Create Spring Data repositories with caching annotations for high-read, low-write taxonomy queries.
- [ ] **Step 3.3**: Implement `CatalogService` & `CatalogController` (`/api/v1/catalog/**`) for public reference discovery.
- [ ] **Step 3.4**: Extend `DataSeeder` with complete official 58 Algerian wilayas, traditional craft categories, material families, and historical periods.

---

## 📍 Phase 4: Artisan & Client Profiles (PARTIALLY COMPLETED)
- [x] **Step 4.1a**: Implement base `Artisan` and `Client` JPA entities linked to `User`.
- [x] **Step 4.2a**: Implement `/api/v1/auth/complete-profile` for artisans (bio, city, address, website).
- [x] **Step 4.3a**: Implement `ArtisanController` (`GET /api/v1/artisan/{id}`) with deduplicated profile view tracking and premium-gated contact info masking.
- [ ] **Step 4.1b**: Implement `ArtisanGalleryImage`, `ArtisanCertification`, `ArtisanAchievement`, `ArtisanSocialLink`, and join tables (`artisan_materials`, `artisan_techniques`, `artisan_epoques`).
- [ ] **Step 4.2b**: Implement multi-step `/api/v1/auth/complete-profile` for clients (client type, company name).
- [ ] **Step 4.3b**: Implement `ArtisanService` portfolio and certification management endpoints.

---

## 📍 Phase 5: Elasticsearch Indexing & Public Directory Search (PENDING PHASE 3 & 4)
- [ ] **Step 5.1**: Add Hibernate Search annotations (`@Indexed`, `@FullTextField`, `@KeywordField`) on `Artisan` and linked taxonomy entities.
- [ ] **Step 5.2**: Implement `DirectorySearchService` using Hibernate Search MassIndexer and boolean query builder.
- [ ] **Step 5.3**: Implement `DirectoryController` (`/api/v1/public/directory`) supporting multi-criteria filtering (wilaya, category, material, era, technique, keyword, featured, rating).
- [ ] **Step 5.4**: Add contact data masking logic for free vs. premium client access.

---

## 📍 Phase 6: Formations (Workshops / Masterclasses)
- [x] **Step 6.0**: **Formateur Governance Subsystem (COMPLETED)**:
  - Artisan teacher eligibility (`isTeacher` flag, application submission `POST /api/v1/artisan/formateur-request`).
  - Formateur state machine (`PENDING`, `APPROVED`, `REJECTED`).
  - 14-day reapplication cooldown enforcement and permanent block flag (`canReapply = false`).
  - Admin governance endpoints (`GET /api/v1/admin/formateur-requests`, approve, reject, lift cooldown, direct grant, direct revoke).
- [ ] **Step 6.1**: Implement `Formation`, `FormationEnrollment`, `FormationReview` entities and repositories.
- [ ] **Step 6.2**: Implement approval state machine: `DRAFT` → `PENDING_REVIEW` → `APPROVED`/`REJECTED` → `PUBLISHED`.
- [ ] **Step 6.3**: Implement `FormationService` and `FormationController` (creation, curriculum update, review submission, client enrollment).

---

## 📍 Phase 7: Social Feed, Reviews & Moderation
- [ ] **Step 7.1**: Implement `FeedPost` (Actualité, Formation, Annonce) with multi-image attachments.
- [ ] **Step 7.2**: Implement `Review` system with automatic recalculation of artisan average rating and review counts.
- [ ] **Step 7.3**: Implement `Report` system for flagging abuse on users, messages, and posts.

---

## 📍 Phase 8: Realtime Direct Messaging & Notifications
- [x] **Step 8.5**: **In-App Notification Engine (COMPLETED)**:
  - Notification persistence store (`Notification` entity & repository).
  - Unread badge counter with atomic decrement semantics (on read, soft-delete, bulk read) and floor-stability.
  - Soft-delete exclusion and query-scoped isolation (404 on cross-user manipulation).
  - Dual-channel delivery via Spring STOMP backed by RabbitMQ external broker relay with deferred push (`TransactionSynchronizationManager.afterCommit()`).
- [ ] **Step 8.1**: Configure Spring STOMP WebSocket chat channels (`/ws`, `/topic`, `/queue`, `/app`, `/user`).
- [ ] **Step 8.2**: Implement `WebSocketAuthInterceptor` for messaging channels.
- [ ] **Step 8.3**: Implement `Conversation`, `ConversationParticipant`, `Message` entities and repositories.
- [ ] **Step 8.4**: Implement `MessageService` with read receipts and realtime STOMP dispatching.

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
- [x] **Step 10.4**: **End-to-End Regression & Integration Suite (COMPLETED)**:
  - 584 unit and slice tests passing across all domain services and controllers.
  - Complete 6-batch Postman E2E testing suite (254 requests, 39 endpoints audited and verified against live server).
- [ ] **Step 10.2**: Implement platform analytics & KPI aggregation (`/api/v1/admin/stats`).
- [ ] **Step 10.3**: Actuator monitoring, health checks, rate-limit fine-tuning, and Swagger/OpenAPI documentation.
