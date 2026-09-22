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

`NotificationService` saves the notification first. When a transaction synchronization is active, the STOMP push is registered with `afterCommit`; broker failures are logged and do not roll back the database write. The destination is `/user/{username}` plus the configured `app.chat.notification-destination` through the external STOMP relay.

`NEW_MESSAGE` is emitted once for every successfully persisted direct message and targets its conversation. `NEW_REVIEW`, `NEW_REPORT`, and subscription/payment notification values are implemented in the current notification taxonomy. Analytics activity events use the grouped `AnalyticsEvent` enums and are delivered through the transactional outbox rather than user notifications.
