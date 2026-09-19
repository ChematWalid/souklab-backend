-- Preserve payment origin so manual grants cannot become cash revenue after a status correction.
ALTER TABLE payments
    ADD COLUMN manual_grant boolean NOT NULL DEFAULT FALSE;

UPDATE payments
SET manual_grant = TRUE
WHERE status = 'MANUALLY_GRANTED';

CREATE INDEX idx_payment_manual_status_created
    ON payments (manual_grant, status, created_at);
