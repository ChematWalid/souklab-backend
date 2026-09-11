package com.project.souklab.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "artisans")
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
    private User user;

    /**
     * Biography, artistic philosophy, and craft background narrative.
     */
    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * Geographic region (Wilaya or Commune) where the artisan's workshop is established.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    /**
     * City, commune, or daïra where the artisan operates.
     */
    @Column(length = 100)
    private String city;

    /**
     * Workshop or studio physical street address.
     */
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
    private JobSubCategory subCategory;

    /**
     * Set of authentic materials utilized by this artisan in their handcrafted creations.
     * Mapped through the pure junction table {@code artisan_materials}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "artisan_materials",
        joinColumns = @JoinColumn(name = "artisan_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    @Builder.Default
    private Set<Material> materials = new HashSet<>();

    /**
     * Set of ancestral crafting techniques and traditional methods mastered by this artisan.
     * Mapped through the pure junction table {@code artisan_techniques}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "artisan_techniques",
        joinColumns = @JoinColumn(name = "artisan_id"),
        inverseJoinColumns = @JoinColumn(name = "technique_id")
    )
    @Builder.Default
    private Set<Technique> techniques = new HashSet<>();

    /**
     * Set of historical eras and cultural design epochs that inspire this artisan's creations.
     * Mapped through the pure junction table {@code artisan_epoques}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "artisan_epoques",
        joinColumns = @JoinColumn(name = "artisan_id"),
        inverseJoinColumns = @JoinColumn(name = "epoque_id")
    )
    @Builder.Default
    private Set<Epoque> epoques = new HashSet<>();

    /**
     * Showcase portfolio imagery representing the artisan's workshop and handcrafted masterpieces.
     * Cascaded and orphan-removed with the artisan lifecycle.
     */
    @OneToMany(mappedBy = "artisan", cascade = CascadeType.ALL, orphanRemoval = true)
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
    @Column(name = "is_teacher", nullable = false)
    @Builder.Default
    private boolean isTeacher = false;

    /**
     * Flag indicating whether the artisan holds an active premium tier subscription.
     */
    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private boolean isPremium = false;

    /**
     * Flag indicating whether the artisan has been verified by platform administrators.
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    /**
     * Aggregate review rating score (0.00 to 5.00).
     */
    @Column(nullable = false)
    @Builder.Default
    private double rating = 0.0;

    /**
     * Total number of verified client reviews received.
     */
    @Column(name = "reviews_count", nullable = false)
    @Builder.Default
    private int reviewsCount = 0;

    /**
     * Cumulative total number of profile page views.
     */
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
