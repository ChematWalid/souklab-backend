package com.project.souklab.dto.subscription;

import com.project.souklab.model.BillingPeriod;
import com.project.souklab.model.SubscriberType;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class SubscriptionPlanSnapshot {
    String planId;
    String name;
    SubscriberType subscriberType;
    BillingPeriod billingPeriod;
    long amount;
    String currency;
    Map<String, String> entitlements;
}
