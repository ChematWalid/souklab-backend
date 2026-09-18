package com.project.souklab.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/** Reports Elasticsearch reachability for production readiness probes. */
@Component("search")
@Profile({"prod", "production"})
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private final AppProperties.Search properties;
    private final HttpClient client;

    public ElasticsearchHealthIndicator(AppProperties appProperties) {
        this.properties = appProperties.getSearch();
        this.client = HttpClient.newBuilder()
                .connectTimeout(timeout(properties.getConnectionTimeout()))
                .build();
    }

    @Override
    public Health health() {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder()
                    .uri(firstUri(properties.getUris()))
                    .timeout(timeout(properties.getReadTimeout()))
                    .GET();
            if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
                String credentials = properties.getUsername() + ":" + properties.getPassword();
                request.header("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            }
            int status = client.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
            return status >= 200 && status < 300 ? Health.up().build() : Health.down().build();
        } catch (Exception exception) {
            return Health.down().build();
        }
    }

    private static URI firstUri(String configuredUris) {
        return URI.create(configuredUris.split(",")[0].trim());
    }

    private static Duration timeout(int millis) {
        return Duration.ofMillis(Math.max(100, millis));
    }
}
