package com.project.souklab.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Central publisher for domain events in the Souklab monolithic architecture.
 * Encapsulates Spring's {@link ApplicationEventPublisher} to provide type-safe dispatching
 * and consistent observability logging.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publishes a domain event synchronously to all registered in-process listeners.
     * Listeners annotated with {@code @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)}
     * will process the event safely only after the caller's transaction has committed.
     *
     * @param event the domain event instance
     * @param <T>   the concrete event type
     */
    public <T extends DomainEvent> void publish(T event) {
        if (event == null) {
            return;
        }
        log.debug("Publishing domain event [{}]: id={}, occurredAt={}",
                event.getClass().getSimpleName(), event.eventId(), event.occurredAt());
        applicationEventPublisher.publishEvent(event);
    }
}
