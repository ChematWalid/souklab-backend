CREATE TABLE analytics_maintenance_jobs (
    id varchar(36) NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6) NULL,
    owner_id varchar(36) NOT NULL,
    operation varchar(16) NOT NULL,
    status varchar(16) NOT NULL,
    from_date date NOT NULL,
    range_to_date date NOT NULL,
    events_read int NOT NULL DEFAULT 0,
    rollups_written int NOT NULL DEFAULT 0,
    permission_scope varchar(200) NOT NULL,
    failure_message text NULL,
    completed_at datetime(6) NULL,
    expires_at datetime(6) NULL,
    PRIMARY KEY (id),
    KEY idx_analytics_maintenance_owner_status (owner_id, status, created_at)
);
