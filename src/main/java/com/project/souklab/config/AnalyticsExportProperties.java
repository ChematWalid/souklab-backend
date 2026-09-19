package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** External policy for persisted JSON and CSV analytics artifacts. */
@Data
@ConfigurationProperties(prefix = "app.analytics.exports")
public class AnalyticsExportProperties {
    private String storagePrefix;
    private Duration retention;
}
