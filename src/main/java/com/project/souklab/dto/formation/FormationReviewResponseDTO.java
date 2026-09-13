package com.project.souklab.dto.formation;

import com.project.souklab.model.FormationReview;
import com.project.souklab.model.FormationReviewDecision;
import com.project.souklab.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object encapsulating administrative moderation review details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationReviewResponseDTO {

    /**
     * Unique identifier of the review record.
     */
    private String id;

    /**
     * Unique identifier of the administrator who performed the review.
     */
    private String adminId;

    /**
     * Full display name or email of the reviewing administrator.
     */
    private String adminName;

    /**
     * Administrative decision reached (APPROVED or REJECTED).
     */
    private FormationReviewDecision decision;

    /**
     * Feedback or rejection rationale provided by the administrator.
     */
    private String comment;

    /**
     * Timestamp when the review decision was finalized.
     */
    private LocalDateTime reviewedAt;

    /**
     * Factory method mapping a FormationReview entity to a FormationReviewResponseDTO.
     *
     * @param review the formation review entity
     * @return populated FormationReviewResponseDTO
     */
    public static FormationReviewResponseDTO from(FormationReview review) {
        if (review == null) {
            return null;
        }
        User admin = review.getAdmin();
        String adminDisplayName = null;
        if (admin != null) {
            String first = admin.getFirstName() != null ? admin.getFirstName() : "";
            String last = admin.getLastName() != null ? admin.getLastName() : "";
            adminDisplayName = (first + " " + last).trim();
            if (adminDisplayName.isEmpty()) {
                adminDisplayName = admin.getEmail();
            }
        }
        return FormationReviewResponseDTO.builder()
                .id(review.getId())
                .adminId(admin != null ? admin.getId() : null)
                .adminName(adminDisplayName)
                .decision(review.getDecision())
                .comment(review.getComment())
                .reviewedAt(review.getReviewedAt())
                .build();
    }
}
