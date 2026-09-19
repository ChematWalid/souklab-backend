package com.project.souklab.model.analytics;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Jackson 3 adapter for stable grouped analytics sort-field values. */
public class AnalyticsSortFieldValueDeserializer extends ValueDeserializer<AnalyticsSortField.Key> {
    @Override
    public AnalyticsSortField.Key deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        return AnalyticsSortField.fromField(parser.getValueAsString());
    }
}
