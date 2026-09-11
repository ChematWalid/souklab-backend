package com.project.souklab.dao;

import com.project.souklab.model.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link JobCategory} entities.
 * Supports public catalog browsing and management of top-level craft categories.
 */
@Repository
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
}
