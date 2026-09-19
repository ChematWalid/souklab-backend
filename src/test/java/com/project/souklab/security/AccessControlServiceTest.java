package com.project.souklab.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies centralized permission predicates and default denial.
 */
class AccessControlServiceTest {

    private final AccessControlService service = new AccessControlService();

    @Test
    void grantsOnlyExplicitPermission() {
        Authentication authentication = new TestingAuthenticationToken("user", "credentials",
                Permission.Admin.USERS);

        assertThat(service.isAdmin(authentication)).isTrue();
        assertThat(service.isArtisan(authentication)).isFalse();
    }

    @Test
    void deniesAnonymousAndMissingPermission() {
        assertThat(service.isAdmin(null)).isFalse();
        assertThat(service.isArtisan(new TestingAuthenticationToken("user", "credentials"))).isFalse();
    }

    @Test
    void keepsAdministrativeCapabilitiesIndependent() {
        Authentication feedModerator = new TestingAuthenticationToken("user", "credentials",
                Permission.Admin.FEED);

        assertThat(service.canModerateFeed(feedModerator)).isTrue();
        assertThat(service.canManageUsers(feedModerator)).isFalse();
        assertThat(service.canManageFormations(feedModerator)).isFalse();
        assertThat(service.canModerateReports(feedModerator)).isFalse();
    }
}
