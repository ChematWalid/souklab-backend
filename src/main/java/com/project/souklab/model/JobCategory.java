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

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a top-level craft category in the Algerian craftsmanship taxonomy.
 * Categorises broad artisanal disciplines such as Métiers du Bois, Métiers de la Terre & Céramique,
 * Métiers des Métaux & Bijouterie, Textile & Tissage, and Cuir & Peausserie.
 */
@Entity
@Table(
    name = "job_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_job_categories_slug", columnNames = {"slug"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCategory extends BaseEntity {

    /**
     * Official display name of the craftsmanship domain (e.g., "Métiers du Bois", "Artisanat d'Art").
     */
    @FullTextField(analyzer = "artisanal_name")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique URL-friendly slug used for catalog routing and directory filtering (e.g., "metiers-du-bois").
     */
    @KeywordField(normalizer = "artisanal_normalizer")
    @Column(nullable = false, length = 120)
    private String slug;

    /**
     * Comprehensive description of the craftsmanship domain and cultural scope.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Asset URL or icon identifier representing this craft category in client interfaces.
     */
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /**
     * Priority weight used to sort categories in directory navigation menus and listings.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * Status flag indicating whether this category is actively visible in public directories.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /**
     * Child craft subcategories classifying specialized traditional trades under this category domain.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobSubCategory> subCategories = new ArrayList<>();
}
