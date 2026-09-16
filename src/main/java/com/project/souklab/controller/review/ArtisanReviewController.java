package com.project.souklab.controller.review;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.review.ArtisanReviewRequestDTO;
import com.project.souklab.dto.review.ArtisanReviewResponseDTO;
import com.project.souklab.service.review.ArtisanReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Formation review endpoints and public artisan review listing.
 */
@RestController
@RequiredArgsConstructor
public class ArtisanReviewController {

    private final ArtisanReviewService reviewService;

    /**
     * Lists visible reviews for an artisan.
     *
     * @param artisanId artisan identifier
     * @param pageable pagination configuration
     * @return visible reviews
     */
    @GetMapping("/api/v1/artisans/{artisanId}/reviews")
    public ResponseEntity<ApiResponse<Page<ArtisanReviewResponseDTO>>> list(
            @PathVariable String artisanId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.list(artisanId, pageable)));
    }

    /**
     * Creates a review for an attended formation.
     *
     * @param formationId formation identifier
     * @param request review payload
     * @return created review
     */
    @PostMapping("/api/v1/artisan/formations/{formationId}/reviews")
    @PreAuthorize("@accessControl.canManageArtisanReviews(authentication)")
    public ResponseEntity<ApiResponse<ArtisanReviewResponseDTO>> create(
            @PathVariable String formationId,
            @Valid @RequestBody ArtisanReviewRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(reviewService.create(formationId, request), "Review submitted successfully."));
    }

    /**
     * Updates the current artisan's review.
     *
     * @param reviewId review identifier
     * @param request review payload
     * @return updated review
     */
    @PutMapping("/api/v1/artisan/reviews/{reviewId}")
    @PreAuthorize("@accessControl.canManageArtisanReviews(authentication)")
    public ResponseEntity<ApiResponse<ArtisanReviewResponseDTO>> update(
            @PathVariable String reviewId,
            @Valid @RequestBody ArtisanReviewRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.update(reviewId, request), "Review updated successfully."));
    }

    /**
     * Removes the current artisan's review.
     *
     * @param reviewId review identifier
     * @return empty success response
     */
    @DeleteMapping("/api/v1/artisan/reviews/{reviewId}")
    @PreAuthorize("@accessControl.canManageArtisanReviews(authentication)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String reviewId) {
        reviewService.delete(reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Review removed successfully."));
    }
}
