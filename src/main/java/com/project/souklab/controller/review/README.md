# Review controller

`ArtisanReviewController` lists visible reviews publicly and exposes attended-formation review creation, owner editing, and owner removal for artisans.

`GET /api/v1/artisan/reviews/{reviewId}` returns a review only while it is published and not deleted.

| Controller | Responsibility |
| --- | --- |
| [`ArtisanReviewController`](ArtisanReviewController.java) | Public review listing and authenticated artisan review lifecycle operations. |
