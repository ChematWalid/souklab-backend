package com.project.souklab.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.project.souklab.model.EnumValue;
/**
 * Stable keys used by the analytics result and rollup contracts.
 *
 * <p>The enum names provide a typed vocabulary inside the application while
 * {@link Key#value()} preserves the existing JSON and database key values.</p>
 */
public final class AnalyticsMetric {
    private AnalyticsMetric() { }

    public interface Key extends EnumValue { }

    public enum Summary implements Key {
        TOTAL_USERS("totalUsers"), NEW_REGISTRATIONS("newRegistrations"),
        VERIFIED_REGISTRATIONS("verifiedRegistrations"), ACTIVATION_RATE("activationRate"),
        VERIFIED_USERS("verifiedUsers"), ARTISAN_PROFILES("artisanProfiles"),
        CLIENT_PROFILES("clientProfiles"), ACTIVE_ARTISAN_PROFILES("activeArtisanProfiles"),
        ACTIVE_CLIENT_PROFILES("activeClientProfiles"), ACTIVE_USERS("activeUsers"),
        PENDING_USERS("pendingUsers"), SUSPENDED_USERS("suspendedUsers"),
        PENDING_USER_APPROVALS("pendingUserApprovals"), MODERATION_ACTIVITY("moderationActivity"),
        USER_STATUSES("userStatuses"), ACTIVITY_EVENTS("activityEvents"),
        SUCCESSFUL_LOGINS("successfulLogins"), PUBLISHED_POSTS("publishedPosts"),
        MESSAGES_SENT("messagesSent"), PROFILE_VIEWS("profileViews"),
        REPORT_RESOLUTIONS("reportResolutions"), AVERAGE_REPORT_RESOLUTION_SECONDS("averageReportResolutionSeconds"),
        DAU("dau"), WAU("wau"), MAU("mau"),
        ENGAGEMENT_BY_ACCOUNT_TYPE("engagementByAccountType"),
        ENGAGEMENT_BY_REGION("engagementByRegion"),
        ENGAGEMENT_BY_CRAFT_CATEGORY("engagementByCraftCategory"),
        LOGIN_RETENTION_COHORTS("loginRetentionCohorts"), FEED_POSTS_CREATED("feedPostsCreated"),
        FORMATIONS_CREATED("formationsCreated"), FORMATION_ENROLLMENTS("formationEnrollments"),
        ACTIVE_INSTRUCTORS("activeInstructors"), FORMATION_UTILIZATION_RATE("formationUtilizationRate"),
        REVIEWS_SUBMITTED("reviewsSubmitted"), PUBLISHED_REVIEWS("publishedReviews"),
        AVERAGE_PUBLISHED_RATING("averagePublishedRating"), REPORTS_SUBMITTED("reportsSubmitted"),
        FORMATEUR_PENDING("formateurPending"), FORMATEUR_APPROVED_IN_RANGE("formateurApprovedInRange"),
        FORMATEUR_REJECTED_IN_RANGE("formateurRejectedInRange"), FORMATEUR_STATUSES("formateurStatuses"),
        PAYMENTS_CREATED("paymentsCreated"), FEED_POSTS_BY_STATUS("feedPostsByStatus"),
        FORMATIONS_BY_STATUS("formationsByStatus"), ENROLLMENTS_BY_STATUS("enrollmentsByStatus"),
        FORMATION_COMPLETIONS("formationCompletions"), ENROLLMENT_CANCELLATION_RATE("enrollmentCancellationRate"),
        REPORTS_BY_STATUS("reportsByStatus"), PAYMENTS_BY_STATUS("paymentsByStatus"),
        SUBSCRIPTIONS_BY_STATUS("subscriptionsByStatus"), SUBSCRIPTIONS_BY_SUBSCRIBER_TYPE("subscriptionsBySubscriberType"),
        SUBSCRIPTION_LIFECYCLE_EVENTS("subscriptionLifecycleEvents"), CHECKOUT_CREATED("checkoutCreated"),
        PAYMENT_STATE_TRANSITIONS("paymentStateTransitions"), GROSS_COLLECTED_DZD("grossCollectedDzd"),
        PROVIDER_FEES_DZD("providerFeesDzd"), NET_COLLECTED_DZD("netCollectedDzd"),
        PAYMENT_CONVERSION_RATE("paymentConversionRate"), MANUAL_GRANTS("manualGrants"),
        OPERATIONAL("operational"), PERIOD_COMPARISON("periodComparison");

        private final String value;

        Summary(String value) { this.value = value; }

        public String value() { return value; }

        @JsonCreator
        public static Summary fromValue(String value) {
            for (Summary key : values()) if (key.value.equals(value)) return key;
            throw new IllegalArgumentException("Unknown analytics summary key: " + value);
        }
    }

    public enum Result implements Key {
        REPORT_TYPE("reportType"), FROM_DATE("fromDate"), TO_DATE("toDate"), BUCKET("bucket"),
        SUMMARY("summary"), TABLES("tables"), SERIES("series"), CONTENT("content");

        private final String value;

        Result(String value) { this.value = value; }

        public String value() { return value; }

        @JsonCreator
        public static Result fromValue(String value) {
            for (Result key : values()) if (key.value.equals(value)) return key;
            throw new IllegalArgumentException("Unknown analytics result key: " + value);
        }
    }

    public enum Series implements Key {
        START_DATE("startDate"), END_DATE("endDate"), ACTIVITY_EVENTS("activityEvents"),
        UNIQUE_ACTORS("uniqueActors"), NEW_REGISTRATIONS("newRegistrations");

        private final String value;

        Series(String value) { this.value = value; }

        public String value() { return value; }

        @JsonCreator
        public static Series fromValue(String value) {
            for (Series key : values()) if (key.value.equals(value)) return key;
            throw new IllegalArgumentException("Unknown analytics series key: " + value);
        }
    }

    public enum Comparison implements Key {
        CURRENT("current"), PREVIOUS("previous");

        private final String value;

        Comparison(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum AccountType implements Key {
        ARTISAN("artisan"), CLIENT("client");

        private final String value;

        AccountType(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum Table implements Key {
        USERS("users"), FORMATEUR_REQUESTS("formateurRequests"), REPORTS("reports"),
        FEED_POSTS("feedPosts"), FORMATIONS("formations"), ENROLLMENTS("enrollments"),
        PAYMENTS("payments"), SUBSCRIPTIONS("subscriptions"), SUBSCRIPTIONS_ARTISAN("subscriptionsArtisan"),
        SUBSCRIPTIONS_CLIENT("subscriptionsClient"), ACTIVITY("activity");

        private final String value;

        Table(String value) { this.value = value; }

        public String value() { return value; }

        @JsonCreator
        public static Table fromValue(String value) {
            for (Table key : values()) if (key.value.equals(value)) return key;
            throw new IllegalArgumentException("Unknown analytics table key: " + value);
        }
    }

    public enum Operational implements Key {
        ANALYTICS_JOBS_QUEUED("analyticsJobsQueued"), ANALYTICS_JOBS_RUNNING("analyticsJobsRunning"),
        ANALYTICS_JOBS_COMPLETED("analyticsJobsCompleted"), ANALYTICS_JOBS_FAILED("analyticsJobsFailed"),
        MAINTENANCE_JOBS_QUEUED("maintenanceJobsQueued"), MAINTENANCE_JOBS_RUNNING("maintenanceJobsRunning"),
        MAINTENANCE_JOBS_COMPLETED("maintenanceJobsCompleted"), MAINTENANCE_JOBS_FAILED("maintenanceJobsFailed"),
        OUTBOX_PENDING("outboxPending"), OUTBOX_PUBLISHED("outboxPublished"), OUTBOX_DEAD_LETTER("outboxDeadLetter"),
        APPLICATION_HEALTH("applicationHealth"), HEALTH_COMPONENTS("healthComponents"),
        REQUEST_COUNTERS("requestCounters"), RATE_LIMIT_REJECTIONS("rateLimitRejections"),
        REQUEST_COUNTER_METRIC("souklab.http.requests"), RATE_LIMIT_REJECTION_METRIC("souklab.rate_limit.rejections");

        private final String value;

        Operational(String value) { this.value = value; }

        public String value() { return value; }

        public enum Metric implements Key {
            UPLOADS("souklab.uploads"), VIRUS_SCANS("souklab.virus.scans"),
            SEARCH_REQUESTS("souklab.search.requests"), WEBSOCKET_CONNECTIONS("souklab.websocket.connections"),
            RATE_LIMIT_REJECTIONS("souklab.rate_limit.rejections"), HTTP_REQUESTS("souklab.http.requests");

            private final String value;

            Metric(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Dependency implements Key {
            ELASTICSEARCH("elasticsearch"), REDIS("redis"), RABBITMQ("rabbitmq"),
            OBJECT_STORAGE("s3"), CLAMAV("clamav");

            private final String value;

            Dependency(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Component implements Key {
            CLAMAV("clamav"), STOMP("stomp");

            private final String value;

            Component(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Operation implements Key {
            AVATAR("avatar");

            private final String value;

            Operation(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Scope implements Key {
            USER("user");

            private final String value;

            Scope(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Backend implements Key {
            RELATIONAL("relational"), ELASTICSEARCH("elasticsearch");

            private final String value;

            Backend(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Outcome implements Key {
            SUCCESS("success"), FAILURE("failure"), AUTHENTICATED("authenticated"),
            REJECTED("rejected"), DISABLED("disabled"), FALLBACK("fallback"),
            ERROR("error"), INFECTED("infected"), ERROR_ALLOWED("error_allowed"),
            ERROR_REJECTED("error_rejected"), CLEAN("clean");

            private final String value;

            Outcome(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum HttpMethod implements Key {
            GET("GET"), POST("POST"), PUT("PUT"), PATCH("PATCH"), DELETE("DELETE"),
            HEAD("HEAD"), OPTIONS("OPTIONS"), TRACE("TRACE"), CONNECT("CONNECT"),
            UNKNOWN("UNKNOWN");

            private final String value;

            HttpMethod(String value) { this.value = value; }

            public String value() { return value; }

            public static HttpMethod fromValue(String value) {
                for (HttpMethod method : values()) {
                    if (method.value.equalsIgnoreCase(value)) return method;
                }
                return UNKNOWN;
            }
        }

        public enum RequestOutcome implements Key {
            SUCCESS_2XX("2xx"), REDIRECT_3XX("3xx"), CLIENT_ERROR_4XX("4xx"),
            SERVER_ERROR_5XX("5xx"), ERROR("error");

            private final String value;

            RequestOutcome(String value) { this.value = value; }

            public String value() { return value; }

            public static RequestOutcome fromStatus(int status) {
                if (status >= 500) return SERVER_ERROR_5XX;
                if (status >= 400) return CLIENT_ERROR_4XX;
                if (status >= 300) return REDIRECT_3XX;
                return SUCCESS_2XX;
            }
        }
    }

    public enum Retention implements Key {
        DAY_1("day1"), DAY_7("day7"), DAY_30("day30"),
        COHORT_DATE("cohortDate"), COHORT_SIZE("cohortSize");

        private final String value;

        Retention(String value) { this.value = value; }

        public String value() { return value; }

        public Row retainedRow() {
            return switch (this) {
                case DAY_1 -> Row.DAY_1_RETAINED;
                case DAY_7 -> Row.DAY_7_RETAINED;
                case DAY_30 -> Row.DAY_30_RETAINED;
                default -> throw new IllegalStateException("Retention row is not a retention window: " + this);
            };
        }

        public Row rateRow() {
            return switch (this) {
                case DAY_1 -> Row.DAY_1_RATE;
                case DAY_7 -> Row.DAY_7_RATE;
                case DAY_30 -> Row.DAY_30_RATE;
                default -> throw new IllegalStateException("Retention row is not a retention window: " + this);
            };
        }

        public enum Row implements Key {
            COHORT_DATE("cohortDate"), COHORT_SIZE("cohortSize"),
            DAY_1_RETAINED("day1Retained"), DAY_1_RATE("day1Rate"),
            DAY_7_RETAINED("day7Retained"), DAY_7_RATE("day7Rate"),
            DAY_30_RETAINED("day30Retained"), DAY_30_RATE("day30Rate");

            private final String value;

            Row(String value) { this.value = value; }

            public String value() { return value; }
        }
    }

    public enum Csv implements Key {
        SECTION("section"), KEY("key"), VALUE("value"), SERIES_PREFIX("series["), TABLE_PREFIX("table.");

        private final String value;

        Csv(String value) { this.value = value; }

        public String value() { return value; }

        @JsonCreator
        public static Csv fromValue(String value) {
            for (Csv key : values()) if (key.value.equals(value)) return key;
            throw new IllegalArgumentException("Unknown analytics CSV key: " + value);
        }
    }

    public enum Historical implements Key {
        REGISTRATIONS("historical.registrations"), FEED_POSTS("historical.feed_posts"),
        FORMATIONS("historical.formations"), ENROLLMENTS("historical.enrollments"),
        REVIEWS("historical.reviews"), REPORTS("historical.reports"), PAYMENTS("historical.payments");

        private final String value;

        Historical(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum EventRollup implements Key {
        PREFIX("event."), SEPARATOR("\u0000");

        private final String value;

        EventRollup(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum Payload implements Key {
        EVENT_ID("eventId"), EVENT_TYPE("eventType"), EVENT_TIME("eventTime"),
        ACTOR_ID("actorId"), SUBJECT_ID("subjectId"), METADATA("metadata");

        private final String value;

        Payload(String value) { this.value = value; }

        public String value() { return value; }
    }
}
