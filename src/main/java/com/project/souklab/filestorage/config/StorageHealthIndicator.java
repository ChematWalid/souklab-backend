package com.project.souklab.filestorage.config;

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

    public StorageHealthIndicator(StorageProperties properties, S3Client s3Client) {
        this.properties = properties;
        this.s3Client = s3Client;
    }

    @Override
    public Health health() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.getS3().getBucket()).build());
            return Health.up().build();
        } catch (RuntimeException exception) {
            return Health.down().withDetail("provider", "s3").build();
        }
    }
}
