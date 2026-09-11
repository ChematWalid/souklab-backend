package com.project.souklab.dto.catalog;

import com.project.souklab.model.Epoque;
import lombok.Builder;
import lombok.Value;

/**
 * Data transfer object representing an Algerian historical epoch or cultural era.
 */
@Value
@Builder
public class EpoqueDTO {

    String id;
    String name;
    String slug;
    String periodEra;
    String description;
    int displayOrder;

    /**
     * Converts an {@link Epoque} entity into an EpoqueDTO.
     *
     * @param entity The Epoque entity
     * @return The populated EpoqueDTO
     */
    public static EpoqueDTO from(Epoque entity) {
        if (entity == null) {
            return null;
        }
        return EpoqueDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .slug(entity.getSlug())
            .periodEra(entity.getPeriodEra())
            .description(entity.getDescription())
            .displayOrder(entity.getDisplayOrder())
            .build();
    }
}
