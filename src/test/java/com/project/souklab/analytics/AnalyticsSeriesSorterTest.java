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
        List<Map<String, Object>> series = new ArrayList<>(List.of(
                point("activityEvents", 4L),
                point("activityEvents", null),
                point("activityEvents", 1L)));

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.ACTIVITY_EVENTS, AnalyticsSortDirection.ASC);
        assertThat(series).extracting(point -> point.get("activityEvents"))
                .containsExactly(1L, 4L, null);

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.ACTIVITY_EVENTS, AnalyticsSortDirection.DESC);
        assertThat(series).extracting(point -> point.get("activityEvents"))
                .containsExactly(4L, 1L, null);
    }

    @Test
    void sortsDateSeriesUsingTheTypedExternalFieldKey() {
        List<Map<String, Object>> series = new ArrayList<>(List.of(
                point("startDate", LocalDate.of(2026, 3, 2)),
                point("startDate", LocalDate.of(2026, 1, 1))));

        AnalyticsSeriesSorter.sort(series, AnalyticsSortField.START_DATE, AnalyticsSortDirection.ASC);

        assertThat(series).extracting(point -> point.get("startDate"))
                .containsExactly(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 2));
    }

    private static Map<String, Object> point(String key, Object value) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put(key, value);
        return point;
    }
}
