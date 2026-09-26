package com.project.souklab.event;

import java.time.Instant;

/**
 * Marker interface for all domain events published within the Souklab application.
 * Enables loose coupling across domain aggregates and asynchronous side-effect processing
 * (notifications, audits, analytics, third-party webhooks) without modifying core domain services.
 */
public interface DomainEvent {

    /**
     * Unique identifier of this event occurrence.
     */
    String eventId();

    /**
     * Timestamp when the domain event occurred.
     */
    Instant occurredAt();
}
