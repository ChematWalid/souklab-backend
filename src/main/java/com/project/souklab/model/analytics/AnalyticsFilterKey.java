package com.project.souklab.model.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.project.souklab.analytics.AnalyticsMetric;

import java.util.Arrays;
import java.util.Optional;

public enum AnalyticsFilterKey {
    EVENT_TYPE(AnalyticsMetric.Payload.EVENT_TYPE);

    private final AnalyticsMetric.Key key;

    AnalyticsFilterKey(AnalyticsMetric.Key key) {
        this.key = key;
    }

    @JsonValue
    public String key() {
        return key.value();
    }

    @JsonCreator
    public static AnalyticsFilterKey fromJson(String key) {
        return fromKey(key).orElseThrow(() -> new IllegalArgumentException("Unsupported analytics filter: " + key));
    }

    public static Optional<AnalyticsFilterKey> fromKey(String key) {
        return Arrays.stream(values()).filter(filter -> filter.key.value().equals(key)).findFirst();
    }
}
