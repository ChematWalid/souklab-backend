package com.project.souklab.dao;

import com.project.souklab.model.Technique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Technique} entities.
 * Supports catalog browsing and artisan portfolio queries by traditional handcrafting techniques.
 */
@Repository
public interface TechniqueRepository extends JpaRepository<Technique, String> {

    /**
     * Retrieves all active craft techniques ordered by display weight.
     *
     * @return Ordered list of active craftsmanship techniques
     */
    List<Technique> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Finds a craft technique by its unique URL-friendly slug.
     *
     * @param slug Unique technique slug (e.g., "filigrane-en-argent")
     * @return Optional containing the matching Technique if found
     */
    Optional<Technique> findBySlug(String slug);

    /**
     * Checks if a craft technique exists with the specified slug.
     *
     * @param slug Technique slug to verify
     * @return True if a technique with the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
}
