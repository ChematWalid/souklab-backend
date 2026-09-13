package com.project.souklab.dto.catalog;

import com.project.souklab.model.Epoque;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compact summary DTO representing a historical era or epoque associated with an artisan's creations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpoqueSummaryDTO {

    private String id;
    private String name;
    private String slug;
    private String periodEra;

    /**
     * Maps an {@link Epoque} entity into a compact EpoqueSummaryDTO.
     *
     * @param epoque the epoque entity
     * @return summary DTO, or null if epoque is null
     */
    public static EpoqueSummaryDTO from(Epoque epoque) {
        if (epoque == null) {
            return null;
        }
        return EpoqueSummaryDTO.builder()
                .id(epoque.getId())
                .name(epoque.getName())
                .slug(epoque.getSlug())
                .periodEra(epoque.getPeriodEra())
                .build();
    }
}
