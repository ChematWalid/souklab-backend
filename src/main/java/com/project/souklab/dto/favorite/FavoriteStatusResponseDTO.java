package com.project.souklab.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing the favorite status of a resource for the authenticated client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStatusResponseDTO {

    private boolean favorited;

    /**
     * Factory constructor for creating a favorite status response.
     *
     * @param favorited whether the resource is favorited by the client
     * @return the constructed status response DTO
     */
    public static FavoriteStatusResponseDTO of(boolean favorited) {
        return new FavoriteStatusResponseDTO(favorited);
    }
}
