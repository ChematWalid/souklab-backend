package com.project.souklab.service.directory;

import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.ArtisanRepository;
import com.project.souklab.dto.common.PaginatedResponse;
import com.project.souklab.dto.directory.ArtisanDirectoryCardDTO;
import com.project.souklab.dto.directory.DirectorySearchFilterDTO;
import com.project.souklab.dto.directory.DirectorySortOrder;
import com.project.souklab.model.Artisan;
import com.project.souklab.model.Epoque;
import com.project.souklab.model.JobCategory;
import com.project.souklab.model.JobSubCategory;
import com.project.souklab.model.Material;
import com.project.souklab.model.Region;
import com.project.souklab.model.Technique;
import com.project.souklab.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.sort.dsl.SearchSortFactory;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Production implementation of {@link DirectorySearchService}.
 * Executes multi-field full-text queries and faceted discovery against Elasticsearch via Hibernate Search 8.
 * Features an automatic, fail-safe fallback to relational JPA specifications when the search backend is disabled or fails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorySearchServiceImpl implements DirectorySearchService {

    private final EntityManager entityManager;
    private final ArtisanRepository artisanRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ArtisanDirectoryCardDTO> search(DirectorySearchFilterDTO filter) {
        if (!appProperties.getSearch().isEnabled()) {
            log.info("Hibernate Search is disabled; routing directory search to relational JPA fallback.");
            return searchRelationalFallback(filter);
        }

        try {
            return searchHibernateSearch(filter);
        } catch (Exception ex) {
            log.warn("Hibernate Search query encountered an error; falling back to relational JPA specification: {}", ex.getMessage());
            return searchRelationalFallback(filter);
        }
    }

    /**
     * Executes the directory query against Elasticsearch using the Hibernate Search 8 query DSL.
     * Hydrates matching entities eagerly using the {@code artisan.directory} named entity graph.
     *
     * @param filter validated directory search criteria
     * @return paginated directory cards
     */
    private PaginatedResponse<ArtisanDirectoryCardDTO> searchHibernateSearch(DirectorySearchFilterDTO filter) {
        SearchSession searchSession = Search.session(entityManager);
        int page = filter.resolvePage();
        int size = filter.resolveSize();
        int offset = page * size;

        SearchResult<Artisan> result = searchSession.search(Artisan.class)
                .where(f -> f.bool(b -> {
                    b.must(buildFullTextQuery(f, filter));
                    for (SearchPredicate filterClause : buildFilterClauses(f, filter)) {
                        b.filter(filterClause);
                    }
                }))
                .sort(f -> buildSort(f, filter))
                .loading(o -> o.graph("artisan.directory", GraphSemantic.FETCH))
                .fetch(offset, size);

        List<ArtisanDirectoryCardDTO> content = result.hits().stream()
                .map(ArtisanDirectoryCardDTO::from)
                .toList();

        long totalElements = result.total().hitCount();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        boolean isLast = (long) (page + 1) * size >= totalElements;

        return PaginatedResponse.<ArtisanDirectoryCardDTO>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(isLast)
                .build();
    }

    /**
     * Constructs the full-text search predicate across multi-field craft attributes.
     * Applies field-specific boosting and fuzzy matching for terms longer than 4 characters.
     *
     * @param f search predicate factory
     * @param filter directory search filter
     * @return scored search predicate
     */
    private SearchPredicate buildFullTextQuery(SearchPredicateFactory f, DirectorySearchFilterDTO filter) {
        if (!filter.hasKeyword()) {
            return f.matchAll().toPredicate();
        }

        String term = filter.getCleanKeyword();
        return f.bool()
                .should(createTextMatch(f, "user.name", term, 2.0f))
                .should(createTextMatch(f, "subCategory.name", term, 1.5f))
                .should(createTextMatch(f, "city", term, 1.2f))
                .should(createTextMatch(f, "materials.name", term, 1.1f))
                .should(createTextMatch(f, "techniques.name", term, 1.1f))
                .should(createTextMatch(f, "bio", term, 1.0f))
                .should(createTextMatch(f, "epoques.name", term, 1.0f))
                .toPredicate();
    }

    /**
     * Creates an individual field match predicate with designated boost and term-length fuzzy configuration.
     *
     * @param f search predicate factory
     * @param field Elasticsearch index field path
     * @param term sanitized search keyword
     * @param boost relevance score multiplier
     * @return search predicate
     */
    private SearchPredicate createTextMatch(SearchPredicateFactory f, String field, String term, float boost) {
        var matchStep = f.match().field(field).matching(term).boost(boost);
        if (term.length() > 4) {
            matchStep.fuzzy(1);
        }
        return matchStep.toPredicate();
    }

    /**
     * Builds cached bitset filter clauses for soft-deletion, taxonomy, geography, and boolean flags.
     *
     * @param f search predicate factory
     * @param filter directory search filter
     * @return list of filter predicates
     */
    private List<SearchPredicate> buildFilterClauses(SearchPredicateFactory f, DirectorySearchFilterDTO filter) {
        List<SearchPredicate> predicates = new ArrayList<>();

        predicates.add(f.not(f.exists().field("deletedAt")).toPredicate());

        if (filter.getRegionSlug() != null && !filter.getRegionSlug().isBlank()) {
            predicates.add(f.bool()
                    .should(f.match().field("region.slug").matching(filter.getRegionSlug()))
                    .should(f.match().field("region.parent.slug").matching(filter.getRegionSlug()))
                    .toPredicate());
        }

        if (filter.getWilayaCode() != null && !filter.getWilayaCode().isBlank()) {
            predicates.add(f.bool()
                    .should(f.match().field("region.code").matching(filter.getWilayaCode()))
                    .should(f.match().field("region.parent.code").matching(filter.getWilayaCode()))
                    .toPredicate());
        }

        if (filter.getCategorySlug() != null && !filter.getCategorySlug().isBlank()) {
            predicates.add(f.match().field("subCategory.category.slug").matching(filter.getCategorySlug()).toPredicate());
        }

        if (filter.getSubCategorySlug() != null && !filter.getSubCategorySlug().isBlank()) {
            predicates.add(f.match().field("subCategory.slug").matching(filter.getSubCategorySlug()).toPredicate());
        }

        if (filter.getMaterials() != null && !filter.getMaterials().isEmpty()) {
            predicates.add(f.terms().field("materials.slug").matchingAny(filter.getMaterials()).toPredicate());
        }

        if (filter.getTechniques() != null && !filter.getTechniques().isEmpty()) {
            predicates.add(f.terms().field("techniques.slug").matchingAny(filter.getTechniques()).toPredicate());
        }

        if (filter.getEpoques() != null && !filter.getEpoques().isEmpty()) {
            predicates.add(f.terms().field("epoques.slug").matchingAny(filter.getEpoques()).toPredicate());
        }

        if (filter.getMinRating() != null && filter.getMinRating() > 0.0) {
            predicates.add(f.range().field("rating").atLeast(filter.getMinRating()).toPredicate());
        }

        if (Boolean.TRUE.equals(filter.getVerifiedOnly())) {
            predicates.add(f.match().field("isVerified").matching(true).toPredicate());
        }

        if (Boolean.TRUE.equals(filter.getPremiumOnly())) {
            predicates.add(f.match().field("isPremium").matching(true).toPredicate());
        }

        if (Boolean.TRUE.equals(filter.getTeacherOnly())) {
            predicates.add(f.match().field("isTeacher").matching(true).toPredicate());
        }

        return predicates;
    }

    /**
     * Maps directory sort parameters to Hibernate Search sort descriptors.
     *
     * @param f search sort factory
     * @param filter directory search filter
     * @return search sort
     */
    private SortFinalStep buildSort(SearchSortFactory f, DirectorySearchFilterDTO filter) {
        DirectorySortOrder sortOrder = filter.resolveSortBy();
        return switch (sortOrder) {
            case RATING_DESC -> f.field("rating").desc()
                    .then().field("reviewsCount").desc();
            case REVIEWS_DESC -> f.field("reviewsCount").desc()
                    .then().field("rating").desc();
            case VIEWS_DESC -> f.field("viewsCount").desc();
            case NEWEST -> f.field("createdAt").desc();
            case RELEVANCE -> {
                if (filter.hasKeyword()) {
                    yield f.score()
                            .then().field("rating").desc();
                } else {
                    yield f.field("rating").desc();
                }
            }
        };
    }

    /**
     * Fallback execution utilizing relational JPA Specifications and named entity graphs.
     * Guarantees zero downtime and consistent API discovery even if Elasticsearch is unreachable.
     *
     * @param filter directory search criteria
     * @return paginated directory card responses
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ArtisanDirectoryCardDTO> searchRelationalFallback(DirectorySearchFilterDTO filter) {
        Specification<Artisan> spec = buildRelationalSpecification(filter);
        Sort sort = buildRelationalSort(filter);
        Pageable pageable = PageRequest.of(filter.resolvePage(), filter.resolveSize(), sort);

        Page<Artisan> page = artisanRepository.findAll(spec, pageable);
        Page<ArtisanDirectoryCardDTO> dtoPage = page.map(ArtisanDirectoryCardDTO::from);
        return PaginatedResponse.from(dtoPage);
    }

    /**
     * Builds a Spring Data JPA specification mirroring the Hibernate Search criteria for relational execution.
     *
     * @param filter directory search filter
     * @return JPA specification
     */
    private Specification<Artisan> buildRelationalSpecification(DirectorySearchFilterDTO filter) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            appendRelationalKeyword(root, cb, filter, predicates);
            appendRelationalGeography(root, cb, filter, predicates);
            appendRelationalTaxonomy(root, cb, filter, predicates);
            appendRelationalAccreditation(root, cb, filter, predicates);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Appends relational full-text keyword matching predicates using LIKE across craft narrative and taxonomy names.
     */
    private void appendRelationalKeyword(
            Root<Artisan> root,
            CriteriaBuilder cb,
            DirectorySearchFilterDTO filter,
            List<Predicate> predicates
    ) {
        if (!filter.hasKeyword()) {
            return;
        }

        String pattern = "%" + filter.getCleanKeyword().toLowerCase() + "%";
        Join<Artisan, User> userJoin = root.join("user", JoinType.LEFT);
        Join<Artisan, JobSubCategory> subCatJoin = root.join("subCategory", JoinType.LEFT);
        Join<Artisan, Material> materialJoin = root.join("materials", JoinType.LEFT);
        Join<Artisan, Technique> techniqueJoin = root.join("techniques", JoinType.LEFT);
        Join<Artisan, Epoque> epoqueJoin = root.join("epoques", JoinType.LEFT);

        predicates.add(cb.or(
                cb.like(cb.lower(root.get("bio")), pattern),
                cb.like(cb.lower(root.get("city")), pattern),
                cb.like(cb.lower(userJoin.get("firstName")), pattern),
                cb.like(cb.lower(userJoin.get("lastName")), pattern),
                cb.like(cb.lower(subCatJoin.get("name")), pattern),
                cb.like(cb.lower(materialJoin.get("name")), pattern),
                cb.like(cb.lower(techniqueJoin.get("name")), pattern),
                cb.like(cb.lower(epoqueJoin.get("name")), pattern)
        ));
    }

    /**
     * Appends relational geographic criteria matching on Wilaya codes or Commune/Wilaya slugs.
     */
    private void appendRelationalGeography(
            Root<Artisan> root,
            CriteriaBuilder cb,
            DirectorySearchFilterDTO filter,
            List<Predicate> predicates
    ) {
        boolean hasRegion = filter.getRegionSlug() != null && !filter.getRegionSlug().isBlank();
        boolean hasWilaya = filter.getWilayaCode() != null && !filter.getWilayaCode().isBlank();

        if (!hasRegion && !hasWilaya) {
            return;
        }

        Join<Artisan, Region> regionJoin = root.join("region", JoinType.LEFT);
        Join<Region, Region> parentJoin = regionJoin.join("parent", JoinType.LEFT);

        if (hasRegion) {
            predicates.add(cb.or(
                    cb.equal(regionJoin.get("slug"), filter.getRegionSlug()),
                    cb.equal(parentJoin.get("slug"), filter.getRegionSlug())
            ));
        }

        if (hasWilaya) {
            predicates.add(cb.or(
                    cb.equal(regionJoin.get("code"), filter.getWilayaCode()),
                    cb.equal(parentJoin.get("code"), filter.getWilayaCode())
            ));
        }
    }

    /**
     * Appends relational craft taxonomy criteria across categories, subcategories, materials, techniques, and epochs.
     */
    private void appendRelationalTaxonomy(
            Root<Artisan> root,
            CriteriaBuilder cb,
            DirectorySearchFilterDTO filter,
            List<Predicate> predicates
    ) {
        boolean hasCategory = filter.getCategorySlug() != null && !filter.getCategorySlug().isBlank();
        boolean hasSubCategory = filter.getSubCategorySlug() != null && !filter.getSubCategorySlug().isBlank();

        if (hasCategory || hasSubCategory) {
            Join<Artisan, JobSubCategory> subCatJoin = root.join("subCategory", JoinType.LEFT);
            if (hasSubCategory) {
                predicates.add(cb.equal(subCatJoin.get("slug"), filter.getSubCategorySlug()));
            }
            if (hasCategory) {
                Join<JobSubCategory, JobCategory> catJoin = subCatJoin.join("category", JoinType.LEFT);
                predicates.add(cb.equal(catJoin.get("slug"), filter.getCategorySlug()));
            }
        }

        if (filter.getMaterials() != null && !filter.getMaterials().isEmpty()) {
            Join<Artisan, Material> materialJoin = root.join("materials", JoinType.LEFT);
            predicates.add(materialJoin.get("slug").in(filter.getMaterials()));
        }

        if (filter.getTechniques() != null && !filter.getTechniques().isEmpty()) {
            Join<Artisan, Technique> techniqueJoin = root.join("techniques", JoinType.LEFT);
            predicates.add(techniqueJoin.get("slug").in(filter.getTechniques()));
        }

        if (filter.getEpoques() != null && !filter.getEpoques().isEmpty()) {
            Join<Artisan, Epoque> epoqueJoin = root.join("epoques", JoinType.LEFT);
            predicates.add(epoqueJoin.get("slug").in(filter.getEpoques()));
        }
    }

    /**
     * Appends relational accreditation, teacher status, and rating threshold predicates.
     */
    private void appendRelationalAccreditation(
            Root<Artisan> root,
            CriteriaBuilder cb,
            DirectorySearchFilterDTO filter,
            List<Predicate> predicates
    ) {
        if (filter.getMinRating() != null && filter.getMinRating() > 0.0) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filter.getMinRating()));
        }

        if (Boolean.TRUE.equals(filter.getVerifiedOnly())) {
            predicates.add(cb.isTrue(root.get("isVerified")));
        }

        if (Boolean.TRUE.equals(filter.getPremiumOnly())) {
            predicates.add(cb.isTrue(root.get("isPremium")));
        }

        if (Boolean.TRUE.equals(filter.getTeacherOnly())) {
            predicates.add(cb.isTrue(root.get("isTeacher")));
        }
    }

    /**
     * Maps directory sort parameters to Spring Data relational Sort criteria.
     *
     * @param filter directory search filter
     * @return relational sort order
     */
    private Sort buildRelationalSort(DirectorySearchFilterDTO filter) {
        DirectorySortOrder sortOrder = filter.resolveSortBy();
        return switch (sortOrder) {
            case RATING_DESC -> Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "reviewsCount"));
            case REVIEWS_DESC -> Sort.by(Sort.Direction.DESC, "reviewsCount")
                    .and(Sort.by(Sort.Direction.DESC, "rating"));
            case VIEWS_DESC -> Sort.by(Sort.Direction.DESC, "viewsCount");
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case RELEVANCE -> Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "reviewsCount"));
        };
    }
}
