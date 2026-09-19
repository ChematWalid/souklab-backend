package com.project.souklab.controller.analytics;

import com.project.souklab.analytics.AnalyticsJobService;
import com.project.souklab.analytics.AnalyticsMaintenanceJobService;
import com.project.souklab.dto.analytics.AnalyticsRebuildRequest;
import com.project.souklab.dto.analytics.AnalyticsJobRequest;
import com.project.souklab.dto.analytics.AnalyticsJobResponse;
import com.project.souklab.dto.analytics.AnalyticsMaintenanceJobResponse;
import com.project.souklab.dto.analytics.AnalyticsResult;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.model.analytics.AnalyticsMaintenanceOperation;
import com.project.souklab.security.Permission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.nio.charset.StandardCharsets;

/** Asynchronous, owner-scoped administrator analytics API. */
@RestController
@RequestMapping({"/api/v1/admin/analytics", "/api/v1/admin/stats"})
@PreAuthorize("@accessControl.canViewAnalytics(authentication)")
@Tag(name = "Admin analytics", description = "Asynchronous, owner-scoped analytics jobs and historical rollups")
@RequiredArgsConstructor
public class AnalyticsJobController {
    private final AnalyticsJobService service;
    private final AnalyticsMaintenanceJobService maintenanceJobs;

    @PostMapping("/rollups/jobs/rebuild")
    @Operation(summary = "Queue a rollup rebuild", description = "Queues a bounded, owner-scoped rollup rebuild and returns its persisted job status.")
    public ResponseEntity<ApiResponse<AnalyticsMaintenanceJobResponse>> queueRebuild(
            @Valid @RequestBody AnalyticsRebuildRequest request, Authentication authentication) {
        return ResponseEntity.accepted().body(ApiResponse.success(
                maintenanceJobs.submit(AnalyticsMaintenanceOperation.REBUILD, request, authentication.getName()),
                "Analytics rollup rebuild queued."));
    }

    @PostMapping("/rollups/jobs/backfill")
    @Operation(summary = "Queue historical backfill", description = "Queues a bounded, owner-scoped historical backfill and returns its persisted job status.")
    public ResponseEntity<ApiResponse<AnalyticsMaintenanceJobResponse>> queueBackfill(
            @Valid @RequestBody AnalyticsRebuildRequest request, Authentication authentication) {
        return ResponseEntity.accepted().body(ApiResponse.success(
                maintenanceJobs.submit(AnalyticsMaintenanceOperation.BACKFILL, request, authentication.getName()),
                "Historical analytics backfill queued."));
    }

    @GetMapping("/rollups/jobs/{id}")
    @Operation(summary = "Get maintenance job status", description = "Returns status for an owned rebuild or backfill job.")
    public ResponseEntity<ApiResponse<AnalyticsMaintenanceJobResponse>> maintenanceStatus(
            @PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceJobs.get(id, authentication.getName())));
    }

    @PostMapping("/rollups/rebuild")
    @Operation(summary = "Queue a rollup rebuild (compatibility alias)", description = "Queues the asynchronous rollup rebuild endpoint under the legacy path.")
    public ResponseEntity<ApiResponse<AnalyticsMaintenanceJobResponse>> rebuild(
            @Valid @RequestBody AnalyticsRebuildRequest request, Authentication authentication) {
        return ResponseEntity.accepted().body(ApiResponse.success(
                maintenanceJobs.submit(AnalyticsMaintenanceOperation.REBUILD, request, authentication.getName()),
                "Analytics rollup rebuild queued."));
    }

    @PostMapping("/rollups/backfill")
    @Operation(summary = "Queue historical backfill (compatibility alias)", description = "Queues the asynchronous historical backfill endpoint under the legacy path.")
    public ResponseEntity<ApiResponse<AnalyticsMaintenanceJobResponse>> backfill(
            @Valid @RequestBody AnalyticsRebuildRequest request, Authentication authentication) {
        return ResponseEntity.accepted().body(ApiResponse.success(
                maintenanceJobs.submit(AnalyticsMaintenanceOperation.BACKFILL, request, authentication.getName()),
                "Historical analytics backfill queued."));
    }

    @PostMapping("/jobs")
    @Operation(summary = "Submit an analytics job", description = "Queues a JSON or CSV report. Financial reports require the financial administrator permission.")
    public ResponseEntity<ApiResponse<AnalyticsJobResponse>> submit(@Valid @RequestBody AnalyticsJobRequest request, Authentication authentication) {
        boolean financial = authentication.getAuthorities().stream().anyMatch(Permission.Financial.ADMIN::matches);
        return ResponseEntity.accepted().body(ApiResponse.success(service.submit(request, authentication.getName(), financial), "Analytics job queued."));
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Get analytics job status", description = "Returns status and failure metadata for an owned job.")
    public ResponseEntity<ApiResponse<AnalyticsJobResponse>> status(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id, authentication.getName())));
    }

    @GetMapping("/jobs/{id}/result")
    @Operation(summary = "Fetch an analytics result", description = "Returns the paginated result after the asynchronous job completes.")
    public ResponseEntity<ApiResponse<AnalyticsResult>> result(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(service.result(id, authentication.getName())));
    }

    @GetMapping("/jobs/{id}/download")
    @Operation(summary = "Download an analytics result", description = "Downloads an owned JSON or CSV artifact using the permission scope captured at submission.")
    public ResponseEntity<byte[]> download(@PathVariable String id, Authentication authentication) {
        var download = service.download(id, authentication.getName());
        byte[] content = download.content().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(download.csv() ? MediaType.parseMediaType("text/csv") : MediaType.APPLICATION_JSON);
        headers.setContentDisposition(ContentDisposition.attachment().filename("analytics-" + id + (download.csv() ? ".csv" : ".json")).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Delete an analytics job", description = "Deletes an owned job and its stored artifact when available.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id, Authentication authentication) {
        service.delete(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Analytics job deleted."));
    }

}
