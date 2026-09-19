package com.project.souklab.dto.analytics;

import com.project.souklab.model.analytics.AnalyticsBucket;
import com.project.souklab.model.analytics.AnalyticsOutputFormat;
import com.project.souklab.model.analytics.AnalyticsReportType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class AnalyticsJobRequest {
    @NotNull private AnalyticsReportType reportType;
    @NotNull private LocalDate fromDate;
    @NotNull private LocalDate toDate;
    @NotNull private AnalyticsBucket bucket;
    private Map<String, String> filters;
    private Integer pageNumber;
    private Integer pageSize;
    private String sortField;
    private String sortDirection;
    private AnalyticsOutputFormat outputFormat;
}
