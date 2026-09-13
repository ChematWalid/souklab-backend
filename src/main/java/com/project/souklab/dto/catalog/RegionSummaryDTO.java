package com.project.souklab.dto.catalog;

import com.project.souklab.model.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compact summary DTO representing an artisan's geographic region.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionSummaryDTO {

    private String id;
    private String name;
    private String slug;
    private String code;

    /**
     * Maps a {@link Region} entity into a compact RegionSummaryDTO.
     *
     * @param region the region entity
     * @return summary DTO, or null if region is null
     */
    public static RegionSummaryDTO from(Region region) {
        if (region == null) {
            return null;
        }
        return RegionSummaryDTO.builder()
                .id(region.getId())
                .name(region.getName())
                .slug(region.getSlug())
                .code(region.getCode())
                .build();
    }
}
