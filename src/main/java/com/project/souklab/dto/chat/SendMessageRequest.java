package com.project.souklab.dto.chat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
public record SendMessageRequest(@NotBlank @Size(max = 128) String idempotencyKey,
                                 @Size(max = 4000) String content,
                                 List<@NotBlank String> attachmentKeys) {}
