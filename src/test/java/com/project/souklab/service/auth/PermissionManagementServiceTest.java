package com.project.souklab.service.auth;

import org.mockito.ArgumentMatchers;

import com.project.souklab.dao.AuthorizationPermissionRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.admin.PermissionAssignmentRequestDTO;
import com.project.souklab.exception.ConflictException;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import com.project.souklab.security.AccessControlService;
import com.project.souklab.security.Permission;
import com.project.souklab.service.audit.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionManagementServiceTest {

    @Mock UserRepository users;
    @Mock AuthorizationPermissionRepository permissions;
    @Mock AccessControlService access;
    @Mock AuditLogService audit;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listsOnlyEnabledPermissions() {
        User user = User.builder().email("user@example.test").permissions(new HashSet<>(Set.of(
                permission(Permission.Admin.USERS, true), permission(Permission.Admin.REPORTS, false)))).build();
        allowAdmin();
        when(users.findById("u1")).thenReturn(Optional.of(user));

        assertThat(service().list("u1")).containsExactly(Permission.Admin.USERS);
    }

    @Test
    void grantsPermissionAndAudits() {
        User user = User.builder().email("user@example.test").permissions(new HashSet<>()).build();
        AuthorizationPermission permission = permission(Permission.Artisan.CONTENT, true);
        allowAdmin();
        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(permissions.findByPermissionKeyAndEnabledTrue(Permission.Artisan.CONTENT.value())).thenReturn(Optional.of(permission));

        assertThat(service().grant("u1", new PermissionAssignmentRequestDTO(Permission.Artisan.CONTENT)))
                .containsExactly(Permission.Artisan.CONTENT);
        verify(users).save(user);
        verify(audit).logAction(any(), ArgumentMatchers.eq("u1:" + Permission.Artisan.CONTENT.value()));
    }

    @Test
    void rejectsDuplicateGrantAndMissingRevoke() {
        AuthorizationPermission permission = permission(Permission.Profile.READ, true);
        User user = User.builder().email("user@example.test").permissions(new HashSet<>(Set.of(permission))).build();
        allowAdmin();
        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(permissions.findByPermissionKeyAndEnabledTrue(Permission.Profile.READ.value())).thenReturn(Optional.of(permission));

        assertThatThrownBy(() -> service().grant("u1", new PermissionAssignmentRequestDTO(Permission.Profile.READ)))
                .isInstanceOf(ConflictException.class);
        user.getPermissions().clear();
        assertThatThrownBy(() -> service().revoke("u1", new PermissionAssignmentRequestDTO(Permission.Profile.READ)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void revokesAssignedPermissionAndAudits() {
        AuthorizationPermission permission = permission(Permission.Artisan.CONTENT, true);
        User user = User.builder().email("user@example.test")
                .permissions(new HashSet<>(Set.of(permission))).build();
        allowAdmin();
        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(permissions.findByPermissionKeyAndEnabledTrue(Permission.Artisan.CONTENT.value()))
                .thenReturn(Optional.of(permission));

        assertThat(service().revoke("u1", new PermissionAssignmentRequestDTO(Permission.Artisan.CONTENT)))
                .isEmpty();
        verify(users).save(user);
        verify(audit).logAction(ArgumentMatchers.any(),
                ArgumentMatchers.eq("u1:" + Permission.Artisan.CONTENT.value()));
    }

    @Test
    void rejectsUnauthorizedAndUnknownResources() {
        when(access.isAdmin(any())).thenReturn(false);
        assertThatThrownBy(() -> service().list("u1")).isInstanceOf(ForbiddenException.class);
        allowAdmin();
        when(users.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service().list("missing")).isInstanceOf(ResourceNotFoundException.class);
        when(users.findById("u1")).thenReturn(Optional.of(User.builder().email("u@example.test").build()));
        assertThatThrownBy(() -> service().grant("u1", new PermissionAssignmentRequestDTO(null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void allowAdmin() {
        when(access.isAdmin(any())).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", "credentials"));
    }

    private PermissionManagementService service() {
        return new PermissionManagementService(users, permissions, access, audit);
    }

    private AuthorizationPermission permission(Permission permission, boolean enabled) {
        return new AuthorizationPermission(permission.value(), permission.description(), enabled);
    }
}
