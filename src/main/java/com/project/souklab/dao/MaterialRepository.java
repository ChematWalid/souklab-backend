package com.project.souklab.dao;

import com.project.souklab.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Material} entities.
 * Supports catalog discovery and artisan portfolio filtering based on authentic materials.
 */
public interface MaterialRepository extends JpaRepository<Material, String> {

    /**
     * Retrieves all active materials belonging to a specified material family ID ordered by display weight.
     *
     * @param familyId Primary key identifier of the parent MaterialFamily
     * @return Ordered list of active materials in the specified family
     */
    List<Material> findByFamilyIdAndIsActiveTrueOrderByDisplayOrderAsc(String familyId);

    /**
     * Retrieves all active materials belonging to a specified material family slug ordered by display weight.
     *
     * @param familySlug Unique slug of the parent MaterialFamily (e.g., "terres-et-argiles")
     * @return Ordered list of active materials in the specified family slug
     */
    List<Material> findByFamilySlugAndIsActiveTrueOrderByDisplayOrderAsc(String familySlug);

    /**
     * Finds a material by its unique URL-friendly slug.
     *
     * @param slug Unique material slug (e.g., "argile-rouge-de-kabylie")
     * @return Optional containing the matching Material if found
     */
    Optional<Material> findBySlug(String slug);

    /**
     * Checks if a material exists with the specified slug.
     *
     * @param slug Material slug to verify
     * @return True if a material with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
}
