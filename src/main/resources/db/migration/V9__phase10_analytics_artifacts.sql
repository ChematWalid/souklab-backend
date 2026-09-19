CREATE TABLE analytics_job_artifacts (
    id varchar(36) NOT NULL,
    job_id varchar(36) NOT NULL,
    storage_key varchar(500) NOT NULL,
    content_type varchar(120) NOT NULL,
    file_name varchar(180) NOT NULL,
    size bigint NOT NULL,
    sha256 varchar(64) NOT NULL,
    expires_at datetime(6) NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    KEY idx_analytics_artifact_job (job_id, created_at),
    CONSTRAINT fk_analytics_artifact_job FOREIGN KEY (job_id) REFERENCES analytics_jobs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
