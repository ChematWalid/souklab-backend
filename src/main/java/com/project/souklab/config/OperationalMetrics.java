package com.project.souklab.config;

import com.project.souklab.analytics.AnalyticsMetric;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Application-level counters used by the production dashboards.
 * Tag values are fixed operation/outcome names so request traffic cannot create
 * unbounded metric cardinality.
 */
@Component
public class OperationalMetrics {

    private static final OperationalMetrics NOOP = new OperationalMetrics(new SimpleMeterRegistry());

    private final MeterRegistry registry;
    private final ConcurrentHashMap<String, AtomicInteger> dependencyAvailability = new ConcurrentHashMap<>();

    public OperationalMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns a no-op recorder for compatibility constructors used by focused unit tests.
     *
     * @return no-op metrics recorder
     */
    public static OperationalMetrics noop() {
        return NOOP;
    }

    public void recordUpload(AnalyticsMetric.Operational.Operation operation,
                             AnalyticsMetric.Operational.Outcome outcome) {
        increment(AnalyticsMetric.Operational.Metric.Upload.COUNTERS, operation, outcome);
    }

    public void recordVirusScan(AnalyticsMetric.Key outcome) {
        increment(AnalyticsMetric.Operational.Metric.Virus.SCANS,
                AnalyticsMetric.Operational.Component.CLAMAV, outcome);
    }

    public void recordSearch(AnalyticsMetric.Operational.Backend backend,
                             AnalyticsMetric.Operational.Outcome outcome) {
        increment(AnalyticsMetric.Operational.Metric.Search.REQUESTS, backend, outcome);
    }

    public void recordWebSocket(AnalyticsMetric.Operational.Outcome outcome) {
        increment(AnalyticsMetric.Operational.Metric.WebSocket.CONNECTIONS,
                AnalyticsMetric.Operational.Component.STOMP, outcome);
    }

    public void recordRateLimitRejection(AnalyticsMetric.Key scope) {
        increment(AnalyticsMetric.Operational.Metric.RateLimit.REJECTIONS,
                scope, AnalyticsMetric.Operational.Outcome.REJECTED);
    }

    public void recordRequest(AnalyticsMetric.Operational.HttpMethod method,
                              AnalyticsMetric.Operational.RequestOutcome.Status.Key outcome) {
        increment(AnalyticsMetric.Operational.Metric.Request.COUNTERS, method, outcome);
    }

    /** Returns the current bounded counter snapshot for an operational report. */
    public Map<String, Double> snapshot(AnalyticsMetric.Key metric) {
        Map<String, Double> snapshot = new LinkedHashMap<>();
        registry.find(metric.value()).counters().forEach(counter -> {
            String key = counter.getId().getTags().stream()
                    .map(tag -> tag.getKey() + "=" + tag.getValue())
                    .sorted()
                    .reduce((left, right) -> left + "," + right)
                    .orElse("unlabelled");
            snapshot.put(key, counter.count());
        });
        return snapshot;
    }

    /**
     * Publishes the latest result of a mandatory dependency readiness probe.
     * The dependency names are fixed by the health-indicator call sites.
     */
    public void setDependencyAvailability(AnalyticsMetric.Key dependency, boolean available) {
        AtomicInteger state = dependencyAvailability.computeIfAbsent(dependency.value(), name -> {
            AtomicInteger value = new AtomicInteger();
            Gauge.builder("souklab.dependency.available", value, AtomicInteger::get)
                    .description("Whether a mandatory production dependency answered its readiness probe")
                    .tag("dependency", name)
                    .register(registry);
            return value;
        });
        state.set(available ? 1 : 0);
    }

    private void increment(AnalyticsMetric.Key metric, AnalyticsMetric.Key operation,
                           AnalyticsMetric.Key outcome) {
        increment(metric.value(), operation.value(), outcome.value());
    }

    private void increment(String metricName, String operation, String outcome) {
        registry.counter(metricName, "operation", operation, "outcome", outcome).increment();
    }
}
