package com.project.souklab.config.search;

import com.project.souklab.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

/**
 * Startup lifecycle runner responsible for synchronizing database records into Elasticsearch.
 * Executes Hibernate Search MassIndexer asynchronously to populate the public artisan index.
 * Designed with fail-safe error boundaries to guarantee that isolated unit tests or offline environments
 * do not crash the application context when Elasticsearch is unreachable.
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.search.sync-on-startup", havingValue = "true", matchIfMissing = true)
public class IndexLifecycleRunner {

    private final AppProperties appProperties;
    private final SearchIndexingService searchIndexingService;

    public void run(ApplicationArguments args) {
        if (!appProperties.getSearch().isSyncOnStartup()) {
            log.info("Hibernate Search startup mass indexing disabled via configuration.");
            return;
        }

        try {
            log.info("Initiating asynchronous Hibernate Search mass indexing for Artisan entity...");
            AppProperties.Search.MassIndexing indexing = appProperties.getSearch().getMassIndexing();
            searchIndexingService.start(new SearchIndexingService.AppIndexingOptions(
                            indexing.getThreadsToLoadObjects(),
                            indexing.getBatchSizeToLoadObjects(),
                            indexing.getIdFetchSize()))
                    .thenAccept(v -> log.info("Hibernate Search mass indexing finished successfully."))
                    .exceptionally(throwable -> {
                        log.warn("Hibernate Search mass indexing encountered an issue: {}", throwable.getMessage());
                        return null;
                    });
        } catch (Exception ex) {
            log.warn("Failed to trigger Hibernate Search mass indexing on startup: {}", ex.getMessage());
        }
    }

    /**
     * Starts indexing only after the application context and Hibernate Search backend
     * have completed initialization.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        run(new org.springframework.boot.DefaultApplicationArguments(new String[0]));
    }
}
