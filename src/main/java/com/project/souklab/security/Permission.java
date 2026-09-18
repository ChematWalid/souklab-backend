package com.project.souklab.security;

/**
 * Type-safe application permissions used by method and domain authorization.
 */
public enum Permission {
    ADMIN_USERS,
    ADMIN_FORMATIONS,
    ADMIN_FEED,
    ADMIN_REPORTS,
    ARTISAN_FORMATIONS,
    ARTISAN_CONTENT,
    ARTISAN_REVIEWS,
    PROFILE_READ,
    PROFILE_WRITE,
    REPORT_CREATE,
    FILE_READ,
    MESSAGE_SEND;

    public String authority() {
        return "permission:" + name().toLowerCase().replace('_', ':');
    }
}
