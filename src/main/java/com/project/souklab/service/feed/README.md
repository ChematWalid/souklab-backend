# Feed services

The feed services enforce verified-artisan or administrator authorship, draft and moderation workflows, public discovery, normalized tags, image validation/scanning, provider-neutral storage, engagement uniqueness, atomic counters, comments/replies, bookmarks, following, typed notifications, and post-commit media cleanup.

To ensure high performance under load:
- `FeedPostMapper` utilizes batch fetching for media attachments and author details, preventing N+1 database queries when rendering feed pages.
- View counting utilizes direct atomic database updates (`incrementViewCountNative`) instead of pessimistic row locking, preventing transaction bottlenecks on viral posts.

| Service | Responsibility |
| --- | --- |
| [`FeedPostService`](FeedPostService.java) | Owns feed post lifecycle, moderation state, single post moderation inspection, media validation, and storage cleanup. |
| [`FeedDiscoveryService`](FeedDiscoveryService.java) | Owns public filters, Hibernate Search text queries with relational fallback, sorting, and favorites-based following. |
| [`FeedEngagementService`](FeedEngagementService.java) | Owns post/comment likes, unlikes, bookmarks, comments, replies, shares, saved posts, and engagement notifications. |
| [`FeedPrivacyService`](FeedPrivacyService.java) | Applies premium-aware identity masking (`Artisan #XXXXX`) to feed posts and comments for non-premium viewers. |
