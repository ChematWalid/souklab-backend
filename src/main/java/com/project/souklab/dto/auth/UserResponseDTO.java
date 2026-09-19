package com.project.souklab.dto.auth;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.security.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private String phone;
    private String avatarUrl;
    private AccountStatus status;
    private boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private Set<Permission> permissions;
    private boolean isPremium;
    private boolean isValidated;
    private boolean isTeacher;
    private LocalDateTime bannedUntil;
    private String banReason;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
