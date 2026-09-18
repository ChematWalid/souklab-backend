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

    public boolean canManageUsers(Authentication authentication) {
        return hasPermission(authentication, Permission.ADMIN_USERS);
    }

    public boolean canManageFormations(Authentication authentication) {
        return hasPermission(authentication, Permission.ADMIN_FORMATIONS);
    }

    public boolean canModerateFeed(Authentication authentication) {
        return hasPermission(authentication, Permission.ADMIN_FEED);
    }

    public boolean canModerateReports(Authentication authentication) {
        return hasPermission(authentication, Permission.ADMIN_REPORTS);
    }

    public boolean canManageFinancialOperations(Authentication authentication) {
        return hasPermission(authentication, Permission.FINANCIAL_ADMIN);
    }

    public boolean canManageArtisanFormations(Authentication authentication) {
        return hasPermission(authentication, Permission.ARTISAN_FORMATIONS);
    }

    public boolean canManageArtisanContent(Authentication authentication) {
        return hasPermission(authentication, Permission.ARTISAN_CONTENT);
    }

    public boolean canManageArtisanReviews(Authentication authentication) {
        return hasPermission(authentication, Permission.ARTISAN_REVIEWS);
    }

    public boolean isArtisan(Authentication authentication) {
        return hasPermission(authentication, Permission.ARTISAN_CONTENT);
    }

    public boolean canReadProfile(Authentication authentication) {
        return hasPermission(authentication, Permission.PROFILE_READ);
    }

    public boolean canWriteProfile(Authentication authentication) {
        return hasPermission(authentication, Permission.PROFILE_WRITE);
    }
}
