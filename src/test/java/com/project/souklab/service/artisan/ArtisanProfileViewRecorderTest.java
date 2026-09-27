package com.project.souklab.service.artisan;

import com.project.souklab.analytics.ActivityEventService;
import com.project.souklab.analytics.AnalyticsEvent;
import com.project.souklab.analytics.AnalyticsMetadata;
import com.project.souklab.dao.ArtisanProfileViewRepository;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.model.AccountRole;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanProfileView;
import com.project.souklab.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtisanProfileViewRecorderTest {

    @Mock
    private ArtisanProfileViewRepository artisanProfileViewRepository;

    @Mock
    private ArtisanRepository artisanRepository;

    @Mock
    private ActivityEventService activityEventService;

    @InjectMocks
    private ArtisanProfileViewRecorder recorder;

    private User viewer;
    private Artisan artisan;

    @BeforeEach
    void setUp() {
        recorder.setActivityEventService(activityEventService);

        viewer = User.builder()
                .email("viewer@souklab.dz")
                .build();
        viewer.setId("viewer-1");

        User artisanUser = User.builder()
                .email("artisan@souklab.dz")
                .build();
        artisanUser.setId("artisan-user-1");

        artisan = Artisan.builder()
                .user(artisanUser)
                .viewsCount(50)
                .build();
        artisan.setId("artisan-1");
    }

    @Test
    @DisplayName("recordProfileViewIfEligible: skips view recording when viewer is viewing self")
    void recordProfileViewIfEligible_whenSelf_skips() {
        recorder.recordProfileViewIfEligible(viewer, artisan, true, false);

        verify(artisanProfileViewRepository, never()).existsByViewerIdAndArtisanId(any(), any());
        verify(artisanProfileViewRepository, never()).saveAndFlush(any());
        verify(artisanRepository, never()).incrementViewsCount(any());
    }

    @Test
    @DisplayName("recordProfileViewIfEligible: skips view recording when viewer is admin")
    void recordProfileViewIfEligible_whenAdmin_skips() {
        recorder.recordProfileViewIfEligible(viewer, artisan, false, true);

        verify(artisanProfileViewRepository, never()).existsByViewerIdAndArtisanId(any(), any());
        verify(artisanProfileViewRepository, never()).saveAndFlush(any());
        verify(artisanRepository, never()).incrementViewsCount(any());
    }

    @Test
    @DisplayName("recordProfileViewIfEligible: skips view recording when view already exists")
    void recordProfileViewIfEligible_whenViewAlreadyExists_skips() {
        when(artisanProfileViewRepository.existsByViewerIdAndArtisanId("viewer-1", "artisan-1")).thenReturn(true);

        recorder.recordProfileViewIfEligible(viewer, artisan, false, false);

        verify(artisanProfileViewRepository, never()).saveAndFlush(any());
        verify(artisanRepository, never()).incrementViewsCount(any());
    }

    @Test
    @DisplayName("recordProfileViewIfEligible: records view, increments viewsCount, and logs activity event")
    void recordProfileViewIfEligible_whenEligible_recordsViewAndIncrements() {
        when(artisanProfileViewRepository.existsByViewerIdAndArtisanId("viewer-1", "artisan-1")).thenReturn(false);

        recorder.recordProfileViewIfEligible(viewer, artisan, false, false);

        ArgumentCaptor<ArtisanProfileView> viewCaptor = ArgumentCaptor.forClass(ArtisanProfileView.class);
        verify(artisanProfileViewRepository).saveAndFlush(viewCaptor.capture());
        ArtisanProfileView capturedView = viewCaptor.getValue();
        assertThat(capturedView.getViewer()).isEqualTo(viewer);
        assertThat(capturedView.getArtisan()).isEqualTo(artisan);

        verify(artisanRepository).incrementViewsCount("artisan-1");
        verify(activityEventService).record(eq(AnalyticsEvent.Profile.VIEW), eq("viewer-1"), eq("artisan-1"),
                eq(Map.of(AnalyticsMetadata.Account.TYPE, AccountRole.CLIENT)));
    }

    @Test
    @DisplayName("recordProfileViewIfEligible: safely handles DataIntegrityViolationException on concurrent duplicate")
    void recordProfileViewIfEligible_whenDataIntegrityViolation_catchesGracefully() {
        when(artisanProfileViewRepository.existsByViewerIdAndArtisanId("viewer-1", "artisan-1")).thenReturn(false);
        when(artisanProfileViewRepository.saveAndFlush(any(ArtisanProfileView.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        assertThatCode(() -> recorder.recordProfileViewIfEligible(viewer, artisan, false, false))
                .doesNotThrowAnyException();

        verify(artisanRepository, never()).incrementViewsCount(any());
    }
}
