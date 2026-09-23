# Souklab Backend — Feature Reference

<!-- AUTO-GENERATED: Do not manually edit the API route tables or enum value tables in this file.
     Source of truth: controller mappings, model enums, .env.example, docker-compose.yml -->

> **Version:** Spring Boot 4.0.8 · Java 21 · Generated from live source tree 2026-09-22.

This document is the canonical, code-derived feature reference for the Souklab backend.  
Every feature listed here was verified in a live exhaustive test campaign (566 scenarios, 100% pass rate) and confirmed by the automated test suite (1,228 tests, 0 failures).

---

## Table of Contents

1. [Platform Overview](#1-platform-overview)
2. [Technology Stack](#2-technology-stack)
3. [Account & Identity System](#3-account--identity-system)
4. [Authentication & JWT Security](#4-authentication--jwt-security)
5. [Permission & Authorization Model](#5-permission--authorization-model)
6. [Artisan Profiles & Gallery](#6-artisan-profiles--gallery)
7. [Client Profiles & Favorites](#7-client-profiles--favorites)
8. [Catalog & Taxonomy Directory](#8-catalog--taxonomy-directory)
9. [Artisan Directory Search](#9-artisan-directory-search)
10. [Formations & Masterclasses](#10-formations--masterclasses)
11. [Formateur Accreditation](#11-formateur-accreditation)
12. [Social Feed & Posts](#12-social-feed--posts)
13. [Reviews](#13-reviews)
14. [Content Reporting & Moderation](#14-content-reporting--moderation)
15. [Real-time Messaging (REST + WebSocket STOMP)](#15-real-time-messaging-rest--websocket-stomp)
16. [Notifications](#16-notifications)
17. [Subscriptions & Payments](#17-subscriptions--payments)
18. [Admin & Moderation Panel](#18-admin--moderation-panel)
19. [Avatars & File Management](#19-avatars--file-management)
20. [File Storage (MinIO / S3)](#20-file-storage-minio--s3)
21. [Analytics & Reporting](#21-analytics--reporting)
22. [Rate Limiting](#22-rate-limiting)
23. [Email Notifications](#23-email-notifications)
24. [Infrastructure & Deployment](#24-infrastructure--deployment)
25. [API Conventions](#25-api-conventions)
26. [Environment Variables Reference](#26-environment-variables-reference)
27. [Database Migrations](#27-database-migrations)
28. [Verification Ledger](#28-verification-ledger)

---

## 1. Platform Overview

Souklab is a **two-sided marketplace** connecting Algerian artisans with clients.

| Actor | Description |
|---|---|
| **Artisan** | Craftsperson who publishes a profile, lists products, runs masterclasses (formations), posts to the social feed, and exchanges messages with clients. |
| **Client** | Consumer who browses the directory, enrolls in formations, follows artisans, and messages them. |
| **Admin** | Platform staff with full moderation powers over users, content, artisan accreditation, subscriptions, and analytics. |

Core value propositions:
- Rich artisan directory with Elasticsearch full-text search, faceted filtering by craft category, wilaya, materials, techniques, and epoque.
- Formations (masterclasses) with a structured lifecycle from draft → admin review → publication → enrollment.
- Real-time STOMP/WebSocket messaging between clients and artisans, with guaranteed delivery and persistence.
- Tiered subscription model (FREE / PRO / PREMIUM) gating artisan feature entitlements.
- Antivirus-scanned, magic-byte-validated file storage via MinIO (S3-compatible) with configurable ClamAV integration.

---

## 2. Technology Stack

<!-- AUTO-GENERATED from pom.xml and docker-compose.yml -->

### Runtime

| Component | Version / Image |
|---|---|
| Java | 21 (Eclipse Temurin) |
| Spring Boot | 4.0.8 |
| Spring Framework | 7.x (auto-resolved via Boot) |
| Tomcat | 11.0.26 |

### Key Libraries

| Library | Version | Purpose |
|---|---|---|
| `spring-boot-starter-webmvc` | 4.0.8 | HTTP REST layer |
| `spring-boot-starter-security` + `spring-boot-starter-oauth2-client` | 4.0.8 | JWT auth + Google OAuth |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | 0.11.5 | JWT signing & parsing |
| `spring-boot-starter-data-jpa` | 4.0.8 | JPA/Hibernate ORM |
| `mariadb-java-client` | — | MariaDB 11.4 JDBC driver |
| `spring-boot-starter-flyway` + `flyway-mysql` | — | DB schema migrations |
| `spring-boot-starter-websocket` + `spring-boot-starter-amqp` | 4.0.8 | STOMP WebSocket + RabbitMQ relay |
| `hibernate-search-mapper-orm` + `hibernate-search-backend-elasticsearch` | 8.2.2.Final | Full-text search via Elasticsearch |
| `bucket4j-core` + `bucket4j-redis` | 8.10.1 | Token-bucket rate limiting (local or Redis-backed) |
| `lettuce-core` | — | Redis client (rate-limit backend) |
| `caffeine` | — | In-memory application cache |
| `aws-sdk-s3` | 2.55.1 | MinIO / AWS S3 file storage |
| `tika-core` | 4.0.0 | MIME type detection (magic-byte inspection) |
| `thumbnailator` | 0.4.21 | Thumbnail generation |
| `pdfbox` | 3.0.8 | PDF processing / validation |
| `springdoc-openapi-starter-webmvc-ui` | 3.1.1 | OpenAPI 3 / Swagger UI |
| `spring-boot-starter-mail` | — | SMTP / MailerSend email dispatch |
| `micrometer-registry-prometheus` | — | Prometheus metrics export |
| `lombok` | — | Boilerplate reduction |

### Infrastructure (Docker Compose)

| Service | Image | Port(s) | Purpose |
|---|---|---|---|
| `mariadb` | `mariadb:11.4` | 3306 (configurable) | Primary relational database |
| `mariadb-flyway` | `mariadb:11.4` | 3308 (configurable) | Isolated schema for Flyway verification |
| `rabbitmq` | `rabbitmq:4.0-management` | 5672, 61613, 15672 | AMQP broker + STOMP relay + management UI |
| `minio` | `quay.io/minio/minio` | 9000, 9001 | S3-compatible object storage + console |
| `clamav` | `clamav/clamav:1.4` | 3310 | Antivirus scanning |
| `redis` | `redis:7.4.1-alpine` | 6379 | Rate-limit store + optional distributed cache |
| `elasticsearch` | `elasticsearch:8.15.3` | 9200 | Full-text search index |
| `app` | `Dockerfile` (build) | 8080 | Spring Boot application container (non-root `souklab`, uses `.env.docker`) |

---

## 3. Account & Identity System

### Account Roles (`AccountRole`)

<!-- AUTO-GENERATED from src/main/java/com/project/souklab/model/AccountRole.java -->

| Value | Description |
|---|---|
| `ARTISAN` | Craftsperson account type. Grants artisan-specific permissions on activation. |
| `CLIENT` | Consumer account type. Grants basic profile and messaging permissions on activation. |
| `ADMIN` | Platform administrator. Granted via admin console, not through self-registration. |

Registration uses the field name `accountType` (not `role`). Accepted values: `ARTISAN`, `CLIENT`.

### Account Statuses (`AccountStatus`)

<!-- AUTO-GENERATED from src/main/java/com/project/souklab/model/AccountStatus.java -->

| Value | Transitions To | Description |
|---|---|---|
| `PENDING` | `ACTIVE`, `REJECTED` | Newly registered — awaiting email verification and admin approval. |
| `ACTIVE` | `SUSPENDED` | Fully operational account. |
| `SUSPENDED` | `ACTIVE` (auto if timeout expires), manual unban | Banned or timed-out account. `bannedUntil` timestamp determines whether it is permanent or temporary (timeout). |
| `REJECTED` | — | Registration explicitly rejected by an administrator. |

### Account Lifecycle

```
Registration → PENDING (email unverified)
→ Email PIN verified → PENDING (awaiting admin approval)
→ Admin approve → ACTIVE
→ Admin ban → SUSPENDED (permanent)
→ Admin timeout → SUSPENDED (expires at bannedUntil)
→ Timeout expires → effectiveStatus = ACTIVE (auto)
→ Admin unban → ACTIVE
```

### Registration DTO (`UserRegistrationDTO`)

| Field | Type | Required | Validation |
|---|---|---|---|
| `email` | `String` | ✅ Yes | `@NotBlank`, `@Email` |
| `password` | `String` | ✅ Yes | `@NotBlank`, `@Size(min=8)` — additional strength rules enforced in service |
| `name` | `String` | No | — |
| `firstName` | `String` | No | — |
| `lastName` | `String` | No | — |
| `accountType` | `AccountRole` | ✅ Yes | `@NotNull`, enum: `ARTISAN` \| `CLIENT` |

---

## 4. Authentication & JWT Security

### Authentication Flow

| Step | Endpoint | Method | Auth Required |
|---|---|---|---|
| 1. Register | `POST /api/v1/auth/register` | POST | None |
| 2. Verify email | `POST /api/v1/auth/verify-email` | POST | None |
| 3. Resend verification | `POST /api/v1/auth/resend-verification` | POST | None |
| 4. Log in | `POST /api/v1/auth/login` | POST | None |
| 5. Refresh token | `POST /api/v1/auth/refresh` | POST | None (Bearer refresh token) |
| 6. Logout | `POST /api/v1/auth/logout` | POST | Bearer access token |
| 7. Change password | `POST /api/v1/auth/change-password` | POST | Bearer access token |
| 8. Forgot password | `POST /api/v1/auth/forgot-password` | POST | None |
| 9. Reset password | `POST /api/v1/auth/reset-password` | POST | None |
| 10. Get own profile | `GET /api/v1/auth/me` | GET | Bearer access token |
| 11. Update own profile | `PATCH /api/v1/auth/me` | PATCH | Bearer access token |
| 12. Complete profile | `POST /api/v1/auth/complete-profile` | POST | Bearer access token |
| 13. Google OAuth (artisan) | `GET /api/v1/auth/oauth/google/artisan` | GET | Browser redirect |
| 14. Google OAuth (client) | `GET /api/v1/auth/oauth/google/client` | GET | Browser redirect |

### Token Specifications

| Property | Value |
|---|---|
| Access token type | JWT (HS256) |
| Access token expiry | `APP_JWT_ACCESS_EXP` ms (default 3600000 = 1 hour) |
| Refresh token expiry | `APP_JWT_REFRESH_EXP` ms (default 86400000 = 24 hours) |
| Refresh token rotation | Yes — each refresh issues a new pair and invalidates the old one |
| Reuse detection | Yes — replaying a rotated refresh token triggers session revocation |

### Security Policies

| Policy | Configured Value |
|---|---|
| Login max failed attempts | `AUTH_LOCKOUT_MAX_ATTEMPTS=5` |
| Lockout duration | `AUTH_LOCKOUT_DURATION_MINUTES=15` |
| Email verification max attempts | `AUTH_VERIFICATION_MAX_ATTEMPTS=5` |
| Verification code expiry | `AUTH_VERIFICATION_EXPIRATION_MINUTES=15` |
| Verification code length | `AUTH_VERIFICATION_CODE_LENGTH=6` (numeric PIN) |

### `PATCH /api/v1/auth/me` — JSON Merge Patch Semantics

- Omitted fields: **unchanged**.
- Explicit `null` values: **clear the field** (nullable fields only).
- Account-critical fields (`email`, `accountStatus`, `password`, `permissions`) are **ignored** even if included in the body — they cannot be changed via this endpoint.
- `isTeacher` cannot be self-granted here; it is set only through the admin formateur-grant path.

---

## 5. Permission & Authorization Model

All authorization is **permission-based**, not role-based. Roles (`ARTISAN`, `CLIENT`, `ADMIN`) are only used to seed the initial permission set on registration/approval. Subsequent access control is driven entirely by which `Permission` enum values a user holds.

### Permission Catalog

<!-- AUTO-GENERATED from src/main/java/com/project/souklab/security/Permission.java -->

| Permission Key | Spring Authority | Holder Description |
|---|---|---|
| `permission:admin:users` | `Admin.USERS` | Manage users: ban/unban/timeout/approve |
| `permission:admin:formations` | `Admin.FORMATIONS` | Review and publish formations |
| `permission:admin:feed` | `Admin.FEED` | Moderate feed posts and comments |
| `permission:admin:reports` | `Admin.REPORTS` | Triage and resolve content reports |
| `permission:financial:admin` | `Financial.ADMIN` | Grant/revoke subscriptions, process refunds |
| `permission:artisan:formations` | `Artisan.FORMATIONS` | Create and manage own formations (requires `isTeacher=true`) |
| `permission:artisan:content` | `Artisan.CONTENT` | Publish feed posts, manage gallery, certifications |
| `permission:artisan:reviews` | `Artisan.REVIEWS` | Read and respond to own reviews |
| `permission:profile:read` | `Profile.READ` | Read own and public profiles |
| `permission:profile:write` | `Profile.WRITE` | Update own profile |
| `permission:report:create` | `Report.CREATE` | Submit content abuse reports |
| `permission:file:read` | `File.READ` | Download protected formation files |
| `permission:message:send` | `Message.SEND` | Send and receive messages |
| `permission:analytics:admin` | `Analytics.ADMIN` | Access analytics dashboard and export data |
| `permission:admin:catalog` | `Admin.CATALOG` | Manage reference taxonomies: techniques, epoques, regions, categories, materials |

### `AccessControlService` Predicates

| Method | Checks Permission |
|---|---|
| `isAdmin()` | `Admin.USERS` |
| `canManageUsers()` | `Admin.USERS` |
| `canManageFormations()` | `Admin.FORMATIONS` |
| `canModerateFeed()` | `Admin.FEED` |
| `canModerateReports()` | `Admin.REPORTS` |
| `canManageFinancialOperations()` | `Financial.ADMIN` |
| `canViewAnalytics()` | `Analytics.ADMIN` |
| `canManageCatalog()` | `Admin.CATALOG` |
| `canManageArtisanFormations()` | `Artisan.FORMATIONS` |
| `canManageArtisanContent()` / `isArtisan()` | `Artisan.CONTENT` |
| `canManageArtisanReviews()` | `Artisan.REVIEWS` |
| `canReadProfile()` | `Profile.READ` |
| `canWriteProfile()` | `Profile.WRITE` |

### Permission Assignment Endpoints

| Endpoint | Method | Caller |
|---|---|---|
| `GET /api/v1/admin/users/{userId}/permissions` | GET | Admin (`Admin.USERS`) |
| `POST /api/v1/admin/users/{userId}/permissions` | POST | Admin (`Admin.USERS`) |
| `DELETE /api/v1/admin/users/{userId}/permissions` | DELETE | Admin (`Admin.USERS`) |

---

## 6. Artisan Profiles & Gallery

### Artisan Profile Endpoints

<!-- AUTO-GENERATED from ArtisanController, ArtisanGalleryController, ArtisanCertificationController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/artisan/{id}` | GET | Optional | Get public artisan profile by ID |
| `POST /api/v1/artisan/gallery` | POST (multipart) | `Artisan.CONTENT` | Upload a new gallery image |
| `GET /api/v1/artisan/gallery` | GET | `Artisan.CONTENT` | List own gallery images |
| `PUT /api/v1/artisan/gallery/order` | PUT | `Artisan.CONTENT` | Reorder gallery images |
| `DELETE /api/v1/artisan/gallery/{id}` | DELETE | `Artisan.CONTENT` | Delete a gallery image (ownership enforced) |
| `POST /api/v1/artisan/certifications` | POST (multipart) | `Artisan.CONTENT` | Upload a certification document (PDF/image) |
| `GET /api/v1/artisan/certifications` | GET | `Artisan.CONTENT` | List own certifications |
| `DELETE /api/v1/artisan/certifications/{id}` | DELETE | `Artisan.CONTENT` | Delete a certification (ownership enforced) |

### Contact Info Masking

Profile endpoints apply graduated contact visibility based on the viewer's relationship:

| Viewer | Phone / Email Visibility |
|---|---|
| Unauthenticated / non-premium client | Masked (`null` or redacted) |
| Premium client | Full phone and email revealed |
| Artisan (self-view) | Full |
| Admin | Full |

### Gallery & Certification Limits

| Setting | Default |
|---|---|
| Gallery max images | `ARTISAN_GALLERY_MAX_IMAGES=20` |
| Gallery max file size | `ARTISAN_GALLERY_MAX_FILE_SIZE=10MB` |
| Gallery allowed MIME types | `image/jpeg`, `image/png`, `image/webp` |
| Certification max count | `ARTISAN_CERTIFICATION_MAX_COUNT=10` |
| Certification max file size | `ARTISAN_CERTIFICATION_MAX_FILE_SIZE=15MB` |
| Certification allowed MIME types | `application/pdf`, `image/jpeg`, `image/png` |

---

## 7. Client Profiles & Favorites

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/client/profile` | GET | `Profile.READ` | Retrieve own client profile |
| `PATCH /api/v1/client/profile` | PATCH | `Profile.WRITE` | Update own client profile |
| `POST /api/v1/client/favorites/{artisanId}` | POST | `Profile.READ` | Add artisan to favorites |
| `DELETE /api/v1/client/favorites/{artisanId}` | DELETE | `Profile.READ` | Remove artisan from favorites |
| `GET /api/v1/client/favorites` | GET | `Profile.READ` | List favorite artisans |

---

## 8. Catalog & Taxonomy Directory

All taxonomy endpoints are **public** — no authentication required.

<!-- AUTO-GENERATED from CatalogController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/catalog/regions` | GET | None | Hierarchical region tree (Algeria: DZ root → 58 Wilayas → Communes) |
| `GET /api/v1/catalog/categories` | GET | None | Craft category hierarchy with nested sub-categories |
| `GET /api/v1/catalog/materials` | GET | None | List of raw materials used in crafts |
| `GET /api/v1/catalog/epoques` | GET | None | Historical eras / style periods |
| `GET /api/v1/catalog/techniques` | GET | None | Artisanal techniques |

### Reference Taxonomies Inventory

The platform seeds a comprehensive Algerian and Mediterranean reference taxonomy:

- **Geographic Administrative Hierarchy (`regions`)**:
  - Root country node: Algeria (`DZ`).
  - 58 Algerian Wilayas (Adrar through El Meniaa) with administrative codes and display orders.
  - Communes nested under their respective parent Wilaya.
- **Artisanal Building Trades (`job_categories` & `job_sub_categories`)**:
  - 8 Categories and 37 Subcategories (French taxonomy):
    - *Gros œuvre & structure* (6 subcategories)
    - *Toiture & enveloppe du bâtiment* (5 subcategories)
    - *Électricité & énergie* (4 subcategories)
    - *Plomberie & systèmes techniques* (4 subcategories)
    - *Finitions & second œuvre* (5 subcategories)
    - *Menuiserie & agencement* (4 subcategories)
    - *Métal & serrurerie* (4 subcategories)
    - *Métiers du patrimoine* (5 subcategories)
- **Mediterranean Building Materials (`material_families` & `materials`)**:
  - 6 Families and 25 Materials:
    - *Matériaux naturels traditionnels* (5 materials)
    - *Matériaux de maçonnerie* (4 materials)
    - *Matériaux de toiture* (4 materials)
    - *Métal & structure* (3 materials)
    - *Isolation & techniques modernes* (5 materials)
    - *Revêtements & finitions* (4 materials)
- **Historical Eras (`epoques`)**:
  - 14 chronological periods (Antiquity, Islamic eras, Ottoman regency, Traditional, and Modern craft movements).
- **Artisanal Techniques (`techniques`)**:
  - 20 traditional and heritage crafting techniques.

---

## 9. Artisan Directory Search

<!-- AUTO-GENERATED from DirectoryController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/public/directory` | GET | None | Full-text search + faceted filter of published artisan profiles |

### Search Parameters

| Parameter | Type | Description |
|---|---|---|
| `q` | `String` | Full-text keyword query (fuzzy match via Elasticsearch) |
| `categorySlug` | `String` | Filter by craft category slug |
| `subCategorySlug` | `String` | Filter by subcategory slug |
| `wilayaCode` | `String` | Filter by Algerian wilaya code |
| `materialIds` | `List<UUID>` | Filter by material identifiers |
| `techniqueIds` | `List<UUID>` | Filter by technique identifiers |
| `epoqueIds` | `List<UUID>` | Filter by epoque identifiers |
| `verifiedOnly` | `Boolean` | Only return verified artisans |
| `premiumOnly` | `Boolean` | Only return premium-tier artisans |
| `teacherOnly` | `Boolean` | Only return accredited formateurs (teachers) |
| `sortBy` | `String` | `relevance` \| `name` \| `createdAt` \| `rating` |
| `page` | `int` | Zero-indexed page number (default 0) |
| `size` | `int` | Page size (default `DIRECTORY_DEFAULT_PAGE_SIZE=20`, max `DIRECTORY_MAX_PAGE_SIZE=100`) |

### Pagination Policy

| Setting | Default |
|---|---|
| Default page index | `DIRECTORY_DEFAULT_PAGE_INDEX=0` |
| Default page size | `DIRECTORY_DEFAULT_PAGE_SIZE=20` |
| Minimum page size | `DIRECTORY_MIN_PAGE_SIZE=1` |
| Maximum page size | `DIRECTORY_MAX_PAGE_SIZE=100` — requests above this are clamped, never rejected |

---

## 10. Formations & Masterclasses

Formations are in-person or hybrid masterclasses created exclusively by artisans with **formateur** (teacher) accreditation.

### Formation Lifecycle States

<!-- AUTO-GENERATED from src/main/java/com/project/souklab/model/FormationStatus.java and model/README.md -->

| State | Description |
|---|---|
| `DRAFT` | Newly created; author can still edit all fields |
| `PENDING_REVIEW` | Submitted for admin review; no edits allowed |
| `APPROVED` | Admin approved; ready to be published |
| `REJECTED` | Admin rejected with feedback; author can revise and resubmit |
| `PUBLISHED` | Publicly visible; enrollment open |

### Enrollment Statuses

| Status | Description |
|---|---|
| `CONFIRMED` | Enrollment accepted, session upcoming |
| `ATTENDED` | Participant marked as attended — required to leave a review |
| `CANCELLED` | Enrollment cancelled (by participant or system) |

### Formation Endpoints — Teacher Artisan

<!-- AUTO-GENERATED from ArtisanFormationController, ArtisanFormationEnrollmentController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `POST /api/v1/artisan/formations` | POST | `Artisan.FORMATIONS` + `isTeacher=true` | Create a formation (starts as `DRAFT`) |
| `PUT /api/v1/artisan/formations/{id}` | PUT | `Artisan.FORMATIONS` + owner | Update formation (DRAFT state only) |
| `POST /api/v1/artisan/formations/{id}/thumbnail` | POST (multipart) | `Artisan.FORMATIONS` + owner | Upload or replace formation thumbnail |
| `POST /api/v1/artisan/formations/{id}/files` | POST (multipart) | `Artisan.FORMATIONS` + owner | Upload a protected course file (up to 10 files) |
| `DELETE /api/v1/artisan/formations/{id}/files/{fileId}` | DELETE | `Artisan.FORMATIONS` + owner | Remove a course file |
| `POST /api/v1/artisan/formations/{id}/submit` | POST | `Artisan.FORMATIONS` + owner | Submit for admin review |
| `DELETE /api/v1/artisan/formations/{id}` | DELETE | `Artisan.FORMATIONS` + owner | Soft-delete (DRAFT/REJECTED only) |
| `GET /api/v1/artisan/formations/me` | GET | `Artisan.FORMATIONS` | List own formations with status filters |
| `GET /api/v1/artisan/formations/{id}` | GET | `Artisan.FORMATIONS` | Get own formation detail |

### Formation Endpoints — Enrollment (Any Artisan)

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/artisan/formations/catalog` | GET | `Artisan.FORMATIONS` | Browse published formation catalog |
| `GET /api/v1/artisan/formations/catalog/{id}` | GET | `Artisan.FORMATIONS` | View a published formation detail |
| `POST /api/v1/artisan/formations/{id}/enroll` | POST | `Artisan.FORMATIONS` | Enroll in a formation |
| `POST /api/v1/artisan/formations/{id}/cancel` | POST | `Artisan.FORMATIONS` | Cancel own enrollment |
| `GET /api/v1/artisan/formations/my-enrollments` | GET | `Artisan.FORMATIONS` | List own enrollments |
| `GET /api/v1/artisan/formations/{id}/files/{fileId}/download` | GET | `File.READ` + enrolled or owner | Download a protected course file |

### Formation Endpoints — Admin

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/admin/formations/pending` | GET | `Admin.FORMATIONS` | List formations awaiting review |
| `POST /api/v1/admin/formations/{id}/review` | POST | `Admin.FORMATIONS` | Submit review decision (APPROVED / REJECTED) |
| `POST /api/v1/admin/formations/{id}/publish` | POST | `Admin.FORMATIONS` | Publish an approved formation |

### Formation Limits

| Setting | Default |
|---|---|
| Thumbnail max size | `FORMATION_THUMBNAIL_MAX_FILE_SIZE=10MB` |
| Thumbnail allowed MIME | `image/jpeg`, `image/png`, `image/webp` |
| Max course files | `FORMATION_FILE_MAX_COUNT=10` |
| Course file max size | `FORMATION_FILE_MAX_FILE_SIZE=25MB` |
| Course file allowed MIME | `application/pdf`, `image/jpeg`, `image/png` |
| Default currency | `FORMATION_DEFAULT_CURRENCY=DZD` |
| Cancellation deadline | `FORMATION_CANCELLATION_DEADLINE_HOURS=24` (cutoff before formation start) |

### Business Rules

- Only artisans with `isTeacher=true` (accredited formateurs) may create formations.
- A formation **author cannot self-enroll** — returns `400 Bad Request`.
- **Duplicate enrollment** on the same formation returns `409 Conflict`.
- **Capacity is hard-enforced** — enrollment beyond `maxParticipants` returns `409 Conflict`.
- A review may only be submitted by an artisan with an `ATTENDED` enrollment on that formation.
- Non-enrolled artisans attempting to download protected files receive `403 Forbidden` (not `404`).
- Formation authors always have access to their own formation's protected files.

---

## 11. Formateur Accreditation

Controls which artisans may create formations (masterclasses).

<!-- AUTO-GENERATED from ArtisanFormateurController, AdminFormateurController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `POST /api/v1/artisan/formateur-request` | POST | `Artisan.CONTENT` | Submit accreditation application |
| `GET /api/v1/admin/formateur-requests` | GET | `Admin.FORMATIONS` | List pending accreditation requests |
| `POST /api/v1/admin/formateur-requests/{id}/approve` | POST | `Admin.FORMATIONS` | Approve request → sets `isTeacher=true` |
| `POST /api/v1/admin/formateur-requests/{id}/reject` | POST | `Admin.FORMATIONS` | Reject with optional cooldown |
| `POST /api/v1/admin/artisans/{artisanId}/formateur-grant` | POST | `Admin.FORMATIONS` | Directly grant teacher status |
| `POST /api/v1/admin/artisans/{artisanId}/formateur-revoke` | POST | `Admin.FORMATIONS` | Revoke teacher status |
| `POST /api/v1/admin/formateur-requests/{artisanId}/lift-cooldown` | POST | `Admin.FORMATIONS` | Lift reapply cooldown after rejection |

### Accreditation State Machine

```
Artisan submits → PENDING
Admin approves → APPROVED (isTeacher=true)
Admin rejects  → REJECTED (with optional reapply cooldown)
  During cooldown: reapply → 403 Forbidden
  Admin lifts cooldown: reapply succeeds
Admin direct-grant: APPROVED (bypass request flow)
Admin direct-revoke: isTeacher=false (formations remain)
Admin permanent block (canReapply=false): future requests → 403 Forbidden until lifted
```

### Reapply Cooldown

| Setting | Default |
|---|---|
| Formateur reapply cooldown | `ARTISAN_FORMATEUR_REAPPLY_COOLDOWN_DAYS=14` |

---

## 12. Social Feed & Posts

<!-- AUTO-GENERATED from FeedPostController, AdminFeedController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/feed` | GET | `Profile.READ` | Paginated feed of published posts |
| `GET /api/v1/feed/{id}` | GET | `Profile.READ` | Get single post detail |
| `POST /api/v1/feed` | POST | `Artisan.CONTENT` | Create a new feed post |
| `PUT /api/v1/feed/{id}` | PUT | `Artisan.CONTENT` + owner | Update post text/tags |
| `DELETE /api/v1/feed/{id}` | DELETE | `Artisan.CONTENT` + owner | Soft-delete own post |
| `POST /api/v1/feed/{id}/media` | POST (multipart) | `Artisan.CONTENT` + owner | Attach media to post |
| `DELETE /api/v1/feed/{id}/media/{mediaId}` | DELETE | `Artisan.CONTENT` + owner | Remove media from post |
| `POST /api/v1/admin/feed/{id}/publish` | POST | `Admin.FEED` | Admin publish a hidden post |
| `POST /api/v1/admin/feed/{id}/hide` | POST | `Admin.FEED` | Admin hide a post |
| `POST /api/v1/admin/feed/{id}/remove` | POST | `Admin.FEED` | Admin permanently remove a post |

### Feed Post Permissions

| Action | Required |
|---|---|
| Create post | `Artisan.CONTENT` + account status `ACTIVE` + artisan verified |
| Edit / delete own post | `Artisan.CONTENT` + ownership check |
| Like / comment | `Profile.READ` |
| Admin moderation | `Admin.FEED` |

### Media Configuration

| Setting | Default |
|---|---|
| Max media items per post | `FEED_MAX_MEDIA_PER_POST=10` |
| Allowed image MIME types | `FEED_ALLOWED_IMAGE_MIME_TYPES=image/jpeg,image/png,image/webp` |

---

## 13. Reviews

### Review Creation — Single Path

> **There is exactly one review creation path: `POST /api/v1/artisan/formations/{formationId}/reviews`**  
> No conversation-based or messaging-tied review mechanism exists.

<!-- AUTO-GENERATED from ArtisanReviewController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/artisans/{artisanId}/reviews` | GET | Optional | List artisan's public reviews (paginated) |
| `POST /api/v1/artisan/formations/{formationId}/reviews` | POST | `Artisan.REVIEWS` | Create a review (requires `ATTENDED` enrollment on that formation) |
| `PUT /api/v1/artisan/reviews/{reviewId}` | PUT | `Artisan.REVIEWS` + owner | Update own review |
| `DELETE /api/v1/artisan/reviews/{reviewId}` | DELETE | `Artisan.REVIEWS` + owner | Delete own review |

### Review Business Rules

- Reviewer must have an `ATTENDED` enrollment on the specific formation being reviewed.
- Duplicate review on the same enrollment returns `409 Conflict` (unique constraint on `enrollment_id`).
- Reviews are linked to an artisan's aggregate rating which updates on create/update/delete.
- Review status: `PUBLISHED` (default on creation).

---

## 14. Content Reporting & Moderation

<!-- AUTO-GENERATED from ContentReportController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `POST /api/v1/reports` | POST | `Report.CREATE` | Submit an abuse report |
| `GET /api/v1/admin/reports` | GET | `Admin.REPORTS` | List open reports (admin) |
| `POST /api/v1/admin/reports/{id}/resolve` | POST | `Admin.REPORTS` | Resolve a report with decision and action |

### Report Targets

Reports may be submitted against:
- Feed posts
- Comments
- User accounts

### Report Reason Codes

Standard reason codes include: `SPAM`, `HARASSMENT`, `INAPPROPRIATE_CONTENT`, `MISINFORMATION`, and `OTHER`.

### Business Rules

- Duplicate report against the same entity by the same user returns `409 Conflict`.
- Admin resolution can include: dismiss (no action), delete the reported content, suspend/ban the reported user.

---

## 15. Real-time Messaging (REST + WebSocket STOMP)

Messaging operates on two complementary layers: REST for state retrieval and STOMP over WebSocket for real-time delivery.

### REST Conversation Endpoints

<!-- AUTO-GENERATED from ConversationController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `POST /api/v1/conversations` | POST | `Message.SEND` | Create or retrieve a 1-on-1 conversation |
| `GET /api/v1/conversations` | GET | `Message.SEND` | List own conversations (paginated) |
| `PATCH /api/v1/conversations/{id}/archive` | PATCH | `Message.SEND` + participant | Archive a conversation |
| `GET /api/v1/conversations/{id}/messages` | GET | `Message.SEND` + participant | Fetch paginated message history |
| `POST /api/v1/conversations/{id}/messages` | POST | `Message.SEND` + participant | Send a message (REST fallback) |
| `PATCH /api/v1/conversations/{conversationId}/messages/{messageId}` | PATCH | `Message.SEND` + sender | Edit own message |
| `DELETE /api/v1/conversations/{conversationId}/messages/{messageId}` | DELETE | `Message.SEND` + sender | Soft-delete own message |
| `POST /api/v1/conversations/{id}/read` | POST | `Message.SEND` + participant | Mark conversation as read |
| `POST /api/v1/conversations/{id}/attachments` | POST (multipart) | `Message.SEND` + participant | Upload a message attachment |

### STOMP WebSocket Protocol

**Connection endpoint:** `ws://{host}/ws/websocket`  
**Authentication:** Include `Authorization: Bearer {access_token}` as a STOMP header on the CONNECT frame.

#### Application Destinations (Client → Server)

| Destination | Payload | Description |
|---|---|---|
| `/app/v1/conversations/{id}/messages.send` | `SendMessageRequest` | Send a message |
| `/app/v1/conversations/{id}/messages.edit` | `EditMessageCommand` | Edit an existing message |
| `/app/v1/conversations/{id}/messages.delete` | `MessageCommand` | Delete a message |
| `/app/v1/conversations/{id}/read` | `MessageCommand` | Send read receipt |
| `/app/v1/conversations/{id}/typing.start` | `TypingCommand` | Broadcast typing started |
| `/app/v1/conversations/{id}/typing.stop` | `TypingCommand` | Broadcast typing stopped |

#### User Destinations (Server → Client)

| Destination | Event Types | Description |
|---|---|---|
| `/user/queue/chat` | `ACKNOWLEDGED`, `READ_UP_TO`, message payloads | Command acknowledgements and message delivery |
| `/user/queue/chat-events` | `EDIT`, `DELETE`, `TYPING_START`, `TYPING_STOP` | Message mutations and presence events |
| `/user/queue/notifications` | Notification payloads | System and app-level notifications |

> **Important:** Subscribe to **all three** destinations to receive the full event surface. Each handles a distinct event class.

### Chat Configuration

| Setting | Default |
|---|---|
| Max message length | `CHAT_MESSAGE_MAX_LENGTH=4000` |
| Max attachments per message | `CHAT_ATTACHMENT_MAX_COUNT=5` |
| Default page size | `CHAT_DEFAULT_PAGE_SIZE=50` |
| Max page size | `CHAT_MAX_PAGE_SIZE=100` |
| Cursor lifetime | `CHAT_CURSOR_LIFETIME=24h` |
| Typing event interval | `CHAT_TYPING_EVENT_INTERVAL=500ms` |

### Business Rules

- Self-messaging (conversation with yourself) returns `400 Bad Request`.
- Non-participant sending to a conversation returns `403 Forbidden`.
- Soft-deleted messages are excluded from subsequent `GET /messages` history responses.
- Reconnecting after disconnect restores full message history without duplicate delivery.
- STOMP frame isolation: User A never receives User B's private queue frames.

---

## 16. Notifications

<!-- AUTO-GENERATED from NotificationController, NotificationType -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/notifications` | GET | Bearer | Paginated notification list (newest first) |
| `GET /api/v1/notifications/unread-count` | GET | Bearer | Raw integer count of unread notifications |
| `PUT /api/v1/notifications/{id}/read` | PUT | Bearer + owner | Mark single notification as read |
| `PUT /api/v1/notifications/read-all` | PUT | Bearer | Mark all own notifications as read |
| `DELETE /api/v1/notifications/{id}` | DELETE | Bearer + owner | Delete own notification |

> **Important:** `GET /api/v1/notifications/unread-count` returns the count as a **raw integer** in the `data` field (type `int64`):  
> `{ "code": 200, "data": 5, "success": true }`  
> It is **not** wrapped as `{ "unreadCount": 5 }`.

### Notification Types

<!-- AUTO-GENERATED from src/main/java/com/project/souklab/model/NotificationType.java -->

| Category | Type Key | Trigger |
|---|---|---|
| Account | `ACCOUNT_VALIDATED` | Admin approves account |
| Account | `ACCOUNT_REJECTED` | Admin rejects account |
| Account | `ACCOUNT_SUSPENDED` | Admin suspends account |
| Account | `ACCOUNT_REINSTATED` | Admin reinstates account |
| Formation | `FORMATION_APPROVED` | Admin approves submitted formation |
| Formation | `FORMATION_REJECTED` | Admin rejects submitted formation |
| Formation | `NEW_FORMATION` | New formation matching artisan interests |
| Message | `NEW_MESSAGE` | Incoming chat message |
| Subscription | `SUBSCRIPTION_RENEWED` | Subscription auto-renewed |
| Subscription | `SUBSCRIPTION_EXPIRED` | Subscription expired |
| Subscription | `SUBSCRIPTION_REVOKED` | Admin revokes subscription |
| Subscription | `SUBSCRIPTION_RENEWAL_REMINDER` | Pre-expiry reminder |
| Subscription | `SUBSCRIPTION_GRANT_MANUAL` | Admin manually grants subscription |
| Payment | `PAYMENT_SUCCESS` | Payment successfully processed |
| Payment | `PAYMENT_FAILED` | Payment processing failed |
| Checkout | `CHECKOUT_CREATED` | Checkout session created |
| Checkout | `CHECKOUT_CANCELED` | Checkout session cancelled |
| Refund | `REFUND_REQUEST_UNAVAILABLE` | Refund not applicable |
| Report | `REPORT_NEW` | New content report received (admin) |
| Review | `REVIEW_NEW` | New review on own formation |
| Formateur | `FORMATEUR_REQUEST_SUBMITTED` | Accreditation request submitted |
| Formateur | `FORMATEUR_APPROVED` | Accreditation approved |
| Formateur | `FORMATEUR_GRANTED` | Direct teacher grant by admin |
| Formateur | `FORMATEUR_REJECTED` | Accreditation rejected |
| Formateur | `FORMATEUR_REVOKED` | Teacher status revoked |

### Ownership Convention

Cross-user access to another user's notification returns **`404 Not Found`** (query-scoped isolation, not `403`). This is the documented design; it differs from formation file access which returns `403` — both are correct for their respective resource types.

---

## 17. Subscriptions & Payments

### Subscription Tiers

| Tier | Description |
|---|---|
| `FREE` | Default tier for all artisans |
| `PRO` | Mid-tier with extended product and formation limits |
| `PREMIUM` | Top-tier with maximum entitlements, featured directory placement, and unlocked client contact info |

### Public Plan Endpoint

`GET /api/v1/subscriptions/plans` — **No authentication required**

### Artisan Subscription Endpoints

<!-- AUTO-GENERATED from SubscriptionAccountController, SubscriptionCheckoutController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/subscriptions/plans` | GET | None | List available subscription plans |
| `GET /api/v1/subscriptions/current` | GET | Bearer | Get own current subscription |
| `GET /api/v1/subscriptions` | GET | Bearer | List own subscription history |
| `POST /api/v1/subscriptions/checkout` | POST | Bearer | Initiate Chargily checkout for a plan |
| `POST /api/v1/subscriptions/{id}/renew` | POST | Bearer | Renew an existing subscription |
| `POST /api/v1/subscriptions/{id}/cancel` | POST | Bearer | Cancel a subscription |
| `GET /api/v1/payments` | GET | Bearer | List own payment records |
| `GET /api/v1/payments/{id}` | GET | Bearer | Get single payment record |

### Admin Subscription Endpoints

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/admin/subscriptions` | GET | `Financial.ADMIN` | List all subscriptions |
| `GET /api/v1/admin/subscriptions/payments` | GET | `Financial.ADMIN` | List all payments |
| `GET /api/v1/admin/subscriptions/webhooks` | GET | `Financial.ADMIN` | List webhook events |
| `POST /api/v1/admin/subscriptions/grant` | POST | `Financial.ADMIN` | Manually grant a subscription |
| `POST /api/v1/admin/subscriptions/{id}/revoke` | POST | `Financial.ADMIN` | Revoke a subscription |
| `POST /api/v1/admin/subscriptions/{id}/cancel` | POST | `Financial.ADMIN` | Admin-cancel a subscription |
| `POST /api/v1/admin/subscriptions/{id}/correct-state` | POST | `Financial.ADMIN` | Correct subscription state |
| `POST /api/v1/admin/subscriptions/payments/{id}/correct-state` | POST | `Financial.ADMIN` | Correct payment state |
| `POST /api/v1/admin/payments/{id}/refund` | POST | `Financial.ADMIN` | Process a refund |
| `GET /api/v1/admin/subscription-plans` | GET | `Financial.ADMIN` | List plans (admin view) |
| `POST /api/v1/admin/subscription-plans` | POST | `Financial.ADMIN` | Create a subscription plan |
| `PUT /api/v1/admin/subscription-plans/{id}` | PUT | `Financial.ADMIN` | Update a subscription plan |
| `DELETE /api/v1/admin/subscription-plans/{id}` | DELETE | `Financial.ADMIN` | Delete a subscription plan |

### Chargily Webhook

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `POST /api/v1/integrations/chargily/webhook` | POST | Signature-verified | Receive Chargily payment events |

Webhook requests with an invalid signature are rejected with `400 Bad Request`. Duplicate `event_id` processing is idempotent.

### Subscription Configuration

| Setting | Default |
|---|---|
| Currency | `SUBSCRIPTION_CURRENCY=DZD` |
| Renewal reminder offsets | `SUBSCRIPTION_REMINDER_OFFSETS=7,1` (days before expiry) |
| Lifecycle job interval | `SUBSCRIPTION_LIFECYCLE_INTERVAL=1h` |
| Checkout idempotency retention | `SUBSCRIPTION_CHECKOUT_IDEMPOTENCY_RETENTION=24h` |
| Webhook event retention | `SUBSCRIPTION_WEBHOOK_RETENTION=90d` |

---

## 18. Admin & Moderation Panel

### User Management

<!-- AUTO-GENERATED from UserManagementController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/admin/users` | GET | `Admin.USERS` | List all users with filters (role, status) |
| `GET /api/v1/admin/users/pending` | GET | `Admin.USERS` | List pending (unverified/unapproved) users |
| `GET /api/v1/admin/users/audit-logs` | GET | `Admin.USERS` | Paginated audit log of admin actions |
| `POST /api/v1/admin/users/{id}/approve` | POST | `Admin.USERS` | Approve a pending user account |
| `POST /api/v1/admin/users/approve-bulk` | POST | `Admin.USERS` | Bulk-approve pending users |
| `POST /api/v1/admin/users/{id}/ban` | POST | `Admin.USERS` | Permanently ban a user |
| `POST /api/v1/admin/users/{id}/timeout` | POST | `Admin.USERS` | Temporarily suspend a user (with expiry) |
| `POST /api/v1/admin/users/{id}/unban` | POST | `Admin.USERS` | Unban or lift a timeout |
| `GET /api/v1/admin/users/{userId}/permissions` | GET | `Admin.USERS` | List a user's permissions |
| `POST /api/v1/admin/users/{userId}/permissions` | POST | `Admin.USERS` | Assign permissions to a user |
| `DELETE /api/v1/admin/users/{userId}/permissions` | DELETE | `Admin.USERS` | Remove permissions from a user |

### Business Rules

- Unbanning a user who is already `ACTIVE` returns `409 Conflict`.
- Timeout expiry is automatically reflected in `effectiveStatus` without a cron job — it is computed at query time.
- Audit log entries are created for every admin action with actor ID, action type, and timestamp.

### Catalog & Taxonomy Management

<!-- AUTO-GENERATED from AdminCatalogController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `POST /api/v1/admin/catalog/techniques` | POST | `Admin.CATALOG` | Create craftsmanship technique |
| `PUT /api/v1/admin/catalog/techniques/{id}` | PUT | `Admin.CATALOG` | Replace technique |
| `PATCH /api/v1/admin/catalog/techniques/{id}` | PATCH | `Admin.CATALOG` | Partial update technique (status/order) |
| `DELETE /api/v1/admin/catalog/techniques/{id}` | DELETE | `Admin.CATALOG` | Delete technique (blocked if linked to artisans) |
| `POST /api/v1/admin/catalog/epoques` | POST | `Admin.CATALOG` | Create historical period/era |
| `PUT /api/v1/admin/catalog/epoques/{id}` | PUT | `Admin.CATALOG` | Replace historical period |
| `PATCH /api/v1/admin/catalog/epoques/{id}` | PATCH | `Admin.CATALOG` | Partial update historical period |
| `DELETE /api/v1/admin/catalog/epoques/{id}` | DELETE | `Admin.CATALOG` | Delete historical period (blocked if linked to artisans) |
| `POST /api/v1/admin/catalog/regions` | POST | `Admin.CATALOG` | Create region (wilaya or commune) |
| `PUT /api/v1/admin/catalog/regions/{id}` | PUT | `Admin.CATALOG` | Replace region (guards against circular ancestry) |
| `PATCH /api/v1/admin/catalog/regions/{id}` | PATCH | `Admin.CATALOG` | Partial update region |
| `DELETE /api/v1/admin/catalog/regions/{id}` | DELETE | `Admin.CATALOG` | Delete region (blocked if has child regions) |
| `POST /api/v1/admin/catalog/categories` | POST | `Admin.CATALOG` | Create job category |
| `PUT /api/v1/admin/catalog/categories/{id}` | PUT | `Admin.CATALOG` | Replace job category |
| `PATCH /api/v1/admin/catalog/categories/{id}` | PATCH | `Admin.CATALOG` | Partial update job category |
| `DELETE /api/v1/admin/catalog/categories/{id}` | DELETE | `Admin.CATALOG` | Delete job category (blocked if has subcategories) |
| `POST /api/v1/admin/catalog/subcategories` | POST | `Admin.CATALOG` | Create subcategory under parent category |
| `PUT /api/v1/admin/catalog/subcategories/{id}` | PUT | `Admin.CATALOG` | Replace subcategory |
| `PATCH /api/v1/admin/catalog/subcategories/{id}` | PATCH | `Admin.CATALOG` | Partial update subcategory |
| `DELETE /api/v1/admin/catalog/subcategories/{id}` | DELETE | `Admin.CATALOG` | Delete subcategory (blocked if linked to artisans) |
| `POST /api/v1/admin/catalog/material-families` | POST | `Admin.CATALOG` | Create material family |
| `PUT /api/v1/admin/catalog/material-families/{id}` | PUT | `Admin.CATALOG` | Replace material family |
| `PATCH /api/v1/admin/catalog/material-families/{id}` | PATCH | `Admin.CATALOG` | Partial update material family |
| `DELETE /api/v1/admin/catalog/material-families/{id}` | DELETE | `Admin.CATALOG` | Delete material family (blocked if has materials) |
| `POST /api/v1/admin/catalog/materials` | POST | `Admin.CATALOG` | Create material under parent family |
| `PUT /api/v1/admin/catalog/materials/{id}` | PUT | `Admin.CATALOG` | Replace material |
| `PATCH /api/v1/admin/catalog/materials/{id}` | PATCH | `Admin.CATALOG` | Partial update material |
| `DELETE /api/v1/admin/catalog/materials/{id}` | DELETE | `Admin.CATALOG` | Delete material (blocked if linked to artisans) |

### Catalog Management Rules

- Auto-derives unique slug via `SlugUtils.toSlug(name)` when `slug` is null or empty.
- Duplicate slug returns `409 Conflict`.
- Two-tier foreign key relationships (`JobCategory` / `JobSubCategory`, `MaterialFamily` / `Material`) prevent deletion of parents with child elements (`409 Conflict`).
- Self-referencing hierarchies (`Region`) detect and reject circular references using a recursive CTE (`422 Unprocessable Entity`).
- Mutations invalidate public Caffeine catalog caches (`@CacheEvict`) and record audit log entries with `AuditLogAction.CATALOG_ITEM_CREATED`, `UPDATED`, and `DELETED`.

---

## 19. Avatars & File Management

<!-- AUTO-GENERATED from AvatarController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `POST /api/v1/users/me/avatars` | POST (multipart) | Bearer | Upload a new avatar image |
| `GET /api/v1/users/me/avatars` | GET | Bearer | List own avatars |
| `DELETE /api/v1/users/me/avatars/{id}` | DELETE | Bearer | Delete an avatar |
| `PUT /api/v1/users/me/avatars/{id}/activate` | PUT | Bearer | Set an avatar as active |

### Avatar Rules

| Setting | Default |
|---|---|
| Max avatars per user | `AVATAR_MAX_PER_USER=10` — 11th upload returns `409 Conflict` |
| Allowed MIME types | `AVATAR_ALLOWED_MIME_TYPES=image/jpeg,image/png,image/webp` |
| Rate limit per user | `AVATAR_RATE_LIMIT_CAPACITY=5` requests per `AVATAR_RATE_LIMIT_REFILL_DURATION=1m` |

### Additional Rules

- Deleting the currently active avatar sets the active avatar to `null` without auto-promoting another avatar.
- Deleting a non-owned or non-existent avatar returns `404 Not Found`.
- Reactivating the already-active avatar is idempotent (returns `200 OK`).

---

## 20. File Storage (MinIO / S3)

### File Serving

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `GET /api/v1/files/{key}` | GET | `File.READ` | Download a stored file by storage key |

### Validation Pipeline

All uploaded files pass through:
1. **Size check** — enforced at servlet layer (`STORAGE_MAX_FILE_SIZE=15MB`).
2. **MIME type allowlist** — checked against `STORAGE_ALLOWED_MIME_TYPES`.
3. **Magic byte inspection** — Apache Tika detects real content type regardless of `Content-Type` header or filename extension. A renamed file with a wrong extension is rejected.
4. **ClamAV antivirus scan** — EICAR-positive files are rejected. Configurable `STORAGE_VIRUS_SCAN_FAIL_OPEN` controls behavior when ClamAV is unreachable.

### File Storage Configuration

| Setting | Default |
|---|---|
| Storage provider | `STORAGE_PROVIDER=s3` (MinIO locally) |
| S3 endpoint | `STORAGE_S3_ENDPOINT=http://localhost:9000` |
| S3 bucket | `STORAGE_S3_BUCKET=souklab-files` |
| Max file size | `STORAGE_MAX_FILE_SIZE=15MB` |
| Allowed MIME types (general) | `image/jpeg,image/png,application/pdf` |
| Antivirus scan enabled | `STORAGE_VIRUS_SCAN_ENABLED=false` (enable in production) |
| ClamAV host | `STORAGE_VIRUS_SCAN_HOST=localhost` |
| ClamAV port | `STORAGE_VIRUS_SCAN_PORT=3310` |
| Rate limit capacity | `STORAGE_RATE_LIMIT_CAPACITY=120` / `STORAGE_RATE_LIMIT_REFILL_DURATION=1m` |

---

## 21. Analytics & Reporting

The analytics module provides a job-based system for computing platform KPIs with configurable time buckets and retention policies.

<!-- AUTO-GENERATED from AnalyticsJobController -->

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `POST /api/v1/admin/analytics/rollups/jobs/rebuild` | POST | `Analytics.ADMIN` | Trigger a rollup rebuild job |
| `POST /api/v1/admin/analytics/rollups/jobs/backfill` | POST | `Analytics.ADMIN` | Trigger a historical backfill job |
| `GET /api/v1/admin/analytics/rollups/jobs/{id}` | GET | `Analytics.ADMIN` | Check rollup job status |
| `POST /api/v1/admin/analytics/rollups/rebuild` | POST | `Analytics.ADMIN` | Synchronous rollup rebuild |
| `POST /api/v1/admin/analytics/rollups/backfill` | POST | `Analytics.ADMIN` | Synchronous historical backfill |
| `POST /api/v1/admin/analytics/jobs` | POST | `Analytics.ADMIN` | Submit an analytics query job |
| `GET /api/v1/admin/analytics/jobs/{id}` | GET | `Analytics.ADMIN` | Get analytics job status |
| `GET /api/v1/admin/analytics/jobs/{id}/result` | GET | `Analytics.ADMIN` | Retrieve job result |
| `GET /api/v1/admin/analytics/jobs/{id}/download` | GET | `Analytics.ADMIN` | Download job result as CSV |
| `DELETE /api/v1/admin/analytics/jobs/{id}` | DELETE | `Analytics.ADMIN` | Delete a job and its artifacts |

> Also reachable at `/api/v1/admin/stats/**` (alias mapping).

### Analytics Configuration

| Setting | Default |
|---|---|
| Supported time buckets | `DAY,WEEK,MONTH,QUARTER` |
| Max result rows | `ANALYTICS_MAX_RESULT_ROWS=500` |
| Max result bytes | `ANALYTICS_MAX_RESULT_BYTES=10485760` (10 MB) |
| Max query range | `ANALYTICS_MAXIMUM_RANGE_DAYS=366` |
| Max bucket count | `ANALYTICS_MAXIMUM_BUCKET_COUNT=500` |
| Raw event retention | `ANALYTICS_RAW_EVENT_RETENTION=90d` |
| Rollup retention | `ANALYTICS_ROLLUP_RETENTION=730d` |
| Job retention | `ANALYTICS_JOB_RETENTION=24h` |
| Export retention | `ANALYTICS_EXPORT_RETENTION=24h` |
| Business timezone | `ANALYTICS_BUSINESS_TIME_ZONE=Africa/Algiers` |
| Job concurrency | `ANALYTICS_JOB_CONCURRENCY=2` |
| Export storage prefix | `ANALYTICS_EXPORT_STORAGE_PREFIX=analytics/exports` |

---

## 22. Rate Limiting

Rate limiting is implemented using **Bucket4j** (token bucket algorithm). The backing store is configurable: `local` (in-memory, per-instance) or `redis` (distributed, shared across instances).

### Rate Limit Tiers

<!-- AUTO-GENERATED from .env.example -->

| Layer | Applies To | Capacity | Refill |
|---|---|---|---|
| IP-based (sensitive public) | `/api/v1/auth/login`, `/api/v1/auth/verify-email`, `/api/v1/auth/forgot-password` | 5 req | per 1 minute |
| Avatar upload (per user) | `POST /api/v1/users/me/avatars` | `AVATAR_RATE_LIMIT_CAPACITY=5` | `AVATAR_RATE_LIMIT_REFILL_DURATION=1m` |
| File serving (per IP) | `GET /api/v1/files/**` | `STORAGE_RATE_LIMIT_CAPACITY=120` | `STORAGE_RATE_LIMIT_REFILL_DURATION=1m` |
| General authenticated users | All authenticated endpoints | `APP_RATE_LIMIT_USER_CAPACITY=120` | `APP_RATE_LIMIT_USER_REFILL_DURATION=PT1M` |
| Admin endpoints (per user) | `/api/v1/admin/**` | `RATE_LIMIT_ADMIN_USER_CAPACITY=60` | per 1 minute |
| Analytics endpoints (per user) | `/api/v1/admin/analytics/**` | `RATE_LIMIT_ANALYTICS_USER_CAPACITY=20` | per 1 minute |
| Analytics result download (per user) | Result download | `RATE_LIMIT_ANALYTICS_RESULTS_USER_CAPACITY=60` | per 1 minute |
| CSV export (per user) | CSV download | `RATE_LIMIT_CSV_USER_CAPACITY=10` | per 1 minute |
| Webhook (per user) | Webhook endpoints | `RATE_LIMIT_WEBHOOK_USER_CAPACITY=120` | per 1 minute |

### 429 Response

Requests that exceed limits receive:
- HTTP `429 Too Many Requests`
- `Retry-After: {seconds}` header derived from bucket refill estimation
- Standard JSON error envelope with `errorCode`

---

## 23. Email Notifications

The platform dispatches transactional emails through two configurable backends:

| Setting | Purpose |
|---|---|
| `APP_EMAIL_USE_SMTP=true` | Use local SMTP (default, development-friendly) |
| `MAILERSEND_API_KEY` set | Use MailerSend REST API for production delivery |

### Email Events That Trigger Dispatch

- Email address verification PIN
- Password reset PIN
- Account approval / rejection
- Formation approval / rejection notification
- Subscription expiry reminders (7 days and 1 day before expiry)

### SMTP Configuration

| Setting | Default |
|---|---|
| SMTP host | `SMTP_HOST=localhost` |
| SMTP port | `SMTP_PORT=1025` |
| Auth | `SMTP_AUTH=false` |
| STARTTLS | `SMTP_STARTTLS=false` |
| Sender email | `MAILERSEND_SENDER_EMAIL=noreply@souklab.dz` |
| Sender name | `MAILERSEND_SENDER_NAME=Souklab` |

---

## 24. Infrastructure & Deployment

### Docker Multi-Stage Build

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
# Runs as non-root user 'souklab'
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Actuator & Observability

| Endpoint | Purpose |
|---|---|
| `/actuator/health` | Health check (liveness / readiness) |
| `/actuator/prometheus` | Prometheus metrics scrape endpoint |
| `APP_HEALTH_DEPENDENCY_TIMEOUT=2s` | Timeout for dependency health probes |

### Async Thread Pools

| Pool | Core | Max | Queue | Prefix |
|---|---|---|---|---|
| Application pool | 4 | 16 | 200 | `souklab-async-` |
| Workflow pool | 8 | 32 | 500 | `souklab-workflow-` |

### Application Cache

| Setting | Default |
|---|---|
| Cache expire-after-write | `APP_CACHE_EXPIRE_AFTER_WRITE=60m` |
| Max cache size | `APP_CACHE_MAXIMUM_SIZE=1000` |

### CORS

| Setting | Default |
|---|---|
| Allowed origins | `APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000` |

### OpenAPI / Swagger UI

| Setting | Default |
|---|---|
| Enabled | `OPENAPI_ENABLED=true` |
| API docs path | `OPENAPI_PATH=/v3/api-docs` |
| Swagger UI path | `OPENAPI_SWAGGER_PATH=/swagger-ui.html` |

---

## 25. API Conventions

### Response Envelope

All REST responses are wrapped in a standard `ApiResponse<T>` envelope:

```json
{
  "code": 200,
  "success": true,
  "message": "Operation description",
  "data": { ... },
  "errors": null
}
```

| Field | Present When | Type |
|---|---|---|
| `code` | Always | Integer (mirrors HTTP status) |
| `success` | Always | Boolean |
| `message` | Always | String |
| `data` | Success responses | Any (object, array, primitive, or `null`) |
| `errors` | `422 Unprocessable Content` only | `Map<String, String>` — field name → validation message |
| `errorCode` | `AppException`-derived errors only | String (domain-specific error code) |

### HTTP Status Usage

| HTTP Status | Meaning |
|---|---|
| `200 OK` | Successful GET, PUT, PATCH, or state-change POST |
| `201 Created` | Successful resource creation |
| `204 No Content` | Successful deletion |
| `400 Bad Request` | Invalid request state (e.g., self-conversation, cancelling after deadline) |
| `401 Unauthorized` | Missing or invalid / expired JWT |
| `403 Forbidden` | Authenticated but insufficient permissions or ownership violation (formation files) |
| `404 Not Found` | Resource not found or ownership-scoped access to another user's resource (notifications) |
| `409 Conflict` | Duplicate entity or conflicting state |
| `413 Payload Too Large` | File exceeds size limit |
| `422 Unprocessable Content` | Bean validation failure — `errors` map included |
| `429 Too Many Requests` | Rate limit exceeded — `Retry-After` header included |
| `500 Internal Server Error` | Unexpected server error |

### Pagination

Paginated endpoints return:

```json
{
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalPages": 5,
    "totalElements": 98
  }
}
```

- `page` is zero-indexed.
- Negative `page` values are clamped to `0`.
- `size` above the configured maximum is clamped (never returns `422`).
- `page` beyond the last page returns an empty `content: []` (never `404`).
- Global default page size: `PAGEABLE_DEFAULT_SIZE=20`, max: `PAGEABLE_MAX_SIZE=100`.

---

## 26. Environment Variables Reference

<!-- AUTO-GENERATED from .env.example -->

### Core Server

| Variable | Required | Default | Description |
|---|---|---|---|
| `PORT` | No | `8080` | HTTP server port |
| `COMPOSE_PROJECT_NAME` | No | `souklab` | Docker Compose project name |

### Database (MariaDB)

| Variable | Required | Default | Description |
|---|---|---|---|
| `DB_NAME` | Yes | `souklab_db` | Database name |
| `DB_URL` | Yes | `jdbc:mariadb://localhost:3306/...` | JDBC connection URL |
| `DB_USERNAME` | Yes | — | Database username |
| `DB_PASSWORD` | Yes | — | Database password |
| `DB_ROOT_PASSWORD` | Yes (Docker) | — | MariaDB root password (Docker only) |
| `JPA_HIBERNATE_DDL_AUTO` | No | `update` | Use `validate` in production with Flyway |
| `DB_POOL_MAX_SIZE` | No | `20` | HikariCP max pool size |
| `FLYWAY_ENABLED` | No | `false` | Enable Flyway migrations |

### JWT & Security

| Variable | Required | Default | Description |
|---|---|---|---|
| `APP_JWT_SECRET` | **Yes** | — | HS256 signing secret — minimum 32 characters |
| `APP_JWT_ACCESS_EXP` | No | `3600000` | Access token expiry in milliseconds (1 hour) |
| `APP_JWT_REFRESH_EXP` | No | `86400000` | Refresh token expiry in milliseconds (24 hours) |
| `AUTH_LOCKOUT_MAX_ATTEMPTS` | No | `5` | Failed login attempts before lockout |
| `AUTH_LOCKOUT_DURATION_MINUTES` | No | `15` | Lockout duration |
| `AUTH_VERIFICATION_MAX_ATTEMPTS` | No | `5` | Email PIN verification max attempts |
| `AUTH_VERIFICATION_EXPIRATION_MINUTES` | No | `15` | Email PIN expiry |

### File Storage (MinIO / S3)

| Variable | Required | Default | Description |
|---|---|---|---|
| `STORAGE_PROVIDER` | No | `s3` | Storage backend (`s3` only supported) |
| `STORAGE_S3_ENDPOINT` | Yes (S3) | `http://localhost:9000` | MinIO / S3 endpoint |
| `STORAGE_S3_BUCKET` | Yes (S3) | `souklab-files` | S3 bucket name |
| `STORAGE_S3_ACCESS_KEY` | Yes (S3) | — | S3 access key |
| `STORAGE_S3_SECRET_KEY` | Yes (S3) | — | S3 secret key |
| `STORAGE_MAX_FILE_SIZE` | No | `15MB` | Maximum upload size |
| `STORAGE_VIRUS_SCAN_ENABLED` | No | `false` | Enable ClamAV scanning |
| `STORAGE_VIRUS_SCAN_FAIL_OPEN` | No | `false` | Allow uploads if ClamAV unreachable |

### Email

| Variable | Required | Default | Description |
|---|---|---|---|
| `APP_EMAIL_USE_SMTP` | No | `true` | Use SMTP instead of MailerSend |
| `SMTP_HOST` | Yes (SMTP) | `localhost` | SMTP server host |
| `SMTP_PORT` | No | `1025` | SMTP server port |
| `MAILERSEND_API_KEY` | Yes (MailerSend) | — | MailerSend API key |
| `MAILERSEND_SENDER_EMAIL` | No | `noreply@souklab.dz` | Sender email address |

### Payments (Chargily)

| Variable | Required | Default | Description |
|---|---|---|---|
| `CHARGILY_API_KEY` | Yes (Payments) | — | Chargily API key |
| `CHARGILY_SECRET_KEY` | Yes (Payments) | — | Chargily webhook secret |
| `CHARGILY_ENABLED` | No | `false` | Enable Chargily integration |
| `SUBSCRIPTION_ENABLED` | No | `false` | Enable subscription system |
| `SUBSCRIPTION_CURRENCY` | No | `DZD` | Subscription billing currency |

### STOMP / RabbitMQ

| Variable | Required | Default | Description |
|---|---|---|---|
| `RELAY_HOST` | Yes (WS) | `localhost` | RabbitMQ STOMP relay host |
| `RELAY_PORT` | No | `61613` | RabbitMQ STOMP port |
| `RELAY_CLIENT_LOGIN` | Yes | — | STOMP client login |
| `RELAY_CLIENT_PASSCODE` | Yes | — | STOMP client passcode |

### Elasticsearch / Hibernate Search

| Variable | Required | Default | Description |
|---|---|---|---|
| `HIBERNATE_SEARCH_ENABLED` | No | `true` | Enable full-text search |
| `ELASTICSEARCH_URIS` | Yes (Search) | `http://localhost:9200` | Elasticsearch endpoint |
| `SEARCH_SYNC_ON_STARTUP` | No | `true` | Re-sync search index on startup |

---

## 27. Database Migrations

Flyway manages all schema changes. Migrations are **immutable** — applied migrations must never be edited.

<!-- AUTO-GENERATED from src/main/resources/db/migration/ -->

| Version | File | Description |
|---|---|---|
| `V0` | `V0__baseline_schema.sql` | Full baseline schema — users, artisans, formations, enrollments, catalog, etc. |
| `V1` | `V1__phase7_social.sql` | Social feed tables: posts, likes, comments, media |
| `V2` | `V2__authorization_permissions.sql` | Permission system: `authorization_permission` table |
| `V3` | `V3__phase8_messaging.sql` | Real-time messaging: conversations, messages, attachments |
| `V4` | `V4__production_query_indexes.sql` | Production query performance indexes |
| `V5` | `V5__phase9_subscriptions_payments.sql` | Subscriptions and payments: plans, subscriptions, payments, webhooks, refunds |
| `V6` | `V6__phase10_analytics.sql` | Analytics: raw events, rollups, job queue, job results |
| `V7` | `V7__phase10_analytics_outbox.sql` | Analytics event outbox pattern |
| `V8` | `V8__phase10_analytics_audit_action.sql` | Audit log actions for analytics |
| `V9` | `V9__phase10_analytics_artifacts.sql` | Analytics export artifact storage |
| `V10` | `V10__phase10_payment_origin.sql` | Payment origin tracking |
| `V11` | `V11__phase10_outbox_retry_schedule.sql` | Outbox retry scheduling columns |
| `V12` | `V12__phase10_analytics_maintenance_jobs.sql` | Analytics maintenance job tracking |
| `V13` | `V13__phase10_report_resolution_time.sql` | Report resolution time tracking |
| `V14` | `V14__phase10_financial_audit_actions.sql` | Financial audit log actions |

---

## 28. Verification Ledger

This section records the live verification campaigns conducted against the running Souklab backend instance.

### Live Verification Campaign — 2026-09-22

| Domain | Test Script | Total | Passed | Failed | Pass Rate |
|---|---|:---:|:---:|:---:|:---:|
| Auth & Identity | `scripts/verify-domain1-auth.py` | 64 | 64 | 0 | **100%** |
| Artisan & Client Profiles | `scripts/verify-domain2-profiles.py` | 45 | 45 | 0 | **100%** |
| Catalog & Directory | `scripts/verify-domain3-catalog.py` | 60 | 60 | 0 | **100%** |
| Formations & Masterclasses | `scripts/verify-domain4-formations.py` | 57 | 57 | 0 | **100%** |
| Social Feed, Reviews, Reports | `scripts/verify-domain5-feed-reports.py` | 80 | 80 | 0 | **100%** |
| Messaging & WebSocket | `scripts/verify-domain6-messaging.py` | 70 | 70 | 0 | **100%** |
| Notifications | `scripts/verify-domain7-notifications.py` | 36 | 36 | 0 | **100%** |
| Subscriptions & Plans | `scripts/verify-domain8-subscriptions.py` | 37 | 37 | 0 | **100%** |
| Admin & Moderation | `scripts/verify-domain9-admin.py` | 36 | 36 | 0 | **100%** |
| Avatars & File Storage | `scripts/verify-domain10-storage.py` | 41 | 41 | 0 | **100%** |
| Rate Limiting | `scripts/verify-domain11-ratelimit.py` | 40 | 40 | 0 | **100%** |
| **TOTAL** | | **566** | **566** | **0** | **100.0%** |

### Maven Test Suite — 2026-09-22

```
Tests run: 1228, Failures: 0, Errors: 0, Skipped: 10
BUILD SUCCESS
Total time: 02:27 min
```

### Code Fixes Applied During Verification

| File | Fix Description |
|---|---|
| `FormationIntegrationTest.java` | Updated content array assertions from fixed index `content[0].id` to Hamcrest `hasItem(formationId)` to handle pre-existing test data |
| `AvatarService.java` | Added `@Transactional` on public methods to prevent `TransactionRequiredException` from Spring proxy self-invocation |
| `RateLimitFilter.java` | Added `Retry-After` header on 429 responses, derived from Bucket4j refill estimation |
| `AvatarUploadRateLimitFilter.java` | Same `Retry-After` fix |
| `FileRateLimitFilter.java` | Same `Retry-After` fix |
| `UserRateLimitFilter.java` | Same `Retry-After` fix |
| `AuthService.java` | Added `@Transactional` on `logout` method |
| `ConversationService.java` | Guarded against null/empty attachment objects in message creation |
| `FeedPostService.java` | Ensured media ID assignment before persisting post media |

---

*Generated by Antigravity from live source tree. Source of truth: controller `@RequestMapping` annotations, model enums, `.env.example`, `docker-compose.yml`, and `pom.xml`.*
