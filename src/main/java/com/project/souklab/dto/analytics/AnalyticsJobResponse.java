package com.project.souklab.dto.analytics;

import com.project.souklab.model.analytics.AnalyticsBucket;
import com.project.souklab.model.analytics.AnalyticsJobStatus;
import com.project.souklab.model.analytics.AnalyticsOutputFormat;
import com.project.souklab.model.analytics.AnalyticsReportType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value @Builder
public class AnalyticsJobResponse {
    String id;
    AnalyticsReportType reportType;
    AnalyticsJobStatus status;
    AnalyticsBucket bucket;
    LocalDate fromDate;
    LocalDate toDate;
    int pageNumber;
    int pageSize;
    AnalyticsOutputFormat outputFormat;
    LocalDateTime completedAt;
    LocalDateTime expiresAt;
    String failureMessage;
}
