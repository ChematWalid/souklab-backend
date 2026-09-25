# REST & Realtime API Specification

All endpoints are versioned with the `/api/v1` prefix. Standard response envelopes and HTTP status codes are consistently applied across the platform.

---

## 1. Response Envelopes & Error Handling

### Standard Response Format
```json
{
  "success": true,
  "code": 200,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Paginated Response Format
```json
{
  "success": true,
  "code": 200,
  "message": "Data retrieved successfully",
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 85,
    "totalPages": 5,
    "last": false
  }
}
```

### Error Response Formats

#### Business Exception Error (`AppException` subclasses)
```json
{
  "success": false,
  "code": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "User not found with id: 123",
  "data": null
}
```

#### Field Validation Error (`422 Unprocessable Entity`)
```json
{
  "success": false,
  "code": 422,
  "message": "Validation failed",
  "data": null,
  "errors": {
    "email": "Email is required",
    "password": "Password must be at least 8 characters long"
  }
}
```

#### Standard / System Error (`400`, `401`, `403`, `405`, `500`)
```json
{
  "success": false,
  "code": 400,
  "message": "Malformed or unreadable request body",
  "data": null
}
```

---

## 2. Authentication & Onboarding (`/api/v1/auth/**`)

Authorization is evaluated using database-backed granular permissions. Ownership, verification, enrollment, account status, and moderation rules are enforced by centralized policies. Missing permissions and failed policies return the standard `403 Forbidden` envelope. Role strings are not accepted by the API; registration accepts an `accountType` only to select initial onboarding permissions.

### `POST /api/v1/auth/register`
Creates a base user account.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "artisan@example.com",
  "password": "StrongPassword123!",
  "name": "Ahmed Benali",
  "accountType": "ARTISAN"
}
```
- **Response**: `201 Created` with User summary & confirmation email dispatch.

### `POST /api/v1/auth/login`
Authenticates credentials and returns JWT access + refresh tokens.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "artisan@example.com",
  "password": "StrongPassword123!"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "7c9e6679-7425...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
      "email": "artisan@example.com",
      "firstName": "Ahmed",
      "lastName": "Benali",
      "name": "Ahmed Benali",
      "phone": "+213 555 12 34 56",
      "avatarUrl": null,
      "accountStatus": "PENDING",
      "permissions": [
        "permission:artisan:content"
      ],
      "emailVerified": true,
      "emailVerifiedAt": "2026-09-01T10:00:00",
      "createdAt": "2026-09-01T10:00:00",
      "updatedAt": "2026-09-01T10:00:00",
      "bio": "Master ceramist specializing in traditional Kabyle and Islamic motifs.",
      "regionId": "reg-15",
      "city": "Tizi Ouzou",
      "address": "Route des Artisans, No. 12",
      "website": "https://artisan-example.dz",
      "subCategoryId": "subcat-pottery-01",
      "teacher": false,
      "verified": false,
      "premium": false,
      "rating": 0.0,
      "reviewsCount": 0
    },
    "permissions": [
      "permission:artisan:content"
    ]
  }
}
```

### `POST /api/v1/auth/refresh`
Rotates refresh tokens and generates a fresh access token.
- **Access**: Public
- **Request Body**: `{ "refreshToken": "7c9e6679-7425..." }`

### `POST /api/v1/auth/verify-email`
Verifies user email using a 6-digit verification code.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Email verified successfully.",
  "data": null
}
```

### `POST /api/v1/auth/resend-verification`
Resends an email verification code if the user exists and is unverified.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "user@example.com"
}
```
- **Response**: `200 OK` (Generic response to prevent user enumeration)
```json
{
  "success": true,
  "message": "If an unverified account exists for this email, a verification code has been sent.",
  "data": null
}
```

### `POST /api/v1/auth/forgot-password`
Initiates password reset by emailing a 6-digit reset code (or OAuth reminder notice).
- **Access**: Public
- **Request Body**:
```json
{
  "email": "user@example.com"
}
```
- **Response**: `200 OK` (Generic response to prevent user enumeration)
```json
{
  "success": true,
  "message": "If an account exists for this email, instructions have been sent.",
  "data": null
}
```

### `POST /api/v1/auth/reset-password`
Resets password using a 6-digit reset code and invalidates existing refresh tokens.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "user@example.com",
  "code": "123456",
  "newPassword": "NewStrongPassword123!"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Password reset successfully. You can now log in with your new password.",
  "data": null
}
```

### `POST /api/v1/auth/change-password`
Changes the authenticated user's password and invalidates active refresh tokens.
- **Access**: Authenticated
- **Request Body**:
```json
{
  "oldPassword": "CurrentPassword123!",
  "newPassword": "NewStrongPassword456!"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully.",
  "data": null
}
```

### `POST /api/v1/auth/complete-profile`
Completes profile details for newly registered Artisans or Clients.
- **Access**: Authenticated
- **Artisan Request Body**:
```json
{
  "bio": "Master ceramist specializing in traditional Kabyle and Islamic motifs.",
  "region": "Tizi Ouzou",
  "city": "Beni Yenni",
  "address": "Route des Artisans, No. 12",
  "subCategoryId": "subcat-pottery-01",
  "materialIds": ["mat-clay-01", "mat-glaze-02"],
  "epoqueIds": ["epoque-berber-01"],
  "techniqueIds": ["tech-hand-turning-01"],
  "isTeacher": true
}
```

### `POST /api/v1/auth/logout`
Revokes the authenticated user's refresh tokens.
- **Access**: Authenticated

### `GET /api/v1/auth/me`
Returns the authenticated user's permission-aware polymorphic profile response (`ArtisanResponseDTO` for artisans, `ClientProfileResponseDTO` for clients).
- **Access**: Authenticated (`Bearer <access-token>`)
- **Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "id": "usr-uuid-1234",
    "email": "artisan@souklab.dz",
    "name": "Amina Benali",
    "role": "ARTISAN",
    "isTeacher": true,
    "avatar": "https://cdn.souklab.dz/avatars/u-123-std.webp",
    "bio": "Céramiste traditionnelle kabyle",
    "craftCategories": ["Poterie", "Céramique"],
    "address": "Beni Yenni, Tizi Ouzou",
    "contactInfoLocked": false
  }
}
```

### `PATCH /api/v1/auth/me`
Partially updates the authenticated user's profile using JSON Merge Patch semantics (`null` removes an optional value; omitted keys are preserved).
- **Access**: Authenticated (`Bearer <access-token>`)
- **Payload Example**:
```json
{
  "bio": "Nouvelle biographie d'atelier mise à jour",
  "phone": "+213555123456",
  "website": "https://atelier-artisan.dz"
}
```
- **Response**: `200 OK` with updated profile payload.

### `GET /api/v1/auth/oauth/google/artisan` and `GET /api/v1/auth/oauth/google/client`
Start Google OAuth2 onboarding with an account-type intent. The callback links or creates the account and issues the normal JWT response.
- **Access**: Public; requires interactive browser navigation and Google consent.

---

## 3. Public Directory & Search Engine (`/api/v1/public/directory/**`)

The directory search engine provides high-performance, full-text scored search and multi-faceted filtering over active, verified artisans across Algeria. Powered by Hibernate Search with Elasticsearch backend, queries rank artisans by keyword match relevance, ratings, and profile engagement.

### `GET /api/v1/public/directory`
Executes multi-facet filtered search over active artisans.
- **Access**: Authenticated (`@PreAuthorize("isAuthenticated()")`). Unauthenticated requests receive `403 Forbidden`.
- **Query Parameters**:
  | Parameter | Type | Required | Default | Description |
  |---|---|---|---|---|
  | `keyword` | string | No | `null` | Full-text query matched against names, bios, specialties, and cities (max 120 chars). |
  | `craftCategory` | string | No | `null` | Filter by top-level craft category slug (e.g., `poterie-ceramique`). |
  | `craftSubCategory` | string | No | `null` | Filter by specific craft subcategory slug (e.g., `poterie-tlemcenienne`). |
  | `wilaya` | string | No | `null` | Filter by administrative Wilaya name or code (e.g., `Tlemcen`, `13`). |
  | `daira` | string | No | `null` | Filter by Daira / District name. |
  | `minRating` | decimal | No | `0.0` | Minimum average client review rating (`0.0` to `5.0`). |
  | `verifiedOnly` | boolean | No | `false` | When `true`, restricts results strictly to verified artisans. |
  | `page` | integer | No | `0` | Zero-based page index (minimum: 0). |
  | `size` | integer | No | `12` | Results per page (1 to 100). |
  | `sort` | string | No | `relevance` | Sort ordering: `relevance`, `rating,desc`, or `views,desc`. |

- **Privacy Gating**: Non-premium clients receive masked artisan names (`"Artisan #XXXXX"`) and locked contact flags (`contactInfoLocked: true`). Premium clients, administrators, and self-views receive the real artisan name and full contact information.

- **Response (Masked — Free / Regular Client View)**: `200 OK`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Data retrieved successfully",
    "data": {
      "content": [
        {
          "id": "e4a18295-e224-4f55-c1b7-d98e57d93e50",
          "artisanName": "Artisan #93E50",
          "avatarUrl": "https://storage.souklab.dz/avatars/e4a18295.webp",
          "coverImageUrl": "https://storage.souklab.dz/gallery/cover_e4a18295.webp",
          "bioSnippet": "Master ceramicist specializing in traditional pottery and earthenware...",
          "city": "Tlemcen",
          "wilayaName": "Tlemcen",
          "wilayaCode": "13",
          "regionSlug": "tlemcen-13",
          "categoryName": "Poterie & Céramique",
          "categorySlug": "poterie-ceramique",
          "subCategoryName": "Poterie Traditionnelle",
          "subCategorySlug": "poterie-traditionnelle",
          "rating": 4.85,
          "reviewsCount": 24,
          "viewsCount": 340,
          "verified": true,
          "premium": false,
          "teacher": true,
          "primaryMaterials": ["Argile", "Terre Cuite"],
          "primaryTechniques": ["Tournage", "Cuisson au Four Traditionnel"],
          "createdAt": "2026-01-15T10:00:00"
        }
      ],
      "pageNumber": 0,
      "pageSize": 12,
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    }
  }
  ```

- **Response (Unmasked — Premium Client / Admin View)**: `200 OK`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Data retrieved successfully",
    "data": {
      "content": [
        {
          "id": "e4a18295-e224-4f55-c1b7-d98e57d93e50",
          "artisanName": "Ahmed Benali",
          "avatarUrl": "https://storage.souklab.dz/avatars/e4a18295.webp",
          "coverImageUrl": "https://storage.souklab.dz/gallery/cover_e4a18295.webp",
          "bioSnippet": "Master ceramicist specializing in traditional pottery and earthenware...",
          "city": "Tlemcen",
          "wilayaName": "Tlemcen",
          "wilayaCode": "13",
          "regionSlug": "tlemcen-13",
          "categoryName": "Poterie & Céramique",
          "categorySlug": "poterie-ceramique",
          "subCategoryName": "Poterie Traditionnelle",
          "subCategorySlug": "poterie-traditionnelle",
          "rating": 4.85,
          "reviewsCount": 24,
          "viewsCount": 340,
          "verified": true,
          "premium": false,
          "teacher": true,
          "primaryMaterials": ["Argile", "Terre Cuite"],
          "primaryTechniques": ["Tournage", "Cuisson au Four Traditionnel"],
          "createdAt": "2026-01-15T10:00:00"
        }
      ],
      "pageNumber": 0,
      "pageSize": 12,
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    }
  }
  ```

- **Status Codes**:
  - `200 OK`: Directory query executed successfully.
  - `401 Unauthorized`: Missing or malformed JWT token.
  - `403 Forbidden`: Unauthenticated caller.
  - `422 Unprocessable Entity`: Invalid query parameters (e.g., negative page number, rating outside `0.0–5.0`).

---

## 4. Artisan Profiles, Credentials & Gallery

Artisan profiles feature rich craftsmanship portfolios, certifications, verified badges, and interactive gallery management with tiered image limits.

### `GET /api/v1/artisan/{id}`
Retrieves complete public representation of an artisan profile. Automatically tracks unique profile views (deduplicated per viewer session).
- **Access**: Authenticated (`@PreAuthorize("isAuthenticated()")`). Unauthenticated callers receive `403 Forbidden`.
- **Path Parameters**:
  - `id` (string, required): Unique identifier (UUID) of the target artisan.
- **Privacy Gating**: Non-premium clients receive masked contact information (`contactInfoLocked: true`, with `phone`, `email`, `website`, and `address` nullified). Premium subscribers and administrators receive unlocked fields.
- **Response**: `200 OK` with `ApiResponse<ArtisanPublicViewDTO>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Artisan profile retrieved successfully",
    "data": {
      "id": "e4a18295-e224-4f55-c1b7-d98e57d93e50",
      "name": "Ahmed Benali",
      "bio": "Master ceramicist with 15 years experience preserving traditional Tlemcen pottery.",
      "city": "Tlemcen",
      "regionId": "reg-13",
      "region": {
        "id": "reg-13",
        "name": "Tlemcen",
        "slug": "tlemcen-13",
        "code": "13"
      },
      "subCategoryId": "subcat-pot-trad",
      "subCategory": {
        "id": "subcat-pot-trad",
        "name": "Poterie Traditionnelle",
        "slug": "poterie-traditionnelle"
      },
      "materials": [
        { "id": "mat-argile", "name": "Argile Rouge", "slug": "argile-rouge" }
      ],
      "techniques": [
        { "id": "tech-tournage", "name": "Tournage Manuel", "slug": "tournage-manuel" }
      ],
      "epoques": [
        { "id": "ep-andalous", "name": "Époque Andalousienne", "slug": "epoque-andalousienne" }
      ],
      "galleryImages": [
        {
          "id": "gal-01",
          "imageUrl": "https://storage.souklab.dz/gallery/vase_01.webp",
          "caption": "Hand-carved floral motif vase",
          "displayOrder": 0
        }
      ],
      "certifications": [
        {
          "id": "cert-01",
          "title": "Maître Artisan d'Art",
          "issuingOrganization": "Chambre d'Artisanat de Tlemcen",
          "issuedAt": "2020-05-12"
        }
      ],
      "rating": 4.85,
      "reviewsCount": 24,
      "teacher": true,
      "verified": true,
      "avatarUrl": "https://storage.souklab.dz/avatars/e4a18295.webp",
      "createdAt": "2026-01-15T10:00:00",
      "contactInfoLocked": false,
      "phone": "+213555123456",
      "email": "ahmed.benali@souklab.dz",
      "website": "https://benali-pottery.dz",
      "address": "12 Rue des Potiers, Tlemcen"
    }
  }
  ```
- **Status Codes**:
  - `200 OK`: Profile retrieved successfully.
  - `401 Unauthorized`: Missing or expired token.
  - `403 Forbidden`: Unauthenticated caller.
  - `404 Not Found`: Artisan profile not found or inactive.

---

### `PATCH /api/v1/auth/me`
Unified endpoint to update the authenticated artisan's biographical details, contact information, and taxonomy associations.
- **Access**: Authenticated Artisan (`permission:profile:write` or artisan self)
- **Request Body**: `UpdateProfileRequestDTO`
  ```json
  {
    "bio": "Updated biographical description of craft experience.",
    "phone": "+213555987654",
    "address": "14 Boulevard des Artisans",
    "city": "Tlemcen",
    "website": "https://atelier-benali.dz",
    "craftSubCategoryIds": ["subcat-pot-trad", "subcat-ceramique-fine"]
  }
  ```
- **Response**: `200 OK` with `ApiResponse<CurrentUserProfileDTO>`

---

### `GET /api/v1/artisan/certifications`
Retrieves all professional accreditation documents and credentials belonging to the authenticated artisan.
- **Access**: Authenticated Artisan (`permission:artisan:content`)
- **Response**: `200 OK` with `ApiResponse<List<CertificationResponseDTO>>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Certifications retrieved successfully",
    "data": [
      {
        "id": "cert-uuid-1",
        "title": "Maître Artisan",
        "issuingOrganization": "Chambre de l'Artisanat et des Métiers (CAM)",
        "issuedAt": "2022-06-15",
        "documentUrl": "https://storage.souklab.dz/certifications/cert-uuid-1.pdf",
        "createdAt": "2026-02-10T14:30:00"
      }
    ]
  }
  ```

---

### `POST /api/v1/artisan/certifications`
Uploads a new professional qualification or certification document.
- **Access**: Authenticated Artisan (`permission:artisan:content`)
- **Content-Type**: `multipart/form-data`
- **Form Parameters**:
  - `title` (string, required): Title or certification designation.
  - `issuingOrganization` (string, required): Granting body or guild.
  - `issuedAt` (date `YYYY-MM-DD`, required): Date of conferral.
  - `document` (file, required): PDF or JPEG/PNG document (max 10MB, scanned by ClamAV when enabled).
- **Response**: `201 Created` with `ApiResponse<CertificationResponseDTO>`
- **Status Codes**:
  - `201 Created`: Certification recorded successfully.
  - `400 Bad Request`: Missing form field or unreadable file.
  - `415 Unsupported Media Type`: Non-document file type.
  - `422 Unprocessable Entity`: File failed security / malware scan.

---

### `PUT /api/v1/artisan/certifications/{id}`
Updates metadata or replaces the document file for an owned certification. Modifying an existing certification resets its verification status.
- **Access**: Authenticated Artisan Owner (`permission:artisan:content`)
- **Content-Type**: `multipart/form-data`
- **Form Parameters**:
  - `title` (string, optional): Updated credential title.
  - `issuer` (string, optional): Granting organization.
  - `issuedAt` (date `YYYY-MM-DD`, optional): Date of conferral.
  - `expiresAt` (date `YYYY-MM-DD`, optional): Expiration date.
  - `file` (file, optional): Replacement document file (PDF or image).
- **Response**: `200 OK` with `ApiResponse<CertificationResponseDTO>`.

---

### `DELETE /api/v1/artisan/certifications/{id}`
Deletes an owned certification document.
- **Access**: Authenticated Artisan Owner (`permission:artisan:content`)
- **Path Parameters**:
  - `id` (string, required): Certification identifier.
- **Response**: `200 OK` with `ApiResponse<Void>`
- **Status Codes**:
  - `200 OK`: Certification removed.
  - `403 Forbidden`: Caller does not own this certification.
  - `404 Not Found`: Certification not found.

---

### `GET /api/v1/artisan/gallery`
Lists all portfolio showcase images for the authenticated artisan.
- **Access**: Authenticated Artisan (`permission:artisan:content`)
- **Response**: `200 OK` with `ApiResponse<List<GalleryImageResponseDTO>>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Gallery images retrieved successfully",
    "data": [
      {
        "id": "gal-uuid-1",
        "imageUrl": "https://storage.souklab.dz/gallery/gal-uuid-1.webp",
        "thumbnailUrl": "https://storage.souklab.dz/gallery/thumbs/gal-uuid-1.webp",
        "caption": "Hand-painted ceramic vase",
        "displayOrder": 0,
        "craftSubCategoryId": "subcat-pot-trad",
        "createdAt": "2026-02-15T09:00:00"
      }
    ]
  }
  ```

---

### `POST /api/v1/artisan/gallery`
Uploads a showcase image to the portfolio gallery. Enforces subscription tier quotas (FREE: 3 images, PRO: 10 images, PREMIUM: 20 images).
- **Access**: Authenticated Artisan (`permission:artisan:content`)
- **Content-Type**: `multipart/form-data`
- **Form Parameters**:
  - `image` (file, required): Image file (JPEG, PNG, WebP up to 10MB).
  - `caption` (string, optional): Description of the displayed piece.
  - `craftSubCategoryId` (string, optional): Associated craft subcategory ID.
- **Response**: `201 Created` with `ApiResponse<GalleryImageResponseDTO>`
- **Status Codes**:
  - `201 Created`: Image uploaded and processed successfully.
  - `400 Bad Request`: Missing image parameter.
  - `409 Conflict`: Subscription tier gallery capacity limit exceeded.
  - `415 Unsupported Media Type`: Non-image file format.

---

### `PUT /api/v1/artisan/gallery/{id}`
Updates metadata or replaces the image file for an owned gallery photo.
- **Access**: Authenticated Artisan Owner (`permission:artisan:content`)
- **Content-Type**: `multipart/form-data`
- **Form Parameters**:
  - `title` (string, optional): Updated image title.
  - `caption` (string, optional): Updated caption description.
  - `craftSubCategoryId` (string, optional): Associated craft subcategory ID.
  - `file` (file, optional): Replacement image file.
- **Response**: `200 OK` with `ApiResponse<GalleryImageResponseDTO>`.

---

### `DELETE /api/v1/artisan/gallery/{id}`
Deletes an owned showcase photo from the portfolio gallery.
- **Access**: Authenticated Artisan Owner (`permission:artisan:content`)
- **Path Parameters**:
  - `id` (string, required): Gallery image identifier.
- **Response**: `200 OK` with `ApiResponse<Void>`
- **Status Codes**:
  - `200 OK`: Image deleted.
  - `403 Forbidden`: Caller does not own this gallery image.
  - `404 Not Found`: Image not found.

---

### `PUT /api/v1/artisan/gallery/order`
Reorders the display sequence of images in the artisan's portfolio gallery.
- **Access**: Authenticated Artisan Owner (`permission:artisan:content`)
- **Request Body**: `List<String>` (array of image IDs in desired display sequence)
  ```json
  [
    "gal-uuid-3",
    "gal-uuid-1",
    "gal-uuid-2"
  ]
  ```
- **Response**: `200 OK` with `ApiResponse<List<GalleryImageResponseDTO>>`

---

### User Avatars (`/api/v1/users/me/avatars/**`)
Authenticated users can upload, switch, and delete profile avatars. Uploads are strictly rate-limited and size-bounded (max 5MB).
- `POST /api/v1/users/me/avatars`: Upload new avatar (`multipart/form-data`, param `file`). Returns `201 Created` with `ApiResponse<AvatarResponseDTO>`.
- `GET /api/v1/users/me/avatars`: List uploaded avatars for current user (`200 OK`).
- `PUT /api/v1/users/me/avatars/{id}/activate`: Set selected avatar as active profile image (`200 OK`).
- `DELETE /api/v1/users/me/avatars/{id}`: Delete an inactive avatar (`200 OK`). Active avatars cannot be deleted without activating another first.

---

## 5. Catalog & Reference Taxonomy (`/api/v1/catalog/**` and `/api/v1/admin/catalog/**`)

Platform reference taxonomies provide standardized classification for regions, craft categories, raw materials, historical eras, and craftsmanship techniques. Public endpoints are cached in-memory using Caffeine; administrative modifications trigger immediate cache eviction.

### Public Read Endpoints

#### `GET /api/v1/catalog/regions`
Returns all 58 Algerian Wilayas and Communes in a hierarchical tree.
- **Access**: Public / Unauthenticated
- **Response**: `200 OK` with `ApiResponse<List<RegionDTO>>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Regions retrieved successfully",
    "data": [
      {
        "id": "reg-13",
        "name": "Tlemcen",
        "slug": "tlemcen-13",
        "code": "13",
        "parentId": null,
        "children": [
          {
            "id": "reg-1301",
            "name": "Mansourah",
            "slug": "mansourah-1301",
            "code": "1301",
            "parentId": "reg-13",
            "children": []
          }
        ]
      }
    ]
  }
  ```

#### `GET /api/v1/catalog/categories`
Returns all craft sectors and nested subcategories.
- **Access**: Public / Unauthenticated
- **Response**: `200 OK` with `ApiResponse<List<JobCategoryDTO>>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Categories retrieved successfully",
    "data": [
      {
        "id": "cat-pot",
        "name": "Poterie & Céramique",
        "slug": "poterie-ceramique",
        "description": "Arts du feu et travail de la terre cuite",
        "displayOrder": 1,
        "isActive": true,
        "subCategories": [
          {
            "id": "subcat-pot-trad",
            "name": "Poterie Traditionnelle",
            "slug": "poterie-traditionnelle",
            "description": "Poterie utilitaire et décorative façon traditionaliste",
            "displayOrder": 1,
            "isActive": true
          }
        ]
      }
    ]
  }
  ```

#### `GET /api/v1/catalog/materials`
Returns raw material families and individual crafting materials.
- **Access**: Public / Unauthenticated
- **Response**: `200 OK` with `ApiResponse<List<MaterialFamilyDTO>>`

#### `GET /api/v1/catalog/epoques`
Returns traditional Algerian cultural and historical periods (e.g., Numidian, Andalusian, Ottoman).
- **Access**: Public / Unauthenticated
- **Response**: `200 OK` with `ApiResponse<List<EpoqueDTO>>`

#### `GET /api/v1/catalog/techniques`
Returns traditional artisanal craftsmanship techniques (e.g., Tournage manuel, Martelage, Tissage sur métier vertical).
- **Access**: Public / Unauthenticated
- **Response**: `200 OK` with `ApiResponse<List<TechniqueDTO>>`

---

### Admin Taxonomy Management (`/api/v1/admin/catalog/**`)

All administrative mutations require `permission:admin:catalog`. Writes automatically invalidate the corresponding Caffeine cache entries in real-time.

| Entity | Create (`POST`) | Replace (`PUT`) | Patch (`PATCH`) | Delete (`DELETE`) | Status Toggle (`PATCH /status`) |
|---|---|---|---|---|---|
| **Techniques** | `/techniques` | `/techniques/{id}` | `/techniques/{id}` | `/techniques/{id}` | — |
| **Epoques** | `/epoques` | `/epoques/{id}` | `/epoques/{id}` | `/epoques/{id}` | — |
| **Regions** | `/regions` | `/regions/{id}` | `/regions/{id}` | `/regions/{id}` | — |
| **Categories** | `/categories` | `/categories/{id}` | `/categories/{id}` | `/categories/{id}` | `/categories/{id}/status` |
| **Subcategories** | `/subcategories` | `/subcategories/{id}` | `/subcategories/{id}` | `/subcategories/{id}` | `/subcategories/{id}/status` |
| **Material Families**| `/material-families`| `/material-families/{id}`| `/material-families/{id}`| `/material-families/{id}`| `/material-families/{id}/status`|
| **Materials** | `/materials` | `/materials/{id}` | `/materials/{id}` | `/materials/{id}` | `/materials/{id}/status` |

- **Validation & Constraint Rules**:
  - `409 Conflict`: Returned if hard-delete is attempted on an item referenced by artisans (e.g., a technique used in profiles) or containing children (e.g., a Wilaya with communes, or a Category with active subcategories).
  - `422 Unprocessable Entity`: Returned for circular parent references in hierarchical structures (such as nested regions).
  - Slug generation: Slugs are automatically normalized from names if omitted. Duplicate slugs return `409 Conflict`.

---

## 6. Formations & Workshops (`/api/v1/artisan/formations/**`)

Artisan masterclasses allow accredited master craftsmen to offer hands-on training, workshop curricula, and downloadable course resources. Administrative review ensures quality control before masterclasses appear in the public catalog.

> **Access Control Note**: Authoring workshops requires `permission:artisan:formations` and accredited instructor status (`isTeacher = true`). Browsing the catalog and enrolling requires `permission:artisan:formations`. Administrative curriculum moderation requires `permission:admin:formations`.

### Authoring & Management (`ArtisanFormationController`)

#### `POST /api/v1/artisan/formations`
Creates a new masterclass draft.
- **Access**: Accredited Instructor Artisan (`permission:artisan:formations` with `isTeacher = true`)
- **Request Body**: `FormationCreateDTO`
  ```json
  {
    "title": "Traditional Berber Pottery Masterclass",
    "description": "Three-day intensive hands-on workshop covering ancient clay preparation and glazing.",
    "scheduledAt": "2026-11-15T09:00:00",
    "durationHours": 18,
    "maxParticipants": 12,
    "price": 15000,
    "currency": "DZD"
  }
  ```
- **Response**: `201 Created` with `ApiResponse<FormationResponseDTO>` (initial status `DRAFT`).

#### `GET /api/v1/artisan/formations/me`
Retrieves a paginated list of formations authored by the authenticated artisan.
- **Access**: Authenticated Artisan (`permission:artisan:formations`)
- **Query Parameters**: `page` (default: 0), `size` (default: 10), `sort` (default: `createdAt,desc`).
- **Response**: `200 OK` with `ApiResponse<PaginatedResponse<FormationResponseDTO>>`.

#### `GET /api/v1/artisan/formations/{id}`
Retrieves complete details of an authored formation, including review history, enrolled student count, and uploaded learning files.
- **Access**: Authenticated Author (`permission:artisan:formations` — author ownership verified)
- **Response**: `200 OK` with `ApiResponse<FormationResponseDTO>`.

#### `PUT /api/v1/artisan/formations/{id}`
Updates an authored formation. If significant schedule or pricing attributes are modified on an already approved formation, its status automatically reverts to `PENDING_REVIEW`.
- **Access**: Authenticated Author (`permission:artisan:formations` — author ownership verified)
- **Request Body**: `FormationUpdateDTO`
- **Response**: `200 OK` with `ApiResponse<FormationResponseDTO>`.

#### `POST /api/v1/artisan/formations/{id}/thumbnail`
Uploads a showcase cover thumbnail image (`multipart/form-data`, param `file`, max 10MB).
- **Access**: Authenticated Author (`permission:artisan:formations`)
- **Response**: `200 OK` with updated thumbnail URL.

#### `POST /api/v1/artisan/formations/{id}/files`
Uploads course syllabus, reading materials, or templates (`multipart/form-data`, param `file`, max 25MB, up to 10 files per formation).
- **Access**: Authenticated Author (`permission:artisan:formations`)
- **Response**: `201 Created` with `ApiResponse<FormationFileResponseDTO>`.

#### `DELETE /api/v1/artisan/formations/{id}/files/{fileId}`
Soft-deletes a course attachment file.
- **Access**: Authenticated Author (`permission:artisan:formations`)
- **Response**: `200 OK`.

#### `POST /api/v1/artisan/formations/{id}/submit`
Submits a `DRAFT` or `REJECTED` masterclass for administrative moderation. Validates syllabus completeness and required fields before transitioning status to `PENDING_REVIEW`.
- **Access**: Authenticated Author (`permission:artisan:formations`)
- **Response**: `200 OK` with `ApiResponse<FormationResponseDTO>`.

#### `DELETE /api/v1/artisan/formations/{id}`
Soft-deletes an authored masterclass. Blocked if active student enrollments exist.
- **Access**: Authenticated Author (`permission:artisan:formations`)
- **Response**: `200 OK`.

---

### Peer Discovery & Enrollment (`ArtisanFormationEnrollmentController`)

#### `GET /api/v1/artisan/formations/catalog`
Browses published masterclasses in the public workshop catalog.
- **Access**: Authenticated Artisan (`permission:artisan:formations`)
- **Query Parameters**:
  - `page` (int, default: 0), `size` (int, default: 12), `sort` (default: `scheduledAt,asc`).
- **Response**: `200 OK` with `ApiResponse<PaginatedResponse<FormationPublicViewDTO>>`.

#### `GET /api/v1/artisan/formations/catalog/{id}`
Retrieves detailed public masterclass curriculum, instructor bio, remaining seat capacity, and syllabus file list.
- **Access**: Authenticated Artisan (`permission:artisan:formations`)
- **Response**: `200 OK` with `ApiResponse<FormationPublicViewDTO>`.

#### `POST /api/v1/artisan/formations/{id}/enroll`
Enrolls the caller in a published masterclass.
- **Access**: Authenticated Artisan (`permission:artisan:formations`)
- **Validation**: Enforces seat capacity (`maxParticipants`). Self-enrollment by the authoring instructor is blocked (`400 Bad Request`). Duplicate enrollment returns `409 Conflict`.
- **Response**: `201 Created` with `ApiResponse<FormationEnrollmentResponseDTO>`.

#### `POST /api/v1/artisan/formations/{id}/cancel`
Cancels an active masterclass enrollment reservation.
- **Access**: Authenticated Artisan (`permission:artisan:formations`)
- **Validation**: Enforces the configured cancellation cutoff deadline before workshop start.
- **Response**: `200 OK` with `ApiResponse<Void>`.

#### `GET /api/v1/artisan/formations/my-enrollments`
Retrieves paginated enrollment history and upcoming registered workshops for the authenticated artisan.
- **Access**: Authenticated Artisan (`permission:artisan:formations`)
- **Response**: `200 OK` with `ApiResponse<PaginatedResponse<FormationEnrollmentDetailDTO>>`.

#### `GET /api/v1/artisan/formations/{id}/files/{fileId}/download`
Streams protected course syllabus and attachment materials.
- **Access**: Enrolled Student or Authoring Instructor (`permission:artisan:formations`). Non-enrolled users receive `403 Forbidden`.
- **Response**: Binary file stream with `Content-Disposition: attachment; filename="..."`.

---

## 7. Social Feed, Reviews & Moderation

### Community Social Feed (`/api/v1/feed/**`)

The social feed enables verified artisans to publish craft updates, workshop announcements, and atelier showcases. Posts are categorized by type (`ACTUALITE`, `FORMATION`, `ANNONCE`). Artisan posts undergo moderation (`PENDING` -> `PUBLISHED`), while admin posts publish immediately.

#### `GET /api/v1/feed`
Retrieves paginated stream of published community posts.
- **Access**: Public / Authenticated
- **Query Parameters**:
  - `type` (string, optional): Filter by `ACTUALITE`, `FORMATION`, or `ANNONCE`.
  - `page` (int, default: 0), `size` (int, default: 20), `sort` (default: `publishedAt,desc`).
- **Response**: `200 OK` with `ApiResponse<PaginatedResponse<FeedPostResponseDTO>>`.
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Feed retrieved successfully",
    "data": {
      "content": [
        {
          "id": "post-uuid-1",
          "authorId": "artisan-uuid-1",
          "authorName": "Fatima Zohra",
          "type": "ACTUALITE",
          "title": "New Ceramic Kiln Firing",
          "body": "Opening the kiln after 72 hours of traditional reduction firing!",
          "status": "PUBLISHED",
          "formationId": null,
          "publishedAt": "2026-09-20T11:00:00",
          "moderationNote": null,
          "media": [
            {
              "id": "media-1",
              "url": "https://storage.souklab.dz/feed/kiln_open.webp",
              "displayOrder": 0
            }
          ]
        }
      ],
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    }
  }
  ```

#### `GET /api/v1/feed/{id}`
Retrieves single post by ID.
- **Response**: `200 OK` with `ApiResponse<FeedPostResponseDTO>`.

#### `POST /api/v1/feed`
Submits a new post. Active verified artisans require `permission:artisan:content` (post status set to `PENDING`). Administrators require `permission:admin:feed` (post status set directly to `PUBLISHED`).
- **Request Body**: `FeedPostCreateDTO`
  ```json
  {
    "type": "ACTUALITE",
    "title": "Wood Carving Demonstration",
    "body": "Join us tomorrow at the Tlemcen Artisan Center for live cedarwood carving.",
    "formationId": null
  }
  ```
- **Response**: `201 Created` with `ApiResponse<FeedPostResponseDTO>`.

#### `PUT /api/v1/feed/{id}` and `DELETE /api/v1/feed/{id}`
Author or administrator updates or removes a post (`200 OK`).

#### `POST /api/v1/feed/{id}/media` and `DELETE /api/v1/feed/{id}/media/{mediaId}`
Attaches (`multipart/form-data`, max 10MB) or removes image attachments from an authored post (`200 OK`).

#### Feed Interactions & Engagement
- `POST /api/v1/feed/{id}/likes` and `DELETE /api/v1/feed/{id}/likes`: Idempotently like or unlike a post (Authenticated).
- `GET /api/v1/feed/{id}/likes`: Query current caller's like status on post (`FeedPostLikeStatusDTO`).
- `POST /api/v1/feed/{id}/bookmarks` and `DELETE /api/v1/feed/{id}/bookmarks`: Save or remove post from bookmarks (Authenticated).
- `GET /api/v1/feed/saved`: Paginated list of caller's saved posts (Authenticated).
- `POST /api/v1/feed/{id}/share`: Increments post share counter and returns share payload.

#### Feed Comments & Replies
- `GET /api/v1/feed/{id}/comments`: Lists root comments for a post (Public, paginated).
- `POST /api/v1/feed/{id}/comments`: Submits a root comment (`FeedPostCommentCreateDTO`: `{ "body": string }`) (Authenticated).
- `GET /api/v1/feed/comments/{commentId}`: Retrieves a single visible comment or reply (Public).
- `GET /api/v1/feed/comments/{commentId}/replies`: Lists replies to a comment (Public, paginated).
- `POST /api/v1/feed/comments/{commentId}/replies`: Submits a reply one level deep (`FeedPostCommentCreateDTO`) (Authenticated).
- `PUT /api/v1/feed/comments/{commentId}`: Updates authored comment text (`FeedPostCommentCreateDTO`) (Authenticated author).
- `DELETE /api/v1/feed/comments/{commentId}`: Soft-deletes authored comment (Authenticated author or admin).
- `POST /api/v1/feed/comments/{commentId}/likes` and `DELETE .../likes`: Idempotently like or unlike a comment (Authenticated).
- `GET /api/v1/feed/comments/{commentId}/likes`: Query caller's like status on a comment (Public).

---

### Artisan Reviews (`/api/v1/artisan/reviews/**` and `/api/v1/artisans/**`)

Client and peer reviews maintain service trust and reputation. Reviews are tied directly to completed workshop enrollments to prevent fraudulent testimonials.

#### `GET /api/v1/artisans/{artisanId}/reviews`
Retrieves public paginated reviews for a specific artisan.
- **Access**: Public / Authenticated
- **Response**: `200 OK` with `ApiResponse<PaginatedResponse<ArtisanReviewResponseDTO>>`

#### `GET /api/v1/artisan/reviews/{reviewId}`
Retrieves a single published, visible artisan review.
- **Access**: Public / Authenticated
- **Response**: `200 OK` with `ApiResponse<ArtisanReviewResponseDTO>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Operation completed successfully",
    "data": {
      "id": "rev-01",
      "reviewerId": "client-uuid-1",
      "reviewerName": "Karim M.",
      "artisanId": "artisan-uuid-1",
      "formationId": "form-uuid-1",
      "rating": 5.00,
      "comment": "Exceptional instruction. Learned traditional pottery wheel fundamentals in one weekend.",
      "createdAt": "2026-08-15T16:20:00",
      "updatedAt": null
    }
  }
  ```

#### `POST /api/v1/artisan/formations/{formationId}/reviews`
Submits a decimal rating (`0.00` to `5.00`) and feedback commentary after attending a completed workshop.
- **Access**: Enrolled Student (`permission:artisan:reviews` with verified `ATTENDED` status).
- **Request Body**: `ArtisanReviewRequestDTO`
  ```json
  {
    "rating": 4.75,
    "comment": "Comprehensive course materials and hands-on guidance. Highly recommended!"
  }
  ```
- **Response**: `201 Created` with `ApiResponse<ArtisanReviewResponseDTO>`.

#### `PUT /api/v1/artisan/reviews/{reviewId}` and `DELETE /api/v1/artisan/reviews/{reviewId}`
Edit or delete an owned review (`200 OK`).

---

### Content Abuse Reports (`/api/v1/reports`)

#### `POST /api/v1/reports`
Submits an abuse report against a user, post, or review.
- **Access**: Authenticated (`permission:report:create`)
- **Request Body**: `ContentReportRequestDTO`
  ```json
  {
    "targetType": "POST",
    "targetId": "post-uuid-1",
    "reason": "Spam or irrelevant commercial advertisement",
    "details": "User is promoting external non-craft services repeatedly."
  }
  ```
- **Response**: `201 Created` with `ApiResponse<ContentReportResponseDTO>`.

---

## 8. Real-Time Messaging & WebSockets (`/api/v1/conversations/**` and `/ws`)

The real-time messaging subsystem provides end-to-end, private one-to-one communications between authenticated platform users (clients and artisans). Both REST and WebSocket STOMP interfaces are supported. Administrators have no private message access bypass.

> [!IMPORTANT]
> **Client Premium Requirement & Privacy Masking**:
> - **Client Premium Gating**: Clients require an active Premium subscription to initiate conversations (`POST /api/v1/conversations`), send messages (`POST /api/v1/conversations/{id}/messages` and STOMP `/messages.send`), edit messages, upload attachments, or broadcast typing events. Non-premium clients attempting any of these operations receive `403 Forbidden` (`FORBIDDEN`).
> - **Artisan Identity Masking**: For non-premium clients viewing conversations (`GET /api/v1/conversations` or `GET /api/v1/conversations/{id}`), the artisan's display name (`participantName`) is masked as `"Artisan #XXXXX"` (e.g. `Artisan #3BD3F`) to prevent off-platform disintermediation. Premium clients, artisans, and administrators receive the unmasked artisan name.

### REST Conversations API

- `POST /api/v1/conversations`: Initiates a new conversation or retrieves existing (`CreateConversationRequest`: `{ "recipientUserId": string }`). Returns `201 Created` with `ApiResponse<ConversationResponse>`. (Requires Premium for clients; self-messaging returns `400 Bad Request`).
- `GET /api/v1/conversations`: Lists active conversations for the authenticated user, optionally filtered by archive status (`?archived=false`). Returns `200 OK` with `ApiResponse<List<ConversationResponse>>`.
- `GET /api/v1/conversations/{id}`: Retrieves single conversation summary for one of its participants (`200 OK` with `ApiResponse<ConversationResponse>`).
- `PATCH /api/v1/conversations/{id}/archive`: Toggles archive status for the conversation (`ArchiveConversationRequest`: `{ "archived": boolean }`). Returns `200 OK`.
- `GET /api/v1/conversations/{id}/messages`: Fetches cursor-paginated message history (`?cursor=string&size=50`). Returns `200 OK` with `ApiResponse<MessagePageResponse>`.
- `POST /api/v1/conversations/{id}/messages`: Fallback HTTP endpoint to send a message (`SendMessageRequest`: `{ "idempotencyKey": string, "content": string, "attachmentKeys": string[] }`). Returns `201 Created` with `ApiResponse<MessageResponse>`. (Requires Premium for clients).
- `PATCH /api/v1/conversations/{conversationId}/messages/{messageId}`: Edits authored message text within the allowed edit window (`EditMessageRequest`: `{ "content": string }`). Returns `200 OK` with `ApiResponse<MessageResponse>`. (Requires Premium for clients).
- `DELETE /api/v1/conversations/{conversationId}/messages/{messageId}`: Soft-deletes an authored message (`200 OK`).
- `POST /api/v1/conversations/{id}/read`: Advances caller's read-up-to state (`ReadReceiptRequest`: `{ "messageId": string }` — optional, if null/omitted marks all read). Returns `200 OK`.
- `POST /api/v1/conversations/{id}/attachments`: Uploads an image or document attachment for chat (`multipart/form-data`, file param `file`, max 10MB, scanned by ClamAV). Returns `201 Created` with `ApiResponse<AttachmentUploadResponse>` (`key`, `filename`, `contentType`, `size`). (Requires Premium for clients).

---

### STOMP WebSocket Protocol (`/ws`)

- **Handshake URL**: `ws://<host>/ws` (or `wss://<host>/ws`, with SockJS fallback enabled)
- **Authentication**: JWT bearer token passed in STOMP `CONNECT` frame:
  ```stomp
  CONNECT
  accept-version:1.2,1.1,1.0
  heart-beat:10000,10000
  Authorization:Bearer <jwt-token>
  ^@
  ```

#### Inbound Destinations (Client -> Server)
- `/app/v1/conversations/{conversationId}/messages.send`: Sends real-time chat message.
  ```json
  {
    "idempotencyKey": "c9284fae-3c92-4f32-8419-58bfa8319dc2",
    "content": "Hello, is the pottery masterclass still accepting participants?",
    "attachmentKeys": []
  }
  ```
- `/app/v1/conversations/{conversationId}/messages.edit`: Edits authored message text.
  ```json
  {
    "messageId": "msg-uuid-1",
    "correlationId": "cor-uuid-1",
    "content": "Updated message content"
  }
  ```
- `/app/v1/conversations/{conversationId}/messages.delete`: Soft-deletes authored message.
  ```json
  {
    "messageId": "msg-uuid-1",
    "correlationId": "cor-uuid-1"
  }
  ```
- `/app/v1/conversations/{conversationId}/read`: Submits read receipt up to specified message.
  ```json
  {
    "messageId": "msg-uuid-1",
    "correlationId": "cor-uuid-1"
  }
  ```
- `/app/v1/conversations/{conversationId}/typing.start`: Broadcasts typing indicator start.
  ```json
  {
    "correlationId": "cor-uuid-1"
  }
  ```
- `/app/v1/conversations/{conversationId}/typing.stop`: Broadcasts typing indicator stop.
  ```json
  {
    "correlationId": "cor-uuid-1"
  }
  ```

#### Subscriptions (Server -> Client)
- `/user/queue/chat`: Direct message deliveries, command acknowledgments, error events.
- `/user/queue/chat-events`: Real-time chat events (typing indicators, read receipts, message edits/deletions).
- `/topic/presence`: Real-time user online/offline status broadcast.
- `/user/queue/notifications`: In-app transactional notifications.

---

## 9. Subscriptions & Chargily Pay V2 (`/api/v1/subscriptions/**` & `/api/v1/integrations/chargily/**`)

Monetization on SoukLab is powered by Chargily Pay V2 with EDA (Event-Driven Architecture) and cryptographic HMAC-SHA256 signature verification.

### Public Subscription Plans

#### `GET /api/v1/subscriptions/plans`
Lists all active public subscription tiers with pricing in Algerian Dinars (DZD).
- **Access**: Public / Unauthenticated
- **Response**: `200 OK` with `ApiResponse<List<SubscriptionPlanResponse>>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Plans retrieved successfully",
    "data": [
      {
        "id": "plan-artisan-pro",
        "name": "Artisan Pro",
        "subscriberType": "ARTISAN",
        "billingPeriod": "MONTHLY",
        "amount": 2500,
        "currency": "DZD",
        "features": [
          "Up to 10 gallery showcase images",
          "5 active formations in catalog",
          "Verified Artisan badge eligibility"
        ]
      },
      {
        "id": "plan-client-premium",
        "name": "Client Premium",
        "subscriberType": "CLIENT",
        "billingPeriod": "YEARLY",
        "amount": 5000,
        "currency": "DZD",
        "features": [
          "Unlocked contact details across entire directory",
          "Direct phone & email access to master craftsmen",
          "Priority formation seat reservation"
        ]
      }
    ]
  }
  ```

---

### Authenticated Subscription Checkout & Management

- `POST /api/v1/subscriptions/checkout`: Initiates checkout session.
  - **Request Body**: `SubscriptionCheckoutRequest` (`{ "planId": string }`)
  - **Response**: `201 Created` with `ApiResponse<SubscriptionCheckoutResponse>`
    ```json
    {
      "success": true,
      "code": 201,
      "message": "Checkout session initialized",
      "data": {
        "paymentId": "pay-uuid-1",
        "subscriptionId": "sub-uuid-1",
        "providerCheckoutId": "chargily-chk-98124",
        "checkoutUrl": "https://pay.chargily.net/test/checkouts/chk_98124/pay"
      }
    }
    ```
  - Redirect the user's browser to `checkoutUrl` to complete EDA payment via EDA/CIB.

- `GET /api/v1/subscriptions/current`: Returns active subscription details, renewal dates, and status (`ACTIVE`, `PENDING_PAYMENT`, `CANCELED`, `EXPIRED`).
- `GET /api/v1/subscriptions`: Returns paginated subscription history for the authenticated user.
- `POST /api/v1/subscriptions/{id}/cancel`: Cancels auto-renewal at period end.
- `POST /api/v1/subscriptions/{id}/renew`: Re-enables subscription auto-renewal.
- `GET /api/v1/payments`: Paginated transaction and invoice history.
- `GET /api/v1/payments/{id}`: Detailed receipt for a specific payment.

---

### Chargily Webhook Processing (`/api/v1/integrations/chargily/webhook`)

- **URL**: `POST /api/v1/integrations/chargily/webhook`
- **Security**: Validates HMAC-SHA256 signature in the `Signature` HTTP header against the configured webhook secret.
- **Idempotency**: Webhook events are deduplicated via `WebhookEventClaimService`. Duplicate delivery attempts return `200 OK` without re-processing.
- **Supported Events**:
  - `checkout.paid`: Transitions payment to `PAID`, activates subscription to `ACTIVE`, and assigns premium capabilities.
  - `checkout.failed`: Transitions payment to `FAILED`, cancels pending subscription.
  - `checkout.expired`: Cleans up abandoned checkout sessions.

---

### Financial Administration (`/api/v1/admin/subscriptions/**` & `/api/v1/admin/payments/**`)

Protected administrative endpoints requiring `permission:financial:admin`.
- `GET /api/v1/admin/subscriptions`: Manage and inspect all platform subscriptions.
- `GET /api/v1/admin/subscriptions/payments`: Query payment audit ledger.
- `GET /api/v1/admin/subscriptions/webhooks`: Query webhook delivery logs.
- `POST /api/v1/admin/subscriptions/grant`: Manually grant a subscription (`ManualSubscriptionGrantRequest`).
- `POST /api/v1/admin/subscriptions/{id}/revoke`: Immediately revoke a subscription with reason.
- `POST /api/v1/admin/payments/{id}/refund`: Process payment refund (`FinancialReasonRequest`).

---

## 10. Admin & Moderation Operations (`/api/v1/admin/**`)

Administrator endpoints provide comprehensive governance over users, content, reviews, workshops, audit logging, and asynchronous platform analytics.

### User Governance (`user-management-controller` & `permission-management-controller`)
- `GET /api/v1/admin/users`: Paginated list of users with optional role, status, and email search query filters (`permission:admin:users`).
- `GET /api/v1/admin/users/pending`: Paginated queue of pending artisan registrations awaiting administrative accreditation (`permission:admin:users`).
- `POST /api/v1/admin/users/{id}/approve`: Approves a pending artisan account, transitioning status to `ACTIVE` (`permission:admin:users`).
- `POST /api/v1/admin/users/approve-bulk`: Bulk approves multiple artisan accounts (`List<String> userIds`) (`permission:admin:users`).
- `POST /api/v1/admin/users/{id}/ban`: Bans user account with mandatory reason (`BanRequestDTO`: `{ "reason": string }`) (`permission:admin:users`).
- `POST /api/v1/admin/users/{id}/timeout`: Temporarily timeouts user account (`TimeoutRequestDTO`: `{ "durationMinutes": int, "reason": string }`) (`permission:admin:users`).
- `POST /api/v1/admin/users/{id}/unban`: Reinstates a banned or timed-out user (`permission:admin:users`).
- `GET /api/v1/admin/users/audit-logs`: Queries the typed administrative audit trail across 55 action types (`permission:admin:users`).
- `GET /api/v1/admin/users/{userId}/permissions`: Lists granular permissions assigned to user.
- `POST /api/v1/admin/users/{userId}/permissions`: Assigns specific capability (`PermissionAssignmentRequestDTO`).
- `DELETE /api/v1/admin/users/{userId}/permissions`: Revokes capability from user.

---

### Asynchronous Analytics & KPI Reports (`/api/v1/admin/analytics/jobs/**`)
Complex platform metric aggregations and reporting jobs are processed asynchronously to preserve database responsiveness.
- `POST /api/v1/admin/analytics/jobs`: Submits asynchronous aggregation job (`USERS`, `FINANCIAL`, `ACTIVITY`). Requires `permission:analytics:admin` (financial metrics also require `permission:financial:admin`).
- `GET /api/v1/admin/analytics/jobs/{id}`: Polls current job status (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`).
- `GET /api/v1/admin/analytics/jobs/{id}/result`: Retrieves computed JSON metric summary once completed.
- `GET /api/v1/admin/analytics/jobs/{id}/download`: Streams exported CSV or JSON report artifact.
- `DELETE /api/v1/admin/analytics/jobs/{id}`: Deletes finished job record and cached report.

---

### Content & Workshop Moderation
- `GET /api/v1/admin/formations/pending`: Review queue for submitted workshops (`permission:admin:formations`).
- `POST /api/v1/admin/formations/{id}/review`: Approves or rejects workshop curriculum (`FormationReviewRequestDTO`: `{ "approved": bool, "reviewNotes": string }`).
- `POST /api/v1/admin/formations/{id}/publish`: Publishes approved workshop to the public catalog (`permission:admin:formations`).
- `GET /api/v1/admin/feed/pending`: Review queue for submitted artisan feed posts (`permission:admin:feed`).
- `POST /api/v1/admin/feed/{id}/publish`: Publishes pending post (`permission:admin:feed`).
- `POST /api/v1/admin/feed/{id}/hide`: Hides published post from community view (`permission:admin:feed`).
- `POST /api/v1/admin/feed/{id}/remove`: Permanently removes offending post (`permission:admin:feed`).
- `GET /api/v1/admin/reports`: Paginated content abuse reports queue (`permission:admin:reports`).
- `GET /api/v1/admin/reports/{id}`: Retrieves complete details of a specific abuse report (`permission:admin:reports`).
- `POST /api/v1/admin/reports/{id}/resolve`: Resolves report with action `DISMISS`, `HIDE`, or `REMOVE` (`ReportResolutionRequestDTO`). Automatically applies action to the targeted content and records resolution in the audit log.

---

## 11. Notifications (`/api/v1/notifications/**`)

The in-app notification system delivers transactional alerts for formation enrollments, status transitions, chat messages, subscription events, and moderation actions.

- `GET /api/v1/notifications`: Paginated list of notifications for the authenticated user (`?page=0&size=20`).
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Notifications retrieved successfully",
    "data": {
      "content": [
        {
          "id": "notif-uuid-1",
          "message": "Your masterclass 'Traditional Berber Pottery' has been approved and published!",
          "isRead": false,
          "type": "FORMATION_APPROVED",
          "targetId": "form-uuid-1",
          "createdAt": "2026-09-24T14:30:00"
        }
      ],
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    }
  }
  ```
- `GET /api/v1/notifications/unread-count`: Returns integer badge count of unread, active notifications.
- `PUT /api/v1/notifications/{id}/read`: Marks a specific notification as read (`200 OK`).
- `PUT /api/v1/notifications/read-all`: Marks all unread notifications for the caller as read (`200 OK`).
- `DELETE /api/v1/notifications/{id}`: Soft-deletes a notification for the authenticated user (`200 OK`).

---

## 12. File Storage (`/api/v1/files/**`)

File storage is backed by MinIO S3-compatible object storage with ClamAV antivirus clearance and centralized security access policies.

### `GET /api/v1/files/{key}`
Streams a stored object.
- **Access**: Evaluated dynamically by `FileAccessService`:
  - **Public Files** (avatars, public gallery images, public feed media): Accessible without authentication.
  - **Protected Files** (formation syllabus attachments): Accessible only to enrolled workshop participants and the authoring instructor.
  - **Private Documents** (accreditation requests, identity verification documents): Accessible only to the owning artisan and platform administrators.
- **Caching & Transport**: Emits `ETag` and `Cache-Control: public, max-age=31536000, immutable` headers for static media. Returns `304 Not Modified` when `If-None-Match` matches.
- **Status Codes**:
  - `200 OK`: Binary file stream with detected `Content-Type`.
  - `304 Not Modified`: Cached file validated.
  - `401 Unauthorized`: Missing credentials for protected file.
  - `403 Forbidden`: Caller not authorized under file access policy.
  - `404 Not Found`: Object key does not exist.

---

## 13. Formateur Governance (`/api/v1/artisan/formateur-request` and `/api/v1/admin/formateur-requests/**`)

Artisans must undergo administrative accreditation to receive instructor privileges (`isTeacher = true`, `permission:artisan:formations`) before publishing workshops.

### Artisan Accreditation Request
- `POST /api/v1/artisan/formateur-request`: Submits instructor application (`permission:artisan:content`).
  - **Request Body**: `FormateurRequestDTO` (`{ "motivation": "10 years teaching experience at Tlemcen guild." }`)
  - **Response**: `201 Created` with `ApiResponse<FormateurRequestResponseDTO>` (status `PENDING`).
  - **Validation**: Blocked if an active pending request exists or if the artisan is in an active rejection cooldown period.

### Administrative Accreditation Governance
Protected endpoints requiring `permission:admin:users`.
- `GET /api/v1/admin/formateur-requests`: Paginated queue of instructor requests with optional status filter (`PENDING`, `APPROVED`, `REJECTED`).
- `POST /api/v1/admin/formateur-requests/{id}/approve`: Approves application (`FormateurApproveDTO`: optional admin notes). Automatically sets `isTeacher = true` and grants `permission:artisan:formations`.
- `POST /api/v1/admin/formateur-requests/{id}/reject`: Rejects application (`FormateurRejectDTO`: reason and cooldown days). Sets cooldown period preventing immediate re-application.
- `POST /api/v1/admin/formateur-requests/{artisanId}/lift-cooldown`: Lifts rejection cooldown early (`FormateurCooldownOverrideDTO`).
- `POST /api/v1/admin/artisans/{artisanId}/formateur-grant`: Directly grants instructor status without prior formal request (`FormateurGrantDTO`).
- `POST /api/v1/admin/artisans/{artisanId}/formateur-revoke`: Revokes instructor accreditation and cancels pending workshops (`FormateurRevokeDTO`).

---

## 14. Client Favorites (`/api/v1/client/favorites/artisans/**`)

Client favorites allow authenticated clients to bookmark artisans, retrieve a paginated directory of favorited artisans, inspect favorite status for specific artisans, and remove favorites. All operations require `permission:client:favorites` and a registered client profile.

### `POST /api/v1/client/favorites/artisans/{artisanId}`
Adds an artisan to the authenticated client's favorites.
- **Access**: Authenticated Client (`@accessControl.canManageFavorites(authentication)`)
- **Path Parameters**:
  - `artisanId` (string, required): Unique identifier of the target artisan.
- **Request Body**: None
- **Response**: `201 Created` with `ApiResponse<ClientFavoriteArtisanResponseDTO>`
  ```json
  {
    "success": true,
    "code": 201,
    "message": "Artisan added to favorites successfully",
    "data": {
      "favoriteId": "d3b07384-d113-4e44-b0a6-c87d46c82d4f",
      "artisanId": "e4a18295-e224-4f55-c1b7-d98e57d93e50",
      "favoritedAt": "2026-09-24T17:56:45.628794"
    }
  }
  ```
  *(Note: `favoritedAt` is serialized by Jackson with microsecond precision without timezone suffix).*
- **Status Codes**:
  - `201 Created`: Artisan added to favorites successfully.
  - `401 Unauthorized`: Missing or invalid authentication token.
  - `403 Forbidden`: Missing `permission:client:favorites` (authorization guard access denied; artisans receive this), or permission held but caller has no client profile (administrators receive this with `"Only registered clients can manage favorites."`).
  - `404 Not Found`: Target artisan does not exist or is not effectively visible (`"Artisan not found with id: <artisanId>"`).
  - `409 Conflict`: Artisan is already favorited (`"Artisan is already favorited."`), or the client's favorite capacity is exceeded (`"Client favorite limit reached."`). Configured via `app.favorites.max-per-client` (`FAVORITES_MAX_PER_CLIENT`, default 500).

### `GET /api/v1/client/favorites/artisans`
Retrieves a paginated list of visible favorite artisans for the authenticated client.
- **Access**: Authenticated Client (`@accessControl.canManageFavorites(authentication)`)
- **Query Parameters**:
  - `page` (integer, optional, default: 0): Zero-based page index.
  - `size` (integer, optional, default: 20, max: 100): Page size.
  - `sort` (string, optional, default: `createdAt,desc`): Sort property and direction.
- **Request Body**: None
- **Response**: `200 OK` with `ApiResponse<PaginatedResponse<ClientFavoriteArtisanItemDTO>>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Success",
    "data": {
      "content": [
        {
          "favoritedAt": "2026-09-24T17:56:45.628794",
          "artisan": {
            "id": "e4a18295-e224-4f55-c1b7-d98e57d93e50",
            "artisanName": "Ahmed Benali",
            "avatarUrl": "https://storage.souklab.dz/avatars/...",
            "coverImageUrl": "https://storage.souklab.dz/gallery/...",
            "bioSnippet": "Master ceramicist specializing in traditional pottery...",
            "city": "Tlemcen",
            "wilayaName": "Tlemcen",
            "wilayaCode": "13",
            "regionSlug": "ouest",
            "categoryName": "Poterie & Céramique",
            "categorySlug": "poterie-ceramique",
            "subCategoryName": "Poterie Traditionnelle",
            "subCategorySlug": "poterie-traditionnelle",
            "rating": 4.9,
            "reviewsCount": 18,
            "viewsCount": 142,
            "verified": true,
            "premium": true,
            "teacher": false,
            "primaryMaterials": ["Argile rouge", "Argile blanche"],
            "primaryTechniques": ["Tournage", "Émaillage"],
            "createdAt": "2026-01-15T10:00:00"
          }
        }
      ],
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 1,
      "totalPages": 1,
      "last": true
    }
  }
  ```
- **Visibility & Masking Behavior**:
  - Automatically filters out suspended accounts or non-visible artisan profiles.
  - Contact masking parity: Only `artisanName` is masked (`"Artisan #XXXXX"` for non-premium, real name for premium, same rule as the public directory).
- **Status Codes**:
  - `200 OK`: Favorites retrieved successfully.
  - `400 Bad Request`: Invalid sort property or query parameter (e.g. unknown sort attribute).
  - `401 Unauthorized`: Missing or invalid authentication token.
  - `403 Forbidden`: Missing `permission:client:favorites` (authorization guard access denied; artisans receive this), or permission held but caller has no client profile (administrators receive this with `"Only registered clients can manage favorites."`).

### `GET /api/v1/client/favorites/artisans/{artisanId}/status`
Checks whether a specific artisan is favorited by the authenticated client.
- **Access**: Authenticated Client (`@accessControl.canManageFavorites(authentication)`)
- **Path Parameters**:
  - `artisanId` (string, required): Unique identifier of the target artisan.
- **Request Body**: None
- **Response**: `200 OK` with `ApiResponse<FavoriteStatusResponseDTO>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Success",
    "data": {
      "favorited": true
    }
  }
  ```
- **Status Codes**:
  - `200 OK`: Favorite status retrieved. Returns `{ "favorited": false }` if the artisan exists in the database but is suspended or not effectively visible.
  - `401 Unauthorized`: Missing or invalid authentication token.
  - `403 Forbidden`: Missing `permission:client:favorites` (authorization guard access denied; artisans receive this), or permission held but caller has no client profile (administrators receive this with `"Only registered clients can manage favorites."`).
  - `404 Not Found`: Artisan ID does not exist in the database (`"Artisan not found with id: <artisanId>"`).

### `DELETE /api/v1/client/favorites/artisans/{artisanId}`
Removes an artisan from the authenticated client's favorites.
- **Access**: Authenticated Client (`@accessControl.canManageFavorites(authentication)`)
- **Path Parameters**:
  - `artisanId` (string, required): Unique identifier of the target artisan.
- **Request Body**: None
- **Response**: `200 OK` with `ApiResponse<Void>`
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Artisan removed from favorites successfully",
    "data": null
  }
  ```
- **Status Codes**:
  - `200 OK`: Artisan removed from favorites successfully (returns HTTP 200 with `data: null`).
  - `401 Unauthorized`: Missing or invalid authentication token.
  - `403 Forbidden`: Missing `permission:client:favorites` (authorization guard access denied; artisans receive this), or permission held but caller has no client profile (administrators receive this with `"Only registered clients can manage favorites."`).
  - `404 Not Found`: Favorite record does not exist for this client and artisan (`"Favorite not found for artisan: <artisanId>"`).

