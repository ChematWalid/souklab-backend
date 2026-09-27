package com.project.souklab.dao;

import com.project.souklab.model.FeedPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FeedPostLikeRepository extends JpaRepository<FeedPostLike, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);
    Optional<FeedPostLike> findByPostIdAndUserId(String postId, String userId);
    long countByPostId(String postId);
    Page<FeedPostLike> findByPostId(String postId, Pageable pageable);
}
