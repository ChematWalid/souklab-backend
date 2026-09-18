package com.project.souklab.dao;

import com.project.souklab.model.PaymentWebhookLog;
import com.project.souklab.model.WebhookProcessingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentWebhookLogRepository extends JpaRepository<PaymentWebhookLog, String> {
    Optional<PaymentWebhookLog> findByProviderEventId(String providerEventId);
    List<PaymentWebhookLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<PaymentWebhookLog> findByCreatedAtBefore(LocalDateTime cutoff, Pageable pageable);
    List<PaymentWebhookLog> findByProviderEventIdContainingIgnoreCaseOrEventTypeContainingIgnoreCaseOrProviderCheckoutIdContainingIgnoreCase(String eventId, String eventType, String checkoutId, Pageable pageable);

    @Modifying
    @Query(value = "INSERT IGNORE INTO payment_webhook_logs (id, provider_event_id, event_type, signature_valid, encrypted_payload, status, provider_checkout_id, created_at, updated_at) VALUES (:id, :eventId, :eventType, TRUE, :payload, :status, :checkoutId, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))", nativeQuery = true)
    int claimEvent(@Param("id") String id, @Param("eventId") String eventId, @Param("eventType") String eventType,
                   @Param("payload") String payload, @Param("status") String status, @Param("checkoutId") String checkoutId);

    @Modifying
    @Query(value = "UPDATE payment_webhook_logs SET status = 'PROCESSING', updated_at = CURRENT_TIMESTAMP(6) WHERE provider_event_id = :eventId AND (status IN ('RECEIVED', 'FAILED') OR (status = 'PROCESSING' AND updated_at < :staleBefore))", nativeQuery = true)
    int acquireProcessing(@Param("eventId") String eventId, @Param("staleBefore") LocalDateTime staleBefore);

    @Modifying
    @Query(value = "UPDATE payment_webhook_logs SET status = 'FAILED', failure_reason = :reason, updated_at = CURRENT_TIMESTAMP(6) WHERE provider_event_id = :eventId AND status = 'PROCESSING'", nativeQuery = true)
    int markFailed(@Param("eventId") String eventId, @Param("reason") String reason);

    List<PaymentWebhookLog> findByStatusAndUpdatedAtBefore(WebhookProcessingStatus status, LocalDateTime cutoff, Pageable pageable);
}
