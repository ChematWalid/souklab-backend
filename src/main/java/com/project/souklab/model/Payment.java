package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private User account;

    @Column(name = "subscription_id", nullable = false, length = 36)
    private String subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PaymentStatus status;

    @Column(name = "provider_checkout_id", unique = true, length = 120)
    private String providerCheckoutId;

    @Column(name = "provider_customer_id", length = 120)
    private String providerCustomerId;

    @Column(name = "provider_invoice_id", length = 120)
    private String providerInvoiceId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column
    private long fees;

    @Column(name = "checkout_url", length = 1000)
    private String checkoutUrl;

    @Column(name = "plan_snapshot", nullable = false, columnDefinition = "TEXT")
    private String planSnapshot;

    @Column(name = "provider_snapshot", columnDefinition = "TEXT")
    private String providerSnapshot;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Version
    @Column(name = "version_number", nullable = false)
    private long version;
}
