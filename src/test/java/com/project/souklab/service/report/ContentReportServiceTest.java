package com.project.souklab.service.report;

import com.project.souklab.dao.ArtisanReviewRepository;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.ContentReportRepository;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.report.ContentReportRequestDTO;
import com.project.souklab.dto.report.ReportResolutionRequestDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.ContentReport;
import com.project.souklab.model.ReportStatus;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.ArtisanReview;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.ReviewStatus;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.ReportResolutionAction;
import com.project.souklab.model.ReportTargetType;
import com.project.souklab.model.User;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.security.AccessControlService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * Verifies report target validation and self-report protection.
 */
@ExtendWith(MockitoExtension.class)
class ContentReportServiceTest {

    @Mock private ContentReportRepository reportRepository;
    @Mock private FeedPostRepository postRepository;
    @Mock private ArtisanReviewRepository reviewRepository;
    @Mock private ArtisanRepository artisanRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private AccessControlService accessControlService;

    private ContentReportService service;
    private User reporter;

    @BeforeEach
    void setUp() {
        service = new ContentReportService(reportRepository, postRepository, reviewRepository, artisanRepository, userRepository, notificationService,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC), accessControlService);
        reporter = User.builder().email("reporter@example.com").build();
        reporter.setId("user-1");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("reporter@example.com", "credentials", List.of()));
        lenient().when(userRepository.findByEmail("reporter@example.com")).thenReturn(Optional.of(reporter));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsSelfReport() {
        ContentReportRequestDTO request = new ContentReportRequestDTO(ReportTargetType.USER, "user-1", "abuse", null);

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsMissingTarget() {
        when(postRepository.findByIdAndDeletedAtIsNull("post-1")).thenReturn(Optional.empty());
        ContentReportRequestDTO request = new ContentReportRequestDTO(ReportTargetType.POST, "post-1", "abuse", null);

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createsReportsForEverySupportedTargetAndNotifiesAdmins() {
        when(userRepository.existsById("target-user")).thenReturn(true);
        when(postRepository.findByIdAndDeletedAtIsNull("target-post")).thenReturn(Optional.of(new FeedPost()));
        when(reviewRepository.findByIdAndDeletedAtIsNull("target-review")).thenReturn(Optional.of(new ArtisanReview()));
        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> {
            ContentReport report = invocation.getArgument(0);
            report.setId("report-1");
            return report;
        });

        for (ReportTargetType type : ReportTargetType.values()) {
            String target = "target-" + type.name().toLowerCase();
            assertThatCode(() -> service.create(new ContentReportRequestDTO(type, target, " reason ", " details ")))
                    .doesNotThrowAnyException();
        }
        verify(notificationService, times(3)).notifyAdmins("New content report submitted.");
    }

    @Test
    void listsWithAllFilterCombinations() {
        allowAdmin();
        when(reportRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(reportRepository.findByTargetType(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(reportRepository.findByStatus(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(reportRepository.findByTargetTypeAndStatus(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.list(null, null, PageRequest.of(0, 10));
        service.list(null, ReportTargetType.POST, PageRequest.of(0, 10));
        service.list(ReportStatus.OPEN, null, PageRequest.of(0, 10));
        service.list(ReportStatus.OPEN, ReportTargetType.REVIEW, PageRequest.of(0, 10));
        verify(reportRepository).findAll(any(Pageable.class));
        verify(reportRepository).findByTargetType(eq(ReportTargetType.POST), any());
        verify(reportRepository).findByStatus(eq(ReportStatus.OPEN), any());
        verify(reportRepository).findByTargetTypeAndStatus(eq(ReportTargetType.REVIEW), eq(ReportStatus.OPEN), any());
    }

    @Test
    void resolvesDismissHideAndRemoveForUserAndPost() {
        allowAdmin();
        when(reportRepository.findById(any())).thenAnswer(invocation -> Optional.of(report("r", ReportTargetType.USER, "target")));
        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById("target")).thenReturn(Optional.of(User.builder().email("target@test").build()));
        service.resolve("r", new ReportResolutionRequestDTO(ReportResolutionAction.DISMISS, "dismiss"));

        ContentReport userHide = report("r2", ReportTargetType.USER, "target");
        when(reportRepository.findById("r2")).thenReturn(Optional.of(userHide));
        service.resolve("r2", new ReportResolutionRequestDTO(ReportResolutionAction.HIDE, "hide"));
        assertThat(userHide.getStatus()).isEqualTo(ReportStatus.RESOLVED);

        ContentReport postRemove = report("r3", ReportTargetType.POST, "post");
        FeedPost post = FeedPost.builder().status(FeedPostStatus.PUBLISHED).build();
        when(reportRepository.findById("r3")).thenReturn(Optional.of(postRemove));
        when(postRepository.findByIdAndDeletedAtIsNull("post")).thenReturn(Optional.of(post));
        service.resolve("r3", new ReportResolutionRequestDTO(ReportResolutionAction.REMOVE, "remove"));
        assertThat(post.getStatus()).isEqualTo(FeedPostStatus.REMOVED);
        assertThat(post.getDeletedAt()).isNotNull();

        ContentReport postHide = report("r4", ReportTargetType.POST, "post-hide");
        FeedPost hiddenPost = FeedPost.builder().status(FeedPostStatus.PUBLISHED).build();
        when(reportRepository.findById("r4")).thenReturn(Optional.of(postHide));
        when(postRepository.findByIdAndDeletedAtIsNull("post-hide")).thenReturn(Optional.of(hiddenPost));
        service.resolve("r4", new ReportResolutionRequestDTO(ReportResolutionAction.HIDE, "hide"));
        assertThat(hiddenPost.getStatus()).isEqualTo(FeedPostStatus.HIDDEN);

        ContentReport userRemove = report("r5", ReportTargetType.USER, "target-remove");
        User removedUser = User.builder().email("remove@test").build();
        when(reportRepository.findById("r5")).thenReturn(Optional.of(userRemove));
        when(userRepository.findById("target-remove")).thenReturn(Optional.of(removedUser));
        service.resolve("r5", new ReportResolutionRequestDTO(ReportResolutionAction.REMOVE, "remove"));
        assertThat(removedUser.getDeletedAt()).isNotNull();
    }

    @Test
    void resolvesReviewActionsAndRecalculatesRating() {
        allowAdmin();
        Artisan artisan = Artisan.builder().id("artisan-1").build();
        ArtisanReview review = ArtisanReview.builder().artisan(artisan).status(ReviewStatus.PUBLISHED).build();
        when(reviewRepository.findByIdAndDeletedAtIsNull("review")).thenReturn(Optional.of(review));
        when(reviewRepository.averageRating("artisan-1", ReviewStatus.PUBLISHED)).thenReturn(new java.math.BigDecimal("4.126"));
        when(reviewRepository.countByArtisanIdAndStatusAndDeletedAtIsNull("artisan-1", ReviewStatus.PUBLISHED)).thenReturn(3L);
        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reportRepository.findById("r")).thenReturn(Optional.of(report("r", ReportTargetType.REVIEW, "review")));
        service.resolve("r", new ReportResolutionRequestDTO(ReportResolutionAction.HIDE, "hide"));
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
        assertThat(artisan.getRating()).isEqualTo(4.13);
        assertThat(artisan.getReviewsCount()).isEqualTo(3);
        when(reportRepository.findById("r2")).thenReturn(Optional.of(report("r2", ReportTargetType.REVIEW, "review")));
        service.resolve("r2", new ReportResolutionRequestDTO(ReportResolutionAction.REMOVE, "remove"));
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.REMOVED);
    }

    @Test
    void recalculatesReviewRatingToZeroWhenNoPublishedReviewsRemain() {
        allowAdmin();
        Artisan artisan = Artisan.builder().id("artisan-zero").build();
        ArtisanReview review = ArtisanReview.builder().artisan(artisan).status(ReviewStatus.PUBLISHED).build();
        when(reviewRepository.findByIdAndDeletedAtIsNull("review-zero")).thenReturn(Optional.of(review));
        when(reviewRepository.averageRating("artisan-zero", ReviewStatus.PUBLISHED)).thenReturn(null);
        when(reviewRepository.countByArtisanIdAndStatusAndDeletedAtIsNull("artisan-zero", ReviewStatus.PUBLISHED))
                .thenReturn(0L);
        when(reportRepository.findById("report-zero"))
                .thenReturn(Optional.of(report("report-zero", ReportTargetType.REVIEW, "review-zero")));
        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.resolve("report-zero", new ReportResolutionRequestDTO(ReportResolutionAction.REMOVE, "removed"));

        assertThat(artisan.getRating()).isZero();
        assertThat(artisan.getReviewsCount()).isZero();
    }

    @Test
    void rejectsUnauthorizedAndAlreadyResolvedReports() {
        when(accessControlService.canModerateReports(any())).thenReturn(false);
        assertThatThrownBy(() -> service.list(null, null, PageRequest.of(0, 10))).isInstanceOf(com.project.souklab.exception.ForbiddenException.class);
        allowAdmin();
        ContentReport closed = report("closed", ReportTargetType.POST, "post");
        closed.setStatus(ReportStatus.RESOLVED);
        when(reportRepository.findById("closed")).thenReturn(Optional.of(closed));
        assertThatThrownBy(() -> service.resolve("closed", new ReportResolutionRequestDTO(ReportResolutionAction.HIDE, "note")))
                .isInstanceOf(BadRequestException.class);
        when(reportRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resolve("missing", new ReportResolutionRequestDTO(ReportResolutionAction.HIDE, "note")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsMissingReviewAndUserTargets() {
        when(reviewRepository.findByIdAndDeletedAtIsNull("review-missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(new ContentReportRequestDTO(
                ReportTargetType.REVIEW, "review-missing", "reason", null)))
                .isInstanceOf(ResourceNotFoundException.class);
        when(userRepository.existsById("user-missing")).thenReturn(false);
        assertThatThrownBy(() -> service.create(new ContentReportRequestDTO(
                ReportTargetType.USER, "user-missing", "reason", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resolutionFailsWhenTargetDisappearsAfterReportCreation() {
        allowAdmin();
        when(reportRepository.findById("post-report")).thenReturn(Optional.of(
                report("post-report", ReportTargetType.POST, "gone-post")));
        when(postRepository.findByIdAndDeletedAtIsNull("gone-post")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resolve("post-report",
                new ReportResolutionRequestDTO(ReportResolutionAction.HIDE, "hide")))
                .isInstanceOf(ResourceNotFoundException.class);

        when(reportRepository.findById("review-report")).thenReturn(Optional.of(
                report("review-report", ReportTargetType.REVIEW, "gone-review")));
        when(reviewRepository.findByIdAndDeletedAtIsNull("gone-review")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resolve("review-report",
                new ReportResolutionRequestDTO(ReportResolutionAction.REMOVE, "remove")))
                .isInstanceOf(ResourceNotFoundException.class);

        when(reportRepository.findById("user-report")).thenReturn(Optional.of(
                report("user-report", ReportTargetType.USER, "gone-user")));
        when(userRepository.findById("gone-user")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resolve("user-report",
                new ReportResolutionRequestDTO(ReportResolutionAction.HIDE, "hide")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createsReportWithNullDetailsAndRejectsMissingAuthentication() {
        when(postRepository.findByIdAndDeletedAtIsNull("post")).thenReturn(Optional.of(new FeedPost()));
        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.create(new ContentReportRequestDTO(
                ReportTargetType.POST, "post", " reason ", null)).getDetails()).isNull();

        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> service.create(new ContentReportRequestDTO(
                ReportTargetType.POST, "post", "reason", null)))
                .isInstanceOf(com.project.souklab.exception.ForbiddenException.class);
    }

    private void allowAdmin() {
        when(accessControlService.canModerateReports(any())).thenReturn(true);
    }

    private ContentReport report(String id, ReportTargetType type, String target) {
        ContentReport report = ContentReport.builder().reporter(reporter).targetType(type).targetId(target)
                .reason("reason").status(ReportStatus.OPEN).build();
        report.setId(id);
        return report;
    }
}
