package com.project.souklab.dao;

import com.project.souklab.model.FeedPostBookmark;
import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeedPostBookmarkRepository extends JpaRepository<FeedPostBookmark, String> {
    boolean existsByPostIdAndUserId(String postId, String userId);
    Optional<FeedPostBookmark> findByPostIdAndUserId(String postId, String userId);

    @Query("select b.post from FeedPostBookmark b where b.user.id = :userId " +
            "and b.post.status = :status and b.post.deletedAt is null and b.deletedAt is null")
    Page<FeedPost> findSavedPosts(@Param("userId") String userId,
                                  @Param("status") FeedPostStatus status,
                                  Pageable pageable);
}
