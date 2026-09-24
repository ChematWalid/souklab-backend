package com.project.souklab.service.favorite;

import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.favorite.ClientFavoriteArtisanItemDTO;
import com.project.souklab.dto.favorite.ClientFavoriteArtisanResponseDTO;
import com.project.souklab.dto.favorite.FavoriteStatusResponseDTO;
import org.springframework.data.domain.Pageable;

/**
 * Application service contract for client artisan favorites management.
 */
public interface ArtisanFavoriteService {

    /**
     * Adds an artisan to the authenticated client's favorites.
     *
     * @param artisanId unique identifier of the target artisan
     * @return response details containing favorite ID, artisan ID, and favorited timestamp
     */
    ClientFavoriteArtisanResponseDTO addFavorite(String artisanId);

    /**
     * Retrieves a paginated list of visible favorite artisans for the authenticated client.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated response of favorite artisan items
     */
    PaginatedResponse<ClientFavoriteArtisanItemDTO> listFavorites(Pageable pageable);

    /**
     * Checks if the specified artisan is favorited by the authenticated client.
     *
     * @param artisanId unique identifier of the target artisan
     * @return status response indicating whether the artisan is favorited
     */
    FavoriteStatusResponseDTO isFavorited(String artisanId);

    /**
     * Removes an artisan from the authenticated client's favorites.
     *
     * @param artisanId unique identifier of the target artisan
     */
    void removeFavorite(String artisanId);
}
