package com.project.souklab.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Moderated community feed post authored by an active artisan or administrator.
 */
@Entity
@Indexed
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
    @FullTextField(analyzer = "artisanal_name")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @FullTextField(analyzer = "artisanal_name")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    @GenericField
    private FeedPostStatus status = FeedPostStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @Column(name = "published_at")
    @GenericField(sortable = org.hibernate.search.engine.backend.types.Sortable.YES)
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by")
    private User moderatedBy;

    @Column(name = "moderation_note", columnDefinition = "TEXT")
    private String moderationNote;

    @Column(name = "like_count", nullable = false)
    @GenericField(sortable = org.hibernate.search.engine.backend.types.Sortable.YES)
    @Builder.Default
    private int likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private int commentCount = 0;

    @Column(name = "bookmark_count", nullable = false)
    @Builder.Default
    private int bookmarkCount = 0;

    @Column(name = "share_count", nullable = false)
    @Builder.Default
    private int shareCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "feed_post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @IndexedEmbedded
    @Builder.Default
    private List<FeedTag> tags = new ArrayList<>();

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
