package com.project.souklab.service.review;

import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.ArtisanReviewRepository;
import com.project.souklab.dao.FormationEnrollmentRepository;
import com.project.souklab.dto.review.ArtisanReviewRequestDTO;
import com.project.souklab.dto.review.ArtisanReviewResponseDTO;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanReview;
import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.FormationStatus;
import com.project.souklab.model.ReviewStatus;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.model.NotificationType;
import com.project.souklab.util.ArtisanSecurityUtils;
import com.project.souklab.security.Permission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Coordinates formation-backed artisan reviews and aggregate rating updates.
 */
@Service
@RequiredArgsConstructor
public class ArtisanReviewService {

    private final ArtisanReviewRepository reviewRepository;
    private final FormationEnrollmentRepository enrollmentRepository;
    private final ArtisanRepository artisanRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    /**
     * Lists visible reviews for an artisan.
     *
     * @param artisanId artisan identifier
     * @param pageable pagination configuration
     * @return visible reviews
     */
    @Transactional(readOnly = true)
    public Page<ArtisanReviewResponseDTO> list(String artisanId, Pageable pageable) {
        return reviewRepository.findByArtisanIdAndStatusAndDeletedAtIsNull(artisanId, ReviewStatus.PUBLISHED, pageable)
                .map(ArtisanReviewResponseDTO::from);
    }

    /**
     * Creates a review for an attended, completed formation enrollment.
     *
     * @param formationId formation identifier
     * @param request rating and comment
     * @return created review
     */
    @Transactional
    public ArtisanReviewResponseDTO create(String formationId, @Valid ArtisanReviewRequestDTO request) {
        Artisan reviewer = currentArtisan();
        var enrollment = enrollmentRepository.findByFormationIdAndArtisanId(formationId, reviewer.getId())
                .orElseThrow(() -> new ForbiddenException("Only enrolled artisans may submit a review."));
        if (enrollment.getStatus() != EnrollmentStatus.ATTENDED
                || enrollment.getFormation().getStatus() != FormationStatus.COMPLETED) {
            throw new ForbiddenException("Reviews require an attended completed formation.");
        }
        Artisan subject = enrollment.getFormation().getAuthor();
        if (subject == null || subject.getId().equals(reviewer.getId())) {
            throw new ConflictException("You cannot review your own formation.");
        }
        if (reviewRepository.findByEnrollmentId(enrollment.getId()).isPresent()) {
            throw new ConflictException("A review already exists for this enrollment.");
        }

        ArtisanReview review = ArtisanReview.builder()
                .reviewer(reviewer)
                .artisan(subject)
                .enrollment(enrollment)
                .rating(normalizeRating(request.getRating()))
                .comment(request.getComment().trim())
                .status(ReviewStatus.PUBLISHED)
                .build();
        ArtisanReview saved = reviewRepository.save(review);
        recalculate(subject);
        notificationService.createForUser(subject.getUser(), "You received a new artisan review.", NotificationType.NEW_REVIEW, saved.getId());
        return ArtisanReviewResponseDTO.from(saved);
    }

    /**
     * Updates a review owned by the current artisan.
     *
     * @param reviewId review identifier
     * @param request new rating and comment
     * @return updated review
     */
    @Transactional
    public ArtisanReviewResponseDTO update(String reviewId, @Valid ArtisanReviewRequestDTO request) {
        Artisan reviewer = currentArtisan();
        ArtisanReview review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));
        if (!review.getReviewer().getId().equals(reviewer.getId())) {
            throw new ForbiddenException("You may only edit your own review.");
        }
        if (review.getStatus() == ReviewStatus.REMOVED) {
            throw new ConflictException("Removed reviews cannot be edited.");
        }
        review.setRating(normalizeRating(request.getRating()));
        review.setComment(request.getComment().trim());
        ArtisanReview saved = reviewRepository.save(review);
        recalculate(review.getArtisan());
        return ArtisanReviewResponseDTO.from(saved);
    }

    /**
     * Removes a review owned by the current artisan and recalculates aggregates.
     *
     * @param reviewId review identifier
     */
    @Transactional
    public void delete(String reviewId) {
        Artisan reviewer = currentArtisan();
        ArtisanReview review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));
        if (!review.getReviewer().getId().equals(reviewer.getId())) {
            throw new ForbiddenException("You may only remove your own review.");
        }
        review.setStatus(ReviewStatus.REMOVED);
        review.setDeletedAt(LocalDateTime.now(clock));
        reviewRepository.save(review);
        recalculate(review.getArtisan());
    }

    private void recalculate(Artisan artisan) {
        BigDecimal average = reviewRepository.averageRating(artisan.getId(), ReviewStatus.PUBLISHED);
        long count = reviewRepository.countByArtisanIdAndStatusAndDeletedAtIsNull(artisan.getId(), ReviewStatus.PUBLISHED);
        artisan.setRating(average == null ? 0.0 : average.setScale(2, RoundingMode.HALF_UP).doubleValue());
        artisan.setReviewsCount((int) count);
        artisanRepository.save(artisan);
    }

    private Artisan currentArtisan() {
        return ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository, Permission.ARTISAN_REVIEWS);
    }

    private BigDecimal normalizeRating(BigDecimal rating) {
        return rating.setScale(2, RoundingMode.HALF_UP);
    }
}
