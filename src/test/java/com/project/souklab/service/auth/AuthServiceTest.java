package com.project.souklab.service.auth;

import com.project.souklab.dao.AuthorizationPermissionRepository;
import com.project.souklab.dao.OAuthIdentityRepository;
import com.project.souklab.dao.RefreshTokenRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies authorization-aware authentication contracts.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Test
    void permissionDefinitionUsesStableNames() {
        AuthorizationPermission permission = new AuthorizationPermission("permission:profile:read", "Read profiles", true);

        assertThat(permission.getPermissionKey()).isEqualTo("permission:profile:read");
        assertThat(permission.isEnabled()).isTrue();
    }
}
