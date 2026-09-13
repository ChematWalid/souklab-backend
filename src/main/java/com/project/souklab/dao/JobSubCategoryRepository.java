package com.project.souklab.dao;

import com.project.souklab.model.JobSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link JobSubCategory} entities.
 * Supports retrieval and filtering of specialized craftsmanship trades and disciplines.
 */
public interface JobSubCategoryRepository extends JpaRepository<JobSubCategory, String> {

    /**
     * Retrieves all active craft subcategories under a given parent category ID ordered by display weight.
     *
     * @param categoryId Primary key identifier of the parent JobCategory
     * @return Ordered list of active subcategories belonging to the specified category
     */
    List<JobSubCategory> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(String categoryId);

    /**
     * Retrieves all active craft subcategories under a given parent category slug ordered by display weight.
     *
     * @param categorySlug Unique slug of the parent JobCategory (e.g., "metiers-du-bois")
     * @return Ordered list of active subcategories belonging to the specified category slug
     */
    List<JobSubCategory> findByCategorySlugAndIsActiveTrueOrderByDisplayOrderAsc(String categorySlug);

    /**
     * Finds a craft subcategory by its unique URL-friendly slug.
     *
     * @param slug Unique subcategory slug (e.g., "ebenisterie-traditionnelle")
     * @return Optional containing the matching JobSubCategory if found
     */
    Optional<JobSubCategory> findBySlug(String slug);

    /**
     * Checks if a craft subcategory exists with the specified slug.
     *
     * @param slug Subcategory slug to verify
     * @return True if a subcategory with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
}
