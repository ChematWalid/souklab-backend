package com.project.souklab.security;

import com.project.souklab.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.OAuth oauth;

    @Mock
    private AppProperties.OAuth.Google google;

    private OAuth2AuthenticationFailureHandler failureHandler;

    @BeforeEach
    void setUp() {
        failureHandler = new OAuth2AuthenticationFailureHandler(appProperties);
        when(appProperties.getOauth()).thenReturn(oauth);
        when(oauth.getGoogle()).thenReturn(google);
        when(google.getAuthorizedRedirectUri()).thenReturn("https://souklab.dz/auth/google/callback");
    }

    @Test
    @DisplayName("onAuthenticationFailure: redirects to frontend callback with error code from OAuth2Error")
    void onAuthenticationFailure_withOAuth2Exception_redirectsToFrontendWithError() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2Error error = new OAuth2Error("access_denied", "User cancelled consent", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(error);

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertThat(response.getRedirectedUrl()).isEqualTo("https://souklab.dz/auth/google/callback?error=access_denied");
    }

    @Test
    @DisplayName("onAuthenticationFailure: extracts error param from request query if present")
    void onAuthenticationFailure_withRequestErrorParam_redirectsWithCode() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("error", "interaction_required");
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(new OAuth2Error("unknown"));

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertThat(response.getRedirectedUrl()).isEqualTo("https://souklab.dz/auth/google/callback?error=interaction_required");
    }
}
