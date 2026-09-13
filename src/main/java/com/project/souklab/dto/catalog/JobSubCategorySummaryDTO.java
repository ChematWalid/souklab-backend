package com.project.souklab.dto.catalog;

import com.project.souklab.model.JobSubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compact summary DTO representing an artisan's craft trade / subcategory.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSubCategorySummaryDTO {

    private String id;
    private String name;
    private String slug;
    private String categoryId;
    private String categoryName;

    /**
     * Maps a {@link JobSubCategory} entity into a compact JobSubCategorySummaryDTO.
     *
     * @param subCategory the subcategory entity
     * @return summary DTO, or null if subCategory is null
     */
    public static JobSubCategorySummaryDTO from(JobSubCategory subCategory) {
        if (subCategory == null) {
            return null;
        }
        return JobSubCategorySummaryDTO.builder()
                .id(subCategory.getId())
                .name(subCategory.getName())
                .slug(subCategory.getSlug())
                .categoryId(subCategory.getCategory() != null ? subCategory.getCategory().getId() : null)
                .categoryName(subCategory.getCategory() != null ? subCategory.getCategory().getName() : null)
                .build();
    }
}
