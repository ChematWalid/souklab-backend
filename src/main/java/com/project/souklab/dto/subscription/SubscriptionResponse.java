package com.project.souklab.dto.subscription;

import com.project.souklab.model.BillingPeriod;
import com.project.souklab.model.SubscriberType;
import com.project.souklab.model.SubscriptionStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class SubscriptionResponse {
    String id;
    SubscriberType subscriberType;
    SubscriptionStatus status;
    String planName;
    BillingPeriod billingPeriod;
    long amount;
    String currency;
    LocalDateTime startsAt;
    LocalDateTime expiresAt;
}
