# Feed services

The feed services enforce verified-artisan or administrator authorship, draft and moderation workflows, public discovery, normalized tags, image validation/scanning, provider-neutral storage, engagement uniqueness, atomic counters, comments/replies, bookmarks, following, typed notifications, and post-commit media cleanup.

| Service | Responsibility |
| --- | --- |
| [`FeedPostService`](FeedPostService.java) | Owns feed post lifecycle, moderation state, media validation, and storage cleanup. |
| [`FeedDiscoveryService`](FeedDiscoveryService.java) | Owns public filters, Hibernate Search text queries with relational fallback, sorting, and favorites-based following. |
| [`FeedEngagementService`](FeedEngagementService.java) | Owns post/comment likes, unlikes, bookmarks, comments, replies, shares, saved posts, and engagement notifications. |
