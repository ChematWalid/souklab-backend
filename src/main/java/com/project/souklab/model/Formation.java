package com.project.souklab.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an artisan-to-artisan masterclass or formation session.
 */
@Entity
@Table(
        name = "formations",
        indexes = {
                @Index(name = "idx_formations_browse", columnList = "status, scheduled_at, deleted_at"),
                @Index(name = "idx_formations_author", columnList = "author_id, status, deleted_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formation extends BaseEntity {

    /**
     * The verified instructor artisan authoring and delivering this formation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Artisan author;

    /**
     * Title or headline of the formation masterclass.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Detailed narrative, learning objectives, and curriculum description.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    /**
     * Publicly viewable showcase thumbnail image URL.
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * In-person workshop address or venue location, if not purely online.
     */
    @Column
    private String location;

    /**
     * Flag indicating whether the masterclass is conducted remotely online.
     */
    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private boolean isOnline = false;

    /**
     * Scheduled start timestamp for the live or in-person masterclass session.
     */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    /**
     * Total planned duration of the training in hours.
     */
    @Column(name = "duration_hours", nullable = false)
    @Builder.Default
    private int durationHours = 0;

    /**
     * Maximum number of participant seats available for enrollment.
     */
    @Column(name = "max_participants", nullable = false)
    @Builder.Default
    private int maxParticipants = 0;

    /**
     * Admission fee per enrolled artisan in major currency units.
     */
    @Column(nullable = false)
    @Builder.Default
    private int price = 0;

    /**
     * ISO standard currency code for the admission price.
     */
    @Column(length = 10, nullable = false)
    @Builder.Default
    private String currency = "DZD";

    /**
     * Publication and moderation lifecycle status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private FormationStatus status = FormationStatus.DRAFT;

    /**
     * Syllabus attachments and protected course files accessible to enrolled participants.
     */
    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FormationFile> files = new ArrayList<>();

    /**
     * Peer artisan participant enrollment reservations.
     */
    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FormationEnrollment> enrollments = new ArrayList<>();

    /**
     * Administrative review and moderation records.
     */
    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FormationReview> reviews = new ArrayList<>();

    /**
     * Calculates the count of active, confirmed participant enrollments.
     *
     * @return count of enrollments with CONFIRMED status and not soft-deleted
     */
    public long getActiveEnrollmentsCount() {
        if (enrollments == null) {
            return 0L;
        }
        return enrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.CONFIRMED && enrollment.getDeletedAt() == null)
                .count();
    }

    /**
     * Associates a course attachment file with this formation.
     *
     * @param file the formation file to add
     */
    public void addFile(FormationFile file) {
        files.add(file);
        file.setFormation(this);
    }

    /**
     * Dissociates and removes a course attachment file from this formation.
     *
     * @param file the formation file to remove
     */
    public void removeFile(FormationFile file) {
        files.remove(file);
        file.setFormation(null);
    }

    /**
     * Associates a participant enrollment with this formation.
     *
     * @param enrollment the enrollment to associate
     */
    public void addEnrollment(FormationEnrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setFormation(this);
    }

    /**
     * Associates an administrative review with this formation.
     *
     * @param review the review to associate
     */
    public void addReview(FormationReview review) {
        reviews.add(review);
        review.setFormation(this);
    }
}
