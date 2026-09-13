package com.project.souklab.model;

/**
 * Participation status of an artisan enrolled in a formation.
 */
public enum EnrollmentStatus {

    /**
     * Enrollment is confirmed and seat is reserved.
     */
    CONFIRMED,

    /**
     * Enrolled artisan attended the scheduled formation session.
     */
    ATTENDED,

    /**
     * Enrollment was cancelled prior to the cancellation cutoff deadline.
     */
    CANCELLED
}
