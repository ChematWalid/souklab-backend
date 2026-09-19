package com.project.souklab.model.analytics;

import com.project.souklab.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_outbox_events", indexes = @Index(name = "idx_analytics_outbox_status_created", columnList = "status,created_at"))
@Getter @Setter @NoArgsConstructor
public class AnalyticsOutboxEvent extends BaseEntity {
    @Column(name = "event_id", nullable = false, unique = true, length = 36) private String eventId;
    @Column(name = "event_type", nullable = false, length = 64) private String eventType;
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT") private String payloadJson;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private OutboxStatus status = OutboxStatus.PENDING;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;
    @Column(name = "next_attempt_at") private LocalDateTime nextAttemptAt;
    @Column(name = "published_at") private LocalDateTime publishedAt;
    @Column(name = "last_error", columnDefinition = "TEXT") private String lastError;

    public enum OutboxStatus { PENDING, PUBLISHED, DEAD_LETTER }
}
