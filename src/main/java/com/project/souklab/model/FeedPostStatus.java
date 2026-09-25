package com.project.souklab.model;

/**
 * Moderation and visibility states for feed posts.
 */
public enum FeedPostStatus implements EnumValue {
    DRAFT,
    PENDING,
    PUBLISHED,
    HIDDEN,
    REJECTED,
    REMOVED
}
