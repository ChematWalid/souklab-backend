package com.project.souklab.dao;

import com.project.souklab.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Region} entities.
 * Supports hierarchical geographical traversal across Algerian Wilayas and Communes.
 */
public interface RegionRepository extends JpaRepository<Region, String> {

    /**
     * Retrieves all active top-level administrative regions (Wilayas) ordered by display weight.
     *
     * @return List of active Wilayas with null parent references
     */
    List<Region> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Retrieves all active subordinate administrative regions (Communes/Daïras) belonging to a specified parent Wilaya.
     *
     * @param parentId Identifier of the parent Wilaya
     * @return List of active child Communes ordered by display weight
     */
    List<Region> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(String parentId);

    /**
     * Finds a region by its unique URL-friendly slug.
     *
     * @param slug Unique regional slug (e.g., "tizi-ouzou", "ghardaia")
     * @return Optional containing the matching Region if found
     */
    Optional<Region> findBySlug(String slug);

    /**
     * Finds a region by its official administrative Wilaya code.
     *
     * @param code Administrative code string (e.g., "15", "47", "16")
     * @return Optional containing the matching Region if found
     */
    Optional<Region> findByCode(String code);

    /**
     * Checks if a region exists with the specified slug.
     *
     * @param slug Regional slug to verify
     * @return True if a region with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Checks if another region (different id) already uses the given slug.
     * Used during updates to enforce uniqueness without triggering a self-conflict.
     *
     * @param slug slug to check
     * @param id   id of the region being updated
     * @return true if a different region already holds this slug
     */
    boolean existsBySlugAndIdNot(String slug, String id);

    /**
     * Returns the current maximum displayOrder value across all regions, or 0 if the table is empty.
     */
    @Query("SELECT COALESCE(MAX(r.displayOrder), 0) FROM Region r")
    int findMaxDisplayOrder();

    /**
     * Returns true if any region references the given region as its parent.
     * Used to prevent hard-deletes when child regions exist.
     *
     * @param parentId id of the region to check for children
     * @return true if at least one child region exists
     */
    boolean existsByParentId(String parentId);

    /**
     * Loads the full ancestor chain for a given region by walking parent_id links iteratively.
     * Used for circular reference detection: if {@code regionId} appears in the ancestor chain
     * of its proposed {@code newParentId}, setting that parent would form a cycle.
     *
     * <p>The query starts at {@code startId} and collects all ancestors up to the root.
     * Returns the list of ancestor region ids (not including the start node itself).
     *
     * @param startId the proposed parent id whose ancestor chain to collect
     * @return ordered list of ancestor ids from immediate parent to root
     */
    @Query(value = """
            WITH RECURSIVE ancestors AS (
                SELECT id, parent_id FROM regions WHERE id = :startId
                UNION ALL
                SELECT r.id, r.parent_id FROM regions r
                INNER JOIN ancestors a ON r.id = a.parent_id
            )
            SELECT id FROM ancestors WHERE id != :startId
            """, nativeQuery = true)
    List<String> findAncestorIds(@Param("startId") String startId);
}
