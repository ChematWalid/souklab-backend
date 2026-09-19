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
                    User.all().toArray(Key[]::new), Engagement.all().toArray(Key[]::new), Content.all().toArray(Key[]::new),
                    Formation.all().toArray(Key[]::new), Moderation.all().toArray(Key[]::new), Report.all().toArray(Key[]::new),
                    Payment.all().toArray(Key[]::new), Subscription.all().toArray(Key[]::new), General.all().toArray(Key[]::new))
                    .flatMap(Arrays::stream)
                    .map(key -> (Key) key)
                    .toList();
        }

        public enum User implements Key {
            ;

            private User() { }

            private static List<Key> all() {
                return Stream.of(Count.values(), Registration.values(), Activation.values(), Verification.values(),
                                Profile.values(), Status.values(), Approval.values())
                        .flatMap(Arrays::stream)
                        .map(key -> (Key) key)
                        .toList();
            }

            public enum Count implements Key {
                TOTAL("totalUsers");
                private final String value;
                Count(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Registration implements Key {
                NEW("newRegistrations"), VERIFIED("verifiedRegistrations");
                private final String value;
                Registration(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Activation implements Key {
                RATE("activationRate");
                private final String value;
                Activation(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Verification implements Key {
                USERS("verifiedUsers");
                private final String value;
                Verification(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Profile implements Key {
                ARTISAN("artisanProfiles"), CLIENT("clientProfiles");
                private final String value;
                Profile(String value) { this.value = value; }
                public String value() { return value; }

                public enum Active implements Key {
                    ARTISAN("activeArtisanProfiles"), CLIENT("activeClientProfiles");
                    private final String value;
                    Active(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }

            public enum Status implements Key {
                ACTIVE("activeUsers"), PENDING("pendingUsers"), SUSPENDED("suspendedUsers"),
                ALL("userStatuses");
                private final String value;
                Status(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Approval implements Key {
                PENDING("pendingUserApprovals");
                private final String value;
                Approval(String value) { this.value = value; }
                public String value() { return value; }
            }
        }

        public enum Engagement implements Key {
            ;

            private Engagement() { }

            private static List<Key> all() {
                return Stream.of(Activity.values(), Login.values(), Post.values(), Message.values(),
                                Profile.values(), Report.values(), Audience.values(), Dimension.Account.values(),
                                Dimension.Region.values(), Dimension.Craft.values(), Retention.Login.values())
                        .flatMap(Arrays::stream)
                        .map(key -> (Key) key)
                        .toList();
            }

            public enum Activity implements Key {
                EVENTS("activityEvents");
                private final String value;
                Activity(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Login implements Key {
                SUCCESSFUL("successfulLogins");
                private final String value;
                Login(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Post implements Key {
                PUBLISHED("publishedPosts");
                private final String value;
                Post(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Message implements Key {
                SENT("messagesSent");
                private final String value;
                Message(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Profile implements Key {
                VIEWS("profileViews");
                private final String value;
                Profile(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Report implements Key {
                RESOLUTIONS("reportResolutions");
                private final String value;
                Report(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Audience implements Key {
                DAU("dau"), WAU("wau"), MAU("mau");
                private final String value;
                Audience(String value) { this.value = value; }
                public String value() { return value; }
            }

            public static final class Dimension {
                private Dimension() { }

                public enum Account implements Key {
                    TYPE("engagementByAccountType");
                    private final String value;
                    Account(String value) { this.value = value; }
                    public String value() { return value; }
                }

                public enum Region implements Key {
                    VALUE("engagementByRegion");
                    private final String value;
                    Region(String value) { this.value = value; }
                    public String value() { return value; }
                }

                public enum Craft implements Key {
                    CATEGORY("engagementByCraftCategory");
                    private final String value;
                    Craft(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }

            public enum Retention implements Key {
                ;

                private Retention() { }

                public enum Login implements Key {
                    COHORTS("loginRetentionCohorts");
                    private final String value;
                    Login(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }
        }

        public enum Content implements Key {
            ;

            private Content() { }

            private static List<Key> all() {
                return Stream.of(Feed.values(), Review.values())
                        .flatMap(Arrays::stream).map(key -> (Key) key).toList();
            }

            public enum Feed implements Key {
                CREATED("feedPostsCreated"), BY_STATUS("feedPostsByStatus");
                private final String value;
                Feed(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Review implements Key {
                SUBMITTED("reviewsSubmitted"), PUBLISHED("publishedReviews"), AVERAGE_RATING("averagePublishedRating");
                private final String value;
                Review(String value) { this.value = value; }
                public String value() { return value; }
            }
        }

        public enum Formation implements Key {
            ;

            private Formation() { }

            private static List<Key> all() {
                return Stream.of(Count.values(), Instructor.values(), Utilization.values(), Status.By.values(), Enrollment.Cancellation.values(), Enrollment.By.values())
                        .flatMap(Arrays::stream).map(key -> (Key) key).toList();
            }

            public enum Count implements Key {
                CREATED("formationsCreated"), ENROLLMENTS("formationEnrollments"), COMPLETIONS("formationCompletions");
                private final String value;
                Count(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Instructor implements Key {
                ACTIVE("activeInstructors");
                private final String value;
                Instructor(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Utilization implements Key {
                RATE("formationUtilizationRate");
                private final String value;
                Utilization(String value) { this.value = value; }
                public String value() { return value; }
            }

            public static final class Status {
                private Status() { }

                public enum By implements Key {
                    STATUS("formationsByStatus");
                    private final String value;
                    By(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }

            public static final class Enrollment {
                private Enrollment() { }

                public enum Cancellation implements Key {
                    RATE("enrollmentCancellationRate");
                    private final String value;
                    Cancellation(String value) { this.value = value; }
                    public String value() { return value; }
                }

                public enum By implements Key {
                    STATUS("enrollmentsByStatus");
                    private final String value;
                    By(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }
        }

        public enum Moderation implements Key {
            ;

            private Moderation() { }

            private static List<Key> all() {
                return Stream.of(Activity.values(), Formateur.values(), Formateur.Approved.values(),
                        Formateur.Rejected.values(), Formateur.Statuses.values())
                        .flatMap(Arrays::stream).map(key -> (Key) key).toList();
            }

            public enum Activity implements Key {
                SUMMARY("moderationActivity");
                private final String value;
                Activity(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Formateur implements Key {
                PENDING("formateurPending");
                private final String value;
                Formateur(String value) { this.value = value; }
                public String value() { return value; }

                public enum Approved implements Key {
                    IN_RANGE("formateurApprovedInRange");
                    private final String value;
                    Approved(String value) { this.value = value; }
                    public String value() { return value; }
                }

                public enum Rejected implements Key {
                    IN_RANGE("formateurRejectedInRange");
                    private final String value;
                    Rejected(String value) { this.value = value; }
                    public String value() { return value; }
                }

                public enum Statuses implements Key {
                    VALUE("formateurStatuses");
                    private final String value;
                    Statuses(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }
        }

        public enum Report implements Key {
            ;

            private Report() { }

            private static List<Key> all() {
                return Stream.of(Submission.values(), Resolution.Average.values(), Status.By.values())
                        .flatMap(Arrays::stream).map(key -> (Key) key).toList();
            }

            public enum Submission implements Key {
                COUNT("reportsSubmitted");
                private final String value;
                Submission(String value) { this.value = value; }
                public String value() { return value; }
            }

            public static final class Resolution {
                private Resolution() { }

                public enum Average implements Key {
                    SECONDS("averageReportResolutionSeconds");
                    private final String value;
                    Average(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }

            public static final class Status {
                private Status() { }

                public enum By implements Key {
                    STATUS("reportsByStatus");
                    private final String value;
                    By(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }
        }

        public enum Payment implements Key {
            ;

            private Payment() { }

            private static List<Key> all() {
                return Stream.of(Count.values(), Status.By.values(), Checkout.values(), State.values(),
                        Revenue.Gross.values(), Revenue.ProviderFees.values(), Revenue.Net.values(), Conversion.values(), Grant.values())
                        .flatMap(Arrays::stream).map(key -> (Key) key).toList();
            }

            public enum Count implements Key {
                CREATED("paymentsCreated");
                private final String value;
                Count(String value) { this.value = value; }
                public String value() { return value; }
            }

            public static final class Status {
                private Status() { }

                public enum By implements Key {
                    STATUS("paymentsByStatus");
                    private final String value;
                    By(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }

            public enum Checkout implements Key {
                CREATED("checkoutCreated");
                private final String value;
                Checkout(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum State implements Key {
                TRANSITIONS("paymentStateTransitions");
                private final String value;
                State(String value) { this.value = value; }
                public String value() { return value; }
            }

            public static final class Revenue {
                private Revenue() { }

                public enum Gross implements Key {
                    COLLECTED_DZD("grossCollectedDzd");
                    private final String value;
                    Gross(String value) { this.value = value; }
                    public String value() { return value; }
                }

                public enum ProviderFees implements Key {
                    DZD("providerFeesDzd");
                    private final String value;
                    ProviderFees(String value) { this.value = value; }
                    public String value() { return value; }
                }

                public enum Net implements Key {
                    COLLECTED_DZD("netCollectedDzd");
                    private final String value;
                    Net(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }

            public enum Conversion implements Key {
                RATE("paymentConversionRate");
                private final String value;
                Conversion(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Grant implements Key {
                MANUAL("manualGrants");
                private final String value;
                Grant(String value) { this.value = value; }
                public String value() { return value; }
            }
        }

        public enum Subscription implements Key {
            ;

            private Subscription() { }

            private static List<Key> all() {
                return Stream.of(Status.By.values(), Subscriber.values(), Lifecycle.values())
                        .flatMap(Arrays::stream).map(key -> (Key) key).toList();
            }

            public static final class Status {
                private Status() { }

                public enum By implements Key {
                    STATUS("subscriptionsByStatus");
                    private final String value;
                    By(String value) { this.value = value; }
                    public String value() { return value; }
                }
            }

            public enum Subscriber implements Key {
                TYPE("subscriptionsBySubscriberType");
                private final String value;
                Subscriber(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Lifecycle implements Key {
                EVENTS("subscriptionLifecycleEvents");
                private final String value;
                Lifecycle(String value) { this.value = value; }
                public String value() { return value; }
            }
        }

        public enum General implements Key {
            ;

            private General() { }

            private static List<Key> all() {
                return Stream.of(Operational.values(), Period.values())
                        .flatMap(Arrays::stream).map(key -> (Key) key).toList();
            }

            public enum Operational implements Key {
                REPORT("operational");
                private final String value;
                Operational(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Period implements Key {
                COMPARISON("periodComparison");
                private final String value;
                Period(String value) { this.value = value; }
                public String value() { return value; }
            }
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

        public static final class Dependency {
            private Dependency() { }

            public enum Elasticsearch implements Key {
                VALUE("elasticsearch");
                private final String value;
                Elasticsearch(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Redis implements Key {
                VALUE("redis");
                private final String value;
                Redis(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum RabbitMq implements Key {
                VALUE("rabbitmq");
                private final String value;
                RabbitMq(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum ObjectStorage implements Key {
                VALUE("s3");
                private final String value;
                ObjectStorage(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Clamav implements Key {
                VALUE("clamav");
                private final String value;
                Clamav(String value) { this.value = value; }
                public String value() { return value; }
            }
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
                    case ONE -> Row.Day.Retained.ONE;
                    case SEVEN -> Row.Day.Retained.SEVEN;
                    case THIRTY -> Row.Day.Retained.THIRTY;
                };
            }

            public Key rateRow() {
                return switch (this) {
                    case ONE -> Row.Day.Rate.ONE;
                    case SEVEN -> Row.Day.Rate.SEVEN;
                    case THIRTY -> Row.Day.Rate.THIRTY;
                };
            }
        }

        public static final class Row {
            private Row() { }

            public enum Cohort implements Key {
                DATE("cohortDate"), SIZE("cohortSize");
                private final String value;
                Cohort(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Day implements Key {
                ;
                private Day() { }

                public enum Retained implements Key {
                    ONE("day1Retained"), SEVEN("day7Retained"), THIRTY("day30Retained");
                    private final String value;
                    Retained(String value) { this.value = value; }
                    public String value() { return value; }
                }

                public enum Rate implements Key {
                    ONE("day1Rate"), SEVEN("day7Rate"), THIRTY("day30Rate");
                    private final String value;
                    Rate(String value) { this.value = value; }
                    public String value() { return value; }
                }
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

    public static final class Historical {
        private Historical() { }

        public enum Registration implements Key {
            COUNT("historical.registrations");
            private final String value;
            Registration(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Feed implements Key {
            POSTS("historical.feed_posts");
            private final String value;
            Feed(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Formation implements Key {
            COUNT("historical.formations");
            private final String value;
            Formation(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Enrollment implements Key {
            COUNT("historical.enrollments");
            private final String value;
            Enrollment(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Review implements Key {
            COUNT("historical.reviews");
            private final String value;
            Review(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Report implements Key {
            COUNT("historical.reports");
            private final String value;
            Report(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Payment implements Key {
            COUNT("historical.payments");
            private final String value;
            Payment(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum EventRollup implements Key {
        PREFIX("event."), SEPARATOR("\u0000");

        private final String value;

        EventRollup(String value) { this.value = value; }

        public String value() { return value; }
    }

    public static final class Payload {
        private Payload() { }

        public enum Event implements Key {
            ID("eventId"), TYPE("eventType"), TIME("eventTime");
            private final String value;
            Event(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Actor implements Key {
            ID("actorId");
            private final String value;
            Actor(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Subject implements Key {
            ID("subjectId");
            private final String value;
            Subject(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Metadata implements Key {
            VALUE("metadata");
            private final String value;
            Metadata(String value) { this.value = value; }
            public String value() { return value; }
        }
    }
}
