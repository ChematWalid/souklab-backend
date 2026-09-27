# Review controller

`ArtisanReviewController` lists visible reviews publicly and exposes attended-formation review creation, owner editing, and owner removal for artisans.

- `GET /api/v1/artisan/reviews/{reviewId}` returns a review only while it is published and not deleted.
- `GET /api/v1/artisan/formations/{formationId}/reviews/me` retrieves the authenticated artisan's existing review for their confirmed enrollment in that formation, or returns `null` if not yet reviewed.
- Reviews require the participant enrollment status to be `ATTENDED` and the parent formation status to be `COMPLETED`.

| Controller | Responsibility |
| --- | --- |
| [`ArtisanReviewController`](ArtisanReviewController.java) | Public review listing and authenticated artisan review lifecycle operations. |
