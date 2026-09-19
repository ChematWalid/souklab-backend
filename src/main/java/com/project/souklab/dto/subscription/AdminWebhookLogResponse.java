package com.project.souklab.dto.subscription;

import com.project.souklab.service.subscription.ChargilyWebhookEvent;
import com.project.souklab.model.WebhookProcessingStatus;
import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
public class AdminWebhookLogResponse {
    String id;
    String providerEventId;
    ChargilyWebhookEvent.Checkout eventType;
    boolean signatureValid;
    WebhookProcessingStatus status;
    String providerCheckoutId;
    String failureReason;
    LocalDateTime createdAt;
}
