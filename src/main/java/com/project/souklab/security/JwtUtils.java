package com.project.souklab.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.project.souklab.config.AppProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {

    private static final String ERROR_UNEXPECTED_PRINCIPAL_TYPE_PREFIX = "Expected principal of type UserDetails, but found: ";
    public static final int AUTHORIZATION_SCHEMA_VERSION = 2;
    private static final String AUTHORIZATION_VERSION_CLAIM = "authz_version";

    private final AppProperties appProperties;
    private final Clock clock;

    @PostConstruct
    void validateConfiguration() {
        String secret = appProperties.getJwt().getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must contain at least 32 UTF-8 bytes");
        }
        if (appProperties.getJwt().getAccessTokenExpirationMs() == null
                || appProperties.getJwt().getAccessTokenExpirationMs() <= 0) {
            throw new IllegalStateException("app.jwt.access-token-expiration-ms must be positive");
        }
        if (appProperties.getJwt().getRefreshTokenExpirationMs() == null
                || appProperties.getJwt().getRefreshTokenExpirationMs() <= 0) {
            throw new IllegalStateException("app.jwt.refresh-token-expiration-ms must be positive");
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserDetails userPrincipal)) {
            throw new IllegalArgumentException(ERROR_UNEXPECTED_PRINCIPAL_TYPE_PREFIX
                    + (authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getSimpleName() : "null"));
        }
        return generateTokenFromUsername(userPrincipal.getUsername(), appProperties.getJwt().getAccessTokenExpirationMs());
    }

    public String generateAccessToken(String username) {
        return generateTokenFromUsername(username, appProperties.getJwt().getAccessTokenExpirationMs());
    }

    public String generateRefreshToken(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserDetails userPrincipal)) {
            throw new IllegalArgumentException(ERROR_UNEXPECTED_PRINCIPAL_TYPE_PREFIX
                    + (authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getSimpleName() : "null"));
        }
        return generateTokenFromUsername(userPrincipal.getUsername(), appProperties.getJwt().getRefreshTokenExpirationMs());
    }

    public String generateTokenFromUsername(String username, long expirationMs) {
        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim(AUTHORIZATION_VERSION_CLAIM, AUTHORIZATION_SCHEMA_VERSION)
                .setIssuedAt(Date.from(Instant.now(clock)))
                .setExpiration(Date.from(Instant.now(clock).plusMillis(expirationMs)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setClock(() -> Date.from(clock.instant()))
                .build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .setClock(() -> Date.from(clock.instant()))
                    .build()
                    .parseClaimsJws(authToken).getBody();
            Object version = claims.get(AUTHORIZATION_VERSION_CLAIM);
            if (!(version instanceof Number number) || number.intValue() != AUTHORIZATION_SCHEMA_VERSION) {
                log.warn("Rejected JWT with obsolete authorization schema version");
                return false;
            }
            return true;
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty");
        }
        return false;
    }
}
