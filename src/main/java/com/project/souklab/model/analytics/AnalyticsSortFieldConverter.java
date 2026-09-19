package com.project.souklab.model.analytics;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AnalyticsSortFieldConverter implements AttributeConverter<AnalyticsSortField.Key, String> {

    @Override
    public String convertToDatabaseColumn(AnalyticsSortField.Key attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public AnalyticsSortField.Key convertToEntityAttribute(String databaseValue) {
        return databaseValue == null ? null : AnalyticsSortField.fromField(databaseValue);
    }
}
