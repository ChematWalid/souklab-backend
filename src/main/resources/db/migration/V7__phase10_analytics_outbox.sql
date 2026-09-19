CREATE TABLE analytics_outbox_events (
    id varchar(36) NOT NULL,
    event_id varchar(36) NOT NULL,
    event_type varchar(64) NOT NULL,
    payload_json text NOT NULL,
    status varchar(16) NOT NULL,
    attempt_count int NOT NULL,
    published_at datetime(6),
    last_error text,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_analytics_outbox_event (event_id),
    KEY idx_analytics_outbox_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE daily_kpi_rollups (
    id varchar(36) NOT NULL,
    rollup_date date NOT NULL,
    kpi_key varchar(80) NOT NULL,
    metric_value bigint NOT NULL,
    source_version int NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_daily_kpi_rollup_date_key (rollup_date, kpi_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE analytics_processed_events (
    id varchar(36) NOT NULL,
    event_id varchar(36) NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_analytics_processed_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
