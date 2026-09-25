# Souklab frontend API handoff

Status: current backend contract. The backend is the source of truth; generate the OpenAPI document from the running application rather than copying DTOs into the frontend repository.

## Environments

| Environment | API base URL | OpenAPI |
|---|---|---|
| Local | `http://localhost:8080/api/v1` | `http://localhost:8080/v3/api-docs` |
| Test/staging | supplied by deployment | same host + `/v3/api-docs` when enabled |
| Production | supplied by deployment | obtain the private CI artifact |

The path and port are configurable with `PORT`, `OPENAPI_PATH`, and `OPENAPI_SWAGGER_PATH`. Do not hard-code a production hostname.

---

## Starter Kit & Developer Tools

To fast-track frontend development, the following battle-tested templates and references are provided in this directory:

| Resource | Description |
| :--- | :--- |
| **[`types.ts`](types.ts)** | Ready-to-import TypeScript definitions covering all models, request bodies, and response envelopes. |
| **[`api-client.ts`](api-client.ts)** | Drop-in Axios instance featuring a race-condition-safe 401 refresh token queue and `@stomp/stompjs` WebSocket helper. |
| **[`api-requests.http`](api-requests.http)** | Executable HTTP test suite for VS Code (REST Client / Thunder Client) and IntelliJ IDEA. |
| **[`ERROR_CODES.md`](ERROR_CODES.md)** | Catalog of backend error codes mapped to HTTP statuses and recommended UX handling (toasts, modals, form states). |

---

## Authentication & User Profile (`/auth/**`, `/me`)

Use `Authorization: Bearer <accessToken>` for all protected REST calls. Login and refresh return access/refresh tokens and expiry metadata. Keep tokens in secure storage (e.g. secure memory or HttpOnly cookies) and never log them.

### Primary Auth & Profile Endpoints
- `POST /api/v1/auth/register`: Starts onboarding (Artisan: `PENDING` validation, Client: `ACTIVE`). Payload: `email`, `password` (8–128 chars), `firstName`, `lastName`, `accountType` (`'ARTISAN' | 'CLIENT'`).
- `POST /api/v1/auth/login`: Authenticates credentials (`email` or `username`, `password` max 128 chars), returns `accessToken` (1h), `refreshToken` (24h), and user profile summary.
- `POST /api/v1/auth/refresh`: Rotates the refresh token and returns a new token pair.
- `POST /api/v1/auth/logout`: Revokes active refresh session.
- `POST /api/v1/auth/verify-email`: Verifies 6-digit OTP code sent on registration. Uniform HTTP 400 (`"Invalid or expired code."`) on invalid/expired codes or unknown emails prevents user enumeration.
- `POST /api/v1/auth/resend-verification`: Issues a fresh 6-digit verification code.
- `POST /api/v1/auth/forgot-password`: Requests 6-digit password reset code via email.
- `POST /api/v1/auth/reset-password`: Resets password using the 6-digit code (`email`, `code`, `newPassword` 8–128 chars).
- `POST /api/v1/auth/change-password`: Authenticated endpoint to change password (`oldPassword` max 128 chars, `newPassword` 8–128 chars, `@DifferentPasswords`).
- `GET /api/v1/auth/oauth/google/artisan`: Google OAuth initiation with artisan intent cookie (`souklab_oauth_intent=ARTISAN`, HttpOnly, SameSite=Lax, Secure).
- `GET /api/v1/auth/oauth/google/client`: Google OAuth initiation with client intent cookie (`souklab_oauth_intent=CLIENT`, HttpOnly, SameSite=Lax, Secure).
- `POST /api/v1/auth/complete-profile`: Onboarding wizard to complete craft/business details.
- `GET /api/v1/auth/me`: Retrieves current user profile (`ProfileResponse`).
- `PATCH /api/v1/auth/me`: Partial updates using JSON Merge Patch semantics.
- `POST /api/v1/users/me/avatars`: Uploads and activates a new profile avatar (`multipart/form-data`, key `file`).
- `GET /api/v1/users/me/avatars`: Lists uploaded avatar history.
- `PUT /api/v1/users/me/avatars/{id}/activate`: Re-activates a past gallery avatar.
- `DELETE /api/v1/users/me/avatars/{id}`: Deletes an avatar from storage.

### Security Headers & Public Endpoint Scoping
- **Public Endpoints Method Scoping**: Unauthenticated public access to `/feed`, `/catalog/**`, `/public/**`, `/subscriptions/plans`, and `/artisans/*/reviews` is restricted strictly to `HttpMethod.GET`. Any unauthenticated modification requests (`POST`, `PUT`, `PATCH`, `DELETE`) return `401 Unauthorized`.
- **Security Headers Enforced**: Every HTTP response carries standard browser hardening headers:
  - `Content-Security-Policy: default-src 'self'; frame-ancestors 'none'; object-src 'none';`
  - `Permissions-Policy: camera=(), microphone=(), geolocation=()`
  - `X-Frame-Options: DENY`
  - `X-Content-Type-Options: nosniff`
  - `Referrer-Policy: no-referrer`

### `GET /api/v1/auth/me` Polymorphic Contract
The `data` payload returned by `GET /api/v1/auth/me` is account-type-specific:
- **Artisan**: Returns `ArtisanResponseDTO` with bio, city, address, website, rating, reviewsCount, `region`, `craftCategory`, `subCategory`, `primaryMaterials`, `primaryTechniques`, `epoques`, `certifications`, `galleryImages`.
- **Client**: Returns `ClientProfileResponseDTO` with companyName, clientType (`INDIVIDUAL` or `ENTERPRISE`), city, address, and client subscription tier.
- Both share common base fields: `id`, `email`, `firstName`, `lastName`, `name`, `phone`, `avatarUrl`, `accountStatus`, `permissions`, `emailVerified`, `createdAt`, `updatedAt`, `premium`.

### `PATCH /api/v1/auth/me` JSON Merge Patch Semantics
- **Omitted key**: Field is untouched.
- **Explicit `null`**: Field is cleared (if nullable).
- **Explicit value**: Field is updated.
- Supported Artisan fields: `bio`, `city`, `address`, `website`, `regionId`, `subCategoryId`, `materialIds`, `techniqueIds`, `epoqueIds`.
- Supported Client fields: `companyName`, `clientType`, `city`, `address`.

### Token Refresh Flow
On `401 Unauthorized`, queue incoming requests and attempt exactly one call to `POST /api/v1/auth/refresh` with the active refresh token. If refresh succeeds, update the stored access token and replay queued requests. If refresh fails with 401 or 403, immediately clear authentication state and redirect the user to `/login`. Note: A `403 Forbidden` response indicates lack of permission or unauthenticated access (e.g. `/public/directory`), NOT an expired token.

## Response and error contracts

Successful REST responses use:

```json
{"success":true,"code":200,"message":"Success","data":{}}
```

`code` is the HTTP status and is stamped from the actual response. Errors use the same envelope, with `success: false`, a stable `errorCode`, and `data: null`. Validation errors additionally include an `errors` object keyed by request field. Branch on `errorCode`, never on message text.

Common statuses are 400 (malformed request), 401 (unauthenticated), 403 (forbidden), 404 (missing resource), 409 (state conflict), 413 (upload too large), 415 (unsupported media type), 422 (validation/rejected content), 429 (rate limited), and 500/503 (server/dependency failure).

## Detailed Domain Integration Guides

---

### 1. Reference Taxonomies (`/api/v1/catalog/**`)

All reference taxonomy endpoints are public and cached in-memory with Caffeine. Use these endpoints to populate dropdown selectors, search filters, and onboarding steps.

#### Endpoints
- `GET /api/v1/catalog/regions`: Full Algerian administrative hierarchy (Algeria root → 58 Wilayas → child Communes). Each node returns `id`, `name`, `slug`, `code`, `displayOrder`, and nested `children: RegionDTO[]`.
- `GET /api/v1/catalog/categories`: Artisanal craft categories with nested `subCategories: JobSubCategoryDTO[]`.
- `GET /api/v1/catalog/materials`: Material families (e.g., Clay, Leather, Metal) with nested child `materials: MaterialDTO[]`.
- `GET /api/v1/catalog/epoques`: Traditional and historical Algerian epochs/periods (`id`, `name`, `slug`, `periodEra`, `displayOrder`).
- `GET /api/v1/catalog/techniques`: Craftsmanship techniques (`id`, `name`, `slug`, `description`, `displayOrder`).

#### Frontend Implementation Guidance
- **Caching Strategy**: Cache catalog responses in client state (e.g. Pinia, Redux, TanStack Query with `staleTime: Infinity` or 24 hours). Reference taxonomies rarely change.
- **Cascading Selectors**: In onboarding forms and search filters, populate the Wilaya selector from `regions` where `parent == null`. When a Wilaya is selected, dynamically populate the Commune selector from that Wilaya node's `children`.

---

### 2. Public Directory & Search Engine (`/api/v1/public/directory`)

Search and discovery over verified, active artisans powered by Hibernate Search with Elasticsearch BM25 scoring and criteria fallback.

#### Endpoint
- `GET /api/v1/public/directory`: Returns `ApiResponse<PaginatedResponse<ArtisanDirectoryCardDTO>>`.
  - **Query Parameters**:
    - `keyword` (string, optional, max 120 chars): Full-text query matched against names, bios, specialties, and cities.
    - `craftCategory` (string, optional): Filter by top-level category slug/name.
    - `craftSubCategory` (string, optional): Filter by subcategory slug/name.
    - `wilaya` (string, optional): Wilaya name filter (e.g., `Tlemcen`, `Alger`).
    - `daira` (string, optional): District/Commune filter.
    - `minRating` (number, optional, 0.0 - 5.0): Filter artisans by minimum average rating.
    - `verifiedOnly` (boolean, default: `false`): Restrict results to verified artisans.
    - `page` (integer, default: 0): Zero-based page number.
    - `size` (integer, default: 12, max: 100): Results per page.
    - `sort` (string, default: `relevance`): Sort order (`relevance`, `rating,desc`, `views,desc`).
  - **Access**: Authenticated (`@PreAuthorize("isAuthenticated()")`). Unauthenticated calls return `403 Forbidden`.

#### Privacy Gating & Contact Masking
- The backend evaluates the viewer's entitlement using `ViewerPremiumResolver`.
- **`contactInfoLocked: true` (Non-Premium Clients)**:
  - `artisanName` is masked as `"Artisan #XXXXX"` (using the first 5 uppercase characters of the artisan's UUID).
  - Phone, email, website, and physical address are absent.
  - **UI Guidance**: Render a lock icon ($\text{🔒}$) with an "Unlock Contact Info" call-to-action button that triggers the subscription upgrade drawer.
- **`contactInfoLocked: false` (Premium Clients, Admins, Self-Views)**:
  - Real artisan name is returned unmasked. Full contact options (call, email, website) are visible.
- **Search Debounce**: Debounce search input keystrokes by 300ms before triggering network requests to preserve rate limits.

---

### 3. Artisan Profiles, Certifications & Showcase Gallery

Artisans manage their portfolio media, certifications, and public digital storefront.

#### Endpoints
- `GET /api/v1/artisan/{id}` (HTTP 200): Public profile details. Enforces contact masking and records deduplicated daily profile views.
- `POST /api/v1/artisan/certifications` (HTTP 201): Uploads an accreditation document (`multipart/form-data`, keys: `title`, `issuer`, `issueDate`, `file`). Max 10 certifications per artisan. Allowed MIME: `application/pdf`, `image/jpeg`, `image/png`. Max 15MB.
- `GET /api/v1/artisan/certifications` (HTTP 200): Lists owned certifications.
- `DELETE /api/v1/artisan/certifications/{id}` (HTTP 200): Removes a certification document.
- `POST /api/v1/artisan/gallery` (HTTP 201): Uploads a showcase portfolio image (`multipart/form-data`, keys: `caption`, `displayOrder`, `file`). Max 20 images. Allowed MIME: `image/jpeg`, `image/png`, `image/webp`. Max 10MB.
- `GET /api/v1/artisan/gallery` (HTTP 200): Lists portfolio images ordered by `displayOrder` ascending.
- `PUT /api/v1/artisan/gallery/{id}` (HTTP 200): Updates caption or display order.
- `DELETE /api/v1/artisan/gallery/{id}` (HTTP 200): Removes an image from the portfolio.
- `PUT /api/v1/artisan/gallery/order` (HTTP 200): Batch updates display order sequence (payload: `[{ "id": string, "displayOrder": number }]`).

---

### 4. Masterclasses & Formations (`/api/v1/artisan/formations/**`)

Artisans with `isTeacher = true` author masterclasses, while artisans and clients enroll in workshops.

#### Authoring Workflow (`permission:artisan:formations`)
1. **Create Draft**: `POST /api/v1/artisan/formations` (`title`, `description`, `priceDZD`, `durationHours`, `capacity`, `scheduledAt`, `subCategoryId`). Status becomes `DRAFT`.
2. **Upload Thumbnail**: `POST /api/v1/artisan/formations/{id}/thumbnail` (multipart image, max 10MB).
3. **Attach Syllabus Files**: `POST /api/v1/artisan/formations/{id}/files` (multipart PDF/document, max 25MB, max 10 files).
4. **Submit for Moderation**: `POST /api/v1/artisan/formations/{id}/submit`. Transitions status to `PENDING_REVIEW`. Core edits to an approved workshop reset it back to `PENDING_REVIEW`.
5. **Manage Own Formations**: `GET /api/v1/artisan/formations/me` (paginated list of authored masterclasses with status badges).

#### Discovery, Enrollment & Attendance
- `GET /api/v1/artisan/formations/catalog`: Browse published masterclasses (`status = PUBLISHED`).
- `GET /api/v1/artisan/formations/catalog/{id}`: Detailed workshop syllabus, seats remaining, and schedule.
- `POST /api/v1/artisan/formations/{id}/enroll`: Confirms enrollment reservation. Blocks author self-enrollment and prevents over-capacity bookings (`409 Conflict`).
- `POST /api/v1/artisan/formations/{id}/cancel`: Cancels an enrollment. Enforces a **24-hour cutoff** before `scheduledAt`; cancellations within 24h are rejected (`400 Bad Request`).
- `GET /api/v1/artisan/formations/my-enrollments`: Paginated list of user enrollments (`CONFIRMED`, `ATTENDED`, `CANCELLED`).
- `GET /api/v1/artisan/formations/{id}/files/{fileId}/download`: Secure binary stream download. Only authorized for confirmed enrolled attendees and the instructor.

---

### 5. Formateur Accreditation (`/api/v1/artisan/formateur-request`)

Artisans apply for teacher accreditation to author masterclasses.

#### Workflow
- `POST /api/v1/artisan/formateur-request`: Submits teacher application (`yearsOfExperience`, `motivation`, `specialtyDescription`, `portfolioUrl`).
- **Cooldown Rule**: If an application is rejected by administrators, a 30-day reapplication cooldown is enforced. The API returns `409 Conflict` with `cooldownUntil` date.
- **Frontend Guidance**: Disable the "Apply for Formateur" button if `cooldownUntil` is in the future and display the remaining cooldown days.

---

### 6. Social Feed & Reviews (`/api/v1/feed/**`, `/api/v1/artisans/{id}/reviews`)

Community engagement platform for artisans and clients.

#### Feed Endpoints
- `GET /api/v1/feed`: Paginated published feed. Supports `type`, `authorId`, normalized `tag`, text `q`, and `sort=latest|popular`.
- `GET /api/v1/feed/{id}`: Single post representation with author profile, tags, engagement counters, and attached media array.
- `GET /api/v1/feed/me`: Author's draft, pending, rejected, published, hidden, and removed posts.
- `GET /api/v1/feed/following`: Authenticated client feed based on favorited artisans.
- `POST /api/v1/feed`: Create a draft or submit a new feed post (`permission:artisan:content` for verified artisans or `permission:admin:feed`).
- `POST /api/v1/feed/{id}/submit`: Submit a draft or rejected post for moderation.
- `POST/DELETE /api/v1/feed/{id}/likes`: Idempotent per-user post like/unlike.
- `POST/DELETE /api/v1/feed/{id}/bookmarks`: Save/remove a post; `GET /api/v1/feed/saved` lists saved posts.
- `GET/POST /api/v1/feed/{id}/comments`: Read root comments or add one.
- `GET/POST /api/v1/feed/comments/{commentId}/replies`: Read or add one-level replies.
- `POST/DELETE /api/v1/feed/comments/{commentId}/likes`: Idempotent comment like/unlike.
- `DELETE /api/v1/feed/comments/{commentId}`: Remove a comment as its author, post owner, or feed moderator.
- `POST /api/v1/feed/{id}/share`: Increment share count and return a relative share path.
- `POST /api/v1/feed/{id}/media`: Attach up to 10 images to a post (`multipart/form-data`).
- `DELETE /api/v1/feed/{id}`: Soft-delete authored post.

Feed statuses are `DRAFT`, `PENDING`, `PUBLISHED`, `REJECTED`, `HIDDEN`, and `REMOVED`. Admins use `GET /api/v1/admin/feed/pending`, then publish, reject, hide, or delete posts. Rejections include a moderation note and can be resubmitted by the author.

#### Reviews Endpoints
- `GET /api/v1/artisans/{artisanId}/reviews`: Paginated list of visible reviews (`rating` from 0.00 to 5.00, `comment`, `createdAt`, reviewer name/avatar).
- `POST /api/v1/artisan/formations/{formationId}/reviews`: Submit a review after completing an enrollment (`permission:artisan:reviews`). Requires status `ATTENDED`. Each attendee can review a formation exactly once (`409 Conflict` on duplicate).
- `PUT /api/v1/artisan/reviews/{reviewId}` and `DELETE /api/v1/artisan/reviews/{reviewId}`: Edit or delete owned review.

---

### 7. Real-Time Messaging & Chat (`/api/v1/conversations/**`, STOMP `/ws`)

Private 1-on-1 messaging between platform users backed by RabbitMQ STOMP message relay.

#### REST Endpoints
- `POST /api/v1/conversations`: Initiate or open conversation (`{ "recipientId": string }`). Returns conversation ID.
- `GET /api/v1/conversations`: Paginated list of user conversations with last message snippet, unread counter, and participant profile.
- `GET /api/v1/conversations/{id}/messages`: Paginated message history with cursor traversal (`?before=<timestamp>&size=50`).
- `POST /api/v1/conversations/{id}/messages`: Send message via REST fallback (`{ "content": string, "attachmentIds": string[] }`).
- `PUT /api/v1/conversations/{id}/read`: Mark all messages up to current timestamp as read.
- `POST /api/v1/conversations/{id}/attachments/presign`: Pre-registers attachment upload reservation.

#### WebSocket / STOMP Integration
- **Connection URL**: `ws://<host>:8080/ws` (or SockJS fallback at `http://<host>:8080/ws`).
- **Authentication**: Pass access token in STOMP `CONNECT` frame header:
  ```
  Authorization: Bearer <accessToken>
  ```
- **Subscriptions**:
  - Chat Messages: Subscribe to `/user/queue/messages`.
  - Notifications: Subscribe to `/user/queue/notifications`.
  - Presence: Subscribe to `/topic/presence`.
- **Sending Messages**: Send STOMP frame to `/app/v1/conversations/{conversationId}/send`:
  ```json
  { "content": "Bonjour!", "attachmentIds": [] }
  ```
- **Typing Indicators**: Send to `/app/v1/conversations/{conversationId}/typing` with `{ "typing": true }`. Ephemeral typing events expire automatically after 5 seconds on the client.

---

### 8. In-App Notifications (`/api/v1/notifications/**`)

Centralized user notification feed.

#### Endpoints
- `GET /api/v1/notifications`: Paginated list of user notifications (`ApiResponse<PaginatedResponse<NotificationResponseDTO>>`).
- `GET /api/v1/notifications/unread-count`: Returns raw integer unread count in `data` (e.g. `{ "data": 3 }`).
- `PUT /api/v1/notifications/{id}/read`: Marks single notification as read.
- `PUT /api/v1/notifications/read-all`: Marks all notifications for user as read.
- `DELETE /api/v1/notifications/{id}`: Soft-deletes a notification.

#### UI Implementation Guidance
- Poll `GET /api/v1/notifications/unread-count` every 60 seconds (or listen for STOMP events on `/user/queue/notifications`) to update the header bell counter badge.
- Optimistically decrement the unread badge counter immediately upon clicking a notification before the network call resolves.

---

### 9. Subscriptions & Payments (`/api/v1/subscription/**`)

Tiered subscription monetization powered by Chargily Pay V2 with HMAC-SHA256 signature verification.

#### Subscription Tiers
| Tier | Target Role | Key Features |
|---|---|---|
| `FREE` | Artisan | Up to 3 gallery images, 1 active formation, directory listing |
| `PRO` | Artisan | Up to 10 gallery images, 5 active formations, verified badge eligibility |
| `PREMIUM` | Artisan / Client | Up to 20 gallery images, unlimited formations, unlocked contact details access, priority search |

#### Endpoints
- `GET /api/v1/subscriptions/plans`: Lists all active public subscription plans with pricing in Algerian Dinars (DZD).
- `POST /api/v1/subscriptions/checkout`: Initiates checkout session (`{ "planId": string }`). Returns `{ "checkoutUrl": string, "invoiceId": string }`. Redirect the user's browser to `checkoutUrl` to complete payment.
- `GET /api/v1/subscriptions/current`: Returns active subscription state (`status`, `tier`, `expiresAt`, `autoRenew`, `daysRemaining`).
- `GET /api/v1/subscriptions`: Paginated subscription history for the authenticated user.
- `POST /api/v1/subscriptions/{id}/cancel`: Cancels auto-renewal at period end.
- `POST /api/v1/subscriptions/{id}/renew`: Re-enables auto-renewal.
- `GET /api/v1/payments`: Paginated payment transaction history.
- `GET /api/v1/payments/{id}`: Detailed payment record.

---

### 10. Content Moderation & Abuse Reporting (`/api/v1/reports`)

Authenticated reporting for community safety.

#### Endpoints
- `POST /api/v1/reports`: Submits an abuse report (`permission:report:create`).
  - **Request Body**:
    ```json
    {
      "targetType": "POST",
      "targetId": "post-uuid-123",
      "reason": "SPAM",
      "details": "Repetitive promotional advertising."
    }
    ```
  - `targetType` accepts `USER`, `POST`, or `REVIEW`.
  - Returns `201 Created` with report reference ID.

---

### 11. File Uploads & Profile Avatars (`/api/v1/users/me/avatars`)

Multipart file handling with ClamAV antivirus scanning, magic-byte format validation, and thumbnail variant generation.

#### Endpoints
- `POST /api/v1/users/me/avatars`: Uploads a new avatar (`multipart/form-data`, file part key: `file`). Max 5MB. Formats: JPEG, PNG, WebP. Automatically generates 150x150, 400x400, and original variants. Max 10 avatars per user.
- `GET /api/v1/users/me/avatars`: Lists all gallery avatars with active pointer.
- `PUT /api/v1/users/me/avatars/{id}/activate`: Sets an existing avatar as active.
- `DELETE /api/v1/users/me/avatars/{id}`: Deletes an avatar from gallery and S3 storage.

#### File Upload Rules
- Never manually set `Content-Type: multipart/form-data` with a fixed boundary; let Axios or `fetch` compute the multipart boundary dynamically.
- Pre-validate file sizes in the browser before dispatching network requests to provide instant user feedback.

---

### 12. Client Favorites (`/api/v1/client/favorites/artisans/**`)

Client favorites allow authenticated clients with `permission:client:favorites` to bookmark artisans, list them with pagination, check favorite status, and remove favorites.

#### Endpoints
- `POST /api/v1/client/favorites/artisans/{artisanId}` (HTTP 201): Adds an artisan to favorites. Returns `ApiResponse<ClientFavoriteArtisanResponseDTO>`. Fails with 409 if already favorited (`"Artisan is already favorited."`) or if the per-client cap is reached (`"Client favorite limit reached."`).
- `GET /api/v1/client/favorites/artisans` (HTTP 200): Paginated list of visible favorite artisans (`ApiResponse<PaginatedResponse<ClientFavoriteArtisanItemDTO>>`). Supports `page` (default 0), `size` (default 20, max 100), and `sort` (default `createdAt,desc`; invalid sort properties return HTTP 400 `INVALID_PARAMETER`). Automatically filters out suspended or non-visible artisan profiles.
- `GET /api/v1/client/favorites/artisans/{artisanId}/status` (HTTP 200): Checks whether an artisan is favorited (`ApiResponse<FavoriteStatusResponseDTO>`). Returns `{ "favorited": boolean }`. Returns `{ "favorited": false }` if the artisan exists but is suspended or non-visible; returns 404 only if the artisan does not exist in the database.
- `DELETE /api/v1/client/favorites/artisans/{artisanId}` (HTTP 200): Removes an artisan from favorites. Returns `ApiResponse<Void>` with `data: null` (never HTTP 204). Returns 404 if the favorite does not exist.

*(Note: `favoritedAt` in response payloads is serialized with microsecond fractional precision without timezone suffix, e.g. `"2026-09-24T17:56:45.628794"`).*

#### Privacy & Masking Parity
- Favorites are completely private. Artisans are never notified and cannot see who favorited them.
- Non-client callers receive HTTP 403: callers lacking `permission:client:favorites` are rejected by authorization guards (standard access denied; artisans receive this); callers with the permission who lack a client profile (administrators receive this) get `403 Forbidden` (`"Only registered clients can manage favorites."`).
- Returned `ArtisanDirectoryCardDTO` objects enforce contact privacy: non-premium clients receive an anonymized `artisanName` (`"Artisan #XXXXX"`), matching the public directory masking; premium clients receive the unmasked full name. Note that `ArtisanDirectoryCardDTO` has no phone or email fields.

---

## Pagination, Files, and Permissions Reference

Use the advertised Spring pageable parameters (`page`, `size`, `sort`); the configured default is 20 and maximum is 100. Preserve server-provided page metadata. Multipart requests must use the browser-generated boundary; never manually set `Content-Type: multipart/form-data`. Downloads may be binary streams or redirects, so inspect the response content type and handle `Content-Disposition` safely.

Authorization is permission-based and database-backed. The canonical permission names and ownership rules are in [AUTHORIZATION_MATRIX.md](../AUTHORIZATION_MATRIX.md). UI checks are for usability only; the backend is authoritative.

## Realtime and Reliability Best Practices

WebSocket/STOMP authentication and destinations come from the backend WebSocket configuration and contract tests. Webhooks are server-to-server and are not called by the browser. Respect `Retry-After`; use bounded retries only for idempotent requests or operations with an idempotency key. Do not blindly retry login, checkout, refund, or message creation.

## TypeScript Generation

Generated code is not committed in this repository:

```bash
npx @hey-api/openapi-ts -i ./openapi.json -o ./src/generated/api
# or
npx openapi-typescript ./openapi.json -o ./src/generated/openapi.d.ts
```

Pin the generator version in the frontend lockfile and review the OpenAPI diff whenever a controller, DTO, status, or enum code changes.

## Common Mistakes to Avoid

1. **Role string confusion**: Do not send legacy `ROLE_*` strings; the backend is capability-based (`Permission` enum).
2. **Assumption of 200 OK**: Always inspect HTTP response status and `ApiResponse.code` before parsing data payloads.
3. **Treating 403 as refresh failure**: `403 Forbidden` indicates authorization rejection (e.g. unverified email or missing capability), NOT an expired token. Never trigger token refresh on 403.
4. **Manual multipart boundary**: Never manually set `Content-Type: multipart/form-data`. Let the browser/runtime set the boundary header.
5. **Token leakage**: Never store refresh tokens in unencrypted local storage or print access tokens in telemetry/console logs.
6. **Masked contact info**: Do not attempt to bypass contact info masking on the client; the backend does not transmit phone/email fields when `contactInfoLocked: true`.
