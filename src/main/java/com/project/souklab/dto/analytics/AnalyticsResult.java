package com.project.souklab.dto.analytics;

import java.util.Map;
import java.time.LocalDate;
import com.project.souklab.model.analytics.AnalyticsBucket;
import com.project.souklab.model.analytics.AnalyticsReportType;
import com.project.souklab.dto.common.PaginatedResponse;

public record AnalyticsResult(
        AnalyticsReportType reportType,
        LocalDate fromDate,
        LocalDate toDate,
        AnalyticsBucket bucket,
        Map<String, Object> summary,
        PaginatedResponse<Map<String, Object>> series,
        Map<String, PaginatedResponse<Map<String, Object>>> tables) { }
