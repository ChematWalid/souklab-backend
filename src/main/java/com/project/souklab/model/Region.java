package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an Algerian administrative geographic region (Wilaya or Commune).
 * Organised in a self-referencing tree hierarchy to model the 58 official Algerian Wilayas
 * and their constituent Daïras and Communes, enabling regional craft provenance and geolocation
 * discovery for artisans across Algeria.
 */
@Entity
@Table(
    name = "regions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_regions_slug", columnNames = {"slug"})
    },
    indexes = {
        @Index(name = "idx_regions_parent", columnList = "parent_id, display_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region extends BaseEntity {

    /**
     * Official name of the Wilaya or Commune in French/Arabic (e.g., "Tizi Ouzou", "Ghardaïa", "Alger").
     */
    @FullTextField(analyzer = "artisanal_name")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique URL-friendly slug used for SEO routing and directory filtering (e.g., "tizi-ouzou", "ghardaia").
     */
    @KeywordField(normalizer = "artisanal_normalizer")
    @Column(nullable = false, length = 120)
    private String slug;

    /**
     * Official administrative Wilaya code (e.g., "15", "47", "16"). Nullable for sub-regional Communes.
     */
    @KeywordField
    @Column(length = 10)
    private String code;

    /**
     * Ordering weight for consistent sorting and display in regional dropdowns and public directories.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Status flag indicating whether this region is active for artisan assignment and catalog discovery.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /**
     * Parent administrative region in the hierarchy (e.g., the Wilaya governing this Commune, or null for a top-level Wilaya).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @IndexedEmbedded(includePaths = {"name", "slug", "code"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private Region parent;

    /**
     * Subordinate administrative divisions (e.g., Communes or Daïras within this Wilaya).
     */
    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<Region> children = new ArrayList<>();
}
