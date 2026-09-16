package com.project.souklab.service.report;

import com.project.souklab.dao.ArtisanReviewRepository;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dao.ContentReportRepository;
import com.project.souklab.dao.FeedPostRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.report.ContentReportRequestDTO;
import com.project.souklab.dto.report.ReportResolutionRequestDTO;
import com.project.souklab.exception.BadRequestException;
import com.project.souklab.model.ContentReport;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        when(userRepository.findByEmail("reporter@example.com")).thenReturn(Optional.of(reporter));
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

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(com.project.souklab.exception.ResourceNotFoundException.class);
    }
}
