package com.project.souklab.dao;
import java.time.LocalDateTime;


import com.project.souklab.model.Formation;
import com.project.souklab.model.FormationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

/**
 * Data access repository for {@link Formation} entities.
 * Supports public discovery, instructor workspace browsing, dynamic specifications, and soft-delete exclusions.
 */
public interface FormationRepository extends JpaRepository<Formation, String>, JpaSpecificationExecutor<Formation> {
    long countByStatusAndDeletedAtIsNull(FormationStatus status);
    long countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(FormationStatus status,
                                                            LocalDateTime from, LocalDateTime to);
    long countByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime from, LocalDateTime to);

    @Query("select count(distinct formation.author.id) from Formation formation "
            + "where formation.status = :status and formation.deletedAt is null")
    long countDistinctAuthorsByStatusAndDeletedAtIsNull(@Param("status") FormationStatus status);

    @Query("select coalesce(sum(formation.maxParticipants), 0) from Formation formation "
            + "where formation.status = :status and formation.createdAt between :from and :to "
            + "and formation.deletedAt is null")
    long sumMaxParticipantsByStatusAndCreatedAtBetweenAndDeletedAtIsNull(
            @Param("status") FormationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * Retrieves active formations matching a specific status, excluding soft-deleted entities.
     *
     * @param status the formation status to filter by
     * @param pageable pagination and sorting parameters
     * @return page of matching active formations
     */
    Page<Formation> findByStatusAndDeletedAtIsNull(FormationStatus status, Pageable pageable);

    /**
     * Retrieves active formations authored by a specific artisan, excluding soft-deleted entities.
     *
     * @param authorId the unique identifier of the authoring artisan
     * @param pageable pagination and sorting parameters
     * @return page of formations authored by the artisan
     */
    Page<Formation> findByAuthorIdAndDeletedAtIsNull(String authorId, Pageable pageable);

    /**
     * Finds an active formation by ID and author ID, excluding soft-deleted entities.
     *
     * @param id the unique identifier of the formation
     * @param authorId the unique identifier of the authoring artisan
     * @return optional containing the formation if found and owned by the author
     */
    Optional<Formation> findByIdAndAuthorIdAndDeletedAtIsNull(String id, String authorId);

    /**
     * Finds an active formation by ID, excluding soft-deleted entities.
     *
     * @param id the unique identifier of the formation
     * @return optional containing the active formation if found
     */
    Optional<Formation> findByIdAndDeletedAtIsNull(String id);

    /**
     * Finds an active formation while holding a write lock for serialized mutations.
     *
     * @param id formation identifier
     * @return optional containing the locked formation
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Formation> findWithLockByIdAndDeletedAtIsNull(String id);

    Optional<Formation> findByThumbnailUrlAndDeletedAtIsNull(String thumbnailUrl);

    /**
     * Retrieves active formations authored by a specific artisan filtered by status, excluding soft-deleted entities.
     *
     * @param authorId the unique identifier of the authoring artisan
     * @param status the formation status to filter by
     * @param pageable pagination and sorting parameters
     * @return page of matching formations authored by the artisan
     */
    Page<Formation> findByAuthorIdAndStatusAndDeletedAtIsNull(String authorId, FormationStatus status, Pageable pageable);
}
