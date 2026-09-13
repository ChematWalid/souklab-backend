package com.project.souklab.dto.directory;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Validated query parameter container for searching and filtering the public artisan directory.
 * Supports full-text keywords, geographic Wilaya codes/slugs, craft taxonomy specializations,
 * boolean accreditation filters, rating thresholds, and pagination controls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectorySearchFilterDTO {

    /**
     * Free-form search keywords matching against artisan names, craft narratives, cities, and addresses.
     */
    @Size(max = 120, message = "Search keyword must not exceed 120 characters")
    private String keyword;

    /**
     * URL-friendly slug representing an administrative region (Wilaya or Commune).
     */
    @Size(max = 120, message = "Region slug must not exceed 120 characters")
    private String regionSlug;

    /**
     * Official 2-digit administrative Wilaya code (e.g., '15', '16', '47').
     */
    @Size(max = 10, message = "Wilaya code must not exceed 10 characters")
    private String wilayaCode;

    /**
     * Craft parent category slug (e.g., 'art-du-feu', 'textile-tissage').
     */
    @Size(max = 120, message = "Category slug must not exceed 120 characters")
    private String categorySlug;

    /**
     * Craft trade subcategory slug (e.g., 'ceramique-poterie-kabylie', 'ebenisterie-traditionnelle').
     */
    @Size(max = 120, message = "Subcategory slug must not exceed 120 characters")
    private String subCategorySlug;

    /**
     * List of authentic material slugs to filter by (e.g., 'argile-rouge-de-kabylie', 'argent-massif-925').
     */
    @Builder.Default
    private List<String> materials = new ArrayList<>();

    /**
     * List of traditional technique slugs to filter by (e.g., 'filigrane-en-argent', 'ciselure-au-marteau').
     */
    @Builder.Default
    private List<String> techniques = new ArrayList<>();

    /**
     * List of historical design epoch slugs to filter by (e.g., 'periode-zianide', 'epoque-ottomane').
     */
    @Builder.Default
    private List<String> epoques = new ArrayList<>();

    /**
     * Minimum client rating threshold (inclusive, 0.0 to 5.0).
     */
    @DecimalMin(value = "0.0", message = "Minimum rating cannot be less than 0.0")
    @DecimalMax(value = "5.0", message = "Minimum rating cannot exceed 5.0")
    private Double minRating;

    /**
     * When true, restricts results strictly to platform-verified master artisans.
     */
    private Boolean verifiedOnly;

    /**
     * When true, restricts results strictly to active premium tier artisans.
     */
    private Boolean premiumOnly;

    /**
     * When true, restricts results strictly to certified master instructors (formateurs).
     */
    private Boolean teacherOnly;

    /**
     * Desired sort order for directory search hits. Defaults to {@link DirectorySortOrder#RELEVANCE}.
     */
    @Builder.Default
    private DirectorySortOrder sortBy = DirectorySortOrder.RELEVANCE;

    /**
     * Zero-based page index for pagination.
     */
    @Min(value = 0, message = "Page index cannot be negative")
    @Builder.Default
    private Integer page = 0;

    /**
     * Maximum number of artisan summary cards per page (bounded between 1 and 100).
     */
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;

    /**
     * Checks if a non-empty full-text keyword query is specified.
     *
     * @return true if keyword contains non-whitespace characters
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * Normalizes the search keyword by trimming leading and trailing whitespace.
     *
     * @return trimmed keyword or empty string if null
     */
    public String getCleanKeyword() {
        return keyword != null ? keyword.trim() : "";
    }

    /**
     * Determines whether any geographic or craft taxonomy filter is active.
     *
     * @return true if any regional, subcategory, material, technique, or epoch constraint is provided
     */
    public boolean hasTaxonomyFilters() {
        boolean hasGeo = isNonBlank(regionSlug) || isNonBlank(wilayaCode);
        boolean hasCategory = isNonBlank(categorySlug) || isNonBlank(subCategorySlug);
        boolean hasTaxonomyCollections = isNonEmpty(materials) || isNonEmpty(techniques) || isNonEmpty(epoques);
        return hasGeo || hasCategory || hasTaxonomyCollections;
    }

    /**
     * Safe null-coalescing page accessor defaulting to 0.
     *
     * @return page index
     */
    public int resolvePage() {
        return page != null ? page : 0;
    }

    /**
     * Safe null-coalescing page size accessor defaulting to 20.
     *
     * @return page size bounded between 1 and 100
     */
    public int resolveSize() {
        if (size == null) {
            return 20;
        }
        return Math.max(1, Math.min(size, 100));
    }

    /**
     * Safe null-coalescing sort order accessor defaulting to RELEVANCE.
     *
     * @return resolved DirectorySortOrder
     */
    public DirectorySortOrder resolveSortBy() {
        return sortBy != null ? sortBy : DirectorySortOrder.RELEVANCE;
    }

    /**
     * Alias getter for keyword, matching query parameter 'q'.
     *
     * @return search keyword
     */
    public String getQ() {
        return keyword;
    }

    /**
     * Alias setter for keyword, binding query parameter 'q'.
     *
     * @param q search keyword
     */
    public void setQ(String q) {
        this.keyword = q;
    }

    private static boolean isNonBlank(String str) {
        return str != null && !str.isBlank();
    }

    private static boolean isNonEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
