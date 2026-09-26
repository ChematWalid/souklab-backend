package com.project.souklab.event.subscription;

import com.project.souklab.event.DomainEvent;
import com.project.souklab.model.PaymentProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a subscription payment transaction completes successfully.
 */
public record PaymentCompletedEvent(
        String eventId,
        String paymentId,
        String accountId,
        BigDecimal amount,
        String planId,
        PaymentProvider provider,
        Instant occurredAt
) implements DomainEvent {

    public static PaymentCompletedEvent of(
            String paymentId,
            String accountId,
            BigDecimal amount,
            String planId,
            PaymentProvider provider
    ) {
        return new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                paymentId,
                accountId,
                amount,
                planId,
                provider,
                Instant.now()
        );
    }
}
