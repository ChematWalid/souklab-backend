package com.project.souklab.dao.analytics;

import com.project.souklab.model.analytics.AnalyticsJobArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface AnalyticsJobArtifactRepository extends JpaRepository<AnalyticsJobArtifact, String> {
    Optional<AnalyticsJobArtifact> findFirstByJobIdOrderByCreatedAtDesc(String jobId);
    List<AnalyticsJobArtifact> findByExpiresAtBeforeOrderByExpiresAtAsc(LocalDateTime before, Pageable pageable);
}
