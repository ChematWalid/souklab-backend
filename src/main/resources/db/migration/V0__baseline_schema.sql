-- Baseline schema generated from the Hibernate MariaDB mapping.

-- This migration creates the pre-feature schema required by V1-V4.

SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE IF NOT EXISTS `artisan_certifications` (
  `expires_at` date DEFAULT NULL,
  `is_verified` bit(1) NOT NULL,
  `issued_at` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `artisan_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `document_url` varchar(500) DEFAULT NULL,
  `issuer` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cert_artisan` (`artisan_id`,`deleted_at`),
  CONSTRAINT `FKe5ht3ptn8dinajox35kb9xqau` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `artisan_epoques` (
  `artisan_id` varchar(36) NOT NULL,
  `epoque_id` varchar(36) NOT NULL,
  PRIMARY KEY (`artisan_id`,`epoque_id`),
  KEY `FK9fh6hnjed7wix4yeh7vw312u6` (`epoque_id`),
  CONSTRAINT `FK9fh6hnjed7wix4yeh7vw312u6` FOREIGN KEY (`epoque_id`) REFERENCES `epoques` (`id`),
  CONSTRAINT `FKh7g6ci0lyox3jtnbf90a177lw` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `artisan_formateur_requests` (
  `can_reapply` bit(1) NOT NULL,
  `cooldown_until` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `decided_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `artisan_id` varchar(36) NOT NULL,
  `decided_by` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `admin_note` text DEFAULT NULL,
  `motivation` text DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_formateur_req_artisan` (`artisan_id`),
  KEY `idx_formateur_req_status` (`status`),
  KEY `FK4m8kl53d4sl19bmeepqjg681n` (`decided_by`),
  CONSTRAINT `FK3h2xd6ahhnq9drj8lai7oemhi` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`),
  CONSTRAINT `FK4m8kl53d4sl19bmeepqjg681n` FOREIGN KEY (`decided_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `artisan_gallery_images` (
  `display_order` int(11) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `artisan_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `caption` text DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_gallery_artisan` (`artisan_id`,`deleted_at`,`display_order`),
  CONSTRAINT `FK1xsm1b104vvknagqrpccsqgrc` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `artisan_materials` (
  `artisan_id` varchar(36) NOT NULL,
  `material_id` varchar(36) NOT NULL,
  PRIMARY KEY (`artisan_id`,`material_id`),
  KEY `FKe58rxkv7h8jrdcq7cwb4rg3r8` (`material_id`),
  CONSTRAINT `FK6ofn5w21m75d0rjpmqfo3mgmy` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`),
  CONSTRAINT `FKe58rxkv7h8jrdcq7cwb4rg3r8` FOREIGN KEY (`material_id`) REFERENCES `materials` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `artisan_profile_views` (
  `viewed_at` datetime(6) NOT NULL,
  `artisan_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `viewer_id` varchar(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_artisan_viewer` (`viewer_id`,`artisan_id`),
  KEY `FKcbgnuhfhmraam7akw1x25urbu` (`artisan_id`),
  CONSTRAINT `FKcbgnuhfhmraam7akw1x25urbu` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`),
  CONSTRAINT `FKhndb2a0dnltxt0cvbe3ll8t9f` FOREIGN KEY (`viewer_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `artisan_techniques` (
  `artisan_id` varchar(36) NOT NULL,
  `technique_id` varchar(36) NOT NULL,
  PRIMARY KEY (`artisan_id`,`technique_id`),
  KEY `FKl49tggyw6otwjopsdgod6p231` (`technique_id`),
  CONSTRAINT `FKfwncehcmnb1ijk43bnpy87aem` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`),
  CONSTRAINT `FKl49tggyw6otwjopsdgod6p231` FOREIGN KEY (`technique_id`) REFERENCES `techniques` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `artisans` (
  `is_premium` bit(1) NOT NULL,
  `is_teacher` bit(1) NOT NULL,
  `is_verified` bit(1) NOT NULL,
  `rating` double NOT NULL,
  `response_rate` int(11) NOT NULL,
  `reviews_count` int(11) NOT NULL,
  `views_count` int(11) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `region_id` varchar(36) DEFAULT NULL,
  `sub_category_id` varchar(36) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_artisan_dir_search` (`deleted_at`,`is_verified`,`is_premium`,`rating` DESC,`sub_category_id`,`region_id`),
  KEY `idx_artisan_region` (`region_id`),
  KEY `idx_artisan_subcat` (`sub_category_id`),
  KEY `idx_artisan_teacher` (`is_teacher`,`deleted_at`),
  KEY `idx_artisan_rating` (`rating` DESC),
  CONSTRAINT `FKeienfjy82qq1jsjjom0v8yw5e` FOREIGN KEY (`sub_category_id`) REFERENCES `job_sub_categories` (`id`),
  CONSTRAINT `FKnuu6ahtbdw4hvtowe0ba4mx4y` FOREIGN KEY (`id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpw2irt2r2djh15obe4bv2i9d8` FOREIGN KEY (`region_id`) REFERENCES `regions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `audit_logs` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `details` varchar(2000) DEFAULT NULL,
  `action` enum('APPROVE_ARTISAN','APPROVE_FORMATION','APPROVE_USER','ASSIGN_PERMISSION_BULK','ASSIGN_ROLE','BAN_USER','DISMISS_REPORT','EMAIL_VERIFIED','PASSWORD_CHANGED','PASSWORD_RESET_COMPLETED','PERMISSION_GRANTED','PERMISSION_REVOKED','REJECT_ARTISAN','REJECT_FORMATION','RESOLVE_REPORT','TIMEOUT_USER','UNBAN_USER') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjs4iimve3y0xssbtve5ysyef0` (`user_id`),
  CONSTRAINT `FKjs4iimve3y0xssbtve5ysyef0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `clients` (
  `is_premium` bit(1) NOT NULL,
  `is_verified` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `region_id` varchar(36) DEFAULT NULL,
  `client_type` varchar(50) NOT NULL,
  `city` varchar(100) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `company_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK1hgwdp9vl25xl9i7s354sifey` FOREIGN KEY (`id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `epoques` (
  `display_order` int(11) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `period_era` varchar(100) DEFAULT NULL,
  `slug` varchar(120) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_epoques_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `formation_enrollments` (
  `cancelled_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `enrolled_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `artisan_id` varchar(36) NOT NULL,
  `formation_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `status` enum('ATTENDED','CANCELLED','CONFIRMED') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_formation_enrollment_formation_artisan` (`formation_id`,`artisan_id`),
  KEY `idx_formation_enrollments_lookup` (`formation_id`,`artisan_id`,`status`),
  KEY `idx_formation_enrollments_artisan` (`artisan_id`,`status`),
  CONSTRAINT `FK6yxxnds0wl516wkxq94arb85d` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`),
  CONSTRAINT `FKq04n00c0px9tyafo59eqre74t` FOREIGN KEY (`formation_id`) REFERENCES `formations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `formation_files` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `file_size` bigint(20) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `formation_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `content_type` varchar(50) NOT NULL,
  `original_filename` varchar(255) NOT NULL,
  `storage_key` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_formation_files_formation` (`formation_id`,`deleted_at`),
  CONSTRAINT `FKrf5hngi8r029qk85m4gfpf3mf` FOREIGN KEY (`formation_id`) REFERENCES `formations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `formation_reviews` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `reviewed_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `admin_id` varchar(36) NOT NULL,
  `formation_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `comment` text DEFAULT NULL,
  `decision` enum('APPROVED','REJECTED') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_formation_reviews_formation` (`formation_id`,`reviewed_at`),
  KEY `FKeagj5sy2b5fcq8mb1jrvtpshp` (`admin_id`),
  CONSTRAINT `FKamd1yo5jr1ss2wc9io9m4napx` FOREIGN KEY (`formation_id`) REFERENCES `formations` (`id`),
  CONSTRAINT `FKeagj5sy2b5fcq8mb1jrvtpshp` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `formations` (
  `duration_hours` int(11) NOT NULL,
  `is_online` bit(1) NOT NULL,
  `max_participants` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `scheduled_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `currency` varchar(10) NOT NULL,
  `author_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
  `description` text NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `status` enum('APPROVED','CANCELLED','COMPLETED','DRAFT','PENDING_REVIEW','PUBLISHED','REJECTED') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_formations_browse` (`status`,`scheduled_at`,`deleted_at`),
  KEY `idx_formations_author` (`author_id`,`status`,`deleted_at`),
  CONSTRAINT `FKqwksyxpq01yxppjn53wenloth` FOREIGN KEY (`author_id`) REFERENCES `artisans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `job_categories` (
  `display_order` int(11) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `icon_url` varchar(500) DEFAULT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_categories_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `job_sub_categories` (
  `display_order` int(11) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `category_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_sub_categories_slug` (`slug`),
  KEY `idx_subcat_category` (`category_id`,`display_order`),
  CONSTRAINT `FKd6n4jky83mx4g10drvpowfowg` FOREIGN KEY (`category_id`) REFERENCES `job_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `material_families` (
  `display_order` int(11) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_families_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `materials` (
  `display_order` int(11) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `family_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_materials_slug` (`slug`),
  KEY `idx_materials_family` (`family_id`,`display_order`),
  CONSTRAINT `FKrxwc0lpkf9c2d6bcdol6eb5p1` FOREIGN KEY (`family_id`) REFERENCES `material_families` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `notifications` (
  `is_read` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `message` text DEFAULT NULL,
  `target_id` varchar(255) DEFAULT NULL,
  `type` enum('ACCOUNT_REINSTATED','ACCOUNT_REJECTED','ACCOUNT_SUSPENDED','ACCOUNT_VALIDATED','FORMATEUR_APPROVED','FORMATEUR_GRANTED','FORMATEUR_REJECTED','FORMATEUR_REQUEST_SUBMITTED','FORMATEUR_REVOKED','FORMATION_APPROVED','FORMATION_REJECTED','NEW_FORMATION','NEW_MESSAGE','NEW_REPORT','NEW_REVIEW','PAYMENT_FAILED','PAYMENT_SUCCESS','SUBSCRIPTION_EXPIRED','SUBSCRIPTION_RENEWED') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9y21adhxn0ayjhfocscqox7bh` (`user_id`),
  CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `oauth_identities` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `provider` varchar(50) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `provider_user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oauth_provider_user` (`provider`,`provider_user_id`),
  KEY `idx_oauth_user_id` (`user_id`),
  KEY `idx_oauth_email` (`email`),
  CONSTRAINT `FKcwhpmr8ej1s107ds2ex7afd65` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `refresh_tokens` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `expiry_date` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `token` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refresh_tokens_token` (`token`),
  UNIQUE KEY `uk_refresh_tokens_user_id` (`user_id`),
  KEY `idx_refresh_tokens_expiry` (`expiry_date`),
  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `regions` (
  `display_order` int(11) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `code` varchar(10) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `parent_id` varchar(36) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_regions_slug` (`slug`),
  KEY `idx_regions_parent` (`parent_id`,`display_order`),
  CONSTRAINT `FK1nqjho4xdkm4f2tw928am9451` FOREIGN KEY (`parent_id`) REFERENCES `regions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `techniques` (
  `display_order` int(11) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_techniques_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `user_avatars` (
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `file_size` bigint(20) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `uploaded_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `content_type` varchar(50) NOT NULL,
  `original_filename` varchar(255) NOT NULL,
  `storage_key_medium` varchar(255) NOT NULL,
  `storage_key_original` varchar(255) NOT NULL,
  `storage_key_thumbnail` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_avatars_user_id` (`user_id`),
  KEY `idx_user_avatars_user_active` (`user_id`,`is_active`),
  KEY `idx_user_avatars_user_uploaded` (`user_id`,`uploaded_at`),
  CONSTRAINT `FKh03scppjwu7ge4p9wj9ap92jx` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `users` (
  `email_verified` bit(1) NOT NULL,
  `failed_login_attempts` int(11) NOT NULL,
  `banned_until` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `email_verified_at` datetime(6) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `locked_until` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `last_login_ip` varchar(45) DEFAULT NULL,
  `first_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `ban_reason` text DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','PENDING','REJECTED','SUSPENDED') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `idx_users_status` (`status`,`deleted_at`),
  KEY `idx_users_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `verification_tokens` (
  `attempts` int(11) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `code_hash` varchar(255) NOT NULL,
  `type` enum('EMAIL_VERIFICATION','PASSWORD_RESET') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_verification_tokens_user_type` (`user_id`,`type`,`used_at`),
  CONSTRAINT `FK54y8mqsnq1rtyf581sfmrbp4f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

SET FOREIGN_KEY_CHECKS=1;


