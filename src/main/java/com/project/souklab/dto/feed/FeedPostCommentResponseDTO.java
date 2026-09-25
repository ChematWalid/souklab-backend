package com.project.souklab.dto.feed;

import com.project.souklab.model.FeedPostComment;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class FeedPostCommentResponseDTO {
    String id;
    String postId;
    String parentId;
    String authorId;
    String authorName;
    String avatarUrl;
    String content;
    int likeCount;
    int replyCount;
    boolean likedByCurrentUser;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static FeedPostCommentResponseDTO from(FeedPostComment comment, boolean liked) {
        String name = comment.getUser().getName();
        return FeedPostCommentResponseDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .parentId(comment.getParent() == null ? null : comment.getParent().getId())
                .authorId(comment.getUser().getId())
                .authorName(name)
                .avatarUrl(comment.getUser().getAvatarUrl())
                .content(comment.getDeletedAt() == null ? comment.getContent() : "[Comment removed]")
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .likedByCurrentUser(liked)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
