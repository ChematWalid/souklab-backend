package com.project.souklab.analytics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.dao.analytics.ActivityEventRepository;
import com.project.souklab.dao.analytics.AnalyticsOutboxRepository;
import com.project.souklab.model.analytics.ActivityEvent;
import com.project.souklab.model.analytics.AnalyticsOutboxEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivityEventServiceTest {
    @Test
    void writesIsoEventTimeToRabbitOutboxPayload() throws Exception {
        ActivityEventRepository events = mock(ActivityEventRepository.class);
        AnalyticsOutboxRepository outbox = mock(AnalyticsOutboxRepository.class);
        when(events.save(any(ActivityEvent.class))).thenAnswer(invocation -> {
            ActivityEvent saved = invocation.getArgument(0);
            saved.setId("event-1");
            return saved;
        });
        Clock clock = Clock.fixed(Instant.parse("2026-09-21T16:00:00Z"), ZoneOffset.UTC);

        new ActivityEventService(events, outbox, new ObjectMapper(), clock)
                .record(AnalyticsEvent.Authentication.Login.SUCCEEDED, "actor-1", "subject-1", Map.of());

        ArgumentCaptor<AnalyticsOutboxEvent> captor = ArgumentCaptor.forClass(AnalyticsOutboxEvent.class);
        verify(outbox).save(captor.capture());
        JsonNode payload = new ObjectMapper().readTree(captor.getValue().getPayloadJson());
        assertThat(payload.get("eventTime").isTextual()).isTrue();
        assertThat(payload.get("eventTime").asText()).isEqualTo("2026-09-21T16:00");
    }
}
