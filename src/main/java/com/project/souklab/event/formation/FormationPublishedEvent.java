package com.project.souklab.event.formation;

import com.project.souklab.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when an accredited masterclass is published.
 */
public record FormationPublishedEvent(
        String eventId,
        String formationId,
        String authorId,
        String title,
        Instant occurredAt
) implements DomainEvent {

    public static FormationPublishedEvent of(String formationId, String authorId, String title) {
        return new FormationPublishedEvent(
                UUID.randomUUID().toString(),
                formationId,
                authorId,
                title,
                Instant.now()
        );
    }
}
