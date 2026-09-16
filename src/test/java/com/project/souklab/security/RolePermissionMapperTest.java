package com.project.souklab.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies compatibility role normalization and permission bundles.
 */
class RolePermissionMapperTest {

    @Test
    void artisanReceivesLegacyAndPermissionAuthorities() {
        assertThat(RolePermissionMapper.authoritiesFor("ARTISAN"))
                .contains(RoleName.ARTISAN.authority(), Permission.ARTISAN_CONTENT.authority())
                .doesNotContain(Permission.ADMIN_USERS.authority());
    }

    @Test
    void unknownRoleReceivesNoAuthorities() {
        assertThat(RolePermissionMapper.authoritiesFor("ROLE_UNKNOWN")).isEmpty();
        assertThat(RolePermissionMapper.permissionsFor("ROLE_UNKNOWN")).isEmpty();
    }
}
