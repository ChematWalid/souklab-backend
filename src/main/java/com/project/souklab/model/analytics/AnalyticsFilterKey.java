package com.project.souklab.model.analytics;

import java.util.Arrays;
import java.util.Optional;

public enum AnalyticsFilterKey {
    EVENT_TYPE("eventType");

    private final String key;

    AnalyticsFilterKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static Optional<AnalyticsFilterKey> fromKey(String key) {
        return Arrays.stream(values()).filter(filter -> filter.key.equals(key)).findFirst();
    }
}
