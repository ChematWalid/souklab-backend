package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Loads a user with a write lock for serialized quota and profile mutations.
     *
     * @param id user identifier
     * @return optional containing the locked user
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findWithLockById(String id);

    @EntityGraph(attributePaths = {"permissions"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"permissions"})
    @Query("SELECT u FROM User u WHERE u.email = :emailOrUsername")
    Optional<User> findByUsername(@Param("emailOrUsername") String emailOrUsername);

    default boolean existsByUsername(String username) {
        return existsByEmail(username);
    }

    Page<User> findByStatus(AccountStatus status, Pageable pageable);

    long countByStatus(AccountStatus status);

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(AccountStatus status);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime from, LocalDateTime to);

    long countByEmailVerifiedTrueAndCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByEmailVerifiedTrueAndCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime from, LocalDateTime to);

    long countByEmailVerifiedTrueAndDeletedAtIsNull();

    @Query("select count(u) from User u where u.artisan is not null and u.deletedAt is null")
    long countArtisanProfiles();

    @Query("select count(u) from User u where u.client is not null and u.deletedAt is null")
    long countClientProfiles();

    @Query("select count(u) from User u where u.artisan is not null and u.status = :status and u.deletedAt is null")
    long countActiveArtisanProfiles(@Param("status") AccountStatus status);

    @Query("select count(u) from User u where u.client is not null and u.status = :status and u.deletedAt is null")
    long countActiveClientProfiles(@Param("status") AccountStatus status);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.permissions p WHERE p.permissionKey = :permissionKey AND p.enabled = true AND u.deletedAt IS NULL")
    List<User> findByPermissionKey(@Param("permissionKey") String permissionKey);
}
