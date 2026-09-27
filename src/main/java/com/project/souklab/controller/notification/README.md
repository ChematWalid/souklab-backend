# Notification Controller Package (`com.project.souklab.controller.notification`)

Exposes in-app notification management endpoints with user-scoped isolation.

---

## Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/notifications` | Authenticated | Paginated feed of notifications for the caller. Excludes soft-deleted items (`deletedAt IS NULL`). Default: newest-first. |
| `GET` | `/api/v1/notifications/{id}` | Authenticated | Retrieves a single notification by ID. Returns 404 if foreign or deleted. |
| `GET` | `/api/v1/notifications/unread-count` | Authenticated | Returns raw integer count of unread, non-deleted notifications. |
| `PUT` | `/api/v1/notifications/{id}/read` | Authenticated | Marks a single notification as read (`read = true`). Returns updated DTO. Returns 404 if foreign or deleted. |
| `PUT` | `/api/v1/notifications/read-all` | Authenticated | Bulk marks all unread, non-deleted notifications as read for current user. |
| `DELETE` | `/api/v1/notifications/{id}` | Authenticated | Soft-deletes a notification (`deletedAt = now()`). Returns 404 if foreign or deleted. |
| `GET` | `/api/v1/notifications/preferences` | Authenticated | Returns all typed preferences; unspecified categories default to enabled. |
| `PUT` | `/api/v1/notifications/preferences` | Authenticated | Updates up to 33 supported notification type values to booleans; unknown, null, or oversized payloads are rejected before persistence. |
| `POST` | `/api/v1/notifications/preferences/reset` | Authenticated | Removes overrides and restores enabled-by-default behavior. |

Preference map keys are the notification wire values returned by `GET`, for example:

```json
{
  "preferences": {
    "FEED_POST_PUBLISHED": false,
    "NEW_FORMATION": true
  }
}
```

Unknown notification values are rejected with `400 BAD_REQUEST`; omitted values remain enabled by default.

The notifications collection accepts optional `read=true|false`; omitting it preserves the original all-state result.

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`NotificationController`](NotificationController.java) | Implements notification feed, badge count, and read/delete state updates. |
