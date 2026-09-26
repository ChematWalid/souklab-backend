package com.project.souklab.event.formation;

import com.project.souklab.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when an artisan successfully enrolls in a formation masterclass.
 */
public record FormationEnrolledEvent(
        String eventId,
        String enrollmentId,
        String formationId,
        String artisanId,
        Instant occurredAt
) implements DomainEvent {

    public static FormationEnrolledEvent of(String enrollmentId, String formationId, String artisanId) {
        return new FormationEnrolledEvent(
                UUID.randomUUID().toString(),
                enrollmentId,
                formationId,
                artisanId,
                Instant.now()
        );
    }
}
