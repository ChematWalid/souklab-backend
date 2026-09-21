-- Authorization development reset: replace role assignments with direct permissions.
CREATE TABLE permissions (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    permission_key VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE user_permissions (
    user_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

INSERT INTO permissions (id, permission_key, description, enabled)
VALUES
    (UUID(), 'permission:admin:users', 'Manage users', TRUE),
    (UUID(), 'permission:admin:formations', 'Moderate formations', TRUE),
    (UUID(), 'permission:admin:feed', 'Moderate feed posts', TRUE),
    (UUID(), 'permission:admin:reports', 'Moderate reports', TRUE),
    (UUID(), 'permission:artisan:formations', 'Manage artisan formations', TRUE),
    (UUID(), 'permission:artisan:content', 'Create artisan content', TRUE),
    (UUID(), 'permission:artisan:reviews', 'Manage artisan reviews', TRUE),
    (UUID(), 'permission:profile:read', 'Read profiles', TRUE),
    (UUID(), 'permission:profile:write', 'Update own profile', TRUE),
    (UUID(), 'permission:report:create', 'Submit reports', TRUE),
    (UUID(), 'permission:file:read', 'Read protected files', TRUE);

DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
