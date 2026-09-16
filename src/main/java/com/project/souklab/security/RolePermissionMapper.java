package com.project.souklab.security;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps legacy persisted roles to the permissions granted by the application.
 */
public final class RolePermissionMapper {

    private static final Map<RoleName, Set<Permission>> ROLE_PERMISSIONS = Map.of(
            RoleName.ADMIN, EnumSet.allOf(Permission.class),
            RoleName.ARTISAN, EnumSet.of(Permission.ARTISAN_FORMATIONS, Permission.ARTISAN_CONTENT,
                    Permission.ARTISAN_REVIEWS, Permission.PROFILE_READ, Permission.PROFILE_WRITE,
                    Permission.REPORT_CREATE, Permission.FILE_READ),
            RoleName.CLIENT, EnumSet.of(Permission.PROFILE_READ, Permission.PROFILE_WRITE,
                    Permission.REPORT_CREATE, Permission.FILE_READ));

    private RolePermissionMapper() {
    }

    public static Set<String> authoritiesFor(String role) {
        String normalized = RoleName.normalize(role);
        Set<String> authorities = new java.util.HashSet<>();
        for (RoleName roleName : RoleName.values()) {
            if (roleName.authority().equals(normalized)) {
                authorities.add(roleName.authority());
                ROLE_PERMISSIONS.get(roleName).stream()
                        .map(Permission::authority)
                        .forEach(authorities::add);
            }
        }
        return authorities;
    }

    public static Set<Permission> permissionsFor(String role) {
        String normalized = RoleName.normalize(role);
        for (RoleName roleName : RoleName.values()) {
            if (roleName.authority().equals(normalized)) {
                return Set.copyOf(ROLE_PERMISSIONS.get(roleName));
            }
        }
        return Set.of();
    }
}
