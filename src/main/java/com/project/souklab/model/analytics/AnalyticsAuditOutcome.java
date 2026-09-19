package com.project.souklab.model.analytics;

import com.project.souklab.model.EnumValue;

/** Closed set of outcomes recorded for administrative analytics actions. */
public enum AnalyticsAuditOutcome implements EnumValue {
    ACCEPTED("ACCEPTED"), STATUS("STATUS"), SUCCESS("SUCCESS");

    private final String value;

    AnalyticsAuditOutcome(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
