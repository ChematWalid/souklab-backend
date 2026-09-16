package com.project.souklab.service.auth;

import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.AuthorizationPermission;
import com.project.souklab.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Set;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Verifies construction of permission-based Spring Security principals.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadsEnabledPermissionsAsAuthorities() {
        AuthorizationPermission permission = new AuthorizationPermission("permission:profile:read", "Read profiles", true);
        User user = User.builder().email("user@example.com").password("secret").status(AccountStatus.ACTIVE)
                .permissions(Set.of(permission)).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        var details = new CustomUserDetailsService(userRepository, Clock.systemUTC()).loadUserByUsername(user.getEmail());

        assertThat(details.getAuthorities()).extracting(Object::toString).contains("permission:profile:read");
    }
}
