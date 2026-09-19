package com.project.souklab.model;

import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/** Typed audit action taxonomy grouped by the domain that produced the action. */
public final class AuditLogAction {
    private AuditLogAction() { }

    @JsonDeserialize(using = AuditLogActionValueDeserializer.class)
    public interface Key extends EnumValue { }

    public static List<Key> all() {
        return List.of(
                Authentication.EMAIL_VERIFIED, Authentication.PASSWORD_RESET_COMPLETED,
                Authentication.PASSWORD_CHANGED, Permission.ASSIGN_ROLE, Permission.ASSIGN_BULK,
                Permission.GRANTED, Permission.REVOKED, User.APPROVED, User.BANNED,
                User.TIMED_OUT, User.UNBANNED, Artisan.APPROVED, Artisan.REJECTED,
                Formation.APPROVED, Formation.REJECTED, Report.RESOLVED, Report.DISMISSED,
                Subscription.GRANTED, Subscription.CANCELED, Subscription.REVOKED,
                Subscription.STATE_CORRECTED, Subscription.PLAN_CREATED, Subscription.PLAN_UPDATED,
                Subscription.PLAN_DEACTIVATED, Payment.STATE_CORRECTED, Refund.REQUEST_REJECTED,
                Analytics.REBUILD, Analytics.JOB_SUBMITTED, Analytics.RESULT_READ, Analytics.EXPORT);
    }

    public static Key fromValue(String value) {
        return all().stream()
                .filter(action -> action.value().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported audit action: " + value));
    }

    public enum Authentication implements Key {
        EMAIL_VERIFIED("EMAIL_VERIFIED"), PASSWORD_RESET_COMPLETED("PASSWORD_RESET_COMPLETED"),
        PASSWORD_CHANGED("PASSWORD_CHANGED");
        private final String value;
        Authentication(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Permission implements Key {
        ASSIGN_ROLE("ASSIGN_ROLE"), ASSIGN_BULK("ASSIGN_PERMISSION_BULK"),
        GRANTED("PERMISSION_GRANTED"), REVOKED("PERMISSION_REVOKED");
        private final String value;
        Permission(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum User implements Key {
        APPROVED("APPROVE_USER"), BANNED("BAN_USER"), TIMED_OUT("TIMEOUT_USER"),
        UNBANNED("UNBAN_USER");
        private final String value;
        User(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Artisan implements Key {
        APPROVED("APPROVE_ARTISAN"), REJECTED("REJECT_ARTISAN");
        private final String value;
        Artisan(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Formation implements Key {
        APPROVED("APPROVE_FORMATION"), REJECTED("REJECT_FORMATION");
        private final String value;
        Formation(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Report implements Key {
        RESOLVED("RESOLVE_REPORT"), DISMISSED("DISMISS_REPORT");
        private final String value;
        Report(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Subscription implements Key {
        GRANTED("SUBSCRIPTION_GRANTED"), CANCELED("SUBSCRIPTION_CANCELED"),
        REVOKED("SUBSCRIPTION_REVOKED"), STATE_CORRECTED("SUBSCRIPTION_STATE_CORRECTED"),
        PLAN_CREATED("SUBSCRIPTION_PLAN_CREATED"), PLAN_UPDATED("SUBSCRIPTION_PLAN_UPDATED"),
        PLAN_DEACTIVATED("SUBSCRIPTION_PLAN_DEACTIVATED");
        private final String value;
        Subscription(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Payment implements Key {
        STATE_CORRECTED("PAYMENT_STATE_CORRECTED");
        private final String value;
        Payment(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Refund implements Key {
        REQUEST_REJECTED("REFUND_REQUEST_REJECTED");
        private final String value;
        Refund(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Analytics implements Key {
        REBUILD("ANALYTICS_REBUILD"), JOB_SUBMITTED("ANALYTICS_JOB_SUBMITTED"),
        RESULT_READ("ANALYTICS_RESULT_READ"), EXPORT("ANALYTICS_EXPORT");
        private final String value;
        Analytics(String value) { this.value = value; }
        public String value() { return value; }
    }
}
