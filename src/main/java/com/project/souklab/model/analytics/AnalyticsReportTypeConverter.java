package com.project.souklab.model.analytics;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AnalyticsReportTypeConverter implements AttributeConverter<AnalyticsReportType.Key, String> {
    @Override
    public String convertToDatabaseColumn(AnalyticsReportType.Key attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public AnalyticsReportType.Key convertToEntityAttribute(String databaseValue) {
        return databaseValue == null ? null : AnalyticsReportType.fromValue(databaseValue);
    }
}
