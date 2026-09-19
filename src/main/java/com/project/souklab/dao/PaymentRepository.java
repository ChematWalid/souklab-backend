package com.project.souklab.dao;
import java.time.LocalDateTime;


import com.project.souklab.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.project.souklab.model.PaymentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    long countByStatus(PaymentStatus status);
    long countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(PaymentStatus status, LocalDateTime from, LocalDateTime to);
    @Query("select count(p) from Payment p where p.status = :status and p.manualGrant = false and p.createdAt >= :from and p.createdAt < :to and p.deletedAt is null")
    long countByStatusAndManualGrantFalseAndCreatedAtBetweenAndDeletedAtIsNull(@Param("status") PaymentStatus status,
                                                                                 @Param("from") LocalDateTime from,
                                                                                 @Param("to") LocalDateTime to);
    long countByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime from, LocalDateTime to);
    long countByManualGrantTrueAndCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime from, LocalDateTime to);
    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status and p.currency = :currency and p.manualGrant = false and p.createdAt >= :from and p.createdAt < :to and p.deletedAt is null")
    long sumAmountByStatusAndCurrencyAndCreatedAtBetween(@Param("status") PaymentStatus status, @Param("currency") String currency,
                                                         @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select coalesce(sum(p.fees), 0) from Payment p where p.status = :status and p.currency = :currency and p.manualGrant = false and p.createdAt >= :from and p.createdAt < :to and p.deletedAt is null")
    long sumFeesByStatusAndCurrencyAndCreatedAtBetween(@Param("status") PaymentStatus status, @Param("currency") String currency,
                                                       @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    Optional<Payment> findByAccountIdAndIdempotencyKey(String accountId, String idempotencyKey);
    Optional<Payment> findByProviderCheckoutId(String providerCheckoutId);
    List<Payment> findBySubscriptionIdAndStatus(String subscriptionId, PaymentStatus status);
    Optional<Payment> findByIdAndAccountId(String id, String accountId);
    List<Payment> findByAccountIdOrderByCreatedAtDesc(String accountId, Pageable pageable);
    List<Payment> findByStatusOrderByCreatedAtAsc(PaymentStatus status, Pageable pageable);
    List<Payment> findByIdContainingIgnoreCaseOrSubscriptionIdContainingIgnoreCaseOrProviderCheckoutIdContainingIgnoreCase(String id, String subscriptionId, String providerCheckoutId, Pageable pageable);
}
