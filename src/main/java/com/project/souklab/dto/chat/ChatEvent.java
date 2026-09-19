package com.project.souklab.dto.chat;

import com.project.souklab.service.chat.ChatEventType;

import java.time.LocalDateTime;

public record ChatEvent(String version, String type, String conversationId, String messageId,
                        String correlationId, LocalDateTime timestamp, Object payload) {
    public static ChatEvent create(String version, ChatEventType.Type type, String conversationId,
                                   String messageId, String correlationId, LocalDateTime timestamp,
                                   Object payload) {
        return new ChatEvent(version, type.value(), conversationId, messageId, correlationId, timestamp, payload);
    }
}
