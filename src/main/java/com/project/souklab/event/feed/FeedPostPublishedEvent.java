package com.project.souklab.event.feed;

import com.project.souklab.event.DomainEvent;
import com.project.souklab.model.FeedPostType;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a feed post is approved and published for the community.
 */
public record FeedPostPublishedEvent(
        String eventId,
        String postId,
        String authorId,
        FeedPostType postType,
        String title,
        Instant occurredAt
) implements DomainEvent {

    public static FeedPostPublishedEvent of(String postId, String authorId, FeedPostType postType, String title) {
        return new FeedPostPublishedEvent(
                UUID.randomUUID().toString(),
                postId,
                authorId,
                postType,
                title,
                Instant.now()
        );
    }
}
