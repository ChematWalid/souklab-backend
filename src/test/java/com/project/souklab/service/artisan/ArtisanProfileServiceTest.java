package com.project.souklab.service.artisan;

import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import com.project.souklab.security.Permission;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies direct permission-based administrator detection inputs.
 */
class ArtisanProfileServiceTest {

    @Test
    void administratorPermissionIsDistinctFromArtisanPermission() {
        AuthorizationPermission admin = new AuthorizationPermission(Permission.ADMIN_USERS.authority(), "Manage users", true);
        User user = User.builder().permissions(Set.of(admin)).build();

        assertThat(user.getPermissions()).extracting(AuthorizationPermission::getPermissionKey)
                .containsExactly(Permission.ADMIN_USERS.authority());
    }
}
