package com.project.souklab.controller.support;

import com.project.souklab.security.Permission;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;

/**
 * Reusable RequestPostProcessor helpers providing authenticated principals
 * matching Souklab's UserDetails convention.
 */
public final class SecurityTestUtils {

    private SecurityTestUtils() {
        /**
         * Private constructor for utility class.
         */
    }

    /**
     * Returns a RequestPostProcessor authenticating as an Artisan user.
     */
    public static RequestPostProcessor artisan(String email) {
        return SecurityMockMvcRequestPostProcessors.user(email)
                .authorities(authorities(Permission.ARTISAN_CONTENT, Permission.ARTISAN_FORMATIONS,
                        Permission.ARTISAN_REVIEWS, Permission.PROFILE_READ, Permission.PROFILE_WRITE,
                        Permission.REPORT_CREATE, Permission.FILE_READ));
    }

    /**
     * Returns a RequestPostProcessor authenticating as a default Artisan user.
     */
    public static RequestPostProcessor artisan() {
        return artisan("artisan@souklab.com");
    }

    /**
     * Returns a RequestPostProcessor authenticating as an Admin user.
     */
    public static RequestPostProcessor admin(String email) {
        return SecurityMockMvcRequestPostProcessors.user(email)
                .authorities(Arrays.stream(Permission.values())
                        .map(SecurityTestUtils::authority)
                        .toArray(SimpleGrantedAuthority[]::new));
    }

    /**
     * Returns a RequestPostProcessor authenticating as a default Admin user.
     */
    public static RequestPostProcessor admin() {
        return admin("admin@souklab.com");
    }

    /**
     * Returns a RequestPostProcessor authenticating as a Client user.
     */
    public static RequestPostProcessor client(String email) {
        return SecurityMockMvcRequestPostProcessors.user(email)
                .authorities(authorities(Permission.PROFILE_READ, Permission.PROFILE_WRITE,
                        Permission.REPORT_CREATE, Permission.FILE_READ));
    }

    /**
     * Returns a RequestPostProcessor authenticating as a default Client user.
     */
    public static RequestPostProcessor client() {
        return client("client@souklab.com");
    }

    private static SimpleGrantedAuthority[] authorities(Permission... permissions) {
        return Arrays.stream(permissions).map(SecurityTestUtils::authority)
                .toArray(SimpleGrantedAuthority[]::new);
    }

    private static SimpleGrantedAuthority authority(Permission permission) {
        return new SimpleGrantedAuthority(permission.authority());
    }
}
