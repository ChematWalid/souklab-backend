package com.project.souklab.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Moderated community feed post authored by an active artisan or administrator.
 */
@Entity
@Table(name = "feed_posts", indexes = {
        @Index(name = "idx_feed_posts_public", columnList = "status, deleted_at, published_at"),
        @Index(name = "idx_feed_posts_author", columnList = "author_id, status, deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeedPostType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private FeedPostStatus status = FeedPostStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FeedPostMedia> media = new ArrayList<>();

    /**
     * Adds media while maintaining both sides of the relationship.
     *
     * @param attachment media attachment
     */
    public void addMedia(FeedPostMedia attachment) {
        media.add(attachment);
        attachment.setPost(this);
    }

    /**
     * Removes media while maintaining both sides of the relationship.
     *
     * @param attachment media attachment
     */
    public void removeMedia(FeedPostMedia attachment) {
        media.remove(attachment);
        attachment.setPost(null);
    }
}
