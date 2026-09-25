package com.project.souklab.controller.user;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.user.AvatarResponseDTO;
import com.project.souklab.security.AvatarUploadSizeFilter;
import com.project.souklab.service.user.AvatarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for authenticated user avatar management.
 * Provides multipart avatar upload, quota enforcement, and tier URL resolution.
 */
@RestController
@RequestMapping(AvatarUploadSizeFilter.AVATAR_UPLOAD_URI)
@Tag(name = "User Avatar", description = "Current authenticated user avatar upload, gallery retrieval, activation, and deletion (/api/v1/users/me/avatars)")
@RequiredArgsConstructor
@Slf4j
public class AvatarController {

    private final AvatarService avatarService;

    /**
     * Uploads, processes, and activates a new profile avatar for the currently authenticated user.
     *
     * @param file the multipart avatar image file
     * @return 201 Created containing AvatarResponseDTO with URLs for all resolution tiers
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload new avatar (/api/v1/users/me/avatars)", description = "Uploads, validates, resizes, and activates a new profile avatar for the currently authenticated user. Enforces image size, allowed extension, ClamAV antivirus scanning, and account avatar quotas. Returns AvatarResponseDTO with URLs for all resolution tiers (thumbnail, medium, full).")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        AvatarResponseDTO response = avatarService.uploadAvatar(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Avatar uploaded successfully"));
    }

    /**
     * Retrieves the paginated avatar gallery history for the currently authenticated user.
     *
     * @param pageable pagination and sorting parameters ordered by uploadedAt descending
     * @return 200 OK containing PaginatedResponse of AvatarResponseDTO objects
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get avatar gallery history", description = "Retrieves paginated history of avatars uploaded by the authenticated user, ordered by upload date descending.")
    public ResponseEntity<ApiResponse<PaginatedResponse<AvatarResponseDTO>>> listAvatars(
            @PageableDefault(sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PaginatedResponse<AvatarResponseDTO> response = avatarService.listAvatars(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves a single avatar belonging to the currently authenticated user.
     *
     * @param id the unique identifier of the avatar
     * @return 200 OK with AvatarResponseDTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get single avatar", description = "Retrieves metadata and image URLs for a specific avatar belonging to the authenticated user.")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> getAvatar(@PathVariable String id) {
        AvatarResponseDTO response = avatarService.getAvatar(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Avatar retrieved successfully"));
    }

    /**
     * Deletes a specific avatar belonging to the currently authenticated user.
     *
     * @param id the unique identifier of the avatar to delete
     * @return 200 OK with confirmation message and null data payload
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete avatar", description = "Deletes a specific avatar record and associated storage files belonging to the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> deleteAvatar(@PathVariable String id) {
        avatarService.deleteAvatar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Avatar deleted successfully"));
    }

    /**
     * Activates a previous avatar belonging to the currently authenticated user without re-uploading.
     *
     * @param id the unique identifier of the avatar to activate
     * @return 200 OK containing the activated AvatarResponseDTO
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Activate avatar", description = "Activates a previously uploaded gallery avatar as the primary profile avatar for the authenticated user.")
    public ResponseEntity<ApiResponse<AvatarResponseDTO>> activateAvatar(@PathVariable String id) {
        AvatarResponseDTO response = avatarService.activateAvatar(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Avatar activated successfully"));
    }
}
