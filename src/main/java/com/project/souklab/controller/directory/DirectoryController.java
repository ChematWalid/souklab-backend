package com.project.souklab.controller.directory;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.directory.ArtisanDirectoryCardDTO;
import com.project.souklab.dto.directory.DirectorySearchFilterDTO;
import com.project.souklab.service.directory.DirectorySearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for the artisan marketplace directory and faceted search engine.
 * Requires client authentication. Non-premium viewers receive directory cards with masked
 * artisan names; premium viewers and administrators receive real artisan identities.
 */
@RestController
@RequestMapping("/api/v1/public/directory")
@Tag(name = "Artisan Directory", description = "Faceted artisan discovery directory and full-text search with contact privacy gating")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectorySearchService directorySearchService;

    /**
     * Searches and filters the artisan directory for authenticated viewers.
     *
     * <p>Requires an authenticated session. Anonymous callers receive a 403 Forbidden response.
     * Non-premium viewers receive directory cards with the artisan name anonymised;
     * premium viewers and administrators see the real artisan name.
     *
     * @param filter validated directory search criteria and pagination options
     * @return 200 OK with paginated list of matching artisan directory cards
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search artisan directory", description = "Faceted full-text search across verified artisans supporting keyword, category, wilaya, rating, materials, techniques, and epochs. Requires authentication. Non-premium viewers receive directory cards with masked names ('Artisan #XXXXX'); premium viewers and administrators receive real artisan names.")
    public ResponseEntity<ApiResponse<PaginatedResponse<ArtisanDirectoryCardDTO>>> search(
            @Valid @ModelAttribute DirectorySearchFilterDTO filter
    ) {
        PaginatedResponse<ArtisanDirectoryCardDTO> response = directorySearchService.search(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
