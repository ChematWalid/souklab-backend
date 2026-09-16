package com.project.souklab.dao;

import com.project.souklab.model.ArtisanGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link ArtisanGalleryImage} entities.
 * Provides ordering, soft-delete filtering, and ownership-scoped queries.
 */
public interface ArtisanGalleryImageRepository extends JpaRepository<ArtisanGalleryImage, String> {

    /**
     * Retrieves all active gallery images for an artisan ordered by display sequence.
     *
     * @param artisanId the unique identifier of the artisan
     * @return list of active gallery images in ascending display order
     */
    List<ArtisanGalleryImage> findByArtisanIdAndDeletedAtIsNullOrderByDisplayOrderAsc(String artisanId);

    /**
     * Finds a specific gallery image by ID and owning artisan ID filtering out soft-deleted records.
     *
     * @param id the unique identifier of the gallery image
     * @param artisanId the unique identifier of the owning artisan
     * @return optional containing the gallery image if found, active, and owned by the artisan
     */
    Optional<ArtisanGalleryImage> findByIdAndArtisanIdAndDeletedAtIsNull(String id, String artisanId);

    /**
     * Counts the total number of active gallery images currently stored for an artisan.
     *
     * @param artisanId the unique identifier of the artisan
     * @return active gallery image count
     */
    long countByArtisanIdAndDeletedAtIsNull(String artisanId);

    Optional<ArtisanGalleryImage> findByImageUrlAndDeletedAtIsNull(String imageUrl);
}
