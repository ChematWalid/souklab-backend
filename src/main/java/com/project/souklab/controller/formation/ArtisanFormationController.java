package com.project.souklab.controller.formation;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.formation.FormationCreateDTO;
import com.project.souklab.dto.formation.FormationFileResponseDTO;
import com.project.souklab.dto.formation.FormationResponseDTO;
import com.project.souklab.dto.formation.FormationSummaryDTO;
import com.project.souklab.dto.formation.FormationUpdateDTO;
import com.project.souklab.service.formation.FormationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for authenticated artisan masterclass authoring operations.
 * Handles formation drafts, updates, thumbnail and course material uploads,
 * review submission, and lifecycle soft deletion.
 */
@Tag(name = "Masterclass Authoring", description = "Artisan masterclass course authoring, syllabus uploads, and review submissions")
@RestController
@RequestMapping("/api/v1/artisan/formations")
@PreAuthorize("@accessControl.canManageArtisanFormations(authentication)")
@RequiredArgsConstructor
public class ArtisanFormationController {

    private final FormationService formationService;

    /**
     * Creates a new formation draft for the authenticated accredited instructor artisan.
     *
     * @param dto formation creation payload
     * @return 201 Created with created formation response DTO
     */
    @Operation(summary = "Create masterclass draft", description = "Create a new formation draft. Requires accredited instructor status (isTeacher = true).")
    @PostMapping
    public ResponseEntity<ApiResponse<FormationResponseDTO>> createFormation(
            @Valid @RequestBody FormationCreateDTO dto
    ) {
        FormationResponseDTO created = formationService.createFormation(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Formation draft created successfully."));
    }

    /**
     * Updates an authored formation.
     *
     * @param id formation unique identifier
     * @param dto formation update payload
     * @return 200 OK with updated formation response DTO
     */
    @Operation(summary = "Update masterclass", description = "Updates curriculum, price, and schedule of an authored masterclass.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FormationResponseDTO>> updateFormation(
            @PathVariable String id,
            @Valid @RequestBody FormationUpdateDTO dto
    ) {
        FormationResponseDTO updated = formationService.updateFormation(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Formation updated successfully."));
    }

    /**
     * Uploads a showcase thumbnail image for the formation.
     *
     * @param id formation unique identifier
     * @param file multipart image file
     * @return 200 OK with updated formation response DTO
     */
    @Operation(summary = "Upload formation thumbnail", description = "Upload a showcase thumbnail photograph for the masterclass (multipart/form-data).")
    @PostMapping(value = "/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FormationResponseDTO>> uploadThumbnail(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file
    ) {
        FormationResponseDTO updated = formationService.uploadThumbnail(id, file);
        return ResponseEntity.ok(ApiResponse.success(updated, "Thumbnail uploaded successfully."));
    }

    /**
     * Uploads a downloadable course document attachment for the formation.
     *
     * @param id formation unique identifier
     * @param file multipart attachment file
     * @return 201 Created with uploaded formation file response DTO
     */
    @Operation(summary = "Upload course attachment", description = "Uploads a course syllabus or resource document attachment (PDF/doc, multipart/form-data).")
    @PostMapping(value = "/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FormationFileResponseDTO>> uploadCourseFile(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file
    ) {
        FormationFileResponseDTO fileDTO = formationService.uploadCourseFile(id, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(fileDTO, "Course file uploaded successfully."));
    }

    /**
     * Soft deletes an attachment file associated with an authored formation.
     *
     * @param id formation unique identifier
     * @param fileId course file unique identifier
     * @return 200 OK confirmation
     */
    @Operation(summary = "Delete course attachment", description = "Deletes a syllabus or resource document attachment from an authored formation.")
    @DeleteMapping("/{id}/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourseFile(
            @PathVariable String id,
            @PathVariable String fileId
    ) {
        formationService.deleteCourseFile(id, fileId);
        return ResponseEntity.ok(ApiResponse.success(null, "Course file deleted successfully."));
    }

    /**
     * Submits a draft or rejected formation for administrative moderation.
     *
     * @param id formation unique identifier
     * @return 200 OK with updated formation response DTO
     */
    @Operation(summary = "Submit masterclass for review", description = "Submits a draft or rejected masterclass for administrative approval.")
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<FormationResponseDTO>> submitForReview(
            @PathVariable String id
    ) {
        FormationResponseDTO submitted = formationService.submitForReview(id);
        return ResponseEntity.ok(ApiResponse.success(submitted, "Formation submitted for review successfully."));
    }

    /**
     * Soft deletes an authored formation.
     *
     * @param id formation unique identifier
     * @return 200 OK confirmation
     */
    @Operation(summary = "Delete masterclass", description = "Soft-deletes an authored masterclass.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFormation(
            @PathVariable String id
    ) {
        formationService.deleteFormation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Formation deleted successfully."));
    }

    /**
     * Retrieves all formations authored by the authenticated artisan.
     *
     * @param pageable pagination parameters
     * @return 200 OK with paginated list of formation summaries
     */
    @Operation(summary = "List authored masterclasses", description = "Retrieves paginated masterclasses authored by the authenticated artisan.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PaginatedResponse<FormationSummaryDTO>>> getMyFormations(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PaginatedResponse<FormationSummaryDTO> response = formationService.getMyFormations(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves full formation details for an authored formation.
     *
     * @param id formation unique identifier
     * @return 200 OK with complete formation details
     */
    @Operation(summary = "Get authored masterclass details", description = "Retrieves complete formation details including syllabus materials and moderation history.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FormationResponseDTO>> getFormationDetails(
            @PathVariable String id
    ) {
        FormationResponseDTO response = formationService.getFormationDetails(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
