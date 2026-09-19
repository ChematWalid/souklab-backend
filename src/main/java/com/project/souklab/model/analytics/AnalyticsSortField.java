package com.project.souklab.model.analytics;

import com.project.souklab.analytics.AnalyticsMetric;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum AnalyticsSortField {
    START_DATE(AnalyticsMetric.Series.START_DATE),
    END_DATE(AnalyticsMetric.Series.END_DATE),
    ACTIVITY_EVENTS(AnalyticsMetric.Series.ACTIVITY_EVENTS),
    NEW_REGISTRATIONS(AnalyticsMetric.Series.NEW_REGISTRATIONS);

    private final AnalyticsMetric.Key field;

    AnalyticsSortField(AnalyticsMetric.Key field) {
        this.field = field;
    }

    @JsonValue
    public String field() {
        return field.value();
    }

    @JsonCreator
    public static AnalyticsSortField fromField(String field) {
        return Arrays.stream(values())
                .filter(sortField -> sortField.field.value().equals(field))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported analytics sort field: " + field));
    }
}
