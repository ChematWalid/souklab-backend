package com.project.souklab.analytics;

import com.project.souklab.model.analytics.AnalyticsSortDirection;
import com.project.souklab.model.analytics.AnalyticsSortField;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Applies the typed ordering requested for a bucketed analytics series. */
public final class AnalyticsSeriesSorter {

    private AnalyticsSeriesSorter() {
    }

    public static void sort(List<Map<String, Object>> series,
                            AnalyticsSortField field,
                            AnalyticsSortDirection direction) {
        if (field == null) {
            return;
        }
        series.sort(comparator(field, direction));
    }

    private static Comparator<Map<String, Object>> comparator(AnalyticsSortField field,
                                                               AnalyticsSortDirection direction) {
        return (left, right) -> {
            Object leftValue = left.get(field.field());
            Object rightValue = right.get(field.field());
            if (leftValue == null || rightValue == null) {
                if (leftValue == rightValue) {
                    return 0;
                }
                return leftValue == null ? 1 : -1;
            }
            int result = compareValues(leftValue, rightValue);
            return direction == AnalyticsSortDirection.DESC ? -result : result;
        };
    }

    @SuppressWarnings("unchecked")
    private static int compareValues(Object left, Object right) {
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        }
        return ((Comparable<Object>) left).compareTo(right);
    }
}
