package com.project.souklab.dto.feed;

import com.project.souklab.model.FeedPostMedia;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedPostMediaResponseDTOTest {

    @Test
    void mapsAttachmentMetadata() {
        FeedPostMedia media = FeedPostMedia.builder()
                .storageKey("posts/image.jpg")
                .contentType("image/jpeg")
                .fileSize(42)
                .displayOrder(3)
                .build();

        FeedPostMediaResponseDTO result = FeedPostMediaResponseDTO.from(media);

        assertThat(result.getId()).isNull();
        assertThat(result.getUrl()).isEqualTo("posts/image.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        assertThat(result.getDisplayOrder()).isEqualTo(3);
    }
}
