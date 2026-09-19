package com.project.souklab.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/** Jackson 2 adapter for audit action values. */
public class AuditLogActionDeserializer extends JsonDeserializer<AuditLogAction.Key> {
    @Override
    public AuditLogAction.Key deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return AuditLogAction.fromValue(parser.getValueAsString());
    }
}
