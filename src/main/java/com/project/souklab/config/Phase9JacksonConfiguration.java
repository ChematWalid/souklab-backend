package com.project.souklab.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides the Jackson 2 mapper used by the provider boundary and persisted snapshots. */
@Configuration
public class Phase9JacksonConfiguration {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper phase9ObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
