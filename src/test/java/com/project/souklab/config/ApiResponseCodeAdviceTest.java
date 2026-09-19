package com.project.souklab.config;

import org.springframework.http.server.ServerHttpResponse;

import com.project.souklab.dto.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.http.server.ServletServerHttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ApiResponseCodeAdviceTest {

    @Test
    void copiesServletStatusIntoApiResponse() {
        ApiResponseCodeAdvice advice = new ApiResponseCodeAdvice();
        ApiResponse<String> body = ApiResponse.success("ok");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        servletResponse.setStatus(202);

        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null, null,
                new ServletServerHttpResponse(servletResponse));

        assertThat(result).isSameAs(body);
        assertThat(body.getCode()).isEqualTo(202);
    }

    @Test
    void leavesNonEnvelopeAndZeroStatusUntouched() {
        ApiResponseCodeAdvice advice = new ApiResponseCodeAdvice();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        servletResponse.setStatus(0);
        ApiResponse<String> body = ApiResponse.success("ok");

        advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null, null,
                new ServletServerHttpResponse(servletResponse));
        assertThat(body.getCode()).isZero();
        assertThat(advice.beforeBodyWrite("plain", null, MediaType.APPLICATION_JSON, null, null,
                mock(ServerHttpResponse.class))).isEqualTo("plain");
    }
}
