package com.project.souklab.controller.formation;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.formation.FormationResponseDTO;
import com.project.souklab.dto.formation.FormationReviewRequestDTO;
import com.project.souklab.dto.formation.FormationSummaryDTO;
import com.project.souklab.service.formation.AdminFormationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for administrative moderation, review decisions, and publication
 * of artisan masterclasses and formations.
 */
@RestController
@RequestMapping("/api/v1/admin/formations")
@PreAuthorize("@accessControl.canManageFormations(authentication)")
@RequiredArgsConstructor
public class AdminFormationController {

    private final AdminFormationService adminFormationService;

    /**
     * Retrieves the pending formations moderation queue.
     *
     * @param pageable pagination parameters
     * @return 200 OK with paginated list of formations awaiting review
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PaginatedResponse<FormationSummaryDTO>>> getPendingFormations(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PaginatedResponse<FormationSummaryDTO> response = adminFormationService.getPendingFormations(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Submits an administrative moderation decision (approval or rejection) for a formation.
     *
     * @param id formation unique identifier
     * @param dto review request payload
     * @return 200 OK with updated formation response DTO
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<FormationResponseDTO>> reviewFormation(
            @PathVariable String id,
            @Valid @RequestBody FormationReviewRequestDTO dto
    ) {
        FormationResponseDTO response = adminFormationService.reviewFormation(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Formation review recorded successfully."));
    }

    /**
     * Publishes an approved formation to the public catalog.
     *
     * @param id formation unique identifier
     * @return 200 OK with published formation response DTO
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<FormationResponseDTO>> publishFormation(
            @PathVariable String id
    ) {
        FormationResponseDTO response = adminFormationService.publishFormation(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Formation published successfully."));
    }
}
