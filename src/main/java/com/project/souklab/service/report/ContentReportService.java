package com.project.souklab.service.report;

import com.project.souklab.dao.ArtisanReviewRepository;
import com.project.souklab.dao.ContentReportRepository;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.report.ContentReportRequestDTO;
import com.project.souklab.dto.report.ContentReportResponseDTO;
import com.project.souklab.dto.report.ReportResolutionRequestDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.ArtisanReview;
import com.project.souklab.model.ContentReport;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.ReportResolutionAction;
import com.project.souklab.model.ReportStatus;
import com.project.souklab.model.ReportTargetType;
import com.project.souklab.model.ReviewStatus;
import com.project.souklab.model.User;
import com.project.souklab.service.notification.NotificationService;
import com.project.souklab.model.NotificationType;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Coordinates abuse report submission and administrator resolution actions.
 */
@Service
@RequiredArgsConstructor
public class ContentReportService {

    private final ContentReportRepository reportRepository;
    private final FeedPostRepository postRepository;
    private final ArtisanReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    /**
     * Creates a report against a supported existing target.
     *
     * @param request report payload
     * @return created report
     */
    @Transactional
    public ContentReportResponseDTO create(ContentReportRequestDTO request) {
        User reporter = currentUser();
        if (reporter.getId().equals(request.getTargetId()) && request.getTargetType() == ReportTargetType.USER) {
            throw new BadRequestException("You cannot report yourself.");
        }
        ensureTargetExists(request.getTargetType(), request.getTargetId());
        ContentReport report = ContentReport.builder()
                .reporter(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason().trim())
                .details(request.getDetails() == null ? null : request.getDetails().trim())
                .status(ReportStatus.OPEN)
                .build();
        ContentReport saved = reportRepository.save(report);
        notificationService.notifyAdmins("New content report submitted.");
        return ContentReportResponseDTO.from(saved);
    }

    /**
     * Lists reports for administrator moderation.
     *
     * @param status optional status filter
     * @param targetType optional target filter
     * @param pageable pagination configuration
     * @return report queue
     */
    @Transactional(readOnly = true)
    public Page<ContentReportResponseDTO> list(ReportStatus status, ReportTargetType targetType, Pageable pageable) {
        requireAdmin();
        Page<ContentReport> reports;
        if (status == null) {
            reports = reportRepository.findAll(pageable);
        } else if (targetType == null) {
            reports = reportRepository.findByStatus(status, pageable);
        } else {
            reports = reportRepository.findByTargetTypeAndStatus(targetType, status, pageable);
        }
        return reports.map(ContentReportResponseDTO::from);
    }

    /**
     * Resolves a report and applies the selected target action.
     *
     * @param reportId report identifier
     * @param request resolution payload
     * @return resolved report
     */
    @Transactional
    public ContentReportResponseDTO resolve(String reportId, ReportResolutionRequestDTO request) {
        requireAdmin();
        ContentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found."));
        if (report.getStatus() != ReportStatus.OPEN) {
            throw new BadRequestException("Report has already been resolved.");
        }
        applyAction(report.getTargetType(), report.getTargetId(), request.getAction());
        User resolver = currentUser();
        report.setResolver(resolver);
        report.setResolutionAction(request.getAction());
        report.setResolutionNote(request.getNote().trim());
        report.setStatus(request.getAction() == ReportResolutionAction.DISMISS ? ReportStatus.DISMISSED : ReportStatus.RESOLVED);
        report.setUpdatedAt(LocalDateTime.now(clock));
        return ContentReportResponseDTO.from(reportRepository.save(report));
    }

    private void ensureTargetExists(ReportTargetType type, String id) {
        boolean exists = switch (type) {
            case USER -> userRepository.existsById(id);
            case POST -> postRepository.findByIdAndDeletedAtIsNull(id).isPresent();
            case REVIEW -> reviewRepository.findByIdAndDeletedAtIsNull(id).isPresent();
        };
        if (!exists) {
            throw new ResourceNotFoundException("Report target not found.");
        }
    }

    private void applyAction(ReportTargetType type, String id, ReportResolutionAction action) {
        if (action == ReportResolutionAction.DISMISS) {
            return;
        }
        switch (type) {
            case POST -> {
                FeedPost post = postRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new ResourceNotFoundException("Post not found."));
                post.setStatus(action == ReportResolutionAction.HIDE ? FeedPostStatus.HIDDEN : FeedPostStatus.REMOVED);
                if (action == ReportResolutionAction.REMOVE) {
                    post.setDeletedAt(LocalDateTime.now(clock));
                }
                postRepository.save(post);
            }
            case REVIEW -> {
                ArtisanReview review = reviewRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new ResourceNotFoundException("Review not found."));
                review.setStatus(action == ReportResolutionAction.HIDE ? ReviewStatus.HIDDEN : ReviewStatus.REMOVED);
                if (action == ReportResolutionAction.REMOVE) {
                    review.setDeletedAt(LocalDateTime.now(clock));
                }
                reviewRepository.save(review);
            }
            case USER -> {
                User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
                if (action == ReportResolutionAction.HIDE) {
                    user.setStatus(com.project.souklab.model.AccountStatus.SUSPENDED);
                } else {
                    user.setDeletedAt(LocalDateTime.now(clock));
                }
                userRepository.save(user);
            }
        }
    }

    private User currentUser() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            throw new ForbiddenException("Authentication is required.");
        }
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private void requireAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new ForbiddenException("Administrator access is required.");
        }
    }
}
