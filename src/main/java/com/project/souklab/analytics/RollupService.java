package com.project.souklab.analytics;

import com.project.souklab.dao.analytics.DailyKpiRollupRepository;
import com.project.souklab.model.analytics.DailyKpiRollup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Idempotent daily rollup writer used by event consumers and bounded rebuilds. */
@Service
@RequiredArgsConstructor
public class RollupService {
    private final DailyKpiRollupRepository repository;

    @Transactional
    public DailyKpiRollup upsert(LocalDate date, String key, long value) {
        DailyKpiRollup rollup = repository.findByRollupDateAndKpiKey(date, key).orElseGet(DailyKpiRollup::new);
        rollup.setRollupDate(date); rollup.setKpiKey(key); rollup.setValue(value); rollup.setSourceVersion(1);
        return repository.save(rollup);
    }
}
