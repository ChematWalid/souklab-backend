package com.project.souklab.controller.notification;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.notification.NotificationResponseDTO;
import com.project.souklab.dto.notification.NotificationPreferencesRequest;
import com.project.souklab.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "User in-app notifications, unread counts, and read status management")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieves paginated notifications for the authenticated user ordered by creation date descending.")
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponseDTO>>> getNotifications(
            @RequestParam(required = false) Boolean read, Pageable pageable) {
        PaginatedResponse<NotificationResponseDTO> response = read == null
                ? notificationService.getCurrentUserNotifications(pageable)
                : notificationService.getCurrentUserNotifications(read, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getPreferences() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotificationPreferences()));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> updatePreferences(
            @Valid @RequestBody NotificationPreferencesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.updateNotificationPreferences(request.preferences())));
    }

    @PostMapping("/preferences/reset")
    @Operation(summary = "Reset notification preferences")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> resetPreferences() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.resetNotificationPreferences()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieves a single notification for the authenticated user.")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> getNotification(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotification(id)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count", description = "Returns the total number of unread notifications for the authenticated user as a numeric value in data.")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read by ID.")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsRead(id)));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications as read for the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Soft-deletes a notification by ID.")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }
}
