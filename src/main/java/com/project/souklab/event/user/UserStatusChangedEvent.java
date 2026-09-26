package com.project.souklab.event.user;

import com.project.souklab.event.DomainEvent;
import com.project.souklab.model.AccountStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published whenever a user's account status transitions
 * (e.g. validated, suspended, timed out, reinstated).
 */
public record UserStatusChangedEvent(
        String eventId,
        String userId,
        String userEmail,
        AccountStatus previousStatus,
        AccountStatus newStatus,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    public static UserStatusChangedEvent of(
            String userId,
            String userEmail,
            AccountStatus previousStatus,
            AccountStatus newStatus,
            String reason
    ) {
        return new UserStatusChangedEvent(
                UUID.randomUUID().toString(),
                userId,
                userEmail,
                previousStatus,
                newStatus,
                reason,
                Instant.now()
        );
    }
}
