package com.project.souklab.security;

import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.User;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Shared component that determines whether contact information should be locked
 * for the current request's viewer.
 *
 * <p>Locking rules:
 * <ul>
 *   <li>Admins — never locked.</li>
 *   <li>Profile owner (isSelf) — never locked.</li>
 *   <li>Premium clients or premium artisans — unlocked.</li>
 *   <li>Non-premium authenticated users — locked.</li>
 *   <li>Anonymous callers — locked (directory endpoint blocks them upstream via
 *       {@code @PreAuthorize("isAuthenticated()")}; this covers any edge case).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class ViewerPremiumResolver {

    private final UserRepository userRepository;

    /**
     * Resolves contact-info lock state for the viewer inferred from the current
     * {@link org.springframework.security.core.context.SecurityContext}.
     *
     * <p>Intended for the directory search path, where the viewer entity is not
     * pre-loaded and there is no self-view concept.
     *
     * @return {@code true} if contact fields must be masked; {@code false} if the
     *         viewer has premium access
     */
    @Transactional(readOnly = true)
    public boolean isContactInfoLocked() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) {
            return true;
        }

        User viewer = userRepository.findByEmail(email.toLowerCase(Locale.ROOT))
                .orElse(null);
        if (viewer == null) {
            return true;
        }

        boolean isAdmin = viewer.getPermissions().stream()
                .anyMatch(p -> Permission.Admin.USERS.matches(p.getPermissionKey()));

        return isContactInfoLocked(viewer, false, isAdmin);
    }

    /**
     * Resolves contact-info lock state for an already-loaded viewer, with explicit
     * self-view and admin flags.
     *
     * <p>Intended for the artisan profile path, where the viewer entity and context
     * flags are computed before calling this method.
     *
     * @param viewer  the authenticated {@link User} performing the request
     * @param isSelf  {@code true} if the viewer is viewing their own profile
     * @param isAdmin {@code true} if the viewer holds administrator permissions
     * @return {@code true} if contact fields must be masked; {@code false} otherwise
     */
    public boolean isContactInfoLocked(User viewer, boolean isSelf, boolean isAdmin) {
        if (isSelf || isAdmin) {
            return false;
        }
        if (viewer.getClient() != null) {
            return !viewer.getClient().isPremium();
        }
        if (viewer.getArtisan() != null) {
            return !viewer.getArtisan().isPremium();
        }
        return true;
    }
}
