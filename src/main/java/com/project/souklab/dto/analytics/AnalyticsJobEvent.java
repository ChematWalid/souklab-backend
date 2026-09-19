package com.project.souklab.dto.analytics;

import com.project.souklab.model.analytics.AnalyticsJobStatus;
import com.project.souklab.model.analytics.AnalyticsReportType;

public record AnalyticsJobEvent(String jobId, AnalyticsJobStatus status, AnalyticsReportType reportType, String failureMessage) { }
