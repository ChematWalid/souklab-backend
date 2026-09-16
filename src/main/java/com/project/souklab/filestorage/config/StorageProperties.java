package com.project.souklab.filestorage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for the file storage module.
 * Binds to prefix "storage" in application.properties or environment.
 * All default values are defined externally in application.properties.
 */
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageProperties {

    /**
     * Storage backend provider name: "in-memory" (test/dev stub), "s3" (MinIO/AWS/R2).
     */
    private String provider;

    /**
     * Validation rules for uploaded files.
     */
    private ValidationProperties validation = new ValidationProperties();

    /**
     * Nested validation properties for uploaded files.
     */
    @Getter
    @Setter
    public static class ValidationProperties {
        /**
         * Maximum allowed file size for uploads (e.g. 10MB).
         */
        private DataSize maxFileSize;

        /**
         * List of allowed MIME types. Note: WebP was deliberately excluded because no pure-Java ImageIO
         * writer exists, and native JNI options were rejected due to portability and container-compatibility concerns.
         */
        private List<String> allowedMimeTypes;
    }

    /**
     * S3-compatible storage configuration (MinIO, AWS S3, Cloudflare R2).
     */
    private S3Properties s3 = new S3Properties();

    /**
     * Nested S3-compatible configuration properties.
     */
    @Getter
    @Setter
    public static class S3Properties {
        /**
         * S3 endpoint URI (e.g. "http://localhost:9000" for local MinIO; empty or null for AWS S3).
         */
        private String endpoint;

        /**
         * AWS region (e.g. "us-east-1").
         */
        private String region;

        /**
         * Target S3 bucket name.
         */
        private String bucket;

        /**
         * S3 Access Key ID / Root User.
         */
        private String accessKey;

        /**
         * S3 Secret Access Key / Root Password.
         */
        private String secretKey;

        /**
         * Whether to use path-style access (e.g. http://endpoint/bucket/key).
         * Required for MinIO; compatible with AWS S3.
         */
        private Boolean pathStyleAccess;

        /**
         * Automatically create bucket on startup if it doesn't exist.
         */
        private Boolean autoCreateBucket;
    }

    /**
     * Antivirus scanning configuration for ClamAV daemon integration.
     */
    private VirusScanProperties virusScan = new VirusScanProperties();

    /**
     * Nested antivirus scanning configuration properties.
     */
    @Getter
    @Setter
    public static class VirusScanProperties {
        /**
         * Whether antivirus scanning via ClamAV is enabled.
         */
        private boolean enabled;

        /**
         * ClamAV daemon hostname or IP address.
         */
        private String host;

        /**
         * ClamAV daemon TCP port (standard: 3310).
         */
        private int port;

        /**
         * Socket connection timeout.
         */
        private Duration connectionTimeout;

        /**
         * Socket read timeout for scanning streaming data.
         */
        private Duration readTimeout;

        /**
         * Failure policy when ClamAV daemon is unreachable or errors:
         * true = fail-open (log warning, allow upload to proceed),
         * false = fail-closed (reject upload with VirusScanException).
         */
        private boolean failOpen;
    }

    /**
     * Rate limiting configuration for file operations.
     */
        private RateLimitProperties rateLimit = new RateLimitProperties();

    /**
     * Nested rate limiting configuration properties for file operations.
     */
    @Getter
    @Setter
    public static class RateLimitProperties {
        /**
         * Whether rate limiting for file operations is enabled.
         */
        private boolean enabled;

        /**
         * Maximum number of requests allowed within the refill duration.
         */
        private int capacity;

        /**
         * Duration over which the rate limit capacity refills (e.g. 1m).
         */
        private Duration refillDuration;

        /**
         * In-memory cache configuration for rate limiting buckets.
         */
        private CacheProperties cache = new CacheProperties();

        /**
         * Cache configuration properties for in-memory rate limiting buckets.
         */
        @Getter
        @Setter
        public static class CacheProperties {
            /**
             * Maximum number of client buckets retained in memory.
             */
            private long maximumSize;

            /**
             * Inactivity duration after which an idle client bucket is evicted.
             */
            private Duration expireAfterAccess;
        }
    }
}
