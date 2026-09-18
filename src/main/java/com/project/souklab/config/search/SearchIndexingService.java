package com.project.souklab.config.search;

import com.project.souklab.model.Artisan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletionStage;

/** Owns the transactional EntityManager boundary used by startup indexing. */
@Service
class SearchIndexingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    CompletionStage<Void> start(AppIndexingOptions options) {
        SearchSession searchSession = Search.session(entityManager);
        MassIndexer massIndexer = searchSession.massIndexer(Artisan.class)
                .threadsToLoadObjects(options.threadsToLoadObjects())
                .batchSizeToLoadObjects(options.batchSizeToLoadObjects())
                .idFetchSize(options.idFetchSize());
        return massIndexer.start().thenApply(ignored -> null);
    }

    record AppIndexingOptions(int threadsToLoadObjects, int batchSizeToLoadObjects, int idFetchSize) {
    }
}
