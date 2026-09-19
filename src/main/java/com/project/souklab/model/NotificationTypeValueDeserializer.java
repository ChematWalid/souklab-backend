package com.project.souklab.model;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Jackson 3 adapter for notification values. */
public class NotificationTypeValueDeserializer extends ValueDeserializer<NotificationType.Key> {
    @Override
    public NotificationType.Key deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        return NotificationType.fromValue(parser.getValueAsString());
    }
}
