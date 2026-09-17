# Feed services

`FeedPostService` enforces verified-artisan or administrator authorship, pending moderation, public visibility filtering, image validation/scanning, provider-neutral storage, and post-commit media cleanup.

| Service | Responsibility |
| --- | --- |
| [`FeedPostService`](FeedPostService.java) | Owns feed post lifecycle, moderation state, media validation, and storage cleanup. |
