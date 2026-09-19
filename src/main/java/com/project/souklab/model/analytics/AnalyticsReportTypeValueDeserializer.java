package com.project.souklab.model.analytics;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Jackson 3 adapter for stable grouped analytics report values. */
public class AnalyticsReportTypeValueDeserializer extends ValueDeserializer<AnalyticsReportType.Key> {
    @Override
    public AnalyticsReportType.Key deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        return AnalyticsReportType.fromValue(parser.getValueAsString());
    }
}
