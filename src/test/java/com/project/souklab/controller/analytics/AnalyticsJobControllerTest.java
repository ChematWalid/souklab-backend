package com.project.souklab.controller.analytics;

import java.time.LocalDate;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.project.souklab.analytics.AnalyticsJobService;
import com.project.souklab.analytics.AnalyticsMaintenanceJobService;
import com.project.souklab.controller.support.ControllerSliceTest;
import com.project.souklab.dto.analytics.AnalyticsJobResponse;
import com.project.souklab.dto.analytics.AnalyticsMaintenanceJobResponse;
import com.project.souklab.model.analytics.AnalyticsMaintenanceOperation;
import com.project.souklab.model.analytics.AnalyticsBucket;
import com.project.souklab.model.analytics.AnalyticsJobStatus;
import com.project.souklab.model.analytics.AnalyticsReportType;
import com.project.souklab.model.analytics.AnalyticsOutputFormat;
import com.project.souklab.security.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ControllerSliceTest(controllers = AnalyticsJobController.class)
class AnalyticsJobControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsJobService jobService;
    @MockitoBean
    private AnalyticsMaintenanceJobService maintenanceJobs;

    @Test
    void statsAliasReturnsOwnerScopedJobStatus() throws Exception {
        when(jobService.get(eq("job-1"), eq("analytics@example.com")))
                .thenReturn(response());

        mockMvc.perform(get("/api/v1/admin/stats/jobs/job-1")
                        .with(financialAnalyticsUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("job-1"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void analyticsSubmissionPassesFinancialScopeToService() throws Exception {
        when(jobService.submit(any(), eq("analytics@example.com"), eq(false)))
                .thenReturn(response());

        mockMvc.perform(post("/api/v1/admin/analytics/jobs")
                        .with(analyticsUser())
                        .contentType("application/json")
                        .content("{\"reportType\":\"OVERVIEW\",\"fromDate\":\"2026-01-01\","
                                + "\"toDate\":\"2026-01-01\",\"bucket\":\"DAY\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.id").value("job-1"));

        verify(jobService).submit(any(), eq("analytics@example.com"), eq(false));
    }

    @Test
    void rebuildIsQueuedAsAnOwnerScopedMaintenanceJob() throws Exception {
        when(maintenanceJobs.submit(eq(AnalyticsMaintenanceOperation.REBUILD), any(),
                eq("analytics@example.com"))).thenReturn(maintenanceResponse());

        mockMvc.perform(post("/api/v1/admin/analytics/rollups/jobs/rebuild")
                        .with(analyticsUser())
                        .contentType("application/json")
                        .content("{\"fromDate\":\"2026-01-01\",\"toDate\":\"2026-01-02\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.id").value("maintenance-1"))
                .andExpect(jsonPath("$.data.status").value("QUEUED"));

        verify(maintenanceJobs).submit(eq(AnalyticsMaintenanceOperation.REBUILD), any(),
                eq("analytics@example.com"));
    }

    @Test
    void legacyBackfillAliasAlsoQueuesAnOwnerScopedMaintenanceJob() throws Exception {
        when(maintenanceJobs.submit(eq(AnalyticsMaintenanceOperation.BACKFILL), any(),
                eq("analytics@example.com"))).thenReturn(maintenanceResponse());

        mockMvc.perform(post("/api/v1/admin/stats/rollups/backfill")
                        .with(analyticsUser())
                        .contentType("application/json")
                        .content("{\"fromDate\":\"2026-01-01\",\"toDate\":\"2026-01-02\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.id").value("maintenance-1"));

        verify(maintenanceJobs).submit(eq(AnalyticsMaintenanceOperation.BACKFILL), any(),
                eq("analytics@example.com"));
    }

    private AnalyticsMaintenanceJobResponse maintenanceResponse() {
        return AnalyticsMaintenanceJobResponse.builder().id("maintenance-1")
                .operation(AnalyticsMaintenanceOperation.REBUILD).status(AnalyticsJobStatus.QUEUED)
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 1, 2)).build();
    }

    private AnalyticsJobResponse response() {
        return AnalyticsJobResponse.builder().id("job-1").reportType(AnalyticsReportType.OVERVIEW)
                .status(AnalyticsJobStatus.COMPLETED).bucket(AnalyticsBucket.DAY)
                .pageNumber(0).pageSize(20).outputFormat(AnalyticsOutputFormat.JSON).build();
    }

    private RequestPostProcessor analyticsUser() {
        return user("analytics@example.com")
                .authorities(new SimpleGrantedAuthority(Permission.Analytics.ADMIN.value()));
    }

    private RequestPostProcessor financialAnalyticsUser() {
        return user("analytics@example.com")
                .authorities(new SimpleGrantedAuthority(Permission.Analytics.ADMIN.value()),
                        new SimpleGrantedAuthority(Permission.Financial.ADMIN.value()));
    }
}
