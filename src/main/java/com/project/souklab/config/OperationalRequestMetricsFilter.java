package com.project.souklab.config;

import com.project.souklab.analytics.AnalyticsMetric;
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
        AnalyticsMetric.Operational.RequestOutcome.Status.Key outcome = AnalyticsMetric.Operational.RequestOutcome.Status.Error.VALUE;
        try {
            filterChain.doFilter(request, response);
            outcome = AnalyticsMetric.Operational.RequestOutcome.Status.fromStatus(response.getStatus());
        } catch (IOException | ServletException | RuntimeException exception) {
            metrics.recordRequest(AnalyticsMetric.Operational.HttpMethod.fromValue(request.getMethod()), outcome);
            throw exception;
        }
        metrics.recordRequest(AnalyticsMetric.Operational.HttpMethod.fromValue(request.getMethod()), outcome);
    }
}
