package com.project.souklab.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.project.souklab.model.EnumValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
/**
 * Stable keys used by the analytics result and rollup contracts.
 *
 * <p>The enum names provide a typed vocabulary inside the application while
 * {@link Key#value()} preserves the existing JSON and database key values.</p>
 */
public final class AnalyticsMetric {
    private AnalyticsMetric() { }

    public interface Key extends EnumValue { }

    public static final class Summary {
        private Summary() { }

        public static Key fromValue(String value) {
            for (Key key : all()) {
                if (key.value().equals(value)) return key;
            }
            throw new IllegalArgumentException("Unknown analytics summary key: " + value);
        }

        private static List<Key> all() {
            return Stream.of(
                    User.values(), Engagement.values(), Content.values(), Formation.values(),
                    Moderation.values(), Report.values(), Payment.values(), Subscription.values(), General.values())
                    .flatMap(Arrays::stream)
                    .map(key -> (Key) key)
                    .toList();
        }

        public enum User implements Key {
            TOTAL("totalUsers"), NEW_REGISTRATIONS("newRegistrations"),
            VERIFIED_REGISTRATIONS("verifiedRegistrations"), ACTIVATION_RATE("activationRate"),
            VERIFIED("verifiedUsers"), ARTISAN_PROFILES("artisanProfiles"), CLIENT_PROFILES("clientProfiles"),
            ACTIVE_ARTISAN_PROFILES("activeArtisanProfiles"), ACTIVE_CLIENT_PROFILES("activeClientProfiles"),
            ACTIVE("activeUsers"), PENDING("pendingUsers"), SUSPENDED("suspendedUsers"),
            PENDING_APPROVALS("pendingUserApprovals"), STATUSES("userStatuses");

            private final String value;
            User(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Engagement implements Key {
            ACTIVITY_EVENTS("activityEvents"), SUCCESSFUL_LOGINS("successfulLogins"),
            PUBLISHED_POSTS("publishedPosts"), MESSAGES_SENT("messagesSent"), PROFILE_VIEWS("profileViews"),
            REPORT_RESOLUTIONS("reportResolutions"), DAU("dau"), WAU("wau"), MAU("mau"),
            BY_ACCOUNT_TYPE("engagementByAccountType"), BY_REGION("engagementByRegion"),
            BY_CRAFT_CATEGORY("engagementByCraftCategory"), LOGIN_RETENTION_COHORTS("loginRetentionCohorts");

            private final String value;
            Engagement(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Content implements Key {
            FEED_POSTS_CREATED("feedPostsCreated"), FEED_POSTS_BY_STATUS("feedPostsByStatus"),
            REVIEWS_SUBMITTED("reviewsSubmitted"), PUBLISHED_REVIEWS("publishedReviews"),
            AVERAGE_PUBLISHED_RATING("averagePublishedRating");

            private final String value;
            Content(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Formation implements Key {
            CREATED("formationsCreated"), ENROLLMENTS("formationEnrollments"), ACTIVE_INSTRUCTORS("activeInstructors"),
            UTILIZATION_RATE("formationUtilizationRate"), BY_STATUS("formationsByStatus"),
            COMPLETIONS("formationCompletions"), ENROLLMENT_CANCELLATION_RATE("enrollmentCancellationRate"),
            ENROLLMENTS_BY_STATUS("enrollmentsByStatus");

            private final String value;
            Formation(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Moderation implements Key {
            ACTIVITY("moderationActivity"), FORMATEUR_PENDING("formateurPending"),
            FORMATEUR_APPROVED_IN_RANGE("formateurApprovedInRange"), FORMATEUR_REJECTED_IN_RANGE("formateurRejectedInRange"),
            FORMATEUR_STATUSES("formateurStatuses");

            private final String value;
            Moderation(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Report implements Key {
            SUBMITTED("reportsSubmitted"), AVERAGE_RESOLUTION_SECONDS("averageReportResolutionSeconds"),
            BY_STATUS("reportsByStatus");

            private final String value;
            Report(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Payment implements Key {
            CREATED("paymentsCreated"), BY_STATUS("paymentsByStatus"), CHECKOUT_CREATED("checkoutCreated"),
            STATE_TRANSITIONS("paymentStateTransitions"), GROSS_COLLECTED_DZD("grossCollectedDzd"),
            PROVIDER_FEES_DZD("providerFeesDzd"), NET_COLLECTED_DZD("netCollectedDzd"),
            CONVERSION_RATE("paymentConversionRate"), MANUAL_GRANTS("manualGrants");

            private final String value;
            Payment(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Subscription implements Key {
            BY_STATUS("subscriptionsByStatus"), BY_SUBSCRIBER_TYPE("subscriptionsBySubscriberType"),
            LIFECYCLE_EVENTS("subscriptionLifecycleEvents");

            private final String value;
            Subscription(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum General implements Key {
            OPERATIONAL("operational"), PERIOD_COMPARISON("periodComparison");

            private final String value;
            General(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public static final class Result {
        private Result() { }

        public interface Key extends AnalyticsMetric.Key { }

        public static Key fromValue(String value) {
            for (Key key : all()) {
                if (key.value().equals(value)) return key;
            }
            throw new IllegalArgumentException("Unknown analytics result key: " + value);
        }

        private static List<Key> all() {
            return Stream.of(Report.values(), Date.values(), Request.values(), Content.values())
                    .flatMap(Arrays::stream)
                    .map(key -> (Key) key)
                    .toList();
        }

        public enum Report implements Key {
            TYPE("reportType");
            private final String value;
            Report(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Date implements Key {
            FROM("fromDate"), TO("toDate");
            private final String value;
            Date(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Request implements Key {
            BUCKET("bucket");
            private final String value;
            Request(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Content implements Key {
            SUMMARY("summary"), TABLES("tables"), SERIES("series"), CONTENT("content");
            private final String value;
            Content(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public static final class Series {
        private Series() { }

        public interface Key extends AnalyticsMetric.Key { }

        public static Key fromValue(String value) {
            for (Key key : all()) {
                if (key.value().equals(value)) return key;
            }
            throw new IllegalArgumentException("Unknown analytics series key: " + value);
        }

        private static List<Key> all() {
            return Stream.of(Date.values(), Activity.values(), Actor.values(), Registration.values())
                    .flatMap(Arrays::stream)
                    .map(key -> (Key) key)
                    .toList();
        }

        public enum Date implements Key {
            START("startDate"), END("endDate");
            private final String value;
            Date(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Activity implements Key {
            EVENTS("activityEvents");
            private final String value;
            Activity(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Actor implements Key {
            UNIQUE("uniqueActors");
            private final String value;
            Actor(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Registration implements Key {
            NEW("newRegistrations");
            private final String value;
            Registration(String value) { this.value = value; }
            public String value() { return value; }
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

    public static final class Table {
        private Table() { }

        public static Key fromValue(String value) {
            for (Key key : all()) {
                if (key.value().equals(value)) return key;
            }
            throw new IllegalArgumentException("Unknown analytics table key: " + value);
        }

        private static List<Key> all() {
            return Stream.of(User.values(), Formateur.values(), Report.values(), Feed.values(), Formation.values(),
                            Enrollment.values(), Payment.values(), Subscription.values(), Activity.values())
                    .flatMap(Arrays::stream)
                    .map(key -> (Key) key)
                    .toList();
        }

        public enum User implements Key {
            USERS("users");
            private final String value;
            User(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Formateur implements Key {
            REQUESTS("formateurRequests");
            private final String value;
            Formateur(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Report implements Key {
            REPORTS("reports");
            private final String value;
            Report(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Feed implements Key {
            POSTS("feedPosts");
            private final String value;
            Feed(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Formation implements Key {
            FORMATIONS("formations");
            private final String value;
            Formation(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Enrollment implements Key {
            ENROLLMENTS("enrollments");
            private final String value;
            Enrollment(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Payment implements Key {
            PAYMENTS("payments");
            private final String value;
            Payment(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Subscription implements Key {
            ALL("subscriptions"), ARTISAN("subscriptionsArtisan"), CLIENT("subscriptionsClient");
            private final String value;
            Subscription(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Activity implements Key {
            EVENTS("activity");
            private final String value;
            Activity(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public static final class Operational {
        private Operational() { }

        public static final class Job {
            private Job() { }

            public enum Analytics implements Key {
                QUEUED("analyticsJobsQueued"), RUNNING("analyticsJobsRunning"),
                COMPLETED("analyticsJobsCompleted"), FAILED("analyticsJobsFailed");

                private final String value;

                Analytics(String value) { this.value = value; }

                public String value() { return value; }
            }

            public enum Maintenance implements Key {
                QUEUED("maintenanceJobsQueued"), RUNNING("maintenanceJobsRunning"),
                COMPLETED("maintenanceJobsCompleted"), FAILED("maintenanceJobsFailed");

                private final String value;

                Maintenance(String value) { this.value = value; }

                public String value() { return value; }
            }
        }

        public enum Outbox implements Key {
            PENDING("outboxPending"), PUBLISHED("outboxPublished"), DEAD_LETTER("outboxDeadLetter");

            private final String value;

            Outbox(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Health implements Key {
            APPLICATION("applicationHealth"), COMPONENTS("healthComponents");

            private final String value;

            Health(String value) { this.value = value; }

            public String value() { return value; }
        }

        public enum Request implements Key {
            COUNTERS("requestCounters"), RATE_LIMIT_REJECTIONS("rateLimitRejections");

            private final String value;

            Request(String value) { this.value = value; }

            public String value() { return value; }
        }

        public static final class Metric {
            private Metric() { }

            public enum Upload implements Key {
                COUNTERS("souklab.uploads");

                private final String value;

                Upload(String value) { this.value = value; }

                public String value() { return value; }
            }

            public enum Virus implements Key {
                SCANS("souklab.virus.scans");

                private final String value;

                Virus(String value) { this.value = value; }

                public String value() { return value; }
            }

            public enum Search implements Key {
                REQUESTS("souklab.search.requests");

                private final String value;

                Search(String value) { this.value = value; }

                public String value() { return value; }
            }

            public enum WebSocket implements Key {
                CONNECTIONS("souklab.websocket.connections");

                private final String value;

                WebSocket(String value) { this.value = value; }

                public String value() { return value; }
            }

            public enum Request implements Key {
                COUNTERS("souklab.http.requests");

                private final String value;

                Request(String value) { this.value = value; }

                public String value() { return value; }
            }

            public enum RateLimit implements Key {
                REJECTIONS("souklab.rate_limit.rejections");

                private final String value;

                RateLimit(String value) { this.value = value; }

                public String value() { return value; }
            }
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
            ERROR("error"), INFECTED("infected"), CLEAN("clean");

            private final String value;

            Outcome(String value) { this.value = value; }

            public String value() { return value; }

            public enum Error implements Key {
                ALLOWED("error_allowed"), REJECTED("error_rejected");

                private final String value;

                Error(String value) { this.value = value; }

                public String value() { return value; }
            }
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

        public static final class RequestOutcome {
            private RequestOutcome() { }

            public enum Status implements Key {
                SUCCESS("2xx"), REDIRECT("3xx"), CLIENT_ERROR("4xx"),
                SERVER_ERROR("5xx"), ERROR("error");

                private final String value;

                Status(String value) { this.value = value; }

                public String value() { return value; }

                public static Status fromStatus(int status) {
                    if (status >= 500) return SERVER_ERROR;
                    if (status >= 400) return CLIENT_ERROR;
                    if (status >= 300) return REDIRECT;
                    return SUCCESS;
                }
            }
        }
    }

    public static final class Retention {
        private Retention() { }

        public enum Day implements Key {
            ONE("day1"), SEVEN("day7"), THIRTY("day30");

            private final String value;

            Day(String value) { this.value = value; }

            public String value() { return value; }

            public Key retainedRow() {
                return switch (this) {
                    case ONE -> Row.Day.ONE_RETAINED;
                    case SEVEN -> Row.Day.SEVEN_RETAINED;
                    case THIRTY -> Row.Day.THIRTY_RETAINED;
                };
            }

            public Key rateRow() {
                return switch (this) {
                    case ONE -> Row.Day.ONE_RATE;
                    case SEVEN -> Row.Day.SEVEN_RATE;
                    case THIRTY -> Row.Day.THIRTY_RATE;
                };
            }
        }

        public enum Row implements Key {
            COHORT_DATE("cohortDate"), COHORT_SIZE("cohortSize");

            private final String value;

            Row(String value) { this.value = value; }

            public String value() { return value; }

            public enum Day implements Key {
                ONE_RETAINED("day1Retained"), ONE_RATE("day1Rate"),
                SEVEN_RETAINED("day7Retained"), SEVEN_RATE("day7Rate"),
                THIRTY_RETAINED("day30Retained"), THIRTY_RATE("day30Rate");

                private final String value;

                Day(String value) { this.value = value; }

                public String value() { return value; }
            }
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
