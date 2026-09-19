package com.project.souklab.config;

import com.project.souklab.model.analytics.AnalyticsBucket;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/** Externalized policy for bounded analytics work. */
@Data
@ConfigurationProperties(prefix = "app.analytics")
public class AnalyticsProperties {
    private int defaultPageSize;
    private int maximumPageSize;
    private int maximumRangeDays;
    private int maximumBucketCount;
    private List<AnalyticsBucket> supportedBuckets;
    private int rollupBatchSize;
    private int backfillBatchSize;
    private Duration queryTimeout;
    private Duration jobRetention;
    private String businessTimeZone;
}
