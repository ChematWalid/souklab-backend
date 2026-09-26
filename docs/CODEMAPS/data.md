<!-- Generated: 2026-09-25 | Files scanned: 510 | Token estimate: ~850 -->

# Data Architecture & Schema Codemap

## 1. Persistence Baseline

- **RDBMS**: MariaDB 11.4 (`utf8mb4_uca1400_ai_ci`)
- **Primary Keys**: 36-character UUID strings generated via `UUID.randomUUID()` in `BaseEntity`.
- **Auditing**: `createdAt` (`datetime(6)`), `updatedAt` (`datetime(6)`), and nullable `deletedAt` for soft-deletable entities.
- **ORM / DDL**: Spring Data JPA / Hibernate 6/7. Production enforces `spring.jpa.hibernate.ddl-auto=validate`.

## 2. Entity Map by Domain (53 JPA Entities)

### Identity & Access Control
- `User` (`users`): Core credentials, status (`PENDING`, `ACTIVE`, `SUSPENDED`, `REJECTED`), role seed (`ARTISAN`, `CLIENT`, `ADMIN`).
- `Permission` (`permissions`) & `UserPermission` (`user_permissions`): Capability-based authorization catalog.
- `RefreshToken` (`refresh_tokens`): Hashed refresh tokens with rotation and reuse revocation.
- `VerificationToken` (`verification_tokens`): 6-digit email verification PINs.
- `OAuthIdentity` (`oauth_identities`): External OAuth2 provider links (e.g. Google sub).
- `Client` (`clients`): Consumer profile details.
- `UserAvatar` (`user_avatars`): User avatar gallery records (max 10, active pointer).

### Artisan & Craft Taxonomy
- `Artisan` (`artisans`): Artisan public profile, bio, website, rating, `isTeacher`, `isVerified`, `isPremium`.
- `ArtisanGalleryImage` (`artisan_gallery_images`): Portfolio media (max 20 per artisan).
- `ArtisanCertification` (`artisan_certifications`): Verified documents (max 10 per artisan).
- `ArtisanProfileView` (`artisan_profile_views`): Deduplicated daily profile visit logs.
- `ArtisanFormateurRequest` (`artisan_formateur_requests`): Accreditation requests (`PENDING`, `APPROVED`, `REJECTED`).
- Taxonomy: `Region` (`regions`), `JobCategory` (`job_categories`), `JobSubCategory` (`job_sub_categories`), `MaterialFamily` (`material_families`), `Material` (`materials`), `Epoque` (`epoques`), `Technique` (`techniques`).

### Formations & Masterclasses
- `Formation` (`formations`): Masterclasses (`DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`, `PUBLISHED`).
- `FormationFile` (`formation_files`): Syllabus & course materials (max 10 per formation, protected).
- `FormationEnrollment` (`formation_enrollments`): Participant bookings (`CONFIRMED`, `ATTENDED`, `CANCELLED`).
  - *Constraint: Unique `(formation_id, artisan_id)`.*
- `FormationReview` (`formation_reviews`): Star rating and comment. Linked to attended enrollment.

### Social Feed & Content Moderation
- `FeedPost` (`feed_posts`): Moderated community updates with types (`ACTUALITE`, `FORMATION`, `ANNONCE`), lifecycle states, normalized tags, and atomic engagement counters.
- `FeedPostMedia` (`feed_post_media`): Attached images (max 10 per post).
- `FeedTag`/`feed_post_tags`: Reusable normalized tag entities and join table.
- `FeedPostLike`/`FeedPostBookmark`: Unique per-user post engagement records.
- `FeedPostComment`/`FeedPostCommentLike`: Root comments, one-level replies, and unique comment likes.
- `ContentReport` (`content_reports`): Abuse reports targeting posts, comments, users, or reviews.
- `AuditLog` (`audit_logs`): Immutable record of admin and moderation actions.

### Real-Time Messaging
- `Conversation` (`conversations`): Private 1-on-1 messaging threads.
- `ConversationParticipant` (`conversation_participants`): Member join table with archived/read state.
- `Message` (`messages`): Text payload, sender, read status, edit flag, soft-delete timestamp.
- `MessageAttachment` (`message_attachments`): Attached files (max 5 per message).

### Subscriptions & Payments
- `SubscriptionPlan` (`subscription_pricing`): Pricing tier definitions and billing periods in DZD.
- `SubscriptionPlanEntitlement` (`subscription_plan_entitlements`): Quotas and feature entitlements per plan.
- `ArtisanSubscription` (`artisan_subscriptions`): Active artisan subscriptions with period validity and auto-renewal flag.
- `ClientSubscription` (`client_subscriptions`): Active client subscriptions granting premium directory and messaging access.
- `Payment` (`payments`): Transaction records tracking amount, status (`PENDING`, `PAID`, `FAILED`), and Chargily checkout IDs.
- `PaymentWebhookLog` (`payment_webhook_logs`): Immutable audit ledger of provider webhooks for idempotency and signature verification.

### Analytics & KPIs
- `ActivityEvent` (`activity_events`): Telemetry event intent records captured across domain actions.
- `DailyKpiRollup` (`daily_kpi_rollups`): Time-bucket KPI aggregations.
- `AnalyticsJob` (`analytics_jobs`): Asynchronous calculation task and report execution tracking (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`).
- `AnalyticsJobArtifact` (`analytics_job_artifacts`): Exported CSV metadata in S3 storage.
- `AnalyticsOutboxEvent` (`analytics_outbox_events`): Reliable event publishing outbox table.
- `AnalyticsProcessedEvent` (`analytics_processed_events`): Idempotency tracker for processed analytics events.
- `AnalyticsMaintenanceJob` (`analytics_maintenance_jobs`): Automated retention & rollup cleanup logs.

### Client Favorites
- `ClientFavoriteArtisan` (`client_favorite_artisans`): Client artisan bookmarking records.
  - *Constraint: Unique `(client_id, artisan_id)`.*

## 3. Flyway Migration History (V0–V18)

| Version | Script Name | Scope |
|---|---|---|
| `V0` | `V0__baseline_schema.sql` | Core schema: users, artisans, catalog, formations, enrollments |
| `V1` | `V1__phase7_social.sql` | Social feed: posts, media, comments, likes |
| `V2` | `V2__authorization_permissions.sql` | Permission-based authorization model |
| `V3` | `V3__phase8_messaging.sql` | 1-on-1 messaging: conversations, messages, attachments |
| `V4` | `V4__production_query_indexes.sql` | Composite query performance indexes |
| `V5` | `V5__phase9_subscriptions_payments.sql` | Plans, subscriptions, payments, webhooks, refunds |
| `V6`–`V9` | `V6..V9__phase10_analytics*.sql` | Analytics raw events, rollups, job queue, outbox, artifacts |
| `V10`–`V14` | `V10..V14__phase10_*.sql` | Payment origins, retry scheduling, maintenance, resolution time, financial audits |
| `V15` | `V15__admin_catalog_permission.sql` | Admin catalog taxonomy management permission and audit action support |
| `V16` | `V16__client_favorites.sql` | Client favorite artisans table, indexes, and client favorites permission (`permission:client:favorites`) |
| `V17` | `V17__feed_social_enhancements.sql` | Feed lifecycle states, normalized tags, likes, bookmarks, comments/replies, counters, and comment reports |
| `V18` | `V18__feed_notification_types.sql` | Feed lifecycle and engagement notification enum values |
