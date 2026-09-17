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
 * Isolated unit tests verifying empty Java defaults and property binding for
 * SupportConfig, Admin defaults, OAuth cookie settings, AuthConfig.VerificationConfig,
 * and ArtisanConfig.FormateurConfig in {@link AppProperties}.
 */
class AppPropertiesTest {

    @Test
    @DisplayName("AppProperties does not provide policy defaults in Java")
    void policyDefaultsAreNotProvidedByJava() {
        AppProperties appProperties = new AppProperties();

        // Support
        assertThat(appProperties.getSupport()).isNotNull();
        assertThat(appProperties.getSupport().getEmail()).isNull();
        assertThat(appProperties.getSupport().getContactMessage()).isNull();

        // Admin defaults
        assertThat(appProperties.getAdmin()).isNotNull();
        assertThat(appProperties.getAdmin().getDefaultBanReason()).isNull();
        assertThat(appProperties.getAdmin().getDefaultTimeoutReason()).isNull();

        // OAuth
        assertThat(appProperties.getOauth()).isNotNull();
        assertThat(appProperties.getOauth().getIntentCookieMaxAgeSeconds()).isZero();

        // Auth verification
        assertThat(appProperties.getAuth()).isNotNull();
        assertThat(appProperties.getAuth().getVerification()).isNotNull();
        assertThat(appProperties.getAuth().getVerification().getMaxAttempts()).isZero();
        assertThat(appProperties.getAuth().getVerification().getExpirationMinutes()).isZero();
        assertThat(appProperties.getAuth().getVerification().getCodeLength()).isZero();

        // Artisan formateur
        assertThat(appProperties.getArtisan()).isNotNull();
        assertThat(appProperties.getArtisan().getFormateur()).isNotNull();
        assertThat(appProperties.getArtisan().getFormateur().getReapplyCooldownDays()).isZero();
    }

    @Test
    @DisplayName("AppProperties binds custom values from environment / property sources")
    void bindsCustomConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test-app-properties", Map.ofEntries(
                Map.entry("app.support.email", "help@customdomain.com"),
                Map.entry("app.support.contact-message", "Please reach out to support team."),
                Map.entry("app.admin.default-ban-reason", "Violated community guidelines"),
                Map.entry("app.admin.default-timeout-reason", "Temporary suspension"),
                Map.entry("app.oauth.intent-cookie-max-age-seconds", "600"),
                Map.entry("app.auth.verification.max-attempts", "3"),
                Map.entry("app.auth.verification.expiration-minutes", "30"),
                Map.entry("app.auth.verification.code-length", "8"),
                Map.entry("app.artisan.formateur.reapply-cooldown-days", "30"),
                Map.entry("app.async.application.core-pool-size", "2"),
                Map.entry("app.async.application.max-pool-size", "8"),
                Map.entry("app.async.application.queue-capacity", "50"),
                Map.entry("app.async.application.thread-name-prefix", "custom-async-"),
                Map.entry("app.cache.expire-after-write", "15m"),
                Map.entry("app.cache.maximum-size", "250"),
                Map.entry("app.search.mass-indexing.threads-to-load-objects", "3"),
                Map.entry("app.search.mass-indexing.batch-size-to-load-objects", "40"),
                Map.entry("app.search.mass-indexing.id-fetch-size", "75")
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
        assertThat(appProperties.getAsync().getApplication().getCorePoolSize()).isEqualTo(2);
        assertThat(appProperties.getAsync().getApplication().getMaxPoolSize()).isEqualTo(8);
        assertThat(appProperties.getAsync().getApplication().getQueueCapacity()).isEqualTo(50);
        assertThat(appProperties.getAsync().getApplication().getThreadNamePrefix()).isEqualTo("custom-async-");
        assertThat(appProperties.getCache().getExpireAfterWrite()).hasMinutes(15);
        assertThat(appProperties.getCache().getMaximumSize()).isEqualTo(250);
        assertThat(appProperties.getSearch().getMassIndexing().getThreadsToLoadObjects()).isEqualTo(3);
        assertThat(appProperties.getSearch().getMassIndexing().getBatchSizeToLoadObjects()).isEqualTo(40);
        assertThat(appProperties.getSearch().getMassIndexing().getIdFetchSize()).isEqualTo(75);
    }
}
