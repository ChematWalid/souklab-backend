package com.project.souklab.dto.subscription;

import com.project.souklab.model.BillingPeriod;
import com.project.souklab.model.SubscriberType;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class SubscriptionPlanResponse {
    String id;
    SubscriberType subscriberType;
    String name;
    String description;
    BillingPeriod billingPeriod;
    long amount;
    String currency;
    boolean active;
    Map<String, String> entitlements;
}
