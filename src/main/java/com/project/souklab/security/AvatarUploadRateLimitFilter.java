package com.project.souklab.security;

import com.project.souklab.config.AvatarProperties;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.ApiErrorCode;
import com.project.souklab.util.ServletResponseUtil;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Dedicated rate-limiting filter for authenticated avatar upload requests (POST /api/v1/users/me/avatars).
 * Operates independently from the global API rate limiter and generic file-serving limiter with
 * configurable capacity and refill window from {@link AvatarProperties.RateLimitProperties}.
 */
public class AvatarUploadRateLimitFilter extends OncePerRequestFilter {

    private final ServletResponseUtil servletResponseUtil;
    private final AvatarProperties.RateLimitProperties rateLimitProperties;
    private final RateLimitBucketStore bucketStore;

    /**
     * Constructs a new AvatarUploadRateLimitFilter with injected response utility and avatar properties.
     *
     * @param servletResponseUtil utility to write standard ApiResponse error envelopes to the servlet response
     * @param avatarProperties configuration properties containing avatar upload rate limits
     */
    public AvatarUploadRateLimitFilter(ServletResponseUtil servletResponseUtil, AvatarProperties avatarProperties) {
        this(servletResponseUtil, avatarProperties, RateLimitBucketStore.inMemory());
    }

    @Autowired
    public AvatarUploadRateLimitFilter(ServletResponseUtil servletResponseUtil, AvatarProperties avatarProperties,
                                       RateLimitBucketStore bucketStore) {
        this.servletResponseUtil = servletResponseUtil;
        this.rateLimitProperties = avatarProperties != null ? avatarProperties.getRateLimit() : new AvatarProperties.RateLimitProperties();
        this.bucketStore = bucketStore;
    }

    /**
     * Determines whether the filter should be skipped for the given request.
     * Only POST requests targeting /api/v1/users/me/avatars are evaluated by this filter,
     * and only when rate limiting is enabled in configuration.
     *
     * @param request current HTTP request
     * @return true if rate limiting is disabled, HTTP method is not POST, or request URI does not match avatar upload path
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (rateLimitProperties != null && !rateLimitProperties.isEnabled()) {
            return true;
        }
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri == null || !AvatarUploadSizeFilter.AVATAR_UPLOAD_URI.equalsIgnoreCase(uri);
    }

    /**
     * Filters incoming avatar upload requests, consuming rate limit tokens and rejecting requests that exceed capacity with HTTP 429.
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

        String key = resolveKey(request);
        ConsumptionProbe probe;
        try {
            Bucket bucket = resolveBucket(key);
            probe = bucket.tryConsumeAndReturnRemaining(1);
        } catch (RuntimeException unavailable) {
            servletResponseUtil.writeResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error(ApiErrorCode.TOO_MANY_REQUESTS, "Too many requests. Please try again later."));
            return;
        }

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            long secondsToWait = Math.max(1L, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(secondsToWait));
            servletResponseUtil.writeResponse(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error(ApiErrorCode.TOO_MANY_REQUESTS, "Too many requests. Please try again later.")
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
            return "user:" + auth.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }

    /**
     * Resolves or creates a Bucket4j bucket for the specified client key.
     *
     * @param key unique bucket key
     * @return active Bucket instance
     */
    public Bucket resolveBucket(String key) {
        if (rateLimitProperties == null
                || rateLimitProperties.getCapacity() <= 0
                || rateLimitProperties.getRefillDuration() == null
                || rateLimitProperties.getRefillDuration().isZero()
                || rateLimitProperties.getRefillDuration().isNegative()) {
            throw new IllegalStateException("avatar.rate-limit must be configured before creating avatar upload buckets");
        }
        return bucketStore.resolve("avatar:" + key, rateLimitProperties.getCapacity(), rateLimitProperties.getRefillDuration());
    }
}
