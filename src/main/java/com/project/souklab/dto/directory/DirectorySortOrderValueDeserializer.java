package com.project.souklab.dto.directory;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Jackson 3 adapter for directory sort values. */
public class DirectorySortOrderValueDeserializer extends ValueDeserializer<DirectorySortOrder.Key> {
    @Override
    public DirectorySortOrder.Key deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        return DirectorySortOrder.fromValue(parser.getValueAsString());
    }
}
