ALTER TABLE content_reports
    ADD COLUMN resolved_at datetime(6) NULL;

CREATE INDEX idx_content_reports_resolution_time
    ON content_reports (resolved_at, created_at, deleted_at);
