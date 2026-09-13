package com.project.souklab.service.directory;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.directory.ArtisanDirectoryCardDTO;
import com.project.souklab.dto.directory.DirectorySearchFilterDTO;
import com.project.souklab.dto.directory.DirectorySortOrder;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Material;
import com.project.souklab.model.Region;
import com.project.souklab.model.Technique;
import com.project.souklab.model.User;
import jakarta.persistence.EntityManager;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.SearchResultTotal;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.engine.search.query.dsl.SearchQuerySelectStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite verifying {@link DirectorySearchServiceImpl}.
 * Validates Hibernate Search query dispatching, automatic relational JPA specification fallback
 * when search is disabled or throws exceptions, and multi-facet sort mapping.
 */
@ExtendWith(MockitoExtension.class)
class DirectorySearchServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ArtisanRepository artisanRepository;

    @Mock
    private AppProperties appProperties;

    private DirectorySearchServiceImpl directorySearchService;

    @BeforeEach
    void setUp() {
        directorySearchService = new DirectorySearchServiceImpl(entityManager, artisanRepository, appProperties);
    }

    /**
     * Verifies that when search is disabled via configuration, queries are immediately routed to JPA fallback.
     */
    @Test
    @DisplayName("search: when Hibernate Search is disabled, routes directly to relational JPA fallback")
    @SuppressWarnings("unchecked")
    void search_whenSearchDisabled_shouldFallbackToRelationalSpecification() {
        AppProperties.Search searchConfig = new AppProperties.Search();
        searchConfig.setEnabled(false);
        when(appProperties.getSearch()).thenReturn(searchConfig);

        Artisan sampleArtisan = createSampleArtisan();
        when(artisanRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleArtisan)));

        DirectorySearchFilterDTO filter = DirectorySearchFilterDTO.builder()
                .keyword("poterie")
                .regionSlug("tizi-ouzou")
                .page(0)
                .size(20)
                .build();

        PaginatedResponse<ArtisanDirectoryCardDTO> response = directorySearchService.search(filter);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo("artisan-user-1");
        assertThat(response.getContent().get(0).getArtisanName()).isEqualTo("Djamel Amrani");
        assertThat(response.getContent().get(0).getWilayaName()).isEqualTo("Tizi Ouzou");
        verify(artisanRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * Verifies that when Hibernate Search throws an exception (e.g. cluster unavailable), it falls back cleanly to JPA.
     */
    @Test
    @DisplayName("search: when Hibernate Search throws an exception, catches and seamlessly falls back to JPA")
    @SuppressWarnings("unchecked")
    void search_whenHibernateSearchThrowsException_shouldFallbackToRelationalSpecification() {
        AppProperties.Search searchConfig = new AppProperties.Search();
        searchConfig.setEnabled(true);
        when(appProperties.getSearch()).thenReturn(searchConfig);

        Artisan sampleArtisan = createSampleArtisan();
        when(artisanRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleArtisan)));

        try (MockedStatic<Search> searchMock = mockStatic(Search.class)) {
            searchMock.when(() -> Search.session(entityManager))
                    .thenThrow(new RuntimeException("Elasticsearch cluster connection refused"));

            DirectorySearchFilterDTO filter = DirectorySearchFilterDTO.builder()
                    .keyword("bijoutier")
                    .build();

            PaginatedResponse<ArtisanDirectoryCardDTO> response = directorySearchService.search(filter);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getId()).isEqualTo("artisan-user-1");
            verify(artisanRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    /**
     * Verifies that when Hibernate Search succeeds, paginated directory cards are mapped and returned.
     */
    @Test
    @DisplayName("search: when Hibernate Search succeeds, returns mapped PaginatedResponse")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void search_whenHibernateSearchSucceeds_shouldReturnDirectoryCards() {
        AppProperties.Search searchConfig = new AppProperties.Search();
        searchConfig.setEnabled(true);
        when(appProperties.getSearch()).thenReturn(searchConfig);

        Artisan sampleArtisan = createSampleArtisan();

        try (MockedStatic<Search> searchMock = mockStatic(Search.class)) {
            SearchSession searchSession = mock(SearchSession.class);
            SearchQuerySelectStep selectStep = mock(SearchQuerySelectStep.class);
            SearchQueryOptionsStep optionsStep = mock(SearchQueryOptionsStep.class);
            SearchResult searchResult = mock(SearchResult.class);
            SearchResultTotal totalResult = mock(SearchResultTotal.class);

            searchMock.when(() -> Search.session(entityManager)).thenReturn(searchSession);
            when(searchSession.search(Artisan.class)).thenReturn(selectStep);
            when(selectStep.where(any(Function.class))).thenReturn(optionsStep);
            when(optionsStep.sort(any(Function.class))).thenReturn(optionsStep);
            when(optionsStep.loading(any(Consumer.class))).thenReturn(optionsStep);
            when(optionsStep.fetch(anyInt(), anyInt())).thenReturn(searchResult);

            when(searchResult.hits()).thenReturn(List.of(sampleArtisan));
            when(searchResult.total()).thenReturn(totalResult);
            when(totalResult.hitCount()).thenReturn(1L);

            DirectorySearchFilterDTO filter = DirectorySearchFilterDTO.builder()
                    .keyword("ceramique")
                    .page(0)
                    .size(20)
                    .build();

            PaginatedResponse<ArtisanDirectoryCardDTO> response = directorySearchService.search(filter);

            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1L);
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getId()).isEqualTo("artisan-user-1");
            assertThat(response.getContent().get(0).getArtisanName()).isEqualTo("Djamel Amrani");
            verify(artisanRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    /**
     * Verifies that relational fallback accurately translates each DirectorySortOrder into Spring Data Sort.
     */
    @ParameterizedTest(name = "searchRelationalFallback: maps {0} to property {1} and direction {2}")
    @CsvSource({
            "RATING_DESC, rating, DESC",
            "REVIEWS_DESC, reviewsCount, DESC",
            "VIEWS_DESC, viewsCount, DESC",
            "NEWEST, createdAt, DESC",
            "RELEVANCE, rating, DESC"
    })
    @DisplayName("searchRelationalFallback: translates each DirectorySortOrder into expected Spring Data Sort")
    @SuppressWarnings("unchecked")
    void searchRelationalFallback_sortOrderMapping(DirectorySortOrder sortOrder, String primaryProperty, Sort.Direction direction) {
        when(artisanRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(createSampleArtisan())));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        DirectorySearchFilterDTO filter = DirectorySearchFilterDTO.builder()
                .sortBy(sortOrder)
                .page(0)
                .size(10)
                .build();

        directorySearchService.searchRelationalFallback(filter);

        verify(artisanRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order order = capturedPageable.getSort().getOrderFor(primaryProperty);
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(direction);
    }

    /**
     * Verifies that relational fallback executes without exceptions across multi-facet filter permutations.
     */
    @Test
    @DisplayName("searchRelationalFallback: builds relational specification across all filter facets")
    @SuppressWarnings("unchecked")
    void searchRelationalFallback_withComprehensiveFilters() {
        Artisan sampleArtisan = createSampleArtisan();
        ArgumentCaptor<Specification<Artisan>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(artisanRepository.findAll(specCaptor.capture(), pageableCaptor.capture()))
                .thenReturn(new PageImpl<>(List.of(sampleArtisan)));

        DirectorySearchFilterDTO filter = DirectorySearchFilterDTO.builder()
                .keyword("poterie")
                .regionSlug("tizi-ouzou")
                .wilayaCode("15")
                .categorySlug("art-du-feu")
                .subCategorySlug("poterie-traditionnelle")
                .materials(List.of("argile-rouge"))
                .techniques(List.of("modelage-ancestral"))
                .epoques(List.of("epoque-ottomane"))
                .minRating(4.0)
                .verifiedOnly(true)
                .premiumOnly(true)
                .teacherOnly(true)
                .sortBy(DirectorySortOrder.RATING_DESC)
                .page(1)
                .size(15)
                .build();

        PaginatedResponse<ArtisanDirectoryCardDTO> response = directorySearchService.searchRelationalFallback(filter);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(specCaptor.getValue()).isNotNull();

        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(1);
        assertThat(capturedPageable.getPageSize()).isEqualTo(15);
    }

    private Artisan createSampleArtisan() {
        User user = User.builder()
                .firstName("Djamel")
                .lastName("Amrani")
                .email("djamel@souklab.dz")
                .avatarUrl("https://storage.souklab.dz/avatars/djamel.jpg")
                .build();

        Region wilaya = Region.builder()
                .name("Tizi Ouzou")
                .slug("tizi-ouzou")
                .code("15")
                .build();

        Region commune = Region.builder()
                .name("Beni Yenni")
                .slug("beni-yenni")
                .parent(wilaya)
                .build();

        JobCategory category = JobCategory.builder()
                .name("Art du Feu")
                .slug("art-du-feu")
                .build();

        JobSubCategory subCategory = JobSubCategory.builder()
                .name("Poterie Traditionnelle")
                .slug("poterie-traditionnelle")
                .category(category)
                .build();

        Material material = Material.builder()
                .name("Argile Rouge")
                .slug("argile-rouge")
                .build();

        Technique technique = Technique.builder()
                .name("Modelage Ancestral")
                .slug("modelage-ancestral")
                .build();

        return Artisan.builder()
                .id("artisan-user-1")
                .user(user)
                .bio("Maître artisan potier perpétuant les traditions ancestrales.")
                .city("Beni Yenni")
                .region(commune)
                .subCategory(subCategory)
                .materials(new HashSet<>(Set.of(material)))
                .techniques(new HashSet<>(Set.of(technique)))
                .rating(4.85)
                .reviewsCount(42)
                .viewsCount(1250)
                .isVerified(true)
                .isPremium(true)
                .isTeacher(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
