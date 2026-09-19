package com.project.souklab.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationTypeConverter implements AttributeConverter<NotificationType.Key, String> {
    @Override
    public String convertToDatabaseColumn(NotificationType.Key attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public NotificationType.Key convertToEntityAttribute(String databaseValue) {
        return databaseValue == null ? null : NotificationType.fromValue(databaseValue);
    }
}
