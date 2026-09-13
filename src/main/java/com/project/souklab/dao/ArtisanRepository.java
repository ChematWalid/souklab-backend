package com.project.souklab.dao;

import com.project.souklab.model.Artisan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access operations for {@link Artisan} entities.
 * Supports relational queries, directory specifications, and multi-facet filtering.
 */
@Repository
public interface ArtisanRepository extends JpaRepository<Artisan, String>, JpaSpecificationExecutor<Artisan> {

    /**
     * Finds an artisan by their associated user account email address, ignoring case.
     *
     * @param email user email address
     * @return optional containing the artisan if found
     */
    Optional<Artisan> findByUserEmailIgnoreCase(String email);

    /**
     * Finds an artisan by ID with eagerly loaded user, region, subcategory, materials, techniques, and gallery images.
     *
     * @param id artisan identifier
     * @return optional containing the fully loaded artisan if found
     */
    @EntityGraph(value = "artisan.directory", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Artisan> findWithDirectoryDetailsById(String id);

    /**
     * Executes a specification query with the named entity graph applied to eliminate N+1 queries.
     *
     * @param spec search criteria specification
     * @param pageable pagination and sorting parameters
     * @return page of artisan entities with associations pre-fetched
     */
    @Override
    @EntityGraph(value = "artisan.directory", type = EntityGraph.EntityGraphType.LOAD)
    Page<Artisan> findAll(Specification<Artisan> spec, Pageable pageable);
}
