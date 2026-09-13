package com.project.souklab.dto.catalog;

import com.project.souklab.model.Technique;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compact summary DTO representing a craft technique used by an artisan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechniqueSummaryDTO {

    private String id;
    private String name;
    private String slug;

    /**
     * Maps a {@link Technique} entity into a compact TechniqueSummaryDTO.
     *
     * @param technique the technique entity
     * @return summary DTO, or null if technique is null
     */
    public static TechniqueSummaryDTO from(Technique technique) {
        if (technique == null) {
            return null;
        }
        return TechniqueSummaryDTO.builder()
                .id(technique.getId())
                .name(technique.getName())
                .slug(technique.getSlug())
                .build();
    }
}
