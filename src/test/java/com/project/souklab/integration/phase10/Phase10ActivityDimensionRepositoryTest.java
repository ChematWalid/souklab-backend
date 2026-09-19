package com.project.souklab.integration.phase10;

import com.project.souklab.dao.analytics.ActivityEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executes the JPQL dimension aggregations against the configured MariaDB
 * schema so association joins and interface projections cannot regress
 * unnoticed during refactoring.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANALYTICS_RABBIT_ENABLED", matches = "true")
class Phase10ActivityDimensionRepositoryTest {

    @Autowired
    private ActivityEventRepository events;

    @Test
    void dimensionAggregationsReturnEmptyResultsWhenNoProfilesExist() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 2, 0, 0);

        assertThat(events.countDistinctActorsByRegionAndEventTimeBetween(null, from, to)).isEmpty();
        assertThat(events.countDistinctActorsByCraftCategoryAndEventTimeBetween(null, from, to)).isEmpty();
    }
}
