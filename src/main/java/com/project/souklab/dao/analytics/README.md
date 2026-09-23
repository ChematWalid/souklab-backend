# Analytics Repositories (`com.project.souklab.dao.analytics`)

Spring Data JPA repositories managing analytics persistence, outbox event queues, and aggregated rollups.

---

## Repositories Reference

| Repository / Projection | Managed Entity | Key Capabilities |
| :--- | :--- | :--- |
| [`ActivityEventRepository`](ActivityEventRepository.java) | `ActivityEvent` | Event log queries, time-bounded filtering, and batch retention pruning. |
| [`AnalyticsJobRepository`](AnalyticsJobRepository.java) | `AnalyticsJob` | Asynchronous query job lifecycle queries, status updates, owner isolation. |
| [`AnalyticsJobArtifactRepository`](AnalyticsJobArtifactRepository.java) | `AnalyticsJobArtifact` | Export file artifact tracking (storage key, content hash, row count). |
| [`AnalyticsMaintenanceJobRepository`](AnalyticsMaintenanceJobRepository.java) | `AnalyticsMaintenanceJob` | Maintenance operation tracking (rebuild, backfill, retention tasks). |
| [`AnalyticsOutboxRepository`](AnalyticsOutboxRepository.java) | `AnalyticsOutboxEvent` | Transactional Outbox pattern polling, status transitions (`PENDING`, `PUBLISHED`, `FAILED`), and retry backoff. |
| [`AnalyticsProcessedEventRepository`](AnalyticsProcessedEventRepository.java) | `AnalyticsProcessedEvent` | Deduplication store ensuring idempotent event processing. |
| [`DailyKpiRollupRepository`](DailyKpiRollupRepository.java) | `DailyKpiRollup` | Date-range aggregated metrics, multi-dimensional slicing, and period summaries. |
| [`AnalyticsDimensionCount`](AnalyticsDimensionCount.java) | Projection | Spring Data JPA projection for dimensional grouping and counts. |
