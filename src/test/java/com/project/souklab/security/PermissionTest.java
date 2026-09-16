package com.project.souklab.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies stable permission identifiers.
 */
class PermissionTest {

    @Test
    void permissionAuthoritiesAreNamespaced() {
        assertThat(Permission.ADMIN_USERS.authority()).isEqualTo("permission:admin:users");
        assertThat(Permission.PROFILE_READ.authority()).isEqualTo("permission:profile:read");
    }
}
