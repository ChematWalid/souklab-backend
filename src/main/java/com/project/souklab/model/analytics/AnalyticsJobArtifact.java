package com.project.souklab.model.analytics;

import com.project.souklab.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_job_artifacts", indexes = @Index(name = "idx_analytics_artifact_job", columnList = "job_id,created_at"))
@Getter @Setter @NoArgsConstructor
public class AnalyticsJobArtifact extends BaseEntity {
    @Column(name = "job_id", nullable = false, length = 36) private String jobId;
    @Column(name = "storage_key", nullable = false, length = 500) private String storageKey;
    @Column(name = "content_type", nullable = false, length = 120) private String contentType;
    @Column(name = "file_name", nullable = false, length = 180) private String fileName;
    @Column(nullable = false) private long size;
    @Column(name = "sha256", nullable = false, length = 64) private String sha256;
    @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
}
