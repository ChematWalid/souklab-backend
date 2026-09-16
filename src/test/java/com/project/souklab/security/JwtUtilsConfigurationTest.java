package com.project.souklab.security;

import com.project.souklab.config.AppProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies fail-fast validation of JWT security configuration.
 */
class JwtUtilsConfigurationTest {

    @Test
    void rejectsShortSecrets() {
        AppProperties properties = validProperties();
        properties.getJwt().setSecret("too-short");

        assertThatThrownBy(() -> new JwtUtils(properties, Clock.systemUTC()).validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 UTF-8 bytes");
    }

    @Test
    void rejectsNonPositiveExpiration() {
        AppProperties properties = validProperties();
        properties.getJwt().setAccessTokenExpirationMs(0L);

        assertThatThrownBy(() -> new JwtUtils(properties, Clock.systemUTC()).validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("access-token-expiration-ms");
    }

    private AppProperties validProperties() {
        AppProperties properties = new AppProperties();
        properties.getJwt().setSecret("a-valid-secret-with-at-least-32-utf8-bytes");
        properties.getJwt().setAccessTokenExpirationMs(3_600_000L);
        properties.getJwt().setRefreshTokenExpirationMs(86_400_000L);
        return properties;
    }
}
