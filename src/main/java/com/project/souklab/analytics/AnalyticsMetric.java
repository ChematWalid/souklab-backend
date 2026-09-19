package com.project.souklab.analytics;

/**
 * Stable keys used by the analytics result and rollup contracts.
 *
 * <p>The enum names provide a typed vocabulary inside the application while
 * {@link Key#value()} preserves the existing JSON and database key values.</p>
 */
public final class AnalyticsMetric {
    private AnalyticsMetric() { }

    public interface Key {
        String value();
    }

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
        MESSAGES_SENT("messagesSent"), DAU("dau"), WAU("wau"), MAU("mau"),
        ENGAGEMENT_BY_ACCOUNT_TYPE("engagementByAccountType"),
        LOGIN_RETENTION_COHORTS("loginRetentionCohorts"), FEED_POSTS_CREATED("feedPostsCreated"),
        FORMATIONS_CREATED("formationsCreated"), FORMATION_ENROLLMENTS("formationEnrollments"),
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
    }

    public enum Result implements Key {
        REPORT_TYPE("reportType"), FROM_DATE("fromDate"), TO_DATE("toDate"), BUCKET("bucket"),
        SUMMARY("summary"), TABLES("tables"), SERIES("series"), CONTENT("content");

        private final String value;

        Result(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum Series implements Key {
        START_DATE("startDate"), END_DATE("endDate"), ACTIVITY_EVENTS("activityEvents"),
        UNIQUE_ACTORS("uniqueActors"), NEW_REGISTRATIONS("newRegistrations");

        private final String value;

        Series(String value) { this.value = value; }

        public String value() { return value; }
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
    }

    public enum Operational implements Key {
        ANALYTICS_JOBS_QUEUED("analyticsJobsQueued"), ANALYTICS_JOBS_RUNNING("analyticsJobsRunning"),
        ANALYTICS_JOBS_COMPLETED("analyticsJobsCompleted"), ANALYTICS_JOBS_FAILED("analyticsJobsFailed"),
        MAINTENANCE_JOBS_QUEUED("maintenanceJobsQueued"), MAINTENANCE_JOBS_RUNNING("maintenanceJobsRunning"),
        MAINTENANCE_JOBS_COMPLETED("maintenanceJobsCompleted"), MAINTENANCE_JOBS_FAILED("maintenanceJobsFailed"),
        OUTBOX_PENDING("outboxPending"), OUTBOX_PUBLISHED("outboxPublished"), OUTBOX_DEAD_LETTER("outboxDeadLetter"),
        APPLICATION_HEALTH("applicationHealth"), HEALTH_COMPONENTS("healthComponents"),
        REQUEST_COUNTERS("requestCounters"), RATE_LIMIT_REJECTIONS("rateLimitRejections");

        private final String value;

        Operational(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum Retention implements Key {
        DAY_1("day1"), DAY_7("day7"), DAY_30("day30"),
        COHORT_DATE("cohortDate"), COHORT_SIZE("cohortSize"),
        RETAINED_SUFFIX("Retained"), RATE_SUFFIX("Rate");

        private final String value;

        Retention(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum Csv implements Key {
        SECTION("section"), KEY("key"), VALUE("value"), SERIES_PREFIX("series["), TABLE_PREFIX("table.");

        private final String value;

        Csv(String value) { this.value = value; }

        public String value() { return value; }
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
        PREFIX("event.");

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
