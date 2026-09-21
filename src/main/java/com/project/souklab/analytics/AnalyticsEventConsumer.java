package com.project.souklab.analytics;

import java.time.ZoneId;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.dao.analytics.AnalyticsProcessedEventRepository;
import com.project.souklab.config.AnalyticsProperties;
import com.project.souklab.dao.analytics.DailyKpiRollupRepository;
import com.project.souklab.model.analytics.AnalyticsProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@ConditionalOnProperty(name = "app.analytics.rabbit.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventConsumer {
    private final ObjectMapper objectMapper;
    private final AnalyticsProcessedEventRepository processed;
    private final DailyKpiRollupRepository rollups;
    private final AnalyticsProperties properties;

    @RabbitListener(queues = "${app.analytics.rabbit.queue}")
    @Transactional
    public void consume(String payload) throws Exception {
        JsonNode event = objectMapper.readTree(payload);
        String id = event.path(AnalyticsMetric.Payload.Event.ID.value()).asText(null);
        String typeValue = event.path(AnalyticsMetric.Payload.Event.TYPE.value()).asText(null);
        if (id == null || typeValue == null) throw new IllegalArgumentException("Analytics event lacks identity or type");
        AnalyticsEvent.Type type = AnalyticsEvent.fromValue(typeValue)
                .orElseThrow(() -> new IllegalArgumentException("Analytics event type is not registered"));
        if (processed.existsByEventId(id)) return;
        LocalDateTime eventTime = parseEventTime(event.path(AnalyticsMetric.Payload.Event.TIME.value()));
        LocalDate day = eventTime.atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(properties.getBusinessTimeZone())).toLocalDate();
        rollups.incrementEventKpi(day, new AnalyticsEventRollupKey(day, type).databaseKey());
        AnalyticsProcessedEvent marker = new AnalyticsProcessedEvent(); marker.setEventId(id); processed.save(marker);
    }

    private LocalDateTime parseEventTime(JsonNode value) {
        if (value == null || value.isMissingNode() || value.isNull()) {
            throw new IllegalArgumentException("Analytics event lacks event time");
        }
        try {
            if (value.isTextual()) return LocalDateTime.parse(value.asText());
            if (value.isArray() && value.size() >= 6) {
                return LocalDateTime.of(value.get(0).asInt(), value.get(1).asInt(), value.get(2).asInt(),
                        value.get(3).asInt(), value.get(4).asInt(), value.get(5).asInt(),
                        value.size() > 6 ? value.get(6).asInt() : 0);
            }
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Analytics event has an invalid event time", exception);
        }
        throw new IllegalArgumentException("Analytics event has an invalid event time");
    }
}
