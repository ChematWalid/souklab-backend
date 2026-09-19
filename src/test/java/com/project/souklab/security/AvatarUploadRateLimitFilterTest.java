package com.project.souklab.security;

import com.project.souklab.config.AvatarProperties;
import com.project.souklab.util.ServletResponseUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Isolated unit tests for {@link AvatarUploadRateLimitFilter}.
 * Verifies exact POST endpoint targeting, capacity consumption, HTTP 429 rejection with standard
 * ApiResponse envelope, disabled toggle bypass, client bucket isolation, and non-POST bypass behavior.
 */
class AvatarUploadRateLimitFilterTest {

    private ServletResponseUtil servletResponseUtil;
    private AvatarProperties avatarProperties;
    private AvatarUploadRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        servletResponseUtil = new ServletResponseUtil(new JsonMapper());
        avatarProperties = new AvatarProperties();
        avatarProperties.getRateLimit().setEnabled(true);
        avatarProperties.getRateLimit().setCapacity(2);
        avatarProperties.getRateLimit().setRefillDuration(Duration.ofMinutes(1));

        filter = new AvatarUploadRateLimitFilter(servletResponseUtil, avatarProperties);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Verifies that requests within capacity are permitted through the filter chain.
     */
    @Test
    @DisplayName("Requests within capacity are permitted through the filter chain")
    void requestsUnderCapacity_arePermitted() throws Exception {
        MockHttpServletRequest request1 = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        request1.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        MockFilterChain filterChain1 = new MockFilterChain();

        filter.doFilter(request1, response1, filterChain1);
        assertThat(filterChain1.getRequest()).isNotNull();
        assertThat(response1.getStatus()).isEqualTo(200);

        MockHttpServletRequest request2 = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
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
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        MockHttpServletRequest rejectedReq = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
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
        avatarProperties.getRateLimit().setEnabled(false);

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
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
                new UsernamePasswordAuthenticationToken("userA@souklab.dz", "pass", List.of(Permission.Profile.READ))
        );

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        MockHttpServletRequest reqExceeded = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        MockHttpServletResponse resExceeded = new MockHttpServletResponse();
        filter.doFilter(reqExceeded, resExceeded, new MockFilterChain());
        assertThat(resExceeded.getStatus()).isEqualTo(429);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("userB@souklab.dz", "pass", List.of(Permission.Artisan.CONTENT))
        );

        MockHttpServletRequest reqUserB = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        MockHttpServletResponse resUserB = new MockHttpServletResponse();
        MockFilterChain chainUserB = new MockFilterChain();
        filter.doFilter(reqUserB, resUserB, chainUserB);

        assertThat(chainUserB.getRequest()).isNotNull();
        assertThat(resUserB.getStatus()).isEqualTo(200);
    }

    /**
     * Verifies that unauthenticated callers fall back to IP-based bucket isolation.
     */
    @Test
    @DisplayName("Unauthenticated requests fall back to remote IP keying")
    void unauthenticatedRequests_fallBackToIpKeying() throws Exception {
        SecurityContextHolder.clearContext();
        String ip1 = "192.168.1.10";
        String ip2 = "192.168.1.20";

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
            req.setRemoteAddr(ip1);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        MockHttpServletRequest reqExceeded = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        reqExceeded.setRemoteAddr(ip1);
        MockHttpServletResponse resExceeded = new MockHttpServletResponse();
        filter.doFilter(reqExceeded, resExceeded, new MockFilterChain());
        assertThat(resExceeded.getStatus()).isEqualTo(429);

        MockHttpServletRequest reqOtherIp = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        reqOtherIp.setRemoteAddr(ip2);
        MockHttpServletResponse resOtherIp = new MockHttpServletResponse();
        MockFilterChain chainOtherIp = new MockFilterChain();
        filter.doFilter(reqOtherIp, resOtherIp, chainOtherIp);

        assertThat(chainOtherIp.getRequest()).isNotNull();
        assertThat(resOtherIp.getStatus()).isEqualTo(200);
    }

    /**
     * Verifies that non-POST requests targeting the avatar resource family bypass this filter.
     */
    @Test
    @DisplayName("DELETE, PUT, and GET requests to avatar endpoints bypass the filter without consuming tokens")
    void nonPostRequests_bypassFilter() throws Exception {
        String clientIp = "10.0.0.99";

        MockHttpServletRequest getReq = new MockHttpServletRequest("GET", "/api/v1/users/me/avatars");
        getReq.setRemoteAddr(clientIp);
        MockHttpServletResponse getRes = new MockHttpServletResponse();
        MockFilterChain getChain = new MockFilterChain();
        filter.doFilter(getReq, getRes, getChain);
        assertThat(getChain.getRequest()).isNotNull();
        assertThat(getRes.getStatus()).isEqualTo(200);

        MockHttpServletRequest deleteReq = new MockHttpServletRequest("DELETE", "/api/v1/users/me/avatars/avatar-123");
        deleteReq.setRemoteAddr(clientIp);
        MockHttpServletResponse deleteRes = new MockHttpServletResponse();
        MockFilterChain deleteChain = new MockFilterChain();
        filter.doFilter(deleteReq, deleteRes, deleteChain);
        assertThat(deleteChain.getRequest()).isNotNull();
        assertThat(deleteRes.getStatus()).isEqualTo(200);

        MockHttpServletRequest putReq = new MockHttpServletRequest("PUT", "/api/v1/users/me/avatars/avatar-123/activate");
        putReq.setRemoteAddr(clientIp);
        MockHttpServletResponse putRes = new MockHttpServletResponse();
        MockFilterChain putChain = new MockFilterChain();
        filter.doFilter(putReq, putRes, putChain);
        assertThat(putChain.getRequest()).isNotNull();
        assertThat(putRes.getStatus()).isEqualTo(200);
    }

    /**
     * Verifies that POST requests targeting other endpoints bypass the filter.
     */
    @Test
    @DisplayName("Requests targeting other endpoints bypass the filter")
    void requestsToOtherPaths_bypassFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldNotFilterHandlesNullUriAndAnonymousPrincipal() {
        MockHttpServletRequest nullUri = new MockHttpServletRequest("POST", null);
        assertThat(filter.shouldNotFilter(nullUri)).isTrue();

        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymous",
                        List.of(SecurityAuthority.Anonymous.ROLE)));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        request.setRemoteAddr("127.0.0.1");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void invalidRateLimitConfigurationFailsWhenBucketIsCreated() {
        AvatarProperties invalid = new AvatarProperties();
        AvatarUploadRateLimitFilter invalidFilter = new AvatarUploadRateLimitFilter(servletResponseUtil, invalid);

        assertThatThrownBy(() -> invalidFilter.resolveBucket("invalid"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("avatar.rate-limit");
    }
}
