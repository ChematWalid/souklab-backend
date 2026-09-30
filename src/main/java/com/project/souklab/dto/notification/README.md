# Notification DTO Package (`com.project.souklab.dto.notification`)

Contracts for in-app notification feeds and realtime dispatch envelopes.

---

## Schema Reference

```json
{
  "id": "2bf04de5-2c7d-4f2c-afe6-c7b7636a1e82",
  "message": "Your account has been approved and is now active!",
  "type": "ACCOUNT_VALIDATED",
  "read": false,
  "targetId": "cabf215a-3aa1-412a-b592-c1715291236b",
  "createdAt": "2026-09-05T18:25:12.664714"
}
```

---

## Classes Reference

| Class | Description |
| :--- | :--- |
| [`NotificationResponseDTO`](NotificationResponseDTO.java) | Outbound representation of a user notification containing unique ID, localized message, `NotificationType`, read boolean, target entity reference ID, and timestamp. |
| [`NotificationPreferencesRequest`](NotificationPreferencesRequest.java) | Inbound payload for configuring user notification channel delivery preferences and opt-outs. |
