CREATE TABLE user_notification_preferences (
    id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    user_id VARCHAR(36) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_user_notification_preferences PRIMARY KEY (id),
    CONSTRAINT uk_notification_preference_user_type UNIQUE (user_id, notification_type),
    CONSTRAINT fk_notification_preference_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_notification_preferences_user ON user_notification_preferences (user_id);
