package com.project.souklab.filestorage.image;

import com.project.souklab.model.EnumValue;
import lombok.Getter;

/**
 * Resolution tiers for processed image variants.
 * Each tier defines a maximum dimension cap (longest side in pixels).
 * Original aspect ratio is preserved across all tiers, and no upscaling is performed.
 */
@Getter
public enum ResolutionTier implements EnumValue {

    /**
     * Thumbnail tier: maximum 150px on the longest side.
     */
    THUMBNAIL(ResolutionTier.THUMBNAIL_MAX_DIMENSION),

    /**
     * Medium tier: maximum 500px on the longest side.
     */
    MEDIUM(ResolutionTier.MEDIUM_MAX_DIMENSION),

    /**
     * Original tier: maximum 2000px on the longest side.
     */
    ORIGINAL(ResolutionTier.ORIGINAL_MAX_DIMENSION);

    /**
     * Named constant for thumbnail tier maximum dimension cap (150px).
     */
    public static final int THUMBNAIL_MAX_DIMENSION = 150;

    /**
     * Named constant for medium tier maximum dimension cap (500px).
     */
    public static final int MEDIUM_MAX_DIMENSION = 500;

    /**
     * Named constant for original tier maximum dimension cap (2000px).
     */
    public static final int ORIGINAL_MAX_DIMENSION = 2000;

    private final int maxDimension;

    ResolutionTier(int maxDimension) {
        this.maxDimension = maxDimension;
    }
}
