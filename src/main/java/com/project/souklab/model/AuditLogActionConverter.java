package com.project.souklab.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AuditLogActionConverter implements AttributeConverter<AuditLogAction.Key, String> {
    @Override
    public String convertToDatabaseColumn(AuditLogAction.Key attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public AuditLogAction.Key convertToEntityAttribute(String databaseValue) {
        return databaseValue == null ? null : AuditLogAction.fromValue(databaseValue);
    }
}
