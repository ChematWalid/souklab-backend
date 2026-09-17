package com.project.souklab.service.auth;

import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CustomUserDetailsService}, verifying account locked and disabled flags
 * based on suspension status and registration decisions.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-11T12:00:00Z");
    private static final ZoneId ZONE = ZoneOffset.UTC;

    @Mock
    private UserRepository userRepository;

    private Clock clock;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(FIXED_INSTANT, ZONE);
        userDetailsService = new CustomUserDetailsService(userRepository, clock);
    }

    /**
     * Verifies that loadUserByUsername returns locked UserDetails when user has an active future timeout.
     */
    @Test
    @DisplayName("loadUserByUsername: sets accountLocked=true when timeout is in the future")
    void loadUserByUsername_whenActiveTimeout_setsAccountLockedTrue() {
        LocalDateTime now = LocalDateTime.ofInstant(FIXED_INSTANT, ZONE);
        User user = buildUser("locked@example.com", AccountStatus.SUSPENDED, now.plusHours(1));
        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("locked@example.com");

        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isEnabled()).isFalse();
    }

    /**
     * Verifies that loadUserByUsername returns unlocked UserDetails when timeout has lapsed.
     */
    @Test
    @DisplayName("loadUserByUsername: sets accountLocked=false when timeout is in the past")
    void loadUserByUsername_whenExpiredTimeout_setsAccountLockedFalse() {
        LocalDateTime now = LocalDateTime.ofInstant(FIXED_INSTANT, ZONE);
        User user = buildUser("expired@example.com", AccountStatus.SUSPENDED, now.minusMinutes(10));
        when(userRepository.findByEmail("expired@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("expired@example.com");

        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isEnabled()).isFalse();
    }

    /**
     * Verifies that loadUserByUsername returns locked UserDetails when user is permanently banned (bannedUntil is null).
     */
    @Test
    @DisplayName("loadUserByUsername: sets accountLocked=true for permanent ban with null bannedUntil")
    void loadUserByUsername_whenPermanentBan_setsAccountLockedTrue() {
        User user = buildUser("permban@example.com", AccountStatus.SUSPENDED, null);
        when(userRepository.findByEmail("permban@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("permban@example.com");

        assertThat(details.isAccountNonLocked()).isFalse();
    }

    /**
     * Verifies that loadUserByUsername returns disabled UserDetails when user registration is REJECTED.
     */
    @Test
    @DisplayName("loadUserByUsername: sets disabled=true when account status is REJECTED")
    void loadUserByUsername_whenRejected_setsDisabledTrue() {
        User user = buildUser("rejected@example.com", AccountStatus.REJECTED, null);
        when(userRepository.findByEmail("rejected@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("rejected@example.com");

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    /**
     * Verifies that loadUserByUsername throws UsernameNotFoundException when user does not exist.
     */
    @Test
    @DisplayName("loadUserByUsername: throws UsernameNotFoundException when user is not found")
    void loadUserByUsername_whenNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: missing@example.com");
    }

    private User buildUser(String email, AccountStatus status, LocalDateTime bannedUntil) {
        AuthorizationPermission role = new AuthorizationPermission();
        role.setPermissionKey("CLIENT");

        return User.builder()
                .email(email)
                .password("encoded-pwd")
                .status(status)
                .bannedUntil(bannedUntil)
                .permissions(Set.of(role))
                .build();
    }
}
