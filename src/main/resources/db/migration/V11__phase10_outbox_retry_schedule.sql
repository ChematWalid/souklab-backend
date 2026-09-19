-- Persist retry scheduling so broker outages do not consume all outbox attempts in one poll cycle.
ALTER TABLE analytics_outbox_events
    ADD COLUMN next_attempt_at datetime(6) NULL;

UPDATE analytics_outbox_events
SET next_attempt_at = COALESCE(next_attempt_at, created_at)
WHERE next_attempt_at IS NULL;

CREATE INDEX idx_analytics_outbox_next_attempt
    ON analytics_outbox_events (status, next_attempt_at, created_at);
