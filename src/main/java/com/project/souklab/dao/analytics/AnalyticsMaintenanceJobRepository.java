package com.project.souklab.dao.analytics;

import com.project.souklab.model.analytics.AnalyticsMaintenanceJob;
import com.project.souklab.model.analytics.AnalyticsJobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AnalyticsMaintenanceJobRepository extends JpaRepository<AnalyticsMaintenanceJob, String> {
    Optional<AnalyticsMaintenanceJob> findByIdAndOwnerId(String id, String ownerId);
    List<AnalyticsMaintenanceJob> findByExpiresAtBeforeOrderByExpiresAtAsc(LocalDateTime before, Pageable pageable);
    long countByStatus(AnalyticsJobStatus status);
}
