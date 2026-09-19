package com.project.souklab.analytics;

import com.project.souklab.config.AnalyticsRetentionProperties;
import com.project.souklab.dao.analytics.ActivityEventRepository;
import com.project.souklab.dao.analytics.AnalyticsJobArtifactRepository;
import com.project.souklab.dao.analytics.AnalyticsJobRepository;
import com.project.souklab.dao.analytics.AnalyticsMaintenanceJobRepository;
import com.project.souklab.dao.analytics.DailyKpiRollupRepository;
import com.project.souklab.filestorage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsRetentionCleanupTest {
    @Mock private ActivityEventRepository events;
    @Mock private AnalyticsJobRepository jobs;
    @Mock private DailyKpiRollupRepository rollups;
    @Mock private AnalyticsRetentionProperties properties;
    @Mock private AnalyticsJobArtifactRepository artifacts;
    @Mock private AnalyticsMaintenanceJobRepository maintenanceJobs;
    @Mock private StorageService storageService;
    @Captor private ArgumentCaptor<LocalDateTime> expirationCaptor;

    @InjectMocks private AnalyticsRetentionCleanup cleanup;

    @Test
    void removesJobsAtExpirationTimeWithoutApplyingRetentionTwice() {
        Instant instant = Instant.parse("2026-09-19T10:00:00Z");
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        cleanup = new AnalyticsRetentionCleanup(events, jobs, rollups, properties, clock,
                artifacts, maintenanceJobs, storageService);
        when(properties.getCleanupBatchSize()).thenReturn(10);
        when(properties.getRawEvents()).thenReturn(Duration.ofDays(90));
        when(properties.getRollups()).thenReturn(Duration.ofDays(730));
        when(events.findByEventTimeBeforeOrderByEventTimeAsc(any(), any())).thenReturn(List.of());
        when(rollups.findByRollupDateBeforeOrderByRollupDateAsc(any(), any())).thenReturn(List.of());
        when(artifacts.findByExpiresAtBeforeOrderByExpiresAtAsc(any(), any())).thenReturn(List.of());
        when(jobs.findByExpiresAtBeforeOrderByExpiresAtAsc(any(), any())).thenReturn(List.of());
        when(maintenanceJobs.findByExpiresAtBeforeOrderByExpiresAtAsc(any(), any())).thenReturn(List.of());

        cleanup.cleanup();

        verify(jobs).findByExpiresAtBeforeOrderByExpiresAtAsc(expirationCaptor.capture(), any(Pageable.class));
        assertThat(expirationCaptor.getValue()).isEqualTo(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
        verify(maintenanceJobs).findByExpiresAtBeforeOrderByExpiresAtAsc(
                eq(LocalDateTime.ofInstant(instant, ZoneOffset.UTC)), any(Pageable.class));
    }
}
