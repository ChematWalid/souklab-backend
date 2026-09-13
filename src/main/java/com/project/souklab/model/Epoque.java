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

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

/**
 * Entity representing an Algerian historical epoch or cultural era in the heritage taxonomy.
 * Captures historical periods (e.g., Période Numide, Époque Rustumide, Fatimide, Zianide,
 * Ottomane, Contemporaine) whose motifs, geometry, and design canons inspire traditional
 * handcrafted artifacts.
 */
@Entity
@Table(
    name = "epoques",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_epoques_slug", columnNames = {"slug"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Epoque extends BaseEntity {

    /**
     * Name of the historical era or cultural epoch (e.g., "Période Zianide", "Époque Ottomane").
     */
    @FullTextField(analyzer = "artisanal_name")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique URL-friendly slug used for era-based heritage filtering and search indexing (e.g., "periode-zianide").
     */
    @KeywordField(normalizer = "artisanal_normalizer")
    @Column(nullable = false, length = 120)
    private String slug;

    /**
     * Historical date range or century span (e.g., "XVIe - XIXe siècle", "Antiquité").
     */
    @Column(name = "period_era", length = 100)
    private String periodEra;

    /**
     * Historical context, artistic styles, architectural motifs, and cultural legacy of the era.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Chronological or display sorting weight for listing historical periods.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Status flag indicating whether this historical epoch is active in catalog filters.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
