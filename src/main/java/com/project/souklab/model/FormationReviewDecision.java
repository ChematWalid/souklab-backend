package com.project.souklab.model;

/**
 * Administrative moderation decision on a submitted formation.
 */
public enum FormationReviewDecision implements EnumValue {

    /**
     * Formation content, schedule, and pricing approved for publishing.
     */
    APPROVED,

    /**
     * Formation rejected; instructor must revise details before resubmitting.
     */
    REJECTED
}
