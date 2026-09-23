package com.project.souklab.dao;

import com.project.souklab.model.Epoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Epoque} entities.
 * Supports heritage catalog filtering and cultural era discovery across Algerian craftsmanship history.
 */
public interface EpoqueRepository extends JpaRepository<Epoque, String> {

    /**
     * Retrieves all active historical epochs ordered by chronological display weight.
     *
     * @return Ordered list of active historical epochs
     */
    List<Epoque> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Finds a historical epoch by its unique URL-friendly slug.
     *
     * @param slug Unique epoch slug (e.g., "periode-zianide")
     * @return Optional containing the matching Epoque if found
     */
    Optional<Epoque> findBySlug(String slug);

    /**
     * Checks if a historical epoch exists with the specified slug.
     *
     * @param slug Epoch slug to verify
     * @return True if an epoch with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Checks if another epoque (different id) already uses the given slug.
     * Used during updates to enforce uniqueness without triggering a self-conflict.
     *
     * @param slug slug to check
     * @param id   id of the epoque being updated
     * @return true if a different epoque already holds this slug
     */
    boolean existsBySlugAndIdNot(String slug, String id);

    /**
     * Returns the current maximum displayOrder value across all epoques, or 0 if the table is empty.
     */
    @Query("SELECT COALESCE(MAX(e.displayOrder), 0) FROM Epoque e")
    int findMaxDisplayOrder();

    /**
     * Counts how many artisans are associated with the given epoque.
     * Used to prevent hard-deletes when artisan references exist.
     *
     * @param epoqueId the epoque id to check
     * @return number of artisans linked to this epoque
     */
    @Query(value = "SELECT COUNT(*) FROM artisan_epoques WHERE epoque_id = :epoqueId", nativeQuery = true)
    int countArtisanReferences(@Param("epoqueId") String epoqueId);
}
