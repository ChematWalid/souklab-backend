package com.project.souklab.security;

import java.util.List;

import com.project.souklab.config.AppProperties;
import com.project.souklab.config.RateLimitEndpointProperties;
import com.project.souklab.util.ServletResponseUtil;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class UserRateLimitFilterTest {
    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesAnalyticsEndpointUserOverrideAndNamespacedUserKey() throws Exception {
        AppProperties properties = new AppProperties();
        properties.getRateLimit().setUserEnabled(true);
        properties.getRateLimit().setUserCapacity(10);
        properties.getRateLimit().setUserRefillDuration(Duration.ofMinutes(1));
        RateLimitEndpointProperties endpoints = new RateLimitEndpointProperties();
        endpoints.getAnalyticsJobs().setEnabled(true);
        endpoints.getAnalyticsJobs().setUserCapacity(1);
        endpoints.getAnalyticsJobs().setUserRefillDuration(Duration.ofMinutes(1));

        Map<String, Bucket> buckets = new ConcurrentHashMap<>();
        AtomicReference<String> key = new AtomicReference<>();
        RateLimitBucketStore store = (bucketKey, capacity, refill) -> {
            key.set(bucketKey);
            return buckets.computeIfAbsent(bucketKey, ignored -> Bucket.builder()
                    .addLimit(io.github.bucket4j.Bandwidth.builder().capacity(capacity)
                            .refillGreedy(capacity, refill).build()).build());
        };
        UserRateLimitFilter filter = new UserRateLimitFilter(
                new ServletResponseUtil(new JsonMapper()), properties, store);
        filter.setEndpointProperties(endpoints);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@example.com", "n/a",
                        List.of(new SimpleGrantedAuthority(Permission.Analytics.ADMIN.authority()))));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/admin/analytics/rollups/jobs/rebuild");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(request, firstResponse, new MockFilterChain());
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        MockFilterChain rejectedChain = new MockFilterChain();
        filter.doFilter(request, secondResponse, rejectedChain);

        assertThat(key).hasValue("user:analytics:admin@example.com");
        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(rejectedChain.getRequest()).isNull();
    }
}
