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
                "storage.rate-limit.refill-duration", "1m"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        StorageProperties properties = binder.bind("storage", StorageProperties.class).get();

        assertThat(properties.getRateLimit().isEnabled()).isTrue();
        assertThat(properties.getRateLimit().getCapacity()).isEqualTo(120);
        assertThat(properties.getRateLimit().getRefillDuration()).isEqualTo(Duration.ofMinutes(1));
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
                "storage.rate-limit.refill-duration", "30s"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        StorageProperties properties = binder.bind("storage", StorageProperties.class).get();

        assertThat(properties.getRateLimit().isEnabled()).isFalse();
        assertThat(properties.getRateLimit().getCapacity()).isEqualTo(60);
        assertThat(properties.getRateLimit().getRefillDuration()).isEqualTo(Duration.ofSeconds(30));
    }
}
