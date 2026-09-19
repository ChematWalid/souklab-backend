package com.project.souklab.model.analytics;

import com.project.souklab.analytics.AnalyticsEvent;
import com.project.souklab.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_events", indexes = {
        @Index(name = "idx_activity_events_type_time", columnList = "event_type,event_time"),
        @Index(name = "idx_activity_events_actor_time", columnList = "actor_id,event_time")
})
@Getter @Setter @NoArgsConstructor
public class ActivityEvent extends BaseEntity {
    @Convert(converter = AnalyticsEventTypeConverter.class)
    @Column(name = "event_type", nullable = false, length = 64) private AnalyticsEvent.Type eventType;
    @Column(name = "actor_id", length = 36) private String actorId;
    @Column(name = "subject_id", length = 36) private String subjectId;
    @Column(name = "event_time", nullable = false) private LocalDateTime eventTime;
    @Column(name = "metadata_json", columnDefinition = "TEXT") private String metadataJson;
}
