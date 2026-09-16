package com.project.souklab.filestorage.controller;

import com.project.souklab.config.AppProperties;
import com.project.souklab.config.AvatarProperties;
import com.project.souklab.config.SecurityConfig;
import com.project.souklab.filestorage.StorageService;
import com.project.souklab.service.storage.FileAccessService;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.security.FileRateLimitFilter;
import com.project.souklab.security.JwtAuthenticationFilter;
import com.project.souklab.security.JwtUtils;
import com.project.souklab.security.OAuth2AuthenticationSuccessHandler;
import com.project.souklab.security.RateLimitFilter;
import com.project.souklab.util.ServletResponseUtil;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security slice test verifying that {@link SecurityConfig}'s authorization rules apply
 * strictly to initial {@link DispatcherType#REQUEST} dispatches on {@code /api/v1/files/**},
 * rejecting unauthenticated calls with 401, while permitting internal async/error dispatches.
 */
@WebMvcTest(controllers = FileServingController.class)
@Import({SecurityConfig.class, FileServingSecuritySliceTest.TestFilterConfig.class, FileServingAsyncTestConfig.class})
class FileServingSecuritySliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private FileAccessService fileAccessService;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Configuration
    static class TestFilterConfig {
        @Bean
        public AppProperties appProperties() {
            AppProperties appProperties = new AppProperties();
            appProperties.getRateLimit().setEnabled(false);
            appProperties.getRateLimit().setCapacity(100);
            appProperties.getRateLimit().setRefillDuration(Duration.ofMinutes(1));
            appProperties.getRateLimit().getCache().setMaximumSize(10000L);
            appProperties.getRateLimit().getCache().setExpireAfterAccess(Duration.ofMinutes(10));
            return appProperties;
        }

        @Bean
        public StorageProperties storageProperties() {
            return new StorageProperties();
        }

        @Bean
        public AvatarProperties avatarProperties() {
            return new AvatarProperties();
        }

        @Bean
        public JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }

        @Bean
        public ServletResponseUtil servletResponseUtil(JsonMapper jsonMapper) {
            return new ServletResponseUtil(jsonMapper);
        }

        @Bean
        public RateLimitFilter rateLimitFilter(ServletResponseUtil servletResponseUtil, AppProperties appProperties) {
            return new RateLimitFilter(servletResponseUtil, appProperties);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }

        @Bean
        public JwtUtils jwtUtils() {
            return mock(JwtUtils.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
            return new JwtAuthenticationFilter(jwtUtils, userDetailsService);
        }

        @Bean
        public FileRateLimitFilter fileRateLimitFilter(ServletResponseUtil servletResponseUtil, StorageProperties storageProperties) {
            return new FileRateLimitFilter(servletResponseUtil, storageProperties);
        }
    }

    @Test
    @DisplayName("GET /api/v1/files/{key} without token is rejected with 401 on initial REQUEST dispatch")
    void unauthenticatedInitialRequest_isRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/v1/files/{key}", "test-file-key.png"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized: Full authentication is required to access this resource"));
    }
}
