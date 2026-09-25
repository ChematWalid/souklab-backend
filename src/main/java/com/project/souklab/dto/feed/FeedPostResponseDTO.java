package com.project.souklab.dto.feed;

import java.util.Objects;
import java.util.stream.Stream;

import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostMedia;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FeedPostType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * Public or moderation representation of a feed post.
 */
@Value
@Builder(toBuilder = true)
public class FeedPostResponseDTO {
    String id;
    String authorId;
    String authorName;
    FeedPostType type;
    String title;
    String body;
    FeedPostStatus status;
    String formationId;
    LocalDateTime publishedAt;
    String moderationNote;
    List<FeedPostMediaResponseDTO> media;
    List<String> tags;
    int likeCount;
    int commentCount;
    int bookmarkCount;
    int shareCount;
    boolean likedByCurrentUser;
    boolean bookmarkedByCurrentUser;

    /**
     * Maps an entity to a response.
     *
     * @param post feed post
     * @return response DTO
     */
    public static FeedPostResponseDTO from(FeedPost post) {
        return from(post, Function.identity());
    }

    /**
     * Maps an entity to a response using a storage-key URL resolver.
     *
     * @param post feed post
     * @param urlResolver resolver for attachment keys
     * @return response DTO
     */
    public static FeedPostResponseDTO from(FeedPost post, Function<String, String> urlResolver) {
        String authorName = Stream.of(post.getAuthor().getFirstName(), post.getAuthor().getLastName())
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .reduce((left, right) -> left + " " + right)
                .orElse(post.getAuthor().getEmail());
        return FeedPostResponseDTO.builder()
                .id(post.getId())
                .authorId(post.getAuthor().getId())
                .authorName(authorName)
                .type(post.getType())
                .title(post.getTitle())
                .body(post.getBody())
                .status(post.getStatus())
                .formationId(post.getFormation() == null ? null : post.getFormation().getId())
                .publishedAt(post.getPublishedAt())
                .moderationNote(post.getModerationNote())
                .tags(post.getTags().stream().map(tag -> tag.getName()).toList())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .bookmarkCount(post.getBookmarkCount())
                .shareCount(post.getShareCount())
                .media(post.getMedia().stream().map(media -> FeedPostMediaResponseDTO.builder()
                        .id(media.getId())
                        .url(urlResolver.apply(media.getStorageKey()))
                        .contentType(media.getContentType())
                        .displayOrder(media.getDisplayOrder())
                        .build()).toList())
                .build();
    }
}
