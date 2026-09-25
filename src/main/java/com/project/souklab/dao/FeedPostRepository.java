package com.project.souklab.dao;
import java.time.LocalDateTime;


import com.project.souklab.model.FeedPost;
import com.project.souklab.model.FeedPostStatus;
import com.project.souklab.model.FeedPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Persistence operations for moderated community feed posts.
 */
public interface FeedPostRepository extends JpaRepository<FeedPost, String> {
    @Query("select distinct p from FeedPost p left join p.tags t " +
            "where p.status = :status and p.deletedAt is null " +
            "and (:type is null or p.type = :type) " +
            "and (:authorId is null or p.author.id = :authorId) " +
            "and (:tag is null or t.slug = :tag) " +
            "and (:query is null or lower(p.title) like lower(concat('%', :query, '%')) " +
            "or lower(p.body) like lower(concat('%', :query, '%')) " +
            "or lower(t.name) like lower(concat('%', :query, '%')))")
    Page<FeedPost> findForDiscovery(@Param("status") FeedPostStatus status,
                                    @Param("type") FeedPostType type,
                                    @Param("authorId") String authorId,
                                    @Param("tag") String tag,
                                    @Param("query") String query,
                                    Pageable pageable);

    @Query("select distinct p from FeedPost p join ClientFavoriteArtisan f on f.artisan.id = p.author.artisan.id " +
            "where f.client.id = :clientId and f.deletedAt is null and p.status = :status and p.deletedAt is null")
    Page<FeedPost> findFollowing(@Param("clientId") String clientId,
                                 @Param("status") FeedPostStatus status,
                                 Pageable pageable);

    @Modifying
    @Query("update FeedPost p set p.likeCount = p.likeCount + 1 where p.id = :id")
    int incrementLikeCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPost p set p.likeCount = case when p.likeCount > 0 then p.likeCount - 1 else 0 end where p.id = :id")
    int decrementLikeCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPost p set p.bookmarkCount = p.bookmarkCount + 1 where p.id = :id")
    int incrementBookmarkCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPost p set p.bookmarkCount = case when p.bookmarkCount > 0 then p.bookmarkCount - 1 else 0 end where p.id = :id")
    int decrementBookmarkCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPost p set p.shareCount = p.shareCount + 1 where p.id = :id")
    int incrementShareCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPost p set p.commentCount = p.commentCount + 1 where p.id = :id")
    int incrementCommentCount(@Param("id") String id);

    @Modifying
    @Query("update FeedPost p set p.commentCount = case when p.commentCount > 0 then p.commentCount - 1 else 0 end where p.id = :id")
    int decrementCommentCount(@Param("id") String id);
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

    Page<FeedPost> findByAuthorIdAndStatusAndDeletedAtIsNull(String authorId, FeedPostStatus status, Pageable pageable);
}
