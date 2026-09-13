package com.project.souklab.filestorage;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.UnsupportedFileTypeException;
import com.project.souklab.filestorage.exception.UnsupportedImageFormatException;
import com.project.souklab.filestorage.image.ImageProcessingService;
import com.project.souklab.filestorage.image.ImageVariant;
import com.project.souklab.filestorage.image.ResolutionTier;
import com.project.souklab.filestorage.image.ThumbnailatorImageProcessingService;
import com.project.souklab.filestorage.validation.FileValidator;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verification test suite for Phase D.0c Image Processing &amp; Variant Generation.
 * Verifies resolution tiers (V1), no upscaling policy (V2), WebP rejection at validation layer (V3),
 * graceful non-image handling (V4), and magic-byte format preservation (V5).
 */
class ImageProcessingVerificationTest {

    private ImageProcessingService imageProcessingService;
    private FileValidator fileValidator;
    private Tika tika;
    private Path outputDir;

    /**
     * Minimal valid WebP binary payload (164 bytes, VP8 encoding, 200x200).
     */
    private static final String VALID_WEBP_HEX =
            "524946469c000000574542505650382090000000500c009d012ac800c8003e9148a14d25a42322204800b01209696ee176b11b40141a"
            + "3d6db45c20c821aaa4d76da2e106410d5526bb6d170832086aa935db68b84190435549aedb45c20c821aaa4d76da2e106410d5526"
            + "bb6d170832086aa935db68b84190435549aeda80000feff9c20bffff7381fffb381fffb381fc75fff876e6c8b792efe27df8100000000000000";

    /**
     * Sets up test services, Tika detector, FileValidator with updated production allowed types,
     * and ensures the artifact output directory exists.
     */
    @BeforeEach
    void setUp() throws IOException {
        imageProcessingService = new ThumbnailatorImageProcessingService();
        tika = new Tika();

        StorageProperties properties = new StorageProperties();
        properties.getValidation().setMaxFileSize(DataSize.ofMegabytes(10));
        properties.getValidation().setAllowedMimeTypes(List.of(
                "image/jpeg",
                "image/png",
                "application/pdf"
        ));
        fileValidator = new FileValidator(properties, tika);

        outputDir = Paths.get("target", "test-image-variants");
        Files.createDirectories(outputDir);
    }

    /**
     * V1: Feed a real JPEG larger than 2000px on its longest side through generateVariants().
     * Confirms 3 outputs returned with actual measured dimensions:
     * thumbnail longest side == 150, medium longest side == 500, original longest side == 2000.
     * Confirms aspect ratio is preserved for all three tiers.
     * Saves all 3 to disk and verifies via ImageIO that they are valid, non-corrupt images.
     */
    @Test
    @DisplayName("V1: Large JPEG (>2000px) -> 3 variants with exact caps (150, 500, 2000), aspect ratio preserved, non-corrupt on disk")
    void v1_largeJpeg_shouldGenerateThreeAspectPreservedVariants() throws IOException {
        int srcWidth = 2400;
        int srcHeight = 1600;
        double srcRatio = (double) srcWidth / srcHeight;

        byte[] jpegBytes = createTestImageBytes(srcWidth, srcHeight, "jpeg", Color.BLUE, Color.ORANGE);

        Map<ResolutionTier, ImageVariant> variants = imageProcessingService.generateVariants(jpegBytes, "image/jpeg");

        assertThat(variants).hasSize(3).containsKeys(
                ResolutionTier.THUMBNAIL,
                ResolutionTier.MEDIUM,
                ResolutionTier.ORIGINAL
        );

        ImageVariant thumbnail = variants.get(ResolutionTier.THUMBNAIL);
        assertThat(thumbnail.width()).isEqualTo(150);
        assertThat(thumbnail.height()).isEqualTo(100);
        assertThat(Math.max(thumbnail.width(), thumbnail.height())).isEqualTo(ResolutionTier.THUMBNAIL_MAX_DIMENSION);
        double thumbRatio = (double) thumbnail.width() / thumbnail.height();
        assertThat(Math.abs(thumbRatio - srcRatio)).isLessThan(0.01);

        ImageVariant medium = variants.get(ResolutionTier.MEDIUM);
        assertThat(medium.width()).isEqualTo(500);
        assertThat(medium.height()).isEqualTo(333);
        assertThat(Math.max(medium.width(), medium.height())).isEqualTo(ResolutionTier.MEDIUM_MAX_DIMENSION);
        double mediumRatio = (double) medium.width() / medium.height();
        assertThat(Math.abs(mediumRatio - srcRatio)).isLessThan(0.01);

        ImageVariant original = variants.get(ResolutionTier.ORIGINAL);
        assertThat(original.width()).isEqualTo(2000);
        assertThat(original.height()).isEqualTo(1333);
        assertThat(Math.max(original.width(), original.height())).isEqualTo(ResolutionTier.ORIGINAL_MAX_DIMENSION);
        double origRatio = (double) original.width() / original.height();
        assertThat(Math.abs(origRatio - srcRatio)).isLessThan(0.01);

        Path thumbPath = outputDir.resolve("v1_thumbnail.jpg");
        Path mediumPath = outputDir.resolve("v1_medium.jpg");
        Path origPath = outputDir.resolve("v1_original.jpg");

        writeBytesToFile(thumbPath, thumbnail.bytes());
        writeBytesToFile(mediumPath, medium.bytes());
        writeBytesToFile(origPath, original.bytes());

        BufferedImage readThumb = ImageIO.read(thumbPath.toFile());
        assertThat(readThumb).isNotNull();
        assertThat(readThumb.getWidth()).isEqualTo(150);
        assertThat(readThumb.getHeight()).isEqualTo(100);

        BufferedImage readMedium = ImageIO.read(mediumPath.toFile());
        assertThat(readMedium).isNotNull();
        assertThat(readMedium.getWidth()).isEqualTo(500);
        assertThat(readMedium.getHeight()).isEqualTo(333);

        BufferedImage readOrig = ImageIO.read(origPath.toFile());
        assertThat(readOrig).isNotNull();
        assertThat(readOrig.getWidth()).isEqualTo(2000);
        assertThat(readOrig.getHeight()).isEqualTo(1333);

        System.out.printf("=== V1 LARGE JPEG VERIFICATION ===%n");
        System.out.printf("Source: %dx%d (ratio: %.4f)%n", srcWidth, srcHeight, srcRatio);
        System.out.printf("Thumbnail: %dx%d (ratio: %.4f, file: %s, size: %d bytes)%n",
                readThumb.getWidth(), readThumb.getHeight(), thumbRatio, thumbPath, thumbnail.getSize());
        System.out.printf("Medium: %dx%d (ratio: %.4f, file: %s, size: %d bytes)%n",
                readMedium.getWidth(), readMedium.getHeight(), mediumRatio, mediumPath, medium.getSize());
        System.out.printf("Original: %dx%d (ratio: %.4f, file: %s, size: %d bytes)%n",
                readOrig.getWidth(), readOrig.getHeight(), origRatio, origPath, original.getSize());
    }

    /**
     * V2: Feed a small PNG (100x80, already under every tier's cap) through generateVariants().
     * Confirms all 3 outputs retain the exact same dimensions as the source (100x80),
     * proving no upscaling occurred at any tier.
     */
    @Test
    @DisplayName("V2: Small PNG (100x80) -> all 3 variants retain native dimensions (no upscaling)")
    void v2_smallPng_shouldNotUpscaleAtAnyTier() throws IOException {
        int srcWidth = 100;
        int srcHeight = 80;

        byte[] pngBytes = createTestImageBytes(srcWidth, srcHeight, "png", Color.RED, Color.GREEN);

        Map<ResolutionTier, ImageVariant> variants = imageProcessingService.generateVariants(pngBytes, "image/png");

        assertThat(variants).hasSize(3);

        for (ResolutionTier tier : ResolutionTier.values()) {
            ImageVariant variant = variants.get(tier);
            assertThat(variant).isNotNull();
            assertThat(variant.width())
                    .as("Tier %s width must not upscale and must match native width", tier)
                    .isEqualTo(100);
            assertThat(variant.height())
                    .as("Tier %s height must not upscale and must match native height", tier)
                    .isEqualTo(80);
            assertThat(variant.contentType()).isEqualTo("image/png");

            Path diskPath = outputDir.resolve("v2_" + tier.name().toLowerCase() + ".png");
            writeBytesToFile(diskPath, variant.bytes());
            BufferedImage diskImage = ImageIO.read(diskPath.toFile());
            assertThat(diskImage).isNotNull();
            assertThat(diskImage.getWidth()).isEqualTo(100);
            assertThat(diskImage.getHeight()).isEqualTo(80);
        }

        System.out.printf("=== V2 SMALL PNG NO-UPSCALING VERIFICATION ===%n");
        System.out.printf("Source: %dx%d PNG%n", srcWidth, srcHeight);
        System.out.printf("Thumbnail: %dx%d%n", variants.get(ResolutionTier.THUMBNAIL).width(), variants.get(ResolutionTier.THUMBNAIL).height());
        System.out.printf("Medium: %dx%d%n", variants.get(ResolutionTier.MEDIUM).width(), variants.get(ResolutionTier.MEDIUM).height());
        System.out.printf("Original: %dx%d%n", variants.get(ResolutionTier.ORIGINAL).width(), variants.get(ResolutionTier.ORIGINAL).height());
    }

    /**
     * V3 (Adjusted): Confirms FileValidator rejects WebP uploads with UnsupportedFileTypeException
     * before image processing is ever invoked, proving WebP is rejected at the validation layer.
     */
    @Test
    @DisplayName("V3: WebP upload rejected by FileValidator with UnsupportedFileTypeException before image processing")
    void v3_webpUpload_shouldBeRejectedByValidatorBeforeImageProcessing() {
        byte[] webpBytes = HexFormat.of().parseHex(VALID_WEBP_HEX);

        assertThatThrownBy(() -> fileValidator.validateAndSanitize(webpBytes, "avatar.webp", "image/webp"))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining("image/webp");

        assertThatThrownBy(() -> fileValidator.validateAndSanitize(webpBytes, "avatar.jpg", "image/jpeg"))
                .isInstanceOf(UnsupportedFileTypeException.class);

        System.out.println("=== V3 WEBP REJECTION VERIFICATION ===");
        System.out.println("Confirmed: WebP is rejected at the FileValidator layer with UnsupportedFileTypeException.");
    }

    /**
     * V4: Feed a non-image content type (application/pdf) to generateVariants().
     * Confirms that UnsupportedImageFormatException is thrown rather than raw Thumbnailator exceptions.
     */
    @Test
    @DisplayName("V4: Non-image content type (application/pdf) throws UnsupportedImageFormatException")
    void v4_nonImageContentType_shouldThrowUnsupportedImageFormatException() {
        byte[] dummyPdfBytes = "%PDF-1.4 dummy pdf content".getBytes();

        assertThatThrownBy(() -> imageProcessingService.generateVariants(dummyPdfBytes, "application/pdf"))
                .isInstanceOf(UnsupportedImageFormatException.class)
                .hasMessageContaining("application/pdf");

        assertThatThrownBy(() -> imageProcessingService.generateVariants(dummyPdfBytes, "text/plain"))
                .isInstanceOf(UnsupportedImageFormatException.class);

        assertThatThrownBy(() -> imageProcessingService.generateVariants(new byte[0], "image/jpeg"))
                .isInstanceOf(UnsupportedImageFormatException.class);

        System.out.println("=== V4 NON-IMAGE CONTENT TYPE VERIFICATION ===");
        System.out.println("Confirmed: UnsupportedImageFormatException thrown for non-image types and invalid streams.");
    }

    /**
     * V5: Confirms format preservation explicitly:
     * A source JPEG produces 3 variants whose magic bytes are verified by Tika as image/jpeg.
     * A source PNG produces 3 variants whose magic bytes are verified by Tika as image/png.
     */
    @Test
    @DisplayName("V5: Format preservation verified via magic bytes (JPEG remains JPEG, PNG remains PNG at all tiers)")
    void v5_formatPreservation_shouldRetainSourceFormatAcrossAllTiers() {
        byte[] jpegBytes = createTestImageBytes(400, 300, "jpeg", Color.BLUE, Color.WHITE);
        Map<ResolutionTier, ImageVariant> jpegVariants = imageProcessingService.generateVariants(jpegBytes, "image/jpeg");

        for (ResolutionTier tier : ResolutionTier.values()) {
            ImageVariant variant = jpegVariants.get(tier);
            String sniffedMime = tika.detect(variant.bytes());
            assertThat(sniffedMime)
                    .as("Tier %s magic bytes must be sniffed as image/jpeg", tier)
                    .isEqualTo("image/jpeg");
            assertThat(variant.contentType()).isEqualTo("image/jpeg");
        }

        byte[] pngBytes = createTestImageBytes(400, 300, "png", Color.MAGENTA, Color.CYAN);
        Map<ResolutionTier, ImageVariant> pngVariants = imageProcessingService.generateVariants(pngBytes, "image/png");

        for (ResolutionTier tier : ResolutionTier.values()) {
            ImageVariant variant = pngVariants.get(tier);
            String sniffedMime = tika.detect(variant.bytes());
            assertThat(sniffedMime)
                    .as("Tier %s magic bytes must be sniffed as image/png", tier)
                    .isEqualTo("image/png");
            assertThat(variant.contentType()).isEqualTo("image/png");
        }

        System.out.println("=== V5 FORMAT PRESERVATION VERIFICATION ===");
        System.out.println("Confirmed: JPEG variants sniffed as image/jpeg, PNG variants sniffed as image/png.");
    }

    /**
     * Verifies that sub-pixel rounding error never exceeds 0.5 pixels (well below 1px threshold)
     * even for extreme aspect ratios (e.g. 3000x400 panoramic image, aspect ratio 7.5).
     */
    @Test
    @DisplayName("Rounding Sanity: Extreme aspect ratio (3000x400, ratio 7.5) -> pixel error never exceeds 0.5px across all tiers")
    void extremeAspectRatio_roundingErrorNeverExceedsHalfPixel() {
        int srcWidth = 3000;
        int srcHeight = 400;
        double srcRatio = (double) srcWidth / srcHeight;

        byte[] jpegBytes = createTestImageBytes(srcWidth, srcHeight, "jpeg", Color.DARK_GRAY, Color.YELLOW);
        Map<ResolutionTier, ImageVariant> variants = imageProcessingService.generateVariants(jpegBytes, "image/jpeg");

        assertThat(variants).hasSize(3);

        System.out.println("=== EXTREME ASPECT RATIO (3000x400, ratio 7.5000) ROUNDING AUDIT ===");

        for (ResolutionTier tier : ResolutionTier.values()) {
            ImageVariant variant = variants.get(tier);
            double scale = (double) tier.getMaxDimension() / Math.max(srcWidth, srcHeight);
            double exactW = srcWidth * scale;
            double exactH = srcHeight * scale;

            double errorX = Math.abs(variant.width() - exactW);
            double errorY = Math.abs(variant.height() - exactH);

            System.out.printf("Tier %s (cap %d): measured %dx%d, exact mathematical %.4fx%.4f, error=(%.4f px, %.4f px), ratio=%.4f%n",
                    tier, tier.getMaxDimension(), variant.width(), variant.height(), exactW, exactH, errorX, errorY, (double) variant.width() / variant.height());

            assertThat(errorX)
                    .as("Width rounding error for tier %s must be <= 0.5px", tier)
                    .isLessThanOrEqualTo(0.5);
            assertThat(errorY)
                    .as("Height rounding error for tier %s must be <= 0.5px", tier)
                    .isLessThanOrEqualTo(0.5);
        }
    }

    /**
     * Guards against degenerate 0 or negative dimensions in ImageVariant compact constructor.
     */
    @Test
    @DisplayName("ImageVariant Guards: Zero or negative dimensions throw IllegalArgumentException")
    void imageVariant_dimensionGuards_shouldRejectZeroOrNegativeDimensions() {
        byte[] dummyBytes = new byte[] { 1, 2, 3 };

        assertThatThrownBy(() -> new ImageVariant(ResolutionTier.THUMBNAIL, dummyBytes, 0, 100, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image width must be strictly positive");

        assertThatThrownBy(() -> new ImageVariant(ResolutionTier.THUMBNAIL, dummyBytes, -10, 100, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image width must be strictly positive");

        assertThatThrownBy(() -> new ImageVariant(ResolutionTier.THUMBNAIL, dummyBytes, 100, 0, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image height must be strictly positive");

        assertThatThrownBy(() -> new ImageVariant(ResolutionTier.THUMBNAIL, dummyBytes, 100, -5, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image height must be strictly positive");

        System.out.println("=== IMAGE VARIANT DIMENSION GUARDS VERIFIED ===");
    }

    private byte[] createTestImageBytes(int width, int height, String format, Color startColor, Color endColor) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(startColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(endColor);
        g2d.drawLine(0, 0, width, height);
        g2d.drawLine(0, height, width, 0);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, format, baos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate test image fixture", e);
        }
        return baos.toByteArray();
    }

    private void writeBytesToFile(Path path, byte[] bytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(bytes);
        }
    }
}
