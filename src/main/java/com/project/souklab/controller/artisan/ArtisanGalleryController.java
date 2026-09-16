package com.project.souklab.controller.artisan;

import com.project.souklab.dto.artisan.GalleryImageResponseDTO;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.service.artisan.ArtisanGalleryService;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

/**
 * REST controller for authenticated artisan showcase gallery operations.
 * Handles portfolio image uploads, sequential ordering, retrieval, and deletion.
 */
@RestController
@RequestMapping("/api/v1/artisan/gallery")
@PreAuthorize("@accessControl.canManageArtisanContent(authentication)")
@RequiredArgsConstructor
public class ArtisanGalleryController {

    private final ArtisanGalleryService artisanGalleryService;

    /**
     * Uploads a new portfolio showcase image for the authenticated artisan.
     *
     * @param file multipart image file
     * @param title optional title
     * @param caption optional caption description
     * @return 201 Created with uploaded gallery image DTO
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GalleryImageResponseDTO>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "caption", required = false) String caption
    ) {
        GalleryImageResponseDTO dto = artisanGalleryService.uploadImage(file, title, caption);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(dto, "Gallery image uploaded successfully"));
    }

    /**
     * Retrieves all showcase gallery images belonging to the authenticated artisan.
     *
     * @return 200 OK with list of gallery images
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GalleryImageResponseDTO>>> getMyGallery() {
        List<GalleryImageResponseDTO> gallery = artisanGalleryService.getMyGallery();
        return ResponseEntity.ok(ApiResponse.success(gallery, "Gallery retrieved successfully"));
    }

    /**
     * Reorders the presentation sequence of gallery images for the authenticated artisan.
     *
     * @param imageIds complete list of active gallery image IDs in desired sequence
     * @return 200 OK with success confirmation
     */
    @PutMapping("/order")
    public ResponseEntity<ApiResponse<Void>> reorderGallery(@RequestBody List<String> imageIds) {
        artisanGalleryService.reorderGallery(imageIds);
        return ResponseEntity.ok(ApiResponse.success(null, "Gallery display order updated successfully"));
    }

    /**
     * Deletes a showcase gallery image belonging to the authenticated artisan.
     *
     * @param id the unique identifier of the image to delete
     * @return 200 OK with success confirmation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable("id") String id) {
        artisanGalleryService.deleteImage(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Gallery image deleted successfully"));
    }
}
