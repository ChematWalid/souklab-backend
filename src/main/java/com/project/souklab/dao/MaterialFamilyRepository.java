package com.project.souklab.dao;

import com.project.souklab.model.MaterialFamily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link MaterialFamily} entities.
 * Supports public reference queries and categorization of raw crafting materials.
 */
public interface MaterialFamilyRepository extends JpaRepository<MaterialFamily, String> {

    /**
     * Retrieves all active material families ordered by display weight.
     *
     * @return Ordered list of active material families
     */
    List<MaterialFamily> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Finds a material family by its unique URL-friendly slug.
     *
     * @param slug Unique material family slug (e.g., "terres-et-argiles")
     * @return Optional containing the matching MaterialFamily if found
     */
    Optional<MaterialFamily> findBySlug(String slug);

    /**
     * Checks if a material family exists with the specified slug.
     *
     * @param slug Material family slug to verify
     * @return True if a material family with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
}
