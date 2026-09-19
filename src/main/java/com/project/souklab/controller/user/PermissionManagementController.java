package com.project.souklab.controller.user;

import com.project.souklab.dto.admin.PermissionAssignmentRequestDTO;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.security.Permission;
import com.project.souklab.service.auth.PermissionManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Set;

/**
 * Administrator endpoints for direct user permission assignments.
 */
@RestController
@RequestMapping("/api/v1/admin/users/{userId}/permissions")
@PreAuthorize("@accessControl.canManageUsers(authentication)")
@RequiredArgsConstructor
public class PermissionManagementController {

    private final PermissionManagementService permissionManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<Set<Permission>>> list(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(permissionManagementService.list(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Set<Permission>>> grant(@PathVariable String userId,
                                                           @Valid @RequestBody PermissionAssignmentRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(permissionManagementService.grant(userId, request), "Permission granted."));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Set<Permission>>> revoke(@PathVariable String userId,
                                                            @Valid @RequestBody PermissionAssignmentRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(permissionManagementService.revoke(userId, request), "Permission revoked."));
    }
}
