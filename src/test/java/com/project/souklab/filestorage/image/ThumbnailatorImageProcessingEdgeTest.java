package com.project.souklab.filestorage.image;

import com.project.souklab.filestorage.exception.UnsupportedImageFormatException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThumbnailatorImageProcessingEdgeTest {
    private final ThumbnailatorImageProcessingService service = new ThumbnailatorImageProcessingService();

    @Test
    void rejectsNullEmptyAndUnsupportedContentTypes() {
        assertThatThrownBy(() -> service.generateVariants((InputStream) null, "image/jpeg"))
                .isInstanceOf(UnsupportedImageFormatException.class);
        assertThatThrownBy(() -> service.generateVariants((byte[]) null, "image/jpeg"))
                .isInstanceOf(UnsupportedImageFormatException.class);
        assertThatThrownBy(() -> service.generateVariants(new byte[]{1}, null))
                .isInstanceOf(UnsupportedImageFormatException.class);
        assertThatThrownBy(() -> service.generateVariants(new byte[]{1}, " "))
                .isInstanceOf(UnsupportedImageFormatException.class);
        assertThatThrownBy(() -> service.generateVariants(new byte[]{1}, "image/gif"))
                .isInstanceOf(UnsupportedImageFormatException.class);
    }

    @Test
    void rejectsUnreadableAndUnsupportedImageStreams() {
        assertThatThrownBy(() -> service.generateVariants(new byte[]{1, 2, 3}, "image/jpeg"))
                .isInstanceOf(UnsupportedImageFormatException.class);
        InputStream noMark = new FilterInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3})) {
            @Override public boolean markSupported() { return false; }
        };
        assertThatThrownBy(() -> service.generateVariants(noMark, "image/jpeg"))
                .isInstanceOf(UnsupportedImageFormatException.class);
    }

    @Test
    void normalizesJpegParametersAndPreservesSmallImageDimensions() throws Exception {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", bytes);

        var variants = service.generateVariants(bytes.toByteArray(), "IMAGE/JPG; charset=binary");

        assertThat(variants).hasSize(ResolutionTier.values().length);
        assertThat(variants.values()).allSatisfy(variant -> {
            assertThat(variant.contentType()).isEqualTo("image/jpeg");
            assertThat(variant.width()).isEqualTo(3);
            assertThat(variant.height()).isEqualTo(2);
        });
    }

    @Test
    void wrapsImageReaderIoFailures() {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("image read failure");
            }
        };

        assertThatThrownBy(() -> service.generateVariants(broken, "image/png"))
                .isInstanceOf(UnsupportedImageFormatException.class)
                .hasMessageContaining("Corrupted or unreadable image stream");
    }
}
