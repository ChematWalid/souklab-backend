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
@Table(name = "analytics_jobs", indexes = @Index(name = "idx_analytics_jobs_owner_status", columnList = "owner_id,status,created_at"))
@Getter @Setter @NoArgsConstructor
public class AnalyticsJob extends BaseEntity {
    @Column(name = "owner_id", nullable = false, length = 36) private String ownerId;
    @Enumerated(EnumType.STRING) @Column(name = "report_type", nullable = false, length = 40) private AnalyticsReportType reportType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private AnalyticsJobStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private AnalyticsBucket bucket;
    @Column(name = "from_date", nullable = false) private LocalDate fromDate;
    @Column(name = "range_to_date", nullable = false) private LocalDate toDate;
    @Column(name = "page_number", nullable = false) private int pageNumber;
    @Column(name = "page_size", nullable = false) private int pageSize;
    @Column(name = "sort_field", length = 64) private String sortField;
    @Enumerated(EnumType.STRING) @Column(name = "sort_direction", length = 8) private AnalyticsSortDirection sortDirection;
    @Column(name = "filters_json", columnDefinition = "TEXT") private String filtersJson;
    @Column(name = "permission_scope", columnDefinition = "TEXT", nullable = false) private String permissionScope;
    @Enumerated(EnumType.STRING) @Column(name = "output_format", nullable = false, length = 8) private AnalyticsOutputFormat outputFormat;
    @Column(name = "result_json", columnDefinition = "LONGTEXT") private String resultJson;
    @Column(name = "failure_message", columnDefinition = "TEXT") private String failureMessage;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @Column(name = "expires_at") private LocalDateTime expiresAt;
}
