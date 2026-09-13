package com.project.souklab.dto.formation;

import com.project.souklab.model.FormationReviewDecision;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for administrative moderation of a submitted formation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationReviewRequestDTO {

    /**
     * Administrative decision (APPROVED or REJECTED).
     */
    @NotNull(message = "Review decision is required.")
    private FormationReviewDecision decision;

    /**
     * Narrative feedback or rejection rationale. Required when decision is REJECTED.
     */
    private String comment;
}
