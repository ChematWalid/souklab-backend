package com.project.souklab.dto.analytics;

import java.util.Map;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.project.souklab.analytics.AnalyticsMetric;
import com.project.souklab.analytics.AnalyticsMetricSummaryKeyDeserializer;
import com.project.souklab.analytics.AnalyticsMetricTableKeyDeserializer;
import com.project.souklab.model.analytics.AnalyticsBucket;
import com.project.souklab.model.analytics.AnalyticsReportType;
import com.project.souklab.dto.common.PaginatedResponse;

public record AnalyticsResult(
        AnalyticsReportType reportType,
        LocalDate fromDate,
        LocalDate toDate,
        AnalyticsBucket bucket,
        @JsonDeserialize(keyUsing = AnalyticsMetricSummaryKeyDeserializer.class)
        Map<AnalyticsMetric.Key, Object> summary,
        PaginatedResponse<Map<AnalyticsMetric.Series, Object>> series,
        @JsonDeserialize(keyUsing = AnalyticsMetricTableKeyDeserializer.class)
        Map<AnalyticsMetric.Key, PaginatedResponse<Map<AnalyticsMetric.Csv, Object>>> tables) { }
