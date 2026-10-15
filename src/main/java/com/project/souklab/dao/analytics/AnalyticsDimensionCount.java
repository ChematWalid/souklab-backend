package com.project.souklab.dao.analytics;

/**
 * Aggregated count for a named analytics dimension.
 */
public interface AnalyticsDimensionCount {
    String getDimension();

    long getCount();
}
