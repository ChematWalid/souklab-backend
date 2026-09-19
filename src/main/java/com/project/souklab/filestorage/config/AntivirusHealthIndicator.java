package com.project.souklab.filestorage.config;

import com.project.souklab.config.OperationalMetrics;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Reports ClamAV reachability for the production upload readiness gate. */
@Component("antivirus")
@Profile({"prod", "production"})
@ConditionalOnProperty(name = "storage.virus-scan.enabled", havingValue = "true")
public class AntivirusHealthIndicator implements HealthIndicator {

    private final StorageProperties.VirusScanProperties properties;
    private final OperationalMetrics metrics;

    public AntivirusHealthIndicator(StorageProperties storageProperties, OperationalMetrics metrics) {
        this.properties = storageProperties.getVirusScan();
        this.metrics = metrics;
    }

    @Override
    public Health health() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(properties.getHost(), properties.getPort()),
                    Math.toIntExact(properties.getConnectionTimeout().toMillis()));
            metrics.setDependencyAvailability("clamav", true);
            return Health.up().build();
        } catch (IOException exception) {
            metrics.setDependencyAvailability("clamav", false);
            return Health.down().build();
        }
    }
}
