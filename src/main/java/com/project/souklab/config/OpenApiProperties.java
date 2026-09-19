package com.project.souklab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.openapi")
public class OpenApiProperties {
    private boolean enabled;
    private String path;
    private String swaggerPath;
    private String title;
    private String version;
}
