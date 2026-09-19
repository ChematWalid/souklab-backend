package com.project.souklab.model;

public final class FinancialAuditOperation {
    private FinancialAuditOperation() { }

    public interface Type extends EnumValue { }

    public enum Manual implements Type {
        GRANT("MANUAL_GRANT");

        private final String value;

        Manual(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum Plan implements Type {
        CREATE("PLAN_CREATE"),
        UPDATE("PLAN_UPDATE"),
        DEACTIVATE("PLAN_DEACTIVATE");

        private final String value;

        Plan(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum Refund implements Type {
        REQUEST("REFUND_REQUEST");

        private final String value;

        Refund(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum State implements Type {
        CORRECTION("STATE_CORRECTION");

        private final String value;

        State(String value) { this.value = value; }

        public String value() { return value; }
    }

    public enum Subscription implements Type {
        REVOKE("REVOKE"), CANCEL("CANCEL");

        private final String value;

        Subscription(String value) { this.value = value; }

        public String value() { return value; }
    }
}
