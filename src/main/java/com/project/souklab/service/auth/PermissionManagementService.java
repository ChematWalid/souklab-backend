package com.project.souklab.service.auth;

import com.project.souklab.dao.AuthorizationPermissionRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.admin.PermissionAssignmentRequestDTO;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.model.User;
import com.project.souklab.security.AccessControlService;
import com.project.souklab.security.Permission;
import com.project.souklab.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Administrates direct user permission assignments.
 */
@Service
@RequiredArgsConstructor
public class PermissionManagementService {

    private final UserRepository userRepository;
    private final AuthorizationPermissionRepository permissionRepository;
    private final AccessControlService accessControlService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Set<String> list(String userId) {
        requireAdmin();
        return listWithoutAuthorization(findUser(userId));
    }

    @Transactional
    public Set<String> grant(String userId, PermissionAssignmentRequestDTO request) {
        requireAdmin();
        User user = findUser(userId);
        AuthorizationPermission permission = findPermission(request.getPermissionKey());
        if (!user.getPermissions().add(permission)) {
            throw new ConflictException("Permission is already assigned.");
        }
        userRepository.save(user);
        auditLogService.logAction(AuditLogAction.PERMISSION_GRANTED, userId + ":" + permission.getPermissionKey());
        return listWithoutAuthorization(user);
    }

    @Transactional
    public Set<String> revoke(String userId, PermissionAssignmentRequestDTO request) {
        requireAdmin();
        User user = findUser(userId);
        AuthorizationPermission permission = findPermission(request.getPermissionKey());
        if (!user.getPermissions().remove(permission)) {
            throw new ConflictException("Permission is not assigned.");
        }
        userRepository.save(user);
        auditLogService.logAction(AuditLogAction.PERMISSION_REVOKED, userId + ":" + permission.getPermissionKey());
        return listWithoutAuthorization(user);
    }

    private Set<String> listWithoutAuthorization(User user) {
        return user.getPermissions().stream().filter(AuthorizationPermission::isEnabled)
                .map(AuthorizationPermission::getPermissionKey)
                .map(Permission::fromValue)
                .flatMap(Optional::stream)
                .map(Permission::value)
                .collect(Collectors.toUnmodifiableSet());
    }

    private User findUser(String id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private AuthorizationPermission findPermission(String key) {
        Permission permission = Permission.fromValue(key.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found."));
        return permissionRepository.findByPermissionKeyAndEnabledTrue(permission.value())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found."));
    }

    private void requireAdmin() {
        if (!accessControlService.isAdmin(SecurityContextHolder.getContext().getAuthentication())) {
            throw new ForbiddenException("Administrator access is required.");
        }
    }
}
