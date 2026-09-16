package com.project.souklab.util;

import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.security.Permission;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.Artisan;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Shared security helper for artisan-scoped service operations.
 * Centralises the pattern of asserting {@code ROLE_ARTISAN} authority,
 * resolving the username from the security context, and loading the
 * {@link Artisan} profile — eliminating copy-paste across multiple services.
 */
public final class ArtisanSecurityUtils {


    private ArtisanSecurityUtils() {
        // Utility class
    }

    /**
     * Resolves the currently authenticated artisan from the Spring Security context
     * using the supplied {@link ArtisanRepository}.
     * <ol>
     *   <li>Asserts a non-anonymous, authenticated principal is present.</li>
     *   <li>Verifies the principal holds {@code ROLE_ARTISAN}.</li>
     *   <li>Loads and returns the matching {@link Artisan} entity by email or ID.</li>
     * </ol>
     *
     * @param artisanRepository repository used to retrieve the artisan entity
     * @return the resolved {@link Artisan} entity for the current request
     * @throws UnauthorizedException if the security context contains no authenticated principal
     * @throws ForbiddenException    if the principal lacks {@code ROLE_ARTISAN} or no artisan profile exists
     */
    public static Artisan resolveAuthenticatedArtisan(ArtisanRepository artisanRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new UnauthorizedException("User is not authenticated");
        }

        boolean hasArtisanRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> Permission.ARTISAN_CONTENT.authority().equals(authority.getAuthority()));
        if (!hasArtisanRole) {
            throw new ForbiddenException("Access denied: artisan role required.");
        }

        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return artisanRepository.findByUserEmailIgnoreCase(username)
                .or(() -> artisanRepository.findById(username))
                .orElseThrow(() -> new ForbiddenException("Only registered artisans can access this resource."));
    }
}
