# Artisan Service Package (`com.project.souklab.service.artisan`)

Business logic for artisan public profiles, contact details gating, deduplicated impression metrics, professional credentials, and portfolio showcase galleries.

---

## Key Capabilities

- **Contact Info Gating**: Public views of artisan profiles mask contact details (phone, email, website, physical address) unless the viewer is an administrator, the artisan themselves, or an active client with a premium subscription.
- **Impression Tracking**: Tracks profile visits in `ArtisanProfileView`, ensuring view counts only increment once per unique viewer-artisan pair.
- **Portfolio Credentials (`ArtisanCertificationService`)**: Manages the upload, verification, storage, listing, single-item retrieval, and soft deletion of official artisan certificates with ClamAV stream scanning.
- **Showcase Gallery (`ArtisanGalleryService`)**: Manages multi-image portfolio uploads (enforcing the configured 20-image quota per artisan), single-image retrieval, display sequence reordering, and soft deletion.
- **Clean Architecture Refactoring**: Delegates caller security context resolution to `ArtisanSecurityUtils` and storage URL generation to `FileUrlResolver`.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`ArtisanProfileService`](ArtisanProfileService.java) | Manages public profile retrieval, contact masking, view metrics, and profile updates. |
| [`ArtisanCertificationService`](ArtisanCertificationService.java) | Handles official certification document uploads with ClamAV scanning, listing, and deletion. |
| [`ArtisanGalleryService`](ArtisanGalleryService.java) | Handles showcase portfolio image uploads, configured 20-photo quota enforcement, ordering, and deletion. |
