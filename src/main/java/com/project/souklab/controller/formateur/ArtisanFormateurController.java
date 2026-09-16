package com.project.souklab.controller.formateur;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.formateur.FormateurRequestDTO;
import com.project.souklab.dto.formateur.FormateurRequestResponseDTO;
import com.project.souklab.service.formateur.ArtisanFormateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/artisan/formateur-request")
@RequiredArgsConstructor
@PreAuthorize("@accessControl.canManageArtisanContent(authentication)")
public class ArtisanFormateurController {

    private final ArtisanFormateurService artisanFormateurService;

    @PostMapping
    public ResponseEntity<ApiResponse<FormateurRequestResponseDTO>> submitRequest(@Valid @RequestBody(required = false) FormateurRequestDTO dto) {
        FormateurRequestResponseDTO response = artisanFormateurService.submitRequest(dto != null ? dto : new FormateurRequestDTO());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Formateur request submitted successfully."));
    }
}
