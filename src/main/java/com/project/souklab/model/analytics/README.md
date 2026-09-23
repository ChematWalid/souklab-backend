# Analytics Model Layer (`com.project.souklab.model.analytics`)

JPA entity models, enums, converters, and deserializers supporting platform analytics, transactional outbox queuing, and KPI rollups.

---

## Entities & Enums Reference

### Entities
| Class | Type | Description |
| :--- | :---: | :--- |
| [`ActivityEvent`](ActivityEvent.java) | `@Entity` | Raw tracked domain activity event with timestamp, actor, category, and metadata payload. |
| [`AnalyticsJob`](AnalyticsJob.java) | `@Entity` | Asynchronous query job submitted by an administrator (parameters, execution status, creator). |
| [`AnalyticsJobArtifact`](AnalyticsJobArtifact.java) | `@Entity` | Generated export artifact (CSV stored in S3/MinIO) linked to a parent job. |
| [`AnalyticsMaintenanceJob`](AnalyticsMaintenanceJob.java) | `@Entity` | Administrative maintenance task record (rebuild, backfill, or retention cleanup). |
| [`AnalyticsOutboxEvent`](AnalyticsOutboxEvent.java) | `@Entity` | Transactional Outbox record ensuring reliable event delivery to the message broker. |
| [`AnalyticsProcessedEvent`](AnalyticsProcessedEvent.java) | `@Entity` | Idempotency log recording already-processed event identifiers to prevent duplicates. |
| [`DailyKpiRollup`](DailyKpiRollup.java) | `@Entity` | Pre-calculated daily KPI rollup storing aggregated metrics across dimensions. |

### Enums & Deserializers
| Class / Enum | Responsibility |
| :--- | :--- |
| [`AnalyticsBucket`](AnalyticsBucket.java) | Aggregation time buckets: `DAY`, `WEEK`, `MONTH`, `QUARTER`. |
| [`AnalyticsJobStatus`](AnalyticsJobStatus.java) | Query job lifecycle states: `QUEUED`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`. |
| [`AnalyticsMaintenanceOperation`](AnalyticsMaintenanceOperation.java) | Maintenance types: `REBUILD`, `BACKFILL`, `RETENTION_PRUNE`. |
| [`AnalyticsOutputFormat`](AnalyticsOutputFormat.java) | Supported export formats: `JSON`, `CSV`. |
| [`AnalyticsReportType`](AnalyticsReportType.java) | Report classification (operational vs financial metric sets). |
| [`AnalyticsSortField`](AnalyticsSortField.java) & [`AnalyticsSortDirection`](AnalyticsSortDirection.java) | Sorting criteria for analytics time-series queries. |
| [`OutboxStatus`](OutboxStatus.java) | Outbox event delivery states: `PENDING`, `PROCESSING`, `PUBLISHED`, `FAILED`, `DEAD_LETTER`. |
