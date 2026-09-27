package com.project.souklab.security;

import com.project.souklab.dto.auth.JwtResponseDTO;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.StringCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.UUID;

/** Redis-backed atomic OAuth authorization-code store with a 60-second TTL. */
@Component
@Primary
@ConditionalOnProperty(name = "app.rate-limit.backend", havingValue = "redis")
public class RedisAuthorizationCodeStore implements AuthorizationCodeStore {
    private static final Duration TTL = Duration.ofSeconds(60);
    private final StatefulRedisConnection<String, String> connection;
    private final JsonMapper mapper;

    public RedisAuthorizationCodeStore(RedisClient client, JsonMapper mapper) {
        this.connection = client.connect(StringCodec.UTF8);
        this.mapper = mapper;
    }

    @Override
    public String put(JwtResponseDTO response) {
        try {
            String code = UUID.randomUUID().toString();
            connection.sync().setex("souklab:oauth:code:" + code, TTL.toSeconds(), mapper.writeValueAsString(response));
            return code;
        } catch (Exception ex) {
            throw new IllegalStateException("Could not create OAuth authorization code", ex);
        }
    }

    @Override
    public JwtResponseDTO consume(String code) {
        if (code == null || code.isBlank()) return null;
        try {
            String value = connection.sync().getdel("souklab:oauth:code:" + code);
            return value == null ? null : mapper.readValue(value, JwtResponseDTO.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not consume OAuth authorization code", ex);
        }
    }
}
