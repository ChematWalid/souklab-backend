-- Phase 8 messaging schema additions.

CREATE TABLE IF NOT EXISTS `conversations` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
INSERT IGNORE INTO permissions (id, permission_key, description, enabled, created_at, updated_at) VALUES (UUID(), 'permission:message:send', 'Send direct messages', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

CREATE TABLE IF NOT EXISTS `conversation_participants` (
  `archived` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `conversation_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `last_read_message_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_participant` (`conversation_id`,`user_id`),
  KEY `FKjukjgq6uinvvk4307y8u9lixu` (`user_id`),
  CONSTRAINT `FK84npv3fo2vwl7ut63im0p417q` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`),
  CONSTRAINT `FKjukjgq6uinvvk4307y8u9lixu` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
CREATE TABLE IF NOT EXISTS `messages` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `edited_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `author_id` varchar(36) NOT NULL,
  `conversation_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `idempotency_key` varchar(128) NOT NULL,
  `content` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_idempotency` (`conversation_id`,`author_id`,`idempotency_key`),
  KEY `FKowtlim26svclkatusptbgi7u1` (`author_id`),
  CONSTRAINT `FKowtlim26svclkatusptbgi7u1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKt492th6wsovh1nush5yl5jj8e` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `message_attachments` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `size` bigint(20) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `message_id` varchar(36) NOT NULL,
  `content_type` varchar(100) NOT NULL,
  `storage_key` varchar(500) NOT NULL,
  `original_filename` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj7twd218e2gqw9cmlhwvo1rth` (`message_id`),
  CONSTRAINT `FKj7twd218e2gqw9cmlhwvo1rth` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `message_attachment_uploads` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `size` bigint(20) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `conversation_id` varchar(36) NOT NULL,
  `id` varchar(36) NOT NULL,
  `owner_id` varchar(36) NOT NULL,
  `content_type` varchar(255) DEFAULT NULL,
  `original_filename` varchar(255) DEFAULT NULL,
  `storage_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKch5vh3pfvl1cesjl4g7lru51p` (`conversation_id`),
  KEY `FKe0kampj5ta5877472r0o68o9i` (`owner_id`),
  CONSTRAINT `FKch5vh3pfvl1cesjl4g7lru51p` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`),
  CONSTRAINT `FKe0kampj5ta5877472r0o68o9i` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
