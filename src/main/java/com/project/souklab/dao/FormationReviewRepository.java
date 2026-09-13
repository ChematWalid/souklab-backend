package com.project.souklab.dao;

import com.project.souklab.model.FormationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access repository for {@link FormationReview} entities.
 * Manages administrative moderation histories and decision logs.
 */
@Repository
public interface FormationReviewRepository extends JpaRepository<FormationReview, String> {

    /**
     * Retrieves all review and moderation entries for a formation ordered chronologically descending.
     *
     * @param formationId the unique identifier of the formation
     * @return list of formation review entries in descending order of review date
     */
    List<FormationReview> findByFormationIdOrderByReviewedAtDesc(String formationId);
}
