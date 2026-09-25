package com.project.souklab.model;

import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/** Typed notification taxonomy grouped by the domain that produced it. */
public final class NotificationType {
    private NotificationType() { }

    @JsonDeserialize(using = NotificationTypeValueDeserializer.class)
    public interface Key extends EnumValue { }

    public static List<Key> all() {
        return List.of(
                Account.VALIDATED, Account.REJECTED, Account.SUSPENDED, Account.REINSTATED,
                Formation.APPROVED, Formation.REJECTED, Formation.NEW,
                Message.NEW, Subscription.RENEWED, Subscription.EXPIRED,
                Subscription.Renewal.REMINDER, Subscription.Grant.MANUAL, Subscription.REVOKED,
                Payment.SUCCESS, Payment.FAILED, Checkout.CREATED, Checkout.CANCELED,
                Refund.Request.UNAVAILABLE, Report.NEW, Review.NEW,
                Formateur.Request.SUBMITTED, Formateur.APPROVED, Formateur.GRANTED,
                Formateur.REJECTED, Formateur.REVOKED,
                Feed.SUBMITTED, Feed.PUBLISHED, Feed.REJECTED, Feed.HIDDEN,
                Feed.POST_LIKED, Feed.POST_COMMENTED, Feed.COMMENT_LIKED, Feed.COMMENT_REPLIED);
    }

    public static Key fromValue(String value) {
        return all().stream()
                .filter(type -> type.value().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported notification type: " + value));
    }

    public enum Account implements Key {
        VALIDATED("ACCOUNT_VALIDATED"), REJECTED("ACCOUNT_REJECTED"),
        SUSPENDED("ACCOUNT_SUSPENDED"), REINSTATED("ACCOUNT_REINSTATED");
        private final String value;
        Account(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Formation implements Key {
        APPROVED("FORMATION_APPROVED"), REJECTED("FORMATION_REJECTED"), NEW("NEW_FORMATION");
        private final String value;
        Formation(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Message implements Key {
        NEW("NEW_MESSAGE");
        private final String value;
        Message(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Subscription implements Key {
        RENEWED("SUBSCRIPTION_RENEWED"), EXPIRED("SUBSCRIPTION_EXPIRED"),
        REVOKED("SUBSCRIPTION_REVOKED");
        private final String value;
        Subscription(String value) { this.value = value; }
        public String value() { return value; }

        public enum Renewal implements Key {
            REMINDER("SUBSCRIPTION_RENEWAL_REMINDER");
            private final String value;
            Renewal(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Grant implements Key {
            MANUAL("SUBSCRIPTION_MANUALLY_GRANTED");
            private final String value;
            Grant(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Payment implements Key {
        SUCCESS("PAYMENT_SUCCESS"), FAILED("PAYMENT_FAILED");
        private final String value;
        Payment(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Checkout implements Key {
        CREATED("CHECKOUT_CREATED"), CANCELED("CHECKOUT_CANCELED");
        private final String value;
        Checkout(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Refund implements Key {
        ;
        private Refund() { }

        public enum Request implements Key {
            UNAVAILABLE("REFUND_REQUEST_UNAVAILABLE");
            private final String value;
            Request(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Report implements Key {
        NEW("NEW_REPORT");
        private final String value;
        Report(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Review implements Key {
        NEW("NEW_REVIEW");
        private final String value;
        Review(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Formateur implements Key {
        APPROVED("FORMATEUR_APPROVED"),
        GRANTED("FORMATEUR_GRANTED"), REJECTED("FORMATEUR_REJECTED"), REVOKED("FORMATEUR_REVOKED");
        private final String value;
        Formateur(String value) { this.value = value; }
        public String value() { return value; }

        public enum Request implements Key {
            SUBMITTED("FORMATEUR_REQUEST_SUBMITTED");
            private final String value;
            Request(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Feed implements Key {
        SUBMITTED("FEED_POST_SUBMITTED"),
        PUBLISHED("FEED_POST_PUBLISHED"),
        REJECTED("FEED_POST_REJECTED"),
        HIDDEN("FEED_POST_HIDDEN"),
        POST_LIKED("FEED_POST_LIKED"),
        POST_COMMENTED("FEED_POST_COMMENTED"),
        COMMENT_LIKED("FEED_COMMENT_LIKED"),
        COMMENT_REPLIED("FEED_COMMENT_REPLIED");

        private final String value;

        Feed(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
