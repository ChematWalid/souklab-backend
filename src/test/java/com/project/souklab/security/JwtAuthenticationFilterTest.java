package com.project.souklab.security;

import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void passesThroughWithoutBearerToken() throws Exception {
        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
        verifyNoInteractions(jwtUtils, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void passesThroughWhenTokenIsInvalidOrLoadingFails() throws Exception {
        MockHttpServletRequest request = requestWithBearer("bad-token");
        when(jwtUtils.validateJwtToken("bad-token")).thenReturn(false);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);
        verify(filterChain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void authenticatesEnabledUnlockedUserFromValidToken() throws Exception {
        when(jwtUtils.validateJwtToken("good-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("good-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(List.of());

        filter.doFilterInternal(requestWithBearer("good-token"), new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull()
                .extracting(authentication -> authentication.getPrincipal()).isSameAs(userDetails);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doesNotAuthenticateDisabledOrLockedUser() throws Exception {
        when(jwtUtils.validateJwtToken("token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(false);

        filter.doFilterInternal(requestWithBearer("token"), new MockHttpServletResponse(), filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doesNotAuthenticateLockedEnabledUser() throws Exception {
        when(jwtUtils.validateJwtToken("locked-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("locked-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(false);

        filter.doFilterInternal(requestWithBearer("locked-token"), new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void swallowsAuthenticationLookupFailuresAndContinuesChain() throws Exception {
        when(jwtUtils.validateJwtToken("exploding")).thenThrow(new IllegalStateException("bad token"));
        filter.doFilterInternal(requestWithBearer("exploding"), new MockHttpServletResponse(), filterChain);
        verify(filterChain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
