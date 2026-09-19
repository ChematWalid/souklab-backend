package com.project.souklab.dto.directory;

import com.project.souklab.model.EnumValue;

/**
 * Enumeration of supported sort orders for public artisan directory search queries.
 */
public enum DirectorySortOrder implements EnumValue {

    /**
     * Relevance scoring (Elasticsearch BM25 score) boosted by featured status.
     */
    RELEVANCE,

    /**
     * Highest rated artisans first, breaking ties by verified review count.
     */
    RATING_DESC,

    /**
     * Most reviewed artisans first, breaking ties by rating score.
     */
    REVIEWS_DESC,

    /**
     * Most viewed artisan profiles first.
     */
    VIEWS_DESC,

    /**
     * Most recently registered artisan profiles first.
     */
    NEWEST
}
