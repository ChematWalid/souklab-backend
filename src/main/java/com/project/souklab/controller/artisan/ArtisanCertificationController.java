package com.project.souklab.controller.artisan;

import com.project.souklab.dto.artisan.CertificationResponseDTO;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.service.artisan.ArtisanCertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for authenticated artisan professional credentials and certifications.
 * Handles certification document uploads, listing, and soft-deletion operations.
 */
@Tag(name = "Artisan Certifications", description = "Professional credentials and qualification certificates management for artisans")
@RestController
@RequestMapping("/api/v1/artisan/certifications")
@PreAuthorize("@accessControl.canManageArtisanContent(authentication)")
@RequiredArgsConstructor
public class ArtisanCertificationController {

    private final ArtisanCertificationService artisanCertificationService;

    /**
     * Uploads and records an official certification or qualification document for the authenticated artisan.
     *
     * @param file multipart certification scan or document
     * @param title title of credential
     * @param issuer issuing institution
     * @param issuedAt date of issuance if applicable
     * @param expiresAt date of expiration if applicable
     * @return 201 Created with uploaded certification DTO
     */
    @Operation(summary = "Upload certification document", description = "Upload and record an official qualification or certification document (PDF/image, multipart/form-data).")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CertificationResponseDTO>> uploadCertification(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("issuer") String issuer,
            @RequestParam(value = "issuedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issuedAt,
            @RequestParam(value = "expiresAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt
    ) {
        CertificationResponseDTO dto = artisanCertificationService.uploadCertification(
                file, title, issuer, issuedAt, expiresAt
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(dto, "Certification uploaded successfully"));
    }

    /**
     * Retrieves all professional certifications recorded for the authenticated artisan.
     *
     * @return 200 OK with list of certifications
     */
    @Operation(summary = "Get artisan certifications", description = "Retrieve all professional certifications and credentials belonging to the authenticated artisan.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CertificationResponseDTO>>> getMyCertifications() {
        List<CertificationResponseDTO> certifications = artisanCertificationService.getMyCertifications();
        return ResponseEntity.ok(ApiResponse.success(certifications, "Certifications retrieved successfully"));
    }

    /**
     * Deletes a professional certification belonging to the authenticated artisan.
     *
     * @param id the unique identifier of the certification to delete
     * @return 200 OK with success confirmation
     */
    @Operation(summary = "Delete certification", description = "Soft-deletes a professional qualification credential by its identifier.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCertification(@PathVariable("id") String id) {
        artisanCertificationService.deleteCertification(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Certification deleted successfully"));
    }
}
