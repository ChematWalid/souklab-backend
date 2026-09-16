package com.project.souklab.dto.feed;

import com.project.souklab.model.FeedPostMedia;
import lombok.Builder;
import lombok.Value;

/**
 * Public representation of a feed media attachment.
 */
@Value
@Builder
public class FeedPostMediaResponseDTO {
    String id;
    String url;
    String contentType;
    int displayOrder;

    /**
     * Maps an attachment using the configured URL resolver.
     *
     * @param media attachment
     * @return response DTO
     */
    public static FeedPostMediaResponseDTO from(FeedPostMedia media) {
        return FeedPostMediaResponseDTO.builder()
                .id(media.getId())
                .url(media.getStorageKey())
                .contentType(media.getContentType())
                .displayOrder(media.getDisplayOrder())
                .build();
    }
}
