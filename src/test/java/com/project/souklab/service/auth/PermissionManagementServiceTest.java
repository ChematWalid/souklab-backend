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
                permission("permission:enabled", true), permission("permission:disabled", false)))).build();
        allowAdmin();
        when(users.findById("u1")).thenReturn(Optional.of(user));

        assertThat(service().list("u1")).containsExactly("permission:enabled");
    }

    @Test
    void grantsPermissionAndAudits() {
        User user = User.builder().email("user@example.test").permissions(new HashSet<>()).build();
        AuthorizationPermission permission = permission("permission:chat:send", true);
        allowAdmin();
        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(permissions.findByPermissionKeyAndEnabledTrue("permission:chat:send")).thenReturn(Optional.of(permission));

        assertThat(service().grant("u1", new PermissionAssignmentRequestDTO(" permission:chat:send ")))
                .containsExactly("permission:chat:send");
        verify(users).save(user);
        verify(audit).logAction(any(), ArgumentMatchers.eq("u1:permission:chat:send"));
    }

    @Test
    void rejectsDuplicateGrantAndMissingRevoke() {
        AuthorizationPermission permission = permission("permission:key", true);
        User user = User.builder().email("user@example.test").permissions(new HashSet<>(Set.of(permission))).build();
        allowAdmin();
        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(permissions.findByPermissionKeyAndEnabledTrue("permission:key")).thenReturn(Optional.of(permission));

        assertThatThrownBy(() -> service().grant("u1", new PermissionAssignmentRequestDTO("permission:key")))
                .isInstanceOf(ConflictException.class);
        user.getPermissions().clear();
        assertThatThrownBy(() -> service().revoke("u1", new PermissionAssignmentRequestDTO("permission:key")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void revokesAssignedPermissionAndAudits() {
        AuthorizationPermission permission = permission("permission:chat:send", true);
        User user = User.builder().email("user@example.test")
                .permissions(new HashSet<>(Set.of(permission))).build();
        allowAdmin();
        when(users.findById("u1")).thenReturn(Optional.of(user));
        when(permissions.findByPermissionKeyAndEnabledTrue("permission:chat:send"))
                .thenReturn(Optional.of(permission));

        assertThat(service().revoke("u1", new PermissionAssignmentRequestDTO("permission:chat:send")))
                .isEmpty();
        verify(users).save(user);
        verify(audit).logAction(ArgumentMatchers.any(),
                ArgumentMatchers.eq("u1:permission:chat:send"));
    }

    @Test
    void rejectsUnauthorizedAndUnknownResources() {
        when(access.isAdmin(any())).thenReturn(false);
        assertThatThrownBy(() -> service().list("u1")).isInstanceOf(ForbiddenException.class);
        allowAdmin();
        when(users.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service().list("missing")).isInstanceOf(ResourceNotFoundException.class);
        when(users.findById("u1")).thenReturn(Optional.of(User.builder().email("u@example.test").build()));
        when(permissions.findByPermissionKeyAndEnabledTrue("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service().grant("u1", new PermissionAssignmentRequestDTO("missing")))
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

    private AuthorizationPermission permission(String key, boolean enabled) {
        return new AuthorizationPermission(key, key, enabled);
    }
}
