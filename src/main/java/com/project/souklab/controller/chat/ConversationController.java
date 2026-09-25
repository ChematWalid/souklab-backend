package com.project.souklab.controller.chat;

import com.project.souklab.dto.chat.*;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.service.chat.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Messaging & Chat", description = "Direct private conversations, cursor-paginated messages, file attachments, and read receipts")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService service;

    @PostMapping
    @Operation(summary = "Create or get conversation", description = "Creates a private direct conversation with the specified recipient user ID, or returns the existing conversation if one already exists.")
    public ResponseEntity<ApiResponse<ConversationResponse>> create(@Valid @RequestBody CreateConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(service.createOrGet(request.recipientUserId()), "Conversation created"));
    }

    @GetMapping
    @Operation(summary = "List conversations", description = "Retrieves all conversations for the authenticated user, optionally filtered by archive status.")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> list(@RequestParam(defaultValue = "false") boolean archived) {
        return ResponseEntity.ok(ApiResponse.success(service.list(archived)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get conversation", description = "Retrieves a conversation summary for one of its participants.")
    public ResponseEntity<ApiResponse<ConversationResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive or unarchive conversation", description = "Toggles archive state for a conversation.")
    public ResponseEntity<ApiResponse<Void>> archive(@PathVariable String id, @Valid @RequestBody ArchiveConversationRequest request) {
        service.archive(id, request.archived()); return ResponseEntity.ok(ApiResponse.success(null, "Conversation archive state updated"));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Get conversation messages", description = "Retrieves messages for a conversation using cursor-based pagination.")
    public ResponseEntity<ApiResponse<MessagePageResponse>> messages(@PathVariable String id, @RequestParam(required = false) String cursor, @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(ApiResponse.success(service.messages(id, cursor, size == null ? 0 : size)));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Send message", description = "Sends a new message in a conversation. Dispatches realtime WebSocket event to participant.")
    public ResponseEntity<ApiResponse<MessageResponse>> send(@PathVariable String id, @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(service.send(id, request), "Message sent"));
    }

    @PatchMapping("/{conversationId}/messages/{messageId}")
    @Operation(summary = "Edit message", description = "Edits the content of an existing sent message owned by the caller.")
    public ResponseEntity<ApiResponse<MessageResponse>> edit(@PathVariable String conversationId, @PathVariable String messageId, @Valid @RequestBody EditMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.edit(conversationId, messageId, request)));
    }

    @DeleteMapping("/{conversationId}/messages/{messageId}")
    @Operation(summary = "Delete message", description = "Soft-deletes a message from a conversation.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String conversationId, @PathVariable String messageId) {
        service.delete(conversationId, messageId); return ResponseEntity.ok(ApiResponse.success(null, "Message deleted"));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark conversation read", description = "Marks conversation messages as read up to the given message ID.")
    public ResponseEntity<ApiResponse<Void>> read(@PathVariable String id, @RequestBody(required = false) ReadReceiptRequest request) {
        service.markRead(id, request == null ? null : request.messageId()); return ResponseEntity.ok(ApiResponse.success(null, "Conversation marked as read"));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload chat attachment", description = "Uploads a multipart file attachment (image or document) for a message in the conversation.")
    public ResponseEntity<ApiResponse<AttachmentUploadResponse>> uploadAttachment(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(service.uploadAttachment(id, file), "Attachment uploaded"));
    }
}
