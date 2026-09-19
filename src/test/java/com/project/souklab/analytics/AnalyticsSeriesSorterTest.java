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
        List<Map<AnalyticsMetric.Series.Key, Object>> series = new ArrayList<>(List.of(
                point(AnalyticsMetric.Series.Activity.EVENTS, 4L),
                point(AnalyticsMetric.Series.Activity.EVENTS, null),
                point(AnalyticsMetric.Series.Activity.EVENTS, 1L)));

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.Activity.EVENTS, AnalyticsSortDirection.ASC);
        assertThat(series).extracting(point -> point.get(AnalyticsMetric.Series.Activity.EVENTS))
                .containsExactly(1L, 4L, null);

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.Activity.EVENTS, AnalyticsSortDirection.DESC);
        assertThat(series).extracting(point -> point.get(AnalyticsMetric.Series.Activity.EVENTS))
                .containsExactly(4L, 1L, null);
    }

    @Test
    void sortsDateSeriesUsingTheTypedExternalFieldKey() {
        List<Map<AnalyticsMetric.Series.Key, Object>> series = new ArrayList<>(List.of(
                point(AnalyticsMetric.Series.Date.START, LocalDate.of(2026, 3, 2)),
                point(AnalyticsMetric.Series.Date.START, LocalDate.of(2026, 1, 1))));

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.Date.START, AnalyticsSortDirection.ASC);

        assertThat(series).extracting(point -> point.get(AnalyticsMetric.Series.Date.START))
                .containsExactly(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 2));
    }

    private static Map<AnalyticsMetric.Series.Key, Object> point(AnalyticsMetric.Series.Key key, Object value) {
        Map<AnalyticsMetric.Series.Key, Object> point = new LinkedHashMap<>();
        point.put(key, value);
        return point;
    }
}
