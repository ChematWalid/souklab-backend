package com.project.souklab.dao;

import com.project.souklab.model.JobSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Checks if another subcategory (different id) already uses the given slug.
     *
     * @param slug slug to check
     * @param id   id of the subcategory being updated
     * @return true if a different subcategory already holds this slug
     */
    boolean existsBySlugAndIdNot(String slug, String id);

    /**
     * Checks if any subcategories exist under the specified parent category ID.
     */
    boolean existsByCategoryId(String categoryId);

    /**
     * Returns the maximum displayOrder value for subcategories belonging to a parent category, or 0.
     */
    @Query("SELECT COALESCE(MAX(s.displayOrder), 0) FROM JobSubCategory s WHERE s.category.id = :categoryId")
    int findMaxDisplayOrderByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Counts how many artisans reference this subcategory via artisans.sub_category_id.
     */
    @Query(value = "SELECT COUNT(*) FROM artisans WHERE sub_category_id = :subCategoryId", nativeQuery = true)
    int countArtisanReferences(@Param("subCategoryId") String subCategoryId);
}
