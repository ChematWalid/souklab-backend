# Data Model

The JPA classes under `src/main/java/com/project/souklab/model` are the authoritative data model. This page intentionally lists implemented concepts only; proposed feed, messaging, and payment models belong in the roadmap until code exists.

## Common lifecycle

`BaseEntity` is a mapped superclass providing a UUID string primary key (`VARCHAR(36)`), `createdAt`, `updatedAt`, and nullable `deletedAt`. IDs are generated with `UUID.randomUUID()` before persistence. Timestamp precision and column lengths are defined by each entity mapping and should be inspected before writing migrations.

## Implemented aggregates

| Aggregate | Entities |
| --- | --- |
| Identity and access | `User`, `AuthorizationPermission`, `RefreshToken`, `VerificationToken`, `OAuthIdentity`, `UserAvatar`, `Client` |
| Artisan | `Artisan`, `ArtisanCertification`, `ArtisanGalleryImage`, `ArtisanProfileView`, `ArtisanFormateurRequest` |
| Catalog | `Region`, `JobCategory`, `JobSubCategory`, `MaterialFamily`, `Material`, `Epoque`, `Technique` |
| Formations | `Formation`, `FormationFile`, `FormationEnrollment`, `FormationReview` |
| Operations | `Notification`, `AuditLog` |

## Important invariants

- User email is unique and account status controls authentication eligibility.
- `FormationEnrollment` has a unique `(formation_id, artisan_id)` constraint; enrollment capacity is checked while the formation row is pessimistically locked.
- Avatar, gallery, certification, and formation file uploads enforce configured quotas and validate storage content before persistence.
- Public media may be served without authentication; certifications and formation files are ownership/enrollment protected by `FileAccessService`.
- Soft-deleted records remain queryable only through explicit repository methods that include deleted rows.

## Enumerations

The current source defines enums including `AccountStatus`, `EnrollmentStatus`, `FormationStatus`, `FormateurRequestStatus`, `FormationReviewDecision`, `NotificationType`, `AuditLogAction`, and `VerificationTokenType`. Refer to each enum for the exact values; documentation must not invent values for future modules.

## Schema maintenance

Local development may use the configurable Hibernate DDL mode. Production uses `application-prod.properties`, which sets Hibernate schema handling to `validate`; schema creation and alterations must therefore be supplied by a reviewed migration process outside the application.
