package com.project.souklab.config;

import lombok.Data;

import java.time.Duration;

@Data
public class RateLimitRule {
    private boolean enabled;
    private int capacity;
    private Duration refillDuration;
    /** Optional authenticated-user override; zero/null means use the global user policy. */
    private int userCapacity;
    private Duration userRefillDuration;
}
