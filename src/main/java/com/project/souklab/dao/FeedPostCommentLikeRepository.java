package com.project.souklab.dao;

import com.project.souklab.model.FeedPostCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedPostCommentLikeRepository extends JpaRepository<FeedPostCommentLike, String> {
    boolean existsByCommentIdAndUserId(String commentId, String userId);
    Optional<FeedPostCommentLike> findByCommentIdAndUserId(String commentId, String userId);
    long countByCommentId(String commentId);
}
