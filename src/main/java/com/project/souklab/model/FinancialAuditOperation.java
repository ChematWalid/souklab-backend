package com.project.souklab.model;

public enum FinancialAuditOperation {
    MANUAL_GRANT("MANUAL_GRANT"),
    REVOKE("REVOKE"),
    STATE_CORRECTION("STATE_CORRECTION"),
    CANCEL("CANCEL");

    private final String value;

    FinancialAuditOperation(String value) { this.value = value; }

    public String value() { return value; }
}
