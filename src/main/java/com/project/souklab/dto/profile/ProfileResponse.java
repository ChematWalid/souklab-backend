package com.project.souklab.dto.profile;

import com.project.souklab.model.AccountStatus;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Common response contract for user profiles (Client and Artisan).
 * Provides compile-time type safety across Auth and Profile endpoints while
 * allowing polymorphic responses without leaking account-type-specific fields.
 */
public interface ProfileResponse {

    String getId();

    String getEmail();

    String getFirstName();

    String getLastName();

    String getName();

    String getPhone();

    String getAvatarUrl();

    AccountStatus getAccountStatus();

    Set<String> getPermissions();

    boolean isEmailVerified();

    LocalDateTime getEmailVerifiedAt();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
