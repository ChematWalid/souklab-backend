package com.project.souklab.dto.catalog;

import com.project.souklab.model.Material;
import lombok.Builder;
import lombok.Value;

/**
 * Data transfer object representing an authentic raw crafting material.
 */
@Value
@Builder
public class MaterialDTO {

    String id;
    String name;
    String slug;
    String description;
    int displayOrder;
    String familyId;

    /**
     * Converts a {@link Material} entity into a MaterialDTO.
     *
     * @param entity The Material entity
     * @return The populated MaterialDTO
     */
    public static MaterialDTO from(Material entity) {
        if (entity == null) {
            return null;
        }
        return MaterialDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .slug(entity.getSlug())
            .description(entity.getDescription())
            .displayOrder(entity.getDisplayOrder())
            .familyId(entity.getFamily() != null ? entity.getFamily().getId() : null)
            .build();
    }
}
