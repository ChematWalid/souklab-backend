-- Phase 9: database-managed plans, manual subscriptions, payments, and verified webhooks.
ALTER TABLE audit_logs MODIFY action enum(
    'APPROVE_ARTISAN','APPROVE_FORMATION','APPROVE_USER','ASSIGN_PERMISSION_BULK','ASSIGN_ROLE',
    'BAN_USER','DISMISS_REPORT','EMAIL_VERIFIED','PASSWORD_CHANGED','PASSWORD_RESET_COMPLETED',
    'PERMISSION_GRANTED','PERMISSION_REVOKED','REJECT_ARTISAN','REJECT_FORMATION','RESOLVE_REPORT',
    'TIMEOUT_USER','UNBAN_USER','SUBSCRIPTION_GRANTED','SUBSCRIPTION_CANCELED','SUBSCRIPTION_REVOKED',
    'SUBSCRIPTION_STATE_CORRECTED','PAYMENT_STATE_CORRECTED','REFUND_REQUEST_REJECTED',
    'SUBSCRIPTION_PLAN_CREATED','SUBSCRIPTION_PLAN_UPDATED','SUBSCRIPTION_PLAN_DEACTIVATED'
) NOT NULL;

ALTER TABLE notifications MODIFY type enum(
    'ACCOUNT_REINSTATED','ACCOUNT_REJECTED','ACCOUNT_SUSPENDED','ACCOUNT_VALIDATED','FORMATEUR_APPROVED',
    'FORMATEUR_GRANTED','FORMATEUR_REJECTED','FORMATEUR_REQUEST_SUBMITTED','FORMATEUR_REVOKED',
    'FORMATION_APPROVED','FORMATION_REJECTED','NEW_FORMATION','NEW_MESSAGE','NEW_REPORT','NEW_REVIEW',
    'PAYMENT_FAILED','PAYMENT_SUCCESS','SUBSCRIPTION_RENEWED','SUBSCRIPTION_EXPIRED','CHECKOUT_CREATED',
    'CHECKOUT_CANCELED','SUBSCRIPTION_RENEWAL_REMINDER','SUBSCRIPTION_MANUALLY_GRANTED','SUBSCRIPTION_REVOKED',
    'REFUND_REQUEST_UNAVAILABLE'
) DEFAULT NULL;

ALTER TABLE audit_logs
    ADD COLUMN target_account_id varchar(36) NULL,
    ADD COLUMN operation varchar(80) NULL,
    ADD COLUMN previous_state varchar(500) NULL,
    ADD COLUMN new_state varchar(500) NULL,
    ADD COLUMN reason varchar(1000) NULL,
    ADD COLUMN payment_id varchar(36) NULL,
    ADD COLUMN subscription_id varchar(36) NULL;

CREATE TABLE subscription_pricing (
    id varchar(36) NOT NULL,
    subscriber_type varchar(20) NOT NULL,
    name varchar(120) NOT NULL,
    description text,
    billing_period varchar(20) NOT NULL,
    amount bigint NOT NULL,
    currency varchar(3) NOT NULL,
    active bit(1) NOT NULL,
    version_number bigint NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    KEY idx_subscription_plans_active (active, subscriber_type, billing_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE subscription_plan_entitlements (
    id varchar(36) NOT NULL,
    plan_id varchar(36) NOT NULL,
    entitlement_key varchar(100) NOT NULL,
    entitlement_value varchar(500) NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_subscription_entitlement (plan_id, entitlement_key),
    CONSTRAINT fk_subscription_entitlement_plan FOREIGN KEY (plan_id) REFERENCES subscription_pricing (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE artisan_subscriptions (
    id varchar(36) NOT NULL,
    account_id varchar(36) NOT NULL,
    status varchar(20) NOT NULL,
    plan_id varchar(36) NOT NULL,
    plan_name varchar(120) NOT NULL,
    billing_period varchar(20) NOT NULL,
    amount bigint NOT NULL,
    currency varchar(3) NOT NULL,
    entitlements_snapshot text NOT NULL,
    starts_at datetime(6),
    expires_at datetime(6),
    reminder_offsets_sent varchar(100),
    version_number bigint NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    active_key varchar(36) AS (IF(status = 'ACTIVE', account_id, NULL)) PERSISTENT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_artisan_active_subscription (active_key),
    KEY idx_artisan_subscription_expiry (status, expires_at),
    KEY idx_artisan_subscription_reminders (status, expires_at, reminder_offsets_sent(50)),
    CONSTRAINT fk_artisan_subscription_account FOREIGN KEY (account_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE client_subscriptions (
    id varchar(36) NOT NULL,
    account_id varchar(36) NOT NULL,
    status varchar(20) NOT NULL,
    plan_id varchar(36) NOT NULL,
    plan_name varchar(120) NOT NULL,
    billing_period varchar(20) NOT NULL,
    amount bigint NOT NULL,
    currency varchar(3) NOT NULL,
    entitlements_snapshot text NOT NULL,
    starts_at datetime(6),
    expires_at datetime(6),
    reminder_offsets_sent varchar(100),
    version_number bigint NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    active_key varchar(36) AS (IF(status = 'ACTIVE', account_id, NULL)) PERSISTENT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_client_active_subscription (active_key),
    KEY idx_client_subscription_expiry (status, expires_at),
    KEY idx_client_subscription_reminders (status, expires_at, reminder_offsets_sent(50)),
    CONSTRAINT fk_client_subscription_account FOREIGN KEY (account_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE payments (
    id varchar(36) NOT NULL,
    account_id varchar(36) NOT NULL,
    subscription_id varchar(36) NOT NULL,
    provider varchar(20) NOT NULL,
    status varchar(25) NOT NULL,
    provider_checkout_id varchar(120),
    provider_customer_id varchar(120),
    provider_invoice_id varchar(120),
    amount bigint NOT NULL,
    currency varchar(3) NOT NULL,
    fees bigint NOT NULL,
    checkout_url varchar(1000),
    plan_snapshot text NOT NULL,
    provider_snapshot text,
    idempotency_key varchar(120) NOT NULL,
    version_number bigint NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_provider_checkout (provider_checkout_id),
    UNIQUE KEY uk_payment_account_idempotency (account_id, idempotency_key),
    KEY idx_payment_status_created (status, created_at),
    CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE payment_webhook_logs (
    id varchar(36) NOT NULL,
    provider_event_id varchar(160) NOT NULL,
    event_type varchar(80) NOT NULL,
    signature_valid bit(1) NOT NULL,
    encrypted_payload text NOT NULL,
    status varchar(20) NOT NULL,
    provider_checkout_id varchar(120),
    failure_reason varchar(500),
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    deleted_at datetime(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_webhook_provider_event (provider_event_id),
    KEY idx_webhook_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

INSERT IGNORE INTO permissions (id, permission_key, description, enabled, created_at, updated_at)
VALUES (UUID(), 'permission:financial:admin', 'Manage subscription and payment operations', TRUE,
        CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
