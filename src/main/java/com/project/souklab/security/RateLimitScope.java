package com.project.souklab.security;

import com.project.souklab.analytics.AnalyticsMetric;

/**
 * Fixed rate-limit scopes used for bucket keys and bounded operational tags.
 */
public final class RateLimitScope {
    private RateLimitScope() { }

    public enum Endpoint implements AnalyticsMetric.Key {
        CSV_EXPORTS("csv"), ANALYTICS("analytics"), AUTHENTICATION("auth"),
        ADMINISTRATION("admin"), PUBLIC_API("public");

        private final String value;

        Endpoint(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
