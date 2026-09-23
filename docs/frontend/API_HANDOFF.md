# Souklab frontend API handoff

Status: current backend contract. The backend is the source of truth; generate the OpenAPI document from the running application rather than copying DTOs into the frontend repository.

## Environments

| Environment | API base URL | OpenAPI |
|---|---|---|
| Local | `http://localhost:8080/api/v1` | `http://localhost:8080/v3/api-docs` |
| Test/staging | supplied by deployment | same host + `/v3/api-docs` when enabled |
| Production | supplied by deployment | obtain the private CI artifact |

The path and port are configurable with `PORT`, `OPENAPI_PATH`, and `OPENAPI_SWAGGER_PATH`. Do not hard-code a production hostname.

## Authentication & User Profile (`/auth/**`, `/me`)

Use `Authorization: Bearer <accessToken>` for all protected REST calls. Login and refresh return access/refresh tokens and expiry metadata. Keep tokens in secure storage (e.g. secure memory or HttpOnly cookies) and never log them.

### Primary Auth & Profile Endpoints
- `POST /api/v1/auth/register`: Starts onboarding (Artisan: `PENDING` validation, Client: `ACTIVE`).
- `POST /api/v1/auth/login`: Authenticates credentials, returns `accessToken` (1h), `refreshToken` (24h), and user summary.
- `POST /api/v1/auth/refresh`: Rotates the refresh token and returns a new token pair.
- `POST /api/v1/auth/logout`: Revokes active refresh session.
- `POST /api/v1/auth/verify-email`: Verifies 6-digit OTP code sent on registration.
- `POST /api/v1/auth/resend-verification`: Issues a fresh 6-digit verification code.
- `POST /api/v1/auth/forgot-password`: Requests 6-digit password reset code via email.
- `POST /api/v1/auth/reset-password`: Resets password using the 6-digit code.
- `POST /api/v1/auth/change-password`: Authenticated endpoint to change password.
- `POST /api/v1/auth/complete-profile`: Onboarding wizard to complete craft/business details.
- `GET /api/v1/auth/me`: Retrieves current user profile (`ProfileResponse`).
- `PATCH /api/v1/auth/me`: Partial updates using JSON Merge Patch semantics.
- `POST /api/v1/users/me/avatars`: Uploads and activates a new profile avatar (`multipart/form-data`, key `file`).
- `GET /api/v1/users/me/avatars`: Lists uploaded avatar history.
- `PUT /api/v1/users/me/avatars/{id}/activate`: Re-activates a past gallery avatar.
- `DELETE /api/v1/users/me/avatars/{id}`: Deletes an avatar from storage.

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

## Endpoint groups

| Group | Base paths | Notes |
|---|---|---|
| Auth/account | `/auth/**` | registration, login, refresh, verification, password, profile |
| Catalog/directory | `/catalog/**`, `/directory/**` | public reference data and search |
| Artisan/profile | `/artisan/**`, `/artisans/**` | profiles, certifications, galleries, reviews |
| Feed/moderation | `/feed/**`, `/admin/feed/**`, `/reports/**` | posts, media, reports |
| Formations | `/artisan/formations/**`, `/admin/formations/**` | authoring, files, enrollment, moderation |
| Messaging | `/conversations/**`, STOMP `/app/**` | conversations, messages, attachments |
| Notifications | `/notifications/**` | feed, unread count (raw integer in `data`), read state |
| Subscriptions/payments | `/subscriptions/**`, `/payments/**`, `/admin/**` | checkout, lifecycle, refunds |
| Administration | `/admin/**` | users, permissions, moderation, catalog taxonomy, analytics |
| Files | `/files/**`, avatar and multipart paths | uploads and protected downloads |

Use the generated OpenAPI artifact for exact path/method pairs, schemas, security requirements, and operation IDs. Do not infer routes from this summary.

## Pagination, files, and permissions

Use the advertised Spring pageable parameters (`page`, `size`, `sort`); the configured default is 20 and maximum is 100. Preserve server-provided page metadata. Multipart requests must use the browser-generated boundary; never manually set `Content-Type: multipart/form-data`. Downloads may be binary streams or redirects, so inspect the response content type and handle `Content-Disposition` safely.

Authorization is permission-based and database-backed. The canonical permission names and ownership rules are in [AUTHORIZATION_MATRIX.md](../AUTHORIZATION_MATRIX.md). UI checks are for usability only; the backend is authoritative.

## Realtime and reliability

WebSocket/STOMP authentication and destinations come from the backend WebSocket configuration and contract tests. Webhooks are server-to-server and are not called by the browser. Respect `Retry-After`; use bounded retries only for idempotent requests or operations with an idempotency key. Do not blindly retry login, checkout, refund, or message creation.

## TypeScript generation

Generated code is not committed in this repository:

```bash
npx @hey-api/openapi-ts -i ./openapi.json -o ./src/generated/api
# or
npx openapi-typescript ./openapi.json -o ./src/generated/openapi.d.ts
```

Pin the generator version in the frontend lockfile and review the OpenAPI diff whenever a controller, DTO, status, or enum code changes.

## Common mistakes

Do not send role strings instead of permissions, assume every operation returns 200, treat 403 as refresh failure, manually set a multipart boundary, expose refresh tokens in logs, or rely on historical Postman examples without checking the generated contract.
