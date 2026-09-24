# Domain Model Layer (`com.project.souklab.model`)

JPA entity models, enums, and base lifecycle abstractions mapped to MariaDB/MySQL tables.

---

## Entity Relationship Diagram

```mermaid
erDiagram
    User ||--o| Artisan : "specializes as"
    User ||--o| Client : "specializes as"
    User ||--o{ UserAvatar : "owns gallery"
    User ||--o{ Notification : "receives"
    User ||--o{ RefreshToken : "owns sessions"
    User ||--o{ VerificationToken : "owns verification"
    User ||--o{ OAuthIdentity : "links OAuth"
    User }o--o{ AuthorizationPermission : "holds"
    Artisan ||--o{ ArtisanFormateurRequest : "submits"
    Artisan ||--o{ ArtisanProfileView : "tracked views"
    Artisan ||--o{ ArtisanCertification : "holds credentials"
    Artisan ||--o{ ArtisanGalleryImage : "showcases portfolio"
    Artisan ||--o{ Formation : "authors masterclasses"
    Artisan ||--o{ FormationEnrollment : "participates in"
    Formation ||--o{ FormationEnrollment : "enrollments"
    Formation ||--o{ FormationFile : "course documents"
    Formation ||--o{ FormationReview : "moderation verdicts"
    Artisan }o--o| Region : "located in"
    Artisan }o--o| JobSubCategory : "craft specialization"
    Artisan }o--o{ Material : "works with"
    Artisan }o--o{ Technique : "employs"
    Artisan }o--o{ Epoque : "inspired by"
    JobCategory ||--o{ JobSubCategory : "subcategories"
    MaterialFamily ||--o{ Material : "materials"
    Client ||--o{ ClientFavoriteArtisan : "bookmarks"
    Artisan ||--o{ ClientFavoriteArtisan : "favorited in"
```

---

## Entities & Enums Reference (Comprehensive Domain Models)

### Lifecycle & Identity Core
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`BaseEntity`](BaseEntity.java) | `@MappedSuperclass` | Auto-generated UUID `id`, `createdAt`, `updatedAt`, and soft-delete `deletedAt` timestamps. |
| [`User`](User.java) | `@Entity` | Central identity: email, password, `AccountStatus`, ban tracking, and direct permissions. |
| [`Client`](Client.java) | `@Entity` | Client profile: client type, company name, premium membership status. |
| [`AuthorizationPermission`](AuthorizationPermission.java) | `@Entity` | Persisted capability assigned directly to users and evaluated by the security layer. |
| [`RefreshToken`](RefreshToken.java) | `@Entity` | Long-lived secure token for JWT rotation with expiry tracking. |
| [`VerificationToken`](VerificationToken.java) | `@Entity` | Single-use 6-digit OTP codes for email activation and password resets. |
| [`OAuthIdentity`](OAuthIdentity.java) | `@Entity` | Third-party OAuth provider binding (Google OAuth2 subject ID). |
| [`AuditLog`](AuditLog.java) | `@Entity` | Administrative audit trail capturing security events and moderation actions. |
| [`AccountStatus`](AccountStatus.java) | `enum` | Account states: `PENDING`, `ACTIVE`, `SUSPENDED`. |
| [`AuditLogAction`](AuditLogAction.java) | grouped enum taxonomy | Audit codes grouped by domain (`AuditLogAction.User`, `AuditLogAction.Catalog`, `AuditLogAction.Authentication`, `AuditLogAction.Subscription`, `AuditLogAction.Formation`, `AuditLogAction.Analytics`). |
| [`VerificationTokenType`](VerificationTokenType.java) | `enum` | Token categories: `EMAIL_VERIFICATION`, `PASSWORD_RESET`. |

### Artisan Profiles & Portfolios
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`Artisan`](Artisan.java) | `@Entity` | Artisan details: bio, ratings, `isTeacher`, views, craft associations. |
| [`ArtisanCertification`](ArtisanCertification.java) | `@Entity` | Official qualification/certificate with credential title, issuer, dates, document URL. |
| [`ArtisanGalleryImage`](ArtisanGalleryImage.java) | `@Entity` | Portfolio showcase photograph with caption and display sequence order. |
| [`ArtisanProfileView`](ArtisanProfileView.java) | `@Entity` | Deduplicated profile impression tracking unique viewer-artisan pairs. |
| [`ArtisanFormateurRequest`](ArtisanFormateurRequest.java) | `@Entity` | Teacher accreditation request with review notes and reapply cooldown date. |
| [`UserAvatar`](UserAvatar.java) | `@Entity` | Gallery avatar entity with thumbnail, medium, and full resolution URLs. |
| [`Notification`](Notification.java) | `@Entity` | In-app notification with recipient reference, message, type, and read flag. |
| [`FormateurRequestStatus`](FormateurRequestStatus.java) | `enum` | Accreditation states: `PENDING`, `APPROVED`, `REJECTED`. |
| [`NotificationType`](NotificationType.java) | grouped enum taxonomy | Notification triggers grouped by domain (`NotificationType.Account.VALIDATED`, `NotificationType.Formateur.GRANTED`, etc.). |

### Reference Taxonomies
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`Region`](Region.java) | `@Entity` | Administrative geography: Wilayas (parent is null) and child Communes. |
| [`JobCategory`](JobCategory.java) | `@Entity` | Top-level craft category (e.g. Pottery, Leathercraft, Weaving). |
| [`JobSubCategory`](JobSubCategory.java) | `@Entity` | Specialized subcategory belonging to a parent category. |
| [`MaterialFamily`](MaterialFamily.java) | `@Entity` | Material grouping (e.g. Clay & Ceramics, Metals, Textiles). |
| [`Material`](Material.java) | `@Entity` | Specific raw craft material belonging to a family. |
| [`Epoque`](Epoque.java) | `@Entity` | Traditional historical period and cultural era in Algerian heritage. |
| [`Technique`](Technique.java) | `@Entity` | Traditional craftsmanship method or technique. |

### Formations & Workshops
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`Formation`](Formation.java) | `@Entity` | Masterclass entity: title, description, schedule, capacity, price, status, author. |
| [`FormationEnrollment`](FormationEnrollment.java) | `@Entity` | Artisan workshop reservation with participant reference and status. |
| [`FormationFile`](FormationFile.java) | `@Entity` | Course document or syllabus attachment with storage key and MIME type. |
| [`FormationReview`](FormationReview.java) | `@Entity` | Administrative moderation record with decision and reviewer comment. |
| [`FormationStatus`](FormationStatus.java) | `enum` | Formation states: `DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`, `PUBLISHED`. |
| [`EnrollmentStatus`](EnrollmentStatus.java) | `enum` | Enrollment states: `CONFIRMED`, `ATTENDED`, `CANCELLED`. |
| [`FormationReviewDecision`](FormationReviewDecision.java) | `enum` | Admin verdicts: `APPROVED`, `REJECTED`. |

### Social Feed, Reviews & Reports
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`FeedPost`](FeedPost.java) | `@Entity` | Moderated public community post. |
| [`FeedPostMedia`](FeedPostMedia.java) | `@Entity` | Provider-neutral image attachment for a feed post. |
| [`ArtisanReview`](ArtisanReview.java) | `@Entity` | Decimal-rated review linked to an attended formation enrollment. |
| [`ContentReport`](ContentReport.java) | `@Entity` | Auditable report targeting a user, post, or review. |
| [`FeedPostType`](FeedPostType.java), [`FeedPostStatus`](FeedPostStatus.java) | `enum` | Feed categorization and moderation visibility states. |
| [`ReviewStatus`](ReviewStatus.java), [`ReportTargetType`](ReportTargetType.java), [`ReportStatus`](ReportStatus.java), [`ReportResolutionAction`](ReportResolutionAction.java) | `enum` | Review visibility, report target, lifecycle, and resolution states. |

### Real-Time Messaging & Chat
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`Conversation`](Conversation.java) | `@Entity` | 1-on-1 private messaging channel between two participants. |
| [`ConversationParticipant`](ConversationParticipant.java) | `@Entity` | Association between a user and conversation tracking unread counts and read markers. |
| [`Message`](Message.java) | `@Entity` | Chat message containing text content, soft-delete timestamp, and edit history. |
| [`MessageAttachment`](MessageAttachment.java) | `@Entity` | Image or document attached to a chat message. |
| [`MessageAttachmentUpload`](MessageAttachmentUpload.java) | `@Entity` | Pre-signed upload tracking reservation for attachments. |

### Subscriptions & Payments
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`SubscriptionPlan`](SubscriptionPlan.java) | `@Entity` | Platform subscription tiers (`FREE`, `PRO`, `PREMIUM`) with DZD pricing and entitlements. |
| [`SubscriptionPlanEntitlement`](SubscriptionPlanEntitlement.java) | `@Entity` | Entitlement limits (e.g. portfolio images, active formations) linked to a plan. |
| [`ArtisanSubscription`](ArtisanSubscription.java) | `@Entity` | Active artisan subscription period, renewal state, and tier reference. |
| [`ClientSubscription`](ClientSubscription.java) | `@Entity` | Client membership subscription granting premium perks. |
| [`Payment`](Payment.java) | `@Entity` | Financial transaction record tracking Chargily checkout ID, amount, and status. |
| [`PaymentWebhookLog`](PaymentWebhookLog.java) | `@Entity` | Immutable audit log of received webhook events for signature verification and idempotency. |
| [`BillingPeriod`](BillingPeriod.java), [`PaymentProvider`](PaymentProvider.java), [`PaymentStatus`](PaymentStatus.java), [`SubscriptionStatus`](SubscriptionStatus.java), [`WebhookProcessingStatus`](WebhookProcessingStatus.java) | `enum` | Billing cycle, payment provider, transaction status, subscription lifecycle, and webhook states. |

### Client Favorites
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`ClientFavorite`](ClientFavorite.java) | `@MappedSuperclass` | Base mapped superclass for client favorites with `client` association, timestamps, and UUID id. |
| [`ClientFavoriteArtisan`](ClientFavoriteArtisan.java) | `@Entity` | Client favorite artisan bookmark linking a client to a favorited artisan. |
| [`FavoriteType`](FavoriteType.java) | `enum` | Extensible favorite category discriminator (`ARTISAN`). |

### Analytics & Outbox
For raw activity events, outbox queues, and aggregated daily KPI rollups, see the dedicated models in [`com.project.souklab.model.analytics`](analytics/README.md) (`ActivityEvent`, `AnalyticsJob`, `AnalyticsOutboxEvent`, `DailyKpiRollup`, etc.).
