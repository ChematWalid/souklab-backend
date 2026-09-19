package com.project.souklab.dto.notification;

import com.project.souklab.model.NotificationType;
import com.project.souklab.model.NotificationTypeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class NotificationResponseDTO {
    String id;
    String message;
    boolean isRead;
    @JsonDeserialize(using = NotificationTypeDeserializer.class)
    NotificationType.Key type;
    String targetId;
    LocalDateTime createdAt;
}
