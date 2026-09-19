CREATE TABLE analytics_jobs (
    id varchar(36) NOT NULL,
    owner_id varchar(36) NOT NULL,
    report_type varchar(40) NOT NULL,
    status varchar(16) NOT NULL,
    bucket varchar(16) NOT NULL,
    from_date date NOT NULL,
    range_to_date date NOT NULL,
    page_number int NOT NULL,
    page_size int NOT NULL,
    sort_field varchar(64),
    sort_direction varchar(8),
    filters_json text,
    permission_scope text NOT NULL,
    output_format varchar(8) NOT NULL,
    result_json longtext,
    failure_message text,
    completed_at datetime(6),
    expires_at datetime(6),
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    KEY idx_analytics_jobs_owner_status (owner_id, status, created_at),
    CONSTRAINT fk_analytics_job_owner FOREIGN KEY (owner_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE activity_events (
    id varchar(36) NOT NULL,
    event_type varchar(64) NOT NULL,
    actor_id varchar(36),
    subject_id varchar(36),
    event_time datetime(6) NOT NULL,
    metadata_json text,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    KEY idx_activity_events_type_time (event_type, event_time),
    KEY idx_activity_events_actor_time (actor_id, event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

INSERT IGNORE INTO permissions (id, permission_key, description, enabled, created_at, updated_at)
VALUES (UUID(), 'permission:analytics:admin', 'Run administrator analytics and exports', TRUE,
        CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
