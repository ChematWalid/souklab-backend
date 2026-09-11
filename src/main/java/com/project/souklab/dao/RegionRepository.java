package com.project.souklab.dao;

import com.project.souklab.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Region} entities.
 * Supports hierarchical geographical traversal across Algerian Wilayas and Communes.
 */
@Repository
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
}
