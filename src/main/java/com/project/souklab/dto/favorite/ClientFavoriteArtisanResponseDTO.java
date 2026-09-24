package com.project.souklab.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object returned when an artisan is added to the authenticated client's favorites.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFavoriteArtisanResponseDTO {

    private String favoriteId;
    private String artisanId;
    private LocalDateTime favoritedAt;
}
