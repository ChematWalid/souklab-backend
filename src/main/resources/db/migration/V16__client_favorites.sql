-- Client Favorites: artisans
-- Adds client_favorite_artisans table, seeds permission:client:favorites, and backfills existing clients.

CREATE TABLE IF NOT EXISTS `client_favorite_artisans` (
  `id` varchar(36) NOT NULL,
  `client_id` varchar(36) NOT NULL,
  `artisan_id` varchar(36) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_favorite_artisans_client_artisan` (`client_id`, `artisan_id`),
  KEY `idx_client_favorite_artisans_client_created` (`client_id`, `created_at` DESC),
  KEY `idx_client_favorite_artisans_artisan` (`artisan_id`),
  CONSTRAINT `fk_client_favorite_artisans_client` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_client_favorite_artisans_artisan` FOREIGN KEY (`artisan_id`) REFERENCES `artisans` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

INSERT IGNORE INTO `permissions` (`id`, `permission_key`, `description`, `enabled`, `created_at`, `updated_at`) VALUES
  (UUID(), 'permission:client:favorites', 'Manage client favorites', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

INSERT IGNORE INTO `user_permissions` (`permission_id`, `user_id`)
SELECT p.id, c.id
FROM `clients` c
CROSS JOIN `permissions` p
WHERE p.permission_key = 'permission:client:favorites'
  AND c.deleted_at IS NULL;
