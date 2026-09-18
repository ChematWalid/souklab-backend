package com.project.souklab.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extended profile entity for artisans, master craftspersons, and heritage practitioners.
 * Manages physical workshop location, craft trade categorization, heritage taxonomy associations,
 * showcase portfolios, and official accreditations.
 */
@Entity
@Table(
    name = "artisans",
    indexes = {
        @Index(name = "idx_artisan_dir_search", columnList = "deleted_at, is_verified, is_premium, rating DESC, sub_category_id, region_id"),
        @Index(name = "idx_artisan_region", columnList = "region_id"),
        @Index(name = "idx_artisan_subcat", columnList = "sub_category_id"),
        @Index(name = "idx_artisan_teacher", columnList = "is_teacher, deleted_at"),
        @Index(name = "idx_artisan_rating", columnList = "rating DESC")
    }
)
@NamedEntityGraph(
    name = "artisan.directory",
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode(value = "region", subgraph = "region.parent"),
        @NamedAttributeNode(value = "subCategory", subgraph = "subCategory.category"),
        @NamedAttributeNode("materials"),
        @NamedAttributeNode("techniques"),
        @NamedAttributeNode("galleryImages")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "region.parent",
            attributeNodes = @NamedAttributeNode("parent")
        ),
        @NamedSubgraph(
            name = "subCategory.category",
            attributeNodes = @NamedAttributeNode("category")
        )
    }
)
@Indexed(index = "artisans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artisan {

    /**
     * Unique identifier matching the owning {@link User} entity identifier.
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * Owning user account sharing the primary key via foreign key constraint.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    @IndexedEmbedded(includePaths = {"name", "firstName", "lastName", "avatarUrl"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private User user;

    /**
     * Biography, artistic philosophy, and craft background narrative.
     */
    @FullTextField(analyzer = "artisanal_text")
    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * Geographic region (Wilaya or Commune) where the artisan's workshop is established.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    @IndexedEmbedded(includePaths = {"name", "slug", "code", "parent.name", "parent.slug", "parent.code"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private Region region;

    /**
     * City, commune, or daïra where the artisan operates.
     */
    @FullTextField(analyzer = "artisanal_name")
    @KeywordField(name = "city_keyword", normalizer = "artisanal_normalizer")
    @Column(length = 100)
    private String city;

    /**
     * Workshop or studio physical street address.
     */
    @FullTextField(analyzer = "artisanal_text")
    @Column(length = 255)
    private String address;

    /**
     * External website or brand showcase URL.
     */
    @Column(length = 255)
    private String website;

    /**
     * Primary specialized craft subcategory or trade practiced by this artisan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    @IndexedEmbedded(includePaths = {"name", "slug", "category.name", "category.slug"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private JobSubCategory subCategory;

    /**
     * Set of authentic materials utilized by this artisan in their handcrafted creations.
     * Mapped through the pure junction table {@code artisan_materials}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @JoinTable(
        name = "artisan_materials",
        joinColumns = @JoinColumn(name = "artisan_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    @Builder.Default
    @IndexedEmbedded(includePaths = {"name", "slug"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private Set<Material> materials = new HashSet<>();

    /**
     * Set of ancestral crafting techniques and traditional methods mastered by this artisan.
     * Mapped through the pure junction table {@code artisan_techniques}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @JoinTable(
        name = "artisan_techniques",
        joinColumns = @JoinColumn(name = "artisan_id"),
        inverseJoinColumns = @JoinColumn(name = "technique_id")
    )
    @Builder.Default
    @IndexedEmbedded(includePaths = {"name", "slug"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private Set<Technique> techniques = new HashSet<>();

    /**
     * Set of historical eras and cultural design epochs that inspire this artisan's creations.
     * Mapped through the pure junction table {@code artisan_epoques}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @JoinTable(
        name = "artisan_epoques",
        joinColumns = @JoinColumn(name = "artisan_id"),
        inverseJoinColumns = @JoinColumn(name = "epoque_id")
    )
    @Builder.Default
    @IndexedEmbedded(includePaths = {"name", "slug"})
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private Set<Epoque> epoques = new HashSet<>();

    /**
     * Showcase portfolio imagery representing the artisan's workshop and handcrafted masterpieces.
     * Cascaded and orphan-removed with the artisan lifecycle.
     */
    @OneToMany(mappedBy = "artisan", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<ArtisanGalleryImage> galleryImages = new ArrayList<>();

    /**
     * Official professional certifications (CAM artisan cards, diplomas, accreditations).
     * Cascaded and orphan-removed with the artisan lifecycle.
     */
    @OneToMany(mappedBy = "artisan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ArtisanCertification> certifications = new ArrayList<>();

    /**
     * Flag indicating whether the artisan has been approved as an instructor (formateur).
     */
    @GenericField
    @Column(name = "is_teacher", nullable = false)
    @Builder.Default
    private boolean isTeacher = false;

    /**
     * Flag indicating whether the artisan holds an active premium tier subscription.
     */
    @GenericField
    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private boolean isPremium = false;

    /**
     * Flag indicating whether the artisan has been verified by platform administrators.
     */
    @GenericField
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    /**
     * Aggregate review rating score (0.00 to 5.00).
     */
    @GenericField(sortable = Sortable.YES)
    @Column(nullable = false)
    @Builder.Default
    private double rating = 0.0;

    /**
     * Total number of verified client reviews received.
     */
    @GenericField(sortable = Sortable.YES)
    @Column(name = "reviews_count", nullable = false)
    @Builder.Default
    private int reviewsCount = 0;

    /**
     * Cumulative total number of profile page views.
     */
    @GenericField(sortable = Sortable.YES)
    @Column(name = "views_count", nullable = false)
    @Builder.Default
    private int viewsCount = 0;

    /**
     * Message responsiveness rate percentage (0 to 100).
     */
    @Column(name = "response_rate", nullable = false)
    @Builder.Default
    private int responseRate = 0;

    /**
     * Timestamp when the profile was initially created.
     */
    @GenericField(sortable = Sortable.YES)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the profile was last modified.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft-delete timestamp for archived artisan profiles.
     */
    @GenericField
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Resolves the unique identifier of the artisan's geographic region if assigned.
     *
     * @return 36-character UUID string or null if unassigned
     */
    public String getRegionId() {
        return region != null ? region.getId() : null;
    }

    /**
     * Backward-compatible setter for regionId.
     *
     * @param regionId the unique identifier of the region or null to unset
     */
    public void setRegionId(String regionId) {
        if (regionId != null) {
            Region r = Region.builder().build();
            r.setId(regionId);
            this.region = r;
        } else {
            this.region = null;
        }
    }

    /**
     * Resolves the unique identifier of the artisan's craft subcategory if assigned.
     *
     * @return 36-character UUID string or null if unassigned
     */
    public String getSubCategoryId() {
        return subCategory != null ? subCategory.getId() : null;
    }

    /**
     * Backward-compatible setter for subCategoryId.
     *
     * @param subCategoryId the unique identifier of the subcategory or null to unset
     */
    public void setSubCategoryId(String subCategoryId) {
        if (subCategoryId != null) {
            JobSubCategory sc = JobSubCategory.builder().build();
            sc.setId(subCategoryId);
            this.subCategory = sc;
        } else {
            this.subCategory = null;
        }
    }

    /**
     * Custom builder extensions supporting backward-compatible string-based ID setters.
     */
    public static class ArtisanBuilder {

        /**
         * Backward-compatible builder helper for setting region via string ID.
         *
         * @param regionId region identifier
         * @return this builder
         */
        public ArtisanBuilder regionId(String regionId) {
            if (regionId != null) {
                Region r = Region.builder().build();
                r.setId(regionId);
                this.region = r;
            } else {
                this.region = null;
            }
            return this;
        }

        /**
         * Backward-compatible builder helper for setting subcategory via string ID.
         *
         * @param subCategoryId subcategory identifier
         * @return this builder
         */
        public ArtisanBuilder subCategoryId(String subCategoryId) {
            if (subCategoryId != null) {
                JobSubCategory sc = JobSubCategory.builder().build();
                sc.setId(subCategoryId);
                this.subCategory = sc;
            } else {
                this.subCategory = null;
            }
            return this;
        }
    }
}
