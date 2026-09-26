package com.project.souklab.event.listener;

import com.project.souklab.event.formation.FormationEnrolledEvent;
import com.project.souklab.event.subscription.PaymentCompletedEvent;
import com.project.souklab.event.user.UserStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Decoupled event listener demonstrating clean modular monolith event handling.
 * Reacts to domain events asynchronously after the initiating transaction has committed,
 * ensuring failure in secondary operations (analytics, notifications, external webhooks)
 * never corrupts or rolls back the core domain transaction.
 */
@Component
@Slf4j
public class AnalyticsDomainEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserStatusChanged(UserStatusChangedEvent event) {
        log.info("Domain Event [AFTER_COMMIT]: User '{}' status changed from {} to {}. Reason: {}",
                event.userId(), event.previousStatus(), event.newStatus(), event.reason());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFormationEnrolled(FormationEnrolledEvent event) {
        log.info("Domain Event [AFTER_COMMIT]: Artisan '{}' enrolled in formation '{}' (enrollment: {})",
                event.artisanId(), event.formationId(), event.enrollmentId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Domain Event [AFTER_COMMIT]: Payment '{}' completed for account '{}'. Amount: {}, Plan: {}",
                event.paymentId(), event.accountId(), event.amount(), event.planId());
    }
}
