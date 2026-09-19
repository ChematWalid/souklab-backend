package com.project.souklab.dao.analytics;

/**
 * Aggregated count for a named analytics dimension.
 *
 * @param dimension stable region or craft-category identifier
 * @param count distinct actors represented by the dimension
 */
public interface AnalyticsDimensionCount {
    String getDimension();

    long getCount();
}
