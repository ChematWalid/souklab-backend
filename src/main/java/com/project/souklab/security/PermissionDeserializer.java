package com.project.souklab.security;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Deserializes the public permission value into its canonical grouped enum. */
public final class PermissionDeserializer extends ValueDeserializer<Permission> {
    @Override
    public Permission deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) return null;
        return Permission.fromValue(value).orElse(null);
    }
}
