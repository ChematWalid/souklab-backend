package com.project.souklab.security;

import com.project.souklab.dto.auth.JwtResponseDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Development/test fallback; production uses the Redis implementation when Redis is configured. */
@Component
@Primary
@ConditionalOnProperty(name = "app.rate-limit.backend", havingValue = "local", matchIfMissing = true)
public class InMemoryAuthorizationCodeStore implements AuthorizationCodeStore {
    private static final long TTL_SECONDS = 60;
    private record Entry(JwtResponseDTO response, Instant expiresAt) { }
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    @Override
    public String put(JwtResponseDTO response) {
        String code = UUID.randomUUID().toString();
        entries.put(code, new Entry(response, Instant.now().plusSeconds(TTL_SECONDS)));
        return code;
    }

    @Override
    public JwtResponseDTO consume(String code) {
        if (code == null || code.isBlank()) return null;
        Entry entry = entries.remove(code);
        return entry != null && Instant.now().isBefore(entry.expiresAt()) ? entry.response() : null;
    }
}
