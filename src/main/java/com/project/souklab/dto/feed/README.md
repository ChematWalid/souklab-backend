# Feed DTOs

Request and response contracts for moderated feed posts, normalized tags, engagement, comments, replies, bookmarks, shares, image attachments, and administrator moderation notes.

| Class | Responsibility |
| --- | --- |
| [`FeedPostCreateDTO`](FeedPostCreateDTO.java) | Validated post creation input. |
| [`FeedPostResponseDTO`](FeedPostResponseDTO.java) | Public and authenticated post representation. |
| [`FeedPostMediaResponseDTO`](FeedPostMediaResponseDTO.java) | Ordered media attachment representation. |
| [`FeedPostModerationDTO`](FeedPostModerationDTO.java) | Administrator moderation decision input. |
| [`FeedPostLikeStatusDTO`](FeedPostLikeStatusDTO.java) | Like count and current-user like state. |
| [`FeedPostCommentCreateDTO`](FeedPostCommentCreateDTO.java) | Root comment or one-level reply input. |
| [`FeedPostCommentResponseDTO`](FeedPostCommentResponseDTO.java) | Comment/reply representation with counters and like state. |
| [`FeedShareResponseDTO`](FeedShareResponseDTO.java) | Relative share path and post summary. |
