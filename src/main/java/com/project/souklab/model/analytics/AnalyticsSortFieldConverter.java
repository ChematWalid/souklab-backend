package com.project.souklab.model.analytics;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AnalyticsSortFieldConverter implements AttributeConverter<AnalyticsSortField, String> {

    @Override
    public String convertToDatabaseColumn(AnalyticsSortField attribute) {
        return attribute == null ? null : attribute.field();
    }

    @Override
    public AnalyticsSortField convertToEntityAttribute(String databaseValue) {
        return databaseValue == null ? null : AnalyticsSortField.fromField(databaseValue);
    }
}
