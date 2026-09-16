package com.project.souklab.dao;

import com.project.souklab.model.ArtisanCertification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link ArtisanCertification} entities.
 * Provides chronological listing, soft-delete filtering, and ownership-scoped queries.
 */
public interface ArtisanCertificationRepository extends JpaRepository<ArtisanCertification, String> {

    /**
     * Retrieves all active certifications for an artisan ordered by creation date descending.
     *
     * @param artisanId the unique identifier of the artisan
     * @return list of active certifications sorted newest to oldest
     */
    List<ArtisanCertification> findByArtisanIdAndDeletedAtIsNullOrderByCreatedAtDesc(String artisanId);

    /**
     * Finds a specific certification by ID and owning artisan ID filtering out soft-deleted records.
     *
     * @param id the unique identifier of the certification
     * @param artisanId the unique identifier of the owning artisan
     * @return optional containing the certification if found, active, and owned by the artisan
     */
    Optional<ArtisanCertification> findByIdAndArtisanIdAndDeletedAtIsNull(String id, String artisanId);

    /**
     * Counts the total number of active certifications currently recorded for an artisan.
     *
     * @param artisanId the unique identifier of the artisan
     * @return active certification count
     */
    long countByArtisanIdAndDeletedAtIsNull(String artisanId);

    Optional<ArtisanCertification> findByDocumentUrlAndDeletedAtIsNull(String documentUrl);
}
