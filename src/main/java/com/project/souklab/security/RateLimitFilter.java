package com.project.souklab.security;

import com.project.souklab.config.AppProperties;
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
        return !appProperties.getRateLimit().isEnabled();
    }

    public Bucket resolveBucket(String ip) {
        AppProperties.RateLimit config = appProperties.getRateLimit();
        return bucketStore.resolve("api:" + ip, config.getCapacity(), config.getRefillDuration());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        Bucket bucket;
        try {
            bucket = resolveBucket(ip);
        } catch (RuntimeException unavailable) {
            servletResponseUtil.writeResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error("Too many requests. Please try again later."));
            return;
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            servletResponseUtil.writeResponse(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error("Too many requests. Please try again later.")
            );
        }
    }
}
