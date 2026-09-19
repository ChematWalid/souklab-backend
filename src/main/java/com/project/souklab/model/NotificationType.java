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
                Subscription.RENEWAL_REMINDER, Subscription.MANUALLY_GRANTED, Subscription.REVOKED,
                Payment.SUCCESS, Payment.FAILED, Checkout.CREATED, Checkout.CANCELED,
                Refund.REQUEST_UNAVAILABLE, Report.NEW, Review.NEW,
                Formateur.REQUEST_SUBMITTED, Formateur.APPROVED, Formateur.GRANTED,
                Formateur.REJECTED, Formateur.REVOKED);
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
        RENEWAL_REMINDER("SUBSCRIPTION_RENEWAL_REMINDER"),
        MANUALLY_GRANTED("SUBSCRIPTION_MANUALLY_GRANTED"), REVOKED("SUBSCRIPTION_REVOKED");
        private final String value;
        Subscription(String value) { this.value = value; }
        public String value() { return value; }
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
        REQUEST_UNAVAILABLE("REFUND_REQUEST_UNAVAILABLE");
        private final String value;
        Refund(String value) { this.value = value; }
        public String value() { return value; }
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
        REQUEST_SUBMITTED("FORMATEUR_REQUEST_SUBMITTED"), APPROVED("FORMATEUR_APPROVED"),
        GRANTED("FORMATEUR_GRANTED"), REJECTED("FORMATEUR_REJECTED"), REVOKED("FORMATEUR_REVOKED");
        private final String value;
        Formateur(String value) { this.value = value; }
        public String value() { return value; }
    }
}
