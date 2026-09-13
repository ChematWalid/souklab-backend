# Souklab Notification System Documentation & Source Code Reference

Complete technical reference and source code for the Notification subsystem in the Souklab platform, including Domain Entities, Database Schema, Data Access Objects (DAO), Data Transfer Objects (DTO), Application Services, REST Endpoints, WebSocket/STOMP external broker relay (RabbitMQ) real-time messaging, and application-wide event integration triggers.

---

## 1. System Overview & Architecture

The notification system provides a production-grade dual-channel architecture:
1. **Persistent Store (Database)**: All notifications are recorded in MariaDB/MySQL for chronological retrieval, unread badge calculation, pagination, and soft deletion.
2. **Real-time Push (WebSocket / STOMP via RabbitMQ Relay)**: When a notification is generated, it is automatically pushed to the recipient's active WebSocket session (`/user/queue/notifications`) via Spring's `SimpMessagingTemplate` backed by an external STOMP Broker Relay (RabbitMQ). This ensures multi-instance horizontal scaling without message loss.
3. **Transactional Isolation**: Real-time WebSocket dispatch is deferred until after successful database transaction commit via `TransactionSynchronizationManager.afterCommit()`, guarded by `try-catch` to ensure broker hiccups never roll back primary database transactions.

```
                  ┌─────────────────────────────────────────┐
                  │           Domain Event Trigger          │
                  │  (User Registration, Moderation, etc.)  │
                  └────────────────────┬────────────────────┘
                                       │
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │           NotificationService           │
                  └────────────┬───────────────────────┬────┘
                               │                       │
              1. Persist State │                       │ 2. Deferred Push (afterCommit)
                               ▼                       ▼
                  ┌──────────────────────┐  ┌─────────────────────────────┐
                  │NotificationRepository│  │   SimpMessagingTemplate     │
                  │     (MariaDB/JPA)    │  │ (STOMP Relay -> RabbitMQ)   │
                  └──────────────────────┘  └──────────────┬──────────────┘
                                                           │
                                                           ▼
                                            ┌─────────────────────────────┐
                                            │    /user/{email}/queue/     │
                                            │        notifications        │
                                            └─────────────────────────────┘
```

---

## 2. Domain Model & Enums

### 2.1. `Notification.java`
- **File Path**: `src/main/java/com/project/souklab/model/Notification.java`
- **Table**: `notifications`
- **Inherits**: `BaseEntity` (provides `id`, `createdAt`, `updatedAt`, `deletedAt`)

```java
package com.project.souklab.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String message;
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    private String targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

---

### 2.2. `NotificationType.java`
- **File Path**: `src/main/java/com/project/souklab/model/NotificationType.java`
- **Description**: Enumeration of all discrete event categories supported across the Souklab ecosystem.

```java
package com.project.souklab.model;

public enum NotificationType {
    ACCOUNT_VALIDATED,
    /** Reserved for Phase 10 (Admin direct account rejection). */
    ACCOUNT_REJECTED,
    ACCOUNT_SUSPENDED,
    /** Reserved for Phase 6 (Formations approval). */
    FORMATION_APPROVED,
    /** Reserved for Phase 6 (Formations rejection). */
    FORMATION_REJECTED,
    /** Reserved for Phase 8 (Realtime Direct Messaging). */
    NEW_MESSAGE,
    /** Reserved for Phase 9 (Monetization & Subscriptions). */
    SUBSCRIPTION_RENEWED,
    /** Reserved for Phase 9 (Monetization & Subscriptions). */
    SUBSCRIPTION_EXPIRED,
    /** Reserved for Phase 9 (Chargily Pay V2 Payments). */
    PAYMENT_SUCCESS,
    /** Reserved for Phase 9 (Chargily Pay V2 Payments). */
    PAYMENT_FAILED,
    /** Reserved for Phase 7 (Social Feed & Moderation Reports). */
    NEW_REPORT,
    /** Reserved for Phase 7 (Social Feed & Reviews). */
    NEW_REVIEW,
    /** Reserved for Phase 6 (Formations announcement). */
    NEW_FORMATION,
    FORMATEUR_REQUEST_SUBMITTED,
    FORMATEUR_APPROVED,
    FORMATEUR_GRANTED,
    FORMATEUR_REJECTED,
    FORMATEUR_REVOKED,
    ACCOUNT_REINSTATED
}
```

---

## 3. Database Schema

### Table: `notifications`

| Column Name | SQL Type | Nullable | Default | Constraints & Notes |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(36)` | `NO` | — | **PK** (UUID from `BaseEntity`) |
| `user_id` | `VARCHAR(36)` | `NO` | — | **FK** ➔ `users.id` (`ON DELETE CASCADE`) |
| `message` | `TEXT` | `YES` | `NULL` | Formatted notification message (supports extended reasons) |
| `is_read` | `BIT(1)` / `BOOLEAN` | `NO` | `0` (`false`) | Read status flag |
| `type` | `VARCHAR(50)` | `YES` | `NULL` | `NotificationType` enum string value |
| `target_id` | `VARCHAR(255)` | `YES` | `NULL` | UUID or identifier of associated domain object |
| `created_at` | `DATETIME(6)` | `NO` | — | Timestamp of notification creation |
| `updated_at` | `DATETIME(6)` | `NO` | — | Timestamp of last modification |
| `deleted_at` | `DATETIME(6)` | `YES` | `NULL` | Soft-delete timestamp |

---

## 4. Data Access Layer (DAO / Repository)

### `NotificationRepository.java`
- **File Path**: `src/main/java/com/project/souklab/dao/NotificationRepository.java`
- **Extends**: `JpaRepository<Notification, String>`

```java
package com.project.souklab.dao;

import com.project.souklab.model.Notification;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    Page<Notification> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Optional<Notification> findFirstByUserAndTypeAndTargetIdAndDeletedAtIsNullOrderByCreatedAtDesc(User user, NotificationType type, String targetId);
    
    Optional<Notification> findByIdAndUserAndDeletedAtIsNull(String id, User user);
    
    long countByUserAndIsReadFalseAndDeletedAtIsNull(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false AND n.deletedAt IS NULL")
    int markAllAsReadForUser(@Param("user") User user);
}
```

---

## 5. Data Transfer Objects (DTO)

### `NotificationResponseDTO.java`
- **File Path**: `src/main/java/com/project/souklab/dto/notification/NotificationResponseDTO.java`
- **Pattern**: Immutable response transfer object with Lombok `@Value` and `@Builder`.

```java
package com.project.souklab.dto.notification;

import com.project.souklab.model.NotificationType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class NotificationResponseDTO {
    String id;
    String message;
    boolean isRead;
    NotificationType type;
    String targetId;
    LocalDateTime createdAt;
}
```

---

## 6. Service Layer (`NotificationService.java`)

- **File Path**: `src/main/java/com/project/souklab/service/notification/NotificationService.java`
- **Description**: Handles notification creation with length validation (4000 char guard), persistence, aggregation, targeted administration alerts, paginated retrieval, unread count, single mark-as-read, bulk mark-as-read, and soft deletion.

```java
package com.project.souklab.service.notification;

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
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_MESSAGE_LENGTH = 4000;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationResponseDTO createForUser(User user, String message) {
        return createForUser(user, message, null, null);
    }

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
            notification.setCreatedAt(LocalDateTime.now());
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

    @Transactional
    public void notifyAdmins(String message) {
        List<User> admins = userRepository.findByRoleName("ROLE_ADMIN");
        for (User admin : admins) {
            createForUser(admin, message);
        }
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<NotificationResponseDTO> getCurrentUserNotifications(Pageable pageable) {
        User user = getCurrentUser();
        Page<Notification> page = notificationRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user, pageable);
        return PaginatedResponse.from(page.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User user = getCurrentUser();
        return notificationRepository.countByUserAndIsReadFalseAndDeletedAtIsNull(user);
    }

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

    @Transactional
    public void markAllAsRead() {
        User user = getCurrentUser();
        notificationRepository.markAllAsReadForUser(user);
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        User user = getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUserAndDeletedAtIsNull(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        notification.setDeletedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private void dispatchRealtimePush(String recipientEmail, NotificationResponseDTO payload) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendRealtimePayload(recipientEmail, payload);
                }
            });
        } else {
            sendRealtimePayload(recipientEmail, payload);
        }
    }

    private void sendRealtimePayload(String recipientEmail, NotificationResponseDTO payload) {
        try {
            messagingTemplate.convertAndSendToUser(recipientEmail, "/queue/notifications", payload);
        } catch (Exception ex) {
            log.warn("Failed to deliver real-time WebSocket notification to user '{}': {}", recipientEmail, ex.getMessage());
        }
    }

    private void validateMessageLength(String message) {
        if (message != null && message.length() > MAX_MESSAGE_LENGTH) {
            throw new BadRequestException("Notification message exceeds maximum allowed length of " + MAX_MESSAGE_LENGTH + " characters.");
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
```

---

## 7. REST Controller Layer (`NotificationController.java`)

- **File Path**: `src/main/java/com/project/souklab/controller/notification/NotificationController.java`
- **Base Path**: `/api/v1/notifications`

```java
package com.project.souklab.controller.notification;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.notification.NotificationResponseDTO;
import com.project.souklab.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationResponseDTO>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(Math.max(1, size), 100));
        return ResponseEntity.ok(ApiResponse.success(notificationService.getCurrentUserNotifications(pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsRead(id)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }
}
```

---

## 8. WebSocket & STOMP Relay Configuration

### 8.1. `WebSocketConfig.java`
- **File Path**: `src/main/java/com/project/souklab/config/WebSocketConfig.java`

```java
package com.project.souklab.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final AppProperties appProperties;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        AppProperties.Relay relay = appProperties.getRelay();
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relay.getHost())
                .setRelayPort(relay.getPort())
                .setClientLogin(relay.getClientLogin())
                .setClientPasscode(relay.getClientPasscode())
                .setSystemLogin(relay.getSystemLogin())
                .setSystemPasscode(relay.getSystemPasscode())
                .setUserDestinationBroadcast("/topic/unresolved-user-destination")
                .setUserRegistryBroadcast("/topic/simp-user-registry");

        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
```

---

## 9. Application-Wide Integration Triggers

### 9.1. Artisan Pending Registration Alert
- **Source**: `AuthService.java` (in `registerUser` method)
```java
notificationService.notifyAdmins("New artisan registration pending approval: " + savedUser.getEmail());
```

### 9.2. Account Moderation Triggers
- **Source**: `UserManagementService.java`
```java
notificationService.createForUser(user, "Your account has been approved and is now active!", NotificationType.ACCOUNT_VALIDATED, user.getId());

notificationService.createForUser(user, "Your account has been permanently suspended. Reason: " + reason, NotificationType.ACCOUNT_SUSPENDED, user.getId());

notificationService.createForUser(user, "Your account has been timed out for " + minutes + " minutes. Reason: " + reason, NotificationType.ACCOUNT_SUSPENDED, user.getId());
```
