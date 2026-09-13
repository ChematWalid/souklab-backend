# Data Model & Schema Specification

This document contains the complete database schema, JPA entity mappings, relationships, enums, and indexing rules for the **Souklab** platform.

---

## 1. Domain Enums

| Enum | Allowed Values | Description |
| :--- | :--- | :--- |
| **`AccountStatus`** | `PENDING`, `ACTIVE`, `SUSPENDED`, `REJECTED` | Lifecycle status of a user / artisan account |
| **`FormationStatus`** | `DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`, `PUBLISHED`, `CANCELLED`, `COMPLETED` | Course / Workshop approval lifecycle |
| **`EnrollmentStatus`** | `CONFIRMED`, `ATTENDED`, `CANCELLED` | Peer artisan workshop registration status |
| **`FormationReviewDecision`** | `APPROVED`, `REJECTED` | Administrative moderation verdict for formations |
| **`PostType`** | `FORMATION`, `ACTUALITE`, `ANNONCE` | Community feed item classification |
| **`PaymentStatus`** | `PENDING`, `SUCCEEDED`, `FAILED`, `REFUNDED`, `CANCELLED` | Transaction status |
| **`PaymentProvider`** | `CHARGILY`, `CARD`, `OTHER` | Payment gateway |
| **`ReportStatus`** | `PENDING`, `REVIEWED`, `RESOLVED`, `DISMISSED` | Moderation report status |
| **`SubscriptionStatus`**| `ACTIVE`, `EXPIRED`, `CANCELLED` | Active status of paid plans |
| **`SubscriptionPlan`** | `FREEMIUM`, `PREMIUM_MONTHLY`, `PREMIUM_YEARLY` | Tier of platform access |
| **`AdminAction`** | `APPROVED`, `REJECTED`, `SUSPENDED`, `REACTIVATED` | Admin audit action log |
| **`NotificationType`** | `ACCOUNT_VALIDATED`, `ACCOUNT_REJECTED`, `ACCOUNT_SUSPENDED`, `FORMATION_APPROVED`, `FORMATION_REJECTED`, `NEW_MESSAGE`, `SUBSCRIPTION_RENEWED`, `SUBSCRIPTION_EXPIRED`, `PAYMENT_SUCCESS`, `PAYMENT_FAILED`, `NEW_REPORT`, `NEW_REVIEW`, `NEW_FORMATION`, `FORMATEUR_REQUEST_SUBMITTED`, `FORMATEUR_APPROVED`, `FORMATEUR_GRANTED`, `FORMATEUR_REJECTED`, `FORMATEUR_REVOKED`, `ACCOUNT_REINSTATED` | Notification dispatcher event types |

---

## 2. Core Entities & Table Mappings

### 2.1 Common Base & Auditing
All entity classes inherit from `BaseEntity`:
- `id`: `Long` (Primary Key, Auto-increment Identity) or `String` UUID
- `createdAt`: `LocalDateTime` (Auto-generated on insert)
- `updatedAt`: `LocalDateTime` (Auto-updated on modification)
- `deletedAt`: `LocalDateTime` (Soft delete flag, nullable)
- `createdBy`: `String` (User email/ID)
- `updatedBy`: `String` (User email/ID)

---

### 2.2 Identity, Auth & Profiles

#### `users`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | PK | Unique User Identifier (UUID) |
| `email` | `VARCHAR(255)` | Unique, Not Null | Account email |
| `password_hash` | `VARCHAR(255)` | Nullable (for OAuth) | BCrypt hashed password |
| `first_name` | `VARCHAR(100)` | Nullable | User first name |
| `last_name` | `VARCHAR(100)` | Nullable | User last name |
| `name` | `VARCHAR(255)` | Not Null | Display name / full name |
| `phone` | `VARCHAR(30)` | Nullable | Contact telephone |
| `image` | `VARCHAR(500)` | Nullable | Avatar image URL |
| `email_verified` | `BOOLEAN` | Default FALSE | Email verification status |
| `email_verified_at` | `TIMESTAMP` | Nullable | Verification timestamp |
| `account_status` | `VARCHAR(30)` | Default 'PENDING' | Enum: `AccountStatus` |
| `is_premium` | `BOOLEAN` | Default FALSE | Premium subscription badge |
| `is_validated` | `BOOLEAN` | Default FALSE | Admin validation status |
| `role_id` | `VARCHAR(36)` | FK -> `roles.id` | Primary user role |
| `last_login_at` | `TIMESTAMP` | Nullable | Last active session |
| `last_login_ip` | `VARCHAR(45)` | Nullable | IP address of last login |

#### `roles` & `permissions`
- `roles`: `id`, `name` (e.g. `ROLE_ADMIN`, `ROLE_ARTISAN`, `ROLE_CLIENT`), `description`
- `permissions`: `id`, `resource` (e.g. `DIRECTORY`, `FORMATION`), `action` (e.g. `READ_FULL`, `CREATE`), `key` (e.g. `directory:read_full`)
- `role_permissions`: Join table mapping `role_id` <-> `permission_id`

#### `artisans`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | PK, FK -> `users.id` (1:1) | Shares PK with User |
| `bio` | `TEXT` | Nullable | Detailed biography & background |
| `region_id` | `VARCHAR(36)` | FK -> `regions.id` | Wilaya / Administrative region |
| `city` | `VARCHAR(100)` | Nullable | City / Commune |
| `address` | `VARCHAR(255)` | Nullable | Workshop street address |
| `website` | `VARCHAR(255)` | Nullable | Website / Social page |
| `sub_category_id` | `VARCHAR(36)` | FK -> `job_sub_categories.id` | Primary craft subcategory |
| `is_teacher` | `BOOLEAN` | Default FALSE | Offers masterclasses / courses |
| `is_premium` | `BOOLEAN` | Default FALSE | Active premium artisan plan |
| `is_verified` | `BOOLEAN` | Default FALSE | Certified by platform admins |
| `rating` | `FLOAT` | Default 0.0 | Average rating (1.0 to 5.0) |
| `reviews_count` | `INT` | Default 0 | Total reviews received |
| `views_count` | `INT` | Default 0 | Total profile views |
| `response_rate` | `INT` | Default 0 | Response percentage rate (0-100) |
| `portfolio` | `JSON` | Nullable | JSON array of portfolio projects |

#### `clients`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | PK, FK -> `users.id` (1:1) | Shares PK with User |
| `client_type_id` | `VARCHAR(36)` | FK -> `client_types.id` | Individual, Boutique, Enterprise, etc. |
| `company_name` | `VARCHAR(255)` | Nullable | Registered company / organization name |
| `region` | `VARCHAR(100)` | Nullable | Region / Wilaya |
| `city` | `VARCHAR(100)` | Nullable | City |
| `is_premium` | `BOOLEAN` | Default FALSE | Full directory access permission |
| `is_verified` | `BOOLEAN` | Default FALSE | Verified buyer badge |

#### `user_avatars`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | PK | Unique Avatar Identifier (UUID) |
| `user_id` | `VARCHAR(36)` | FK -> `users.id` | Owning User Identifier |
| `storage_key_original` | `VARCHAR(255)` | Not Null | S3 storage key for original tier (max 2000px) |
| `storage_key_medium` | `VARCHAR(255)` | Not Null | S3 storage key for medium tier (max 500px) |
| `storage_key_thumbnail` | `VARCHAR(255)` | Not Null | S3 storage key for thumbnail tier (max 150px) |
| `original_filename` | `VARCHAR(255)` | Not Null | Sanitized client original filename |
| `content_type` | `VARCHAR(50)` | Not Null | MIME type (`image/jpeg`, `image/png`, `image/webp`) |
| `file_size` | `BIGINT` | Not Null | Uploaded original image size in bytes |
| `is_active` | `BOOLEAN` | Default FALSE | Active profile avatar selection flag |
| `uploaded_at` | `TIMESTAMP` | Not Null | Avatar upload timestamp |

---

### 2.3 Catalog & Heritage Taxonomy

- `regions`: `id`, `name`, `slug` (Unique), `parent_id` (FK -> `regions.id`), `display_order`, `is_active`
- `job_categories`: `id`, `name`, `slug` (Unique), `description`, `icon_url`, `display_order`, `is_active`
- `job_sub_categories`: `id`, `category_id` (FK -> `job_categories.id`), `name`, `slug` (Unique), `description`, `display_order`, `is_active`
- `material_families`: `id`, `name`, `slug` (Unique), `display_order`
- `materials`: `id`, `family_id` (FK -> `material_families.id`), `name`, `slug` (Unique), `display_order`, `is_active`
- `epoques`: `id`, `name`, `slug` (Unique), `period`, `description`, `display_order`, `is_active`
- `techniques`: `id`, `name`, `slug` (Unique), `description`, `display_order`, `is_active`

#### Join Tables for Artisan Attributes & Heritage
- `artisan_materials` (`artisan_id`, `material_id`)
- `artisan_techniques` (`artisan_id`, `technique_id`)
- `artisan_epoques` (`artisan_id`, `epoque_id`)
- `epoques_materials` (`epoque_id`, `material_id`)
- `epoques_techniques` (`epoque_id`, `technique_id`)

---

### 2.4 Artisan Portfolio & Verification Artifacts

- `artisan_gallery_images`: `id`, `artisan_id` (FK), `image_url`, `caption`, `display_order`
- `artisan_certifications`: `id`, `artisan_id` (FK), `title`, `issuer`, `issued_at`, `expires_at`, `document_url`
- `artisan_achievements`: `id`, `artisan_id` (FK), `title`, `description`, `image_urls` (JSON), `completed_at`, `location`
- `artisan_social_links`: `id`, `artisan_id` (FK), `platform` (INSTAGRAM, FACEBOOK, YOUTUBE, TIKTOK), `url`
- `artisan_validations`: `id`, `artisan_id` (FK), `admin_id` (FK -> `users.id`), `action` (`AdminAction`), `note`, `performed_at`

---

### 2.5 Formations (Workshops & Masterclasses)

#### `formations`
- `id`: `VARCHAR(36)` (PK)
- `author_id`: `VARCHAR(36)` (FK -> `artisans.id`)
- `title`: `VARCHAR(255)` (Not Null)
- `description`: `TEXT` (Not Null)
- `thumbnail_url`: `VARCHAR(500)`
- `location`: `VARCHAR(255)` (Physical studio address or Online link)
- `is_online`: `BOOLEAN` (Default false)
- `scheduled_at`: `DATETIME(6)`
- `duration_hours`: `INT` (Default 0)
- `max_participants`: `INT` (Default 0)
- `price`: `INT` (in DZD, Default 0)
- `currency`: `VARCHAR(10)` (Default 'DZD')
- `status`: `VARCHAR(30)` (`FormationStatus`: `DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`, `PUBLISHED`, `CANCELLED`, `COMPLETED`)

#### `formation_files`
- `id`: `VARCHAR(36)` (PK)
- `formation_id`: `VARCHAR(36)` (FK -> `formations.id`)
- `storage_key`: `VARCHAR(255)` (Not Null)
- `original_filename`: `VARCHAR(255)` (Not Null)
- `content_type`: `VARCHAR(50)` (Not Null)
- `file_size`: `BIGINT` (Not Null)

#### `formation_enrollments` & `formation_reviews`
- `formation_enrollments`: `id`, `formation_id` (FK -> `formations.id`), `artisan_id` (FK -> `artisans.id`), `status` (`EnrollmentStatus`: `CONFIRMED`, `ATTENDED`, `CANCELLED`), `enrolled_at`, `cancelled_at`
- `formation_reviews`: `id`, `formation_id` (FK -> `formations.id`), `admin_id` (FK -> `users.id`), `decision` (`FormationReviewDecision`: `APPROVED`, `REJECTED`), `comment`, `reviewed_at`

---

### 2.6 Direct Messaging & Feed

- `conversations`: `id`, `last_message_at`, `created_at`, `updated_at`
- `conversation_participants`: `conversation_id`, `user_id`, `joined_at`, `last_read_at`
- `messages`: `id`, `conversation_id`, `sender_id`, `content`, `attachment_urls` (JSON), `status` (`SENT`, `DELIVERED`, `READ`), `sent_at`, `read_at`
- `feed_posts`: `id`, `author_id`, `type` (`PostType`), `title`, `content`, `image_urls` (JSON), `formation_id` (FK, nullable), `is_published`, `published_at`
- `reviews`: `id`, `artisan_id` (FK -> `users.id`), `client_id` (FK -> `users.id`), `rating` (1-5), `comment`
- `reports`: `id`, `reporter_user_id`, `target_user_id`, `target_message_id`, `target_post_id`, `reason`, `description`, `status`, `resolved_by`, `resolution`

---

### 2.7 Subscriptions & Chargily Pay Payments

- `subscription_pricing`: `id`, `plan_type` (`SubscriptionPlan`), `price` (in DZD), `currency`, `is_active`
- `client_subscriptions`: `id`, `client_id` (Unique, FK), `plan` (`SubscriptionPlan`), `status` (`SubscriptionStatus`), `started_at`, `expires_at`, `auto_renew`
- `artisan_subscriptions`: `id`, `artisan_id` (Unique, FK), `plan` (`SubscriptionPlan`), `status` (`SubscriptionStatus`), `started_at`, `expires_at`, `auto_renew`
- `payments`: `id`, `client_subscription_id`, `artisan_subscription_id`, `provider` (`CHARGILY`), `provider_payment_id`, `provider_checkout_url`, `amount`, `currency`, `status` (`PaymentStatus`), `paid_at`, `failure_reason`
- `payment_webhook_logs`: `id`, `payment_id`, `event`, `payload` (JSON), `received_at`, `is_processed`
