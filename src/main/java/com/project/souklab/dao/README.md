# Data Access Object Layer (`com.project.souklab.dao`)

Spring Data JPA repository interfaces defining database queries, soft-delete filtering, and pagination contracts.

---

## Architecture & Data Flow

Repositories inherit from `JpaRepository` and `JpaSpecificationExecutor`. All queries touching entities with soft-delete support automatically include `deletedAt IS NULL` conditions to maintain data integrity.

```mermaid
graph TD
    Service["Service Layer"] --> Repo["Spring Data JPA Repository"]
    Repo --> Hibernate["Hibernate ORM"]
    Hibernate --> Hikari["HikariCP Connection Pool"]
    Hikari --> DB[("MariaDB / MySQL Database")]
```

---

## Repositories Reference (47 Repositories Across DAO Packages)

### Identity, Security & Auditing
| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`UserRepository`](UserRepository.java) | `User` | `findByEmail`, `existsByEmail`, `findByStatusAndDeletedAtIsNull`, search query filters. |
| [`AuthorizationPermissionRepository`](AuthorizationPermissionRepository.java) | `AuthorizationPermission` | Enabled permission lookup by key and bulk key lookup. |
| [`RefreshTokenRepository`](RefreshTokenRepository.java) | `RefreshToken` | `findByToken`, `deleteByUser`, revocation cleanup. |
| [`VerificationTokenRepository`](VerificationTokenRepository.java) | `VerificationToken` | `findActiveToken`, `invalidateActiveTokens` for email verification and password reset. |
| [`OAuthIdentityRepository`](OAuthIdentityRepository.java) | `OAuthIdentity` | `findByProviderAndProviderUserId`, OAuth account linking. |
| [`AuditLogRepository`](AuditLogRepository.java) | `AuditLog` | `findByActionOrderByCreatedAtDesc`, administrative audit queries. |

### Profiles & Portfolios
| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`ArtisanRepository`](ArtisanRepository.java) | `Artisan` | `findById`, `findByTeacherTrue`, `findByVerifiedTrue`, public directory lookups. |
| [`ClientRepository`](ClientRepository.java) | `Client` | `findById`, `findByPremiumTrue`. |
| [`ArtisanFormateurRequestRepository`](ArtisanFormateurRequestRepository.java) | `ArtisanFormateurRequest` | `findFirstByArtisanIdOrderByCreatedAtDesc`, `findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc`. |
| [`ArtisanProfileViewRepository`](ArtisanProfileViewRepository.java) | `ArtisanProfileView` | `existsByViewerIdAndArtisanId`, deduplicated profile view metrics. |
| [`UserAvatarRepository`](UserAvatarRepository.java) | `UserAvatar` | `findByUserIdOrderByCreatedAtDesc`, `countByUserId`, `findByUserIdAndIsActiveTrue`. |
| [`ArtisanCertificationRepository`](ArtisanCertificationRepository.java) | `ArtisanCertification` | `findByArtisanIdAndDeletedAtIsNullOrderByCreatedAtDesc`, credential management. |
| [`ArtisanGalleryImageRepository`](ArtisanGalleryImageRepository.java) | `ArtisanGalleryImage` | `findByArtisanIdAndDeletedAtIsNullOrderByDisplayOrderAsc`, showcase sequence queries. |
| [`NotificationRepository`](NotificationRepository.java) | `Notification` | `findByRecipientIdAndDeletedAtIsNullOrderByCreatedAtDesc`, `countByRecipientIdAndReadFalseAndDeletedAtIsNull`, query-scoped mark-read. |

### Reference Taxonomies
| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`RegionRepository`](RegionRepository.java) | `Region` | `findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc`, `findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc`, `existsBySlug`, `existsBySlugAndIdNot`, `existsByParentId`, `countByParentId`, `findAncestorIds` (recursive CTE cycle check). |
| [`JobCategoryRepository`](JobCategoryRepository.java) | `JobCategory` | `findByIsActiveTrueOrderByDisplayOrderAsc`, `existsBySlug`, `existsBySlugAndIdNot`, craftsmanship category hierarchy and subcategory existence checks. |
| [`JobSubCategoryRepository`](JobSubCategoryRepository.java) | `JobSubCategory` | `findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc`, `existsBySlug`, `existsBySlugAndIdNot`, `existsByCategoryId`, `countByCategoryId`, `isReferencedByArtisans`. |
| [`MaterialFamilyRepository`](MaterialFamilyRepository.java) | `MaterialFamily` | `findByIsActiveTrueOrderByDisplayOrderAsc`, `existsBySlug`, `existsBySlugAndIdNot`, raw material family hierarchy and child material existence checks. |
| [`MaterialRepository`](MaterialRepository.java) | `Material` | `findByFamilyIdAndIsActiveTrueOrderByDisplayOrderAsc`, `existsBySlug`, `existsBySlugAndIdNot`, `existsByFamilyId`, `countByFamilyId`, `isReferencedByArtisans`. |
| [`EpoqueRepository`](EpoqueRepository.java) | `Epoque` | `findByIsActiveTrueOrderByDisplayOrderAsc`, `existsBySlug`, `existsBySlugAndIdNot`, `isReferencedByArtisans`, chronological historical epochs. |
| [`TechniqueRepository`](TechniqueRepository.java) | `Technique` | `findByIsActiveTrueOrderByDisplayOrderAsc`, `existsBySlug`, `existsBySlugAndIdNot`, `isReferencedByArtisans`, traditional craftsmanship techniques. |

### Formations & Workshops
| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`FormationRepository`](FormationRepository.java) | `Formation` | `findByStatusAndDeletedAtIsNull`, `findByAuthorIdAndDeletedAtIsNull`, browse and author queries. |
| [`FormationEnrollmentRepository`](FormationEnrollmentRepository.java) | `FormationEnrollment` | `countByFormationIdAndStatus`, `findByFormationIdAndArtisanId`, seat capacity checks. |
| [`FormationFileRepository`](FormationFileRepository.java) | `FormationFile` | `findByFormationIdAndDeletedAtIsNull`, course syllabus attachments. |
| [`FormationReviewRepository`](FormationReviewRepository.java) | `FormationReview` | `findByFormationIdOrderByReviewedAtDesc`, administrative review history. |

### Social Feed, Reviews & Reports
| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`FeedPostRepository`](FeedPostRepository.java) | `FeedPost` | Public visibility, type filtering, author and moderation queue queries. |
| [`FeedPostMediaRepository`](FeedPostMediaRepository.java) | `FeedPostMedia` | Ordered post attachment lookup. |
| [`ArtisanReviewRepository`](ArtisanReviewRepository.java) | `ArtisanReview` | Visible review pages, enrollment uniqueness, average and count aggregates. |
| [`ContentReportRepository`](ContentReportRepository.java) | `ContentReport` | Status and target-type moderation queue filters. |

### Real-Time Messaging & Chat
| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`ConversationRepository`](ConversationRepository.java) | `Conversation` | `findBetweenUsers`, user conversation listings with latest activity sorting. |
| [`ConversationParticipantRepository`](ConversationParticipantRepository.java) | `ConversationParticipant` | Membership lookup, unread count queries, and read timestamp markers. |
| [`MessageRepository`](MessageRepository.java) | `Message` | Paginated message history, cursor traversal, and soft deletion. |
| [`MessageAttachmentRepository`](MessageAttachmentRepository.java) | `MessageAttachment` | File attachment metadata linked to chat messages. |
| [`MessageAttachmentUploadRepository`](MessageAttachmentUploadRepository.java) | `MessageAttachmentUpload` | Pre-signed upload reservation lifecycle tracking. |

### Subscriptions & Payments
| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`SubscriptionPlanRepository`](SubscriptionPlanRepository.java) | `SubscriptionPlan` | Active plans by target role (`ARTISAN`, `CLIENT`) and billing frequency. |
| [`ArtisanSubscriptionRepository`](ArtisanSubscriptionRepository.java) | `ArtisanSubscription` | Active artisan tier subscriptions, expiry dates, and renewals. |
| [`ClientSubscriptionRepository`](ClientSubscriptionRepository.java) | `ClientSubscription` | Active client subscriptions and premium feature gating. |
| [`PaymentRepository`](PaymentRepository.java) | `Payment` | Transaction tracking by gateway checkout ID, user ID, and payment status. |
| [`PaymentWebhookLogRepository`](PaymentWebhookLogRepository.java) | `PaymentWebhookLog` | Webhook idempotency event tracking and signature audit log. |

### Client Favorites
| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`ClientFavoriteArtisanRepository`](ClientFavoriteArtisanRepository.java) | `ClientFavoriteArtisan` | `findByClientIdAndArtisanId`, `existsByClientIdAndArtisanId`, `countByClientId`, `findByClientIdWithArtisan` (batch fetch), `deleteByClientIdAndArtisanId`. |

### Analytics & Outbox
For activity events, outbox queues, and aggregated KPI rollups, see the dedicated repositories in [`com.project.souklab.dao.analytics`](analytics/README.md) (`ActivityEventRepository`, `AnalyticsJobRepository`, `DailyKpiRollupRepository`, `AnalyticsOutboxRepository`, etc.).
