package com.project.souklab.integration.chargily;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.souklab.service.subscription.ChargilyWebhookEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChargilyWebhookPayload {
    private String id;
    private ChargilyWebhookEvent.Checkout type;
    private JsonNode data;
    @JsonProperty("created_at")
    private Long createdAt;
}
