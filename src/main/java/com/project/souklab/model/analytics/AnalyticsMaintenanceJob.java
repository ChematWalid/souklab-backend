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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_maintenance_jobs",
        indexes = @Index(name = "idx_analytics_maintenance_owner_status", columnList = "owner_id,status,created_at"))
@Getter
@Setter
@NoArgsConstructor
public class AnalyticsMaintenanceJob extends BaseEntity {
    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AnalyticsMaintenanceOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AnalyticsJobStatus status;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "range_to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "events_read", nullable = false)
    private int eventsRead;

    @Column(name = "rollups_written", nullable = false)
    private int rollupsWritten;

    @Column(name = "permission_scope", nullable = false, length = 200)
    private String permissionScope;

    @Column(name = "failure_message", columnDefinition = "TEXT")
    private String failureMessage;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
