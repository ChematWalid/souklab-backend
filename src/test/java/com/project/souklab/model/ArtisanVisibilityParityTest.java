package com.project.souklab.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parity test ensuring {@link Artisan#isEffectivelyVisible(LocalDateTime)} and
 * relational list-filtering predicate rules agree across all visibility states.
 */
class ArtisanVisibilityParityTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 9, 23, 12, 0, 0);

    /**
     * Replicates the exact relational JPQL WHERE clause used by the favorites list query:
     * artisan.deletedAt IS NULL AND artisan.user.deletedAt IS NULL AND
     * (user.status != 'SUSPENDED' OR (user.bannedUntil IS NOT NULL AND user.bannedUntil <= :now))
     */
    private boolean evaluateRelationalPredicate(Artisan artisan, LocalDateTime referenceTime) {
        if (artisan.getDeletedAt() != null) {
            return false;
        }
        User user = artisan.getUser();
        if (user == null || user.getDeletedAt() != null) {
            return false;
        }
        if (user.getStatus() != AccountStatus.SUSPENDED) {
            return true;
        }
        return user.getBannedUntil() != null && !user.getBannedUntil().isAfter(referenceTime);
    }

    private void assertParity(Artisan artisan, boolean expectedVisibility) {
        boolean entityResult = artisan.isEffectivelyVisible(now);
        boolean predicateResult = evaluateRelationalPredicate(artisan, now);

        assertThat(entityResult)
                .as("Entity isEffectivelyVisible must match expected")
                .isEqualTo(expectedVisibility);
        assertThat(predicateResult)
                .as("Relational JPQL predicate must match expected")
                .isEqualTo(expectedVisibility);
        assertThat(entityResult)
                .as("Entity method and relational predicate must be in full parity")
                .isEqualTo(predicateResult);
    }

    @Test
    @DisplayName("Active artisan with active user is effectively visible")
    void fullyVisibleArtisanReturnsTrue() {
        User user = User.builder()
                .status(AccountStatus.ACTIVE)
                .build();
        Artisan artisan = Artisan.builder()
                .user(user)
                .build();

        assertParity(artisan, true);
    }

    @Test
    @DisplayName("Soft-deleted artisan profile is not visible even if user is active")
    void softDeletedArtisanReturnsFalse() {
        User user = User.builder()
                .status(AccountStatus.ACTIVE)
                .build();
        Artisan artisan = Artisan.builder()
                .user(user)
                .deletedAt(now.minusDays(1))
                .build();

        assertParity(artisan, false);
    }

    @Test
    @DisplayName("Artisan with soft-deleted user account is not visible")
    void softDeletedUserReturnsFalse() {
        User user = User.builder()
                .status(AccountStatus.ACTIVE)
                .build();
        user.setDeletedAt(now.minusDays(1));

        Artisan artisan = Artisan.builder()
                .user(user)
                .build();

        assertParity(artisan, false);
    }

    @Test
    @DisplayName("Suspended user without ban expiration is indefinitely hidden")
    void suspendedUserWithoutBannedUntilReturnsFalse() {
        User user = User.builder()
                .status(AccountStatus.SUSPENDED)
                .bannedUntil(null)
                .build();
        Artisan artisan = Artisan.builder()
                .user(user)
                .build();

        assertParity(artisan, false);
    }

    @Test
    @DisplayName("Suspended user with future ban expiration is currently hidden")
    void suspendedUserWithFutureBannedUntilReturnsFalse() {
        User user = User.builder()
                .status(AccountStatus.SUSPENDED)
                .bannedUntil(now.plusHours(2))
                .build();
        Artisan artisan = Artisan.builder()
                .user(user)
                .build();

        assertParity(artisan, false);
    }

    @Test
    @DisplayName("Suspended user with elapsed ban expiration is effectively visible")
    void suspendedUserWithPastBannedUntilReturnsTrue() {
        User user = User.builder()
                .status(AccountStatus.SUSPENDED)
                .bannedUntil(now.minusMinutes(5))
                .build();
        Artisan artisan = Artisan.builder()
                .user(user)
                .build();

        assertParity(artisan, true);
    }

    @Test
    @DisplayName("Suspended user with ban expiration matching current time is visible")
    void suspendedUserWithExactNowBannedUntilReturnsTrue() {
        User user = User.builder()
                .status(AccountStatus.SUSPENDED)
                .bannedUntil(now)
                .build();
        Artisan artisan = Artisan.builder()
                .user(user)
                .build();

        assertParity(artisan, true);
    }

    @Test
    @DisplayName("Artisan without associated user is not visible")
    void nullUserReturnsFalse() {
        Artisan artisan = Artisan.builder()
                .user(null)
                .build();

        assertParity(artisan, false);
    }
}
