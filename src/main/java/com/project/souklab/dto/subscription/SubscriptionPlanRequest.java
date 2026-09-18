package com.project.souklab.dto.subscription;

import com.project.souklab.model.BillingPeriod;
import com.project.souklab.model.SubscriberType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

@Data
public class SubscriptionPlanRequest {
    @NotNull
    private SubscriberType subscriberType;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private BillingPeriod billingPeriod;
    @Positive
    private long amount;
    private boolean active = true;
    private Map<String, String> entitlements;
    @NotBlank
    private String reason;
}
