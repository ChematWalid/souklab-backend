package com.project.souklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a showcase photograph in an artisan's portfolio gallery.
 * Showcases workshop spaces, tools, works-in-progress, and finished authentic handicrafts.
 * Managed with soft-delete support to preserve audit trails.
 */
@Entity
@Table(
    name = "artisan_gallery_images",
    indexes = {
        @Index(name = "idx_gallery_artisan", columnList = "artisan_id, deleted_at, display_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtisanGalleryImage extends BaseEntity {

    /**
     * The artisan who owns and created this portfolio showcase image.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    /**
     * Accessible URL or storage path for the portfolio showcase image.
     */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * Optional headline or title for the showcased piece or workshop snapshot.
     */
    @Column(length = 255)
    private String title;

    /**
     * Optional narrative description detailing the creative process, materials, or cultural significance.
     */
    @Column(columnDefinition = "TEXT")
    private String caption;

    /**
     * Zero-indexed sequence weighting determining presentation order in the artisan's public showcase.
     */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}
