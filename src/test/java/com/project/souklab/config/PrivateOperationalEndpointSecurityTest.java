package com.project.souklab.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the public/private boundary for operational probes, detailed health,
 * Prometheus metrics, and generated OpenAPI documentation.
 *
 * <p>These checks require the Docker-backed Phase 10 environment because the
 * application context validates the configured infrastructure dependencies.
 * Enable them with {@code PHASE10_ENDPOINT_SECURITY=true}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "PHASE10_ENDPOINT_SECURITY", matches = "true")
class PrivateOperationalEndpointSecurityTest {

    private static final String LIVENESS_PATH = "/actuator/health/liveness";
    private static final String READINESS_PATH = "/actuator/health/readiness";
    private static final String DETAILED_HEALTH_PATH = "/actuator/health";
    private static final String PROMETHEUS_PATH = "/actuator/prometheus";
    private static final String OPENAPI_PATH = "/v3/api-docs";
    private static final String SWAGGER_PATH = "/swagger-ui.html";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicProbesRemainReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get(LIVENESS_PATH))
                .andExpect(result -> assertNotEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus()));
        mockMvc.perform(get(READINESS_PATH))
                .andExpect(result -> assertNotEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus()));
    }

    @Test
    void detailedOperationalEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get(DETAILED_HEALTH_PATH))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(PROMETHEUS_PATH))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(OPENAPI_PATH))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(SWAGGER_PATH))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedOperatorsCanInspectPrivateOperationalEndpoints() throws Exception {
        mockMvc.perform(get(DETAILED_HEALTH_PATH).with(user("operator")))
                .andExpect(result -> assertNotEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus()));
        mockMvc.perform(get(PROMETHEUS_PATH).with(user("operator")))
                .andExpect(result -> assertNotEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus()));
        mockMvc.perform(get(OPENAPI_PATH).with(user("operator")))
                .andExpect(result -> assertNotEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus()));
        mockMvc.perform(get(SWAGGER_PATH).with(user("operator")))
                .andExpect(result -> assertNotEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus()));
    }
}
