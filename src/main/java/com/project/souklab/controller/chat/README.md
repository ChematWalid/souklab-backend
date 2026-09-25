# Chat & Messaging Controller Package (`com.project.souklab.controller.chat`)

HTTP REST adapter and STOMP WebSocket controller for private direct conversations, cursor-paginated messages, file attachments, and read receipts.

---

## REST Endpoints (`ConversationController`)

All endpoints require authentication (`Authorization: Bearer <accessToken>`).

> [!IMPORTANT]
> **Client Premium Requirement**: Clients require an active Premium subscription to initiate conversations (`POST /api/v1/conversations`), send messages (`POST /api/v1/conversations/{id}/messages` and STOMP `/app/v1/conversations/{conversationId}/messages.send`), edit messages, upload attachments, or broadcast typing events. Non-premium clients attempting these operations receive `403 Forbidden`. Non-premium clients may view past conversation messages and submit read receipts, but the artisan's name will be masked (`Artisan #XXXXX`). Artisans and administrators are exempt from client subscription checks.

| Method | Endpoint | Summary | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/conversations` | Create/get conversation | Creates a private conversation with `recipientUserId` or returns existing. |
| `GET` | `/api/v1/conversations` | List conversations | Lists all user conversations, optionally filtered by `?archived=true/false`. |
| `GET` | `/api/v1/conversations/{id}` | Get conversation | Retrieves one conversation summary for a participant. |
| `PATCH`| `/api/v1/conversations/{id}/archive` | Archive conversation | Toggles archive state for a conversation (`{ "archived": true/false }`). |
| `GET` | `/api/v1/conversations/{id}/messages` | Get messages | Retrieves cursor-paginated message history (`?cursor=<id>&size=20`). |
| `POST` | `/api/v1/conversations/{id}/messages` | Send message | Sends a text message in the conversation. |
| `PATCH`| `/api/v1/conversations/{conversationId}/messages/{messageId}` | Edit message | Edits an owned message (`{ "content": "..." }`). |
| `DELETE`| `/api/v1/conversations/{conversationId}/messages/{messageId}`| Delete message | Soft-deletes a message from the conversation. |
| `POST` | `/api/v1/conversations/{id}/read` | Mark read | Marks messages as read up to `{ "messageId": "..." }`. |
| `POST` | `/api/v1/conversations/{id}/attachments` | Upload attachment | Multipart upload (`file`) returning attachment metadata and storage URL. |

---

## WebSocket & Realtime STOMP (`ChatStompController`)

### Connect
- **Endpoint**: `/ws` (with SockJS fallback when enabled)
- **STOMP Broker Destination Prefix**: `/topic`, `/queue`
- **Application Destination Prefix**: `/app`

### Inbound Destinations (Client -> Server)
- `/app/v1/conversations/{conversationId}/messages.send`: Send message (`SendMessageRequest`)
- `/app/v1/conversations/{conversationId}/messages.edit`: Edit message (`EditMessageCommand`)
- `/app/v1/conversations/{conversationId}/messages.delete`: Soft-delete message (`MessageCommand`)
- `/app/v1/conversations/{conversationId}/read`: Mark read receipt (`MessageCommand`)
- `/app/v1/conversations/{conversationId}/typing.start`: Typing indicator start (`TypingCommand`)
- `/app/v1/conversations/{conversationId}/typing.stop`: Typing indicator stop (`TypingCommand`)

### Subscriptions (Server -> Client)
- `/user/queue/chat`: Message deliveries, command acknowledgments, errors
- `/user/queue/chat-events`: Real-time typing indicators, read receipts, message updates
- `/topic/presence`: Real-time user online/offline presence broadcasts
- `/user/queue/notifications`: In-app notifications
