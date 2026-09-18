package com.project.souklab.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void recordUpload(String operation, String outcome) {
        increment("souklab.uploads", operation, outcome);
    }

    public void recordVirusScan(String outcome) {
        increment("souklab.virus.scans", "clamav", outcome);
    }

    public void recordSearch(String backend, String outcome) {
        increment("souklab.search.requests", backend, outcome);
    }

    public void recordWebSocket(String outcome) {
        increment("souklab.websocket.connections", "stomp", outcome);
    }

    /**
     * Publishes the latest result of a mandatory dependency readiness probe.
     * The dependency names are fixed by the health-indicator call sites.
     */
    public void setDependencyAvailability(String dependency, boolean available) {
        AtomicInteger state = dependencyAvailability.computeIfAbsent(dependency, name -> {
            AtomicInteger value = new AtomicInteger();
            Gauge.builder("souklab.dependency.available", value, AtomicInteger::get)
                    .description("Whether a mandatory production dependency answered its readiness probe")
                    .tag("dependency", name)
                    .register(registry);
            return value;
        });
        state.set(available ? 1 : 0);
    }

    private void increment(String metricName, String operation, String outcome) {
        registry.counter(metricName, "operation", operation, "outcome", outcome).increment();
    }
}
