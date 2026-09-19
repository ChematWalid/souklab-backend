package com.project.souklab.security;

import com.project.souklab.dto.auth.JwtResponseDTO;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.service.auth.AuthService;
import com.project.souklab.util.ServletResponseUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String OAUTH_INTENT_COOKIE_NAME = "SOUKLAB_OAUTH_INTENT";

    @Lazy
    private final AuthService authService;
    private final ServletResponseUtil servletResponseUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            throw new IllegalArgumentException("Expected principal of type OAuth2User, but found: "
                    + (authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getSimpleName() : "null"));
        }

        String intentRole = extractIntentRole(request);

        JwtResponseDTO jwtResponse = authService.processOAuth2Success(oAuth2User, intentRole, request);

        clearIntentCookie(response);

        ApiResponse<JwtResponseDTO> apiResponse = ApiResponse.success(jwtResponse, "Google OAuth authentication successful.");
        servletResponseUtil.writeResponse(response, HttpServletResponse.SC_OK, apiResponse);
    }

    private String extractIntentRole(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (OAUTH_INTENT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String sessionRole = (String) request.getSession().getAttribute(OAUTH_INTENT_COOKIE_NAME);
        if (sessionRole != null) {
            request.getSession().removeAttribute(OAUTH_INTENT_COOKIE_NAME);
            return sessionRole;
        }
        return null;
    }

    private void clearIntentCookie(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from(OAUTH_INTENT_COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }
}
