package com.project.souklab.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.souklab.dao.analytics.ActivityEventRepository;
import com.project.souklab.dao.analytics.AnalyticsOutboxRepository;
import com.project.souklab.model.analytics.ActivityEvent;
import com.project.souklab.model.analytics.AnalyticsOutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;

/** Small, reusable event-intent writer; callers invoke it in their transaction. */
@Service
@RequiredArgsConstructor
public class ActivityEventService {
    private final ActivityEventRepository repository;
    private final AnalyticsOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public ActivityEvent record(AnalyticsEvent.Type type, String actorId, String subjectId, Map<String, ?> metadata) {
        ActivityEvent event = new ActivityEvent();
        event.setEventType(type); event.setActorId(actorId); event.setSubjectId(subjectId);
        event.setEventTime(LocalDateTime.now(clock));
        try { event.setMetadataJson(objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata)); }
        catch (JsonProcessingException e) { throw new IllegalArgumentException("Activity metadata is not serializable", e); }
        ActivityEvent saved = repository.save(event);
        AnalyticsOutboxEvent outbox = new AnalyticsOutboxEvent();
        outbox.setEventId(saved.getId());
        outbox.setEventType(type);
        outbox.setNextAttemptAt(LocalDateTime.now(clock));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(AnalyticsMetric.Payload.EVENT_ID.value(), saved.getId());
        payload.put(AnalyticsMetric.Payload.EVENT_TYPE.value(), type.value());
        payload.put(AnalyticsMetric.Payload.ACTOR_ID.value(), actorId == null ? "" : actorId);
        payload.put(AnalyticsMetric.Payload.SUBJECT_ID.value(), subjectId == null ? "" : subjectId);
        payload.put(AnalyticsMetric.Payload.EVENT_TIME.value(), saved.getEventTime());
        payload.put(AnalyticsMetric.Payload.METADATA.value(), metadata == null ? Map.of() : metadata);
        try { outbox.setPayloadJson(objectMapper.writeValueAsString(payload)); }
        catch (JsonProcessingException e) { throw new IllegalArgumentException("Activity metadata is not serializable", e); }
        outboxRepository.save(outbox);
        return saved;
    }
}
