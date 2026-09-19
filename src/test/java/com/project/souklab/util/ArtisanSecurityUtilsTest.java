package com.project.souklab.util;

import org.mockito.Mockito;

import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.exception.ForbiddenException;
import com.project.souklab.exception.UnauthorizedException;
import com.project.souklab.model.Artisan;
import com.project.souklab.security.Permission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtisanSecurityUtilsTest {

    @Mock
    private ArtisanRepository artisanRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsMissingAndAnonymousAuthentication() {
        assertThatThrownBy(() -> ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository))
                .isInstanceOf(UnauthorizedException.class);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of()));
        assertThatThrownBy(() -> ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository))
                .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(artisanRepository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null));
        assertThatThrownBy(() -> ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void rejectsMissingPermissionAndMissingUsername() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null, List.of()));
        assertThatThrownBy(() -> ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository))
                .isInstanceOf(ForbiddenException.class);

        Authentication noUsername = Mockito.mock(Authentication.class);
        when(noUsername.isAuthenticated()).thenReturn(true);
        when(noUsername.getName()).thenReturn(null);
        when(noUsername.getPrincipal()).thenReturn(new Object());
        Mockito.doReturn(List.<GrantedAuthority>of(
                Permission.Artisan.CONTENT))
                .when(noUsername).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(noUsername);
        assertThatThrownBy(() -> ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void resolvesByEmailThenFallsBackToId() {
        Artisan artisan = Artisan.builder().id("id-1").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("id-1", null,
                        List.of(Permission.Artisan.CONTENT)));
        when(artisanRepository.findByUserEmailIgnoreCase("id-1")).thenReturn(Optional.empty());
        when(artisanRepository.findById("id-1")).thenReturn(Optional.of(artisan));

        assertThat(ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository)).isSameAs(artisan);
        verify(artisanRepository).findByUserEmailIgnoreCase("id-1");
        verify(artisanRepository).findById("id-1");
    }

    @Test
    void resolvesByEmailAndRejectsUnregisteredArtisan() {
        Artisan artisan = Artisan.builder().id("id-2").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("artisan@example.com", null,
                        List.of(Permission.Artisan.CONTENT)));
        when(artisanRepository.findByUserEmailIgnoreCase("artisan@example.com"))
                .thenReturn(Optional.of(artisan));

        assertThat(ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository)).isSameAs(artisan);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing", null,
                        List.of(Permission.Artisan.CONTENT)));
        when(artisanRepository.findByUserEmailIgnoreCase("missing")).thenReturn(Optional.empty());
        when(artisanRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ArtisanSecurityUtils.resolveAuthenticatedArtisan(artisanRepository))
                .isInstanceOf(ForbiddenException.class);
    }
}
