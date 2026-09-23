# Analytics DTOs (`com.project.souklab.dto.analytics`)

Request payloads, response representations, and internal events for administrative analytics query jobs, rollups, and exports.

---

## DTOs Reference

| Class | Type | Responsibility |
| :--- | :--- | :--- |
| [`AnalyticsJobRequest`](AnalyticsJobRequest.java) | Request | Payload to enqueue a query job: report type, metrics, bucket granularity, date ranges, and filters. |
| [`AnalyticsJobResponse`](AnalyticsJobResponse.java) | Response | Response representing job status (`QUEUED`, `RUNNING`, `COMPLETED`, `FAILED`), error details, and artifact download link. |
| [`AnalyticsResult`](AnalyticsResult.java) | Data Contract | Multi-dimensional aggregated time-series results, totals, and summary cards. |
| [`AnalyticsRebuildRequest`](AnalyticsRebuildRequest.java) | Request | Maintenance request specifying rebuild scope or backfill parameters. |
| [`AnalyticsRebuildResponse`](AnalyticsRebuildResponse.java) | Response | Acknowledgment response for accepted maintenance operations. |
| [`AnalyticsJobEvent`](AnalyticsJobEvent.java) | Event | Internal event dispatched upon job completion or failure. |
| [`AnalyticsMaintenanceJobResponse`](AnalyticsMaintenanceJobResponse.java) | Response | Detailed status and execution metadata for background maintenance jobs. |
