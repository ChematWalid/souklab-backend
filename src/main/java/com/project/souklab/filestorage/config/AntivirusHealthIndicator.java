package com.project.souklab.filestorage.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Reports ClamAV reachability for the production upload readiness gate. */
@Component("antivirus")
@Profile({"prod", "production"})
public class AntivirusHealthIndicator implements HealthIndicator {

    private static final int CONNECT_TIMEOUT_MILLIS = 2_000;

    private final StorageProperties.VirusScanProperties properties;

    public AntivirusHealthIndicator(StorageProperties storageProperties) {
        this.properties = storageProperties.getVirusScan();
    }

    @Override
    public Health health() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(properties.getHost(), properties.getPort()), CONNECT_TIMEOUT_MILLIS);
            return Health.up().build();
        } catch (IOException exception) {
            return Health.down().build();
        }
    }
}
