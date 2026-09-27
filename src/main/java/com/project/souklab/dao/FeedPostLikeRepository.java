package com.project.souklab.dao;

import com.project.souklab.model.FeedPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedPostLikeRepository extends JpaRepository<FeedPostLike, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);
    Optional<FeedPostLike> findByPostIdAndUserId(String postId, String userId);
    long countByPostId(String postId);
    Page<FeedPostLike> findByPostId(String postId, Pageable pageable);

    @Query("select l.post.id from FeedPostLike l where l.user.id = :userId and l.post.id in :postIds")
    Set<String> findLikedPostIdsByUserIdAndPostIdIn(@Param("userId") String userId, @Param("postIds") Collection<String> postIds);
}
