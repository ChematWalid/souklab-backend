package com.project.souklab.dao.analytics;

import com.project.souklab.model.analytics.AnalyticsOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalyticsOutboxRepository extends JpaRepository<AnalyticsOutboxEvent, String> {
    @Query("select e from AnalyticsOutboxEvent e where e.status = :status and (e.nextAttemptAt is null or e.nextAttemptAt <= :now) order by e.createdAt asc")
    List<AnalyticsOutboxEvent> findReadyByStatus(@Param("status") AnalyticsOutboxEvent.OutboxStatus status,
                                                  @Param("now") LocalDateTime now, Pageable pageable);
    long countByStatus(AnalyticsOutboxEvent.OutboxStatus status);
}
