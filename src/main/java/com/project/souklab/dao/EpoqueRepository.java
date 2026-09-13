package com.project.souklab.dao;

import com.project.souklab.model.Epoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Epoque} entities.
 * Supports heritage catalog filtering and cultural era discovery across Algerian craftsmanship history.
 */
public interface EpoqueRepository extends JpaRepository<Epoque, String> {

    /**
     * Retrieves all active historical epochs ordered by chronological display weight.
     *
     * @return Ordered list of active historical epochs
     */
    List<Epoque> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Finds a historical epoch by its unique URL-friendly slug.
     *
     * @param slug Unique epoch slug (e.g., "periode-zianide")
     * @return Optional containing the matching Epoque if found
     */
    Optional<Epoque> findBySlug(String slug);

    /**
     * Checks if a historical epoch exists with the specified slug.
     *
     * @param slug Epoch slug to verify
     * @return True if an epoch with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
}
