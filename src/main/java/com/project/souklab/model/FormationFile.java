package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Entity representing an attachment or course file associated with a formation.
 * Access is restricted to confirmed enrolled artisans and the authoring instructor.
 */
@Entity
@Table(
        name = "formation_files",
        indexes = {
                @Index(name = "idx_formation_files_formation", columnList = "formation_id, deleted_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormationFile extends BaseEntity {

    /**
     * Parent formation that this course file belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false)
    private Formation formation;

    /**
     * Storage reference or S3 object key identifying the physical file.
     */
    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    /**
     * Original filename as uploaded by the instructor artisan.
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * MIME media type of the course attachment (e.g. application/pdf, image/png).
     */
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    /**
     * Size of the uploaded file payload in bytes.
     */
    @Column(name = "file_size", nullable = false)
    private long fileSize;
}
