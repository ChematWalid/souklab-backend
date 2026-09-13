package com.project.souklab.dao;

import com.project.souklab.model.Artisan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access operations for {@link Artisan} entities.
 */
@Repository
public interface ArtisanRepository extends JpaRepository<Artisan, String> {

    /**
     * Finds an artisan by their associated user account email address, ignoring case.
     *
     * @param email user email address
     * @return optional containing the artisan if found
     */
    Optional<Artisan> findByUserEmailIgnoreCase(String email);
}
