package com.project.souklab.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.model.analytics.AnalyticsBucket;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigurationPolicyValidatorTest {
    @Test
    void acceptsCompleteSafeConfiguration() {
        Fixture fixture = new Fixture();
        assertThatCode(() -> fixture.validator.validate()).doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidCoreAndStoragePolicies() {
        assertInvalid(f -> f.avatar.setMaxPerUser(0), "avatar.max-per-user");
        assertInvalid(f -> f.avatar.setAllowedMimeTypes(List.of(" ")), "avatar.allowed-mime-types");
        assertInvalid(f -> f.storage.getValidation().setMaxFileSize(DataSize.ofBytes(0)), "storage.validation.max-file-size");
        assertInvalid(f -> f.storage.getValidation().setAllowedMimeTypes(List.of()), "storage.validation.allowed-mime-types");
        assertInvalid(f -> f.app.getCors().setAllowedOrigins(List.of("*")), "app.cors.allowed-origins");
    }

    @Test
    void validatesRateLimitAsyncCacheAndIndexPolicies() {
        assertInvalid(f -> { f.avatar.getRateLimit().setEnabled(true); f.avatar.getRateLimit().setCapacity(0); }, "avatar.rate-limit.capacity");
        assertInvalid(f -> { f.avatar.getRateLimit().setEnabled(true); f.avatar.getRateLimit().setCapacity(1); f.avatar.getRateLimit().setRefillDuration(Duration.ZERO); }, "refill-duration");
        assertInvalid(f -> f.app.getAsync().getApplication().setCorePoolSize(0), "app.async.application");
        assertInvalid(f -> f.app.getCache().setMaximumSize(0), "app.cache");
        assertInvalid(f -> f.app.getSearch().getMassIndexing().setIdFetchSize(0), "mass-indexing");
    }

    @Test
    void rejectsRemainingRateLimitAndExecutorVariants() {
        assertInvalid(f -> { f.avatar.getRateLimit().setEnabled(true); f.avatar.getRateLimit().setCapacity(1); f.avatar.getRateLimit().setRefillDuration(Duration.ofSeconds(-1)); }, "refill-duration");
        assertInvalid(f -> { f.avatar.getRateLimit().setEnabled(true); f.avatar.getRateLimit().setCapacity(1); f.avatar.getRateLimit().setRefillDuration(null); }, "refill-duration");
        assertInvalid(f -> f.app.getAsync().getApplication().setMaxPoolSize(0), "app.async.application");
        assertInvalid(f -> f.app.getAsync().getApplication().setCorePoolSize(3), "app.async.application");
        assertInvalid(f -> f.app.getAsync().getApplication().setQueueCapacity(0), "app.async.application");
        assertInvalid(f -> f.app.getAsync().getApplication().setThreadNamePrefix(" "), "app.async.application");
        assertInvalid(f -> f.app.getAsync().setWorkflow(null), "app.async.workflow");
        assertInvalid(f -> f.app.getAsync().getWorkflow().setThreadNamePrefix(null), "app.async.workflow");
    }

    @Test
    void rejectsInvalidEndpointUserOverride() {
        Fixture fixture = new Fixture();
        RateLimitEndpointProperties endpoints = new RateLimitEndpointProperties();
        endpoints.getAdminApi().setEnabled(true);
        endpoints.getAdminApi().setCapacity(10);
        endpoints.getAdminApi().setRefillDuration(Duration.ofMinutes(1));
        endpoints.getAdminApi().setUserCapacity(5);
        endpoints.getAdminApi().setUserRefillDuration(Duration.ZERO);
        fixture.validator.setRateLimitEndpointProperties(endpoints);

        assertThatThrownBy(() -> fixture.validator.validate())
                .hasMessageContaining("user-refill-duration");
    }

    @Test
    void validatesEnabledOpenApiContract() {
        Fixture fixture = new Fixture();
        OpenApiProperties openApi = new OpenApiProperties();
        openApi.setEnabled(true);
        openApi.setPath("/v3/api-docs");
        openApi.setSwaggerPath("/swagger-ui.html");
        openApi.setTitle("Souklab API");
        openApi.setVersion("1.0.0");
        fixture.validator.setOpenApiProperties(openApi);

        assertThatCode(() -> fixture.validator.validate()).doesNotThrowAnyException();
        openApi.setPath("v3/api-docs");
        assertThatThrownBy(() -> fixture.validator.validate()).hasMessageContaining("app.openapi.path");
    }

    @Test
    void rejectsEmptyOrDuplicateAnalyticsBuckets() {
        Fixture fixture = new Fixture();
        AnalyticsProperties analytics = validAnalyticsProperties();
        fixture.validator.setAnalyticsProperties(analytics);

        analytics.setSupportedBuckets(List.of());
        assertThatThrownBy(() -> fixture.validator.validate())
                .hasMessageContaining("app.analytics page, range, and bucket limits");

        analytics.setSupportedBuckets(List.of(AnalyticsBucket.DAY, AnalyticsBucket.DAY));
        assertThatThrownBy(() -> fixture.validator.validate())
                .hasMessageContaining("supported-buckets");
    }

    private AnalyticsProperties validAnalyticsProperties() {
        AnalyticsProperties analytics = new AnalyticsProperties();
        analytics.setDefaultPageSize(20);
        analytics.setMaximumPageSize(100);
        analytics.setMaximumRangeDays(366);
        analytics.setMaximumBucketCount(500);
        analytics.setSupportedBuckets(List.of(AnalyticsBucket.DAY, AnalyticsBucket.WEEK));
        analytics.setRollupBatchSize(100);
        analytics.setBackfillBatchSize(100);
        analytics.setQueryTimeout(Duration.ofSeconds(10));
        analytics.setJobRetention(Duration.ofHours(24));
        analytics.setBusinessTimeZone("Africa/Algiers");
        return analytics;
    }

    @Test
    void rejectsMissingAndMalformedNestedPolicies() {
        assertInvalid(f -> f.storage.setValidation(null), "storage.validation.max-file-size");
        assertInvalid(f -> f.app.setAsync(null), "app.async");
        assertInvalid(f -> f.app.getCache().setExpireAfterWrite(null), "app.cache");
        assertInvalid(f -> f.app.getSearch().setMassIndexing(null), "mass-indexing");
        assertInvalid(f -> f.app.getCors().setAllowedOrigins(List.of(" ")), "app.cors.allowed-origins");
        assertInvalid(f -> f.app.getCors().setAllowedOrigins(new ArrayList<>(Collections.singletonList(null))), "app.cors.allowed-origins");
        assertInvalid(f -> f.app.getCors().setAllowedOrigins(List.of()), "app.cors.allowed-origins");
        assertInvalid(f -> f.app.getSearch().getMassIndexing().setThreadsToLoadObjects(-1), "mass-indexing");
        assertInvalid(f -> f.app.getSearch().getMassIndexing().setBatchSizeToLoadObjects(-1), "mass-indexing");
        assertInvalid(f -> f.app.getCache().setExpireAfterWrite(Duration.ofSeconds(-1)), "app.cache");
        assertInvalid(f -> f.app.setDirectory(null), "app.directory");
        assertInvalid(f -> f.app.getDirectory().setDefaultPageIndex(-1), "app.directory");
        assertInvalid(f -> f.app.getDirectory().setDefaultPageSize(0), "app.directory");
        assertInvalid(f -> f.app.getDirectory().setMaxPageSize(0), "app.directory");
        assertInvalid(f -> f.app.getCors().setAllowedOrigins(null), "app.cors.allowed-origins");
        assertInvalid(f -> f.app.getAsync().setApplication(null), "app.async.application");
        assertInvalid(f -> f.app.getAsync().setWorkflow(null), "app.async.workflow");
        assertInvalid(f -> f.app.setCache(null), "app.cache");
        assertInvalid(f -> f.app.getSearch().getMassIndexing().setThreadsToLoadObjects(0), "mass-indexing");
        assertInvalid(f -> f.app.getSearch().getMassIndexing().setBatchSizeToLoadObjects(0), "mass-indexing");
        assertInvalid(f -> f.app.getSearch().getMassIndexing().setIdFetchSize(-1), "mass-indexing");
        assertInvalid(f -> f.app.getDirectory().setDefaultPageSize(0), "app.directory");
        assertInvalid(f -> { f.app.getDirectory().setMinPageSize(10); f.app.getDirectory().setDefaultPageSize(5); }, "app.directory");
        assertInvalid(f -> { f.app.getDirectory().setMinPageSize(10); f.app.getDirectory().setMaxPageSize(5); }, "app.directory");
    }

    @Test
    void rejectsInvalidFavoritesPolicy() {
        assertInvalid(f -> f.app.getFavorites().setMaxPerClient(0), "app.favorites.max-per-client");
    }

    @Test
    void rejectsMissingFileServingPrefix() {
        Fixture fixture = new Fixture();
        fixture.app.getStorage().setFileServingPrefix(null);

        assertThatThrownBy(() -> fixture.validator.validate())
                .hasMessage("app.storage.file-serving-prefix must be configured");
    }

    @Test
    void rejectsProductionBucketAutoCreation() {
        Fixture fixture = new Fixture();
        fixture.storage.getS3().setAutoCreateBucket(true);
        when(fixture.environment.matchesProfiles("prod", "production")).thenReturn(true);

        assertThatThrownBy(() -> fixture.validator.validate())
                .hasMessageContaining("auto-create-bucket");
    }

    private void assertInvalid(Consumer<Fixture> change, String message) {
        Fixture fixture = new Fixture();
        change.accept(fixture);
        assertThatThrownBy(() -> fixture.validator.validate()).hasMessageContaining(message);
    }

    private static final class Fixture {
        final AppProperties app = new AppProperties();
        final AvatarProperties avatar = new AvatarProperties();
        final StorageProperties storage = new StorageProperties();
        final Environment environment = mock(Environment.class);
        final ConfigurationPolicyValidator validator;

        Fixture() {
            avatar.setMaxPerUser(3);
            avatar.setAllowedMimeTypes(List.of("image/jpeg"));
            storage.getValidation().setMaxFileSize(DataSize.ofMegabytes(1));
            storage.getValidation().setAllowedMimeTypes(List.of("image/jpeg"));
            app.getCors().setAllowedOrigins(List.of("http://localhost"));
            app.getStorage().setFileServingPrefix("/api/v1/files/");
            configureExecutor(app.getAsync().getApplication());
            configureExecutor(app.getAsync().getWorkflow());
            app.getCache().setMaximumSize(10); app.getCache().setExpireAfterWrite(Duration.ofMinutes(1));
            app.getSearch().getMassIndexing().setThreadsToLoadObjects(1);
            app.getSearch().getMassIndexing().setBatchSizeToLoadObjects(1);
            app.getSearch().getMassIndexing().setIdFetchSize(1);
            app.getDirectory().setDefaultPageIndex(0);
            app.getDirectory().setDefaultPageSize(20);
            app.getDirectory().setMinPageSize(1);
            app.getDirectory().setMaxPageSize(100);
            app.getFavorites().setMaxPerClient(500);
            when(environment.matchesProfiles("prod", "production")).thenReturn(false);
            validator = new ConfigurationPolicyValidator(app, avatar, storage, environment);
        }

        private void configureExecutor(AppProperties.Async.Executor executor) {
            executor.setCorePoolSize(1); executor.setMaxPoolSize(2);
            executor.setQueueCapacity(10); executor.setThreadNamePrefix("test-");
        }
    }
}
