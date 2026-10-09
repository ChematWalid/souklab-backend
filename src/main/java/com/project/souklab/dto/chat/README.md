# Chat & Messaging DTOs (`com.project.souklab.dto.chat`)

Data Transfer Objects and WebSocket event contracts for realtime 1-on-1 messaging, attachments, and typing indicators.

---

## DTOs Reference

| Class | Type | Responsibility |
| :--- | :--- | :--- |
| [`ConversationResponse`](ConversationResponse.java) | Response | Conversation summary including participants, active status, unread count, last message, `lastReadMessageId` (caller's read position), and `participantLastReadMessageId` (partner's read position). |
| [`CreateConversationRequest`](CreateConversationRequest.java) | Request | Initiates a conversation between a client and an artisan (self-conversations rejected). |
| [`ArchiveConversationRequest`](ArchiveConversationRequest.java) | Request | Payload for archiving or unarchiving a conversation. |
| [`ChatMetadata`](ChatMetadata.java) | Model | Conversation metadata attributes and settings. |
| [`SendMessageRequest`](SendMessageRequest.java) | Request | REST and STOMP message payload (text content, optional attachment references). |
| [`MessageResponse`](MessageResponse.java) | Response | Complete message representation (sender, content, attachments, read state, delivery time). |
| [`MessageAttachmentResponse`](MessageAttachmentResponse.java) | Response | Outbound representation of a message attachment. |
| [`MessagePageResponse`](MessagePageResponse.java) | Response | Paginated message history representation. |
| [`EditMessageRequest`](EditMessageRequest.java) | Request | Modifies text of a previously sent message within the allowed editing window. |
| [`ReadReceiptRequest`](ReadReceiptRequest.java) | Request | Marks conversation messages up to a specific message ID as read. |
| [`TypingCommand`](TypingCommand.java) | Command | Ephemeral typing indicator payload sent over WebSocket. |
| [`ChatEvent`](ChatEvent.java) | Event | Structured envelope broadcast to user queues over STOMP/RabbitMQ. |
| [`ChatEventType`](ChatEventType.java) | Enum | Grouped event taxonomy: `Message` (`CREATED`, `UPDATED`, `DELETED`), `Presence` (`ONLINE`, `OFFLINE`), `Typing` (`STARTED`, `STOPPED`), `Read.UpTo`, and `Command` (`ACKNOWLEDGED`, `ERROR`). |
| [`AttachmentUploadResponse`](AttachmentUploadResponse.java) | Response | Details of uploaded chat attachment (key, MIME type, size). |
