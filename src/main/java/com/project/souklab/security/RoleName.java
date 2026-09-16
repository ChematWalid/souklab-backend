package com.project.souklab.security;

/**
 * Persisted role names retained as a compatibility boundary for existing data and clients.
 */
public enum RoleName {
    ADMIN,
    ARTISAN,
    CLIENT;

    public String authority() {
        return "ROLE_" + name();
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}
