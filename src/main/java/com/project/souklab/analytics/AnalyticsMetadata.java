package com.project.souklab.analytics;

/**
 * Stable keys used in activity-event metadata.
 *
 * <p>The enum values are the persisted/API boundary representation. Application
 * code uses the grouped constants so metadata contracts are not spread as
 * unrelated string literals.</p>
 */
public final class AnalyticsMetadata {
    private AnalyticsMetadata() {
    }

    public interface Key {
        String value();
    }

    public enum Account implements Key {
        TYPE("accountType"),
        SUBSCRIBER_TYPE("subscriberType");

        private final String value;

        Account(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Moderation implements Key {
        REASON_PRESENT("reasonPresent"),
        MINUTES("minutes"),
        DECISION("decision");

        private final String value;

        Moderation(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Content implements Key {
        POST_TYPE("postType"),
        FORMATION_ID("formationId"),
        TARGET_TYPE("targetType"),
        ACTION("action"),
        RATING("rating");

        private final String value;

        Content(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Message implements Key {
        CONVERSATION_ID("conversationId");

        private final String value;

        Message(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum State implements Key {
        STATUS("status");

        private final String value;

        State(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Subscription implements Key {
        PLAN_ID("planId"),
        PREVIOUS_STATUS("previousStatus"),
        SOURCE("source");

        private final String value;

        Subscription(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Payment implements Key {
        STATUS("paymentStatus"),
        PROVIDER_EVENT("providerEvent"),
        ID("payment_id");

        private final String value;

        Payment(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Provider implements Key {
        SUBSCRIPTION_ID("subscription_id");

        private final String value;

        Provider(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
