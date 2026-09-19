package com.project.souklab.security;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.common.ApiErrorCode;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.util.ServletResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter providing an early request-level multipart payload size guard for avatar uploads.
 * Inspects incoming HTTP Content-Length before the servlet container or multipart resolver buffers
 * up to the 100MB global servlet limit, failing fast with HTTP 413 if the payload exceeds the
 * configured avatar storage limit (with a 1MB multipart boundary/header tolerance).
 */
@RequiredArgsConstructor
@Slf4j
public class AvatarUploadSizeFilter extends OncePerRequestFilter {

    /**
     * Target URI path for avatar upload requests.
     */
    public static final String AVATAR_UPLOAD_URI = "/api/v1/users/me/avatars";

    /**
     * Permitted byte tolerance for multipart/form-data boundary markers, Content-Disposition headers,
     * and MIME headers surrounding the file stream (1 megabyte).
     */
    private static final long MULTIPART_HEADER_OVERHEAD_BYTES = 1024L * 1024L;

    private final StorageProperties storageProperties;
    private final ServletResponseUtil servletResponseUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod()) && AVATAR_UPLOAD_URI.equalsIgnoreCase(request.getRequestURI())) {
            if (storageProperties.getValidation() != null && storageProperties.getValidation().getMaxFileSize() != null) {
                long maxFileBytes = storageProperties.getValidation().getMaxFileSize().toBytes();
                long maxAllowedRequestBytes = maxFileBytes + MULTIPART_HEADER_OVERHEAD_BYTES;
                long contentLength = getContentLength(request);

                if (contentLength > maxAllowedRequestBytes) {
                    log.warn("Avatar upload rejected early: Content-Length {} exceeds request limit {} bytes",
                            contentLength, maxAllowedRequestBytes);

                    ApiResponse<Void> apiResponse = ApiResponse.error(
                            ApiErrorCode.FILE_TOO_LARGE,
                            String.format("Avatar upload exceeds maximum permitted file size of %d bytes", maxFileBytes)
                    );
                    servletResponseUtil.writeResponse(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, apiResponse);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Resolves the request content length from getContentLengthLong or the Content-Length header fallback.
     *
     * @param request the incoming HTTP servlet request
     * @return content length in bytes, or -1 if unparseable or absent
     */
    private long getContentLength(HttpServletRequest request) {
        long len = request.getContentLengthLong();
        if (len <= 0) {
            String header = request.getHeader("Content-Length");
            if (header != null && !header.isBlank()) {
                try {
                    len = Long.parseLong(header.trim());
                } catch (NumberFormatException ignored) {
                    return -1;
                }
            }
        }
        return len;
    }
}
