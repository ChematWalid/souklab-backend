package com.project.souklab.dao;

import com.project.souklab.model.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link JobCategory} entities.
 * Supports public catalog browsing and management of top-level craft categories.
 */
public interface JobCategoryRepository extends JpaRepository<JobCategory, String> {

    /**
     * Retrieves all active craft categories ordered by display priority weight.
     *
     * @return Ordered list of active job categories
     */
    List<JobCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Finds a craft category by its unique URL-friendly slug.
     *
     * @param slug Unique category slug (e.g., "metiers-du-bois")
     * @return Optional containing the matching JobCategory if found
     */
    Optional<JobCategory> findBySlug(String slug);

    /**
     * Checks if a craft category exists with the specified slug.
     *
     * @param slug Category slug to verify
     * @return True if a category with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Checks if another category (different id) already uses the given slug.
     *
     * @param slug slug to check
     * @param id   id of the category being updated
     * @return true if a different category already holds this slug
     */
    boolean existsBySlugAndIdNot(String slug, String id);

    /**
     * Returns the current maximum displayOrder value across all categories, or 0 if empty.
     */
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM JobCategory c")
    int findMaxDisplayOrder();
}
