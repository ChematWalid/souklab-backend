# Review services

`ArtisanReviewService` requires an attended completed formation, prevents self and duplicate reviews, supports owner edits/removal, and recalculates the reviewed artisan's decimal rating and count.

When an artisan re-submits a review for a completed formation where a soft-deleted review already exists for that enrollment, the service reactivates the existing review (`deletedAt = null`) with the new rating and comment. This preserves database integrity and prevents duplicate key conflicts on `uk_artisan_review_enrollment`.

| Service | Responsibility |
| --- | --- |
| [`ArtisanReviewService`](ArtisanReviewService.java) | Owns review validation, persistence, reactivation, removal, and rating recalculation. |
