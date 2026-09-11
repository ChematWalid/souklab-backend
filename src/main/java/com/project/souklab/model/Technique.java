package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an ancestral crafting technique, method, or artisanal skill in Algeria.
 * Models traditional methods (e.g., Filigrane en argent, Ciselure au marteau, Émaillage traditionnel,
 * Tissage au métier vertical, Repoussé sur cuir, Tournage sur bois) practiced by Algerian artisans.
 */
@Entity
@Table(
    name = "techniques",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_techniques_slug", columnNames = {"slug"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technique extends BaseEntity {

    /**
     * Name of the artisanal crafting technique (e.g., "Filigrane en argent", "Ciselure au marteau").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique URL-friendly slug for technique filtering and search indexing (e.g., "filigrane-en-argent").
     */
    @Column(nullable = false, length = 120)
    private String slug;

    /**
     * Technical description of the method, required artisanal tools, and craftsmanship steps.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Sort priority for technique listings in catalog filters and artisan profile portfolios.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Status flag indicating whether this technique is active in reference selection menus.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
