# Data Model

The JPA classes under `src/main/java/com/project/souklab/model` are the authoritative data model. This page lists implemented identity, content, subscription, payment, and analytics concepts.

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
| Monetization | `ArtisanSubscription`, `ClientSubscription`, `SubscriptionPlan`, `SubscriptionPlanEntitlement`, `Payment`, `PaymentWebhookLog` |
| Analytics | `ActivityEvent`, `AnalyticsOutboxEvent`, `AnalyticsProcessedEvent`, `AnalyticsJob`, `AnalyticsJobArtifact`, `DailyKpiRollup`, `AnalyticsMaintenanceJob` |

## Important invariants

- User email is unique and account status controls authentication eligibility.
- `FormationEnrollment` has a unique `(formation_id, artisan_id)` constraint; enrollment capacity is checked while the formation row is pessimistically locked.
- Avatar, gallery, certification, and formation file uploads enforce configured quotas and validate storage content before persistence.
- Public media may be served without authentication; certifications and formation files are ownership/enrollment protected by `FileAccessService`.
- Soft-deleted records remain queryable only through explicit repository methods that include deleted rows.
- Catalog taxonomies enforce unique slug constraints (`SlugUtils.toSlug(name)`); duplicate slugs trigger 409 Conflict.
- Two-tier parent/child taxonomies (`JobCategory` -> `JobSubCategory`, `MaterialFamily` -> `Material`) maintain foreign key integrity with delete protection (409 Conflict if parent has active children).
- Self-referencing hierarchies (`Region` -> `parent_id`) guard against circular references via recursive CTE ancestors resolution (`RegionRepository.findAncestorIds`).

## Seeded reference data inventory

The reference catalog is seeded on startup when absent via `DataSeeder`:

- **Wilayas & Regions**: 58 Algerian wilayas (plus DZ country root and sample communes).
- **Job Categories & Subcategories**: 8 French construction/artisanat categories with 37 subcategories:
  - *Gros œuvre & structure* (5 subcategories)
  - *Couverture, étanchéité & zinguerie* (4 subcategories)
  - *Menuiserie, fermetures & agencement* (5 subcategories)
  - *Revêtements, finitions & décoration* (5 subcategories)
  - *Plomberie & systèmes techniques* (4 subcategories)
  - *Électricité & énergie* (4 subcategories)
  - *Métal & serrurerie* (5 subcategories)
  - *Aménagements extérieurs & paysage* (5 subcategories)
- **Material Families & Materials**: 6 Mediterranean material families with 25 materials:
  - *Pierre & roche* (5 materials)
  - *Terre & céramique* (4 materials)
  - *Bois & dérivés* (4 materials)
  - *Métaux & alliages* (4 materials)
  - *Chaux, plâtre & liants traditionnels* (4 materials)
  - *Fibres, végétaux & isolants naturels* (4 materials)
- **Epoques**: 14 historical eras spanning Antiquity, Islamic periods, Ottoman, and Modern/Contemporary craft periods.
- **Techniques**: 20 traditional craftsmanship techniques (carving, weaving, joinery, smithing, ceramics, etc.).

## Enumerations

The current source defines enums including `AccountStatus`, `EnrollmentStatus`, `FormationStatus`, `FormateurRequestStatus`, `FormationReviewDecision`, `NotificationType`, `AuditLogAction`, and `VerificationTokenType`. Refer to each enum for the exact values; documentation must not invent values for future modules.

## Schema maintenance

Local development may use the configurable Hibernate DDL mode. Production uses `application-prod.properties`, which sets Hibernate schema handling to `validate`; schema creation and alterations must therefore be supplied by a reviewed migration process outside the application.
