package com.project.souklab.filestorage.config;

import com.project.souklab.analytics.AnalyticsMetric;
import com.project.souklab.config.OperationalMetrics;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/** Reports S3 availability so production readiness probes do not hide storage outages. */
@Component("storage")
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class StorageHealthIndicator implements HealthIndicator {

    private final StorageProperties properties;
    private final S3Client s3Client;
    private final OperationalMetrics metrics;

    public StorageHealthIndicator(StorageProperties properties, S3Client s3Client, OperationalMetrics metrics) {
        this.properties = properties;
        this.s3Client = s3Client;
        this.metrics = metrics;
    }

    @Override
    public Health health() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.getS3().getBucket()).build());
            metrics.setDependencyAvailability(AnalyticsMetric.Operational.Dependency.OBJECT_STORAGE, true);
            return Health.up().build();
        } catch (RuntimeException exception) {
            metrics.setDependencyAvailability(AnalyticsMetric.Operational.Dependency.OBJECT_STORAGE, false);
            return Health.down().withDetail("provider", "s3").build();
        }
    }
}
