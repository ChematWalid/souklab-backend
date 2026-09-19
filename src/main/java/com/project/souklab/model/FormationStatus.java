package com.project.souklab.model;

/**
 * Lifecycle states of an artisan formation or masterclass.
 */
public enum FormationStatus implements EnumValue {

    /**
     * Initial draft created by the instructor artisan, not yet submitted for review.
     */
    DRAFT,

    /**
     * Submitted by the instructor and awaiting administrative moderation.
     */
    PENDING_REVIEW,

    /**
     * Reviewed and approved by an administrator, ready for publishing.
     */
    APPROVED,

    /**
     * Rejected during moderation; revisions requested from the instructor.
     */
    REJECTED,

    /**
     * Active and visible in the public masterclass catalog for peer artisan enrollment.
     */
    PUBLISHED,

    /**
     * Cancelled by the instructor or administrator prior to execution.
     */
    CANCELLED,

    /**
     * Masterclass has taken place and completed its scheduled execution.
     */
    COMPLETED
}
