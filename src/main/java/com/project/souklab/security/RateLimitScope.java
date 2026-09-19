package com.project.souklab.security;

import com.project.souklab.analytics.AnalyticsMetric;

/**
 * Fixed rate-limit scopes used for bucket keys and bounded operational tags.
 */
public final class RateLimitScope {
    private RateLimitScope() { }

    public static final class Endpoint {
        private Endpoint() { }

        public enum Csv implements AnalyticsMetric.Key {
            EXPORTS("csv");

            private final String value;

            Csv(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Analytics implements AnalyticsMetric.Key {
            API("analytics");

            private final String value;

            Analytics(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Authentication implements AnalyticsMetric.Key {
            API("auth");

            private final String value;

            Authentication(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Administration implements AnalyticsMetric.Key {
            API("admin");

            private final String value;

            Administration(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Public implements AnalyticsMetric.Key {
            API("public");

            private final String value;

            Public(String value) { this.value = value; }

            public String value() { return value; }
        }
    }
}
