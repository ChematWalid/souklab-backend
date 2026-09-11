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
 * Entity representing an authentic crafting material, mineral, or organic substance in Algeria.
 * Models discrete materials utilized by artisans in handcrafted goods (e.g., Argile Rouge de Kabylie,
 * Argent Massif 925, Cuir Tanné au Végétal, Laine Chaouie, Bois de Cèdre de l'Atlas) grouped under
 * a parent {@link MaterialFamily}.
 */
@Entity
@Table(
    name = "materials",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_materials_slug", columnNames = {"slug"})
    },
    indexes = {
        @Index(name = "idx_materials_family", columnList = "family_id, display_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material extends BaseEntity {

    /**
     * Parent material family grouping this raw substance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private MaterialFamily family;

    /**
     * Name of the raw crafting material (e.g., "Argile Rouge de Kabylie", "Argent Massif 925").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique URL-safe slug for filtering directory queries by material (e.g., "argile-rouge-de-kabylie").
     */
    @Column(nullable = false, length = 120)
    private String slug;

    /**
     * Physical properties, geographic provenance, and artisanal usage of the material.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Ordering index within the parent family for catalog presentation.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Status flag indicating whether this material is available for artisan selection and filtering.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
