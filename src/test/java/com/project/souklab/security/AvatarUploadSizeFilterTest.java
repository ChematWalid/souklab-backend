package com.project.souklab.security;

import org.mockito.ArgumentMatchers;

import tools.jackson.databind.json.JsonMapper;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.util.ServletResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AvatarUploadSizeFilter} verifying request-level Content-Length
 * early aborts, endpoint isolation, and chain pass-through behavior.
 */
@ExtendWith(MockitoExtension.class)
class AvatarUploadSizeFilterTest {

    @Mock
    private FilterChain filterChain;

    private StorageProperties storageProperties;
    private AvatarUploadSizeFilter filter;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        StorageProperties.ValidationProperties validation = new StorageProperties.ValidationProperties();
        validation.setMaxFileSize(DataSize.ofMegabytes(10));
        storageProperties.setValidation(validation);

        ServletResponseUtil servletResponseUtil = new ServletResponseUtil(new JsonMapper());
        filter = new AvatarUploadSizeFilter(storageProperties, servletResponseUtil);
    }

    /**
     * Verifies that an avatar upload exceeding 10MB (+ 1MB overhead) is rejected early with 413
     * and does not continue down the filter chain.
     */
    @Test
    @DisplayName("Avatar upload exceeding limit returns 413 and halts filter chain")
    void doFilter_whenAvatarUploadExceedsSize_writes413ResponseAndHaltsChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        request.setContent(new byte[0]);
        request.addHeader("Content-Length", String.valueOf(15 * 1024 * 1024));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertThat(response.getContentAsString()).contains("FILE_TOO_LARGE");
        verify(filterChain, never()).doFilter(request, response);
    }

    /**
     * Verifies that an avatar upload within the 10MB limit passes through to the filter chain.
     */
    @Test
    @DisplayName("Avatar upload within limit passes through to next filter")
    void doFilter_whenAvatarUploadWithinLimit_passesThroughChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        request.addHeader("Content-Length", String.valueOf(5 * 1024 * 1024));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    /**
     * Verifies that non-avatar endpoints are ignored by this filter and allowed to continue.
     */
    @Test
    @DisplayName("Upload to different endpoint is ignored by size filter")
    void doFilter_whenOtherEndpointExceedsSize_passesThroughChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/other/upload");
        request.addHeader("Content-Length", String.valueOf(50 * 1024 * 1024));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    /**
     * Verifies that non-POST requests to the avatar URI pass through to the filter chain.
     */
    @Test
    @DisplayName("GET request to avatar URI passes through to next filter")
    void doFilter_whenGetRequest_passesThroughChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me/avatars");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_handlesMissingValidationAndHeaderFallbacks() throws Exception {
        storageProperties.setValidation(null);
        filter.doFilter(new MockHttpServletRequest("POST", "/api/v1/users/me/avatars"),
                new MockHttpServletResponse(), filterChain);
        verify(filterChain).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());

        StorageProperties.ValidationProperties validation = new StorageProperties.ValidationProperties();
        storageProperties.setValidation(validation);
        MockHttpServletRequest invalidHeader = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        invalidHeader.addHeader("Content-Length", "not-a-number");
        filter = new AvatarUploadSizeFilter(storageProperties, new ServletResponseUtil(new JsonMapper()));
        filter.doFilter(invalidHeader, new MockHttpServletResponse(), filterChain);

        MockHttpServletRequest blankHeader = new MockHttpServletRequest("POST", "/api/v1/users/me/avatars");
        blankHeader.addHeader("Content-Length", " ");
        filter.doFilter(blankHeader, new MockHttpServletResponse(), filterChain);
    }
}
