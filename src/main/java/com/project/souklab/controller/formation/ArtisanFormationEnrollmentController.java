package com.project.souklab.controller.formation;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.formation.FormationEnrollmentDetailDTO;
import com.project.souklab.dto.formation.FormationEnrollmentResponseDTO;
import com.project.souklab.dto.formation.FormationPublicViewDTO;
import com.project.souklab.dto.formation.FormationSummaryDTO;
import com.project.souklab.filestorage.StorageResource;
import com.project.souklab.service.formation.FormationEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * REST controller managing peer artisan workshop discovery, workshop enrollment reservations,
 * cancellation workflows, enrollment history, and protected course document downloads.
 */
@RestController
@RequestMapping("/api/v1/artisan/formations")
@PreAuthorize("@accessControl.isArtisan(authentication)")
@RequiredArgsConstructor
@Slf4j
public class ArtisanFormationEnrollmentController {

    private final FormationEnrollmentService formationEnrollmentService;

    /**
     * Browses the public catalog of published masterclass formations.
     *
     * @param pageable pagination and sorting parameters
     * @return 200 OK containing paginated formation summary cards
     */
    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<PaginatedResponse<FormationSummaryDTO>>> getPublishedCatalog(
            @PageableDefault(size = 10, sort = "scheduledAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<FormationSummaryDTO> page = formationEnrollmentService.getPublishedCatalog(pageable);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.from(page)));
    }

    /**
     * Retrieves detailed public information for a published masterclass formation.
     *
     * @param id formation unique identifier
     * @return 200 OK containing comprehensive public view representation
     */
    @GetMapping("/catalog/{id}")
    public ResponseEntity<ApiResponse<FormationPublicViewDTO>> getPublishedFormationDetails(
            @PathVariable String id
    ) {
        FormationPublicViewDTO details = formationEnrollmentService.getPublishedFormationDetails(id);
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    /**
     * Enrolls the authenticated artisan into a published masterclass formation.
     *
     * @param id formation unique identifier
     * @return 200 OK with confirmed enrollment response DTO
     */
    @PostMapping("/{id}/enroll")
    public ResponseEntity<ApiResponse<FormationEnrollmentResponseDTO>> enroll(
            @PathVariable String id
    ) {
        FormationEnrollmentResponseDTO enrolled = formationEnrollmentService.enrollInFormation(id);
        return ResponseEntity.ok(ApiResponse.success(enrolled, "Enrolled in formation successfully."));
    }

    /**
     * Cancels an active enrollment reservation for the authenticated artisan.
     *
     * @param id formation unique identifier
     * @return 200 OK with updated cancelled enrollment response DTO
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<FormationEnrollmentResponseDTO>> cancel(
            @PathVariable String id
    ) {
        FormationEnrollmentResponseDTO cancelled = formationEnrollmentService.cancelEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success(cancelled, "Formation enrollment cancelled successfully."));
    }

    /**
     * Retrieves paginated enrollment history for the authenticated artisan.
     *
     * @param pageable pagination parameters
     * @return 200 OK containing paginated enrollment details
     */
    @GetMapping("/my-enrollments")
    public ResponseEntity<ApiResponse<PaginatedResponse<FormationEnrollmentDetailDTO>>> getMyEnrollments(
            @PageableDefault(size = 10, sort = "enrolledAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<FormationEnrollmentDetailDTO> page = formationEnrollmentService.getMyEnrollments(pageable);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.from(page)));
    }

    /**
     * Streams or downloads a protected course document attachment.
     * Access is restricted to confirmed enrolled participants and the authoring instructor.
     *
     * @param id formation unique identifier
     * @param fileId course file unique identifier
     * @return 200 OK with downloadable binary resource stream
     */
    @GetMapping("/{id}/files/{fileId}/download")
    public ResponseEntity<Resource> downloadCourseFile(
            @PathVariable String id,
            @PathVariable String fileId
    ) {
        StorageResource resource = formationEnrollmentService.downloadCourseFile(id, fileId);

        String filename = (resource.originalFilename() != null && !resource.originalFilename().isBlank())
                ? resource.originalFilename()
                : "course-file";

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        MediaType mediaType;
        if (resource.contentType() != null && !resource.contentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(resource.contentType());
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(mediaType)
                .contentLength(resource.size())
                .body(new InputStreamResource(resource.content()));
    }
}
