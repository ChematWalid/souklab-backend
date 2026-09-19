package com.project.souklab.analytics;

import com.project.souklab.model.EnumValue;

/** Stable, grouped keys used in activity-event metadata. */
public final class AnalyticsMetadata {
    private AnalyticsMetadata() { }

    public interface Key extends EnumValue { }

    public enum Account implements Key {
        TYPE("accountType");
        private final String value;
        Account(String value) { this.value = value; }
        public String value() { return value; }

        public enum Subscriber implements Key {
            TYPE("subscriberType");
            private final String value;
            Subscriber(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Moderation implements Key {
        ;
        private Moderation() { }

        public enum Reason implements Key {
            PRESENT("reasonPresent");
            private final String value;
            Reason(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Duration implements Key {
            MINUTES("minutes");
            private final String value;
            Duration(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Decision implements Key {
            VALUE("decision");
            private final String value;
            Decision(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Content implements Key {
        ;
        private Content() { }

        public enum Post implements Key {
            TYPE("postType");
            private final String value;
            Post(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Formation implements Key {
            ID("formationId");
            private final String value;
            Formation(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Target implements Key {
            TYPE("targetType");
            private final String value;
            Target(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Action implements Key {
            VALUE("action");
            private final String value;
            Action(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Rating implements Key {
            VALUE("rating");
            private final String value;
            Rating(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Message implements Key {
        ;
        private Message() { }

        public enum Conversation implements Key {
            ID("conversationId");
            private final String value;
            Conversation(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum State implements Key {
        STATUS("status");
        private final String value;
        State(String value) { this.value = value; }
        public String value() { return value; }
    }

    public enum Subscription implements Key {
        ;
        private Subscription() { }

        public enum Plan implements Key {
            ID("planId");
            private final String value;
            Plan(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum State implements Key {
            PREVIOUS("previousStatus");
            private final String value;
            State(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Source implements Key {
            VALUE("source");
            private final String value;
            Source(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Payment implements Key {
        ;
        private Payment() { }

        public enum State implements Key {
            STATUS("paymentStatus");
            private final String value;
            State(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Provider implements Key {
            EVENT("providerEvent");
            private final String value;
            Provider(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Identifier implements Key {
            ID("payment_id");
            private final String value;
            Identifier(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Provider implements Key {
        ;
        private Provider() { }

        public enum Subscription implements Key {
            ID("subscription_id");
            private final String value;
            Subscription(String value) { this.value = value; }
            public String value() { return value; }
        }
    }

    public enum Audit implements Key {
        ;
        private Audit() { }

        public enum Job implements Key {
            ID("jobId");
            private final String value;
            Job(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum MaintenanceJob implements Key {
            ID("maintenanceJobId");
            private final String value;
            MaintenanceJob(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Report implements Key {
            TYPE("reportType");
            private final String value;
            Report(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Operation implements Key {
            VALUE("operation");
            private final String value;
            Operation(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Range implements Key {
            VALUE("range");
            private final String value;
            Range(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Filter implements Key {
            VALUE("filters");
            private final String value;
            Filter(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Permission implements Key {
            SCOPE("permissionScope");
            private final String value;
            Permission(String value) { this.value = value; }
            public String value() { return value; }
        }

        public enum Outcome implements Key {
            VALUE("outcome");
            private final String value;
            Outcome(String value) { this.value = value; }
            public String value() { return value; }
        }
    }
}
