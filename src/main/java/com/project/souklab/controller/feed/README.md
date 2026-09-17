# Feed controllers

`FeedPostController` serves published public posts and authenticated author operations. `AdminFeedController` exposes the pending queue and publish, hide, and remove moderation actions under `/api/v1/admin/feed/**`.

| Controller | Responsibility |
| --- | --- |
| [`FeedPostController`](FeedPostController.java) | Public feed browsing and authenticated post creation, editing, and deletion. |
| [`AdminFeedController`](AdminFeedController.java) | Permission-protected feed moderation. |
