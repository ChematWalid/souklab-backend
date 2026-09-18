package com.project.souklab.service.notification;

import com.project.souklab.dto.notification.NotificationResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;

/** Sends one notification through STOMP after the surrounding transaction commits. */
@Slf4j
final class RealtimeNotificationAfterCommit implements TransactionSynchronization {

    private final SimpMessagingTemplate messagingTemplate;
    private final String recipientEmail;
    private final String destination;
    private final NotificationResponseDTO payload;

    RealtimeNotificationAfterCommit(SimpMessagingTemplate messagingTemplate,
                                    String recipientEmail,
                                    String destination,
                                    NotificationResponseDTO payload) {
        this.messagingTemplate = messagingTemplate;
        this.recipientEmail = recipientEmail;
        this.destination = destination;
        this.payload = payload;
    }

    @Override
    public void afterCommit() {
        try {
            messagingTemplate.convertAndSendToUser(recipientEmail, destination, payload);
        } catch (Exception exception) {
            log.warn("Failed to deliver real-time WebSocket notification to user '{}': {}",
                    recipientEmail, exception.getMessage());
        }
    }
}
