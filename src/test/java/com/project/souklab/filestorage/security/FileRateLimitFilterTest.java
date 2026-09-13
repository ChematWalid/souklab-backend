package com.project.souklab.filestorage.security;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.util.ServletResponseUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Isolated unit tests for FileRateLimitFilter.
 * Verifies request path targeting, capacity consumption, HTTP 429 rejection with standard ApiResponse envelope,
 * disabled toggle bypass, and client bucket isolation without requiring real storage or Spring context.
 */
class FileRateLimitFilterTest {

    private ServletResponseUtil servletResponseUtil;
    private StorageProperties properties;
    private FileRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        servletResponseUtil = new ServletResponseUtil(new JsonMapper());
        properties = new StorageProperties();
        properties.getRateLimit().setEnabled(true);
        properties.getRateLimit().setCapacity(2);
        properties.getRateLimit().setRefillDuration(Duration.ofMinutes(1));

        filter = new FileRateLimitFilter(servletResponseUtil, properties);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Verifies that requests targeting non-file routes bypass the rate-limiting filter.
     */
    @Test
    @DisplayName("Requests to non-file paths bypass the filter without consuming tokens")
    void nonFileRequest_bypassesFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /**
     * Verifies that requests under the configured capacity are allowed through to the filter chain.
     */
    @Test
    @DisplayName("Requests within capacity are permitted through the filter chain")
    void requestsUnderCapacity_arePermitted() throws Exception {
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/v1/files/sample.jpg");
        request1.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        MockFilterChain filterChain1 = new MockFilterChain();

        filter.doFilter(request1, response1, filterChain1);
        assertThat(filterChain1.getRequest()).isNotNull();
        assertThat(response1.getStatus()).isEqualTo(200);

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/v1/files/sample.jpg");
        request2.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        MockFilterChain filterChain2 = new MockFilterChain();

        filter.doFilter(request2, response2, filterChain2);
        assertThat(filterChain2.getRequest()).isNotNull();
        assertThat(response2.getStatus()).isEqualTo(200);
    }

    /**
     * Verifies that requests exceeding capacity are rejected with HTTP 429 and the standard ApiResponse envelope.
     */
    @Test
    @DisplayName("Requests exceeding capacity are rejected with HTTP 429 and standard error envelope")
    void requestsOverCapacity_areRejectedWith429() throws Exception {
        String clientIp = "10.0.0.50";

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/files/image.png");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        MockHttpServletRequest rejectedReq = new MockHttpServletRequest("GET", "/api/v1/files/image.png");
        rejectedReq.setRemoteAddr(clientIp);
        MockHttpServletResponse rejectedRes = new MockHttpServletResponse();
        MockFilterChain rejectedChain = new MockFilterChain();

        filter.doFilter(rejectedReq, rejectedRes, rejectedChain);

        assertThat(rejectedChain.getRequest()).isNull();
        assertThat(rejectedRes.getStatus()).isEqualTo(429);
        assertThat(rejectedRes.getContentType()).contains("application/json");

        String responseJson = rejectedRes.getContentAsString();
        assertThat(responseJson).contains("\"success\":false");
        assertThat(responseJson).contains("\"code\":429");
        assertThat(responseJson).contains("Too many requests. Please try again later.");
    }

    /**
     * Verifies that when rate limiting is disabled via configuration, requests pass through unrestricted.
     */
    @Test
    @DisplayName("Disabled rate limiter permits requests even beyond capacity")
    void disabledRateLimiter_permitsAllRequests() throws Exception {
        properties.getRateLimit().setEnabled(false);

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/files/doc.pdf");
            req.setRemoteAddr("172.16.0.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(req, res, chain);

            assertThat(chain.getRequest()).isNotNull();
            assertThat(res.getStatus()).isEqualTo(200);
        }
    }

    /**
     * Verifies that distinct authenticated users have independent token buckets.
     */
    @Test
    @DisplayName("Authenticated users have independent rate-limiting buckets")
    void authenticatedUsers_haveIndependentBuckets() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("userA@souklab.dz", "pass", List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/files/avatar.jpg");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        MockHttpServletRequest reqExceeded = new MockHttpServletRequest("GET", "/api/v1/files/avatar.jpg");
        MockHttpServletResponse resExceeded = new MockHttpServletResponse();
        filter.doFilter(reqExceeded, resExceeded, new MockFilterChain());
        assertThat(resExceeded.getStatus()).isEqualTo(429);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("userB@souklab.dz", "pass", List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")))
        );

        MockHttpServletRequest reqUserB = new MockHttpServletRequest("GET", "/api/v1/files/avatar.jpg");
        MockHttpServletResponse resUserB = new MockHttpServletResponse();
        MockFilterChain chainUserB = new MockFilterChain();
        filter.doFilter(reqUserB, resUserB, chainUserB);

        assertThat(chainUserB.getRequest()).isNotNull();
        assertThat(resUserB.getStatus()).isEqualTo(200);

        SecurityContextHolder.clearContext();
        MockHttpServletRequest reqAnon = new MockHttpServletRequest("GET", "/api/v1/files/avatar.jpg");
        MockHttpServletResponse resAnon = new MockHttpServletResponse();
        MockFilterChain chainAnon = new MockFilterChain();
        filter.doFilter(reqAnon, resAnon, chainAnon);

        assertThat(chainAnon.getRequest()).isNotNull();
        assertThat(resAnon.getStatus()).isEqualTo(200);
    }
}
