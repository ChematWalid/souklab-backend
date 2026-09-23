# Analytics Controllers (`com.project.souklab.controller.analytics`)

REST controllers exposing administrative analytics reporting, asynchronous query jobs, CSV export downloads, and rollup maintenance.

---

## Controllers Reference

| Controller | Base Route | Permission Required | Responsibility |
| :--- | :--- | :--- | :--- |
| [`AnalyticsJobController`](AnalyticsJobController.java) | `/api/v1/admin/analytics` & `/api/v1/admin/stats` | `permission:admin:analytics` (operational) / `permission:admin:financials` (financial) | Submits async query jobs, checks execution status, streams CSV export artifacts, and triggers maintenance rebuilds/backfills. |

---

## Key Endpoints

- `POST /api/v1/admin/analytics/jobs` — Enqueue asynchronous analytics query job.
- `GET /api/v1/admin/analytics/jobs/{id}` — Poll execution status and progress.
- `GET /api/v1/admin/analytics/jobs/{id}/download` — Stream generated CSV export file.
- `POST /api/v1/admin/analytics/rollups/rebuild` — Trigger background rebuild of KPI rollups.
- `POST /api/v1/admin/analytics/rollups/backfill` — Trigger bounded historical backfill.
- `GET /api/v1/admin/stats` — High-level platform statistics dashboard endpoint.
