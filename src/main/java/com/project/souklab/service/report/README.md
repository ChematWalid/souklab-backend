# Report services

`ContentReportService` validates report targets, persists an auditable queue, notifies administrators, and applies administrator-selected dismiss, hide, or remove actions.

When a user is moderated with the `REMOVE` action:
- The user account status is set to `AccountStatus.DELETED` and `deletedAt` is populated.
- The user's original email is safely migrated to a synthetic alias `deleted+<id>@deleted.souklab.invalid`, freeing the unique email constraint in MariaDB so the original address can be re-registered cleanly.
- Authentication paths filter out soft-deleted users (`findByEmailAndDeletedAtIsNull`), returning `401 Unauthorized`.

| Service | Responsibility |
| --- | --- |
| [`ContentReportService`](ContentReportService.java) | Owns report creation, administrator notification, moderation resolution, soft-delete handling, and audit behavior. |
