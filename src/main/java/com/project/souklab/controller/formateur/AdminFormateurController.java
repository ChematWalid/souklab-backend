package com.project.souklab.controller.formateur;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.formateur.FormateurApproveDTO;
import com.project.souklab.dto.formateur.FormateurCooldownOverrideDTO;
import com.project.souklab.dto.formateur.FormateurGrantDTO;
import com.project.souklab.dto.formateur.FormateurRejectDTO;
import com.project.souklab.dto.formateur.FormateurRequestResponseDTO;
import com.project.souklab.dto.formateur.FormateurRevokeDTO;
import com.project.souklab.service.formateur.ArtisanFormateurService;
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

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("@accessControl.canManageUsers(authentication)")
public class AdminFormateurController {

    private final ArtisanFormateurService artisanFormateurService;

    @GetMapping("/formateur-requests")
    public ResponseEntity<ApiResponse<PaginatedResponse<FormateurRequestResponseDTO>>> getPendingRequests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(artisanFormateurService.getPendingRequests(pageable)));
    }

    @PostMapping("/formateur-requests/{id}/approve")
    public ResponseEntity<ApiResponse<FormateurRequestResponseDTO>> approveRequest(
            @PathVariable String id,
            @Valid @RequestBody FormateurApproveDTO dto) {
        FormateurRequestResponseDTO response = artisanFormateurService.approveRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Formateur request approved successfully."));
    }

    @PostMapping("/formateur-requests/{id}/reject")
    public ResponseEntity<ApiResponse<FormateurRequestResponseDTO>> rejectRequest(
            @PathVariable String id,
            @Valid @RequestBody FormateurRejectDTO dto) {
        FormateurRequestResponseDTO response = artisanFormateurService.rejectRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Formateur request rejected successfully."));
    }

    @PostMapping("/artisans/{artisanId}/formateur-grant")
    public ResponseEntity<ApiResponse<FormateurRequestResponseDTO>> grantDirectly(
            @PathVariable String artisanId,
            @Valid @RequestBody FormateurGrantDTO dto) {
        FormateurRequestResponseDTO response = artisanFormateurService.grantDirectly(artisanId, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Formateur status granted successfully."));
    }

    @PostMapping("/artisans/{artisanId}/formateur-revoke")
    public ResponseEntity<ApiResponse<Void>> revokeDirectly(
            @PathVariable String artisanId,
            @Valid @RequestBody FormateurRevokeDTO dto) {
        artisanFormateurService.revokeDirectly(artisanId, dto);
        return ResponseEntity.ok(ApiResponse.success(null, "Formateur status revoked successfully."));
    }

    @PostMapping("/formateur-requests/{artisanId}/lift-cooldown")
    public ResponseEntity<ApiResponse<FormateurRequestResponseDTO>> liftCooldown(
            @PathVariable String artisanId,
            @RequestBody(required = false) FormateurCooldownOverrideDTO dto) {
        FormateurRequestResponseDTO response = artisanFormateurService.liftCooldown(artisanId, dto != null ? dto : new FormateurCooldownOverrideDTO());
        return ResponseEntity.ok(ApiResponse.success(response, "Cooldown configuration updated successfully."));
    }
}
