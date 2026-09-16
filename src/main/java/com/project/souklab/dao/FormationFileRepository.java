package com.project.souklab.dao;

import com.project.souklab.model.FormationFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data access repository for {@link FormationFile} entities.
 * Handles syllabus attachments, course materials, and soft-delete filtering.
 */
public interface FormationFileRepository extends JpaRepository<FormationFile, String> {

    /**
     * Retrieves all active course files belonging to a specific formation.
     *
     * @param formationId the unique identifier of the parent formation
     * @return list of active formation files
     */
    List<FormationFile> findByFormationIdAndDeletedAtIsNull(String formationId);

    /**
     * Finds an active course file by ID within a specific formation.
     *
     * @param id the unique identifier of the formation file
     * @param formationId the unique identifier of the parent formation
     * @return optional containing the formation file if found and active
     */
    Optional<FormationFile> findByIdAndFormationIdAndDeletedAtIsNull(String id, String formationId);

    Optional<FormationFile> findByStorageKeyAndDeletedAtIsNull(String storageKey);
}
