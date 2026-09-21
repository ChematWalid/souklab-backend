# Souklab OpenAPI contract

Generated from the running application on 2026-09-21T03:44:24Z. This Markdown view is a human-readable companion to the machine-readable `/v3/api-docs` document.

- OpenAPI version: `3.1.0`
- API title: `Souklab API`
- API version: `1.0.0`
- Paths: `123`
- Schemas: `160`

## Security

### `bearerAuth`

```json
{"type":"http","description":"JWT access token issued by the authentication API.","scheme":"bearer","bearerFormat":"JWT"}
```


## Endpoints

### `/api/v1/users/me/avatars/{id}/activate`

#### PUT — activateAvatar

- Operation ID: `activateAvatar`
- Tags: `avatar-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/{id}/read`

#### PUT — markAsRead

- Operation ID: `markAsRead`
- Tags: `notification-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/read-all`

#### PUT — markAllAsRead

- Operation ID: `markAllAsRead`
- Tags: `notification-controller`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### GET — get

- Operation ID: `get`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### PUT — update

- Operation ID: `update`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### DELETE — remove

- Operation ID: `remove`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/reviews/{reviewId}`

#### PUT — update_1

- Operation ID: `update_1`
- Tags: `artisan-review-controller`
- Parameters:
  - `reviewId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/reviews/{reviewId}`

#### DELETE — delete

- Operation ID: `delete`
- Tags: `artisan-review-controller`
- Parameters:
  - `reviewId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery/order`

#### PUT — reorderGallery

- Operation ID: `reorderGallery`
- Tags: `artisan-gallery-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### GET — getFormationDetails

- Operation ID: `getFormationDetails`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### PUT — updateFormation

- Operation ID: `updateFormation`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### DELETE — deleteFormation

- Operation ID: `deleteFormation`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans/{id}`

#### PUT — update_2

- Operation ID: `update_2`
- Tags: `admin-subscription-plan-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans/{id}`

#### DELETE — deactivate

- Operation ID: `deactivate`
- Tags: `admin-subscription-plan-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars`

#### GET — listAvatars

- Operation ID: `listAvatars`
- Tags: `avatar-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars`

#### POST — uploadAvatar

- Operation ID: `uploadAvatar`
- Tags: `avatar-controller`
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/{id}/renew`

#### POST — renew

- Operation ID: `renew`
- Tags: `subscription-checkout-controller`
- Parameters:
  - `id` (`path`, required)
  - `Idempotency-Key` (`header`, optional)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/{id}/cancel`

#### POST — cancel

- Operation ID: `cancel`
- Tags: `subscription-account-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/checkout`

#### POST — checkout

- Operation ID: `checkout`
- Tags: `subscription-checkout-controller`
- Parameters:
  - `Idempotency-Key` (`header`, optional)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/reports`

#### POST — create

- Operation ID: `create`
- Tags: `content-report-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/integrations/chargily/webhook`

#### POST — webhook

- Operation ID: `webhook`
- Tags: `chargily-webhook-controller`
- Parameters:
  - `signature` (`header`, optional)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed`

#### GET — list

- Operation ID: `list`
- Tags: `feed-post-controller`
- Parameters:
  - `type` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed`

#### POST — create_1

- Operation ID: `create_1`
- Tags: `feed-post-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/media`

#### POST — addMedia

- Operation ID: `addMedia`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/conversations`

#### GET — list_1

- Operation ID: `list_1`
- Tags: `conversation-controller`
- Parameters:
  - `archived` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/conversations`

#### POST — create_2

- Operation ID: `create_2`
- Tags: `conversation-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/read`

#### POST — read

- Operation ID: `read`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/messages`

#### GET — messages

- Operation ID: `messages`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
  - `cursor` (`query`, optional)
  - `size` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/messages`

#### POST — send

- Operation ID: `send`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/attachments`

#### POST — uploadAttachment

- Operation ID: `uploadAttachment`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/verify-email`

#### POST — verifyEmail

- Operation ID: `verifyEmail`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/reset-password`

#### POST — resetPassword

- Operation ID: `resetPassword`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/resend-verification`

#### POST — resendVerification

- Operation ID: `resendVerification`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/register`

#### POST — register

- Operation ID: `register`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/refresh`

#### POST — refreshToken

- Operation ID: `refreshToken`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/logout`

#### POST — logout

- Operation ID: `logout`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/login`

#### POST — login

- Operation ID: `login`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/forgot-password`

#### POST — forgotPassword

- Operation ID: `forgotPassword`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/complete-profile`

#### POST — completeProfile

- Operation ID: `completeProfile`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/change-password`

#### POST — changePassword

- Operation ID: `changePassword`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery`

#### GET — getMyGallery

- Operation ID: `getMyGallery`
- Tags: `artisan-gallery-controller`
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery`

#### POST — uploadImage

- Operation ID: `uploadImage`
- Tags: `artisan-gallery-controller`
- Parameters:
  - `title` (`query`, optional)
  - `caption` (`query`, optional)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations`

#### POST — createFormation

- Operation ID: `createFormation`
- Tags: `artisan-formation-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/thumbnail`

#### POST — uploadThumbnail

- Operation ID: `uploadThumbnail`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/submit`

#### POST — submitForReview

- Operation ID: `submitForReview`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files`

#### POST — uploadCourseFile

- Operation ID: `uploadCourseFile`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/enroll`

#### POST — enroll

- Operation ID: `enroll`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/cancel`

#### POST — cancel_1

- Operation ID: `cancel_1`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{formationId}/reviews`

#### POST — create_3

- Operation ID: `create_3`
- Tags: `artisan-review-controller`
- Parameters:
  - `formationId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formateur-request`

#### POST — submitRequest

- Operation ID: `submitRequest`
- Tags: `artisan-formateur-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications`

#### GET — getMyCertifications

- Operation ID: `getMyCertifications`
- Tags: `artisan-certification-controller`
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications`

#### POST — uploadCertification

- Operation ID: `uploadCertification`
- Tags: `artisan-certification-controller`
- Parameters:
  - `title` (`query`, required)
  - `issuer` (`query`, required)
  - `issuedAt` (`query`, optional)
  - `expiresAt` (`query`, optional)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{userId}/permissions`

#### GET — list_2

- Operation ID: `list_2`
- Tags: `permission-management-controller`
- Parameters:
  - `userId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{userId}/permissions`

#### POST — grant

- Operation ID: `grant`
- Tags: `permission-management-controller`
- Parameters:
  - `userId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{userId}/permissions`

#### DELETE — revoke

- Operation ID: `revoke`
- Tags: `permission-management-controller`
- Parameters:
  - `userId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{id}/unban`

#### POST — unbanUser

- Operation ID: `unbanUser`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{id}/timeout`

#### POST — timeoutUser

- Operation ID: `timeoutUser`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{id}/ban`

#### POST — banUser

- Operation ID: `banUser`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{id}/approve`

#### POST — approveUser

- Operation ID: `approveUser`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/approve-bulk`

#### POST — approveUsersBulk

- Operation ID: `approveUsersBulk`
- Tags: `user-management-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/{id}/revoke`

#### POST — revoke_1

- Operation ID: `revoke_1`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/{id}/correct-state`

#### POST — correctSubscriptionState

- Operation ID: `correctSubscriptionState`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/{id}/cancel`

#### POST — cancel_2

- Operation ID: `cancel_2`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/payments/{id}/correct-state`

#### POST — correctPaymentState

- Operation ID: `correctPaymentState`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/grant`

#### POST — grant_1

- Operation ID: `grant_1`
- Tags: `admin-subscription-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans`

#### GET — list_3

- Operation ID: `list_3`
- Tags: `admin-subscription-plan-controller`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans`

#### POST — create_4

- Operation ID: `create_4`
- Tags: `admin-subscription-plan-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/reports/{id}/resolve`

#### POST — resolve

- Operation ID: `resolve`
- Tags: `content-report-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/payments/{id}/refund`

#### POST — rejectRefund

- Operation ID: `rejectRefund`
- Tags: `admin-refund-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/formations/{id}/review`

#### POST — reviewFormation

- Operation ID: `reviewFormation`
- Tags: `admin-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/formations/{id}/publish`

#### POST — publishFormation

- Operation ID: `publishFormation`
- Tags: `admin-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/formateur-requests/{id}/reject`

#### POST — rejectRequest

- Operation ID: `rejectRequest`
- Tags: `admin-formateur-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/formateur-requests/{id}/approve`

#### POST — approveRequest

- Operation ID: `approveRequest`
- Tags: `admin-formateur-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/formateur-requests/{artisanId}/lift-cooldown`

#### POST — liftCooldown

- Operation ID: `liftCooldown`
- Tags: `admin-formateur-controller`
- Parameters:
  - `artisanId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/remove`

#### POST — remove_1

- Operation ID: `remove_1`
- Tags: `admin-feed-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/publish`

#### POST — publish

- Operation ID: `publish`
- Tags: `admin-feed-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/hide`

#### POST — hide

- Operation ID: `hide`
- Tags: `admin-feed-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/artisans/{artisanId}/formateur-revoke`

#### POST — revokeDirectly

- Operation ID: `revokeDirectly`
- Tags: `admin-formateur-controller`
- Parameters:
  - `artisanId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/artisans/{artisanId}/formateur-grant`

#### POST — grantDirectly

- Operation ID: `grantDirectly`
- Tags: `admin-formateur-controller`
- Parameters:
  - `artisanId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/rebuild`

#### POST — Queue a rollup rebuild (compatibility alias)

- Operation ID: `rebuild`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/rebuild`

#### POST — Queue a rollup rebuild (compatibility alias)

- Operation ID: `rebuild_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/jobs/rebuild`

#### POST — Queue a rollup rebuild

- Operation ID: `queueRebuild`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/jobs/rebuild`

#### POST — Queue a rollup rebuild

- Operation ID: `queueRebuild_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/jobs/backfill`

#### POST — Queue historical backfill

- Operation ID: `queueBackfill`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/jobs/backfill`

#### POST — Queue historical backfill

- Operation ID: `queueBackfill_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/backfill`

#### POST — Queue historical backfill (compatibility alias)

- Operation ID: `backfill`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/backfill`

#### POST — Queue historical backfill (compatibility alias)

- Operation ID: `backfill_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs`

#### POST — Submit an analytics job

- Operation ID: `submit`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs`

#### POST — Submit an analytics job

- Operation ID: `submit_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/archive`

#### PATCH — archive

- Operation ID: `archive`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{conversationId}/messages/{messageId}`

#### DELETE — delete_1

- Operation ID: `delete_1`
- Tags: `conversation-controller`
- Parameters:
  - `conversationId` (`path`, required)
  - `messageId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/conversations/{conversationId}/messages/{messageId}`

#### PATCH — edit

- Operation ID: `edit`
- Tags: `conversation-controller`
- Parameters:
  - `conversationId` (`path`, required)
  - `messageId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/me`

#### GET — getCurrentUser

- Operation ID: `getCurrentUser`
- Tags: `auth-controller`
- Responses:
  - `200` — OK

### `/api/v1/auth/me`

#### PATCH — patchCurrentUser

- Operation ID: `patchCurrentUser`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions`

#### GET — history

- Operation ID: `history`
- Tags: `subscription-account-controller`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/plans`

#### GET — listPlans

- Operation ID: `listPlans`
- Tags: `subscription-plan-controller`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/current`

#### GET — current

- Operation ID: `current`
- Tags: `subscription-account-controller`
- Responses:
  - `200` — OK

### `/api/v1/public/directory`

#### GET — search

- Operation ID: `search`
- Tags: `directory-controller`
- Parameters:
  - `filter` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/payments`

#### GET — payments

- Operation ID: `payments`
- Tags: `subscription-account-controller`
- Responses:
  - `200` — OK

### `/api/v1/payments/{id}`

#### GET — payment

- Operation ID: `payment`
- Tags: `subscription-account-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications`

#### GET — getNotifications

- Operation ID: `getNotifications`
- Tags: `notification-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/unread-count`

#### GET — getUnreadCount

- Operation ID: `getUnreadCount`
- Tags: `notification-controller`
- Responses:
  - `200` — OK

### `/api/v1/files/{key}`

#### GET — serveFile

- Operation ID: `serveFile`
- Tags: `file-serving-controller`
- Parameters:
  - `key` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/catalog/techniques`

#### GET — getTechniques

- Operation ID: `getTechniques`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/catalog/regions`

#### GET — getRegions

- Operation ID: `getRegions`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/catalog/materials`

#### GET — getMaterials

- Operation ID: `getMaterials`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/catalog/epoques`

#### GET — getEpoques

- Operation ID: `getEpoques`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/catalog/categories`

#### GET — getCategories

- Operation ID: `getCategories`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/auth/oauth/google/client`

#### GET — initiateGoogleOAuthClient

- Operation ID: `initiateGoogleOAuthClient`
- Tags: `auth-controller`
- Responses:
  - `200` — OK

### `/api/v1/auth/oauth/google/artisan`

#### GET — initiateGoogleOAuthArtisan

- Operation ID: `initiateGoogleOAuthArtisan`
- Tags: `auth-controller`
- Responses:
  - `200` — OK

### `/api/v1/artisans/{artisanId}/reviews`

#### GET — list_4

- Operation ID: `list_4`
- Tags: `artisan-review-controller`
- Parameters:
  - `artisanId` (`path`, required)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/{id}`

#### GET — getArtisanProfile

- Operation ID: `getArtisanProfile`
- Tags: `artisan-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files/{fileId}/download`

#### GET — downloadCourseFile

- Operation ID: `downloadCourseFile`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `id` (`path`, required)
  - `fileId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/my-enrollments`

#### GET — getMyEnrollments

- Operation ID: `getMyEnrollments`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/me`

#### GET — getMyFormations

- Operation ID: `getMyFormations`
- Tags: `artisan-formation-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/catalog`

#### GET — getPublishedCatalog

- Operation ID: `getPublishedCatalog`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/catalog/{id}`

#### GET — getPublishedFormationDetails

- Operation ID: `getPublishedFormationDetails`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users`

#### GET — getAllUsers

- Operation ID: `getAllUsers`
- Tags: `user-management-controller`
- Parameters:
  - `search` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/pending`

#### GET — getPendingUsers

- Operation ID: `getPendingUsers`
- Tags: `user-management-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions`

#### GET — subscriptions

- Operation ID: `subscriptions`
- Tags: `admin-subscription-controller`
- Parameters:
  - `limit` (`query`, optional)
  - `query` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/webhooks`

#### GET — webhooks

- Operation ID: `webhooks`
- Tags: `admin-subscription-controller`
- Parameters:
  - `limit` (`query`, optional)
  - `query` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/payments`

#### GET — payments_1

- Operation ID: `payments_1`
- Tags: `admin-subscription-controller`
- Parameters:
  - `limit` (`query`, optional)
  - `query` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/admin/reports`

#### GET — list_5

- Operation ID: `list_5`
- Tags: `content-report-controller`
- Parameters:
  - `status` (`query`, optional)
  - `targetType` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/formations/pending`

#### GET — getPendingFormations

- Operation ID: `getPendingFormations`
- Tags: `admin-formation-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/formateur-requests`

#### GET — getPendingRequests

- Operation ID: `getPendingRequests`
- Tags: `admin-formateur-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/pending`

#### GET — listPending

- Operation ID: `listPending`
- Tags: `admin-feed-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/jobs/{id}`

#### GET — Get maintenance job status

- Operation ID: `maintenanceStatus`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/jobs/{id}`

#### GET — Get maintenance job status

- Operation ID: `maintenanceStatus_1`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs/{id}/result`

#### GET — Fetch an analytics result

- Operation ID: `result`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs/{id}/result`

#### GET — Fetch an analytics result

- Operation ID: `result_1`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs/{id}/download`

#### GET — Download an analytics result

- Operation ID: `download`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs/{id}/download`

#### GET — Download an analytics result

- Operation ID: `download_1`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs/{id}`

#### GET — Get analytics job status

- Operation ID: `status`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs/{id}`

#### DELETE — Delete an analytics job

- Operation ID: `delete_2`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs/{id}`

#### GET — Get analytics job status

- Operation ID: `status_1`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs/{id}`

#### DELETE — Delete an analytics job

- Operation ID: `delete_3`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars/{id}`

#### DELETE — deleteAvatar

- Operation ID: `deleteAvatar`
- Tags: `avatar-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/{id}`

#### DELETE — deleteNotification

- Operation ID: `deleteNotification`
- Tags: `notification-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/media/{mediaId}`

#### DELETE — removeMedia

- Operation ID: `removeMedia`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
  - `mediaId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery/{id}`

#### DELETE — deleteImage

- Operation ID: `deleteImage`
- Tags: `artisan-gallery-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files/{fileId}`

#### DELETE — deleteCourseFile

- Operation ID: `deleteCourseFile`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
  - `fileId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications/{id}`

#### DELETE — deleteCertification

- Operation ID: `deleteCertification`
- Tags: `artisan-certification-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

## Schemas

### `ApiResponseAvatarResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AvatarResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `AvatarResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"urlOriginal":{"type":"string"},"urlMedium":{"type":"string"},"urlThumbnail":{"type":"string"},"originalFilename":{"type":"string"},"contentType":{"type":"string"},"fileSize":{"type":"integer","format":"int64"},"uploadedAt":{"type":"string","format":"date-time"},"active":{"type":"boolean"},"isActive":{"type":"boolean"}}}
```

### `ApiResponseNotificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/NotificationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `NotificationResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"message":{"type":"string"},"type":{"type":"string"},"targetId":{"type":"string"},"createdAt":{"type":"string","format":"date-time"},"read":{"type":"boolean"}}}
```

### `ApiResponseVoid`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FeedPostCreateDTO`

```json
{"type":"object","properties":{"type":{"type":"string","enum":["ACTUALITE","FORMATION","ANNONCE"]},"title":{"type":"string","maxLength":200,"minLength":0},"body":{"type":"string","maxLength":10000,"minLength":0},"formationId":{"type":"string","maxLength":36,"minLength":0}},"required":["body","title","type"]}
```

### `ApiResponseFeedPostResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FeedPostResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FeedPostMediaResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"url":{"type":"string"},"contentType":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `FeedPostResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"authorId":{"type":"string"},"authorName":{"type":"string"},"type":{"type":"string","enum":["ACTUALITE","FORMATION","ANNONCE"]},"title":{"type":"string"},"body":{"type":"string"},"status":{"type":"string","enum":["PENDING","PUBLISHED","HIDDEN","REMOVED"]},"formationId":{"type":"string"},"publishedAt":{"type":"string","format":"date-time"},"moderationNote":{"type":"string"},"media":{"type":"array","items":{"$ref":"#/components/schemas/FeedPostMediaResponseDTO"}}}}
```

### `ArtisanReviewRequestDTO`

```json
{"type":"object","properties":{"rating":{"type":"number","maximum":5.00,"minimum":0.00},"comment":{"type":"string","maxLength":5000,"minLength":0}},"required":["comment","rating"]}
```

### `ApiResponseArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ArtisanReviewResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"reviewerId":{"type":"string"},"reviewerName":{"type":"string"},"artisanId":{"type":"string"},"formationId":{"type":"string"},"rating":{"type":"number"},"comment":{"type":"string"},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"}}}
```

### `FormationUpdateDTO`

```json
{"type":"object","properties":{"title":{"type":"string","maxLength":255,"minLength":0},"description":{"type":"string","minLength":1},"location":{"type":"string"},"isOnline":{"type":"boolean"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32","minimum":1},"maxParticipants":{"type":"integer","format":"int32","minimum":1},"price":{"type":"integer","format":"int32","minimum":0},"currency":{"type":"string"},"online":{"type":"boolean"}},"required":["description","title"]}
```

### `ApiResponseFormationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormationAuthorDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"avatarUrl":{"type":"string"},"city":{"type":"string"},"teacher":{"type":"boolean"}}}
```

### `FormationFileResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"originalFilename":{"type":"string"},"contentType":{"type":"string"},"fileSize":{"type":"integer","format":"int64"},"downloadUrl":{"type":"string"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `FormationResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"author":{"$ref":"#/components/schemas/FormationAuthorDTO"},"title":{"type":"string"},"description":{"type":"string"},"thumbnailUrl":{"type":"string"},"location":{"type":"string"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32"},"maxParticipants":{"type":"integer","format":"int32"},"price":{"type":"integer","format":"int32"},"currency":{"type":"string"},"status":{"type":"string","enum":["DRAFT","PENDING_REVIEW","APPROVED","REJECTED","PUBLISHED","CANCELLED","COMPLETED"]},"activeEnrollmentsCount":{"type":"integer","format":"int64"},"files":{"type":"array","items":{"$ref":"#/components/schemas/FormationFileResponseDTO"}},"reviews":{"type":"array","items":{"$ref":"#/components/schemas/FormationReviewResponseDTO"}},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"},"online":{"type":"boolean"}}}
```

### `FormationReviewResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"adminId":{"type":"string"},"adminName":{"type":"string"},"decision":{"type":"string","enum":["APPROVED","REJECTED"]},"comment":{"type":"string"},"reviewedAt":{"type":"string","format":"date-time"}}}
```

### `SubscriptionPlanRequest`

```json
{"type":"object","properties":{"subscriberType":{"type":"string","enum":["ARTISAN","CLIENT"]},"name":{"type":"string","minLength":1},"description":{"type":"string"},"billingPeriod":{"type":"string","enum":["MONTHLY","YEARLY"]},"amount":{"type":"integer","format":"int64","exclusiveMinimum":0},"active":{"type":"boolean"},"entitlements":{"type":"object","additionalProperties":{"type":"string"}},"reason":{"type":"string","minLength":1}},"required":["billingPeriod","name","reason","subscriberType"]}
```

### `ApiResponseSubscriptionPlanResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/SubscriptionPlanResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `SubscriptionPlanResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"subscriberType":{"type":"string","enum":["ARTISAN","CLIENT"]},"name":{"type":"string"},"description":{"type":"string"},"billingPeriod":{"type":"string","enum":["MONTHLY","YEARLY"]},"amount":{"type":"integer","format":"int64"},"currency":{"type":"string"},"active":{"type":"boolean"},"entitlements":{"type":"object","additionalProperties":{"type":"string"}}}}
```

### `SubscriptionCheckoutRequest`

```json
{"type":"object","properties":{"planId":{"type":"string","minLength":1},"successUrl":{"type":"string"},"failureUrl":{"type":"string"}},"required":["planId"]}
```

### `ApiResponseSubscriptionCheckoutResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/SubscriptionCheckoutResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `SubscriptionCheckoutResponse`

```json
{"type":"object","properties":{"paymentId":{"type":"string"},"subscriptionId":{"type":"string"},"providerCheckoutId":{"type":"string"},"checkoutUrl":{"type":"string"}}}
```

### `ContentReportRequestDTO`

```json
{"type":"object","properties":{"targetType":{"type":"string","enum":["USER","POST","REVIEW"]},"targetId":{"type":"string","maxLength":36,"minLength":0},"reason":{"type":"string","maxLength":100,"minLength":0},"details":{"type":"string","maxLength":5000,"minLength":0}},"required":["reason","targetId","targetType"]}
```

### `ApiResponseContentReportResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ContentReportResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ContentReportResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"reporterId":{"type":"string"},"targetType":{"type":"string","enum":["USER","POST","REVIEW"]},"targetId":{"type":"string"},"reason":{"type":"string"},"details":{"type":"string"},"status":{"type":"string","enum":["OPEN","DISMISSED","RESOLVED"]},"resolutionAction":{"type":"string","enum":["DISMISS","HIDE","REMOVE"]},"resolverId":{"type":"string"},"resolutionNote":{"type":"string"},"resolvedAt":{"type":"string","format":"date-time"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponseFeedPostMediaResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FeedPostMediaResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `CreateConversationRequest`

```json
{"type":"object","properties":{"recipientUserId":{"type":"string","minLength":1}},"required":["recipientUserId"]}
```

### `ApiResponseConversationResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ConversationResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ConversationResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"participantUserId":{"type":"string"},"participantName":{"type":"string"},"archived":{"type":"boolean"},"lastMessagePreview":{"type":"string"},"unreadCount":{"type":"integer","format":"int64"},"updatedAt":{"type":"string","format":"date-time"}}}
```

### `ReadReceiptRequest`

```json
{"type":"object","properties":{"messageId":{"type":"string"}}}
```

### `SendMessageRequest`

```json
{"type":"object","properties":{"idempotencyKey":{"type":"string","maxLength":128,"minLength":0},"content":{"type":"string","minLength":1},"attachmentKeys":{"type":"array","items":{"type":"string","minLength":1}}},"required":["content","idempotencyKey"]}
```

### `ApiResponseMessageResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/MessageResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MessageAttachmentResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"filename":{"type":"string"},"contentType":{"type":"string"},"size":{"type":"integer","format":"int64"},"downloadUrl":{"type":"string"}}}
```

### `MessageResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"conversationId":{"type":"string"},"authorId":{"type":"string"},"content":{"type":"string"},"deleted":{"type":"boolean"},"createdAt":{"type":"string","format":"date-time"},"editedAt":{"type":"string","format":"date-time"},"attachments":{"type":"array","items":{"$ref":"#/components/schemas/MessageAttachmentResponse"}}}}
```

### `ApiResponseAttachmentUploadResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AttachmentUploadResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `AttachmentUploadResponse`

```json
{"type":"object","properties":{"key":{"type":"string"},"filename":{"type":"string"},"contentType":{"type":"string"},"size":{"type":"integer","format":"int64"}}}
```

### `VerifyEmailRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"code":{"type":"string","minLength":1,"pattern":"\\d{6}"}},"required":["code","email"]}
```

### `ResetPasswordRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"code":{"type":"string","minLength":1,"pattern":"\\d{6}"},"newPassword":{"type":"string","maxLength":2147483647,"minLength":8}},"required":["code","email","newPassword"]}
```

### `ResendVerificationRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1}},"required":["email"]}
```

### `UserRegistrationDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"password":{"type":"string","maxLength":2147483647,"minLength":8},"name":{"type":"string"},"firstName":{"type":"string"},"lastName":{"type":"string"},"accountType":{"type":"string","enum":["ADMIN","ARTISAN","CLIENT"]}},"required":["accountType","email","password"]}
```

### `ApiResponseProfileResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ProfileResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ProfileResponse`

```json
{"type":"object","properties":{"name":{"type":"string"},"permissions":{"type":"array","items":{"type":"string"},"uniqueItems":true},"id":{"type":"string"},"email":{"type":"string"},"accountStatus":{"type":"string","enum":["PENDING","ACTIVE","SUSPENDED","REJECTED"]},"createdAt":{"type":"string","format":"date-time"},"phone":{"type":"string"},"avatarUrl":{"type":"string"},"emailVerified":{"type":"boolean"},"firstName":{"type":"string"},"lastName":{"type":"string"},"updatedAt":{"type":"string","format":"date-time"},"emailVerifiedAt":{"type":"string","format":"date-time"}}}
```

### `TokenRefreshRequestDTO`

```json
{"type":"object","properties":{"refreshToken":{"type":"string","minLength":1}},"required":["refreshToken"]}
```

### `ApiResponseJwtResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/JwtResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `JwtResponseDTO`

```json
{"type":"object","properties":{"accessToken":{"type":"string"},"refreshToken":{"type":"string"},"tokenType":{"type":"string","enum":["Bearer"]},"expiresIn":{"type":"integer","format":"int64"},"user":{"$ref":"#/components/schemas/ProfileResponse"},"permissions":{"type":"array","items":{"type":"string"}}}}
```

### `LoginDTO`

```json
{"type":"object","properties":{"email":{"type":"string"},"username":{"type":"string"},"password":{"type":"string","minLength":1},"loginIdentifier":{"type":"string"}},"required":["password"]}
```

### `ForgotPasswordRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1}},"required":["email"]}
```

### `CompleteProfileRequestDTO`

```json
{"type":"object","properties":{"regionId":{"type":"string"},"region":{"type":"string"},"city":{"type":"string"},"bio":{"type":"string"},"address":{"type":"string"},"website":{"type":"string"},"subCategoryId":{"type":"string"},"materialIds":{"type":"array","items":{"type":"string"}},"epoqueIds":{"type":"array","items":{"type":"string"}},"techniqueIds":{"type":"array","items":{"type":"string"}},"clientType":{"type":"string","enum":["INDIVIDUAL","BUSINESS","ENTERPRISE"]},"companyName":{"type":"string"}}}
```

### `ChangePasswordRequestDTO`

```json
{"type":"object","properties":{"oldPassword":{"type":"string","minLength":1},"newPassword":{"type":"string","maxLength":2147483647,"minLength":8}},"required":["newPassword","oldPassword"]}
```

### `ApiResponseGalleryImageResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/GalleryImageResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `GalleryImageResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"imageUrl":{"type":"string"},"title":{"type":"string"},"caption":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `FormationCreateDTO`

```json
{"type":"object","properties":{"title":{"type":"string","maxLength":255,"minLength":0},"description":{"type":"string","minLength":1},"location":{"type":"string"},"isOnline":{"type":"boolean"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32","minimum":1},"maxParticipants":{"type":"integer","format":"int32","minimum":1},"price":{"type":"integer","format":"int32","minimum":0},"currency":{"type":"string"},"online":{"type":"boolean"}},"required":["description","title"]}
```

### `ApiResponseFormationFileResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormationFileResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseFormationEnrollmentResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormationEnrollmentResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormationEnrollmentResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"formationId":{"type":"string"},"formationTitle":{"type":"string"},"artisanId":{"type":"string"},"artisanName":{"type":"string"},"status":{"type":"string","enum":["CONFIRMED","ATTENDED","CANCELLED"]},"enrolledAt":{"type":"string","format":"date-time"},"cancelledAt":{"type":"string","format":"date-time"}}}
```

### `FormateurRequestDTO`

```json
{"type":"object","properties":{"motivation":{"type":"string"}}}
```

### `ApiResponseFormateurRequestResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormateurRequestResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormateurRequestResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"artisanId":{"type":"string"},"artisanName":{"type":"string"},"artisanEmail":{"type":"string"},"status":{"type":"string","enum":["PENDING","APPROVED","REJECTED"]},"motivation":{"type":"string"},"adminNote":{"type":"string"},"canReapply":{"type":"boolean"},"cooldownUntil":{"type":"string","format":"date-time"},"decidedByAdminId":{"type":"string"},"decidedByAdminEmail":{"type":"string"},"decidedAt":{"type":"string","format":"date-time"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponseCertificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/CertificationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `CertificationResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"title":{"type":"string"},"issuer":{"type":"string"},"issuedAt":{"type":"string","format":"date"},"expiresAt":{"type":"string","format":"date"},"documentUrl":{"type":"string"},"verified":{"type":"boolean"}}}
```

### `PermissionAssignmentRequestDTO`

```json
{"type":"object","properties":{"permissionKey":{"type":"string"}},"required":["permissionKey"]}
```

### `ApiResponseSetPermission`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"type":"string"},"uniqueItems":true},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `TimeoutRequestDTO`

```json
{"type":"object","properties":{"minutes":{"type":"integer","format":"int32","exclusiveMinimum":0},"reason":{"type":"string"}}}
```

### `BanRequestDTO`

```json
{"type":"object","properties":{"reason":{"type":"string","minLength":1}},"required":["reason"]}
```

### `FinancialReasonRequest`

```json
{"type":"object","properties":{"reason":{"type":"string","minLength":1}},"required":["reason"]}
```

### `SubscriptionStateCorrectionRequest`

```json
{"type":"object","properties":{"status":{"type":"string","enum":["PENDING","ACTIVE","CANCELED","EXPIRED","REVOKED"]},"reason":{"type":"string","minLength":1}},"required":["reason","status"]}
```

### `PaymentStateCorrectionRequest`

```json
{"type":"object","properties":{"status":{"type":"string","enum":["CREATED","PENDING","PAID","FAILED","CANCELED","EXPIRED","MANUALLY_GRANTED"]},"reason":{"type":"string","minLength":1}},"required":["reason","status"]}
```

### `ManualSubscriptionGrantRequest`

```json
{"type":"object","properties":{"accountId":{"type":"string","minLength":1},"planId":{"type":"string","minLength":1},"reason":{"type":"string","minLength":1}},"required":["accountId","planId","reason"]}
```

### `ApiResponseSubscriptionResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/SubscriptionResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `SubscriptionResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"subscriberType":{"type":"string","enum":["ARTISAN","CLIENT"]},"status":{"type":"string","enum":["PENDING","ACTIVE","CANCELED","EXPIRED","REVOKED"]},"planName":{"type":"string"},"billingPeriod":{"type":"string","enum":["MONTHLY","YEARLY"]},"amount":{"type":"integer","format":"int64"},"currency":{"type":"string"},"startsAt":{"type":"string","format":"date-time"},"expiresAt":{"type":"string","format":"date-time"}}}
```

### `ReportResolutionRequestDTO`

```json
{"type":"object","properties":{"action":{"type":"string","enum":["DISMISS","HIDE","REMOVE"]},"note":{"type":"string","maxLength":2000,"minLength":0}},"required":["action","note"]}
```

### `FormationReviewRequestDTO`

```json
{"type":"object","properties":{"decision":{"type":"string","enum":["APPROVED","REJECTED"]},"comment":{"type":"string"}},"required":["decision"]}
```

### `FormateurRejectDTO`

```json
{"type":"object","properties":{"adminNote":{"type":"string","minLength":1},"cooldownUntil":{"type":"string","format":"date-time"},"canReapply":{"type":"boolean"}},"required":["adminNote"]}
```

### `FormateurApproveDTO`

```json
{"type":"object","properties":{"adminNote":{"type":"string","minLength":1}},"required":["adminNote"]}
```

### `FormateurCooldownOverrideDTO`

```json
{"type":"object","properties":{"canReapply":{"type":"boolean"},"cooldownUntil":{"type":"string","format":"date-time"}}}
```

### `FeedPostModerationDTO`

```json
{"type":"object","properties":{"note":{"type":"string","maxLength":2000,"minLength":0}},"required":["note"]}
```

### `FormateurRevokeDTO`

```json
{"type":"object","properties":{"reason":{"type":"string","minLength":1}},"required":["reason"]}
```

### `FormateurGrantDTO`

```json
{"type":"object","properties":{"adminNote":{"type":"string","minLength":1}},"required":["adminNote"]}
```

### `AnalyticsRebuildRequest`

```json
{"type":"object","properties":{"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"}},"required":["fromDate","toDate"]}
```

### `AnalyticsMaintenanceJobResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"operation":{"type":"string","enum":["REBUILD","BACKFILL"]},"status":{"type":"string","enum":["QUEUED","RUNNING","COMPLETED","FAILED","EXPIRED"]},"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"eventsRead":{"type":"integer","format":"int32"},"rollupsWritten":{"type":"integer","format":"int32"},"completedAt":{"type":"string","format":"date-time"},"expiresAt":{"type":"string","format":"date-time"},"failureMessage":{"type":"string"}}}
```

### `ApiResponseAnalyticsMaintenanceJobResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AnalyticsMaintenanceJobResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `AnalyticsJobRequest`

```json
{"type":"object","properties":{"reportType":{"type":"string"},"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"bucket":{"type":"string","enum":["DAY","WEEK","MONTH","QUARTER"]},"filters":{"type":"object","additionalProperties":{"type":"string"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"sortField":{"type":"string"},"sortDirection":{"type":"string","enum":["ASC","DESC"]},"outputFormat":{"type":"string","enum":["JSON","CSV"]}},"required":["bucket","fromDate","reportType","toDate"]}
```

### `AnalyticsJobResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"reportType":{"type":"string"},"status":{"type":"string","enum":["QUEUED","RUNNING","COMPLETED","FAILED","EXPIRED"]},"bucket":{"type":"string","enum":["DAY","WEEK","MONTH","QUARTER"]},"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"outputFormat":{"type":"string","enum":["JSON","CSV"]},"completedAt":{"type":"string","format":"date-time"},"expiresAt":{"type":"string","format":"date-time"},"failureMessage":{"type":"string"}}}
```

### `ApiResponseAnalyticsJobResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AnalyticsJobResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ArchiveConversationRequest`

```json
{"type":"object","properties":{"archived":{"type":"boolean"}}}
```

### `EditMessageRequest`

```json
{"type":"object","properties":{"content":{"type":"string","minLength":1}},"required":["content"]}
```

### `PatchFieldClientType`

```json
{"type":"object","properties":{"defined":{"type":"boolean"},"value":{"type":"string","enum":["INDIVIDUAL","BUSINESS","ENTERPRISE"]},"null":{"type":"boolean"}}}
```

### `PatchFieldListString`

```json
{"type":"object","properties":{"defined":{"type":"boolean"},"value":{"type":"array","items":{"type":"string"}},"null":{"type":"boolean"}}}
```

### `PatchFieldString`

```json
{"type":"object","properties":{"defined":{"type":"boolean"},"value":{"type":"string"},"null":{"type":"boolean"}}}
```

### `UserPatchDTO`

```json
{"type":"object","properties":{"bio":{"$ref":"#/components/schemas/PatchFieldString"},"city":{"$ref":"#/components/schemas/PatchFieldString"},"address":{"$ref":"#/components/schemas/PatchFieldString"},"website":{"$ref":"#/components/schemas/PatchFieldString"},"regionId":{"$ref":"#/components/schemas/PatchFieldString"},"region":{"$ref":"#/components/schemas/PatchFieldString"},"subCategoryId":{"$ref":"#/components/schemas/PatchFieldString"},"materialIds":{"$ref":"#/components/schemas/PatchFieldListString"},"techniqueIds":{"$ref":"#/components/schemas/PatchFieldListString"},"epoqueIds":{"$ref":"#/components/schemas/PatchFieldListString"},"companyName":{"$ref":"#/components/schemas/PatchFieldString"},"clientType":{"$ref":"#/components/schemas/PatchFieldClientType"},"empty":{"type":"boolean"}}}
```

### `Pageable`

```json
{"type":"object","properties":{"page":{"type":"integer","format":"int32","minimum":0},"size":{"type":"integer","format":"int32","minimum":1},"sort":{"type":"array","items":{"type":"string"}}}}
```

### `ApiResponsePaginatedResponseAvatarResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseAvatarResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseAvatarResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/AvatarResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseListSubscriptionResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/SubscriptionResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListSubscriptionPlanResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/SubscriptionPlanResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `DirectorySearchFilterDTO`

```json
{"type":"object","properties":{"keyword":{"type":"string","maxLength":120,"minLength":0},"regionSlug":{"type":"string","maxLength":120,"minLength":0},"wilayaCode":{"type":"string","maxLength":10,"minLength":0},"categorySlug":{"type":"string","maxLength":120,"minLength":0},"subCategorySlug":{"type":"string","maxLength":120,"minLength":0},"materials":{"type":"array","items":{"type":"string"}},"techniques":{"type":"array","items":{"type":"string"}},"epoques":{"type":"array","items":{"type":"string"}},"minRating":{"type":"number","format":"double","maximum":5.0,"minimum":0.0},"verifiedOnly":{"type":"boolean"},"premiumOnly":{"type":"boolean"},"teacherOnly":{"type":"boolean"},"sortBy":{"type":"string"},"page":{"type":"integer","format":"int32","minimum":0},"size":{"type":"integer","format":"int32","maximum":100,"minimum":1},"q":{"type":"string"},"cleanKeyword":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseArtisanDirectoryCardDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseArtisanDirectoryCardDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ArtisanDirectoryCardDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"artisanName":{"type":"string"},"avatarUrl":{"type":"string"},"coverImageUrl":{"type":"string"},"bioSnippet":{"type":"string"},"city":{"type":"string"},"wilayaName":{"type":"string"},"wilayaCode":{"type":"string"},"regionSlug":{"type":"string"},"categoryName":{"type":"string"},"categorySlug":{"type":"string"},"subCategoryName":{"type":"string"},"subCategorySlug":{"type":"string"},"rating":{"type":"number","format":"double"},"reviewsCount":{"type":"integer","format":"int32"},"viewsCount":{"type":"integer","format":"int32"},"verified":{"type":"boolean"},"premium":{"type":"boolean"},"teacher":{"type":"boolean"},"primaryMaterials":{"type":"array","items":{"type":"string"}},"primaryTechniques":{"type":"array","items":{"type":"string"}},"createdAt":{"type":"string","format":"date-time"}}}
```

### `PaginatedResponseArtisanDirectoryCardDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/ArtisanDirectoryCardDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseListPaymentResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/PaymentResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaymentResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"subscriptionId":{"type":"string"},"provider":{"type":"string","enum":["CHARGILY"]},"status":{"type":"string","enum":["CREATED","PENDING","PAID","FAILED","CANCELED","EXPIRED","MANUALLY_GRANTED"]},"amount":{"type":"integer","format":"int64"},"currency":{"type":"string"},"checkoutUrl":{"type":"string"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponsePaymentResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaymentResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseNotificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseNotificationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseNotificationResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/NotificationResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseLong`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"integer","format":"int64"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `StreamingResponseBody`

```json
{}
```

### `ApiResponsePageFeedPostResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PageFeedPostResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PageFeedPostResponseDTO`

```json
{"type":"object","properties":{"totalPages":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"size":{"type":"integer","format":"int32"},"content":{"type":"array","items":{"$ref":"#/components/schemas/FeedPostResponseDTO"}},"number":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"pageable":{"$ref":"#/components/schemas/PageableObject"},"numberOfElements":{"type":"integer","format":"int32"},"first":{"type":"boolean"},"last":{"type":"boolean"},"empty":{"type":"boolean"}}}
```

### `PageableObject`

```json
{"type":"object","properties":{"offset":{"type":"integer","format":"int64"},"paged":{"type":"boolean"},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"unpaged":{"type":"boolean"}}}
```

### `SortObject`

```json
{"type":"object","properties":{"empty":{"type":"boolean"},"sorted":{"type":"boolean"},"unsorted":{"type":"boolean"}}}
```

### `ApiResponseListConversationResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/ConversationResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseMessagePageResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/MessagePageResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MessagePageResponse`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/MessageResponse"}},"nextCursor":{"type":"string"},"last":{"type":"boolean"}}}
```

### `ApiResponseListTechniqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/TechniqueDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `TechniqueDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `ApiResponseListRegionDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/RegionDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `RegionDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"code":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"children":{"type":"array","items":{"$ref":"#/components/schemas/RegionDTO"}}}}
```

### `ApiResponseListMaterialFamilyDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/MaterialFamilyDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MaterialDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"familyId":{"type":"string"}}}
```

### `MaterialFamilyDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"materials":{"type":"array","items":{"$ref":"#/components/schemas/MaterialDTO"}}}}
```

### `ApiResponseListEpoqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/EpoqueDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `EpoqueDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"periodEra":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `ApiResponseListJobCategoryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/JobCategoryDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `JobCategoryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"iconUrl":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"subCategories":{"type":"array","items":{"$ref":"#/components/schemas/JobSubCategoryDTO"}}}}
```

### `JobSubCategoryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"categoryId":{"type":"string"}}}
```

### `ApiResponsePageArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PageArtisanReviewResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PageArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"totalPages":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"size":{"type":"integer","format":"int32"},"content":{"type":"array","items":{"$ref":"#/components/schemas/ArtisanReviewResponseDTO"}},"number":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"pageable":{"$ref":"#/components/schemas/PageableObject"},"numberOfElements":{"type":"integer","format":"int32"},"first":{"type":"boolean"},"last":{"type":"boolean"},"empty":{"type":"boolean"}}}
```

### `ApiResponseArtisanPublicViewDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ArtisanPublicViewDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ArtisanPublicViewDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"bio":{"type":"string"},"city":{"type":"string"},"regionId":{"type":"string"},"region":{"$ref":"#/components/schemas/RegionSummaryDTO"},"subCategoryId":{"type":"string"},"subCategory":{"$ref":"#/components/schemas/JobSubCategorySummaryDTO"},"materials":{"type":"array","items":{"$ref":"#/components/schemas/MaterialSummaryDTO"},"uniqueItems":true},"techniques":{"type":"array","items":{"$ref":"#/components/schemas/TechniqueSummaryDTO"},"uniqueItems":true},"epoques":{"type":"array","items":{"$ref":"#/components/schemas/EpoqueSummaryDTO"},"uniqueItems":true},"galleryImages":{"type":"array","items":{"$ref":"#/components/schemas/GalleryImageResponseDTO"}},"certifications":{"type":"array","items":{"$ref":"#/components/schemas/CertificationResponseDTO"}},"rating":{"type":"number","format":"double"},"reviewsCount":{"type":"integer","format":"int32"},"teacher":{"type":"boolean"},"verified":{"type":"boolean"},"avatarUrl":{"type":"string"},"createdAt":{"type":"string","format":"date-time"},"contactInfoLocked":{"type":"boolean"},"name":{"type":"string"},"phone":{"type":"string"},"email":{"type":"string"},"website":{"type":"string"},"address":{"type":"string"}}}
```

### `EpoqueSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"periodEra":{"type":"string"}}}
```

### `JobSubCategorySummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"categoryId":{"type":"string"},"categoryName":{"type":"string"}}}
```

### `MaterialSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"familyId":{"type":"string"},"familyName":{"type":"string"}}}
```

### `RegionSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"code":{"type":"string"}}}
```

### `TechniqueSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"}}}
```

### `ApiResponseListGalleryImageResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/GalleryImageResponseDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseFormationEnrollmentDetailDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFormationEnrollmentDetailDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormationEnrollmentDetailDTO`

```json
{"type":"object","properties":{"enrollment":{"$ref":"#/components/schemas/FormationEnrollmentResponseDTO"},"formation":{"$ref":"#/components/schemas/FormationSummaryDTO"}}}
```

### `FormationSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"author":{"$ref":"#/components/schemas/FormationAuthorDTO"},"title":{"type":"string"},"thumbnailUrl":{"type":"string"},"location":{"type":"string"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32"},"maxParticipants":{"type":"integer","format":"int32"},"price":{"type":"integer","format":"int32"},"currency":{"type":"string"},"status":{"type":"string","enum":["DRAFT","PENDING_REVIEW","APPROVED","REJECTED","PUBLISHED","CANCELLED","COMPLETED"]},"activeEnrollmentsCount":{"type":"integer","format":"int64"},"createdAt":{"type":"string","format":"date-time"},"online":{"type":"boolean"}}}
```

### `PaginatedResponseFormationEnrollmentDetailDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FormationEnrollmentDetailDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponsePaginatedResponseFormationSummaryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFormationSummaryDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseFormationSummaryDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FormationSummaryDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseFormationPublicViewDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormationPublicViewDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormationFileDescriptorDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"filename":{"type":"string"},"contentType":{"type":"string"},"fileSize":{"type":"integer","format":"int64"},"downloadUrl":{"type":"string"}}}
```

### `FormationPublicViewDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"author":{"$ref":"#/components/schemas/FormationAuthorDTO"},"title":{"type":"string"},"description":{"type":"string"},"thumbnailUrl":{"type":"string"},"location":{"type":"string"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32"},"maxParticipants":{"type":"integer","format":"int32"},"price":{"type":"integer","format":"int32"},"currency":{"type":"string"},"status":{"type":"string","enum":["DRAFT","PENDING_REVIEW","APPROVED","REJECTED","PUBLISHED","CANCELLED","COMPLETED"]},"activeEnrollmentsCount":{"type":"integer","format":"int64"},"availableSeats":{"type":"integer","format":"int64"},"files":{"type":"array","items":{"$ref":"#/components/schemas/FormationFileDescriptorDTO"}},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"},"enrolled":{"type":"boolean"},"online":{"type":"boolean"},"isOnline":{"type":"boolean"},"isEnrolled":{"type":"boolean"}}}
```

### `ApiResponseListCertificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/CertificationResponseDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseUserResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseUserResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseUserResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/UserResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `UserResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"email":{"type":"string"},"firstName":{"type":"string"},"lastName":{"type":"string"},"name":{"type":"string"},"phone":{"type":"string"},"avatarUrl":{"type":"string"},"status":{"type":"string","enum":["PENDING","ACTIVE","SUSPENDED","REJECTED"]},"emailVerified":{"type":"boolean"},"emailVerifiedAt":{"type":"string","format":"date-time"},"permissions":{"type":"array","items":{"type":"string"},"uniqueItems":true},"bannedUntil":{"type":"string","format":"date-time"},"banReason":{"type":"string"},"lastLoginAt":{"type":"string","format":"date-time"},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"},"teacher":{"type":"boolean"},"premium":{"type":"boolean"},"validated":{"type":"boolean"}}}
```

### `AdminWebhookLogResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"providerEventId":{"type":"string"},"eventType":{"type":"string","enum":["checkout.paid","checkout.failed","checkout.canceled","unknown"]},"signatureValid":{"type":"boolean"},"status":{"type":"string","enum":["RECEIVED","PROCESSING","PROCESSED","IGNORED","FAILED"]},"providerCheckoutId":{"type":"string"},"failureReason":{"type":"string"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponseListAdminWebhookLogResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/AdminWebhookLogResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePageContentReportResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PageContentReportResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PageContentReportResponseDTO`

```json
{"type":"object","properties":{"totalPages":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"size":{"type":"integer","format":"int32"},"content":{"type":"array","items":{"$ref":"#/components/schemas/ContentReportResponseDTO"}},"number":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"pageable":{"$ref":"#/components/schemas/PageableObject"},"numberOfElements":{"type":"integer","format":"int32"},"first":{"type":"boolean"},"last":{"type":"boolean"},"empty":{"type":"boolean"}}}
```

### `ApiResponsePaginatedResponseFormateurRequestResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFormateurRequestResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseFormateurRequestResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FormateurRequestResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `AnalyticsResult`

```json
{"type":"object","properties":{"reportType":{"type":"string"},"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"bucket":{"type":"string","enum":["DAY","WEEK","MONTH","QUARTER"]},"summary":{"type":"object","additionalProperties":{}},"series":{"$ref":"#/components/schemas/PaginatedResponseMapKeyObject"},"tables":{"type":"object","additionalProperties":{"$ref":"#/components/schemas/PaginatedResponseMapCsvObject"}}}}
```

### `ApiResponseAnalyticsResult`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AnalyticsResult"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseMapCsvObject`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"type":"object","additionalProperties":{}}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `PaginatedResponseMapKeyObject`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"type":"object","additionalProperties":{}}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

