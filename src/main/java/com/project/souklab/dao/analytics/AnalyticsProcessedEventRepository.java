package com.project.souklab.dao.analytics;

import com.project.souklab.model.analytics.AnalyticsProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsProcessedEventRepository extends JpaRepository<AnalyticsProcessedEvent, String> {
    boolean existsByEventId(String eventId);
}
