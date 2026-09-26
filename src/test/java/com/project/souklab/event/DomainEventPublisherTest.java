package com.project.souklab.event;

import com.project.souklab.event.user.UserStatusChangedEvent;
import com.project.souklab.model.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DomainEventPublisher domainEventPublisher;

    @Test
    @DisplayName("publish: delegates domain event to Spring ApplicationEventPublisher")
    void publish_withValidEvent_shouldPublishEvent() {
        UserStatusChangedEvent event = UserStatusChangedEvent.of(
                "user-1", "user@example.com", AccountStatus.PENDING, AccountStatus.ACTIVE, "Approval"
        );

        domainEventPublisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.userId()).isEqualTo("user-1");
        assertThat(event.newStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("publish: safely ignores null event")
    void publish_withNullEvent_shouldNotThrowOrPublish() {
        domainEventPublisher.publish(null);

        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
