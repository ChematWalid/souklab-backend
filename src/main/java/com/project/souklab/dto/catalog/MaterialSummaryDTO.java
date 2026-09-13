package com.project.souklab.dto.catalog;

import com.project.souklab.model.Material;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compact summary DTO representing a raw material used by an artisan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialSummaryDTO {

    private String id;
    private String name;
    private String slug;
    private String familyId;
    private String familyName;

    /**
     * Maps a {@link Material} entity into a compact MaterialSummaryDTO.
     *
     * @param material the material entity
     * @return summary DTO, or null if material is null
     */
    public static MaterialSummaryDTO from(Material material) {
        if (material == null) {
            return null;
        }
        return MaterialSummaryDTO.builder()
                .id(material.getId())
                .name(material.getName())
                .slug(material.getSlug())
                .familyId(material.getFamily() != null ? material.getFamily().getId() : null)
                .familyName(material.getFamily() != null ? material.getFamily().getName() : null)
                .build();
    }
}
