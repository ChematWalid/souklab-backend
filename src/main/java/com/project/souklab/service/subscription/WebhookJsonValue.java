package com.project.souklab.service.subscription;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebhookJsonValue {
    private final JsonNode node;

    public String text(String... paths) {
        for (String path : paths) {
            JsonNode value = node;
            for (String part : path.split("\\.")) {
                value = value == null ? null : value.get(part);
            }
            if (value != null && value.isTextual() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return null;
    }
}
