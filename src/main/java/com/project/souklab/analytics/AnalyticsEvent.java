package com.project.souklab.analytics;

import java.util.List;
import java.util.Optional;

/**
 * Canonical activity-event taxonomy. Nested enums keep related event names
 * discoverable while {@link #value()} preserves the stable persisted value.
 */
public final class AnalyticsEvent {
    private AnalyticsEvent() { }

    public interface Type {
        String value();
    }

    public enum Source {
        PAYMENT_WEBHOOK("PAYMENT_WEBHOOK"),
        ACCOUNT_ACTION("ACCOUNT_ACTION"),
        ADMIN_ACTION("ADMIN_ACTION"),
        ADMIN_CORRECTION("ADMIN_CORRECTION");

        private final String value;

        Source(String value) { this.value = value; }

        public String value() { return value; }
    }

    public static List<Type> all() {
        return List.of(
                Registration.CREATED, Authentication.LOGIN_SUCCEEDED,
                User.APPROVED, User.SUSPENDED, User.TIMED_OUT, User.REINSTATED,
                Profile.VIEW, Message.SENT,
                Formation.SUBMITTED, Formation.MODERATION_APPROVED, Formation.MODERATION_REJECTED,
                Formation.PUBLISHED, Formation.ENROLLMENT,
                Review.SUBMITTED, Report.SUBMITTED, Report.RESOLVED,
                Feed.POST_PUBLISHED, Checkout.CREATED, Payment.STATE_TRANSITION,
                Subscription.ACTIVATED, Subscription.EXPIRED, Subscription.CANCELED,
                Subscription.REVOKED, Subscription.RENEWAL);
    }

    public static Optional<Type> fromValue(String value) {
        return all().stream().filter(type -> type.value().equals(value)).findFirst();
    }

    public enum Registration implements Type {
        CREATED("REGISTRATION_CREATED");
        private final String value;
        Registration(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Authentication implements Type {
        LOGIN_SUCCEEDED("LOGIN_SUCCEEDED");
        private final String value;
        Authentication(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum User implements Type {
        APPROVED("USER_APPROVED"), SUSPENDED("USER_SUSPENDED"),
        TIMED_OUT("USER_TIMED_OUT"), REINSTATED("USER_REINSTATED");
        private final String value;
        User(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Profile implements Type {
        VIEW("PROFILE_VIEW");
        private final String value;
        Profile(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Message implements Type {
        SENT("MESSAGE_SENT");
        private final String value;
        Message(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Formation implements Type {
        SUBMITTED("FORMATION_SUBMITTED"),
        MODERATION_APPROVED("FORMATION_MODERATION_APPROVED"),
        MODERATION_REJECTED("FORMATION_MODERATION_REJECTED"),
        PUBLISHED("FORMATION_PUBLISHED"), ENROLLMENT("FORMATION_ENROLLMENT");
        private final String value;
        Formation(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Review implements Type {
        SUBMITTED("REVIEW_SUBMITTED");
        private final String value;
        Review(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Report implements Type {
        SUBMITTED("REPORT_SUBMITTED"), RESOLVED("REPORT_RESOLVED");
        private final String value;
        Report(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Feed implements Type {
        POST_PUBLISHED("FEED_POST_PUBLISHED");
        private final String value;
        Feed(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Checkout implements Type {
        CREATED("CHECKOUT_CREATED");
        private final String value;
        Checkout(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Payment implements Type {
        STATE_TRANSITION("PAYMENT_STATE_TRANSITION");
        private final String value;
        Payment(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Subscription implements Type {
        ACTIVATED("SUBSCRIPTION_ACTIVATED"), EXPIRED("SUBSCRIPTION_EXPIRED"),
        CANCELED("SUBSCRIPTION_CANCELED"), REVOKED("SUBSCRIPTION_REVOKED"),
        RENEWAL("SUBSCRIPTION_RENEWAL");
        private final String value;
        Subscription(String value) { this.value = value; }
        public String value() { return value; }
        public static String prefix() { return "SUBSCRIPTION_"; }
    }
}
