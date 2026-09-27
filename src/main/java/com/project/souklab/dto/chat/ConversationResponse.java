package com.project.souklab.dto.chat;
import java.time.LocalDateTime;
public record ConversationResponse(String id, String participantUserId, String participantName, boolean archived,
                                   String lastMessagePreview, long unreadCount, LocalDateTime updatedAt,
                                   String participantAvatarUrl, String participantRole, String lastReadMessageId) {
    public ConversationResponse(String id, String participantUserId, String participantName, boolean archived,
                                 String lastMessagePreview, long unreadCount, LocalDateTime updatedAt) {
        this(id, participantUserId, participantName, archived, lastMessagePreview, unreadCount, updatedAt, null, null, null);
    }
    public ConversationResponse(String id, String participantUserId, String participantName, boolean archived,
                                 String lastMessagePreview, LocalDateTime updatedAt) {
        this(id, participantUserId, participantName, archived, lastMessagePreview, 0, updatedAt, null, null, null);
    }
}
