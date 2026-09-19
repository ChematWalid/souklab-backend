package com.project.souklab.service.notification;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.NotificationRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.notification.NotificationResponseDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.Notification;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import com.project.souklab.security.Permission;
import com.project.souklab.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Clock clock;
    private final AppProperties appProperties;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                                SimpMessagingTemplate messagingTemplate, Clock clock, AppProperties appProperties) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.clock = clock;
        this.appProperties = appProperties;
    }

    /**
     * Creates a new notification for a specific user and sends it over WebSocket.
     * The notification is saved to the database as unread and broadcasted to the user's active session.
     *
     * @param user the recipient of the notification
     * @param message the content of the notification
     * @return a NotificationResponseDTO representing the newly created notification
     */
    @Transactional
    public NotificationResponseDTO createForUser(User user, String message) {
        return createForUser(user, message, null, null);
    }

    /**
     * Creates a new notification for a specific user and sends it over WebSocket.
     * Includes type and targetId for aggregating or identifying specific entities.
     *
     * @param user the recipient of the notification
     * @param message the content of the notification
     * @param type the type of notification
     * @param targetId the UUID string ID of the related entity
     * @return a NotificationResponseDTO representing the newly created notification
     */
    @Transactional
    public NotificationResponseDTO createForUser(User user, String message, NotificationType type, String targetId) {
        validateMessageLength(message);

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUser(user);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationResponseDTO response = mapToDTO(saved);
        dispatchRealtimePush(user.getEmail(), response);
        return response;
    }

    /**
     * Creates or updates an aggregated notification.
     *
     * @param user the recipient of the notification
     * @param type the type of notification
     * @param targetId the UUID string ID of the related entity
     * @param baseMessage the base message format
     * @param count the number of occurrences to aggregate
     * @param initiatorUsername the username/email of the latest initiator
     * @return a NotificationResponseDTO representing the newly created or updated notification
     */
    @Transactional
    public NotificationResponseDTO createOrUpdateAggregatedNotification(User user, NotificationType type, String targetId, String baseMessage, int count, String initiatorUsername) {
        Optional<Notification> opt = notificationRepository.findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc(user, type, targetId);
        Notification notification;
        String message;
        if (count > 1) {
            message = initiatorUsername + " and " + (count - 1) + " others " + baseMessage;
        } else {
            message = initiatorUsername + " " + baseMessage;
        }
        validateMessageLength(message);

        if (opt.isPresent()) {
            notification = opt.get();
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now(clock));
        } else {
            notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setTargetId(targetId);
            notification.setMessage(message);
            notification.setRead(false);
        }

        Notification saved = notificationRepository.save(notification);
        NotificationResponseDTO response = mapToDTO(saved);
        dispatchRealtimePush(user.getEmail(), response);
        return response;
    }

    /**
     * Sends a system-wide notification to all users who possess the administrator permission.
     * Uses targeted repository query to avoid scanning the entire users table.
     *
     * @param message the content of the notification to be sent to admins
     */
    @Transactional
    public void notifyAdmins(String message) {
        List<User> admins = userRepository.findByPermissionKey(Permission.Admin.USERS.value());
        for (User admin : admins) {
            createForUser(admin, message);
        }
    }

    /**
     * Retrieves a paginated list of notifications belonging to the currently authenticated user,
     * excluding soft-deleted notifications, ordered newest to oldest.
     *
     * @param pageable pagination parameters
     * @return a PaginatedResponse envelope of the user's mapped notifications
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<NotificationResponseDTO> getCurrentUserNotifications(Pageable pageable) {
        User user = getCurrentUser();
        Page<Notification> page = notificationRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user, pageable);
        return PaginatedResponse.from(page.map(this::mapToDTO));
    }

    /**
     * Returns the exact count of unread, non-deleted notifications for the current authenticated user.
     *
     * @return unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User user = getCurrentUser();
        return notificationRepository.countByUserAndIsReadFalseAndDeletedAtIsNull(user);
    }

    /**
     * Marks a specific notification as 'read' if it belongs to the current user and is not soft-deleted.
     *
     * @param notificationId the unique identifier of the notification
     * @return the updated NotificationResponseDTO reflecting the read status
     * @throws ResourceNotFoundException if the notification cannot be found or doesn't belong to the user
     */
    @Transactional
    public NotificationResponseDTO markAsRead(String notificationId) {
        User user = getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUserAndDeletedAtIsNull(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        return mapToDTO(notification);
    }

    /**
     * Efficiently marks all unread notifications belonging to the current authenticated user as 'read'
     * using a single bulk @Modifying update query.
     */
    @Transactional
    public void markAllAsRead() {
        User user = getCurrentUser();
        notificationRepository.markAllAsReadForUser(user);
    }

    /**
     * Soft-deletes a notification belonging to the current authenticated user by setting its deletedAt timestamp.
     *
     * @param notificationId the unique identifier of the notification
     * @throws ResourceNotFoundException if the notification cannot be found or doesn't belong to the user
     */
    @Transactional
    public void deleteNotification(String notificationId) {
        User user = getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUserAndDeletedAtIsNull(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        notification.setDeletedAt(LocalDateTime.now(clock));
        notificationRepository.save(notification);
    }

    private void dispatchRealtimePush(String recipientEmail, NotificationResponseDTO payload) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new RealtimeNotificationAfterCommit(messagingTemplate, recipientEmail,
                            appProperties.getChat().getNotificationDestination(), payload));
        } else {
            sendRealtimePayload(recipientEmail, payload);
        }
    }

    private void sendRealtimePayload(String recipientEmail, NotificationResponseDTO payload) {
        try {
            messagingTemplate.convertAndSendToUser(recipientEmail, appProperties.getChat().getNotificationDestination(), payload);
        } catch (Exception ex) {
            log.warn("Failed to deliver real-time WebSocket notification to user '{}': {}", recipientEmail, ex.getMessage());
        }
    }

    private void validateMessageLength(String message) {
        int maxMessageLength = appProperties.getNotification().getMaxMessageLength();
        if (message != null && message.length() > maxMessageLength) {
            throw new BadRequestException("Notification message exceeds maximum allowed length of " + maxMessageLength + " characters.");
        }
    }

    private User getCurrentUser() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private NotificationResponseDTO mapToDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .type(notification.getType())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
