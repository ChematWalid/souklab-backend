package com.project.souklab.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a raw material family in the Algerian craftsmanship taxonomy.
 * Groups authentic raw materials and natural substances (e.g., Terres & Argiles,
 * Métaux & Alliages, Bois & Dérivés, Fibres Végétales & Textiles, Cuirs & Peaux)
 * historically used across Algerian artisan traditions.
 */
@Entity
@Table(
    name = "material_families",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_material_families_slug", columnNames = {"slug"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialFamily extends BaseEntity {

    /**
     * Name of the raw material family (e.g., "Terres & Argiles", "Métaux & Alliages").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique URL slug for material family filtering and navigation (e.g., "terres-et-argiles").
     */
    @Column(nullable = false, length = 120)
    private String slug;

    /**
     * Technical and contextual description of this family of substances.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Display order weight for ordering material families in catalog browsers and selectors.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Status flag indicating whether this material family is active in the reference taxonomy.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /**
     * Specific raw materials belonging to this family (e.g., Argile rouge, Kaolin, Argent 925).
     */
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Material> materials = new ArrayList<>();
}
