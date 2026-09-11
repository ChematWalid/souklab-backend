package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Entity representing a specialized artisanal craft subcategory or trade in Algeria.
 * Models discrete craftsmanship specializations (e.g., Céramique & Poterie de Kabylie,
 * Ébénisterie Traditionnelle, Dinanderie de Constantine, Bijoux Kabyles en Argent, Tapis du M'zab)
 * grouped under a parent {@link JobCategory}.
 */
@Entity
@Table(
    name = "job_sub_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_job_sub_categories_slug", columnNames = {"slug"})
    },
    indexes = {
        @Index(name = "idx_subcat_category", columnList = "category_id, display_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSubCategory extends BaseEntity {

    /**
     * Parent craft category providing broad classification for this trade specialization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private JobCategory category;

    /**
     * Specific trade or craft subcategory name (e.g., "Ébénisterie Traditionnelle", "Dinanderie").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique URL-friendly slug used in directory route filtering (e.g., "ebenisterie-traditionnelle").
     */
    @Column(nullable = false, length = 120)
    private String slug;

    /**
     * Detailed overview of the trade, techniques, and cultural heritage background.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Sorting priority for displaying subcategories within the parent category.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Status flag indicating whether this trade specialization is currently active and selectable.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
