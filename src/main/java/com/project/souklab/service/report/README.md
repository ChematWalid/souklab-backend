# Report services

`ContentReportService` validates report targets, persists an auditable queue, notifies administrators, and applies administrator-selected dismiss, hide, or remove actions.

| Service | Responsibility |
| --- | --- |
| [`ContentReportService`](ContentReportService.java) | Owns report creation, administrator notification, moderation resolution, and audit behavior. |
