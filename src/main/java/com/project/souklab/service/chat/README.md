# Direct messaging

The chat module owns private one-to-one conversations, participant archive/read state,
messages, validated attachment reservations, idempotency, and realtime event dispatch.
REST resources live under `/api/v1/conversations`. STOMP commands use the configured
application prefix and `/v1/conversations/{id}/...` paths. All message mutations recheck
account status, email verification, participant ownership, and `permission:message:send`.
For client accounts, initiating conversations, sending/editing messages, uploading attachments,
and sending typing indicators strictly require an active Premium subscription (`403 Forbidden` if not).
In conversation responses viewed by non-premium clients, the artisan's display name is automatically
masked as `Artisan #XXXXX` (using the uppercase suffix of the artisan ID). `ConversationResponse`
tracks bidirectional read progress with `lastReadMessageId` (caller's read position) and
`participantLastReadMessageId` (partner's read position).

## Services & Components

| Class | Responsibility |
| :--- | :--- |
| [`ConversationService`](ConversationService.java) | Core application service orchestrating 1-on-1 conversations, message exchange, attachment validation, read receipts, and archive state. |
| [`ChatPresenceService`](ChatPresenceService.java) | Tracks active WebSocket sessions and delivers real-time online/offline presence notifications strictly to active conversation partners via private user queues. |
| [`MessageCursor`](MessageCursor.java) | Cursor-based pagination token helper for scrolling message history. |
