# Database Schema

The schema is defined by the JPA mappings in `com.project.souklab.model`; this document is an inventory rather than a hand-maintained SQL contract. Always inspect the entity annotation before authoring a migration.

## Tables mapped by the current code

| Area | Tables / mappings |
| --- | --- |
| Identity | `users`, `roles`, `user_roles`, `refresh_tokens`, `verification_tokens`, `oauth_identities`, `clients`, `user_avatars` |
| Artisan | `artisans`, `artisan_gallery_images`, `artisan_certifications`, `artisan_profile_views`, `artisan_formateur_requests`, and artisan taxonomy join tables |
| Catalog | `regions`, `job_categories`, `job_sub_categories`, `material_families`, `materials`, `epoques`, `techniques` |
| Formations | `formations`, `formation_files`, `formation_enrollments`, `formation_reviews` |
| Social | `feed_posts`, `feed_post_media`, `artisan_reviews`, `content_reports` |
| Operations | `notifications`, `audit_logs` |

All entities inherit the UUID and audit timestamp fields from `BaseEntity`. Soft-delete is represented by `deleted_at` only where the entity mapping includes that inherited field in persistence queries; join-table behavior and foreign-key actions are controlled by the annotations on each relationship.

## Constraints that affect application behavior

- Unique email and OAuth-provider identity constraints prevent duplicate account bindings.
- `formation_enrollments` has a unique `(formation_id, artisan_id)` constraint.
- Catalog slugs and other unique fields are declared in their entity `@Table` mappings.
- Upload records retain opaque storage keys; physical object deletion is coordinated after a successful database commit.

## Deployment rule

The default development configuration remains environment-driven. The `prod` Spring profile sets `spring.jpa.hibernate.ddl-auto=validate` and Hibernate Search schema management to `validate`. Production schema changes must be applied by a reviewed, versioned migration tool or SQL deployment step before the application is started. No migration history is assumed by this repository.
