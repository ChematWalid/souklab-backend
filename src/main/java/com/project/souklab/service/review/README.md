# Review services

`ArtisanReviewService` requires an attended completed formation, prevents self and duplicate reviews, supports owner edits/removal, and recalculates the reviewed artisan's decimal rating and count.

| Service | Responsibility |
| --- | --- |
| [`ArtisanReviewService`](ArtisanReviewService.java) | Owns review validation, persistence, removal, and rating recalculation. |
