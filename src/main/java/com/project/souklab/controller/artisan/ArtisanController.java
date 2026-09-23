package com.project.souklab.controller.artisan;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.profile.ArtisanPublicViewDTO;
import com.project.souklab.service.artisan.ArtisanProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/artisan")
@Tag(name = "Artisan Profile", description = "Public artisan profile viewing, view count tracking, and contact information gating")
@RequiredArgsConstructor
public class ArtisanController {

    private final ArtisanProfileService artisanProfileService;

    @GetMapping("/{id}")
    @Operation(summary = "Get artisan public profile", description = "Retrieves public profile details for an artisan by ID. Non-premium viewers receive masked contact details (name, phone, address, website); premium viewers and administrators see unmasked contact info.")
    public ResponseEntity<ApiResponse<ArtisanPublicViewDTO>> getArtisanProfile(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(artisanProfileService.getArtisanProfile(id)));
    }
}
