package com.project.souklab.config;

import com.project.souklab.config.AvatarProperties.RateLimitProperties;
import com.project.souklab.filestorage.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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

        if (isProduction() && Boolean.TRUE.equals(storageProperties.getS3().getAutoCreateBucket())) {
            throw new IllegalStateException("storage.s3.auto-create-bucket must be false in production");
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
        if (properties.getCache() == null || properties.getCache().getMaximumSize() <= 0
                || properties.getCache().getExpireAfterAccess() == null
                || properties.getCache().getExpireAfterAccess().isZero()
                || properties.getCache().getExpireAfterAccess().isNegative()) {
            throw new IllegalStateException(prefix + ".cache must have positive size and duration");
        }
    }

    private void validateCors(java.util.List<String> origins) {
        if (origins == null || origins.isEmpty() || origins.stream().anyMatch(origin -> origin == null
                || origin.isBlank() || "*".equals(origin.trim()))) {
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
}
