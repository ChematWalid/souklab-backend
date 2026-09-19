package com.project.souklab.config;

import java.time.Duration;
import org.springframework.core.env.Environment;

import com.project.souklab.security.RateLimitBucketStore;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Production rate-limit state. Redis is authoritative; no bucket state is cached in the JVM. */
@Configuration
public class RateLimitRedisConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "app.rate-limit.backend", havingValue = "redis")
    RedisClient rateLimitRedisClient(Environment environment) {
        RedisURI.Builder builder = RedisURI.builder()
                .withHost(environment.getRequiredProperty("app.rate-limit.redis.host"))
                .withPort(Integer.parseInt(environment.getRequiredProperty("app.rate-limit.redis.port")))
                .withDatabase(Integer.parseInt(environment.getProperty("app.rate-limit.redis.database", "0")))
                .withTimeout(Duration.ofMillis(Long.parseLong(
                        environment.getProperty("app.rate-limit.redis.connection-timeout", "2000"))));
        String password = environment.getProperty("app.rate-limit.redis.password");
        if (password != null && !password.isBlank()) {
            builder.withPassword(password.toCharArray());
        }
        RedisClient client = RedisClient.create(builder.build());
        client.setOptions(ClientOptions.builder().timeoutOptions(TimeoutOptions.enabled(Duration.ofMillis(
                Long.parseLong(environment.getProperty("app.rate-limit.redis.command-timeout", "1000"))))).build());
        return client;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "app.rate-limit.backend", havingValue = "redis")
    RateLimitBucketStore redisRateLimitBucketStore(RedisClient client,
                                                   Environment environment) {
        StatefulRedisConnection<String, byte[]> connection = client.connect(
                io.lettuce.core.codec.RedisCodec.of(io.lettuce.core.codec.StringCodec.UTF8,
                        io.lettuce.core.codec.ByteArrayCodec.INSTANCE));
        LettuceBasedProxyManager<String> manager = LettuceBasedProxyManager.builderFor(connection).build();
        return new RedisBucketStore(manager, connection,
                environment.getProperty("app.rate-limit.key-prefix", "souklab:local"));
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.backend", havingValue = "local", matchIfMissing = true)
    RateLimitBucketStore localRateLimitBucketStore() {
        return RateLimitBucketStore.inMemory();
    }

    private static final class RedisBucketStore implements RateLimitBucketStore, AutoCloseable {
        private final LettuceBasedProxyManager<String> manager;
        private final StatefulRedisConnection<String, byte[]> connection;
        private final String keyPrefix;

        private RedisBucketStore(LettuceBasedProxyManager<String> manager,
                                 StatefulRedisConnection<String, byte[]> connection,
                                 String keyPrefix) {
            this.manager = manager;
            this.connection = connection;
            this.keyPrefix = keyPrefix;
        }

        @Override
        public io.github.bucket4j.Bucket resolve(String key, long capacity,
                                                  Duration refillDuration) {
            return manager.builder().build(keyPrefix + ":" + key,
                    RateLimitBucketStore.configuration(capacity, refillDuration));
        }

        @Override
        public void close() {
            connection.close();
        }
    }
}
