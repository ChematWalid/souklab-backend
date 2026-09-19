package com.project.souklab.model.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.project.souklab.analytics.AnalyticsMetric;
import com.project.souklab.model.EnumValue;

import java.util.Arrays;
import java.util.Optional;

public final class AnalyticsFilterKey {
    private AnalyticsFilterKey() { }

    public enum Event implements EnumValue {
        TYPE(AnalyticsMetric.Payload.Event.TYPE);

        private final AnalyticsMetric.Key key;

        Event(AnalyticsMetric.Key key) {
            this.key = key;
        }

        public AnalyticsMetric.Key metricKey() {
            return key;
        }

        @Override
        @JsonValue
        public String value() {
            return key.value();
        }

        @JsonCreator
        public static Event fromJson(String key) {
            return fromKey(key).orElseThrow(() -> new IllegalArgumentException("Unsupported analytics filter: " + key));
        }

        public static Optional<Event> fromKey(String key) {
            return Arrays.stream(values()).filter(filter -> filter.value().equals(key)).findFirst();
        }
    }
}
