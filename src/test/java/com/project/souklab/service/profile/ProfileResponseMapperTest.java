package com.project.souklab.service.profile;

import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies permission exposure in profile responses.
 */
class ProfileResponseMapperTest {

    @Test
    void exposesEffectivePermissions() {
        AuthorizationPermission permission = new AuthorizationPermission("permission:profile:read", "Read profiles", true);
        User user = User.builder().email("user@example.com").permissions(Set.of(permission)).build();

        var response = new ProfileResponseMapper().mapToProfileResponse(user);

        assertThat(response.getPermissions()).containsExactly("permission:profile:read");
    }
}
