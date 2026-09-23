# Database Schema

The schema is defined by the JPA mappings in `com.project.souklab.model`; this document is an inventory rather than a hand-maintained SQL contract. Always inspect the entity annotation before authoring a migration.

## Tables mapped by the current code

| Area | Tables / mappings |
| --- | --- |
| Identity | `users`, `permissions`, `user_permissions`, `refresh_tokens`, `verification_tokens`, `oauth_identities`, `clients`, `user_avatars` |
| Artisan | `artisans`, `artisan_gallery_images`, `artisan_certifications`, `artisan_profile_views`, `artisan_formateur_requests`, and artisan taxonomy join tables |
| Catalog | `regions`, `job_categories`, `job_sub_categories`, `material_families`, `materials`, `epoques`, `techniques` |
| Formations | `formations`, `formation_files`, `formation_enrollments`, `formation_reviews` |
| Social | `feed_posts`, `feed_post_media`, `artisan_reviews`, `content_reports` |
| Messaging | `conversations`, `conversation_participants`, `messages`, `message_attachments` |
| Subscriptions & Payments | `subscription_plans`, `subscriptions`, `subscription_payments`, `chargily_webhook_events`, `subscription_refunds` |
| Analytics | `analytics_raw_events`, `analytics_rollups`, `analytics_job_runs`, `analytics_outbox_events`, `analytics_artifact_records`, `analytics_maintenance_jobs` |
| Operations | `notifications`, `audit_logs` |

All entities inherit the UUID and audit timestamp fields from `BaseEntity`. Soft-delete is represented by `deleted_at` only where the entity mapping includes that inherited field in persistence queries; join-table behavior and foreign-key actions are controlled by the annotations on each relationship.

## Constraints that affect application behavior

- Unique email and OAuth-provider identity constraints prevent duplicate account bindings.
- `formation_enrollments` has a unique `(formation_id, artisan_id)` constraint.
- Catalog slugs and other unique fields are declared in their entity `@Table` mappings.
- Upload records retain opaque storage keys; physical object deletion is coordinated after a successful database commit.

## Database Migrations & Deployment

Schema changes are versioned and managed using **Flyway**. The repository maintains 16 versioned migrations (`V0` through `V15`) located in `src/main/resources/db/migration/`:
- `V0`: Baseline schema (users, artisans, catalog, formations, enrollments)
- `V1`: Social feed tables (posts, media, comments, likes)
- `V2`: Authorization permissions (`permissions`, `user_permissions`)
- `V3`: Messaging tables (conversations, messages, attachments)
- `V4`: Production query performance indexes
- `V5`: Subscriptions and Chargily Pay V2 payments
- `V6`–`V14`: Analytics raw events, rollups, job queue, outbox patterns, and audit action tracking
- `V15`: Admin catalog taxonomy management permission (`permission:admin:catalog`)

The `prod` Spring profile sets `spring.jpa.hibernate.ddl-auto=validate` and Hibernate Search schema management to `validate`. Production schema changes must be applied via Flyway (`FLYWAY_ENABLED=true`) prior to application startup. Applied migrations are immutable and must never be modified.
