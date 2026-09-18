package com.project.souklab.config;

import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class SubscriptionProperties {
    private boolean enabled;
    private String currency;
    private List<Long> reminderOffsets;
    private Duration lifecycleInterval;
    private String lifecycleTimeZone;
    private int lifecycleBatchSize;
    private int minimumPlanAmount;
    private int maximumPlanAmount;
    private Duration checkoutIdempotencyRetention;
    private Duration webhookRetention;
}
