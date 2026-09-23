# Chat & Messaging Controller Package (`com.project.souklab.controller.chat`)

HTTP REST adapter and STOMP WebSocket controller for private direct conversations, cursor-paginated messages, file attachments, and read receipts.

---

## REST Endpoints (`ConversationController`)

All endpoints require authentication (`Authorization: Bearer <accessToken>`).

| Method | Endpoint | Summary | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/conversations` | Create/get conversation | Creates a private conversation with `recipientUserId` or returns existing. |
| `GET` | `/api/v1/conversations` | List conversations | Lists all user conversations, optionally filtered by `?archived=true/false`. |
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

### Destinations
- **Client Subscribe**: `/user/queue/messages` (receives incoming private messages and read receipts)
- **Send Message**: `/app/chat.send`
- **Mark Read**: `/app/chat.read`
- **Typing Indicator**: `/app/chat.typing`
