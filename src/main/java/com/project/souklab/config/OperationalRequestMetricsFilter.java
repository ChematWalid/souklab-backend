package com.project.souklab.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Records bounded request outcome counters for the operational dashboard. */
@Component
public class OperationalRequestMetricsFilter extends OncePerRequestFilter {
    private final OperationalMetrics metrics;

    public OperationalRequestMetricsFilter(ObjectProvider<OperationalMetrics> metrics) {
        this.metrics = metrics.getIfAvailable(OperationalMetrics::noop);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String outcome = "error";
        try {
            filterChain.doFilter(request, response);
            outcome = statusClass(response.getStatus());
        } catch (IOException | ServletException | RuntimeException exception) {
            metrics.recordRequest(request.getMethod(), outcome);
            throw exception;
        }
        metrics.recordRequest(request.getMethod(), outcome);
    }

    private String statusClass(int status) {
        if (status >= 500) return "5xx";
        if (status >= 400) return "4xx";
        if (status >= 300) return "3xx";
        return "2xx";
    }
}
