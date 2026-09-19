package com.project.souklab.security;

import com.project.souklab.analytics.AnalyticsMetric;
import java.time.Duration;

import com.project.souklab.config.AppProperties;
import com.project.souklab.config.OperationalMetrics;
import com.project.souklab.config.RateLimitEndpointProperties;
import com.project.souklab.config.RateLimitRule;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.util.ServletResponseUtil;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Authenticated-user bucket layered after JWT authentication. */
@Component
public class UserRateLimitFilter extends OncePerRequestFilter {
    private final ServletResponseUtil responses;
    private final AppProperties properties;
    private final RateLimitBucketStore store;
    private RateLimitEndpointProperties endpointProperties;
    private OperationalMetrics metrics = OperationalMetrics.noop();

    public UserRateLimitFilter(ServletResponseUtil responses, AppProperties properties, RateLimitBucketStore store) {
        this.responses = responses;
        this.properties = properties;
        this.store = store;
    }

    @Autowired
    public UserRateLimitFilter(ObjectProvider<ServletResponseUtil> responses,
                               AppProperties properties, ObjectProvider<RateLimitBucketStore> store) {
        this(responses.getIfAvailable(), properties,
                store.getIfAvailable(RateLimitBucketStore::inMemory));
    }

    @Autowired(required = false)
    void setOperationalMetrics(OperationalMetrics metrics) { this.metrics = metrics; }

    @Autowired(required = false)
    void setEndpointProperties(RateLimitEndpointProperties endpointProperties) {
        this.endpointProperties = endpointProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return properties.getRateLimit() == null
                || !properties.getRateLimit().isUserEnabled()
                || SecurityContextHolder.getContext().getAuthentication() == null;
    }

    public Bucket resolveBucket(String username) {
        AppProperties.RateLimit limit = properties.getRateLimit();
        if (limit == null) throw new IllegalStateException("Rate-limit configuration is unavailable");
        return store.resolve("user:" + username, limit.getUserCapacity(), limit.getUserRefillDuration());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) { chain.doFilter(request, response); return; }
        try {
            RateLimitRule rule = selectRule(request);
            if (rule != null && !rule.isEnabled()) {
                chain.doFilter(request, response);
                return;
            }
            AppProperties.RateLimit global = properties.getRateLimit();
            int capacity = rule != null && rule.getUserCapacity() > 0
                    ? rule.getUserCapacity() : global.getUserCapacity();
            Duration refill = rule != null && rule.getUserRefillDuration() != null
                    ? rule.getUserRefillDuration() : global.getUserRefillDuration();
            String scope = ruleName(request);
            Bucket bucket = store.resolve("user:" + scope + ":" + authentication.getName(), capacity, refill);
            if (!bucket.tryConsume(1)) {
                metrics.recordRateLimitRejection(AnalyticsMetric.Operational.Scope.USER.value());
                reject(response);
                return;
            }
        } catch (RuntimeException unavailable) {
            metrics.recordRateLimitRejection(AnalyticsMetric.Operational.Scope.USER.value());
            reject(response);
            return;
        }
        chain.doFilter(request, response);
    }

    private RateLimitRule selectRule(HttpServletRequest request) {
        if (endpointProperties == null) return null;
        String path = request.getRequestURI();
        if (path.contains("/integrations/chargily/webhook")) return endpointProperties.getChargilyWebhook();
        if (isAnalyticsJobPath(path)) {
            if (path.endsWith("/download")) return endpointProperties.getCsvExports();
            if (path.endsWith("/result")) return endpointProperties.getAnalyticsResults();
            return endpointProperties.getAnalyticsJobs();
        }
        if (path.startsWith("/api/v1/auth/")) return endpointProperties.getAuthentication();
        if (path.startsWith("/api/v1/admin/")) return endpointProperties.getAdminApi();
        return endpointProperties.getPublicApi();
    }

    private String ruleName(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.contains("/download")) return "csv";
        if (isAnalyticsJobPath(path)) return "analytics";
        if (path.startsWith("/api/v1/auth/")) return "auth";
        if (path.startsWith("/api/v1/admin/")) return "admin";
        return "public";
    }

    private boolean isAnalyticsJobPath(String path) {
        return path.contains("/analytics/jobs") || path.contains("/stats/jobs")
                || path.contains("/analytics/rollups/") || path.contains("/stats/rollups/");
    }

    private void reject(HttpServletResponse response) throws IOException {
        if (responses != null) {
            responses.writeResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error("Too many requests. Please try again later."));
        } else {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. Please try again later.");
        }
    }
}
