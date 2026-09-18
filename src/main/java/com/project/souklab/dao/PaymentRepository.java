package com.project.souklab.dao;

import com.project.souklab.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.project.souklab.model.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByAccountIdAndIdempotencyKey(String accountId, String idempotencyKey);
    Optional<Payment> findByProviderCheckoutId(String providerCheckoutId);
    List<Payment> findBySubscriptionIdAndStatus(String subscriptionId, PaymentStatus status);
    Optional<Payment> findByIdAndAccountId(String id, String accountId);
    List<Payment> findByAccountIdOrderByCreatedAtDesc(String accountId, Pageable pageable);
    List<Payment> findByStatusOrderByCreatedAtAsc(PaymentStatus status, Pageable pageable);
    List<Payment> findByIdContainingIgnoreCaseOrSubscriptionIdContainingIgnoreCaseOrProviderCheckoutIdContainingIgnoreCase(String id, String subscriptionId, String providerCheckoutId, Pageable pageable);
}
