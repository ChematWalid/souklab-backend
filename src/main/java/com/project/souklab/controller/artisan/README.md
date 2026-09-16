# Artisan Controller Package (`com.project.souklab.controller.artisan`)

Handles HTTP endpoints for artisan public profile discovery, self-service profile management, professional certifications, and portfolio showcase gallery operations.

---

## Endpoints

### Profile Management (`ArtisanController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/artisans/{artisanId}` | Authenticated | Retrieves public view of an artisan profile with contact info gating and view tracking. |
| `PATCH` | `/api/v1/artisan/profile` | `permission:artisan:content` | Partial updates to bio, address, website, craft subcategories, and techniques. |

### Professional Credentials (`ArtisanCertificationController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/artisan/certifications` | `permission:artisan:content` | Uploads and records an official qualification or certification document (multipart, scanned when enabled). |
| `GET` | `/api/v1/artisan/certifications` | `permission:artisan:content` | Lists all certifications recorded for the authenticated artisan. |
| `DELETE` | `/api/v1/artisan/certifications/{id}` | `permission:artisan:content` | Soft-deletes a certification document belonging to the authenticated artisan. |

### Showcase Gallery (`ArtisanGalleryController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/artisan/gallery` | `permission:artisan:content` | Uploads a portfolio showcase photograph (multipart, max 10 images quota, scanned when enabled). |
| `GET` | `/api/v1/artisan/gallery` | `permission:artisan:content` | Retrieves all active portfolio gallery images ordered by display sequence. |
| `PUT` | `/api/v1/artisan/gallery/order` | `permission:artisan:content` | Updates the sequential presentation order of portfolio images. |
| `DELETE` | `/api/v1/artisan/gallery/{id}` | `permission:artisan:content` | Soft-deletes a portfolio showcase photograph. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`ArtisanController`](ArtisanController.java) | REST controller mapping `/api/v1/artisans` and `/api/v1/artisan/profile`. Delegates to `ArtisanProfileService`. |
| [`ArtisanCertificationController`](ArtisanCertificationController.java) | REST controller handling artisan credential uploads, listing, and deletion via `ArtisanCertificationService`. |
| [`ArtisanGalleryController`](ArtisanGalleryController.java) | REST controller managing portfolio showcase uploads, reordering, and deletion via `ArtisanGalleryService`. |
