package com.project.souklab.dto.catalog;

import com.project.souklab.model.Technique;
import lombok.Builder;
import lombok.Value;

/**
 * Data transfer object representing an artisanal crafting technique or traditional skill.
 */
@Value
@Builder
public class TechniqueDTO {

    String id;
    String name;
    String slug;
    String description;
    int displayOrder;

    /**
     * Converts a {@link Technique} entity into a TechniqueDTO.
     *
     * @param entity The Technique entity
     * @return The populated TechniqueDTO
     */
    public static TechniqueDTO from(Technique entity) {
        if (entity == null) {
            return null;
        }
        return TechniqueDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .slug(entity.getSlug())
            .description(entity.getDescription())
            .displayOrder(entity.getDisplayOrder())
            .build();
    }
}
