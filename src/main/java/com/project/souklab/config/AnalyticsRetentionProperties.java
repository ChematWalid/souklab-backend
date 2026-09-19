package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.analytics.retention")
public class AnalyticsRetentionProperties {
    private Duration rawEvents;
    private Duration jobs;
    private Duration rollups;
    private Duration cleanupInterval;
    private int cleanupBatchSize;
}
