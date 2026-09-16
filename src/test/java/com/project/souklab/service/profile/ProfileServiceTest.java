package com.project.souklab.service.profile;

import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import com.project.souklab.security.Permission;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies profile authorization is represented by permissions.
 */
class ProfileServiceTest {

    @Test
    void artisanCapabilityIsExplicit() {
        AuthorizationPermission permission = new AuthorizationPermission(Permission.ARTISAN_CONTENT.authority(), "Create artisan content", true);
        User user = User.builder().permissions(Set.of(permission)).build();

        assertThat(user.getPermissions()).extracting(AuthorizationPermission::getPermissionKey)
                .contains(Permission.ARTISAN_CONTENT.authority());
    }
}
