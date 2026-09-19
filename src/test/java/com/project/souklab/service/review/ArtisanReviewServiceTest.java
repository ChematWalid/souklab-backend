package com.project.souklab.service.review;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.security.Permission;

import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.ArtisanReviewRepository;
import com.project.souklab.dao.FormationEnrollmentRepository;
import com.project.souklab.dto.review.ArtisanReviewRequestDTO;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.FormationEnrollment;
import com.project.souklab.model.ReviewStatus;
import com.project.souklab.model.ArtisanReview;
import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationStatus;
import com.project.souklab.model.User;
import com.project.souklab.service.notification.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies review eligibility, duplicate prevention, and rating aggregation behavior.
 */
@ExtendWith(MockitoExtension.class)
class ArtisanReviewServiceTest {

    @Mock private ArtisanReviewRepository reviewRepository;
    @Mock private FormationEnrollmentRepository enrollmentRepository;
    @Mock private ArtisanRepository artisanRepository;
    @Mock private NotificationService notificationService;

    private ArtisanReviewService service;
    private Artisan reviewer;
    private Artisan subject;
    private Formation formation;
    private FormationEnrollment enrollment;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        service = new ArtisanReviewService(reviewRepository, enrollmentRepository, artisanRepository, notificationService, clock);
        User reviewerUser = User.builder().email("reviewer@example.com").firstName("A").lastName("Reviewer").build();
        reviewerUser.setId("reviewer-user");
        reviewer = Artisan.builder().id("reviewer").user(reviewerUser).build();
        User subjectUser = User.builder().email("subject@example.com").firstName("B").lastName("Subject").build();
        subjectUser.setId("subject-user");
        subject = Artisan.builder().id("subject").user(subjectUser).build();
        formation = Formation.builder().author(subject).status(FormationStatus.COMPLETED).build();
        formation.setId("formation");
        enrollment = FormationEnrollment.builder()
                .formation(formation).artisan(reviewer).status(EnrollmentStatus.ATTENDED).build();
        enrollment.setId("enrollment");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "reviewer@example.com", "credentials", List.of(Permission.Artisan.REVIEWS)));
        when(artisanRepository.findByUserEmailIgnoreCase("reviewer@example.com")).thenReturn(Optional.of(reviewer));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsReviewAndRecalculatesAggregate() {
        when(enrollmentRepository.findByFormationIdAndArtisanId("formation", "reviewer")).thenReturn(Optional.of(enrollment));
        when(reviewRepository.findByEnrollmentId("enrollment")).thenReturn(Optional.empty());
        when(reviewRepository.save(any(ArtisanReview.class))).thenAnswer(invocation -> {
            ArtisanReview review = invocation.getArgument(0);
            review.setId("review");
            return review;
        });
        when(reviewRepository.averageRating("subject", ReviewStatus.PUBLISHED)).thenReturn(new BigDecimal("4.25"));
        when(reviewRepository.countByArtisanIdAndStatusAndDeletedAtIsNull("subject", ReviewStatus.PUBLISHED)).thenReturn(1L);

        var result = service.create("formation", new ArtisanReviewRequestDTO(new BigDecimal("4.2"), "Excellent"));

        assertThat(result.getRating()).isEqualByComparingTo("4.20");
        assertThat(subject.getRating()).isEqualTo(4.25);
        assertThat(subject.getReviewsCount()).isEqualTo(1);
        verify(artisanRepository).save(subject);
    }

    @Test
    void rejectsDuplicateReview() {
        when(enrollmentRepository.findByFormationIdAndArtisanId("formation", "reviewer")).thenReturn(Optional.of(enrollment));
        when(reviewRepository.findByEnrollmentId("enrollment")).thenReturn(Optional.of(new ArtisanReview()));

        assertThatThrownBy(() -> service.create("formation", new ArtisanReviewRequestDTO(BigDecimal.ONE, "Duplicate")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsReviewBeforeAttendance() {
        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        when(enrollmentRepository.findByFormationIdAndArtisanId("formation", "reviewer")).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> service.create("formation", new ArtisanReviewRequestDTO(BigDecimal.ONE, "Too early")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void listsAndUpdatesOwnedReviewWithRoundedRating() {
        ArtisanReview review = ArtisanReview.builder().reviewer(reviewer).artisan(subject)
                .enrollment(enrollment).rating(new BigDecimal("3.50")).comment("old").status(ReviewStatus.PUBLISHED).build();
        review.setId("review");
        when(reviewRepository.findByArtisanIdAndStatusAndDeletedAtIsNull("subject", ReviewStatus.PUBLISHED, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(review)));
        assertThat(service.list("subject", PageRequest.of(0, 10))).hasSize(1);
        when(reviewRepository.findByIdAndDeletedAtIsNull("review")).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(ArtisanReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.averageRating("subject", ReviewStatus.PUBLISHED)).thenReturn(null);
        when(reviewRepository.countByArtisanIdAndStatusAndDeletedAtIsNull("subject", ReviewStatus.PUBLISHED)).thenReturn(0L);
        var result = service.update("review", new ArtisanReviewRequestDTO(new BigDecimal("4.126"), " changed "));
        assertThat(result.getRating()).isEqualByComparingTo("4.13");
        assertThat(review.getComment()).isEqualTo("changed");
        assertThat(subject.getRating()).isZero();
    }

    @Test
    void deletesOwnedReviewAndRejectsForeignOrRemovedEdits() {
        ArtisanReview review = ArtisanReview.builder().reviewer(reviewer).artisan(subject).enrollment(enrollment)
                .rating(BigDecimal.ONE).comment("old").status(ReviewStatus.PUBLISHED).build();
        review.setId("review");
        when(reviewRepository.findByIdAndDeletedAtIsNull("review")).thenReturn(Optional.of(review));
        when(reviewRepository.averageRating("subject", ReviewStatus.PUBLISHED)).thenReturn(new BigDecimal("2.5"));
        when(reviewRepository.countByArtisanIdAndStatusAndDeletedAtIsNull("subject", ReviewStatus.PUBLISHED)).thenReturn(2L);
        service.delete("review");
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.REMOVED);
        assertThat(review.getDeletedAt()).isNotNull();

        Artisan other = Artisan.builder().id("other").build();
        review.setStatus(ReviewStatus.PUBLISHED); review.setReviewer(other);
        assertThatThrownBy(() -> service.update("review", new ArtisanReviewRequestDTO(BigDecimal.TEN, "x")))
                .isInstanceOf(ForbiddenException.class);
        review.setReviewer(reviewer); review.setStatus(ReviewStatus.REMOVED);
        assertThatThrownBy(() -> service.update("review", new ArtisanReviewRequestDTO(BigDecimal.TEN, "x")))
                .isInstanceOf(ConflictException.class);

        review.setStatus(ReviewStatus.PUBLISHED);
        review.setReviewer(other);
        assertThatThrownBy(() -> service.delete("review"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rejectsMissingReviewsForUpdateAndDelete() {
        when(reviewRepository.findByIdAndDeletedAtIsNull("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update("missing", new ArtisanReviewRequestDTO(BigDecimal.ONE, "x")))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsMissingEnrollmentSelfReviewAndExistingReview() {
        when(enrollmentRepository.findByFormationIdAndArtisanId("missing", "reviewer")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create("missing", new ArtisanReviewRequestDTO(BigDecimal.ONE, "x")))
                .isInstanceOf(ForbiddenException.class);
        formation.setAuthor(reviewer);
        when(enrollmentRepository.findByFormationIdAndArtisanId("formation", "reviewer")).thenReturn(Optional.of(enrollment));
        assertThatThrownBy(() -> service.create("formation", new ArtisanReviewRequestDTO(BigDecimal.ONE, "x")))
                .isInstanceOf(ConflictException.class);
        formation.setAuthor(subject);
        when(reviewRepository.findByEnrollmentId("enrollment")).thenReturn(Optional.of(new ArtisanReview()));
        assertThatThrownBy(() -> service.create("formation", new ArtisanReviewRequestDTO(BigDecimal.ONE, "x")))
                .isInstanceOf(ConflictException.class);
    }
}
