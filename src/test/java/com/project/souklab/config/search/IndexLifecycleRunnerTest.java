package com.project.souklab.config.search;

import com.project.souklab.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexLifecycleRunnerTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private SearchIndexingService searchIndexingService;

    @InjectMocks
    private IndexLifecycleRunner runner;

    @Test
    void doesNothingWhenStartupSynchronizationIsDisabled() {
        AppProperties.Search search = new AppProperties.Search();
        search.setSyncOnStartup(false);
        when(appProperties.getSearch()).thenReturn(search);

        assertThatCode(() -> runner.run(new DefaultApplicationArguments(new String[0]))).doesNotThrowAnyException();
        verifyNoInteractions(searchIndexingService);
    }

    @Test
    void configuresAndStartsMassIndexer() {
        AppProperties.Search search = searchProperties();
        when(appProperties.getSearch()).thenReturn(search);
        when(searchIndexingService.start(any())).thenReturn(CompletableFuture.completedFuture(null));

        assertThatCode(() -> runner.run(new DefaultApplicationArguments(new String[0]))).doesNotThrowAnyException();
        verify(searchIndexingService).start(any());
    }

    @Test
    void absorbsAsynchronousMassIndexFailure() {
        AppProperties.Search search = searchProperties();
        when(appProperties.getSearch()).thenReturn(search);
        when(searchIndexingService.start(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("backend unavailable")));

        assertThatCode(() -> runner.run(new DefaultApplicationArguments(new String[0]))).doesNotThrowAnyException();
    }

    @Test
    void absorbsImmediateSearchSetupFailure() {
        AppProperties.Search search = searchProperties();
        when(appProperties.getSearch()).thenReturn(search);

        when(searchIndexingService.start(any())).thenThrow(new IllegalStateException("not available"));
        assertThatCode(() -> runner.run(new DefaultApplicationArguments(new String[0]))).doesNotThrowAnyException();
    }

    private AppProperties.Search searchProperties() {
        AppProperties.Search search = new AppProperties.Search();
        search.setSyncOnStartup(true);
        search.getMassIndexing().setThreadsToLoadObjects(2);
        search.getMassIndexing().setBatchSizeToLoadObjects(25);
        search.getMassIndexing().setIdFetchSize(50);
        return search;
    }
}
