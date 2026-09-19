package com.project.souklab.analytics;

import com.project.souklab.model.analytics.AnalyticsSortDirection;
import com.project.souklab.model.analytics.AnalyticsSortField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsSeriesSorterTest {

    @Test
    void sortsNumericSeriesInBothDirectionsAndKeepsNullsLast() {
        List<Map<AnalyticsMetric.Series, Object>> series = new ArrayList<>(List.of(
                point(AnalyticsMetric.Series.ACTIVITY_EVENTS, 4L),
                point(AnalyticsMetric.Series.ACTIVITY_EVENTS, null),
                point(AnalyticsMetric.Series.ACTIVITY_EVENTS, 1L)));

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.Activity.EVENTS, AnalyticsSortDirection.ASC);
        assertThat(series).extracting(point -> point.get(AnalyticsMetric.Series.ACTIVITY_EVENTS))
                .containsExactly(1L, 4L, null);

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.Activity.EVENTS, AnalyticsSortDirection.DESC);
        assertThat(series).extracting(point -> point.get(AnalyticsMetric.Series.ACTIVITY_EVENTS))
                .containsExactly(4L, 1L, null);
    }

    @Test
    void sortsDateSeriesUsingTheTypedExternalFieldKey() {
        List<Map<AnalyticsMetric.Series, Object>> series = new ArrayList<>(List.of(
                point(AnalyticsMetric.Series.START_DATE, LocalDate.of(2026, 3, 2)),
                point(AnalyticsMetric.Series.START_DATE, LocalDate.of(2026, 1, 1))));

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.Date.START, AnalyticsSortDirection.ASC);

        assertThat(series).extracting(point -> point.get(AnalyticsMetric.Series.START_DATE))
                .containsExactly(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 2));
    }

    private static Map<AnalyticsMetric.Series, Object> point(AnalyticsMetric.Series key, Object value) {
        Map<AnalyticsMetric.Series, Object> point = new LinkedHashMap<>();
        point.put(key, value);
        return point;
    }
}
