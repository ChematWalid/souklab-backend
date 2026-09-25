package com.project.souklab.controller.support;

import tools.jackson.databind.json.JsonMapper;
import com.project.souklab.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import com.project.souklab.config.AppProperties;
import com.project.souklab.security.AccessControlService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Shared test security configuration for controller slice tests.
 * Enables method security for @PreAuthorize evaluations, disables CSRF to match
 * production stateless token architecture, and installs an authentication entry point
 * that returns standard 401 ApiResponse JSON.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(AppProperties.class)
@Import(AccessControlService.class)
public class ControllerSliceSecurityConfig {

    /**
     * Configures the test security filter chain.
     */
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http, JsonMapper jsonMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/verify-email",
                                "/api/v1/auth/resend-verification",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/oauth/**",
                                "/api/v1/catalog/**",
                                "/api/v1/public/**",
                                "/api/v1/subscriptions/plans",
                                "/api/v1/subscriptions/plans/**",
                                "/api/v1/integrations/chargily/webhook"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            ApiResponse<Void> apiResponse = ApiResponse.error("UNAUTHORIZED", "Full authentication is required to access this resource");
                            apiResponse.setCode(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(jsonMapper.writeValueAsString(apiResponse));
                            response.getWriter().flush();
                        })
                );
        return http.build();
    }
}
