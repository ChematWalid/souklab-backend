package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.ClientFavoriteArtisan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data access operations for {@link ClientFavoriteArtisan} entities.
 * Supports exists checks, count queries, and batch-fetching visible favorites.
 */
public interface ClientFavoriteArtisanRepository extends JpaRepository<ClientFavoriteArtisan, String> {

    /**
     * Checks if a favorite relationship exists between the given client and artisan.
     *
     * @param clientId identifier of the client
     * @param artisanId identifier of the artisan
     * @return {@code true} if the favorite record exists
     */
    boolean existsByClientIdAndArtisanId(String clientId, String artisanId);

    /**
     * Finds a favorite record by client ID and artisan ID.
     *
     * @param clientId identifier of the client
     * @param artisanId identifier of the artisan
     * @return optional containing the favorite record if found
     */
    Optional<ClientFavoriteArtisan> findByClientIdAndArtisanId(String clientId, String artisanId);

    /**
     * Counts the total number of favorites recorded for a given client.
     *
     * @param clientId identifier of the client
     * @return total count of favorites
     */
    long countByClientId(String clientId);

    /**
     * Retrieves a paginated list of visible favorited artisans for a given client,
     * eager-loading single associations via entity graph while avoiding Cartesian collection joins.
     * Non-visible artisans (soft-deleted profile/user or currently suspended) are filtered out.
     *
     * @param clientId identifier of the client
     * @param now current temporal reference for ban status evaluation
     * @param pageable pagination parameters
     * @return page of favorite artisan entities
     */
    @EntityGraph(attributePaths = {
            "artisan.user",
            "artisan.region",
            "artisan.region.parent",
            "artisan.subCategory",
            "artisan.subCategory.category"
    })
    @Query(
            value = "SELECT cfa FROM ClientFavoriteArtisan cfa "
                    + "JOIN cfa.artisan a "
                    + "JOIN a.user u "
                    + "WHERE cfa.client.id = :clientId "
                    + "AND cfa.deletedAt IS NULL "
                    + "AND a.deletedAt IS NULL "
                    + "AND u.deletedAt IS NULL "
                    + "AND (u.status != :suspendedStatus "
                    + "     OR (u.bannedUntil IS NOT NULL AND u.bannedUntil <= :now))",
            countQuery = "SELECT COUNT(cfa) FROM ClientFavoriteArtisan cfa "
                    + "JOIN cfa.artisan a "
                    + "JOIN a.user u "
                    + "WHERE cfa.client.id = :clientId "
                    + "AND cfa.deletedAt IS NULL "
                    + "AND a.deletedAt IS NULL "
                    + "AND u.deletedAt IS NULL "
                    + "AND (u.status != :suspendedStatus "
                    + "     OR (u.bannedUntil IS NOT NULL AND u.bannedUntil <= :now))"
    )
    Page<ClientFavoriteArtisan> findVisibleByClientId(
            @Param("clientId") String clientId,
            @Param("suspendedStatus") AccountStatus suspendedStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
