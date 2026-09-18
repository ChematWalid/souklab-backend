package com.project.souklab.controller.chat;

import com.project.souklab.dto.chat.*;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.service.chat.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> create(@Valid @RequestBody CreateConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(service.createOrGet(request.recipientUserId()), "Conversation created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> list(@RequestParam(defaultValue = "false") boolean archived) {
        return ResponseEntity.ok(ApiResponse.success(service.list(archived)));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<Void>> archive(@PathVariable String id, @Valid @RequestBody ArchiveConversationRequest request) {
        service.archive(id, request.archived()); return ResponseEntity.ok(ApiResponse.success(null, "Conversation archive state updated"));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessagePageResponse>> messages(@PathVariable String id, @RequestParam(required = false) String cursor, @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(ApiResponse.success(service.messages(id, cursor, size == null ? 0 : size)));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> send(@PathVariable String id, @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(service.send(id, request), "Message sent"));
    }

    @PatchMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> edit(@PathVariable String conversationId, @PathVariable String messageId, @Valid @RequestBody EditMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.edit(conversationId, messageId, request)));
    }

    @DeleteMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String conversationId, @PathVariable String messageId) {
        service.delete(conversationId, messageId); return ResponseEntity.ok(ApiResponse.success(null, "Message deleted"));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> read(@PathVariable String id, @RequestBody(required = false) ReadReceiptRequest request) {
        service.markRead(id, request == null ? null : request.messageId()); return ResponseEntity.ok(ApiResponse.success(null, "Conversation marked as read"));
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<AttachmentUploadResponse>> uploadAttachment(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(service.uploadAttachment(id, file), "Attachment uploaded"));
    }
}
