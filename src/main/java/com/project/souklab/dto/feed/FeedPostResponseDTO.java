package com.project.souklab.dto.feed;

import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostMedia;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public or moderation representation of a feed post.
 */
@Value
@Builder
public class FeedPostResponseDTO {
    String id;
    String authorId;
    String authorName;
    String type;
    String title;
    String body;
    String status;
    String formationId;
    LocalDateTime publishedAt;
    String moderationNote;
    List<FeedPostMediaResponseDTO> media;

    /**
     * Maps an entity to a response.
     *
     * @param post feed post
     * @return response DTO
     */
    public static FeedPostResponseDTO from(FeedPost post) {
        String authorName = java.util.stream.Stream.of(post.getAuthor().getFirstName(), post.getAuthor().getLastName())
                .filter(java.util.Objects::nonNull)
                .filter(value -> !value.isBlank())
                .reduce((left, right) -> left + " " + right)
                .orElse(post.getAuthor().getEmail());
        return FeedPostResponseDTO.builder()
                .id(post.getId())
                .authorId(post.getAuthor().getId())
                .authorName(authorName)
                .type(post.getType().name())
                .title(post.getTitle())
                .body(post.getBody())
                .status(post.getStatus().name())
                .formationId(post.getFormation() == null ? null : post.getFormation().getId())
                .publishedAt(post.getPublishedAt())
                .moderationNote(post.getModerationNote())
                .media(post.getMedia().stream().map(FeedPostMediaResponseDTO::from).toList())
                .build();
    }
}
