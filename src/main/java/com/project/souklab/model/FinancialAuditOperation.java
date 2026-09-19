package com.project.souklab.model;

public final class FinancialAuditOperation {
    private FinancialAuditOperation() { }

    public interface Type {
        String value();
    }

    public enum Manual implements Type {
        GRANT("MANUAL_GRANT");

        private final String value;

        Manual(String value) { this.value = value; }

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
