package com.project.souklab.dto.directory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/** Jackson 2 adapter for directory sort values. */
public class DirectorySortOrderDeserializer extends JsonDeserializer<DirectorySortOrder.Key> {
    @Override
    public DirectorySortOrder.Key deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return DirectorySortOrder.fromValue(parser.getValueAsString());
    }
}
