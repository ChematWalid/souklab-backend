package com.project.souklab.dto.chat;
import java.time.LocalDateTime;
public record ChatEvent(String version, String type, String conversationId, String messageId, String correlationId, LocalDateTime timestamp, Object payload) {}
