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
Returns the authenticated user's permission-aware profile response.
- **Access**: Authenticated

### `PATCH /api/v1/auth/me`
Partially updates the authenticated user's profile using the documented PATCH field semantics.
- **Access**: Authenticated

### `GET /api/v1/auth/oauth/google/artisan` and `GET /api/v1/auth/oauth/google/client`
Start Google OAuth2 onboarding with an account-type intent. The callback links or creates the account and issues the normal JWT response.
- **Access**: Public; requires interactive browser navigation and Google consent.

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
  - `page` (int, default: `0`), `size` (int, default: `20`), `sort` (string, default: `rating,desc`)
- **Response**: `200 OK` with paginated `ArtisanDirectoryCardDTO` list. Contact info (phone/email) is masked unless the requesting user has an active premium client subscription.

---

## 4. Artisan Profiles, Credentials & Gallery

- `GET /api/v1/artisan/{id}`: Retrieve an artisan public view with deduplicated profile-view tracking and premium-gated contact fields.
- `PATCH /api/v1/artisan/profile`: Update the authenticated artisan profile (`permission:artisan:content`).
- `POST/GET/DELETE /api/v1/artisan/certifications[/{id}]`: Manage owned certification documents (`permission:artisan:content`).
- `POST/GET/PUT/DELETE /api/v1/artisan/gallery[/{id}]` and `PUT /api/v1/artisan/gallery/order`: Manage the configured artisan gallery (`permission:artisan:content`).

---

## 5. Catalog & Reference Taxonomy (`/api/v1/catalog/**` and `/api/v1/admin/catalog/**`)

Public read endpoints:
- `GET /api/v1/catalog/regions`: All wilayas and communes in hierarchical tree.
- `GET /api/v1/catalog/categories`: Categories with child subcategories.
- `GET /api/v1/catalog/materials`: Material families and individual crafting materials.
- `GET /api/v1/catalog/epoques`: Traditional and historical periods.
- `GET /api/v1/catalog/techniques`: Craftsmanship techniques.

### Admin Taxonomy Management (`/api/v1/admin/catalog/**`)
Protected write endpoints requiring `permission:admin:catalog`. All writes invalidate the corresponding Caffeine cache entries in real-time.

- `POST /api/v1/admin/catalog/techniques`: Create technique (201 Created). Auto-generates slug from name if omitted; 409 on duplicate slug.
- `PUT /api/v1/admin/catalog/techniques/{id}`: Replace technique (200 OK).
- `PATCH /api/v1/admin/catalog/techniques/{id}`: Partial update technique (200 OK; toggle isActive, displayOrder).
- `DELETE /api/v1/admin/catalog/techniques/{id}`: Hard-delete technique (200 OK, `data: null`). 409 Conflict if referenced by artisans.
- `POST /api/v1/admin/catalog/epoques`: Create epoque with optional periodEra (201 Created).
- `PUT /api/v1/admin/catalog/epoques/{id}`: Replace epoque (200 OK).
- `PATCH /api/v1/admin/catalog/epoques/{id}`: Partial update epoque (200 OK).
- `DELETE /api/v1/admin/catalog/epoques/{id}`: Hard-delete epoque (200 OK, `data: null`). 409 Conflict if referenced by artisans.
- `POST /api/v1/admin/catalog/regions`: Create Wilaya or Commune with optional parentId and code (201 Created). Missing parentId yields 404.
- `PUT /api/v1/admin/catalog/regions/{id}`: Replace region (200 OK). Circular parent reference yields 422 Unprocessable Entity.
- `PATCH /api/v1/admin/catalog/regions/{id}`: Partial update region (200 OK).
- `DELETE /api/v1/admin/catalog/regions/{id}`: Hard-delete region (200 OK, `data: null`). 409 Conflict if region has child regions.
- `POST /api/v1/admin/catalog/categories`: Create category (201 Created). Auto-generates slug from name if omitted; 409 on duplicate slug.
- `PUT /api/v1/admin/catalog/categories/{id}`: Replace category (200 OK).
- `PATCH /api/v1/admin/catalog/categories/{id}`: Partial update category (200 OK; toggle isActive, displayOrder).
- `DELETE /api/v1/admin/catalog/categories/{id}`: Hard-delete category (200 OK, `data: null`). 409 Conflict if category has child subcategories.
- `POST /api/v1/admin/catalog/subcategories`: Create subcategory under a category (201 Created). Missing categoryId yields 400, non-existent category yields 404; 409 on duplicate slug.
- `PUT /api/v1/admin/catalog/subcategories/{id}`: Replace subcategory (200 OK).
- `PATCH /api/v1/admin/catalog/subcategories/{id}`: Partial update subcategory (200 OK; toggle isActive, displayOrder).
- `DELETE /api/v1/admin/catalog/subcategories/{id}`: Hard-delete subcategory (200 OK, `data: null`). 409 Conflict if referenced by artisans.
- `POST /api/v1/admin/catalog/material-families`: Create material family (201 Created). Auto-generates slug from name if omitted; 409 on duplicate slug.
- `PUT /api/v1/admin/catalog/material-families/{id}`: Replace material family (200 OK).
- `PATCH /api/v1/admin/catalog/material-families/{id}`: Partial update material family (200 OK; toggle isActive, displayOrder).
- `DELETE /api/v1/admin/catalog/material-families/{id}`: Hard-delete material family (200 OK, `data: null`). 409 Conflict if family has child materials.
- `POST /api/v1/admin/catalog/materials`: Create raw material under a family (201 Created). Missing familyId yields 400, non-existent family yields 404; 409 on duplicate slug.
- `PUT /api/v1/admin/catalog/materials/{id}`: Replace material (200 OK).
- `PATCH /api/v1/admin/catalog/materials/{id}`: Partial update material (200 OK; toggle isActive, displayOrder).
- `DELETE /api/v1/admin/catalog/materials/{id}`: Hard-delete material (200 OK, `data: null`). 409 Conflict if referenced by artisans.



---

## 6. Formations & Workshops (`/api/v1/artisan/formations/**`)

> **Access Control Note**: Formation authoring and enrollment require `permission:artisan:formations`; administrative moderation requires `permission:admin:formations`.

### Authoring & Workshop Management (`ArtisanFormationController`)
- `POST /api/v1/artisan/formations`: Create a new masterclass draft (`permission:artisan:formations` — additionally requires accredited instructor status `isTeacher = true`).
- `GET /api/v1/artisan/formations/me`: Retrieve paginated list of formations authored by the authenticated artisan (`permission:artisan:formations`).
- `GET /api/v1/artisan/formations/{id}`: Retrieve comprehensive details for an authored formation including review history and course materials (`permission:artisan:formations` — author ownership verified).
- `PUT /api/v1/artisan/formations/{id}`: Update an authored formation's curriculum and scheduling metadata (`permission:artisan:formations` — author ownership verified; core schedule/pricing changes on approved/published formations reset status to `PENDING_REVIEW`).
- `POST /api/v1/artisan/formations/{id}/thumbnail`: Upload showcase thumbnail image (`permission:artisan:formations` — author ownership verified; multipart image up to 10MB, scanned when enabled).
- `POST /api/v1/artisan/formations/{id}/files`: Upload course syllabus or learning resource attachment (`permission:artisan:formations` — author ownership verified; multipart document up to 25MB, max 10 attachments per formation, scanned when enabled).
- `DELETE /api/v1/artisan/formations/{id}/files/{fileId}`: Soft-delete an attachment file from an authored formation (`permission:artisan:formations` — author ownership verified).
- `POST /api/v1/artisan/formations/{id}/submit`: Submit a draft or rejected formation for administrative moderation (`permission:artisan:formations` — author ownership verified; validates completeness, transitions to `PENDING_REVIEW`).
- `DELETE /api/v1/artisan/formations/{id}`: Soft-delete an authored formation (`permission:artisan:formations` — author ownership verified).

### Peer Discovery, Enrollment & Materials (`ArtisanFormationEnrollmentController`)
- `GET /api/v1/artisan/formations/catalog`: Browse published masterclass catalog (`permission:artisan:formations` — paginated, default sorted by `scheduledAt` ascending).
- `GET /api/v1/artisan/formations/catalog/{id}`: Retrieve detailed public representation of a published masterclass, active enrollment count, and syllabus overview (`permission:artisan:formations`).
- `POST /api/v1/artisan/formations/{id}/enroll`: Enroll the authenticated artisan in a published masterclass (`permission:artisan:formations` — author self-enrollment blocked, enforces maximum participant capacity).
- `POST /api/v1/artisan/formations/{id}/cancel`: Cancel confirmed enrollment reservation (`permission:artisan:formations` — requires active enrollment, enforces configured cancellation cutoff deadline before start).
- `GET /api/v1/artisan/formations/my-enrollments`: Retrieve paginated enrollment history and upcoming registered workshops for the authenticated artisan (`permission:artisan:formations`).
- `GET /api/v1/artisan/formations/{id}/files/{fileId}/download`: Download protected course document attachment stream (`permission:artisan:formations` — restricted strictly to confirmed enrolled participants and the authoring instructor).


---

## 7. Social Feed, Reviews & Moderation

- `GET /api/v1/feed`: Browse published posts with optional `type` filter and pagination.
- `GET /api/v1/feed/{id}`: Retrieve one published post and its media.
- `POST /api/v1/feed`: Submit an `ACTUALITE`, `FORMATION`, or `ANNONCE` post for moderation (`permission:artisan:content` for active verified artisans or `permission:admin:feed`).
- `PUT /api/v1/feed/{id}` and `DELETE /api/v1/feed/{id}`: Author/admin update or remove a post.
- `POST/DELETE /api/v1/feed/{id}/media[/{mediaId}]`: Add or remove validated image attachments.
- `GET /api/v1/artisans/{artisanId}/reviews`: Browse visible artisan reviews.
- `POST /api/v1/artisan/formations/{formationId}/reviews`: Submit one decimal `0.00–5.00` review after an `ATTENDED` completed enrollment.
- `PUT/DELETE /api/v1/artisan/reviews/{reviewId}`: Edit or remove an owned review.
- `POST /api/v1/reports`: Report a user, post, or review.
- `GET /api/v1/admin/feed/pending`: Administrator moderation queue.
- `POST /api/v1/admin/feed/{id}/publish|hide|remove`: Administrator post moderation actions.
- `GET /api/v1/admin/reports`: Administrator report queue with status and target filters.
- `POST /api/v1/admin/reports/{id}/resolve`: Dismiss, hide, or remove the reported target.

---

## 8. Realtime notifications (`/ws`)

Direct messaging is exposed through `/api/v1/conversations` and the authenticated `/ws` STOMP endpoint. Conversations are private one-to-one resources. Clients can create/list/archive conversations, page message history with cursors, upload validated attachments, send idempotent messages, edit/delete authored messages, and advance read-up-to state. STOMP commands use `/app/v1/conversations/{conversationId}/...`; versioned events are delivered to participant user destinations. Typing events are ephemeral and presence is broadcast on the configured presence destination. Only active, verified users with `permission:message:send` may mutate chat; existing participants may read history while ineligible. Administrators have no private-chat bypass.

---

## 9. Subscriptions & Chargily Pay V2

Subscription and Chargily Pay V2 endpoints are implemented under `/api/v1/subscription/**`, including checkout creation, account/subscription operations, administrative plan and subscription operations, and the signed idempotent webhook. Access is permission-scoped; financial operations additionally require the financial administrator permission.

---

## 10. Admin & Moderation Operations (`/api/v1/admin/**`)

- `GET /api/v1/admin/users`: Paginated list of users with optional search filter.
- `GET /api/v1/admin/users/pending`: Paginated list of pending artisan registrations.
- `POST /api/v1/admin/users/{id}/approve`: Approve a user account (`ACTIVE`).
- `POST /api/v1/admin/users/approve-bulk`: Bulk approve multiple user accounts.
- `POST /api/v1/admin/users/{id}/ban`: Ban a user account with reason.
- `POST /api/v1/admin/users/{id}/timeout`: Timeout user for specified minutes with reason.
- `POST /api/v1/admin/users/{id}/unban`: Reinstate a banned or timed-out account.
- `GET /api/v1/admin/users/audit-logs`: Query administrative audit logs.
- `GET/POST/DELETE /api/v1/admin/users/{userId}/permissions`: List, assign, or revoke enabled permissions.
Platform KPI statistics are exposed asynchronously through `/api/v1/admin/analytics/jobs` and the
compatibility alias `/api/v1/admin/stats/jobs`. Results are owner-scoped and retrieved through
the corresponding status, result, and download endpoints; financial reports require the
financial administrator permission in addition to analytics permission.
- `GET /api/v1/admin/formations/pending`: Paginated queue of formations awaiting administrative review.
- `POST /api/v1/admin/formations/{id}/review`: Approve or reject workshop curriculum.
- `POST /api/v1/admin/formations/{id}/publish`: Publish approved workshop to public catalog.
- `GET /api/v1/admin/reports`: Paginated report queue with optional status and target filters.
- `POST /api/v1/admin/reports/{id}/resolve`: Resolve an open report with `DISMISS`, `HIDE`, or `REMOVE`; the action is applied to the reported target and recorded for auditability.

---

## 11. Notifications (`/api/v1/notifications/**`)

- `GET /api/v1/notifications`: Paginated list of notifications for the authenticated user (`?page=0&size=20`).
- `GET /api/v1/notifications/unread-count`: Count of unread, non-deleted notifications.
- `PUT /api/v1/notifications/{id}/read`: Mark a specific notification as read.
- `PUT /api/v1/notifications/read-all`: Mark all notifications for the authenticated user as read.
- `DELETE /api/v1/notifications/{id}`: Soft-delete a notification for the authenticated user.

## 12. File Storage (`/api/v1/files/**`)

- `GET /api/v1/files/{key}`: Stream a stored object after the application file-access policy authorizes the request; MIME detection, immutable cache headers, and configured rate limiting are applied.

## 13. Formateur Governance (`/api/v1/artisan/formateur-request` and `/api/v1/admin/formateur-requests/**`)

- `POST /api/v1/artisan/formateur-request`: Submit an artisan accreditation request (`permission:artisan:content`).
- `GET /api/v1/admin/formateur-requests`, approve/reject/lift-cooldown, and direct grant/revoke endpoints: Administrator governance under `permission:admin:users`.
