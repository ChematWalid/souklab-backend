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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an artisan's participation enrollment in a masterclass formation.
 */
@Entity
@Table(
        name = "formation_enrollments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_formation_enrollment_formation_artisan", columnNames = {"formation_id", "artisan_id"})
        },
        indexes = {
                @Index(name = "idx_formation_enrollments_lookup", columnList = "formation_id, artisan_id, status"),
                @Index(name = "idx_formation_enrollments_artisan", columnList = "artisan_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormationEnrollment extends BaseEntity {

    /**
     * Masterclass formation that the artisan is enrolled in.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false)
    private Formation formation;

    /**
     * Enrolled peer artisan attending the formation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    /**
     * Current status of this enrollment reservation.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.CONFIRMED;

    /**
     * Timestamp when the enrollment seat was initially confirmed.
     */
    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    /**
     * Timestamp when the enrollment was cancelled by the artisan, if applicable.
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
