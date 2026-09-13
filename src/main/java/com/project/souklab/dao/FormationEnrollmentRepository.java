package com.project.souklab.dao;

import com.project.souklab.model.EnrollmentStatus;
import com.project.souklab.model.FormationEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access repository for {@link FormationEnrollment} entities.
 * Manages participant seat reservations, status transitions, and capacity validations.
 */
@Repository
public interface FormationEnrollmentRepository extends JpaRepository<FormationEnrollment, String> {

    /**
     * Checks if an enrollment exists matching the given formation, artisan, and status.
     *
     * @param formationId the unique identifier of the formation
     * @param artisanId the unique identifier of the artisan
     * @param status the enrollment status
     * @return true if an enrollment with the specified parameters exists, false otherwise
     */
    boolean existsByFormationIdAndArtisanIdAndStatus(String formationId, String artisanId, EnrollmentStatus status);

    /**
     * Finds an enrollment by formation ID, artisan ID, and enrollment status.
     *
     * @param formationId the unique identifier of the formation
     * @param artisanId the unique identifier of the artisan
     * @param status the enrollment status
     * @return optional containing the matching enrollment if found
     */
    Optional<FormationEnrollment> findByFormationIdAndArtisanIdAndStatus(String formationId, String artisanId, EnrollmentStatus status);

    /**
     * Finds an enrollment record for an artisan in a formation regardless of status,
     * supporting registration re-activation workflows.
     *
     * @param formationId the unique identifier of the formation
     * @param artisanId the unique identifier of the artisan
     * @return optional containing the existing enrollment record if present
     */
    Optional<FormationEnrollment> findByFormationIdAndArtisanId(String formationId, String artisanId);

    /**
     * Counts the total number of enrollments for a formation matching a specific status.
     *
     * @param formationId the unique identifier of the formation
     * @param status the enrollment status to filter by
     * @return count of matching enrollments
     */
    long countByFormationIdAndStatus(String formationId, EnrollmentStatus status);

    /**
     * Retrieves paginated active enrollments for a given artisan, excluding soft-deleted records.
     *
     * @param artisanId the unique identifier of the artisan
     * @param pageable pagination and sorting parameters
     * @return page of artisan enrollments
     */
    Page<FormationEnrollment> findByArtisanIdAndDeletedAtIsNull(String artisanId, Pageable pageable);
}
