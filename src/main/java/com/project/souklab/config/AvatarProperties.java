package com.project.souklab.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for user avatar management.
 * Binds to prefix "avatar" in application.properties or environment.
 * All default values are defined externally in application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "avatar")
@Getter
@Setter
public class AvatarProperties {

    /**
     * Maximum allowed avatars stored per user in gallery history.
     */
    private long maxPerUser;

    /**
     * Strict set of MIME types allowed for user avatar uploads.
     */
    private List<String> allowedMimeTypes;

    /**
     * Dedicated rate-limiting configuration for avatar uploads.
     */
    private RateLimitProperties rateLimit = new RateLimitProperties();

    /**
     * Nested rate limiting configuration properties for avatar uploads.
     */
    @Getter
    @Setter
    public static class RateLimitProperties {
        /**
         * Whether rate limiting for avatar uploads is enabled.
         */
        private boolean enabled;

        /**
         * Maximum number of upload requests allowed within the refill duration.
         */
        private int capacity;

        /**
         * Duration over which the rate limit capacity refills (e.g. 1m).
         */
        private Duration refillDuration;

    }
}
