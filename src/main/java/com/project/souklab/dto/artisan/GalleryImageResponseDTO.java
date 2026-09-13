package com.project.souklab.dto.artisan;

import com.project.souklab.model.ArtisanGalleryImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representation of a portfolio showcase image in an artisan's gallery.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryImageResponseDTO {

    private String id;
    private String imageUrl;
    private String title;
    private String caption;
    private int displayOrder;

    /**
     * Maps an {@link ArtisanGalleryImage} entity into a response DTO.
     *
     * @param image the gallery image entity
     * @return response DTO, or null if image is null
     */
    public static GalleryImageResponseDTO from(ArtisanGalleryImage image) {
        if (image == null) {
            return null;
        }
        return GalleryImageResponseDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .title(image.getTitle())
                .caption(image.getCaption())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}
