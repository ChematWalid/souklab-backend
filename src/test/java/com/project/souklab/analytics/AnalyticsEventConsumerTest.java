package com.project.souklab.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.config.AnalyticsProperties;
import com.project.souklab.dao.analytics.AnalyticsProcessedEventRepository;
import com.project.souklab.dao.analytics.DailyKpiRollupRepository;
import com.project.souklab.model.analytics.AnalyticsProcessedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventConsumerTest {
    @Mock
    private AnalyticsProcessedEventRepository processed;
    @Mock
    private DailyKpiRollupRepository rollups;

    @Test
    void consumesEventWithAtomicRollupAndIdempotencyMarker() throws Exception {
        AnalyticsProperties properties = new AnalyticsProperties();
        properties.setBusinessTimeZone("Africa/Algiers");
        when(processed.existsByEventId("event-1")).thenReturn(false);
        AnalyticsEventConsumer consumer = new AnalyticsEventConsumer(
                new ObjectMapper(), processed, rollups, properties);

        consumer.consume("{\"eventId\":\"event-1\",\"eventType\":\"" + AnalyticsEvent.Authentication.Login.SUCCEEDED.value() + "\","
                + "\"eventTime\":\"2026-01-01T23:30:00\"}");

        verify(rollups).incrementEventKpi(LocalDate.of(2026, 1, 2),
                AnalyticsMetric.EventRollup.PREFIX.value() + AnalyticsEvent.Authentication.Login.SUCCEEDED.value());
        verify(processed).save(any(AnalyticsProcessedEvent.class));
    }

    @Test
    void duplicateEventDoesNotTouchRollup() throws Exception {
        AnalyticsProperties properties = new AnalyticsProperties();
        properties.setBusinessTimeZone("UTC");
        when(processed.existsByEventId("event-1")).thenReturn(true);
        AnalyticsEventConsumer consumer = new AnalyticsEventConsumer(
                new ObjectMapper(), processed, rollups, properties);

        consumer.consume("{\"eventId\":\"event-1\",\"eventType\":\"" + AnalyticsEvent.Authentication.Login.SUCCEEDED.value() + "\","
                + "\"eventTime\":\"2026-01-01T12:00:00\"}");

        verify(rollups, never()).incrementEventKpi(any(), any());
        verify(processed, never()).save(any(AnalyticsProcessedEvent.class));
    }
}
