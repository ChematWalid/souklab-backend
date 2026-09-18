package com.project.souklab.dto.chat;
import java.time.LocalDateTime;
import java.util.List;
public record MessageResponse(String id, String conversationId, String authorId, String content, boolean deleted, LocalDateTime createdAt, LocalDateTime editedAt, List<MessageAttachmentResponse> attachments) {}
