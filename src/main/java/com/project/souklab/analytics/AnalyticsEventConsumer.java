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
        String id = event.path("eventId").asText(null);
        String type = event.path("eventType").asText(null);
        if (id == null || type == null) throw new IllegalArgumentException("Analytics event lacks identity or type");
        if (processed.existsByEventId(id)) return;
        LocalDate day = LocalDateTime.parse(event.path("eventTime").asText()).atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(properties.getBusinessTimeZone())).toLocalDate();
        String key = "event." + type;
        rollups.incrementEventKpi(day, key);
        AnalyticsProcessedEvent marker = new AnalyticsProcessedEvent(); marker.setEventId(id); processed.save(marker);
    }
}
