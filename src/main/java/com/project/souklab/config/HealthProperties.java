package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Externalized timeout policy for dependency readiness probes. */
@Data
@ConfigurationProperties(prefix = "app.health")
public class HealthProperties {
    private Duration dependencyTimeout;
}
