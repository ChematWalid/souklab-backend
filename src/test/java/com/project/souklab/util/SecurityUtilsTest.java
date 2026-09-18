package com.project.souklab.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void returnsNullForMissingOrUnauthenticatedContext() {
        assertThat(SecurityUtils.getCurrentUsername()).isNull();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "credentials"));
        assertThat(SecurityUtils.getCurrentUsername()).isNull();
    }

    @Test void resolvesUserDetailsAndStringPrincipals() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        User.withUsername("details@example.test").password("x").authorities(List.of()).build(), "x", List.of()));
        assertThat(SecurityUtils.getCurrentUsername()).isEqualTo("details@example.test");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("string@example.test", "x", List.of()));
        assertThat(SecurityUtils.getCurrentUsername()).isEqualTo("string@example.test");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("anonymousUser", "x", List.of()));
        assertThat(SecurityUtils.getCurrentUsername()).isNull();
    }

    @Test
    void fallsBackToAuthenticationNameForNonStandardPrincipal() {
        var authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        org.mockito.Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn(new Object());
        org.mockito.Mockito.when(authentication.getName()).thenReturn("named-user");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(SecurityUtils.getCurrentUsername()).isEqualTo("named-user");

        org.mockito.Mockito.when(authentication.getName()).thenReturn("anonymousUser");
        assertThat(SecurityUtils.getCurrentUsername()).isNull();
    }
}
