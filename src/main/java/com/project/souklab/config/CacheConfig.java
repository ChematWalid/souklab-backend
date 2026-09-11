package com.project.souklab.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Spring Cache abstraction configuration with Caffeine in-memory store.
 * Registers catalog taxonomy caches with a 60-minute time-to-live expiration policy
 * to support low-latency reads for high-frequency reference data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_REGIONS = "catalog_regions";
    public static final String CACHE_CATEGORIES = "catalog_categories";
    public static final String CACHE_MATERIALS = "catalog_materials";
    public static final String CACHE_EPOQUES = "catalog_epoques";
    public static final String CACHE_TECHNIQUES = "catalog_techniques";

    /**
     * Configures the primary {@link CacheManager} registering dedicated catalog cache buckets
     * backed by Caffeine with 60-minute write expiration and maximum size bounds.
     *
     * @return Configured CaffeineCacheManager bean
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            CACHE_REGIONS,
            CACHE_CATEGORIES,
            CACHE_MATERIALS,
            CACHE_EPOQUES,
            CACHE_TECHNIQUES
        );
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(1000)
        );
        return cacheManager;
    }
}
