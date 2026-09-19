package com.project.souklab.dto.profile;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.ClientType;
import com.project.souklab.security.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Profile response DTO scoped to CLIENT users.
 * Deliberately excludes any artisan-specific fields (teacher, premium-as-artisan-premium, etc.)
 * so that client users never see those in their own profile responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProfileResponseDTO implements ProfileResponse {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private String phone;
    /** Placeholder — wired to real upload path in Phase D. */
    private String avatarUrl;
    private AccountStatus accountStatus;
    private Set<Permission> permissions;
    private boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ClientType clientType;
    private String companyName;
    private String bio;
    private String address;
    private String regionId;
    private String city;
}
