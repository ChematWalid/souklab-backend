package com.project.souklab.analytics;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

/** Deserializes grouped analytics table keys to their concrete enum type. */
public class AnalyticsMetricTableKeyDeserializer extends KeyDeserializer {

    @Override
    public AnalyticsMetric.Key deserializeKey(String key, DeserializationContext context) throws IOException {
        try {
            return AnalyticsMetric.Table.fromValue(key);
        } catch (IllegalArgumentException exception) {
            return (AnalyticsMetric.Key) context.handleWeirdKey(AnalyticsMetric.Key.class, key, exception.getMessage());
        }
    }
}
