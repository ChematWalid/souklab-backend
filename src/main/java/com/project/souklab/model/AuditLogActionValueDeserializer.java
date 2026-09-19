package com.project.souklab.model;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Jackson 3 adapter for audit action values. */
public class AuditLogActionValueDeserializer extends ValueDeserializer<AuditLogAction.Key> {
    @Override
    public AuditLogAction.Key deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        return AuditLogAction.fromValue(parser.getValueAsString());
    }
}
