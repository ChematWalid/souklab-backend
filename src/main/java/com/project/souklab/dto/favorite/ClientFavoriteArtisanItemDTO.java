package com.project.souklab.dto.favorite;

import com.project.souklab.dto.directory.ArtisanDirectoryCardDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object representing an artisan favorite entry within a paginated list response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFavoriteArtisanItemDTO {

    private LocalDateTime favoritedAt;
    private ArtisanDirectoryCardDTO artisan;
}
