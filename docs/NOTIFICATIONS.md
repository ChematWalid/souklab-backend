# Notifications

The implemented notification subsystem persists user-scoped notifications and optionally broadcasts them over STOMP after the surrounding transaction commits.

## HTTP API

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/api/v1/notifications` | Paginated notifications for the authenticated user. |
| `GET` | `/api/v1/notifications/unread-count` | Unread count for the authenticated user. |
| `PUT` | `/api/v1/notifications/{id}/read` | Marks one owned notification as read. |
| `PUT` | `/api/v1/notifications/read-all` | Marks all owned notifications as read. |
| `DELETE` | `/api/v1/notifications/{id}` | Soft-deletes one owned notification. |

All routes require authentication. Repository queries scope by recipient and exclude soft-deleted records, preventing cross-user access.

## Persistence

`Notification` maps to `notifications` and inherits UUID and audit timestamps from `BaseEntity`. It stores the recipient, message, `NotificationType`, optional target identifier, read state, and nullable `deletedAt`.

## Delivery

`NotificationService` saves the notification first. When a transaction synchronization is active, the STOMP push is registered with `afterCommit`; broker failures are logged and do not roll back the database write. The destination is `/user/{username}/queue/notifications` through the configured external STOMP relay.

The enum still contains future-facing values for messaging and payments. `NEW_REVIEW` and `NEW_REPORT` are active Phase 7 event types; messaging and payment values remain reserved until those modules are implemented.
