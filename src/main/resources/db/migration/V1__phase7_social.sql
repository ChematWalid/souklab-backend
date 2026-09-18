-- Phase 7 schema additions with MariaDB types matching the Hibernate mapping.

CREATE TABLE IF NOT EXISTS `feed_posts` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `published_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `author_id` varchar(36) NOT NULL,
  `formation_id` varchar(36) DEFAULT NULL,
  `id` varchar(36) NOT NULL,
  `moderated_by` varchar(36) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `body` text NOT NULL,
  `moderation_note` text DEFAULT NULL,
  `status` enum('HIDDEN','PENDING','PUBLISHED','REMOVED') NOT NULL,
  `type` enum('ACTUALITE','ANNONCE','FORMATION') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feed_posts_public` (`status`,`deleted_at`,`published_at`),
  KEY `idx_feed_posts_author` (`author_id`,`status`,`deleted_at`),
  KEY `FK13qfl80xfl2pho2u67v64p40v` (`formation_id`),
  KEY `FKeying8r17u4u397uqh6s73ykn` (`moderated_by`),
  CONSTRAINT `FK13qfl80xfl2pho2u67v64p40v` FOREIGN KEY (`formation_id`) REFERENCES `formations` (`id`),
  CONSTRAINT `FKavi9fu6x29ixg81gk9clvjw0r` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKeying8r17u4u397uqh6s73ykn` FOREIGN KEY (`moderated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `feed_post_media` (
  `display_order` int(11) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `file_size` bigint(20) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `post_id` varchar(36) NOT NULL,
  `content_type` varchar(100) NOT NULL,
  `storage_key` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKckhtslxg8e903tpe9rb1hjrw2` (`post_id`),
  CONSTRAINT `FKckhtslxg8e903tpe9rb1hjrw2` FOREIGN KEY (`post_id`) REFERENCES `feed_posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `artisan_reviews` (
  `rating` decimal(3,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `artisan_id` varchar(36) NOT NULL,
  `enrollment_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `reviewer_id` varchar(36) NOT NULL,
  `comment` text NOT NULL,
  `status` enum('HIDDEN','PUBLISHED','REMOVED') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_artisan_review_enrollment` (`enrollment_id`),
  KEY `idx_artisan_reviews_artisan` (`artisan_id`,`status`,`created_at`),
  KEY `idx_artisan_reviews_reviewer` (`reviewer_id`,`created_at`),
  CONSTRAINT `FK7npyfqd6l0if31o4aayxvshsk` FOREIGN KEY (`reviewer_id`) REFERENCES `artisans` (`id`),
  CONSTRAINT `FKe27m416io4a7w47jgk1lny9mg` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`),
  CONSTRAINT `FKehba8n01ct9piu6oysa6yd8vf` FOREIGN KEY (`enrollment_id`) REFERENCES `formation_enrollments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `content_reports` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `reporter_id` varchar(36) NOT NULL,
  `resolver_id` varchar(36) DEFAULT NULL,
  `target_id` varchar(36) NOT NULL,
  `reason` varchar(100) NOT NULL,
  `details` text DEFAULT NULL,
  `resolution_note` text DEFAULT NULL,
  `resolution_action` enum('DISMISS','HIDE','REMOVE') DEFAULT NULL,
  `status` enum('DISMISSED','OPEN','RESOLVED') NOT NULL,
  `target_type` enum('POST','REVIEW','USER') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_content_reports_queue` (`status`,`created_at`),
  KEY `idx_content_reports_target` (`target_type`,`target_id`),
  KEY `FK40bn3nq9t2qk66fkm6c3qwvjq` (`reporter_id`),
  KEY `FKtls6enms3r362ykxo3refu29` (`resolver_id`),
  CONSTRAINT `FK40bn3nq9t2qk66fkm6c3qwvjq` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKtls6enms3r362ykxo3refu29` FOREIGN KEY (`resolver_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
