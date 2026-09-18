package com.project.souklab.service.user;

import com.project.souklab.dao.UserRepository;
import com.project.souklab.exception.ResourceNotFoundException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserProviderTest {
    @Mock UserRepository users;

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void resolvesAndNormalizesAuthenticatedEmail() {
        User user = User.builder().email("person@example.test").build();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("  PERSON@EXAMPLE.TEST  ", "credentials", java.util.List.of()));
        when(users.findByEmail("person@example.test")).thenReturn(Optional.of(user));

        assertThat(new CurrentUserProvider(users).requireCurrentUser()).isSameAs(user);
        verify(users).findByEmail("person@example.test");
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThatThrownBy(() -> new CurrentUserProvider(users).requireCurrentUser())
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void rejectsBlankAuthenticatedName() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("   ", "credentials", java.util.List.of()));

        assertThatThrownBy(() -> new CurrentUserProvider(users).requireCurrentUser())
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void reportsMissingAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("missing@example.test", "credentials", java.util.List.of()));
        when(users.findByEmail("missing@example.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CurrentUserProvider(users).requireCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
