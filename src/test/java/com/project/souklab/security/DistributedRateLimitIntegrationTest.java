package com.project.souklab.security;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies that independent application managers consume one shared Redis bucket. */
@EnabledIfEnvironmentVariable(named = "APP_RATE_LIMIT_BACKEND", matches = "redis")
class DistributedRateLimitIntegrationTest {

    @Test
    void independentManagersShareOneBucket() {
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String password = System.getenv().getOrDefault("REDIS_PASSWORD", "");
        RedisURI.Builder uri = RedisURI.builder().withHost(host).withPort(port);
        if (!password.isBlank()) {
            uri.withPassword(password.toCharArray());
        }

        RedisClient client = RedisClient.create(uri.build());
        StatefulRedisConnection<String, byte[]> firstConnection = client.connect(codec());
        StatefulRedisConnection<String, byte[]> secondConnection = client.connect(codec());
        LettuceBasedProxyManager<String> first = LettuceBasedProxyManager.builderFor(firstConnection).build();
        LettuceBasedProxyManager<String> second = LettuceBasedProxyManager.builderFor(secondConnection).build();
        String key = "souklab:integration:shared:" + UUID.randomUUID();
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder().capacity(1).refillGreedy(1, Duration.ofMinutes(1)).build())
                .build();

        try {
            assertThat(first.builder().build(key, configuration).tryConsume(1)).isTrue();
            assertThat(second.builder().build(key, configuration).tryConsume(1)).isFalse();
        } finally {
            first.removeProxy(key);
            firstConnection.close();
            secondConnection.close();
            client.shutdown();
        }
    }

    private static RedisCodec<String, byte[]> codec() {
        return RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
    }
}
