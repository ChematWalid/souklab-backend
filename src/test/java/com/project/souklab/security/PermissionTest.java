package com.project.souklab.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies stable permission identifiers.
 */
class PermissionTest {

    @Test
    void permissionAuthoritiesAreNamespaced() {
        assertThat(Permission.Admin.USERS.value()).isEqualTo("permission:admin:users");
        assertThat(Permission.Profile.READ.value()).isEqualTo("permission:profile:read");
    }
}
