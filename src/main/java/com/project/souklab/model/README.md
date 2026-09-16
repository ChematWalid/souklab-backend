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
    User }o--o{ Role : "holds"
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
```

---

## Entities & Enums Reference (34 Models)

### Lifecycle & Identity Core
| Class / Enum | Type | Description |
| :--- | :---: | :--- |
| [`BaseEntity`](BaseEntity.java) | `@MappedSuperclass` | Auto-generated UUID `id`, `createdAt`, `updatedAt`, and soft-delete `deletedAt` timestamps. |
| [`User`](User.java) | `@Entity` | Central identity: email, password, `AccountStatus`, ban tracking, roles. |
| [`Client`](Client.java) | `@Entity` | Client profile: client type, company name, premium membership status. |
| [`Role`](Role.java) | `@Entity` | Platform authority (`ROLE_CLIENT`, `ROLE_ARTISAN`, `ROLE_ADMIN`). |
| [`RefreshToken`](RefreshToken.java) | `@Entity` | Long-lived secure token for JWT rotation with expiry tracking. |
| [`VerificationToken`](VerificationToken.java) | `@Entity` | Single-use 6-digit OTP codes for email activation and password resets. |
| [`OAuthIdentity`](OAuthIdentity.java) | `@Entity` | Third-party OAuth provider binding (Google OAuth2 subject ID). |
| [`AuditLog`](AuditLog.java) | `@Entity` | Administrative audit trail capturing security events and moderation actions. |
| [`AccountStatus`](AccountStatus.java) | `enum` | Account states: `PENDING`, `ACTIVE`, `SUSPENDED`. |
| [`AuditLogAction`](AuditLogAction.java) | `enum` | Audit codes (`APPROVE_USER`, `BAN_USER`, `TIMEOUT_USER`, `EMAIL_VERIFIED`). |
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
| [`NotificationType`](NotificationType.java) | `enum` | Notification triggers (`ACCOUNT_VALIDATED`, `FORMATEUR_GRANTED`, etc.). |

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
