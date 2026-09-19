package com.project.souklab.model.analytics;

import com.project.souklab.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "analytics_processed_events")
@Getter @Setter @NoArgsConstructor
public class AnalyticsProcessedEvent extends BaseEntity {
    @Column(name = "event_id", nullable = false, unique = true, length = 36) private String eventId;
}
