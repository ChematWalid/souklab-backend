package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.rate-limit.endpoints")
public class RateLimitEndpointProperties {
    private RateLimitRule authentication = new RateLimitRule();
    private RateLimitRule publicApi = new RateLimitRule();
    private RateLimitRule adminApi = new RateLimitRule();
    private RateLimitRule analyticsJobs = new RateLimitRule();
    private RateLimitRule analyticsResults = new RateLimitRule();
    private RateLimitRule csvExports = new RateLimitRule();
    private RateLimitRule chargilyWebhook = new RateLimitRule();
}
