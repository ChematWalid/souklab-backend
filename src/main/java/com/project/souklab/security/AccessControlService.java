package com.project.souklab.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Centralized authorization predicates for Spring method security and domain services.
 */
@Component("accessControl")
public class AccessControlService {

    public boolean hasPermission(Authentication authentication, Permission permission) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> permission.authority().equals(authority.getAuthority()));
    }

    public boolean isAdmin(Authentication authentication) {
        return hasPermission(authentication, Permission.ADMIN_USERS);
    }

    public boolean isArtisan(Authentication authentication) {
        return hasPermission(authentication, Permission.ARTISAN_CONTENT);
    }
}
