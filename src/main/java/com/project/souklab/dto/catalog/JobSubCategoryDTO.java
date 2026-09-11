package com.project.souklab.dto.catalog;

import com.project.souklab.model.JobSubCategory;
import lombok.Builder;
import lombok.Value;

/**
 * Data transfer object representing a specialized craft trade or subcategory.
 */
@Value
@Builder
public class JobSubCategoryDTO {

    String id;
    String name;
    String slug;
    String description;
    int displayOrder;
    String categoryId;

    /**
     * Converts a {@link JobSubCategory} entity into a JobSubCategoryDTO.
     *
     * @param entity The JobSubCategory entity
     * @return The populated JobSubCategoryDTO
     */
    public static JobSubCategoryDTO from(JobSubCategory entity) {
        if (entity == null) {
            return null;
        }
        return JobSubCategoryDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .slug(entity.getSlug())
            .description(entity.getDescription())
            .displayOrder(entity.getDisplayOrder())
            .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
            .build();
    }
}
