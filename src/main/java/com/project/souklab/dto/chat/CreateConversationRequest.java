package com.project.souklab.dto.chat;
import jakarta.validation.constraints.NotBlank;
public record CreateConversationRequest(@NotBlank String recipientUserId) {}
