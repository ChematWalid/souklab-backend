# Production Database Schema Specification

This document details the production-grade relational database schema for the **Souklab** platform. It defines all table structures, columns, data types, constraints, foreign key relationships, fetch/cascade rules, and performance indexes across all domain modules.

---

## 1. Global Architectural & Schema Conventions

### 1.1 Identifiers & Primary Keys
- **UUID Primary Keys**: Every entity uses a 36-character UUID (`VARCHAR(36)`), generated at the application layer (`java.util.UUID.randomUUID().toString()`) prior to persistence.
- Sequential auto-increment integer IDs are **never** used as primary keys, preventing ID enumeration in public APIs and ensuring seamless distributed-system compatibility.

### 1.2 Base Entity & Auditing Columns
Every table representing a full domain entity includes standard auditing columns:
- `id`: `VARCHAR(36)` — Primary Key.
- `created_at`: `DATETIME(6)` `NOT NULL` — UTC timestamp of creation.
- `updated_at`: `DATETIME(6)` `NOT NULL` — UTC timestamp of last update.
- `deleted_at`: `DATETIME(6)` `NULL` — Soft-delete timestamp (only on soft-deletable entities).

### 1.3 Soft-Delete vs. Hard-Delete Policy
| Classification | Soft-Delete (`deleted_at` present) | Hard-Delete / Immutable Append-Only | Rationale |
| :--- | :--- | :--- | :--- |
| **Domain Entities** | `users`, `artisans`, `clients`, `formations`, `formation_files`, `feed_posts`, `artisan_gallery_images`, `artisan_certifications`, `artisan_achievements`, `artisan_social_links`, `conversations`, `messages`, `reviews` | — | User-facing assets that users or artisans can delete or archive, while preserving referential integrity and audit trails. |
| **Join / Link Tables, Auth Links & Avatars** | — | `user_roles`, `oauth_identities`, `verification_tokens`, `user_avatars`, `artisan_materials`, `artisan_techniques`, `artisan_epoques`, `conversation_participants` | Pure junction tables, third-party identity bindings, and user avatars. Associations, linked OAuth accounts, and gallery avatars are unlinked or hard-deleted directly to purge physical storage and enforce quotas. |
| **Financial & Ledger** | — | `payments`, `client_subscriptions`, `artisan_subscriptions`, `subscription_pricing` | Financial transaction history must remain immutable. Subscriptions transition to `CANCELLED` or `EXPIRED` status rather than being deleted. |
| **Auditing & Moderation** | — | `audit_logs`, `payment_webhook_logs`, `artisan_validations`, `formation_reviews`, `formation_enrollments`, `reports`, `notifications` | Append-only security and administrative decision records. Must never be altered or deleted. |

### 1.4 Naming Rules
- **Tables**: `snake_case`, pluralized (e.g., `artisans`, `formation_enrollments`).
- **Columns**: `snake_case` (e.g., `is_verified`, `sub_category_id`).
- **Foreign Keys**: `<singular_referenced_table>_id` or descriptive semantic prefix (e.g., `author_id`, `parent_id`, `resolved_by`).
- **Indexes**: `idx_<table_name>_<column(s)>` (or `uk_<table_name>_<column(s)>` for unique constraints).

---

## 2. Identity & Authentication

### 2.1 `users`
Core identity record for all platform actors (Admins, Artisans, and Clients).

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `email` | `VARCHAR(255)` | `NO` | — | Unique login identifier (normalized lowercase) |
| `password` | `VARCHAR(255)` | `YES` | `NULL` | BCrypt password hash (nullable for OAuth-only users). Application layer enforces: at least one authentication method must exist (`password != null` OR >= 1 linked `oauth_identities` record). |
| `first_name` | `VARCHAR(100)` | `YES` | `NULL` | User given name |
| `last_name` | `VARCHAR(100)` | `YES` | `NULL` | User family name |
| `phone` | `VARCHAR(30)` | `YES` | `NULL` | Contact telephone number |
| `avatar_url` | `VARCHAR(500)` | `YES` | `NULL` | Avatar image storage path or CDN URL |
| `status` | `VARCHAR(30)` | `NO` | `'PENDING'` | Enum: `PENDING`, `ACTIVE`, `SUSPENDED`, `REJECTED` |
| `email_verified` | `BOOLEAN` | `NO` | `FALSE` | Email verification flag |
| `email_verified_at` | `DATETIME(6)` | `YES` | `NULL` | Timestamp of email verification |
| `last_login_at` | `DATETIME(6)` | `YES` | `NULL` | Timestamp of latest login session |
| `last_login_ip` | `VARCHAR(45)` | `YES` | `NULL` | IPv4 / IPv6 address of last login |
| `banned_until` | `DATETIME(6)` | `YES` | `NULL` | Temporary suspension expiration date |
| `ban_reason` | `VARCHAR(255)` | `YES` | `NULL` | Administrative moderation note |
| `failed_login_attempts` | `INT` | `NO` | `0` | Consecutive failed login attempts counter |
| `locked_until` | `DATETIME(6)` | `YES` | `NULL` | Automatic temporary lockout expiration timestamp |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `ManyToMany` with `Role` via `user_roles` (owning side: `User`, fetch: `EAGER` for Spring Security authentication, cascade: none).
  - `OneToMany` with `OAuthIdentity` (mappedBy: `user`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToOne` with `Artisan` (mappedBy: `user`, fetch: `LAZY`, cascade: `ALL`).
  - `OneToOne` with `Client` (mappedBy: `user`, fetch: `LAZY`, cascade: `ALL`).
  - `OneToOne` with `RefreshToken` (mappedBy: `user`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToMany` with `Notification` (mappedBy: `recipient`, fetch: `LAZY`, cascade: `ALL`).
- **Indexes & Unique Constraints**:
  - `uk_users_email` (`email`): Unique index for login lookup and preventing duplicate accounts.
  - `idx_users_status` (`status`, `deleted_at`): Indexed for admin user moderation queries.
  - `idx_users_phone` (`phone`): Indexed for phone search/verification lookups.

---

### 2.2 `roles`
System authorization roles (`ROLE_ADMIN`, `ROLE_ARTISAN`, `ROLE_CLIENT`).

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `name` | `VARCHAR(50)` | `NO` | — | Unique role name (`ROLE_ADMIN`, `ROLE_ARTISAN`, `ROLE_CLIENT`) |
| `description` | `VARCHAR(255)` | `YES` | `NULL` | Human-readable role description |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

- **Relationships**:
  - `ManyToMany` with `User` (mappedBy: `roles`, fetch: `LAZY`).
- **Indexes & Unique Constraints**:
  - `uk_roles_name` (`name`): Unique constraint to prevent duplicate role definitions.

---

### 2.3 `user_roles`
Junction table mapping users to their system roles.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `user_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `users.id` (`ON DELETE CASCADE`) |
| `role_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `roles.id` (`ON DELETE RESTRICT`) |

- **Indexes**:
  - Primary Key on (`user_id`, `role_id`).
  - `idx_user_roles_role_id` (`role_id`): Indexed for fast reverse lookups (e.g. finding all admin users).

---

### 2.4 `refresh_tokens`
Opaque refresh tokens for rolling JWT session renewal.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `user_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE CASCADE`) |
| `token` | `VARCHAR(255)` | `NO` | — | Secure cryptographic random token string |
| `expiry_date` | `DATETIME(6)` | `NO` | — | Expiration timestamp (86,400,000 ms / 24h default) |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |

- **Relationships**:
  - `OneToOne` with `User` (owning side: `RefreshToken`, fetch: `LAZY`, foreign key column: `user_id`).
- **Indexes & Unique Constraints**:
  - `uk_refresh_tokens_token` (`token`): Unique index for O(1) token validation on refresh requests.
  - `idx_refresh_tokens_user_id` (`user_id`): Indexed for fast session invalidation upon user logout or password change.
  - `idx_refresh_tokens_expiry` (`expiry_date`): Indexed for periodic cleanup cron jobs of expired tokens.

---

### 2.5 `oauth_identities`
External social provider identity mappings (Google, Facebook, Apple) linked to user accounts.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `user_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE CASCADE`) |
| `provider` | `VARCHAR(50)` | `NO` | — | Provider identifier (e.g. `'GOOGLE'`, `'FACEBOOK'`, `'APPLE'`) |
| `provider_user_id` | `VARCHAR(255)` | `NO` | — | Subject identifier provided by OAuth provider (e.g. Google `sub`) |
| `email` | `VARCHAR(255)` | `YES` | `NULL` | Email reported by OAuth provider (used for verified email auto-link) |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |

- **Relationships**:
  - `ManyToOne` with `User` (owning side: `OAuthIdentity`, fetch: `LAZY`, foreign key column: `user_id`).
- **Indexes & Unique Constraints**:
  - `uk_oauth_provider_user` (`provider`, `provider_user_id`): **Unique constraint**. Guarantees that a specific third-party account cannot be linked to more than one user account.
  - `idx_oauth_user_id` (`user_id`): Fast retrieval of all linked social accounts for a user profile.
  - `idx_oauth_email` (`email`): Lookup index supporting the auto-link-by-verified-email authentication flow.

---

### 2.6 `verification_tokens`
Cryptographically hashed single-use verification and password-reset codes with attempt limits.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `user_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE CASCADE`) |
| `type` | `VARCHAR(50)` | `NO` | — | Enum: `EMAIL_VERIFICATION`, `PASSWORD_RESET` |
| `code_hash` | `VARCHAR(255)` | `NO` | — | SHA-256 hex digest of the 6-digit verification code |
| `expires_at` | `DATETIME(6)` | `NO` | — | Expiration timestamp (now + 15 minutes) |
| `used_at` | `DATETIME(6)` | `YES` | `NULL` | Timestamp when the code was successfully verified/consumed |
| `attempts` | `INT` | `NO` | `0` | Failed verification attempt count for lockout protection |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Inherited soft-delete column |

- **Relationships**:
  - `ManyToOne` with `User` (owning side: `VerificationToken`, fetch: `LAZY`, foreign key column: `user_id`).
- **Indexes & Unique Constraints**:
  - `idx_verification_tokens_user_type` (`user_id`, `type`, `used_at`): Composite index for fast active token lookups and invalidations.

---

### 2.7 `user_avatars`
User avatar gallery assets with references to processed multi-resolution storage tiers (original, medium, thumbnail). Supports gallery history up to 10 avatars per user with exactly one active avatar. Hard-deleted on removal to physically purge S3/MinIO objects and free quota.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `user_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE CASCADE`) |
| `storage_key_original` | `VARCHAR(255)` | `NO` | — | S3 storage key for original resolution tier (max 2000px) |
| `storage_key_medium` | `VARCHAR(255)` | `NO` | — | S3 storage key for medium resolution tier (max 500px) |
| `storage_key_thumbnail` | `VARCHAR(255)` | `NO` | — | S3 storage key for thumbnail resolution tier (max 150px) |
| `original_filename` | `VARCHAR(255)` | `NO` | — | Sanitized original client filename |
| `content_type` | `VARCHAR(50)` | `NO` | — | Verified MIME type (`image/jpeg`, `image/png`, `image/webp`) |
| `file_size` | `BIGINT` | `NO` | — | Payload size in bytes of the original uploaded image |
| `is_active` | `BOOLEAN` | `NO` | `FALSE` | Active profile avatar flag (at most one true per user) |
| `uploaded_at` | `DATETIME(6)` | `NO` | — | Timestamp when avatar was uploaded and persisted |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Inherited BaseEntity column (unused; hard-delete policy) |

- **Relationships**:
  - `ManyToOne` with `User` (owning side: `UserAvatar`, fetch: `LAZY`, foreign key column: `user_id`).
- **Indexes & Unique Constraints**:
  - `idx_user_avatars_user_id` (`user_id`): Fast retrieval and quota checking per user.
  - `idx_user_avatars_user_active` (`user_id`, `is_active`): Fast lookup for the user's currently active avatar.
  - `idx_user_avatars_user_uploaded` (`user_id`, `uploaded_at`): Efficient sorting for gallery history listings.

---

## 3. Artisan & Client Domain

### 3.1 `artisans`
Extended profile for craftspersons, workshop masters, and heritage practitioners.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `users.id` (`ON DELETE CASCADE`) |
| `bio` | `TEXT` | `YES` | `NULL` | Biography, background, and artisanal philosophy |
| `region_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `regions.id` (`ON DELETE SET NULL`) |
| `city` | `VARCHAR(100)` | `YES` | `NULL` | City / Commune / Daïra |
| `address` | `VARCHAR(255)` | `YES` | `NULL` | Workshop / Studio physical street address |
| `website` | `VARCHAR(255)` | `YES` | `NULL` | External website or brand showcase URL |
| `sub_category_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `job_sub_categories.id` (`ON DELETE SET NULL`) |
| `is_teacher` | `BOOLEAN` | `NO` | `FALSE` | Offers masterclasses, workshops, and formations |
| `is_premium` | `BOOLEAN` | `NO` | `FALSE` | Active premium artisan plan badge |
| `is_verified` | `BOOLEAN` | `NO` | `FALSE` | Admin-certified master artisan badge |
| `rating` | `DECIMAL(3,2)` | `NO` | `0.00` | Aggregate rating score (range: `0.00` to `5.00`) |
| `reviews_count` | `INT` | `NO` | `0` | Total verified client reviews count |
| `views_count` | `INT` | `NO` | `0` | Total profile page visits count |
| `response_rate` | `INT` | `NO` | `0` | Message responsiveness percentage (`0` to `100`) |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `OneToOne` with `User` (owning side: `Artisan`, primary key joins on `users.id`).
  - `ManyToOne` with `Region` (fetch: `LAZY`).
  - `ManyToOne` with `JobSubCategory` (fetch: `LAZY`).
  - `ManyToMany` with `Material` via `artisan_materials` (owning side: `Artisan`, fetch: `LAZY`).
  - `ManyToMany` with `Technique` via `artisan_techniques` (owning side: `Artisan`, fetch: `LAZY`).
  - `ManyToMany` with `Epoque` via `artisan_epoques` (owning side: `Artisan`, fetch: `LAZY`).
  - `OneToMany` with `ArtisanGalleryImage` (mappedBy: `artisan`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToMany` with `ArtisanCertification` (mappedBy: `artisan`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToMany` with `ArtisanAchievement` (mappedBy: `artisan`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToMany` with `ArtisanSocialLink` (mappedBy: `artisan`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToMany` with `Formation` (mappedBy: `author`, fetch: `LAZY`, cascade: `ALL`).
  - `OneToMany` with `Review` (mappedBy: `artisan`, fetch: `LAZY`).
- **Indexes & Directory Search Optimizations**:
  - `idx_artisan_dir_search` (`deleted_at`, `is_verified`, `is_premium`, `rating` DESC, `sub_category_id`, `region_id`): Composite index optimizing directory browsing and multi-facet filtering.
  - `idx_artisan_region` (`region_id`): Filter artisans by geographic wilaya.
  - `idx_artisan_subcat` (`sub_category_id`): Filter artisans by craft subcategory.
  - `idx_artisan_teacher` (`is_teacher`, `deleted_at`): Fast lookup for formation instructors.
  - `idx_artisan_rating` (`rating` DESC): Sorting directory results by top-rated artisans.

---

### 3.2 `clients`
Extended profile for registered buyers, collectors, boutiques, and institutions.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `users.id` (`ON DELETE CASCADE`) |
| `client_type` | `VARCHAR(50)` | `NO` | `'INDIVIDUAL'` | Enum/String: `INDIVIDUAL`, `BOUTIQUE`, `ENTERPRISE`, `INSTITUTION` |
| `company_name` | `VARCHAR(255)` | `YES` | `NULL` | Registered company / boutique entity name |
| `region_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `regions.id` (`ON DELETE SET NULL`) |
| `city` | `VARCHAR(100)` | `YES` | `NULL` | City / Commune |
| `is_premium` | `BOOLEAN` | `NO` | `FALSE` | Active premium client plan (unmasks artisan direct contact info) |
| `is_verified` | `BOOLEAN` | `NO` | `FALSE` | Verified corporate / institutional buyer badge |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `OneToOne` with `User` (owning side: `Client`, primary key joins on `users.id`).
  - `ManyToOne` with `Region` (fetch: `LAZY`).
  - `OneToMany` with `Review` (mappedBy: `client`, fetch: `LAZY`).
- **Indexes**:
  - `idx_clients_region` (`region_id`): Indexed for regional client analytics.
  - `idx_clients_premium` (`is_premium`): Indexed for subscription entitlement verification.

---

## 4. Catalog & Heritage Taxonomy

### 4.1 `regions`
Hierarchical Algerian geographic regions (Wilayas and Communes).

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `parent_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `regions.id` (`ON DELETE RESTRICT`) |
| `name` | `VARCHAR(100)` | `NO` | — | Region/Wilaya/Commune official name |
| `slug` | `VARCHAR(120)` | `NO` | — | URL-friendly unique slug (e.g. `alger`, `tizi-ouzou`) |
| `code` | `VARCHAR(10)` | `YES` | `NULL` | Wilaya administrative code (e.g. `16`, `15`) |
| `display_order` | `INT` | `NO` | `0` | UI display ordering weight |
| `is_active` | `BOOLEAN` | `NO` | `TRUE` | Enable / disable region display |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

- **Relationships**:
  - `ManyToOne` self-referencing `parent` (fetch: `LAZY`).
  - `OneToMany` self-referencing `children` (mappedBy: `parent`, fetch: `LAZY`).
- **Indexes & Unique Constraints**:
  - `uk_regions_slug` (`slug`): Unique index for SEO-friendly routing (`/catalog/regions/tizi-ouzou`).
  - `idx_regions_parent` (`parent_id`, `display_order`): Fast retrieval of child communes for a given wilaya.

---

### 4.2 `job_categories` & `job_sub_categories`
Two-tier hierarchy of artisanal crafts (e.g. *Artisanat d'Art* -> *Céramique & Poterie*).

#### `job_categories`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `name` | `VARCHAR(100)` | `NO` | — | Category name (e.g. *Métiers du Bois*) |
| `slug` | `VARCHAR(120)` | `NO` | — | Unique URL slug (e.g. `metiers-du-bois`) |
| `description` | `TEXT` | `YES` | `NULL` | Category overview |
| `icon_url` | `VARCHAR(500)` | `YES` | `NULL` | Icon or badge asset URL |
| `display_order` | `INT` | `NO` | `0` | UI sort weight |
| `is_active` | `BOOLEAN` | `NO` | `TRUE` | Active flag |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

#### `job_sub_categories`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `category_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `job_categories.id` (`ON DELETE CASCADE`) |
| `name` | `VARCHAR(100)` | `NO` | — | Subcategory name (e.g. *Ébénisterie Traditionnelle*) |
| `slug` | `VARCHAR(120)` | `NO` | — | Unique URL slug |
| `description` | `TEXT` | `YES` | `NULL` | Detailed description |
| `display_order` | `INT` | `NO` | `0` | UI sort weight |
| `is_active` | `BOOLEAN` | `NO` | `TRUE` | Active flag |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

- **Relationships**:
  - `ManyToOne` from `JobSubCategory` to `JobCategory` (owning side: `JobSubCategory`, fetch: `LAZY`).
  - `OneToMany` from `JobCategory` to `JobSubCategory` (mappedBy: `category`, fetch: `LAZY`, cascade: `ALL`).
- **Indexes & Unique Constraints**:
  - `uk_job_categories_slug` (`slug`): Unique slug.
  - `uk_job_sub_categories_slug` (`slug`): Unique slug.
  - `idx_subcat_category` (`category_id`, `display_order`): Fast retrieval of subcategories within a category.

---

### 4.3 `material_families` & `materials`
Raw materials hierarchy (e.g. *Métaux Précieux* -> *Argent Massif 925*).

#### `material_families`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `name` | `VARCHAR(100)` | `NO` | — | Family name (e.g. *Terres & Argiles*) |
| `slug` | `VARCHAR(120)` | `NO` | — | Unique URL slug |
| `description` | `TEXT` | `YES` | `NULL` | Family description |
| `display_order` | `INT` | `NO` | `0` | UI sort order |
| `is_active` | `BOOLEAN` | `NO` | `TRUE` | Active status |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

#### `materials`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `family_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `material_families.id` (`ON DELETE CASCADE`) |
| `name` | `VARCHAR(100)` | `NO` | — | Material name (e.g. *Argile Rouge de Kabylie*) |
| `slug` | `VARCHAR(120)` | `NO` | — | Unique URL slug |
| `description` | `TEXT` | `YES` | `NULL` | Material characteristics |
| `display_order` | `INT` | `NO` | `0` | UI sort order |
| `is_active` | `BOOLEAN` | `NO` | `TRUE` | Active status |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

- **Relationships**:
  - `ManyToOne` from `Material` to `MaterialFamily` (fetch: `LAZY`).
  - `ManyToMany` between `Artisan` and `Material` via `artisan_materials`.
- **Indexes & Unique Constraints**:
  - `uk_material_families_slug` (`slug`), `uk_materials_slug` (`slug`): Unique constraints.
  - `idx_materials_family` (`family_id`, `display_order`): Retrieval by material family.

---

### 4.4 `epoques` & `techniques`
Heritage historical eras and traditional craftsmanship techniques.

#### `epoques`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `name` | `VARCHAR(100)` | `NO` | — | Epoch name (e.g. *Période Numide*, *Ottomane*, *Contemporaine*) |
| `slug` | `VARCHAR(120)` | `NO` | — | Unique URL slug |
| `period_era` | `VARCHAR(100)` | `YES` | `NULL` | Historical century/date span (e.g. *XVIe - XIXe siècle*) |
| `description` | `TEXT` | `YES` | `NULL` | Historical context |
| `display_order` | `INT` | `NO` | `0` | UI display sort |
| `is_active` | `BOOLEAN` | `NO` | `TRUE` | Active flag |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

#### `techniques`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `name` | `VARCHAR(100)` | `NO` | — | Technique name (e.g. *Filigrane*, *Ciselure au marteau*) |
| `slug` | `VARCHAR(120)` | `NO` | — | Unique URL slug |
| `description` | `TEXT` | `YES` | `NULL` | Methodological description |
| `display_order` | `INT` | `NO` | `0` | UI display sort |
| `is_active` | `BOOLEAN` | `NO` | `TRUE` | Active flag |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

- **Indexes & Unique Constraints**:
  - `uk_epoques_slug` (`slug`), `uk_techniques_slug` (`slug`): Unique slug constraints.

---

### 4.5 Artisan Taxonomy Junction Tables

#### `artisan_materials`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `material_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `materials.id` (`ON DELETE CASCADE`) |

- **Indexes**: PK (`artisan_id`, `material_id`), `idx_art_mat_material` (`material_id`, `artisan_id`).

#### `artisan_techniques`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `technique_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `techniques.id` (`ON DELETE CASCADE`) |

- **Indexes**: PK (`artisan_id`, `technique_id`), `idx_art_tech_technique` (`technique_id`, `artisan_id`).

#### `artisan_epoques`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `epoque_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `epoques.id` (`ON DELETE CASCADE`) |

- **Indexes**: PK (`artisan_id`, `epoque_id`), `idx_art_epoque_epoque` (`epoque_id`, `artisan_id`).

---

## 5. Artisan Portfolio & Verification Artifacts

### 5.1 `artisan_gallery_images`
Showcase imagery for an artisan's workshop and handcrafted masterpieces.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `image_url` | `VARCHAR(500)` | `NO` | — | Stored image URI / path |
| `title` | `VARCHAR(255)` | `YES` | `NULL` | Image artwork title |
| `caption` | `TEXT` | `YES` | `NULL` | Artwork details and background |
| `display_order` | `INT` | `NO` | `0` | Gallery presentation ordering |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**: `ManyToOne` with `Artisan` (fetch: `LAZY`).
- **Indexes**: `idx_gallery_artisan` (`artisan_id`, `deleted_at`, `display_order`).

---

### 5.2 `artisan_certifications`
Official artisan cards (Chambre d'Artisanat et des Métiers - CAM), diplomas, and accreditations.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `title` | `VARCHAR(255)` | `NO` | — | Certification title (e.g. *Carte d'Artisan Professionnel*) |
| `issuer` | `VARCHAR(255)` | `NO` | — | Issuing institution (e.g. *CAM Tizi Ouzou*) |
| `issued_at` | `DATE` | `YES` | `NULL` | Date of issuance |
| `expires_at` | `DATE` | `YES` | `NULL` | Expiration date (if applicable) |
| `document_url` | `VARCHAR(500)` | `YES` | `NULL` | PDF / Scan verification document |
| `is_verified` | `BOOLEAN` | `NO` | `FALSE` | Admin verification badge |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**: `ManyToOne` with `Artisan` (fetch: `LAZY`).
- **Indexes**: `idx_cert_artisan` (`artisan_id`, `deleted_at`).

---

### 5.3 `artisan_achievements`
Major projects, architectural restorations, exhibitions, and cultural awards.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `title` | `VARCHAR(255)` | `NO` | — | Project / Award title |
| `description` | `TEXT` | `YES` | `NULL` | Detailed description of the work |
| `image_url` | `VARCHAR(500)` | `YES` | `NULL` | Feature photograph URL |
| `completed_at` | `DATE` | `YES` | `NULL` | Completion or award date |
| `location` | `VARCHAR(255)` | `YES` | `NULL` | City, monument, or gallery location |
| `display_order` | `INT` | `NO` | `0` | Portfolio ordering weight |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**: `ManyToOne` with `Artisan` (fetch: `LAZY`).
- **Indexes**: `idx_achieve_artisan` (`artisan_id`, `deleted_at`, `display_order`).

---

### 5.4 `artisan_social_links`
External social presence channels (Instagram, Facebook, TikTok, YouTube, LinkedIn).

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `platform` | `VARCHAR(50)` | `NO` | — | Enum/String: `INSTAGRAM`, `FACEBOOK`, `TIKTOK`, `YOUTUBE`, `LINKEDIN` |
| `url` | `VARCHAR(500)` | `NO` | — | Verified channel URL |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**: `ManyToOne` with `Artisan` (fetch: `LAZY`).
- **Indexes**: `idx_social_artisan` (`artisan_id`, `platform`).

---

### 5.5 `artisan_validations`
Immutable administrative audit log of profile validations, rejections, suspensions, and reactivations.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `admin_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE RESTRICT`) |
| `action` | `VARCHAR(30)` | `NO` | — | Enum: `APPROVED`, `REJECTED`, `SUSPENDED`, `REACTIVATED` |
| `note` | `TEXT` | `YES` | `NULL` | Administrative decision justification / remarks |
| `performed_at` | `DATETIME(6)` | `NO` | — | Exact action execution timestamp |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |

- **Relationships**:
  - `ManyToOne` with `Artisan` (fetch: `LAZY`).
  - `ManyToOne` with `User` (`admin`, fetch: `LAZY`).
- **Indexes**: `idx_art_validations_artisan` (`artisan_id`, `performed_at` DESC).

---

## 6. Formations (Workshops & Masterclasses)

### 6.1 `formations`
Workshops, courses, and apprenticeship masterclasses authored by verified teacher artisans.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `author_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `title` | `VARCHAR(255)` | `NO` | — | Workshop title |
| `description` | `TEXT` | `NO` | — | Full curriculum, prerequisites, and learning objectives |
| `thumbnail_url` | `VARCHAR(500)` | `YES` | `NULL` | Course promotional cover image |
| `location` | `VARCHAR(255)` | `YES` | `NULL` | Physical studio address or virtual platform link |
| `is_online` | `BOOLEAN` | `NO` | `FALSE` | Course delivery format (in-person vs virtual) |
| `scheduled_at` | `DATETIME(6)` | `YES` | `NULL` | Scheduled session start date and time |
| `duration_hours` | `INT` | `NO` | `0` | Total course duration in hours |
| `max_participants`| `INT` | `NO` | `0` | Maximum student capacity |
| `price` | `INT` | `NO` | `0` | Enrollment price in DZD (`0` for free community workshops) |
| `currency` | `VARCHAR(10)` | `NO` | `'DZD'` | ISO currency code (`DZD`) |
| `status` | `VARCHAR(30)` | `NO` | `'DRAFT'` | Enum: `DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`, `PUBLISHED`, `CANCELLED`, `COMPLETED` |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `ManyToOne` with `Artisan` (owning side: `Formation`, fetch: `LAZY`).
  - `OneToMany` with `FormationFile` (mappedBy: `formation`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToMany` with `FormationEnrollment` (mappedBy: `formation`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToMany` with `FormationReview` (mappedBy: `formation`, fetch: `LAZY`, cascade: `ALL`, orphanRemoval: true).
  - `OneToMany` with `FeedPost` (mappedBy: `formation`, fetch: `LAZY`).
- **Indexes**:
  - `idx_formations_browse` (`status`, `scheduled_at`, `deleted_at`): Public marketplace query for published upcoming formations.
  - `idx_formations_author` (`author_id`, `status`, `deleted_at`): Artisan workshop management dashboard.

---

### 6.2 `formation_enrollments`
Peer artisan registrations and seat reservations for workshops and masterclasses.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID from `BaseEntity`) |
| `formation_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `formations.id` (`ON DELETE CASCADE`) |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `status` | `VARCHAR(30)` | `NO` | `'CONFIRMED'` | Enum: `CONFIRMED`, `ATTENDED`, `CANCELLED` |
| `enrolled_at` | `DATETIME(6)` | `NO` | — | Enrollment reservation timestamp |
| `cancelled_at` | `DATETIME(6)` | `YES` | `NULL` | Cancellation timestamp (when cancelled prior to cutoff) |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `ManyToOne` with `Formation` (fetch: `LAZY`).
  - `ManyToOne` with `Artisan` (fetch: `LAZY`).
- **Indexes & Unique Constraints**:
  - `uk_enrollment_artisan` (`formation_id`, `artisan_id`): Prevents duplicate enrollments by the same peer artisan for the same workshop.
  - `idx_formation_enrollments_lookup` (`formation_id`, `artisan_id`, `status`): Composite lookup for active reservation status.
  - `idx_formation_enrollments_artisan` (`artisan_id`, `status`): Fast lookup for an artisan's workshop enrollment history.

---

### 6.3 `formation_reviews`
Administrative curriculum reviews and compliance approvals.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID from `BaseEntity`) |
| `formation_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `formations.id` (`ON DELETE CASCADE`) |
| `admin_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE RESTRICT`) |
| `decision` | `VARCHAR(30)` | `NO` | — | Enum: `APPROVED`, `REJECTED` |
| `comment` | `TEXT` | `YES` | `NULL` | Reviewer feedback or rejection criteria |
| `reviewed_at` | `DATETIME(6)` | `NO` | — | Decision timestamp |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `ManyToOne` with `Formation` (fetch: `LAZY`).
  - `ManyToOne` with `User` (`admin`, fetch: `LAZY`).
- **Indexes**: `idx_formation_reviews_formation` (`formation_id`, `reviewed_at` DESC).

---

### 6.4 `formation_files`
Course attachments, syllabi, and supplemental training files accessible to the instructor and confirmed enrolled peer artisans.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID from `BaseEntity`) |
| `formation_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `formations.id` (`ON DELETE CASCADE`) |
| `storage_key` | `VARCHAR(255)` | `NO` | — | Storage object key / physical file identifier |
| `original_filename`| `VARCHAR(255)` | `NO` | — | Original file name as uploaded by author |
| `content_type` | `VARCHAR(50)` | `NO` | — | MIME type (`application/pdf`, `image/jpeg`, `image/png`) |
| `file_size` | `BIGINT` | `NO` | — | File size in bytes |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `ManyToOne` with `Formation` (fetch: `LAZY`).
- **Indexes**:
  - `idx_formation_files_formation` (`formation_id`, `deleted_at`): Fast lookup for non-deleted files belonging to a formation.

---

## 7. Social Feed, Messaging & Community

### 7.1 `feed_posts`
Community updates, course announcements, workshop recaps, and news.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `author_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE CASCADE`) |
| `type` | `VARCHAR(30)` | `NO` | — | Enum: `FORMATION`, `ACTUALITE`, `ANNONCE` |
| `title` | `VARCHAR(255)` | `NO` | — | Post title |
| `content` | `TEXT` | `NO` | — | Post body text |
| `image_urls` | `JSON` | `YES` | `NULL` | JSON array of uploaded media URLs |
| `formation_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `formations.id` (`ON DELETE SET NULL`, populated when `type = 'FORMATION'`) |
| `is_published` | `BOOLEAN` | `NO` | `TRUE` | Visibility flag |
| `published_at` | `DATETIME(6)` | `NO` | — | Publication timestamp |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `ManyToOne` with `User` (`author`, fetch: `LAZY`).
  - `ManyToOne` with `Formation` (fetch: `LAZY`).
- **Indexes**:
  - `idx_feed_published` (`deleted_at`, `is_published`, `published_at` DESC): Feed timeline pagination.
  - `idx_feed_author` (`author_id`, `published_at` DESC): User profile feed tab.

---

### 7.2 `conversations` & `conversation_participants`
Realtime conversation containers and participant tracking supporting unread badges.

#### `conversations`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `last_message_at`| `DATETIME(6)` | `YES` | `NULL` | Timestamp of newest message (for inbox ordering) |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

#### `conversation_participants`
| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `conversation_id`| `VARCHAR(36)` | `NO` | — | **PK, FK** -> `conversations.id` (`ON DELETE CASCADE`) |
| `user_id` | `VARCHAR(36)` | `NO` | — | **PK, FK** -> `users.id` (`ON DELETE CASCADE`) |
| `joined_at` | `DATETIME(6)` | `NO` | — | Timestamp user joined the conversation |
| `last_read_at` | `DATETIME(6)` | `NO` | — | Timestamp of last message read by this user |

- **Relationships**:
  - `OneToMany` from `Conversation` to `ConversationParticipant` (mappedBy: `conversation`, fetch: `LAZY`, cascade: `ALL`).
  - `OneToMany` from `Conversation` to `Message` (mappedBy: `conversation`, fetch: `LAZY`, cascade: `ALL`).
- **Indexes & Unread Count Performance**:
  - Primary Key on (`conversation_id`, `user_id`).
  - `idx_conv_part_user` (`user_id`, `last_read_at`): O(1) retrieval of a user's active conversation list with unread detection.
  - `idx_conv_last_msg` (`deleted_at`, `last_message_at` DESC): Sorting user inbox by recent activity.

---

### 7.3 `messages`
Individual direct chat messages with media attachments and delivery states.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `conversation_id`| `VARCHAR(36)` | `NO` | — | **FK** -> `conversations.id` (`ON DELETE CASCADE`) |
| `sender_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE RESTRICT`) |
| `content` | `TEXT` | `NO` | — | Text content |
| `attachment_urls`| `JSON` | `YES` | `NULL` | JSON array of uploaded attachments (images, PDFs) |
| `status` | `VARCHAR(30)` | `NO` | `'SENT'` | Enum: `SENT`, `DELIVERED`, `READ` |
| `sent_at` | `DATETIME(6)` | `NO` | — | Message dispatch timestamp |
| `read_at` | `DATETIME(6)` | `YES` | `NULL` | First read timestamp |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `ManyToOne` with `Conversation` (fetch: `LAZY`).
  - `ManyToOne` with `User` (`sender`, fetch: `LAZY`).
- **Indexes**:
  - `idx_msg_conv_time` (`conversation_id`, `deleted_at`, `sent_at` ASC): Fast paginated retrieval of chat transcript history.
  - `idx_msg_unread_count` (`conversation_id`, `sender_id`, `sent_at`): Evaluates unread messages (`sent_at > last_read_at AND sender_id != current_user`).

---

### 7.4 `reviews`
Client testimonials and 1-5 star ratings for verified artisan collaborations.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `client_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `clients.id` (`ON DELETE CASCADE`) |
| `rating` | `INT` | `NO` | — | Rating integer from `1` to `5` (`CHECK (rating >= 1 AND rating <= 5)`) |
| `comment` | `TEXT` | `YES` | `NULL` | Testimonial text feedback |
| `verified_interaction_type` | `VARCHAR(50)` | `NO` | — | Enum/String: `CONVERSATION`, `FORMATION_ENROLLMENT` |
| `verified_interaction_id` | `VARCHAR(36)` | `NO` | — | ID of the conversation or enrollment proving prior interaction |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

- **Relationships**:
  - `ManyToOne` with `Artisan` (fetch: `LAZY`).
  - `ManyToOne` with `Client` (fetch: `LAZY`).
- **Indexes & Unique Constraints**:
  - `uk_reviews_artisan_client` (`artisan_id`, `client_id`, `deleted_at`): Ensures one active review per client per artisan (updates permitted).
  - `idx_reviews_artisan` (`artisan_id`, `deleted_at`, `created_at` DESC): Profile review list pagination.

---

### 7.5 `reports`
Moderation abuse tickets filed against users, messages, feed posts, formations, or reviews.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `reporter_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE RESTRICT`) |
| `target_type` | `VARCHAR(30)` | `NO` | — | Enum/String: `USER`, `POST`, `MESSAGE`, `FORMATION`, `REVIEW` |
| `target_id` | `VARCHAR(36)` | `NO` | — | UUID of the reported target entity |
| `reason` | `VARCHAR(100)` | `NO` | — | Reason code (e.g. `SPAM`, `HARASSMENT`, `FRAUD`, `INAPPROPRIATE`) |
| `description` | `TEXT` | `YES` | `NULL` | Reporter explanation details |
| `status` | `VARCHAR(30)` | `NO` | `'PENDING'` | Enum: `PENDING`, `REVIEWED`, `RESOLVED`, `DISMISSED` |
| `resolved_by` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `users.id` (`admin`, `ON DELETE SET NULL`) |
| `resolution_notes` | `TEXT` | `YES` | `NULL` | Admin action summary (e.g. *User suspended for 7 days*) |
| `resolved_at` | `DATETIME(6)` | `YES` | `NULL` | Resolution completion timestamp |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |

- **Relationships**:
  - `ManyToOne` with `User` (`reporter`, fetch: `LAZY`).
  - `ManyToOne` with `User` (`resolvedByAdmin`, fetch: `LAZY`).
- **Indexes**:
  - `idx_reports_queue` (`status`, `created_at` ASC): Admin triage inbox queue.
  - `idx_reports_target` (`target_type`, `target_id`): Aggregating report count on a specific entity.

---

## 8. Subscriptions & Payments (Chargily Pay V2)

### 8.1 `subscription_pricing`
Platform subscription pricing tiers and entitlement specifications.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `target_role` | `VARCHAR(30)` | `NO` | — | Target user role (`ROLE_CLIENT` or `ROLE_ARTISAN`) |
| `plan_type` | `VARCHAR(30)` | `NO` | — | Enum: `FREEMIUM`, `PREMIUM_MONTHLY`, `PREMIUM_YEARLY` |
| `price` | `INT` | `NO` | `0` | Fee in DZD (`0` for Freemium, e.g. `1500` for monthly) |
| `currency` | `VARCHAR(10)` | `NO` | `'DZD'` | Currency code |
| `features` | `JSON` | `YES` | `NULL` | JSON array of feature entitlements for UI rendering |
| `is_active` | `BOOLEAN` | `NO` | `TRUE` | Currently offered plan flag |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |

- **Indexes & Unique Constraints**:
  - `uk_pricing_role_plan` (`target_role`, `plan_type`, `is_active`): Ensures unique active pricing tier per role/plan.

---

### 8.2 `client_subscriptions`
Active and historical premium access plans for Clients.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `client_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `clients.id` (`ON DELETE CASCADE`) |
| `pricing_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `subscription_pricing.id` (`ON DELETE RESTRICT`) |
| `plan` | `VARCHAR(30)` | `NO` | — | Enum: `FREEMIUM`, `PREMIUM_MONTHLY`, `PREMIUM_YEARLY` |
| `status` | `VARCHAR(30)` | `NO` | `'ACTIVE'` | Enum: `ACTIVE`, `EXPIRED`, `CANCELLED`, `PENDING_PAYMENT` |
| `started_at` | `DATETIME(6)` | `NO` | — | Plan effective start timestamp |
| `expires_at` | `DATETIME(6)` | `YES` | `NULL` | Expiration timestamp (`NULL` for permanent freemium) |
| `auto_renew` | `BOOLEAN` | `NO` | `FALSE` | Auto-renewal preference flag |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |

- **Relationships**:
  - `ManyToOne` with `Client` (fetch: `LAZY`).
  - `ManyToOne` with `SubscriptionPricing` (fetch: `LAZY`).
  - `OneToMany` with `Payment` (mappedBy: `clientSubscription`, fetch: `LAZY`).
- **Indexes & Unique Constraints**:
  - `idx_client_sub_active` (`client_id`, `status`, `expires_at`): Validates client directory contact unmasking permissions in O(1).

---

### 8.3 `artisan_subscriptions`
Active and historical premium visibility & promotion plans for Artisans.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `artisan_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `artisans.id` (`ON DELETE CASCADE`) |
| `pricing_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `subscription_pricing.id` (`ON DELETE RESTRICT`) |
| `plan` | `VARCHAR(30)` | `NO` | — | Enum: `FREEMIUM`, `PREMIUM_MONTHLY`, `PREMIUM_YEARLY` |
| `status` | `VARCHAR(30)` | `NO` | `'ACTIVE'` | Enum: `ACTIVE`, `EXPIRED`, `CANCELLED`, `PENDING_PAYMENT` |
| `started_at` | `DATETIME(6)` | `NO` | — | Plan effective start timestamp |
| `expires_at` | `DATETIME(6)` | `YES` | `NULL` | Expiration timestamp (`NULL` for freemium) |
| `auto_renew` | `BOOLEAN` | `NO` | `FALSE` | Auto-renewal preference flag |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |

- **Relationships**:
  - `ManyToOne` with `Artisan` (fetch: `LAZY`).
  - `ManyToOne` with `SubscriptionPricing` (fetch: `LAZY`).
  - `OneToMany` with `Payment` (mappedBy: `artisanSubscription`, fetch: `LAZY`).
- **Indexes & Unique Constraints**:
  - `idx_artisan_sub_active` (`artisan_id`, `status`, `expires_at`): Validates artisan featured listing status.

---

### 8.4 `payments`
Financial transaction ledger for Chargily Pay V2 checkouts.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `payment_type` | `VARCHAR(30)` | `NO` | — | Enum: `CLIENT_SUBSCRIPTION`, `ARTISAN_SUBSCRIPTION`, `FORMATION_ENROLLMENT` |
| `client_subscription_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `client_subscriptions.id` (`ON DELETE SET NULL`) |
| `artisan_subscription_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `artisan_subscriptions.id` (`ON DELETE SET NULL`) |
| `formation_enrollment_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `formation_enrollments.id` (`ON DELETE SET NULL`) |
| `payer_user_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE RESTRICT`) |
| `provider` | `VARCHAR(30)` | `NO` | `'CHARGILY'` | Enum: `CHARGILY`, `EDAHABIA`, `CIB`, `CARD` |
| `provider_checkout_id` | `VARCHAR(255)` | `YES` | `NULL` | Chargily checkout session identifier |
| `provider_checkout_url`| `VARCHAR(500)` | `YES` | `NULL` | Chargily payment redirect URL |
| `amount` | `INT` | `NO` | — | Transaction amount in DZD |
| `currency` | `VARCHAR(10)` | `NO` | `'DZD'` | ISO currency code (`DZD`) |
| `status` | `VARCHAR(30)` | `NO` | `'PENDING'` | Enum: `PENDING`, `SUCCEEDED`, `FAILED`, `REFUNDED`, `CANCELLED` |
| `paid_at` | `DATETIME(6)` | `YES` | `NULL` | Payment confirmation timestamp |
| `failure_reason` | `VARCHAR(255)` | `YES` | `NULL` | Failure reason message |
| `created_at` | `DATETIME(6)` | `NO` | — | Audit creation timestamp |
| `updated_at` | `DATETIME(6)` | `NO` | — | Audit update timestamp |

- **Relationships**:
  - `ManyToOne` with `ClientSubscription` (fetch: `LAZY`).
  - `ManyToOne` with `ArtisanSubscription` (fetch: `LAZY`).
  - `ManyToOne` with `FormationEnrollment` (fetch: `LAZY`).
  - `ManyToOne` with `User` (`payer`, fetch: `LAZY`).
  - `OneToMany` with `PaymentWebhookLog` (mappedBy: `payment`, fetch: `LAZY`).
- **Indexes & Integrity Constraints**:
  - `uk_payments_checkout_id` (`provider_checkout_id`): Unique index on Chargily checkout identifier.
  - `idx_payments_payer` (`payer_user_id`, `created_at` DESC): User billing and receipt history.
  - `idx_payments_status` (`status`, `created_at` DESC): Financial auditing and accounting dashboard.
  - **Polymorphic Integrity Rule**: Exactly one foreign key among (`client_subscription_id`, `artisan_subscription_id`, `formation_enrollment_id`) must match the `payment_type` discriminator.

---

### 8.5 `payment_webhook_logs`
Immutable Chargily webhook ingestion log guaranteeing idempotency and auditability.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `event_id` | `VARCHAR(255)` | `NO` | — | Unique Chargily webhook event ID (e.g. `evt_...`) |
| `payment_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `payments.id` (`ON DELETE SET NULL`) |
| `event_type` | `VARCHAR(100)` | `NO` | — | Webhook event type (e.g. `checkout.paid`, `checkout.failed`) |
| `payload` | `JSON` | `NO` | — | Raw webhook JSON payload for replayability & debugging |
| `signature` | `VARCHAR(255)` | `YES` | `NULL` | `Signature` header for audit verification |
| `is_processed` | `BOOLEAN` | `NO` | `FALSE` | Processing completion flag |
| `processed_at` | `DATETIME(6)` | `YES` | `NULL` | Processing timestamp |
| `error_message` | `TEXT` | `YES` | `NULL` | Error details if processing failed |
| `created_at` | `DATETIME(6)` | `NO` | — | Ingestion timestamp |

- **Relationships**: `ManyToOne` with `Payment` (fetch: `LAZY`).
- **Indexes & Unique Constraints**:
  - `uk_webhook_event_id` (`event_id`): **Unique constraint enforcing strict idempotency**. Any duplicate webhook event from Chargily triggers a duplicate key violation and is safely acknowledged without double processing.
  - `idx_webhook_payment` (`payment_id`): Lookup logs associated with a payment transaction.
  - `idx_webhook_unprocessed` (`is_processed`, `created_at` ASC): Retry queue for failed webhook deliveries.

---

## 9. Audit Logging & System Notifications

### 9.1 `audit_logs`
Append-only system-wide security, administrative, and domain event ledger.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `actor_id` | `VARCHAR(36)` | `YES` | `NULL` | **FK** -> `users.id` (`ON DELETE SET NULL`, null for system crons) |
| `action` | `VARCHAR(50)` | `NO` | — | Enum: `AuditLogAction` |
| `target_entity_type`| `VARCHAR(50)` | `NO` | — | Target entity name (e.g. `USER`, `ARTISAN`, `FORMATION`) |
| `target_entity_id` | `VARCHAR(36)` | `NO` | — | Target entity UUID |
| `details` | `JSON` | `YES` | `NULL` | Structured context payload (diffs, parameters, metadata) |
| `ip_address` | `VARCHAR(45)` | `YES` | `NULL` | Originating client IP address |
| `performed_at` | `DATETIME(6)` | `NO` | — | Event timestamp |
| `created_at` | `DATETIME(6)` | `NO` | — | Ingestion timestamp |

- **Relationships**: `ManyToOne` with `User` (`actor`, fetch: `LAZY`).
- **Indexes**:
  - `idx_audit_actor` (`actor_id`, `performed_at` DESC): Search audit logs by acting user.
  - `idx_audit_target` (`target_entity_type`, `target_entity_id`, `performed_at` DESC): Trace complete audit history of any specific record across the platform.
  - `idx_audit_action` (`action`, `performed_at` DESC): Filter audit logs by action type.

---

### 9.2 `notifications`
Realtime in-app notification records dispatched via WebSocket STOMP and persistent storage.

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID) |
| `recipient_id` | `VARCHAR(36)` | `NO` | — | **FK** -> `users.id` (`ON DELETE CASCADE`) |
| `type` | `VARCHAR(50)` | `NO` | — | Enum: `NotificationType` (13 domain event types) |
| `title` | `VARCHAR(255)` | `NO` | — | In-app notification title |
| `message` | `TEXT` | `NO` | — | Formatted notification message |
| `target_entity_type`| `VARCHAR(50)` | `YES` | `NULL` | Related entity type for deep-linking in UI |
| `target_entity_id` | `VARCHAR(36)` | `YES` | `NULL` | Related entity UUID |
| `is_read` | `BOOLEAN` | `NO` | `FALSE` | Read status |
| `read_at` | `DATETIME(6)` | `YES` | `NULL` | Timestamp of notification opening |
| `created_at` | `DATETIME(6)` | `NO` | — | Dispatch timestamp |

- **Relationships**: `ManyToOne` with `User` (`recipient`, fetch: `LAZY`).
- **Indexes**:
  - `idx_notif_recipient` (`recipient_id`, `is_read`, `created_at` DESC): Fast in-app notification drawer retrieval and badge count.

---

## 10. Flagged Decisions

The following architectural and design decisions were made to guarantee performance, maintainability, and clean decoupling:

1. **Shared Primary Key Pattern for `Artisan` and `Client`**:
   - `artisans.id` and `clients.id` share the exact primary key with `users.id` (1:1 with `@MapsId` in JPA).
   - *Rationale*: Guarantees identity consistency, eliminates redundant foreign key joins when navigating from user to profile, and simplifies access control in `@PreAuthorize` SpEL checks (e.g. `#id == authentication.principal.id`).

2. **Directory Search: Hybrid MySQL + Hibernate Search / Elasticsearch Strategy**:
   - **Elasticsearch (Hibernate Search)**: Handles fuzzy text queries, phonetic matching, keyword search across bio, craft title, and descriptions (`q=...`).
   - **MySQL Composite Index (`idx_artisan_dir_search`)**: Handles high-performance multi-facet relational filtering (`region_id`, `sub_category_id`, `is_verified`, `is_premium`, `rating`) and join table queries (`artisan_materials`, `artisan_techniques`, `artisan_epoques`).
   - *Rationale*: Offloads heavy search while keeping transactional state and pagination fully consistent.

3. **Polymorphic Payment Associations with Exclusive Foreign Keys**:
   - In `payments`, rather than using an untyped generic string ID for payments, three explicit nullable foreign keys are defined: `client_subscription_id`, `artisan_subscription_id`, and `formation_enrollment_id`, guided by a `payment_type` discriminator.
   - *Rationale*: Preserves full relational integrity, foreign key cascading safety, and query performance in SQL while maintaining polymorphic payment handling for Chargily checkouts.

4. **Review Verification Enforcement Strategy**:
   - A client is only permitted to submit a review for an artisan if they have a verified prior interaction.
   - `reviews` stores `verified_interaction_type` (`CONVERSATION` or `FORMATION_ENROLLMENT`) and `verified_interaction_id`.
   - *Enforcement Mechanism*:
     - **Database Layer**: Unique constraint on `(artisan_id, client_id, deleted_at)` restricts each client to one review per artisan.
     - **Service Layer (`ReviewService`)**: Before accepting a review creation request, the service runs a fast indexed existence query:
       ```sql
       -- Option A: Verified Conversation with at least 1 message exchange
       SELECT EXISTS(
         SELECT 1 FROM conversations c
         JOIN conversation_participants cp1 ON c.id = cp1.conversation_id AND cp1.user_id = :clientId
         JOIN conversation_participants cp2 ON c.id = cp2.conversation_id AND cp2.user_id = :artisanId
         WHERE EXISTS (SELECT 1 FROM messages m WHERE m.conversation_id = c.id AND m.sender_id = :clientId)
       );
       -- Option B: Completed or Confirmed Formation Enrollment
       SELECT EXISTS(
         SELECT 1 FROM formation_enrollments fe
         JOIN formations f ON fe.formation_id = f.id
         WHERE fe.artisan_id = :reviewerArtisanId AND f.author_id = :artisanId AND fe.status IN ('CONFIRMED', 'ATTENDED')
       );
       ```

5. **Chargily Webhook Idempotency via Unique Event ID**:
   - `payment_webhook_logs` defines `event_id` as `VARCHAR(255) UNIQUE NOT NULL`.
   - *Rationale*: Chargily Pay (and payment gateways in general) employ at-least-once delivery with exponential retries. The unique constraint guarantees that concurrent or repeated webhook deliveries are immediately deduplicated at the database constraint level.

6. **Region Self-Referencing Hierarchy**:
   - Modeled via `parent_id` in `regions` table rather than separate `wilayas` and `communes` tables.
   - *Rationale*: Clean, unified taxonomy structure that supports flexible administrative divisions (Wilayas, Daïras, Communes) in a single queryable table with recursive parent-child tree mapping.

7. **OAuth Identities & Auto-Link by Verified Email Strategy**:
   - Social logins are normalized into a dedicated `oauth_identities` table rather than hardcoding provider-specific columns on `users`.
   - *Hard-Delete Classification*: Unlinking an OAuth provider deletes the corresponding `oauth_identities` record directly without soft-delete overhead.
   - *Auto-Link Behavior*: When an OAuth login callback arrives:
     1. Look up `oauth_identities` by `(provider, provider_user_id)`. If present, authenticate the associated `user_id`.
     2. If not found, look up `users` by `email` provided by the OAuth provider. If an existing user matches and has `email_verified = TRUE` (or if the OAuth provider guarantees email verification, such as Google), link a new `OAuthIdentity` record to the existing user and authenticate.
     3. If no matching user exists, register a new `User` (with `password = NULL`, `email_verified = TRUE`, and the designated role) and persist the new `OAuthIdentity`.
   - *Constraint Compatibility*: The unique constraint `uk_users_email` on `users.email` does not conflict with this flow. An existing user retains their unique `email` in `users`, and multiple third-party logins (Google, Facebook, Apple) simply attach as individual rows in `oauth_identities` referencing that same `user_id`.

