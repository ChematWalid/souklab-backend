package com.project.souklab.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Isolated unit tests verifying default values and property binding for
 * SupportConfig, Admin defaults, OAuth cookie settings, AuthConfig.VerificationConfig,
 * and ArtisanConfig.FormateurConfig in {@link AppProperties}.
 */
class AppPropertiesTest {

    @Test
    @DisplayName("AppProperties default values are correctly initialized")
    void defaultValuesArePresentUponInstantiation() {
        AppProperties appProperties = new AppProperties();

        // Support
        assertThat(appProperties.getSupport()).isNotNull();
        assertThat(appProperties.getSupport().getEmail()).isEqualTo("support@souklab.dz");
        assertThat(appProperties.getSupport().getContactMessage()).isEqualTo("Please contact support.");

        // Admin defaults
        assertThat(appProperties.getAdmin()).isNotNull();
        assertThat(appProperties.getAdmin().getDefaultBanReason()).isEqualTo("Account banned by administrator");
        assertThat(appProperties.getAdmin().getDefaultTimeoutReason()).isEqualTo("Account timed out by administrator");

        // OAuth
        assertThat(appProperties.getOauth()).isNotNull();
        assertThat(appProperties.getOauth().getIntentCookieMaxAgeSeconds()).isEqualTo(300);

        // Auth verification
        assertThat(appProperties.getAuth()).isNotNull();
        assertThat(appProperties.getAuth().getVerification()).isNotNull();
        assertThat(appProperties.getAuth().getVerification().getMaxAttempts()).isEqualTo(5);
        assertThat(appProperties.getAuth().getVerification().getExpirationMinutes()).isEqualTo(15);
        assertThat(appProperties.getAuth().getVerification().getCodeLength()).isEqualTo(6);

        // Artisan formateur
        assertThat(appProperties.getArtisan()).isNotNull();
        assertThat(appProperties.getArtisan().getFormateur()).isNotNull();
        assertThat(appProperties.getArtisan().getFormateur().getReapplyCooldownDays()).isEqualTo(14L);
    }

    @Test
    @DisplayName("AppProperties binds custom values from environment / property sources")
    void bindsCustomConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test-app-properties", Map.of(
                "app.support.email", "help@customdomain.com",
                "app.support.contact-message", "Please reach out to support team.",
                "app.admin.default-ban-reason", "Violated community guidelines",
                "app.admin.default-timeout-reason", "Temporary suspension",
                "app.oauth.intent-cookie-max-age-seconds", "600",
                "app.auth.verification.max-attempts", "3",
                "app.auth.verification.expiration-minutes", "30",
                "app.auth.verification.code-length", "8",
                "app.artisan.formateur.reapply-cooldown-days", "30"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AppProperties appProperties = binder.bind("app", AppProperties.class).get();

        assertThat(appProperties.getSupport().getEmail()).isEqualTo("help@customdomain.com");
        assertThat(appProperties.getSupport().getContactMessage()).isEqualTo("Please reach out to support team.");
        assertThat(appProperties.getAdmin().getDefaultBanReason()).isEqualTo("Violated community guidelines");
        assertThat(appProperties.getAdmin().getDefaultTimeoutReason()).isEqualTo("Temporary suspension");
        assertThat(appProperties.getOauth().getIntentCookieMaxAgeSeconds()).isEqualTo(600);
        assertThat(appProperties.getAuth().getVerification().getMaxAttempts()).isEqualTo(3);
        assertThat(appProperties.getAuth().getVerification().getExpirationMinutes()).isEqualTo(30);
        assertThat(appProperties.getAuth().getVerification().getCodeLength()).isEqualTo(8);
        assertThat(appProperties.getArtisan().getFormateur().getReapplyCooldownDays()).isEqualTo(30L);
    }
}
