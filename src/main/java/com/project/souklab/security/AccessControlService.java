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
                .anyMatch(permission::matches);
    }

    public boolean isAdmin(Authentication authentication) {
        return hasPermission(authentication, Permission.Admin.USERS);
    }

    public boolean canManageUsers(Authentication authentication) {
        return hasPermission(authentication, Permission.Admin.USERS);
    }

    public boolean canManageFormations(Authentication authentication) {
        return hasPermission(authentication, Permission.Admin.FORMATIONS);
    }

    public boolean canModerateFeed(Authentication authentication) {
        return hasPermission(authentication, Permission.Admin.FEED);
    }

    public boolean canModerateReports(Authentication authentication) {
        return hasPermission(authentication, Permission.Admin.REPORTS);
    }

    public boolean canManageFinancialOperations(Authentication authentication) {
        return hasPermission(authentication, Permission.Financial.ADMIN);
    }

    public boolean canViewAnalytics(Authentication authentication) {
        return hasPermission(authentication, Permission.Analytics.ADMIN);
    }

    public boolean canManageArtisanFormations(Authentication authentication) {
        return hasPermission(authentication, Permission.Artisan.FORMATIONS);
    }

    public boolean canManageArtisanContent(Authentication authentication) {
        return hasPermission(authentication, Permission.Artisan.CONTENT);
    }

    public boolean canManageArtisanReviews(Authentication authentication) {
        return hasPermission(authentication, Permission.Artisan.REVIEWS);
    }

    public boolean isArtisan(Authentication authentication) {
        return hasPermission(authentication, Permission.Artisan.CONTENT);
    }

    public boolean canReadProfile(Authentication authentication) {
        return hasPermission(authentication, Permission.Profile.READ);
    }

    public boolean canWriteProfile(Authentication authentication) {
        return hasPermission(authentication, Permission.Profile.WRITE);
    }

    public boolean canManageCatalog(Authentication authentication) {
        return hasPermission(authentication, Permission.Admin.CATALOG);
    }

    /**
     * Determines whether the authenticated principal has permission to manage client favorites.
     *
     * @param authentication the current authentication token
     * @return {@code true} if the principal holds {@link Permission.Client#FAVORITES}
     */
    public boolean canManageFavorites(Authentication authentication) {
        return hasPermission(authentication, Permission.Client.FAVORITES);
    }
}
