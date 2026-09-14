package com.project.souklab.filestorage.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.controller.FileServingController;
import com.project.souklab.util.ServletResponseUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Dedicated rate-limiting filter for file-serving and upload endpoints (/api/v1/files/**).
 * Operates independently from the global API rate limiter with configurable capacity and refill window.
 */
@Component
public class FileRateLimitFilter extends OncePerRequestFilter {

    private static final int DEFAULT_CAPACITY = 120;
    private static final Duration DEFAULT_REFILL_DURATION = Duration.ofMinutes(1);
    private static final long TOKENS_PER_REQUEST = 1L;
    private static final String USER_KEY_PREFIX = "user:";
    private static final String IP_KEY_PREFIX = "ip:";
    private static final String ERROR_TOO_MANY_REQUESTS = "Too many requests. Please try again later.";

    private final ServletResponseUtil servletResponseUtil;
    private final StorageProperties.RateLimitProperties rateLimitProperties;
    private final Cache<String, Bucket> cache;

    /**
     * Constructs a new FileRateLimitFilter with injected response utility and storage properties.
     *
     * @param servletResponseUtil utility to write standard ApiResponse error envelopes to the servlet response
     * @param properties configuration properties containing file rate-limiting limits
     */
    public FileRateLimitFilter(ServletResponseUtil servletResponseUtil, StorageProperties properties) {
        this.servletResponseUtil = servletResponseUtil;
        this.rateLimitProperties = properties != null ? properties.getRateLimit() : new StorageProperties.RateLimitProperties();
        StorageProperties.RateLimitProperties.CacheProperties cacheConfig = this.rateLimitProperties.getCache();
        this.cache = Caffeine.newBuilder()
                .maximumSize(cacheConfig.getMaximumSize())
                .expireAfterAccess(cacheConfig.getExpireAfterAccess())
                .build();
    }

    /**
     * Determines whether the filter should be skipped for the given request.
     * Only requests targeting /api/v1/files/** are evaluated by this filter.
     *
     * @param request current HTTP request
     * @return true if request URI does not start with /api/v1/files, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith(FileServingController.BASE_PATH);
    }

    /**
     * Filters incoming file requests, consuming rate limit tokens and rejecting requests that exceed capacity with HTTP 429.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain servlet filter chain
     * @throws ServletException in case of servlet processing errors
     * @throws IOException in case of I/O errors
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (rateLimitProperties != null && !rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        Bucket bucket = resolveBucket(key);

        if (bucket.tryConsume(TOKENS_PER_REQUEST)) {
            filterChain.doFilter(request, response);
        } else {
            servletResponseUtil.writeResponse(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error(ERROR_TOO_MANY_REQUESTS)
            );
        }
    }

    /**
     * Resolves the rate-limiting key for the request.
     * Authenticated users are keyed by username; unauthenticated callers are keyed by remote IP.
     *
     * @param request current HTTP request
     * @return unique bucket key
     */
    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return USER_KEY_PREFIX + auth.getName();
        }
        return IP_KEY_PREFIX + request.getRemoteAddr();
    }

    /**
     * Resolves or creates a Bucket4j bucket for the specified client key.
     *
     * @param key unique bucket key
     * @return active Bucket instance
     */
    public Bucket resolveBucket(String key) {
        return cache.get(key, k -> createNewBucket());
    }

    /**
     * Creates a new Bucket instance initialized from the configured rateLimitProperties.
     *
     * @return new configured Bucket
     */
    private Bucket createNewBucket() {
        int capacity = (rateLimitProperties != null && rateLimitProperties.getCapacity() > 0)
                ? rateLimitProperties.getCapacity()
                : DEFAULT_CAPACITY;
        Duration refillDuration = (rateLimitProperties != null && rateLimitProperties.getRefillDuration() != null)
                ? rateLimitProperties.getRefillDuration()
                : DEFAULT_REFILL_DURATION;

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillDuration)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
