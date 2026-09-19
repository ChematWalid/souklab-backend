package com.project.souklab.analytics;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import org.springframework.transaction.TransactionStatus;

import com.project.souklab.config.AnalyticsRabbitProperties;
import com.project.souklab.dao.analytics.AnalyticsOutboxRepository;
import com.project.souklab.model.analytics.AnalyticsOutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsOutboxRelayTest {
    @Mock
    private AnalyticsOutboxRepository repository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private TransactionTemplate transactionTemplate;

    private AnalyticsRabbitProperties properties;
    private AnalyticsOutboxRelay relay;

    @BeforeEach
    void setUp() {
        properties = new AnalyticsRabbitProperties();
        properties.setExchange("analytics");
        properties.setRoutingKey("activity");
        properties.setDeadLetterExchange("analytics.dlx");
        properties.setConfirmTimeout(Duration.ofSeconds(1));
        properties.setRetryBackoff(Duration.ofSeconds(5));
        properties.setMaxRetryBackoff(Duration.ofMinutes(1));
        properties.setRetryMultiplier(2.0);
        properties.setMaxAttempts(3);
        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        relay = new AnalyticsOutboxRelay(repository, rabbitTemplate, properties,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC), transactionTemplate);
    }

    @Test
    void confirmedPublishMarksEventPublishedAndClearsRetrySchedule() {
        AnalyticsOutboxEvent event = event();
        when(rabbitTemplate.invoke(any())).thenReturn(null);

        relay.publishOne(event);

        assertThat(event.getStatus()).isEqualTo(AnalyticsOutboxEvent.OutboxStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(event.getNextAttemptAt()).isNull();
        verify(repository).save(event);
    }

    @Test
    void failedPublishPersistsExponentialRetryMetadata() {
        AnalyticsOutboxEvent event = event();
        when(rabbitTemplate.invoke(any())).thenThrow(new IllegalStateException("broker unavailable"));

        relay.publishOne(event);

        assertThat(event.getStatus()).isEqualTo(AnalyticsOutboxEvent.OutboxStatus.PENDING);
        assertThat(event.getAttemptCount()).isEqualTo(1);
        assertThat(event.getLastError()).isEqualTo("broker unavailable");
        assertThat(event.getNextAttemptAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0, 5));
        verify(repository).save(event);
    }

    @Test
    void finalFailureRequiresConfirmedDeadLetterBeforeMarkingDeadLetter() {
        AnalyticsOutboxEvent event = event();
        event.setAttemptCount(2);
        when(rabbitTemplate.invoke(any())).thenThrow(
                new IllegalStateException("broker unavailable"),
                new IllegalStateException("dead-letter unavailable"));

        relay.publishOne(event);

        assertThat(event.getStatus()).isEqualTo(AnalyticsOutboxEvent.OutboxStatus.PENDING);
        assertThat(event.getAttemptCount()).isEqualTo(3);
        assertThat(event.getNextAttemptAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0, 20));
    }

    private AnalyticsOutboxEvent event() {
        AnalyticsOutboxEvent event = new AnalyticsOutboxEvent();
        event.setId("event-1");
        event.setEventId("event-1");
        event.setEventType(AnalyticsEvent.Authentication.LOGIN_SUCCEEDED.value());
        event.setPayloadJson("{}");
        event.setStatus(AnalyticsOutboxEvent.OutboxStatus.PENDING);
        event.setNextAttemptAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return event;
    }
}
