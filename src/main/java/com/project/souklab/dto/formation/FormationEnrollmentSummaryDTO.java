package com.project.souklab.dto.formation;

import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.User;

import java.time.LocalDateTime;

/**
 * Lightweight projection of a participant enrollment for the Formateur's attendance roster.
 * Uses the participant's {@link User#getPublicDisplayName() public display name}
 * to avoid exposing PII such as email or full last name.
 *
 * @param enrollmentId unique identifier of the enrollment record
 * @param participantName abbreviated public name of the enrolled artisan
 * @param status current participation status
 * @param enrolledAt timestamp when the seat was initially confirmed
 */
public record FormationEnrollmentSummaryDTO(
        String enrollmentId,
        String participantName,
        EnrollmentStatus status,
        LocalDateTime enrolledAt
) {
}
