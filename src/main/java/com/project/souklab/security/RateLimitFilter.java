package com.project.souklab.security;

import com.project.souklab.config.AppProperties;
import com.project.souklab.config.RateLimitEndpointProperties;
import com.project.souklab.config.RateLimitRule;
import com.project.souklab.config.OperationalMetrics;
import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.util.ServletResponseUtil;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ServletResponseUtil servletResponseUtil;
    private final AppProperties appProperties;
    private final RateLimitBucketStore bucketStore;
    private RateLimitEndpointProperties endpointProperties;
    private OperationalMetrics metrics = OperationalMetrics.noop();

    public RateLimitFilter(ServletResponseUtil servletResponseUtil, AppProperties appProperties) {
        this(servletResponseUtil, appProperties, RateLimitBucketStore.inMemory());
    }

    @Autowired
    public RateLimitFilter(ServletResponseUtil servletResponseUtil, AppProperties appProperties,
                           RateLimitBucketStore bucketStore) {
        this.servletResponseUtil = servletResponseUtil;
        this.appProperties = appProperties;
        this.bucketStore = bucketStore;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return appProperties.getRateLimit() == null || !appProperties.getRateLimit().isEnabled();
    }

    public Bucket resolveBucket(String ip) {
        AppProperties.RateLimit config = appProperties.getRateLimit();
        if (config == null) throw new IllegalStateException("Rate-limit configuration is unavailable");
        return bucketStore.resolve("api:" + ip, config.getCapacity(), config.getRefillDuration());
    }

    @Autowired(required = false)
    void setEndpointProperties(RateLimitEndpointProperties endpointProperties) { this.endpointProperties = endpointProperties; }

    @Autowired(required = false)
    void setOperationalMetrics(OperationalMetrics metrics) { this.metrics = metrics; }

    Bucket resolveBucket(HttpServletRequest request) {
        AppProperties.RateLimit global = appProperties.getRateLimit();
        RateLimitRule rule = selectRule(request);
        if (rule == null || !rule.isEnabled()) return resolveBucket(request.getRemoteAddr());
        return bucketStore.resolve("api:" + ruleName(request).value() + ":" + request.getRemoteAddr(),
                rule.getCapacity(), rule.getRefillDuration());
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

    private RateLimitScope.Endpoint ruleName(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.contains("/download")) return RateLimitScope.Endpoint.CSV_EXPORTS;
        if (isAnalyticsJobPath(path)) return RateLimitScope.Endpoint.ANALYTICS;
        if (path.startsWith("/api/v1/auth/")) return RateLimitScope.Endpoint.AUTHENTICATION;
        if (path.startsWith("/api/v1/admin/")) return RateLimitScope.Endpoint.ADMINISTRATION;
        return RateLimitScope.Endpoint.PUBLIC_API;
    }

    private boolean isAnalyticsJobPath(String path) {
        return path.contains("/analytics/jobs") || path.contains("/stats/jobs")
                || path.contains("/analytics/rollups/") || path.contains("/stats/rollups/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (endpointDisabled(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        Bucket bucket;
        try {
            bucket = resolveBucket(request);
        } catch (RuntimeException unavailable) {
            metrics.recordRateLimitRejection(ruleName(request));
            servletResponseUtil.writeResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error("Too many requests. Please try again later."));
            return;
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            metrics.recordRateLimitRejection(ruleName(request));
            servletResponseUtil.writeResponse(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error("Too many requests. Please try again later.")
            );
        }
    }

    private boolean endpointDisabled(HttpServletRequest request) {
        RateLimitRule rule = selectRule(request);
        return rule != null && !rule.isEnabled();
    }
}
