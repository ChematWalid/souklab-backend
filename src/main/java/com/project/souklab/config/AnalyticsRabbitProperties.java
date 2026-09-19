package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.analytics.rabbit")
public class AnalyticsRabbitProperties {
    private boolean enabled;
    private String host;
    private int port;
    private String username;
    private String password;
    private String exchange;
    private String queue;
    private String routingKey;
    private String deadLetterExchange;
    private String deadLetterQueue;
    private int relayBatchSize;
    private int maxAttempts;
    private Duration retryBackoff;
    private Duration maxRetryBackoff;
    private double retryMultiplier;
    private Duration pollInterval;
    private Duration confirmTimeout;
}
