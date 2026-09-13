package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an administrative moderation review of a submitted formation.
 */
@Entity
@Table(
        name = "formation_reviews",
        indexes = {
                @Index(name = "idx_formation_reviews_formation", columnList = "formation_id, reviewed_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormationReview extends BaseEntity {

    /**
     * Formation evaluated during this moderation review.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false)
    private Formation formation;

    /**
     * Administrator user who conducted the moderation review.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    /**
     * Administrative decision reached (APPROVED or REJECTED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FormationReviewDecision decision;

    /**
     * Qualitative feedback, revision instructions, or approval comments.
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * Timestamp when the administrative decision was finalized.
     */
    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;
}
