package com.project.souklab.config;

import com.project.souklab.analytics.AnalyticsMetric;
import io.lettuce.core.RedisClient;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;


/** Readiness probe for the mandatory production rate-limit store. */
@Component("redis")
@ConditionalOnBean(RedisClient.class)
@Profile({"prod", "production"})
public class RedisHealthIndicator implements HealthIndicator {
    private final RedisClient client;
    private final OperationalMetrics metrics;

    public RedisHealthIndicator(RedisClient client, OperationalMetrics metrics) {
        this.client = client;
        this.metrics = metrics;
    }

    @Override
    public Health health() {
        try (var connection = client.connect()) {
            boolean available = "PONG".equalsIgnoreCase(connection.sync().ping());
            metrics.setDependencyAvailability(AnalyticsMetric.Operational.Dependency.Redis.VALUE, available);
            return available ? Health.up().build() : Health.down().build();
        } catch (RuntimeException exception) {
            metrics.setDependencyAvailability(AnalyticsMetric.Operational.Dependency.Redis.VALUE, false);
            return Health.down().build();
        }
    }
}
