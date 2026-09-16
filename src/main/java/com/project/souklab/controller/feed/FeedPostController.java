package com.project.souklab.controller.feed;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.feed.FeedPostCreateDTO;
import com.project.souklab.dto.feed.FeedPostMediaResponseDTO;
import com.project.souklab.dto.feed.FeedPostResponseDTO;
import com.project.souklab.model.FeedPostType;
import com.project.souklab.service.feed.FeedPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Public feed and authenticated post management endpoints.
 */
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedPostController {

    private final FeedPostService feedPostService;

    /**
     * Lists publicly published feed posts.
     *
     * @param type optional type filter
     * @param pageable pagination configuration
     * @return published posts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FeedPostResponseDTO>>> list(
            @RequestParam(required = false) FeedPostType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(feedPostService.listPublic(type, pageable)));
    }

    /**
     * Retrieves one published feed post.
     *
     * @param id post identifier
     * @return published post
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedPostResponseDTO>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(feedPostService.getPublic(id)));
    }

    /**
     * Submits a feed post for moderation.
     *
     * @param request post payload
     * @return created pending post
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FeedPostResponseDTO>> create(@Valid @RequestBody FeedPostCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(feedPostService.create(request), "Feed post submitted for moderation."));
    }

    /**
     * Updates an owned feed post.
     *
     * @param id post identifier
     * @param request post payload
     * @return updated post
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FeedPostResponseDTO>> update(@PathVariable String id, @Valid @RequestBody FeedPostCreateDTO request) {
        return ResponseEntity.ok(ApiResponse.success(feedPostService.update(id, request), "Feed post updated successfully."));
    }

    /**
     * Removes an owned feed post.
     *
     * @param id post identifier
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable String id) {
        feedPostService.remove(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Feed post removed successfully."));
    }

    /**
     * Uploads an image attachment to a post.
     *
     * @param id post identifier
     * @param file image payload
     * @return stored attachment
     */
    @PostMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FeedPostMediaResponseDTO>> addMedia(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(feedPostService.addMedia(id, file), "Feed media uploaded successfully."));
    }

    /**
     * Removes an image attachment from a post.
     *
     * @param id post identifier
     * @param mediaId attachment identifier
     * @return empty success response
     */
    @DeleteMapping("/{id}/media/{mediaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeMedia(@PathVariable String id, @PathVariable String mediaId) {
        feedPostService.removeMedia(id, mediaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Feed media removed successfully."));
    }

}
