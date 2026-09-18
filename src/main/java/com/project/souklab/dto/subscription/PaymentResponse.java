package com.project.souklab.dto.subscription;

import com.project.souklab.model.PaymentProvider;
import com.project.souklab.model.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class PaymentResponse {
    String id;
    String subscriptionId;
    PaymentProvider provider;
    PaymentStatus status;
    long amount;
    String currency;
    String checkoutUrl;
    LocalDateTime createdAt;
}
