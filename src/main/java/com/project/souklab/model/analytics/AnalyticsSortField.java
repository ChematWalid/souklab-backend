package com.project.souklab.model.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum AnalyticsSortField {
    START_DATE("startDate"),
    END_DATE("endDate"),
    ACTIVITY_EVENTS("activityEvents"),
    NEW_REGISTRATIONS("newRegistrations");

    private final String field;

    AnalyticsSortField(String field) {
        this.field = field;
    }

    @JsonValue
    public String field() {
        return field;
    }

    @JsonCreator
    public static AnalyticsSortField fromField(String field) {
        return Arrays.stream(values())
                .filter(sortField -> sortField.field.equals(field))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported analytics sort field: " + field));
    }
}
