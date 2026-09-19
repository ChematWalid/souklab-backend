package com.project.souklab.dao;
import java.time.LocalDateTime;


import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FeedPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Persistence operations for moderated community feed posts.
 */
public interface FeedPostRepository extends JpaRepository<FeedPost, String> {
    long countByStatusAndDeletedAtIsNull(FeedPostStatus status);
    long countByStatusAndCreatedAtBetweenAndDeletedAtIsNull(FeedPostStatus status,
                                                            LocalDateTime from, LocalDateTime to);
    long countByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime from, LocalDateTime to);

    /**
     * Lists published, non-deleted posts for public feed consumption.
     *
     * @param pageable page and sort configuration
     * @return visible feed posts
     */
    Page<FeedPost> findByStatusAndDeletedAtIsNull(FeedPostStatus status, Pageable pageable);

    /**
     * Lists published posts filtered by type.
     *
     * @param status visibility state
     * @param type post type
     * @param pageable page and sort configuration
     * @return matching feed posts
     */
    Page<FeedPost> findByStatusAndTypeAndDeletedAtIsNull(FeedPostStatus status, FeedPostType type, Pageable pageable);

    /**
     * Finds an active post by identifier.
     *
     * @param id post identifier
     * @return matching post
     */
    Optional<FeedPost> findByIdAndDeletedAtIsNull(String id);

    /**
     * Lists posts submitted by an author.
     *
     * @param authorId author identifier
     * @param pageable page and sort configuration
     * @return author's posts
     */
    Page<FeedPost> findByAuthorIdAndDeletedAtIsNull(String authorId, Pageable pageable);
}
