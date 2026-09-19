package com.project.souklab.analytics;

import java.time.LocalDate;

/** Typed in-memory key for one daily event rollup. */
public record AnalyticsEventRollupKey(LocalDate rollupDate, AnalyticsEvent.Type eventType) {
    public String databaseKey() {
        return AnalyticsMetric.EventRollup.PREFIX.value() + eventType.value();
    }
}
