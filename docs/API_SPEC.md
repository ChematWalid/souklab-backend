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

### `POST /api/v1/auth/register`
Creates a base user account.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "artisan@example.com",
  "password": "StrongPassword123!",
  "name": "Ahmed Benali",
  "role": "ROLE_ARTISAN"
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
      "roles": [
        "ROLE_ARTISAN"
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
    "roles": [
      "ROLE_ARTISAN"
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

---

## 3. Public Directory & Search Engine (`/api/v1/public/directory/**`)

### `GET /api/v1/public/directory`
Full-text search and multi-facet filtering over active, verified artisans.
- **Access**: Public
- **Query Parameters**:
  - `q` (string, optional): Search keyword (e.g. `ceramique`, `cuir`, `Ahmed`)
  - `category` (string, optional): Category slug
  - `subcategory` (string, optional): Subcategory slug or ID
  - `region` (string, optional): Wilaya / Region slug
  - `material` (array of strings, optional): Material slugs
  - `epoque` (array of strings, optional): Historical era slugs
  - `technique` (array of strings, optional): Craft technique slugs
  - `featured` (boolean, optional): Filter premium/featured artisans
  - `page` (int, default: `0`), `size` (int, default: `12`), `sort` (string, default: `rating,desc`)
- **Response**: `200 OK` with paginated `ArtisanDirectoryCardDTO` list. Contact info (phone/email) is masked unless the requesting user has an active premium client subscription.

### `GET /api/v1/public/directory/{id}`
Returns complete artisan public dossier (Bio, Gallery, Certifications, Achievements, Reviews, Active Formations).

---

## 4. Catalog & Reference Taxonomy (`/api/v1/catalog/**`)

- `GET /api/v1/catalog/regions`: All wilayas and communes in hierarchical tree.
- `GET /api/v1/catalog/categories`: Categories with child subcategories.
- `GET /api/v1/catalog/materials`: Material families and individual crafting materials.
- `GET /api/v1/catalog/epoques`: Traditional and historical periods.
- `GET /api/v1/catalog/techniques`: Craftsmanship techniques.

---

## 5. Formations & Workshops (`/api/v1/artisan/formations/**`)

> **Access Control Note**: ROLE_CLIENT is rejected with 403 Forbidden on every route in this section. Formations are strictly peer-to-peer among artisans — there is no client-facing enrollment path.

### Authoring & Workshop Management (`ArtisanFormationController`)
- `POST /api/v1/artisan/formations`: Create a new masterclass draft (`ROLE_ARTISAN` — additionally requires accredited instructor status `isTeacher = true`).
- `GET /api/v1/artisan/formations/me`: Retrieve paginated list of formations authored by the authenticated artisan (`ROLE_ARTISAN`).
- `GET /api/v1/artisan/formations/{id}`: Retrieve comprehensive details for an authored formation including review history and course materials (`ROLE_ARTISAN` — author ownership verified).
- `PUT /api/v1/artisan/formations/{id}`: Update an authored formation's curriculum and scheduling metadata (`ROLE_ARTISAN` — author ownership verified; core schedule/pricing changes on approved/published formations reset status to `PENDING_REVIEW`).
- `POST /api/v1/artisan/formations/{id}/thumbnail`: Upload showcase thumbnail image (`ROLE_ARTISAN` — author ownership verified; multipart image up to 10MB, scanned when enabled).
- `POST /api/v1/artisan/formations/{id}/files`: Upload course syllabus or learning resource attachment (`ROLE_ARTISAN` — author ownership verified; multipart document up to 25MB, max 10 attachments per formation, scanned when enabled).
- `DELETE /api/v1/artisan/formations/{id}/files/{fileId}`: Soft-delete an attachment file from an authored formation (`ROLE_ARTISAN` — author ownership verified).
- `POST /api/v1/artisan/formations/{id}/submit`: Submit a draft or rejected formation for administrative moderation (`ROLE_ARTISAN` — author ownership verified; validates completeness, transitions to `PENDING_REVIEW`).
- `DELETE /api/v1/artisan/formations/{id}`: Soft-delete an authored formation (`ROLE_ARTISAN` — author ownership verified).

### Peer Discovery, Enrollment & Materials (`ArtisanFormationEnrollmentController`)
- `GET /api/v1/artisan/formations/catalog`: Browse published masterclass catalog (`ROLE_ARTISAN` — paginated, default sorted by `scheduledAt` ascending).
- `GET /api/v1/artisan/formations/catalog/{id}`: Retrieve detailed public representation of a published masterclass, active enrollment count, and syllabus overview (`ROLE_ARTISAN`).
- `POST /api/v1/artisan/formations/{id}/enroll`: Enroll the authenticated artisan in a published masterclass (`ROLE_ARTISAN` — author self-enrollment blocked, enforces maximum participant capacity).
- `POST /api/v1/artisan/formations/{id}/cancel`: Cancel confirmed enrollment reservation (`ROLE_ARTISAN` — requires active enrollment, enforces configured cancellation cutoff deadline before start).
- `GET /api/v1/artisan/formations/my-enrollments`: Retrieve paginated enrollment history and upcoming registered workshops for the authenticated artisan (`ROLE_ARTISAN`).
- `GET /api/v1/artisan/formations/{id}/files/{fileId}/download`: Download protected course document attachment stream (`ROLE_ARTISAN` — restricted strictly to confirmed enrolled participants and the authoring instructor).


---

## 6. Social Feed, Reviews & Moderation

These resources are planned and are not exposed by the current source tree. No feed, review, or report controllers/entities are currently implemented.

---

## 7. Realtime notifications (`/ws`)

Direct messaging REST resources and chat handlers are planned and are not exposed by the current source tree. The implemented WebSocket endpoint is `/ws`; authenticated clients may subscribe to `/user/queue/notifications` for notification pushes.

---

## 8. Subscriptions & Chargily Pay V2

This module is planned and is not exposed by the current source tree. There are no `/api/v1/subscription/**` controllers, payment entities, or Chargily client beans yet. See [`ROADMAP.md`](ROADMAP.md) Phase 9 for the implementation plan.

---

## 9. Admin & Moderation Operations (`/api/v1/admin/**`)

- `GET /api/v1/admin/users`: Paginated list of users with optional search filter.
- `GET /api/v1/admin/users/pending`: Paginated list of pending artisan registrations.
- `POST /api/v1/admin/users/{id}/approve`: Approve a user account (`ACTIVE`).
- `POST /api/v1/admin/users/approve-bulk`: Bulk approve multiple user accounts.
- `POST /api/v1/admin/users/{id}/ban`: Ban a user account with reason.
- `POST /api/v1/admin/users/{id}/timeout`: Timeout user for specified minutes with reason.
Platform KPI statistics are planned and no `/api/v1/admin/stats` endpoint is currently exposed.
- `GET /api/v1/admin/formations/pending`: Paginated queue of formations awaiting administrative review.
- `POST /api/v1/admin/formations/{id}/review`: Approve or reject workshop curriculum.
- `POST /api/v1/admin/formations/{id}/publish`: Publish approved workshop to public catalog.
Report moderation is planned and no `/api/v1/admin/reports/**` endpoint is currently exposed.

---

## 10. Notifications (`/api/v1/notifications/**`)

- `GET /api/v1/notifications`: Paginated list of notifications for the authenticated user (`?page=0&size=20`).
- `GET /api/v1/notifications/unread-count`: Count of unread, non-deleted notifications.
- `PUT /api/v1/notifications/{id}/read`: Mark a specific notification as read.
- `PUT /api/v1/notifications/read-all`: Mark all notifications for the authenticated user as read.
- `DELETE /api/v1/notifications/{id}`: Soft-delete a notification for the authenticated user.
