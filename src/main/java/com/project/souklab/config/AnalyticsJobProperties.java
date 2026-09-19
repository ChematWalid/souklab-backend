package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** External limits for asynchronous analytics execution and result materialization. */
@Data
@ConfigurationProperties(prefix = "app.analytics.jobs")
public class AnalyticsJobProperties {
    private int concurrency;
    private int maximumResultRows;
    private long maximumResultBytes;
}
