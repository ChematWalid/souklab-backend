package com.project.souklab.dao;

import com.project.souklab.model.Technique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Technique} entities.
 * Supports catalog browsing and artisan portfolio queries by traditional handcrafting techniques.
 */
public interface TechniqueRepository extends JpaRepository<Technique, String> {

    /**
     * Retrieves all active craft techniques ordered by display weight.
     *
     * @return Ordered list of active craftsmanship techniques
     */
    List<Technique> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Finds a craft technique by its unique URL-friendly slug.
     *
     * @param slug Unique technique slug (e.g., "filigrane-en-argent")
     * @return Optional containing the matching Technique if found
     */
    Optional<Technique> findBySlug(String slug);

    /**
     * Checks if a craft technique exists with the specified slug.
     *
     * @param slug Technique slug to verify
     * @return True if a technique with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Checks if another technique (different id) already uses the given slug.
     * Used during updates to enforce uniqueness without triggering a self-conflict.
     *
     * @param slug slug to check
     * @param id   id of the technique being updated
     * @return true if a different technique already holds this slug
     */
    boolean existsBySlugAndIdNot(String slug, String id);

    /**
     * Returns the current maximum displayOrder value across all techniques, or 0 if the table is empty.
     * Used to auto-assign the next display order position.
     */
    @Query("SELECT COALESCE(MAX(t.displayOrder), 0) FROM Technique t")
    int findMaxDisplayOrder();

    /**
     * Counts how many artisans are associated with the given technique.
     * Used to prevent hard-deletes when artisan references exist.
     *
     * @param techniqueId the technique id to check
     * @return number of artisans linked to this technique
     */
    @Query(value = "SELECT COUNT(*) FROM artisan_techniques WHERE technique_id = :techniqueId", nativeQuery = true)
    int countArtisanReferences(@Param("techniqueId") String techniqueId);
}
