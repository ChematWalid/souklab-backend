package com.project.souklab.analytics;
import com.project.souklab.model.analytics.AnalyticsJobArtifact;
import java.util.ArrayList;

import com.project.souklab.config.AnalyticsRetentionProperties;
import com.project.souklab.dao.analytics.ActivityEventRepository;
import com.project.souklab.dao.analytics.AnalyticsJobRepository;
import com.project.souklab.dao.analytics.DailyKpiRollupRepository;
import com.project.souklab.dao.analytics.AnalyticsJobArtifactRepository;
import com.project.souklab.dao.analytics.AnalyticsMaintenanceJobRepository;
import com.project.souklab.filestorage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;

@Component
@RequiredArgsConstructor
public class AnalyticsRetentionCleanup {
    private final ActivityEventRepository events;
    private final AnalyticsJobRepository jobs;
    private final DailyKpiRollupRepository rollups;
    private final AnalyticsRetentionProperties properties;
    private final Clock clock;
    private final AnalyticsJobArtifactRepository artifacts;
    private final AnalyticsMaintenanceJobRepository maintenanceJobs;
    private final StorageService storageService;

    @Scheduled(fixedDelayString = "${app.analytics.retention.cleanup-interval}")
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now(clock);
        var page = PageRequest.of(0, properties.getCleanupBatchSize());
        events.deleteAll(events.findByEventTimeBeforeOrderByEventTimeAsc(now.minus(properties.getRawEvents()), page));
        rollups.deleteAll(rollups.findByRollupDateBeforeOrderByRollupDateAsc(now.minus(properties.getRollups()).toLocalDate(), page));
        var expiredArtifacts = artifacts.findByExpiresAtBeforeOrderByExpiresAtAsc(now, page);
        var deletedArtifacts = new ArrayList<AnalyticsJobArtifact>();
        expiredArtifacts.forEach(artifact -> {
            try {
                storageService.delete(artifact.getStorageKey());
                deletedArtifacts.add(artifact);
            } catch (RuntimeException ignored) {
                // Keep the metadata so a later bounded cleanup can retry the object deletion.
            }
        });
        artifacts.deleteAll(deletedArtifacts);
        // Retain an expired job while its object metadata remains, so a storage outage
        // cannot turn a retryable artifact into an orphaned foreign-key failure.
        jobs.findByExpiresAtBeforeOrderByExpiresAtAsc(now.minus(properties.getJobs()), page).stream()
                .filter(job -> artifacts.findFirstByJobIdOrderByCreatedAtDesc(job.getId()).isEmpty())
                .forEach(jobs::delete);
        maintenanceJobs.deleteAll(maintenanceJobs.findByExpiresAtBeforeOrderByExpiresAtAsc(now.minus(properties.getJobs()), page));
    }
}
