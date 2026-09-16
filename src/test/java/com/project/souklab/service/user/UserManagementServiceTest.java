package com.project.souklab.service.user;

import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import com.project.souklab.security.Permission;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies user management data is permission-oriented.
 */
class UserManagementServiceTest {

    @Test
    void userStoresDirectPermissionAssignments() {
        AuthorizationPermission permission = new AuthorizationPermission(Permission.PROFILE_WRITE.authority(), "Update own profile", true);
        User user = User.builder().permissions(Set.of(permission)).build();

        assertThat(user.getPermissions()).hasSize(1);
        assertThat(user.getPermissions().iterator().next().getPermissionKey()).isEqualTo(Permission.PROFILE_WRITE.authority());
    }
}
