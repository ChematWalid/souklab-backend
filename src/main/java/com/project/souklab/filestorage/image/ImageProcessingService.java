package com.project.souklab.filestorage.image;

import com.project.souklab.filestorage.validation.ValidatedFile;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * Provider-agnostic image processing service interface.
 * Generates resolution variants for uploaded images across defined resolution tiers,
 * preserving original aspect ratio and format without performing any upscaling.
 */
public interface ImageProcessingService {

    /**
     * Generates resolution variants for the given image input stream.
     *
     * @param content readable stream containing the source image bytes
     * @param contentType MIME content type of the image (e.g. image/jpeg, image/png)
     * @return an immutable map of ResolutionTier to ImageVariant
     * @throws UnsupportedImageFormatException if content type is unsupported or image decoding fails
     */
    Map<ResolutionTier, ImageVariant> generateVariants(InputStream content, String contentType);

    /**
     * Generates resolution variants for the given image byte array.
     *
     * @param bytes raw image bytes
     * @param contentType MIME content type of the image (e.g. image/jpeg, image/png)
     * @return an immutable map of ResolutionTier to ImageVariant
     * @throws UnsupportedImageFormatException if content type is unsupported or image decoding fails
     */
    Map<ResolutionTier, ImageVariant> generateVariants(byte[] bytes, String contentType);

    /**
     * Generates resolution variants for a pre-validated file.
     *
     * @param file validated file container
     * @return an immutable map of ResolutionTier to ImageVariant
     * @throws UnsupportedImageFormatException if content type is unsupported or image decoding fails
     */
    default Map<ResolutionTier, ImageVariant> generateVariants(ValidatedFile file) {
        Objects.requireNonNull(file, "ValidatedFile cannot be null");
        return generateVariants(file.content(), file.detectedMimeType());
    }
}
