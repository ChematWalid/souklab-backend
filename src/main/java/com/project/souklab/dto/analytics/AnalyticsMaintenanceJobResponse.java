package com.project.souklab.dto.analytics;

import com.project.souklab.model.analytics.AnalyticsJobStatus;
import com.project.souklab.model.analytics.AnalyticsMaintenanceOperation;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class AnalyticsMaintenanceJobResponse {
    String id;
    AnalyticsMaintenanceOperation operation;
    AnalyticsJobStatus status;
    LocalDate fromDate;
    LocalDate toDate;
    int eventsRead;
    int rollupsWritten;
    LocalDateTime completedAt;
    LocalDateTime expiresAt;
    String failureMessage;
}
