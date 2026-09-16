package com.project.souklab.controller.support;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

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
                .authorities(new SimpleGrantedAuthority("permission:artisan:content"), new SimpleGrantedAuthority("permission:artisan:formations"), new SimpleGrantedAuthority("permission:artisan:reviews"));
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
                .authorities(new SimpleGrantedAuthority("permission:admin:users"), new SimpleGrantedAuthority("permission:admin:formations"), new SimpleGrantedAuthority("permission:admin:feed"), new SimpleGrantedAuthority("permission:admin:reports"));
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
                .authorities(new SimpleGrantedAuthority("permission:profile:read"), new SimpleGrantedAuthority("permission:profile:write"));
    }

    /**
     * Returns a RequestPostProcessor authenticating as a default Client user.
     */
    public static RequestPostProcessor client() {
        return client("client@souklab.com");
    }
}
