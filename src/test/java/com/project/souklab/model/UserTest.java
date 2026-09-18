package com.project.souklab.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for domain methods on {@link User}, specifically suspension active checks
 * and effective account status calculations.
 */
class UserTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 9, 11, 12, 0, 0);

    @Test
    void getNameUsesAvailableIdentityFields() {
        User both = User.builder().firstName("  First ").lastName(" Last ").email("both@test").build();
        User first = User.builder().firstName(" First ").email("first@test").build();
        User last = User.builder().lastName(" Last ").email("last@test").build();
        User neither = User.builder().email("fallback@test").build();
        assertThat(both.getName()).isEqualTo("First Last");
        assertThat(first.getName()).isEqualTo("First");
        assertThat(last.getName()).isEqualTo("Last");
        assertThat(neither.getName()).isEqualTo("fallback@test");
    }

    @Nested
    @DisplayName("isSuspensionActive")
    class IsSuspensionActiveTests {

        /**
         * Verifies that an active timeout (bannedUntil in the future) is considered actively suspended.
         */
        @Test
        @DisplayName("isSuspensionActive: returns true when status is SUSPENDED and bannedUntil is in the future")
        void isSuspensionActive_whenTimeoutInFuture_returnsTrue() {
            User user = User.builder()
                    .status(AccountStatus.SUSPENDED)
                    .bannedUntil(now.plusHours(2))
                    .build();

            assertThat(user.isSuspensionActive(now)).isTrue();
        }

        /**
         * Verifies that an expired timeout (bannedUntil in the past) is not considered actively suspended.
         */
        @Test
        @DisplayName("isSuspensionActive: returns false when status is SUSPENDED and bannedUntil is in the past")
        void isSuspensionActive_whenTimeoutInPast_returnsFalse() {
            User user = User.builder()
                    .status(AccountStatus.SUSPENDED)
                    .bannedUntil(now.minusSeconds(1))
                    .build();

            assertThat(user.isSuspensionActive(now)).isFalse();
        }

        /**
         * Verifies that when bannedUntil is exactly equal to the reference time, the suspension is considered expired.
         */
        @Test
        @DisplayName("isSuspensionActive: returns false when bannedUntil is exactly equal to current time")
        void isSuspensionActive_whenTimeoutEqualToNow_returnsFalse() {
            User user = User.builder()
                    .status(AccountStatus.SUSPENDED)
                    .bannedUntil(now)
                    .build();

            assertThat(user.isSuspensionActive(now)).isFalse();
        }

        /**
         * Verifies that a permanent ban (bannedUntil is null) is considered actively suspended.
         */
        @Test
        @DisplayName("isSuspensionActive: returns true when status is SUSPENDED and bannedUntil is null")
        void isSuspensionActive_whenPermanentBan_returnsTrue() {
            User user = User.builder()
                    .status(AccountStatus.SUSPENDED)
                    .bannedUntil(null)
                    .build();

            assertThat(user.isSuspensionActive(now)).isTrue();
        }

        /**
         * Verifies that an ACTIVE user is not considered suspended even if bannedUntil was somehow populated.
         */
        @Test
        @DisplayName("isSuspensionActive: returns false when status is ACTIVE")
        void isSuspensionActive_whenStatusIsActive_returnsFalse() {
            User user = User.builder()
                    .status(AccountStatus.ACTIVE)
                    .bannedUntil(null)
                    .build();

            assertThat(user.isSuspensionActive(now)).isFalse();
        }

        /**
         * Verifies that a PENDING user is not considered suspended.
         */
        @Test
        @DisplayName("isSuspensionActive: returns false when status is PENDING")
        void isSuspensionActive_whenStatusIsPending_returnsFalse() {
            User user = User.builder()
                    .status(AccountStatus.PENDING)
                    .build();

            assertThat(user.isSuspensionActive(now)).isFalse();
        }
    }

    @Nested
    @DisplayName("getEffectiveStatus")
    class GetEffectiveStatusTests {

        /**
         * Verifies that an active timeout preserves SUSPENDED as the effective status.
         */
        @Test
        @DisplayName("getEffectiveStatus: returns SUSPENDED when timeout is still in the future")
        void getEffectiveStatus_whenActiveTimeout_returnsSuspended() {
            User user = User.builder()
                    .status(AccountStatus.SUSPENDED)
                    .bannedUntil(now.plusDays(1))
                    .build();

            assertThat(user.getEffectiveStatus(now)).isEqualTo(AccountStatus.SUSPENDED);
        }

        /**
         * Verifies that an expired timeout evaluates to ACTIVE as the effective status.
         */
        @Test
        @DisplayName("getEffectiveStatus: returns ACTIVE when timeout has expired")
        void getEffectiveStatus_whenExpiredTimeout_returnsActive() {
            User user = User.builder()
                    .status(AccountStatus.SUSPENDED)
                    .bannedUntil(now.minusMinutes(5))
                    .build();

            assertThat(user.getEffectiveStatus(now)).isEqualTo(AccountStatus.ACTIVE);
        }

        /**
         * Verifies that a permanent ban preserves SUSPENDED as the effective status.
         */
        @Test
        @DisplayName("getEffectiveStatus: returns SUSPENDED for permanent ban with null bannedUntil")
        void getEffectiveStatus_whenPermanentBan_returnsSuspended() {
            User user = User.builder()
                    .status(AccountStatus.SUSPENDED)
                    .bannedUntil(null)
                    .build();

            assertThat(user.getEffectiveStatus(now)).isEqualTo(AccountStatus.SUSPENDED);
        }

        /**
         * Verifies that non-suspended statuses return their persisted status unchanged.
         */
        @Test
        @DisplayName("getEffectiveStatus: returns persisted status for ACTIVE, PENDING, and REJECTED accounts")
        void getEffectiveStatus_whenNotSuspended_returnsPersistedStatus() {
            User activeUser = User.builder().status(AccountStatus.ACTIVE).build();
            User pendingUser = User.builder().status(AccountStatus.PENDING).build();
            User rejectedUser = User.builder().status(AccountStatus.REJECTED).build();

            assertThat(activeUser.getEffectiveStatus(now)).isEqualTo(AccountStatus.ACTIVE);
            assertThat(pendingUser.getEffectiveStatus(now)).isEqualTo(AccountStatus.PENDING);
            assertThat(rejectedUser.getEffectiveStatus(now)).isEqualTo(AccountStatus.REJECTED);
        }
    }
}
