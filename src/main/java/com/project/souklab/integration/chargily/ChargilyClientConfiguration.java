package com.project.souklab.integration.chargily;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ChargilyClientConfiguration {
    @Bean
    @ConditionalOnMissingBean(WebClient.Builder.class)
    public WebClient.Builder chargilyWebClientBuilder() {
        return WebClient.builder();
    }
}
