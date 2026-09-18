package com.project.souklab.filestorage.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Isolated unit tests for StorageProperties configuration binding.
 * Verifies default Java property values and Spring Boot property binding.
 */
class StoragePropertiesTest {

    /**
 * Verifies that a newly instantiated StorageProperties object has no policy defaults.
     */
    @Test
    @DisplayName("StorageProperties uninitialized instance has no Java policy defaults")
    void uninitializedInstanceHasNoJavaPolicyDefaults() {
        StorageProperties properties = new StorageProperties();

        assertThat(properties.getRateLimit()).isNotNull();
        assertThat(properties.getRateLimit().isEnabled()).isFalse();
        assertThat(properties.getRateLimit().getCapacity()).isZero();
        assertThat(properties.getRateLimit().getRefillDuration()).isNull();
        assertThat(properties.getRateLimit().getCache()).isNotNull();
        assertThat(properties.getRateLimit().getCache().getMaximumSize()).isZero();
        assertThat(properties.getRateLimit().getCache().getExpireAfterAccess()).isNull();
    }

    /**
     * Verifies that StorageProperties successfully binds default rate-limiting configuration values.
     */
    @Test
    @DisplayName("StorageProperties binds default rate limiting and cache configuration values")
    void bindsDefaultConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "storage.rate-limit.enabled", "true",
                "storage.rate-limit.capacity", "120",
                "storage.rate-limit.refill-duration", "1m",
                "storage.rate-limit.cache.maximum-size", "10000",
                "storage.rate-limit.cache.expire-after-access", "10m"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        StorageProperties properties = binder.bind("storage", StorageProperties.class).get();

        assertThat(properties.getRateLimit().isEnabled()).isTrue();
        assertThat(properties.getRateLimit().getCapacity()).isEqualTo(120);
        assertThat(properties.getRateLimit().getRefillDuration()).isEqualTo(Duration.ofMinutes(1));
        assertThat(properties.getRateLimit().getCache().getMaximumSize()).isEqualTo(10000L);
        assertThat(properties.getRateLimit().getCache().getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(10));
    }

    /**
     * Verifies that StorageProperties binds custom rate-limiting configuration overrides.
     */
    @Test
    @DisplayName("StorageProperties binds custom rate limiting configuration overrides")
    void bindsCustomConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "storage.rate-limit.enabled", "false",
                "storage.rate-limit.capacity", "60",
                "storage.rate-limit.refill-duration", "30s",
                "storage.rate-limit.cache.maximum-size", "5000",
                "storage.rate-limit.cache.expire-after-access", "5m"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        StorageProperties properties = binder.bind("storage", StorageProperties.class).get();

        assertThat(properties.getRateLimit().isEnabled()).isFalse();
        assertThat(properties.getRateLimit().getCapacity()).isEqualTo(60);
        assertThat(properties.getRateLimit().getRefillDuration()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getRateLimit().getCache().getMaximumSize()).isEqualTo(5000L);
        assertThat(properties.getRateLimit().getCache().getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(5));
    }
}
