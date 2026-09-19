package com.project.souklab.model.analytics;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/** Deserializes the stable analytics sort-field value into its grouped enum. */
public class AnalyticsSortFieldDeserializer extends JsonDeserializer<AnalyticsSortField.Key> {
    @Override
    public AnalyticsSortField.Key deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return AnalyticsSortField.fromField(parser.getValueAsString());
    }
}
