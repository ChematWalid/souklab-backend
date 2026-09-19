package com.project.souklab.integration.phase10;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies that independent application instances share authenticated rate-limit state. */
@EnabledIfEnvironmentVariable(named = "PHASE10_REDIS_INTEGRATION", matches = "true")
class Phase10RedisRateLimitIntegrationTest {
    @Test
    void independentManagersConsumeOneSharedUserOrIpBucket() {
        try (GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort())) {
            redis.start();
            RedisURI uri = RedisURI.builder().withHost("127.0.0.1")
                    .withPort(redis.getMappedPort(6379)).build();
            RedisClient client = RedisClient.create(uri);
            StatefulRedisConnection<String, byte[]> firstConnection = client.connect(codec());
            StatefulRedisConnection<String, byte[]> secondConnection = client.connect(codec());
            LettuceBasedProxyManager<String> first = LettuceBasedProxyManager.builderFor(firstConnection).build();
            LettuceBasedProxyManager<String> second = LettuceBasedProxyManager.builderFor(secondConnection).build();
            String key = "souklab:phase10:shared:" + UUID.randomUUID();
            BucketConfiguration configuration = BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder().capacity(1)
                            .refillGreedy(1, Duration.ofMinutes(1)).build())
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
    }

    private static RedisCodec<String, byte[]> codec() {
        return RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
    }
}
