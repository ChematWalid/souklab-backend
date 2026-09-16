package com.project.souklab.filestorage.image;

import com.project.souklab.filestorage.exception.UnsupportedImageFormatException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Thumbnailator-based implementation of {@link ImageProcessingService}.
 * Resizes images across configured resolution tiers preserving original aspect ratio and format.
 *
 * <p><strong>No Upscaling Policy:</strong>
 * If the source image's longest side is already less than or equal to a tier's maximum dimension cap,
 * the variant is generated at its native size without upscaling or omitting the tier.
 * Every tier always produces an output variant; some outputs may match source dimensions when small.
 *
 * <p><strong>Memory and Buffering Strategy:</strong>
 * Each variant is buffered fully in memory as byte arrays. Given the project's configured file size
 * limits (avatar and profile scale, default 10MB) and resolution caps (at most 2000px on the longest side),
 * in-memory buffering provides predictable, transient heap allocation without disk spillover,
 * temporary file leakage, or native library dependencies.
 */
@Service
@Slf4j
public class ThumbnailatorImageProcessingService implements ImageProcessingService {

    private static final String MIME_IMAGE_JPEG = "image/jpeg";
    private static final String MIME_IMAGE_JPG = "image/jpg";
    private static final String MIME_IMAGE_PNG = "image/png";
    private static final String FORMAT_JPEG = "jpeg";
    private static final String FORMAT_PNG = "png";
    private static final long MAX_SOURCE_IMAGE_PIXELS = 40_000_000L;

    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            MIME_IMAGE_JPEG,
            MIME_IMAGE_JPG,
            MIME_IMAGE_PNG
    );

    @Override
    public Map<ResolutionTier, ImageVariant> generateVariants(InputStream content, String contentType) {
        if (content == null) {
            throw new UnsupportedImageFormatException("Image content stream cannot be null", null);
        }
        validateContentType(contentType);

        BufferedImage sourceImage = readSourceImage(content, contentType);

        return processVariants(sourceImage, normalizeFormat(contentType), normalizeContentType(contentType));
    }

    @Override
    public Map<ResolutionTier, ImageVariant> generateVariants(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new UnsupportedImageFormatException("Image bytes cannot be null or empty", null);
        }
        return generateVariants(new ByteArrayInputStream(bytes), contentType);
    }

    private Map<ResolutionTier, ImageVariant> processVariants(BufferedImage sourceImage, String format, String normalizedContentType) {
        int srcWidth = sourceImage.getWidth();
        int srcHeight = sourceImage.getHeight();
        int longestSide = Math.max(srcWidth, srcHeight);

        Map<ResolutionTier, ImageVariant> variants = new EnumMap<>(ResolutionTier.class);

        for (ResolutionTier tier : ResolutionTier.values()) {
            int targetWidth;
            int targetHeight;

            if (longestSide <= tier.getMaxDimension()) {
                targetWidth = srcWidth;
                targetHeight = srcHeight;
            } else {
                double ratio = (double) tier.getMaxDimension() / longestSide;
                targetWidth = Math.max(1, (int) Math.round(srcWidth * ratio));
                targetHeight = Math.max(1, (int) Math.round(srcHeight * ratio));
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                Thumbnails.of(sourceImage)
                        .size(targetWidth, targetHeight)
                        .outputFormat(format)
                        .toOutputStream(outputStream);
            } catch (Exception e) {
                log.error("Failed to generate variant for tier {} with format {}", tier, format, e);
                throw new UnsupportedImageFormatException("Failed to process image variant for tier " + tier, e);
            }

            byte[] variantBytes = outputStream.toByteArray();
            variants.put(tier, new ImageVariant(tier, variantBytes, targetWidth, targetHeight, normalizedContentType));
        }

        return Collections.unmodifiableMap(variants);
    }

    private BufferedImage readSourceImage(InputStream content, String contentType) {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(content)) {
            if (imageInput == null) {
                throw new UnsupportedImageFormatException("Could not decode image from stream", null);
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new UnsupportedImageFormatException("Corrupted or unreadable image stream for content type: " + contentType);
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_SOURCE_IMAGE_PIXELS) {
                    throw new UnsupportedImageFormatException("Image dimensions exceed the supported processing limit", null);
                }
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new UnsupportedImageFormatException("Corrupted or unreadable image stream for content type: " + contentType);
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            log.error("Failed to read image stream for content type {}", contentType, e);
            throw new UnsupportedImageFormatException("Could not decode image from stream", e);
        }
    }

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new UnsupportedImageFormatException("Content-Type must not be null or blank");
        }
        String cleanType = contentType.split(";")[0].trim().toLowerCase();
        if (!SUPPORTED_IMAGE_TYPES.contains(cleanType)) {
            throw new UnsupportedImageFormatException(contentType);
        }
    }

    private String normalizeFormat(String contentType) {
        String cleanType = contentType.split(";")[0].trim().toLowerCase();
        if (cleanType.equals(MIME_IMAGE_JPEG) || cleanType.equals(MIME_IMAGE_JPG)) {
            return FORMAT_JPEG;
        }
        if (cleanType.equals(MIME_IMAGE_PNG)) {
            return FORMAT_PNG;
        }
        throw new UnsupportedImageFormatException(contentType);
    }

    private String normalizeContentType(String contentType) {
        String cleanType = contentType.split(";")[0].trim().toLowerCase();
        if (cleanType.equals(MIME_IMAGE_JPEG) || cleanType.equals(MIME_IMAGE_JPG)) {
            return MIME_IMAGE_JPEG;
        }
        return MIME_IMAGE_PNG;
    }
}
