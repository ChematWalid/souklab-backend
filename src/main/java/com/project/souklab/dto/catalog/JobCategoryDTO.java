package com.project.souklab.dto.catalog;

import com.project.souklab.model.JobCategory;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * Data transfer object representing a top-level artisanal craft category.
 * Contains a nested list of specialized subcategories under this domain.
 */
@Value
@Builder
public class JobCategoryDTO {

    String id;
    String name;
    String slug;
    String description;
    String iconUrl;
    int displayOrder;
    List<JobSubCategoryDTO> subCategories;

    /**
     * Converts a {@link JobCategory} entity and its child subcategories into a JobCategoryDTO.
     *
     * @param entity The JobCategory entity
     * @param subCategories The mapped child subcategories
     * @return The populated JobCategoryDTO
     */
    public static JobCategoryDTO from(JobCategory entity, List<JobSubCategoryDTO> subCategories) {
        if (entity == null) {
            return null;
        }
        return JobCategoryDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .slug(entity.getSlug())
            .description(entity.getDescription())
            .iconUrl(entity.getIconUrl())
            .displayOrder(entity.getDisplayOrder())
            .subCategories(subCategories != null ? subCategories : Collections.emptyList())
            .build();
    }
}
