package com.project.souklab.config;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.security.FileRateLimitFilter;
import com.project.souklab.security.AvatarUploadRateLimitFilter;
import com.project.souklab.security.AvatarUploadSizeFilter;
import com.project.souklab.security.JwtAuthenticationFilter;
import com.project.souklab.security.OAuth2AuthenticationSuccessHandler;
import com.project.souklab.security.RateLimitFilter;
import com.project.souklab.util.ServletResponseUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties appProperties;
    private final StorageProperties storageProperties;
    private final AvatarProperties avatarProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final FileRateLimitFilter fileRateLimitFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final ServletResponseUtil servletResponseUtil;

    @Bean
    public AvatarUploadSizeFilter avatarUploadSizeFilter() {
        return new AvatarUploadSizeFilter(storageProperties, servletResponseUtil);
    }

    @Bean
    public AvatarUploadRateLimitFilter avatarUploadRateLimitFilter() {
        return new AvatarUploadRateLimitFilter(servletResponseUtil, avatarProperties);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(rateLimitFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<FileRateLimitFilter> fileRateLimitFilterRegistration() {
        FilterRegistrationBean<FileRateLimitFilter> registration = new FilterRegistrationBean<>(fileRateLimitFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AvatarUploadSizeFilter> avatarUploadSizeFilterRegistration(AvatarUploadSizeFilter filter) {
        FilterRegistrationBean<AvatarUploadSizeFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AvatarUploadRateLimitFilter> avatarUploadRateLimitFilterRegistration(AvatarUploadRateLimitFilter filter) {
        FilterRegistrationBean<AvatarUploadRateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the security filter chain for stateless JWT authentication.
     * CSRF protection is intentionally disabled — this API is stateless and
     * token-authenticated (JWT Bearer), not cookie/session-based, so CSRF does not apply.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
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
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error",
                                "/ws/**",
                                "/api/v1/catalog/**",
                                "/api/v1/public/**",
                                "/api/v1/feed/**",
                                "/api/v1/artisans/*/reviews"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                servletResponseUtil.writeResponse(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        ApiResponse.error("Unauthorized: " + authException.getMessage())
                                )
                        )
                );

        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(fileRateLimitFilter, JwtAuthenticationFilter.class);
        http.addFilterAfter(avatarUploadSizeFilter(), JwtAuthenticationFilter.class);
        http.addFilterAfter(avatarUploadRateLimitFilter(), AvatarUploadSizeFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(appProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(List.of("Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
