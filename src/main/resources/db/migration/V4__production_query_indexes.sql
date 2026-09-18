-- Query-supporting indexes for active conversation/message and upload lookups.
-- This migration is forward-only; removal requires a separately reviewed migration.
CREATE INDEX idx_messages_conversation_active_created
    ON messages (conversation_id, deleted_at, created_at, id);

CREATE INDEX idx_message_uploads_owner_conversation_active
    ON message_attachment_uploads (owner_id, conversation_id, used_at, deleted_at);
