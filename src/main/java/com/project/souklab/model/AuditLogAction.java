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
                Authentication.Email.VERIFIED, Authentication.Password.Reset.COMPLETED,
                Authentication.Password.Changed.VALUE, Permission.Assignment.ROLE, Permission.Assignment.BULK,
                Permission.Grant.VALUE, Permission.Revoke.VALUE, User.APPROVED, User.BANNED,
                User.Timeout.VALUE, User.UNBANNED, Artisan.APPROVED, Artisan.REJECTED,
                Formation.APPROVED, Formation.REJECTED, Report.RESOLVED, Report.DISMISSED,
                Subscription.GRANTED, Subscription.ACTIVATED, Subscription.CANCELED, Subscription.REVOKED,
                Subscription.State.CORRECTED, Subscription.Plan.CREATED, Subscription.Plan.UPDATED,
                Subscription.Plan.DEACTIVATED, Payment.State.PAID, Payment.State.FAILED,
                Payment.State.CANCELED, Payment.State.CORRECTED, Refund.Request.REJECTED,
                Analytics.REBUILD, Analytics.Job.SUBMITTED, Analytics.Result.READ, Analytics.EXPORT);
    }

    public static Key fromValue(String value) {
        return all().stream()
                .filter(action -> action.value().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported audit action: " + value));
    }

    public static final class Authentication {
        private Authentication() { }

        public enum Email implements Key {
            VERIFIED("EMAIL_VERIFIED");
            private final String value;
            Email(String value) { this.value = value; }
            public String value() { return value; }
        }

        public static final class Password {
            private Password() { }

            public enum Reset implements Key {
                COMPLETED("PASSWORD_RESET_COMPLETED");
                private final String value;
                Reset(String value) { this.value = value; }
                public String value() { return value; }
            }

            public enum Changed implements Key {
                VALUE("PASSWORD_CHANGED");
                private final String value;
                Changed(String value) { this.value = value; }
                public String value() { return value; }
            }
        }
    }

    public static final class Permission {
        private Permission() { }

        public enum Assignment implements Key {
            ROLE("ASSIGN_ROLE"), BULK("ASSIGN_PERMISSION_BULK");
            private final String value;
            Assignment(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Grant implements Key {
            VALUE("PERMISSION_GRANTED");
            private final String value;
            Grant(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Revoke implements Key {
            VALUE("PERMISSION_REVOKED");
            private final String value;
            Revoke(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum User implements Key {
        APPROVED("APPROVE_USER"), BANNED("BAN_USER"), UNBANNED("UNBAN_USER");
        private final String value;
        User(String value) { this.value = value; }
        public String value() { return value; }

        public enum Timeout implements Key {
            VALUE("TIMEOUT_USER");
            private final String value;
            Timeout(String value) { this.value = value; }
            public String value() { return value; }
        }

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
        GRANTED("SUBSCRIPTION_GRANTED"), ACTIVATED("SUBSCRIPTION_ACTIVATED"),
        CANCELED("SUBSCRIPTION_CANCELED"), REVOKED("SUBSCRIPTION_REVOKED");
        private final String value;
        Subscription(String value) { this.value = value; }
        public String value() { return value; }

        public enum State implements Key {
            CORRECTED("SUBSCRIPTION_STATE_CORRECTED");
            private final String value;
            State(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Plan implements Key {
            CREATED("SUBSCRIPTION_PLAN_CREATED"), UPDATED("SUBSCRIPTION_PLAN_UPDATED"),
            DEACTIVATED("SUBSCRIPTION_PLAN_DEACTIVATED");
            private final String value;
            Plan(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public static final class Payment {
        private Payment() { }

        public enum State implements Key {
            PAID("PAYMENT_PAID"), FAILED("PAYMENT_FAILED"), CANCELED("PAYMENT_CANCELED"),
            CORRECTED("PAYMENT_STATE_CORRECTED");
            private final String value;
            State(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public static final class Refund {
        private Refund() { }

        public enum Request implements Key {
            REJECTED("REFUND_REQUEST_REJECTED");
            private final String value;
            Request(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Analytics implements Key {
        REBUILD("ANALYTICS_REBUILD"), EXPORT("ANALYTICS_EXPORT");
        private final String value;
        Analytics(String value) { this.value = value; }
        public String value() { return value; }

        public enum Job implements Key {
            SUBMITTED("ANALYTICS_JOB_SUBMITTED");
            private final String value;
            Job(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Result implements Key {
            READ("ANALYTICS_RESULT_READ");
            private final String value;
            Result(String value) { this.value = value; }
            public String value() { return value; }
        }
    }
}
