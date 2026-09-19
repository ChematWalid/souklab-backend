package com.project.souklab.dto.analytics;

import com.project.souklab.model.analytics.AnalyticsJobStatus;
import com.project.souklab.model.analytics.AnalyticsMaintenanceOperation;

/** Metadata-only STOMP notification for an analytics maintenance job. */
public record AnalyticsMaintenanceJobEvent(
        String jobId,
        AnalyticsMaintenanceOperation operation,
        AnalyticsJobStatus status,
        String failureMessage) { }
