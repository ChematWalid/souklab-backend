package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_webhook_logs")
@Getter
@Setter
@NoArgsConstructor
public class PaymentWebhookLog extends BaseEntity {
    @Column(name = "provider_event_id", nullable = false, unique = true, length = 160)
    private String providerEventId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "signature_valid", nullable = false)
    private boolean signatureValid;

    @Column(name = "encrypted_payload", nullable = false, columnDefinition = "TEXT")
    private String encryptedPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookProcessingStatus status;

    @Column(name = "provider_checkout_id", length = 120)
    private String providerCheckoutId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;
}
