package com.project.souklab.dao.analytics;

import com.project.souklab.model.analytics.AnalyticsJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.project.souklab.model.analytics.AnalyticsJobStatus;

public interface AnalyticsJobRepository extends JpaRepository<AnalyticsJob, String> {
    Optional<AnalyticsJob> findByIdAndOwnerId(String id, String ownerId);
    List<AnalyticsJob> findByExpiresAtBeforeOrderByExpiresAtAsc(LocalDateTime before, Pageable pageable);
    long countByStatus(AnalyticsJobStatus status);
}
