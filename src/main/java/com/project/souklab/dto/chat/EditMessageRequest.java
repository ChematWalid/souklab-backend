package com.project.souklab.dto.chat;
import jakarta.validation.constraints.NotBlank;
public record EditMessageRequest(@NotBlank String content) {}
