package com.project.souklab.dto.catalog;

import com.project.souklab.model.Region;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * Data transfer object representing an Algerian administrative region.
 * Wilayas include a nested list of active child Communes ordered by display priority.
 */
@Value
@Builder
public class RegionDTO {

    String id;
    String name;
    String slug;
    String code;
    int displayOrder;
    List<RegionDTO> children;

    /**
     * Converts a {@link Region} entity and its child Communes into a RegionDTO.
     *
     * @param entity The Region entity
     * @param children The mapped child Communes
     * @return The populated RegionDTO
     */
    public static RegionDTO from(Region entity, List<RegionDTO> children) {
        if (entity == null) {
            return null;
        }
        return RegionDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .slug(entity.getSlug())
            .code(entity.getCode())
            .displayOrder(entity.getDisplayOrder())
            .children(children != null ? children : Collections.emptyList())
            .build();
    }

    /**
     * Converts a leaf {@link Region} entity (e.g. a Commune with no children) into a RegionDTO.
     *
     * @param entity The Region entity
     * @return The populated RegionDTO with an empty list of children
     */
    public static RegionDTO from(Region entity) {
        return from(entity, Collections.emptyList());
    }
}
