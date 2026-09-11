package com.project.souklab.dto.catalog;

import com.project.souklab.model.MaterialFamily;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * Data transfer object representing a raw material family.
 * Contains a nested list of authentic crafting materials belonging to this family.
 */
@Value
@Builder
public class MaterialFamilyDTO {

    String id;
    String name;
    String slug;
    String description;
    int displayOrder;
    List<MaterialDTO> materials;

    /**
     * Converts a {@link MaterialFamily} entity and its child materials into a MaterialFamilyDTO.
     *
     * @param entity The MaterialFamily entity
     * @param materials The mapped child materials
     * @return The populated MaterialFamilyDTO
     */
    public static MaterialFamilyDTO from(MaterialFamily entity, List<MaterialDTO> materials) {
        if (entity == null) {
            return null;
        }
        return MaterialFamilyDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .slug(entity.getSlug())
            .description(entity.getDescription())
            .displayOrder(entity.getDisplayOrder())
            .materials(materials != null ? materials : Collections.emptyList())
            .build();
    }
}
