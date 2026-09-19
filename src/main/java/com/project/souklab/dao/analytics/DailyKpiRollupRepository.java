package com.project.souklab.dao.analytics;

import com.project.souklab.model.analytics.DailyKpiRollup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyKpiRollupRepository extends JpaRepository<DailyKpiRollup, String> {
    Optional<DailyKpiRollup> findByRollupDateAndKpiKey(LocalDate date, String key);
    List<DailyKpiRollup> findByRollupDateBeforeOrderByRollupDateAsc(LocalDate before, Pageable pageable);
    List<DailyKpiRollup> findByRollupDateBetween(LocalDate from, LocalDate to);

    /** Atomic event-consumer increment; avoids lost updates under concurrent delivery. */
    @Modifying
    @Query(value = "INSERT INTO daily_kpi_rollups "
            + "(id, rollup_date, kpi_key, metric_value, source_version, created_at, updated_at) "
            + "VALUES (UUID(), :date, :key, 1, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)) "
            + "ON DUPLICATE KEY UPDATE metric_value = metric_value + 1, updated_at = CURRENT_TIMESTAMP(6)",
            nativeQuery = true)
    int incrementEventKpi(@Param("date") LocalDate date, @Param("key") String key);
}
