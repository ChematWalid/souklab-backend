# Artisan Controller Package (`com.project.souklab.controller.artisan`)

HTTP adapters for artisan public profiles, professional qualification certifications, and portfolio showcase galleries.

> [!NOTE]
> **Profile Updates**: Updating an artisan's own bio, address, website, or crafts is handled via the unified [`PATCH /api/v1/auth/me`](../auth/README.md#2-patch-apiv1authme--partial-profile-update) endpoint.

---

## Endpoints

### 1. Profile Management (`ArtisanController`)

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/artisan/{id}` | Authenticated | Get artisan profile | Retrieves public artisan profile with dynamic contact information privacy gating and view count tracking. |

#### Privacy Gating Rules
- **Non-Premium Viewers**: `name` is masked to `"Artisan #XXXXX"`, `phone`, `address`, and `website` are `null`, and `contactInfoLocked: true`.
- **Premium Viewers, Administrators, or Self**: Returns unmasked name and full contact details (`contactInfoLocked: false`).

---

### 2. Professional Credentials (`ArtisanCertificationController`)

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/artisan/certifications` | `artisan:content` | Upload certification | Uploads and records an official qualification or certification document (PDF/image, multipart). |
| `GET` | `/api/v1/artisan/certifications` | `artisan:content` | List certifications | Lists all certifications belonging to the authenticated artisan. |
| `GET` | `/api/v1/artisan/certifications/{id}` | `artisan:content` | Get certification | Retrieves a single certification belonging to the authenticated artisan by ID. |
| `PUT` | `/api/v1/artisan/certifications/{id}` | `artisan:content` | Update certification | Multipart metadata/file update; resets verification and safely replaces the stored file. |
| `DELETE` | `/api/v1/artisan/certifications/{id}` | `artisan:content` | Delete certification | Soft-deletes a certification document by ID. |

#### Upload Details (`multipart/form-data`)
- Form fields:
  - `file`: MultipartFile (PDF, JPEG, PNG, max 10MB)
  - `title`: String (e.g., "Diplôme National d'Artisanat")
  - `issuingOrganization`: String (e.g., "Chambre des Métiers de Tizi Ouzou")
  - `issueDate`: ISO Date string (`YYYY-MM-DD`)
  - `expiryDate`: Optional ISO Date string (`YYYY-MM-DD`)

---

### 3. Showcase Gallery (`ArtisanGalleryController`)

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/artisan/gallery` | `artisan:content` | Upload gallery image | Uploads a portfolio showcase photograph (multipart, max 20 images quota). |
| `GET` | `/api/v1/artisan/gallery` | `artisan:content` | List gallery images | Retrieves all active portfolio gallery images ordered by sequence. |
| `GET` | `/api/v1/artisan/gallery/{id}` | `artisan:content` | Get gallery image | Retrieves a single portfolio showcase photograph belonging to the authenticated artisan by ID. |
| `PUT` | `/api/v1/artisan/gallery/{id}` | `artisan:content` | Update gallery image | Multipart metadata update with optional image replacement. |
| `PUT` | `/api/v1/artisan/gallery/order` | `artisan:content` | Reorder gallery images | Updates the sequential presentation order of portfolio images. |
| `DELETE` | `/api/v1/artisan/gallery/{id}` | `artisan:content` | Delete gallery image | Soft-deletes a portfolio showcase photograph. |

#### Reorder Payload (`PUT /api/v1/artisan/gallery/order`)
```json
{
  "imageIds": [
    "img-uuid-3",
    "img-uuid-1",
    "img-uuid-2"
  ]
}
```

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`ArtisanController`](ArtisanController.java) | REST controller handling public profile view `/api/v1/artisan/{id}` with contact gating. |
| [`ArtisanCertificationController`](ArtisanCertificationController.java) | REST controller handling qualification credential uploads, listing, and deletion. |
| [`ArtisanGalleryController`](ArtisanGalleryController.java) | REST controller managing portfolio showcase uploads, ordering, and deletion. |
