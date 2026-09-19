package com.project.souklab.model.analytics;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/** Deserializes stable report type values into grouped report enums. */
public class AnalyticsReportTypeDeserializer extends JsonDeserializer<AnalyticsReportType.Key> {
    @Override
    public AnalyticsReportType.Key deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return AnalyticsReportType.fromValue(parser.getValueAsString());
    }
}
