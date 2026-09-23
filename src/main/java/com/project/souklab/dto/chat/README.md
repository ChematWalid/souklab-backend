# Chat & Messaging DTOs (`com.project.souklab.dto.chat`)

Data Transfer Objects and WebSocket event contracts for realtime 1-on-1 messaging, attachments, and typing indicators.

---

## DTOs Reference

| Class | Type | Responsibility |
| :--- | :--- | :--- |
| [`ConversationResponse`](ConversationResponse.java) | Response | Conversation summary including participants, active status, unread count, and last message. |
| [`CreateConversationRequest`](CreateConversationRequest.java) | Request | Initiates a conversation between a client and an artisan (self-conversations rejected). |
| [`SendMessageRequest`](SendMessageRequest.java) | Request | REST and STOMP message payload (text content, optional attachment references). |
| [`MessageResponse`](MessageResponse.java) | Response | Complete message representation (sender, content, attachments, read state, delivery time). |
| [`MessagePageResponse`](MessagePageResponse.java) | Response | Paginated message history representation. |
| [`EditMessageRequest`](EditMessageRequest.java) | Request | Modifies text of a previously sent message within the allowed editing window. |
| [`ReadReceiptRequest`](ReadReceiptRequest.java) | Request | Marks conversation messages up to a specific message ID as read. |
| [`TypingCommand`](TypingCommand.java) | Command | Ephemeral typing indicator payload sent over WebSocket. |
| [`ChatEvent`](ChatEvent.java) | Event | Structured envelope broadcast to user queues over STOMP/RabbitMQ. |
| [`ChatEventType`](ChatEventType.java) | Enum | Event taxonomy: `MESSAGE_SENT`, `MESSAGE_EDITED`, `MESSAGE_DELETED`, `MESSAGE_READ`, `TYPING_STARTED`, `TYPING_STOPPED`. |
| [`AttachmentUploadResponse`](AttachmentUploadResponse.java) | Response | Details of uploaded chat attachment (key, MIME type, size). |
