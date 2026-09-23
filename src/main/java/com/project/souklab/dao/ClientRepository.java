package com.project.souklab.dao;

import com.project.souklab.model.Client;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

/**
 * Data access operations for {@link Client} profile entities.
 */
public interface ClientRepository extends JpaRepository<Client, String> {

    /**
     * Loads a client with an exclusive write lock for quota serialization.
     *
     * @param id client identifier
     * @return optional containing the locked client
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Client> findWithLockById(String id);
}
