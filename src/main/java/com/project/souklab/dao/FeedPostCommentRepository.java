package com.project.souklab.dao;

import com.project.souklab.model.FeedPostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeedPostCommentRepository extends JpaRepository<FeedPostComment, String> {
    @Modifying
    @Query("update FeedPostComment c set c.likeCount = c.likeCount + 1 where c.id = :id")
    int incrementLikeCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPostComment c set c.likeCount = case when c.likeCount > 0 then c.likeCount - 1 else 0 end where c.id = :id")
    int decrementLikeCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPostComment c set c.replyCount = c.replyCount + 1 where c.id = :id")
    int incrementReplyCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPostComment c set c.replyCount = case when c.replyCount > 0 then c.replyCount - 1 else 0 end where c.id = :id")
    int decrementReplyCount(@Param("id") String id);
    Page<FeedPostComment> findByPostIdAndParentIsNullAndDeletedAtIsNull(String postId, Pageable pageable);
    Page<FeedPostComment> findByParentIdAndDeletedAtIsNull(String parentId, Pageable pageable);
    Optional<FeedPostComment> findByIdAndDeletedAtIsNull(String id);
}
