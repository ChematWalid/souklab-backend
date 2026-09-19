package com.project.souklab.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/** Jackson 2 adapter for notification values. */
public class NotificationTypeDeserializer extends JsonDeserializer<NotificationType.Key> {
    @Override
    public NotificationType.Key deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return NotificationType.fromValue(parser.getValueAsString());
    }
}
