package com.project.souklab.dto.notification;

import com.project.souklab.model.NotificationType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import jakarta.validation.constraints.Size;

/** Partial or complete notification preference update keyed by notification value. */
public record NotificationPreferencesRequest(@NotNull @Size(max = 40) Map<String, Boolean> preferences) {
}
