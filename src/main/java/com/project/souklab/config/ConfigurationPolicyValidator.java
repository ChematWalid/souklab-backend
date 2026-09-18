package com.project.souklab.config;

import com.project.souklab.config.AvatarProperties.RateLimitProperties;
import com.project.souklab.filestorage.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Base64;

/** Fails fast on unsafe or unusable runtime policy configuration. */
@Component
@RequiredArgsConstructor
public class ConfigurationPolicyValidator {

    private final AppProperties appProperties;
    private final AvatarProperties avatarProperties;
    private final StorageProperties storageProperties;
    private final Environment environment;

    @PostConstruct
    void validate() {
        requirePositive("avatar.max-per-user", avatarProperties.getMaxPerUser());
        requireNonEmpty("avatar.allowed-mime-types", avatarProperties.getAllowedMimeTypes());
        validateRateLimit("avatar.rate-limit", avatarProperties.getRateLimit());

        StorageProperties.ValidationProperties validation = storageProperties.getValidation();
        if (validation == null || validation.getMaxFileSize() == null
                || validation.getMaxFileSize().toBytes() <= 0) {
            throw new IllegalStateException("storage.validation.max-file-size must be positive");
        }
        requireNonEmpty("storage.validation.allowed-mime-types", validation.getAllowedMimeTypes());
        requireConfigured("app.storage.file-serving-prefix", appProperties.getStorage().getFileServingPrefix());
        validateCors(appProperties.getCors().getAllowedOrigins());
        validateAsync(appProperties.getAsync());
        validateCache(appProperties.getCache());
        validateMassIndexing(appProperties.getSearch().getMassIndexing());
        validateDirectory(appProperties.getDirectory());
        validateSubscriptionPolicy();

        if (isProduction() && Boolean.TRUE.equals(storageProperties.getS3().getAutoCreateBucket())) {
            throw new IllegalStateException("storage.s3.auto-create-bucket must be false in production");
        }

        if (isProduction()) {
            validateProductionPolicy();
        }
    }

    private void validateSubscriptionPolicy() {
        SubscriptionProperties subscription = appProperties.getSubscription();
        ChargilyProperties chargily = appProperties.getChargily();
        if (chargily != null && chargily.isEnabled()) {
            validateChargily(chargily);
        }
        if (subscription == null || !subscription.isEnabled()) {
            return;
        }
        if (!"DZD".equals(subscription.getCurrency())) {
            throw new IllegalStateException("app.subscription.currency must be DZD");
        }
        if (subscription.getMinimumPlanAmount() <= 0
                || subscription.getMaximumPlanAmount() < subscription.getMinimumPlanAmount()) {
            throw new IllegalStateException("app.subscription plan amount bounds are invalid");
        }
        if (subscription.getLifecycleBatchSize() <= 0 || subscription.getReminderOffsets() == null
                || subscription.getReminderOffsets().stream().anyMatch(offset -> offset == null || offset <= 0)) {
            throw new IllegalStateException("app.subscription lifecycle settings are invalid");
        }
        validatePositiveDuration("app.subscription.lifecycle-interval", subscription.getLifecycleInterval());
        requireConfigured("app.subscription.lifecycle-time-zone", subscription.getLifecycleTimeZone());
        try {
            ZoneId.of(subscription.getLifecycleTimeZone());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("app.subscription.lifecycle-time-zone must be a valid time zone", exception);
        }
        validatePositiveDuration("app.subscription.checkout-idempotency-retention", subscription.getCheckoutIdempotencyRetention());
        validatePositiveDuration("app.subscription.webhook-retention", subscription.getWebhookRetention());
        if (chargily == null || !chargily.isEnabled()) {
            throw new IllegalStateException("enabled subscriptions require enabled Chargily configuration");
        }
        validateChargily(chargily);
    }

    private void validateChargily(ChargilyProperties chargily) {
        requireConfigured("app.chargily.api-key", chargily.getApiKey());
        requireConfigured("app.chargily.secret-key", chargily.getSecretKey());
        requireConfigured("app.chargily.base-url", chargily.getBaseUrl());
        requireConfigured("app.chargily.webhook-encryption-key", chargily.getWebhookEncryptionKey());
        validateUrl("app.chargily.base-url", chargily.getBaseUrl());
        validateUrl("app.chargily.webhook-url", chargily.getWebhookUrl());
        validateUrl("app.chargily.success-url", chargily.getSuccessUrl());
        validateUrl("app.chargily.failure-url", chargily.getFailureUrl());
        validatePositiveDuration("app.chargily.connect-timeout", chargily.getConnectTimeout());
        validatePositiveDuration("app.chargily.read-timeout", chargily.getReadTimeout());
        validatePositiveDuration("app.chargily.response-timeout", chargily.getResponseTimeout());
        validatePositiveDuration("app.chargily.retry-backoff", chargily.getRetryBackoff());
        if (chargily.getRetryCount() < 0 || chargily.getRequestBodyLimit() <= 0
                || !"DZD".equals(chargily.getCurrency())) {
            throw new IllegalStateException("Chargily retry, request limit, or currency configuration is invalid");
        }
        try {
            if (Base64.getDecoder().decode(chargily.getWebhookEncryptionKey()).length != 32) {
                throw new IllegalStateException("app.chargily.webhook-encryption-key must decode to 32 bytes");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("app.chargily.webhook-encryption-key must be valid Base64", exception);
        }
    }

    private void validateUrl(String name, String value) {
        requireConfigured(name, value);
        try {
            URI uri = URI.create(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("not absolute");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(name + " must be a valid absolute URL", exception);
        }
    }

    private void validateProductionPolicy() {
        requireExactValue("spring.jpa.hibernate.ddl-auto", environment.getProperty("spring.jpa.hibernate.ddl-auto"), "validate");
        requireExactValue("spring.jpa.show-sql", environment.getProperty("spring.jpa.show-sql"), "false");
        requireExactValue("spring.jpa.properties.hibernate.format_sql", environment.getProperty("spring.jpa.properties.hibernate.format_sql"), "false");
        requireExactValue("spring.flyway.enabled", environment.getProperty("spring.flyway.enabled"), "true");
        requireExactValue("spring.mail.properties.mail.debug", environment.getProperty("spring.mail.properties.mail.debug"), "false");
        requirePositiveProperty("spring.mail.properties.mail.smtp.connectiontimeout");
        requirePositiveProperty("spring.mail.properties.mail.smtp.timeout");
        requirePositiveProperty("spring.mail.properties.mail.smtp.writetimeout");
        requireExactValue("logging.level.root", environment.getProperty("logging.level.root"), "INFO");
        requireExactValue("logging.level.com.project.souklab", environment.getProperty("logging.level.com.project.souklab"), "INFO");
        requireExactValue("logging.level.org.hibernate.search", environment.getProperty("logging.level.org.hibernate.search"), "WARN");
        requireExactValue("management.endpoint.health.show-details", environment.getProperty("management.endpoint.health.show-details"), "never");
        requireExactValue("management.endpoints.web.exposure.include", environment.getProperty("management.endpoints.web.exposure.include"), "health,info,prometheus");
        requireExactValue("management.info.env.enabled", environment.getProperty("management.info.env.enabled"), "false");
        validatePositiveDuration("app.mailersend.connection-timeout", appProperties.getMailersend().getConnectionTimeout());
        validatePositiveDuration("app.mailersend.read-timeout", appProperties.getMailersend().getReadTimeout());
        requireExactValue("storage.provider", environment.getProperty("storage.provider"), "s3");
        requireExactValue("storage.virus-scan.enabled", environment.getProperty("storage.virus-scan.enabled"), "true");
        requireExactValue("storage.virus-scan.fail-open", environment.getProperty("storage.virus-scan.fail-open"), "false");
        requireExactValue("app.admin.bootstrap-enabled", environment.getProperty("app.admin.bootstrap-enabled"), "false");
        requireExactValue("app.rate-limit.backend", environment.getProperty("app.rate-limit.backend"), "redis");
        requireConfigured("app.rate-limit.redis.host", environment.getProperty("app.rate-limit.redis.host"));
        requirePositiveProperty("app.rate-limit.redis.port");
        requirePositiveProperty("app.rate-limit.redis.connection-timeout");
        requirePositiveProperty("app.rate-limit.redis.command-timeout");
        requireExactValue("app.search.enabled", environment.getProperty("app.search.enabled"), "true");
        if (appProperties.getSearch().isSchemaBootstrapEnabled()) {
            requireExactValue("app.search.schema-management", appProperties.getSearch().getSchemaManagement(), "create-or-update");
        } else {
            requireExactValue("app.search.schema-management", appProperties.getSearch().getSchemaManagement(), "validate");
        }
        requireConfigured("app.search.uris", appProperties.getSearch().getUris());
        requireConfigured("app.relay.host", appProperties.getRelay().getHost());
        requireConfigured("app.relay.client-login", appProperties.getRelay().getClientLogin());
        requireConfigured("app.relay.client-passcode", appProperties.getRelay().getClientPasscode());
        requireConfigured("app.relay.system-login", appProperties.getRelay().getSystemLogin());
        requireConfigured("app.relay.system-passcode", appProperties.getRelay().getSystemPasscode());
    }

    private void requireExactValue(String name, String actual, String expected) {
        if (!expected.equalsIgnoreCase(actual)) {
            throw new IllegalStateException(name + " must be " + expected + " in production");
        }
    }

    private void validateRateLimit(String prefix, RateLimitProperties properties) {
        if (properties == null || !properties.isEnabled()) {
            return;
        }
        requirePositive(prefix + ".capacity", properties.getCapacity());
        if (properties.getRefillDuration() == null || properties.getRefillDuration().isZero()
                || properties.getRefillDuration().isNegative()) {
            throw new IllegalStateException(prefix + ".refill-duration must be positive");
        }
    }

    private void validateCors(java.util.List<String> origins) {
        if (origins == null || origins.isEmpty() || origins.stream().anyMatch(origin -> origin == null
                || origin.isBlank() || origin.contains("*"))) {
            throw new IllegalStateException("app.cors.allowed-origins must contain explicit origins when credentials are enabled");
        }
    }

    private boolean isProduction() {
        return environment.matchesProfiles("prod", "production");
    }

    private void validateAsync(AppProperties.Async async) {
        if (async == null) {
            throw new IllegalStateException("app.async must be configured");
        }
        validateExecutor("app.async.application", async.getApplication());
        validateExecutor("app.async.workflow", async.getWorkflow());
    }

    private void validateExecutor(String name, AppProperties.Async.Executor executor) {
        if (executor == null || executor.getCorePoolSize() <= 0 || executor.getMaxPoolSize() <= 0
                || executor.getCorePoolSize() > executor.getMaxPoolSize()
                || executor.getQueueCapacity() <= 0
                || executor.getThreadNamePrefix() == null || executor.getThreadNamePrefix().isBlank()) {
            throw new IllegalStateException(name + " must define valid pool sizes, queue capacity, and thread prefix");
        }
    }

    private void validateCache(AppProperties.Cache cache) {
        if (cache == null || cache.getMaximumSize() <= 0 || cache.getExpireAfterWrite() == null
                || cache.getExpireAfterWrite().isZero() || cache.getExpireAfterWrite().isNegative()) {
            throw new IllegalStateException("app.cache must define a positive size and expiration");
        }
    }

    private void validateMassIndexing(AppProperties.Search.MassIndexing indexing) {
        if (indexing == null || indexing.getThreadsToLoadObjects() <= 0
                || indexing.getBatchSizeToLoadObjects() <= 0 || indexing.getIdFetchSize() <= 0) {
            throw new IllegalStateException("app.search.mass-indexing values must be positive");
        }
    }

    private void validateDirectory(AppProperties.Directory directory) {
        if (directory == null || directory.getDefaultPageIndex() < 0
                || directory.getDefaultPageSize() < directory.getMinPageSize()
                || directory.getMinPageSize() <= 0
                || directory.getMaxPageSize() < directory.getMinPageSize()) {
            throw new IllegalStateException("app.directory pagination values must be valid");
        }
    }

    private void requirePositive(String name, long value) {
        if (value <= 0) {
            throw new IllegalStateException(name + " must be positive");
        }
    }

    private void requireNonEmpty(String name, java.util.List<String> values) {
        if (values == null || values.isEmpty() || values.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalStateException(name + " must not be empty");
        }
    }

    private void requireConfigured(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be configured");
        }
    }

    private void requirePositiveProperty(String name) {
        String value = environment.getProperty(name);
        try {
            if (value == null || Long.parseLong(value) <= 0) {
                throw new IllegalStateException(name + " must be positive in production");
            }
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(name + " must be a positive integer in production", exception);
        }
    }

    private void validatePositiveDuration(String name, java.time.Duration value) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalStateException(name + " must be positive in production");
        }
    }
}
