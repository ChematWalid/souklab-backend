-- Authorization schema migration.

DROP TABLE IF EXISTS user_roles;

DROP TABLE IF EXISTS roles;

CREATE TABLE IF NOT EXISTS `permissions` (
  `enabled` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` varchar(36) NOT NULL,
  `permission_key` varchar(100) NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh1ss8mmscopr690vkcj25uj9a` (`permission_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS `user_permissions` (
  `permission_id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  PRIMARY KEY (`permission_id`,`user_id`),
  KEY `FKkowxl8b2bngrxd1gafh13005u` (`user_id`),
  CONSTRAINT `FKkowxl8b2bngrxd1gafh13005u` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKq4qlrabt4s0etm9tfkoqfuib1` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

INSERT IGNORE INTO permissions (id, permission_key, description, enabled, created_at, updated_at) VALUES

    (UUID(), 'permission:admin:users', 'Manage users', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:admin:formations', 'Moderate formations', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:admin:feed', 'Moderate feed posts', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:admin:reports', 'Moderate reports', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:artisan:formations', 'Manage artisan formations', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:artisan:content', 'Create artisan content', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:artisan:reviews', 'Manage artisan reviews', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:profile:read', 'Read profiles', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:profile:write', 'Update own profile', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:report:create', 'Submit reports', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),

    (UUID(), 'permission:file:read', 'Read protected files', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
