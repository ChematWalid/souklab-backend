package com.project.souklab.dto.chat;
import java.time.LocalDateTime;
public record ConversationResponse(String id, String participantUserId, String participantName, boolean archived,
                                   String lastMessagePreview, long unreadCount, LocalDateTime updatedAt) {
    public ConversationResponse(String id, String participantUserId, String participantName, boolean archived,
                                 String lastMessagePreview, LocalDateTime updatedAt) {
        this(id, participantUserId, participantName, archived, lastMessagePreview, 0, updatedAt);
    }
}
