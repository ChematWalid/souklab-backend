package com.project.souklab.controller.formateur;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.formateur.FormateurRequestDTO;
import com.project.souklab.dto.formateur.FormateurRequestResponseDTO;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.service.formateur.ArtisanFormateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Instructor Accreditation", description = "Artisan applications for masterclass instructor (formateur) accreditation")
@RestController
@RequestMapping("/api/v1/artisan")
@RequiredArgsConstructor
@PreAuthorize("@accessControl.canManageArtisanContent(authentication)")
public class ArtisanFormateurController {

    private final ArtisanFormateurService artisanFormateurService;

    @Operation(summary = "Submit formateur accreditation request", description = "Submit an application to be accredited as a masterclass instructor.")
    @PostMapping("/formateur-request")
    public ResponseEntity<ApiResponse<FormateurRequestResponseDTO>> submitRequest(@Valid @RequestBody(required = false) FormateurRequestDTO dto) {
        FormateurRequestResponseDTO response = artisanFormateurService.submitRequest(dto != null ? dto : new FormateurRequestDTO());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Formateur request submitted successfully."));
    }

    @Operation(summary = "Get latest formateur request", description = "Retrieves the authenticated artisan's latest accreditation request.")
    @GetMapping("/formateur-request")
    public ResponseEntity<ApiResponse<FormateurRequestResponseDTO>> getLatestRequest() {
        return ResponseEntity.ok(ApiResponse.success(artisanFormateurService.getLatestRequest(), "Formateur request retrieved successfully."));
    }

    @Operation(summary = "Get formateur request history", description = "Retrieves paginated accreditation request history for the authenticated artisan.")
    @GetMapping("/formateur-requests")
    public ResponseEntity<ApiResponse<PaginatedResponse<FormateurRequestResponseDTO>>> getRequestHistory(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(artisanFormateurService.getRequestHistory(pageable), "Formateur request history retrieved successfully."));
    }
}
