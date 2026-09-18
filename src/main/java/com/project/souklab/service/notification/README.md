# Notification Service Package (`com.project.souklab.service.notification`)

Coordinates in-app notification records, badge counters, and realtime WebSocket push dispatch.

---

## Dual-Dispatch Mechanism

```mermaid
sequenceDiagram
    participant Domain as Domain Service
    participant NotifService as NotificationService
    participant DB as MariaDB (JPA)
    participant STOMP as SimpMessagingTemplate (RabbitMQ)
    participant Client as Web/Mobile Client (STOMP)

    Domain->>NotifService: createForUser(recipient, message, type, targetId)
    NotifService->>DB: save(Notification)
    NotifService->>STOMP: register afterCommit delivery
NotifService->>STOMP: convertAndSendToUser(recipientEmail, configured notification destination, DTO)
    STOMP-->>Client: Realtime Push Notification
```

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`NotificationService`](NotificationService.java) | Handles notification persistence, paginated feeds excluding soft-deleted items (`deletedAt IS NULL`), unread counts, query-scoped mark-read, bulk mark-all-read, soft-delete updates, and post-commit STOMP delivery. |
| [`RealtimeNotificationAfterCommit`](RealtimeNotificationAfterCommit.java) | Named transaction synchronization that sends a persisted notification over STOMP after a successful commit. |
