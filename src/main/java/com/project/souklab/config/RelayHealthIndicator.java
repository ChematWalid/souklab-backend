package com.project.souklab.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Reports TCP reachability of the RabbitMQ STOMP relay endpoint in production. */
@Component("relay")
@Profile({"prod", "production"})
public class RelayHealthIndicator implements HealthIndicator {

    private static final int CONNECT_TIMEOUT_MILLIS = 2_000;

    private final AppProperties.Relay properties;
    private final OperationalMetrics metrics;

    public RelayHealthIndicator(AppProperties appProperties, OperationalMetrics metrics) {
        this.properties = appProperties.getRelay();
        this.metrics = metrics;
    }

    @Override
    public Health health() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(properties.getHost(), properties.getPort()), CONNECT_TIMEOUT_MILLIS);
            metrics.setDependencyAvailability("rabbitmq", true);
            return Health.up().build();
        } catch (IOException exception) {
            metrics.setDependencyAvailability("rabbitmq", false);
            return Health.down().build();
        }
    }
}
