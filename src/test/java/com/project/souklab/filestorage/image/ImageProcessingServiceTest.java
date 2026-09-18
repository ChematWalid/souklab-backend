package com.project.souklab.filestorage.image;

import com.project.souklab.filestorage.validation.ValidatedFile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageProcessingServiceTest {

    @Test
    void validatedFileOverloadUsesItsContentAndDetectedType() {
        ImageProcessingService service = new ImageProcessingService() {
            @Override
            public Map<ResolutionTier, ImageVariant> generateVariants(java.io.InputStream content, String contentType) {
                assertThat(contentType).isEqualTo("image/png");
                return Map.of();
            }

            @Override
            public Map<ResolutionTier, ImageVariant> generateVariants(byte[] bytes, String contentType) {
                return Map.of();
            }
        };

        assertThat(service.generateVariants(new ValidatedFile(
                new ByteArrayInputStream(new byte[]{1}), "x.png", "image/png", 1))).isEmpty();
    }

    @Test
    void validatedFileOverloadRejectsNull() {
        ImageProcessingService service = new ImageProcessingService() {
            @Override public Map<ResolutionTier, ImageVariant> generateVariants(java.io.InputStream c, String t) { return Map.of(); }
            @Override public Map<ResolutionTier, ImageVariant> generateVariants(byte[] b, String t) { return Map.of(); }
        };

        assertThatThrownBy(() -> service.generateVariants((ValidatedFile) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ValidatedFile cannot be null");
    }
}
