package com.project.souklab.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AvatarPropertiesTest {

    @Test
    @DisplayName("AvatarProperties uninitialized instance has no hardcoded Java defaults")
    void uninitializedInstanceHasNoJavaDefaults() {
        AvatarProperties properties = new AvatarProperties();

        assertThat(properties.getMaxPerUser()).isZero();
        assertThat(properties.getAllowedMimeTypes()).isNull();
        assertThat(properties.getRateLimit()).isNotNull();
        assertThat(properties.getRateLimit().isEnabled()).isFalse();
        assertThat(properties.getRateLimit().getCapacity()).isZero();
        assertThat(properties.getRateLimit().getRefillDuration()).isNull();
        assertThat(properties.getRateLimit().getCache()).isNotNull();
        assertThat(properties.getRateLimit().getCache().getMaximumSize()).isZero();
        assertThat(properties.getRateLimit().getCache().getExpireAfterAccess()).isNull();
    }

    @Test
    @DisplayName("AvatarProperties binds default configuration values")
    void bindsDefaultConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test", Map.of(
                "avatar.max-per-user", "10",
                "avatar.allowed-mime-types", "image/jpeg,image/png,image/webp",
                "avatar.rate-limit.enabled", "true",
                "avatar.rate-limit.capacity", "5",
                "avatar.rate-limit.refill-duration", "1m",
                "avatar.rate-limit.cache.maximum-size", "1000",
                "avatar.rate-limit.cache.expire-after-access", "10m"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AvatarProperties properties = binder.bind("avatar", AvatarProperties.class).get();

        assertThat(properties.getMaxPerUser()).isEqualTo(10L);
        assertThat(properties.getAllowedMimeTypes()).containsExactly("image/jpeg", "image/png", "image/webp");
        assertThat(properties.getRateLimit().isEnabled()).isTrue();
        assertThat(properties.getRateLimit().getCapacity()).isEqualTo(5);
        assertThat(properties.getRateLimit().getRefillDuration()).isEqualTo(Duration.ofMinutes(1));
        assertThat(properties.getRateLimit().getCache().getMaximumSize()).isEqualTo(1000L);
        assertThat(properties.getRateLimit().getCache().getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("AvatarProperties binds configured values from environment/properties")
    void bindsConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test", Map.of(
                "avatar.max-per-user", "5",
                "avatar.allowed-mime-types", "image/png,image/webp",
                "avatar.rate-limit.enabled", "false",
                "avatar.rate-limit.capacity", "2",
                "avatar.rate-limit.refill-duration", "30s",
                "avatar.rate-limit.cache.maximum-size", "500",
                "avatar.rate-limit.cache.expire-after-access", "5m"
        )));

        Binder binder = new Binder(ConfigurationPropertySources.from(environment.getPropertySources()));
        AvatarProperties properties = binder.bind("avatar", AvatarProperties.class).get();

        assertThat(properties.getMaxPerUser()).isEqualTo(5L);
        assertThat(properties.getAllowedMimeTypes()).containsExactly("image/png", "image/webp");
        assertThat(properties.getRateLimit().isEnabled()).isFalse();
        assertThat(properties.getRateLimit().getCapacity()).isEqualTo(2);
        assertThat(properties.getRateLimit().getRefillDuration()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getRateLimit().getCache().getMaximumSize()).isEqualTo(500L);
        assertThat(properties.getRateLimit().getCache().getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(5));
    }
}
