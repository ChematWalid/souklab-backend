package com.project.souklab.config.search;

import com.project.souklab.model.Artisan;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchIndexingServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private SearchSession searchSession;

    @Mock
    private MassIndexer massIndexer;

    @InjectMocks
    private SearchIndexingService service;

    @Test
    void startsConfiguredMassIndexerThroughEntityManager() {
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
        SearchIndexingService.AppIndexingOptions options =
                new SearchIndexingService.AppIndexingOptions(2, 25, 50);
        when(searchSession.massIndexer(Artisan.class)).thenReturn(massIndexer);
        when(massIndexer.threadsToLoadObjects(2)).thenReturn(massIndexer);
        when(massIndexer.batchSizeToLoadObjects(25)).thenReturn(massIndexer);
        when(massIndexer.idFetchSize(50)).thenReturn(massIndexer);
        when(massIndexer.start()).thenAnswer(invocation -> CompletableFuture.completedFuture(null));

        try (MockedStatic<Search> searchMock = mockStatic(Search.class)) {
            searchMock.when(() -> Search.session(entityManager)).thenReturn(searchSession);
            assertThat(service.start(options)).isCompleted();
        }
    }
}
