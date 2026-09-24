package com.project.souklab.controller.favorite;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.favorite.ClientFavoriteArtisanItemDTO;
import com.project.souklab.dto.favorite.ClientFavoriteArtisanResponseDTO;
import com.project.souklab.dto.favorite.FavoriteStatusResponseDTO;
import com.project.souklab.service.favorite.ArtisanFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authenticated client artisan favorites management.
 * Provides endpoints to bookmark artisans, list paginated favorites, check favorite status, and remove favorites.
 */
@RestController
@RequestMapping("/api/v1/client/favorites/artisans")
@Tag(name = "Client Favorites", description = "Endpoints for managing client artisan favorites (/api/v1/client/favorites/artisans)")
@RequiredArgsConstructor
public class ClientFavoriteArtisanController {

    private final ArtisanFavoriteService artisanFavoriteService;

    /**
     * Adds an artisan to the authenticated client's favorites list.
     *
     * @param artisanId unique identifier of the target artisan
     * @return 201 Created with favorite metadata
     */
    @PostMapping("/{artisanId}")
    @PreAuthorize("@accessControl.canManageFavorites(authentication)")
    @Operation(
            summary = "Add favorite artisan",
            description = "Adds an artisan to the authenticated client's favorites.",
            operationId = "addFavoriteArtisan"
    )
    public ResponseEntity<ApiResponse<ClientFavoriteArtisanResponseDTO>> addFavorite(@PathVariable String artisanId) {
        ClientFavoriteArtisanResponseDTO response = artisanFavoriteService.addFavorite(artisanId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Artisan added to favorites successfully"));
    }

    /**
     * Retrieves a paginated list of visible favorite artisans for the authenticated client.
     *
     * @param pageable pagination and sorting parameters (default 20, max 100, sorted by createdAt desc)
     * @return 200 OK with paginated favorite items
     */
    @GetMapping
    @PreAuthorize("@accessControl.canManageFavorites(authentication)")
    @Operation(
            summary = "List favorite artisans",
            description = "Retrieves a paginated list of visible favorite artisans for the authenticated client.",
            operationId = "listFavoriteArtisans"
    )
    public ResponseEntity<ApiResponse<PaginatedResponse<ClientFavoriteArtisanItemDTO>>> listFavorites(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PaginatedResponse<ClientFavoriteArtisanItemDTO> response = artisanFavoriteService.listFavorites(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Checks if the specified artisan is currently favorited by the authenticated client.
     *
     * @param artisanId unique identifier of the target artisan
     * @return 200 OK with favorite status
     */
    @GetMapping("/{artisanId}/status")
    @PreAuthorize("@accessControl.canManageFavorites(authentication)")
    @Operation(
            summary = "Check favorite artisan status",
            description = "Checks whether an artisan is favorited by the authenticated client.",
            operationId = "getFavoriteArtisanStatus"
    )
    public ResponseEntity<ApiResponse<FavoriteStatusResponseDTO>> isFavorited(@PathVariable String artisanId) {
        FavoriteStatusResponseDTO response = artisanFavoriteService.isFavorited(artisanId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Removes an artisan from the authenticated client's favorites list.
     *
     * @param artisanId unique identifier of the target artisan
     * @return 200 OK with null data
     */
    @DeleteMapping("/{artisanId}")
    @PreAuthorize("@accessControl.canManageFavorites(authentication)")
    @Operation(
            summary = "Remove favorite artisan",
            description = "Removes an artisan from the authenticated client's favorites.",
            operationId = "removeFavoriteArtisan"
    )
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable String artisanId) {
        artisanFavoriteService.removeFavorite(artisanId);
        return ResponseEntity.ok(ApiResponse.success(null, "Artisan removed from favorites successfully"));
    }
}
