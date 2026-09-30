package com.project.souklab.security;

import com.project.souklab.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handles OAuth2 authentication failures (such as user cancelling consent or account refusal)
 * by redirecting to the frontend authorized redirect URI with an ?error=<CODE> query parameter,
 * rather than Spring Security's default /login?error.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final AppProperties appProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorCode = resolveErrorCode(request, exception);
        log.warn("OAuth2 authentication failure: code={}, message={}", errorCode, exception.getMessage());

        String callback = appProperties.getOauth().getGoogle().getAuthorizedRedirectUri();
        String targetUrl = UriComponentsBuilder.fromUriString(callback)
                .queryParam("error", errorCode)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }

    private String resolveErrorCode(HttpServletRequest request, AuthenticationException exception) {
        String requestError = request.getParameter("error");
        if (requestError != null && !requestError.isBlank()) {
            return requestError;
        }
        if (exception instanceof OAuth2AuthenticationException oauthException) {
            OAuth2Error error = oauthException.getError();
            if (error != null && error.getErrorCode() != null && !error.getErrorCode().isBlank()) {
                return error.getErrorCode();
            }
        }
        return "ACCESS_DENIED";
    }
}
