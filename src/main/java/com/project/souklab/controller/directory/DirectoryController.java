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

/**
 * Public REST controller for the artisan directory and faceted search engine.
 * Exposes full-text search, geographic filtering, and craft taxonomy discovery
 * without requiring client authentication.
 */
@RestController
@RequestMapping("/api/v1/public/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectorySearchService directorySearchService;

    /**
     * Searches and filters the artisan directory for authenticated viewers.
     *
     * <p>Requires an authenticated session. Anonymous callers receive a 401 response.
     * Non-premium viewers receive directory cards with the artisan name anonymised;
     * premium viewers and administrators see the real artisan name.
     *
     * @param filter validated directory search criteria and pagination options
     * @return 200 OK with paginated list of matching artisan directory cards
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginatedResponse<ArtisanDirectoryCardDTO>>> search(
            @Valid @ModelAttribute DirectorySearchFilterDTO filter
    ) {
        PaginatedResponse<ArtisanDirectoryCardDTO> response = directorySearchService.search(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
