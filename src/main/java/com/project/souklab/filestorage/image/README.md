# Image Processing Package (`com.project.souklab.filestorage.image`)

Image transformation, aspect-ratio preservation, and multi-tier thumbnail generation.

---

## Resolution Tiers

| Tier | Dimensions | Quality | Usage |
| :--- | :---: | :---: | :--- |
| `THUMBNAIL` | 150 x 150 px | 85% | Avatar thumbnails, comment author icons, notification badges. |
| `MEDIUM` | 500 x 500 px | 85% | Profile headers, user gallery previews. |
| `ORIGINAL` | Original (max 2000px) | 90% | High-resolution portfolio showcase. |

---

## Classes Reference

| Class / Enum | Type | Responsibility |
| :--- | :---: | :--- |
| [`ImageProcessingService`](ImageProcessingService.java) | Interface | Contract for variant generation from an input file stream. |
| [`ThumbnailatorImageProcessingService`](ThumbnailatorImageProcessingService.java) | Service Class | Production implementation using Thumbnailator library for memory-efficient image resizing. |
| [`ResolutionTier`](ResolutionTier.java) | `enum` | Catalog of defined resolutions (`THUMBNAIL`, `MEDIUM`, `FULL`). |
| [`ImageVariant`](ImageVariant.java) | Record | Container holding transformed byte array, content type, and dimensions for a specific tier. |
