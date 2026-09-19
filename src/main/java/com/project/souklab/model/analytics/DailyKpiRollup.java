package com.project.souklab.model.analytics;

import com.project.souklab.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_kpi_rollups", indexes = @Index(name = "uk_daily_kpi_rollup_date_key", columnList = "rollup_date,kpi_key", unique = true))
@Getter @Setter @NoArgsConstructor
public class DailyKpiRollup extends BaseEntity {
    @Column(name = "rollup_date", nullable = false) private LocalDate rollupDate;
    @Column(name = "kpi_key", nullable = false, length = 80) private String kpiKey;
    @Column(name = "metric_value", nullable = false) private long value;
    @Column(name = "source_version", nullable = false) private int sourceVersion = 1;
}
