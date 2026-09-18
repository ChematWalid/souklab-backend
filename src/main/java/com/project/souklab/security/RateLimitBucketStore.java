package com.project.souklab.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;

import java.time.Duration;

/** Resolves a rate-limit bucket from the configured shared state store. */
public interface RateLimitBucketStore {

    Bucket resolve(String key, long capacity, Duration refillDuration);

    /** Small local implementation used by unit tests and non-production slices. */
    static RateLimitBucketStore inMemory() {
        java.util.concurrent.ConcurrentHashMap<String, Bucket> buckets = new java.util.concurrent.ConcurrentHashMap<>();
        return (key, capacity, refillDuration) -> buckets.computeIfAbsent(key, ignored ->
                Bucket.builder().addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillDuration)
                        .build()).build());
    }

    static BucketConfiguration configuration(long capacity, Duration refillDuration) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillDuration)
                        .build())
                .build();
    }
}
