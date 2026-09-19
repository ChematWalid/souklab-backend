package com.project.souklab.model.analytics;

import com.project.souklab.analytics.AnalyticsMetric;

import java.util.Arrays;
import java.util.Optional;

public enum AnalyticsFilterKey {
    EVENT_TYPE(AnalyticsMetric.Payload.EVENT_TYPE);

    private final AnalyticsMetric.Key key;

    AnalyticsFilterKey(AnalyticsMetric.Key key) {
        this.key = key;
    }

    public String key() {
        return key.value();
    }

    public static Optional<AnalyticsFilterKey> fromKey(String key) {
        return Arrays.stream(values()).filter(filter -> filter.key.value().equals(key)).findFirst();
    }
}
