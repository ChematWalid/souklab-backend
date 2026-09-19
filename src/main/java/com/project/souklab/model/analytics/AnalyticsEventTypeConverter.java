package com.project.souklab.model.analytics;

import com.project.souklab.analytics.AnalyticsEvent;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AnalyticsEventTypeConverter implements AttributeConverter<AnalyticsEvent.Type, String> {
    @Override
    public String convertToDatabaseColumn(AnalyticsEvent.Type attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public AnalyticsEvent.Type convertToEntityAttribute(String value) {
        if (value == null) return null;
        return AnalyticsEvent.fromValue(value)
                .orElseThrow(() -> new IllegalArgumentException("Unknown analytics event type: " + value));
    }
}
