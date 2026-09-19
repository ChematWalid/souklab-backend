package com.project.souklab.config;

import com.project.souklab.analytics.AnalyticsMetric;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OperationalMetricsTest {
    @Test
    void acceptsTypedMetricAndDependencyEnums() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OperationalMetrics metrics = new OperationalMetrics(registry);

        metrics.recordRequest(AnalyticsMetric.Operational.HttpMethod.GET,
                AnalyticsMetric.Operational.RequestOutcome.Status.SUCCESS);
        metrics.setDependencyAvailability(AnalyticsMetric.Operational.Dependency.REDIS, true);

        Map<String, Double> snapshot = metrics.snapshot(AnalyticsMetric.Operational.REQUEST_COUNTER_METRIC);

        assertThat(snapshot).containsEntry("operation=GET,outcome=2xx", 1.0);
        assertThat(registry.get("souklab.dependency.available")
                .tag("dependency", AnalyticsMetric.Operational.Dependency.REDIS.value())
                .gauge().value()).isEqualTo(1.0);
    }
}
