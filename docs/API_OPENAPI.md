# Souklab OpenAPI contract

Generated from the running application on 2026-09-25T23:06:40Z. This Markdown view is a human-readable companion to the machine-readable `/v3/api-docs` document.

- OpenAPI version: `3.1.0`
- API title: `Souklab API`
- API version: `1.0.0`
- Paths: `167`
- Schemas: `197`

## Security

### `bearerAuth`

```json
{"type":"http","description":"JWT access token issued by the authentication API.","scheme":"bearer","bearerFormat":"JWT"}
```


## Endpoints

### `/api/v1/users/me/avatars/{id}/activate`

#### PUT — Activate avatar

- Operation ID: `activateAvatar`
- Tags: `User Avatar`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/{id}/read`

#### PUT — Mark notification as read

- Operation ID: `markAsRead`
- Tags: `Notifications`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/read-all`

#### PUT — Mark all notifications as read

- Operation ID: `markAllAsRead`
- Tags: `Notifications`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### GET — Get published feed post

- Operation ID: `get`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### PUT — Update feed post

- Operation ID: `update`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### DELETE — Delete feed post

- Operation ID: `remove`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/comments/{commentId}`

#### GET — Get feed comment

- Operation ID: `getComment`
- Tags: `Community Feed`
- Parameters:
  - `commentId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/comments/{commentId}`

#### PUT — Update feed comment

- Operation ID: `updateComment`
- Tags: `Community Feed`
- Parameters:
  - `commentId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/comments/{commentId}`

#### DELETE — deleteComment

- Operation ID: `deleteComment`
- Tags: `Community Feed`
- Parameters:
  - `commentId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/reviews/{reviewId}`

#### GET — Get published artisan review

- Operation ID: `get_1`
- Tags: `Artisan Reviews`
- Parameters:
  - `reviewId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/reviews/{reviewId}`

#### PUT — Update review

- Operation ID: `update_1`
- Tags: `Artisan Reviews`
- Parameters:
  - `reviewId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/reviews/{reviewId}`

#### DELETE — Delete review

- Operation ID: `delete`
- Tags: `Artisan Reviews`
- Parameters:
  - `reviewId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery/{id}`

#### GET — Get single gallery image

- Operation ID: `getImage`
- Tags: `Artisan Showcase Gallery`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery/{id}`

#### PUT — Update portfolio image

- Operation ID: `updateImage`
- Tags: `Artisan Showcase Gallery`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery/{id}`

#### DELETE — Delete gallery image

- Operation ID: `deleteImage`
- Tags: `Artisan Showcase Gallery`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery/order`

#### PUT — Reorder gallery images

- Operation ID: `reorderGallery`
- Tags: `Artisan Showcase Gallery`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### GET — Get authored masterclass details

- Operation ID: `getFormationDetails`
- Tags: `Masterclass Authoring`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### PUT — Update masterclass

- Operation ID: `updateFormation`
- Tags: `Masterclass Authoring`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### DELETE — Delete masterclass

- Operation ID: `deleteFormation`
- Tags: `Masterclass Authoring`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications/{id}`

#### GET — Get single certification

- Operation ID: `getCertification`
- Tags: `Artisan Certifications`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications/{id}`

#### PUT — Update certification

- Operation ID: `updateCertification`
- Tags: `Artisan Certifications`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications/{id}`

#### DELETE — Delete certification

- Operation ID: `deleteCertification`
- Tags: `Artisan Certifications`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans/{id}`

#### GET — getById

- Operation ID: `getById`
- Tags: `admin-subscription-plan-controller`
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

### `/api/v1/admin/catalog/techniques/{id}`

#### PUT — updateTechnique

- Operation ID: `updateTechnique`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/techniques/{id}`

#### DELETE — deleteTechnique

- Operation ID: `deleteTechnique`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/techniques/{id}`

#### PATCH — patchTechnique

- Operation ID: `patchTechnique`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories/{id}`

#### PUT — updateSubCategory

- Operation ID: `updateSubCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories/{id}`

#### DELETE — deleteSubCategory

- Operation ID: `deleteSubCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories/{id}`

#### PATCH — patchSubCategory

- Operation ID: `patchSubCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/regions/{id}`

#### PUT — updateRegion

- Operation ID: `updateRegion`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/regions/{id}`

#### DELETE — deleteRegion

- Operation ID: `deleteRegion`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/regions/{id}`

#### PATCH — patchRegion

- Operation ID: `patchRegion`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials/{id}`

#### PUT — updateMaterial

- Operation ID: `updateMaterial`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials/{id}`

#### DELETE — deleteMaterial

- Operation ID: `deleteMaterial`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials/{id}`

#### PATCH — patchMaterial

- Operation ID: `patchMaterial`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families/{id}`

#### PUT — updateMaterialFamily

- Operation ID: `updateMaterialFamily`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families/{id}`

#### DELETE — deleteMaterialFamily

- Operation ID: `deleteMaterialFamily`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families/{id}`

#### PATCH — patchMaterialFamily

- Operation ID: `patchMaterialFamily`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/epoques/{id}`

#### PUT — updateEpoque

- Operation ID: `updateEpoque`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/epoques/{id}`

#### DELETE — deleteEpoque

- Operation ID: `deleteEpoque`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/epoques/{id}`

#### PATCH — patchEpoque

- Operation ID: `patchEpoque`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories/{id}`

#### PUT — updateCategory

- Operation ID: `updateCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories/{id}`

#### DELETE — deleteCategory

- Operation ID: `deleteCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories/{id}`

#### PATCH — patchCategory

- Operation ID: `patchCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars`

#### GET — Get avatar gallery history

- Operation ID: `listAvatars`
- Tags: `User Avatar`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars`

#### POST — Upload new avatar (/api/v1/users/me/avatars)

- Operation ID: `uploadAvatar`
- Tags: `User Avatar`
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/{id}/renew`

#### POST — Renew subscription

- Operation ID: `renew`
- Tags: `Subscription Checkout`
- Parameters:
  - `id` (`path`, required)
  - `Idempotency-Key` (`header`, optional)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/{id}/cancel`

#### POST — Cancel subscription

- Operation ID: `cancel`
- Tags: `Subscription Account`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/checkout`

#### POST — Checkout subscription

- Operation ID: `checkout`
- Tags: `Subscription Checkout`
- Parameters:
  - `Idempotency-Key` (`header`, optional)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/reports`

#### POST — Submit content report

- Operation ID: `create`
- Tags: `Content Moderation & Reports`
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

#### GET — List public feed posts

- Operation ID: `list`
- Tags: `Community Feed`
- Parameters:
  - `type` (`query`, optional)
  - `authorId` (`query`, optional)
  - `tag` (`query`, optional)
  - `q` (`query`, optional)
  - `sort` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed`

#### POST — Submit feed post

- Operation ID: `create_1`
- Tags: `Community Feed`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/submit`

#### POST — submit

- Operation ID: `submit`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/share`

#### POST — share

- Operation ID: `share`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/media`

#### POST — Upload post media attachment

- Operation ID: `addMedia`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/likes`

#### GET — likeStatus

- Operation ID: `likeStatus`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/likes`

#### POST — like

- Operation ID: `like`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/likes`

#### DELETE — unlike

- Operation ID: `unlike`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/comments`

#### GET — comments

- Operation ID: `comments`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/comments`

#### POST — comment

- Operation ID: `comment`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/bookmarks`

#### POST — bookmark

- Operation ID: `bookmark`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/bookmarks`

#### DELETE — removeBookmark

- Operation ID: `removeBookmark`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/comments/{commentId}/replies`

#### GET — replies

- Operation ID: `replies`
- Tags: `Community Feed`
- Parameters:
  - `commentId` (`path`, required)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/comments/{commentId}/replies`

#### POST — reply

- Operation ID: `reply`
- Tags: `Community Feed`
- Parameters:
  - `commentId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/comments/{commentId}/likes`

#### GET — commentLikeStatus

- Operation ID: `commentLikeStatus`
- Tags: `Community Feed`
- Parameters:
  - `commentId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/comments/{commentId}/likes`

#### POST — likeComment

- Operation ID: `likeComment`
- Tags: `Community Feed`
- Parameters:
  - `commentId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/comments/{commentId}/likes`

#### DELETE — unlikeComment

- Operation ID: `unlikeComment`
- Tags: `Community Feed`
- Parameters:
  - `commentId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/conversations`

#### GET — List conversations

- Operation ID: `list_1`
- Tags: `Messaging & Chat`
- Parameters:
  - `archived` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/conversations`

#### POST — Create or get conversation

- Operation ID: `create_2`
- Tags: `Messaging & Chat`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/read`

#### POST — Mark conversation read

- Operation ID: `read`
- Tags: `Messaging & Chat`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/messages`

#### GET — Get conversation messages

- Operation ID: `messages`
- Tags: `Messaging & Chat`
- Parameters:
  - `id` (`path`, required)
  - `cursor` (`query`, optional)
  - `size` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/messages`

#### POST — Send message

- Operation ID: `send`
- Tags: `Messaging & Chat`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/attachments`

#### POST — Upload chat attachment

- Operation ID: `uploadAttachment`
- Tags: `Messaging & Chat`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/client/favorites/artisans/{artisanId}`

#### POST — Add favorite artisan

- Operation ID: `addFavoriteArtisan`
- Tags: `Client Favorites`
- Parameters:
  - `artisanId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/client/favorites/artisans/{artisanId}`

#### DELETE — Remove favorite artisan

- Operation ID: `removeFavoriteArtisan`
- Tags: `Client Favorites`
- Parameters:
  - `artisanId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/auth/verify-email`

#### POST — Verify email

- Operation ID: `verifyEmail`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/reset-password`

#### POST — Reset password

- Operation ID: `resetPassword`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/resend-verification`

#### POST — Resend verification email

- Operation ID: `resendVerification`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/register`

#### POST — Register user

- Operation ID: `register`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/refresh`

#### POST — Rotate refresh token

- Operation ID: `refreshToken`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/logout`

#### POST — Logout user

- Operation ID: `logout`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/login`

#### POST — Login with credentials

- Operation ID: `login`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/forgot-password`

#### POST — Forgot password

- Operation ID: `forgotPassword`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/complete-profile`

#### POST — Complete user profile

- Operation ID: `completeProfile`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/change-password`

#### POST — Change password

- Operation ID: `changePassword`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery`

#### GET — Get artisan gallery

- Operation ID: `getMyGallery`
- Tags: `Artisan Showcase Gallery`
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery`

#### POST — Upload portfolio image

- Operation ID: `uploadImage`
- Tags: `Artisan Showcase Gallery`
- Parameters:
  - `title` (`query`, optional)
  - `caption` (`query`, optional)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations`

#### POST — Create masterclass draft

- Operation ID: `createFormation`
- Tags: `Masterclass Authoring`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/thumbnail`

#### POST — Upload formation thumbnail

- Operation ID: `uploadThumbnail`
- Tags: `Masterclass Authoring`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/submit`

#### POST — Submit masterclass for review

- Operation ID: `submitForReview`
- Tags: `Masterclass Authoring`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files`

#### POST — Upload course attachment

- Operation ID: `uploadCourseFile`
- Tags: `Masterclass Authoring`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/enroll`

#### POST — Enroll in masterclass

- Operation ID: `enroll`
- Tags: `Masterclass Catalog & Enrollment`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/cancel`

#### POST — Cancel masterclass enrollment

- Operation ID: `cancel_1`
- Tags: `Masterclass Catalog & Enrollment`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{formationId}/reviews`

#### POST — Submit workshop review

- Operation ID: `create_3`
- Tags: `Artisan Reviews`
- Parameters:
  - `formationId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formateur-request`

#### GET — Get latest formateur request

- Operation ID: `getLatestRequest`
- Tags: `Instructor Accreditation`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formateur-request`

#### POST — Submit formateur accreditation request

- Operation ID: `submitRequest`
- Tags: `Instructor Accreditation`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications`

#### GET — Get artisan certifications

- Operation ID: `getMyCertifications`
- Tags: `Artisan Certifications`
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications`

#### POST — Upload certification document

- Operation ID: `uploadCertification`
- Tags: `Artisan Certifications`
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

#### POST — Resolve report

- Operation ID: `resolve`
- Tags: `Content Moderation & Reports`
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
- Tags: `Community Feed Administration`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/reject`

#### POST — reject

- Operation ID: `reject`
- Tags: `Community Feed Administration`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/publish`

#### POST — publish

- Operation ID: `publish`
- Tags: `Community Feed Administration`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/hide`

#### POST — hide

- Operation ID: `hide`
- Tags: `Community Feed Administration`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/techniques`

#### POST — createTechnique

- Operation ID: `createTechnique`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories`

#### POST — createSubCategory

- Operation ID: `createSubCategory`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/regions`

#### POST — createRegion

- Operation ID: `createRegion`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials`

#### POST — createMaterial

- Operation ID: `createMaterial`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families`

#### POST — createMaterialFamily

- Operation ID: `createMaterialFamily`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/epoques`

#### POST — createEpoque

- Operation ID: `createEpoque`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories`

#### POST — createCategory

- Operation ID: `createCategory`
- Tags: `admin-catalog-controller`
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

- Operation ID: `submit_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs`

#### POST — Submit an analytics job

- Operation ID: `submit_2`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/archive`

#### PATCH — Archive or unarchive conversation

- Operation ID: `archive`
- Tags: `Messaging & Chat`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{conversationId}/messages/{messageId}`

#### DELETE — Delete message

- Operation ID: `delete_1`
- Tags: `Messaging & Chat`
- Parameters:
  - `conversationId` (`path`, required)
  - `messageId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/conversations/{conversationId}/messages/{messageId}`

#### PATCH — Edit message

- Operation ID: `edit`
- Tags: `Messaging & Chat`
- Parameters:
  - `conversationId` (`path`, required)
  - `messageId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/me`

#### GET — Get current user profile (/me)

- Operation ID: `getCurrentUser`
- Tags: `Authentication & Profile`
- Responses:
  - `200` — OK

### `/api/v1/auth/me`

#### PATCH — Update current user profile (/me)

- Operation ID: `patchCurrentUser`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories/{id}/status`

#### PATCH — patchSubCategory_1

- Operation ID: `patchSubCategory_1`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials/{id}/status`

#### PATCH — patchMaterial_1

- Operation ID: `patchMaterial_1`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families/{id}/status`

#### PATCH — patchMaterialFamily_1

- Operation ID: `patchMaterialFamily_1`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories/{id}/status`

#### PATCH — patchCategory_1

- Operation ID: `patchCategory_1`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars/{id}`

#### GET — Get single avatar

- Operation ID: `getAvatar`
- Tags: `User Avatar`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars/{id}`

#### DELETE — Delete avatar

- Operation ID: `deleteAvatar`
- Tags: `User Avatar`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/subscriptions`

#### GET — Get subscription history

- Operation ID: `history`
- Tags: `Subscription Account`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/plans`

#### GET — Get subscription plans

- Operation ID: `listPlans`
- Tags: `Subscription Plans`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/plans/{id}`

#### GET — Get subscription plan by ID

- Operation ID: `getPlan`
- Tags: `Subscription Plans`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/current`

#### GET — Get current subscription

- Operation ID: `current`
- Tags: `Subscription Account`
- Responses:
  - `200` — OK

### `/api/v1/public/directory`

#### GET — Search artisan directory

- Operation ID: `search`
- Tags: `Artisan Directory`
- Parameters:
  - `filter` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/payments`

#### GET — Get payment history

- Operation ID: `payments`
- Tags: `Subscription Account`
- Responses:
  - `200` — OK

### `/api/v1/payments/{id}`

#### GET — Get payment by ID

- Operation ID: `payment`
- Tags: `Subscription Account`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications`

#### GET — Get user notifications

- Operation ID: `getNotifications`
- Tags: `Notifications`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/{id}`

#### GET — Get notification by ID

- Operation ID: `getNotification`
- Tags: `Notifications`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/{id}`

#### DELETE — Delete notification

- Operation ID: `deleteNotification`
- Tags: `Notifications`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/unread-count`

#### GET — Get unread notifications count

- Operation ID: `getUnreadCount`
- Tags: `Notifications`
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

### `/api/v1/feed/saved`

#### GET — saved

- Operation ID: `saved`
- Tags: `Community Feed`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/me`

#### GET — mine

- Operation ID: `mine`
- Tags: `Community Feed`
- Parameters:
  - `status` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/following`

#### GET — following

- Operation ID: `following`
- Tags: `Community Feed`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}`

#### GET — Get conversation

- Operation ID: `get_2`
- Tags: `Messaging & Chat`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/client/favorites/artisans`

#### GET — List favorite artisans

- Operation ID: `listFavoriteArtisans`
- Tags: `Client Favorites`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/client/favorites/artisans/{artisanId}/status`

#### GET — Check favorite artisan status

- Operation ID: `getFavoriteArtisanStatus`
- Tags: `Client Favorites`
- Parameters:
  - `artisanId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/catalog/techniques`

#### GET — Get techniques taxonomy

- Operation ID: `getTechniques`
- Tags: `Catalog Taxonomy`
- Responses:
  - `200` — OK

### `/api/v1/catalog/regions`

#### GET — Get regions taxonomy

- Operation ID: `getRegions`
- Tags: `Catalog Taxonomy`
- Responses:
  - `200` — OK

### `/api/v1/catalog/materials`

#### GET — Get materials taxonomy

- Operation ID: `getMaterials`
- Tags: `Catalog Taxonomy`
- Responses:
  - `200` — OK

### `/api/v1/catalog/epoques`

#### GET — Get epochs taxonomy

- Operation ID: `getEpoques`
- Tags: `Catalog Taxonomy`
- Responses:
  - `200` — OK

### `/api/v1/catalog/categories`

#### GET — Get craft categories

- Operation ID: `getCategories`
- Tags: `Catalog Taxonomy`
- Responses:
  - `200` — OK

### `/api/v1/auth/oauth/google/client`

#### GET — Google OAuth2 (Client)

- Operation ID: `initiateGoogleOAuthClient`
- Tags: `Authentication & Profile`
- Responses:
  - `200` — OK

### `/api/v1/auth/oauth/google/artisan`

#### GET — Google OAuth2 (Artisan)

- Operation ID: `initiateGoogleOAuthArtisan`
- Tags: `Authentication & Profile`
- Responses:
  - `200` — OK

### `/api/v1/artisans/{artisanId}/reviews`

#### GET — List artisan reviews

- Operation ID: `list_4`
- Tags: `Artisan Reviews`
- Parameters:
  - `artisanId` (`path`, required)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/{id}`

#### GET — Get artisan public profile

- Operation ID: `getArtisanProfile`
- Tags: `Artisan Profile`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files/{fileId}/download`

#### GET — Download course document

- Operation ID: `downloadCourseFile`
- Tags: `Masterclass Catalog & Enrollment`
- Parameters:
  - `id` (`path`, required)
  - `fileId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/my-enrollments`

#### GET — List my masterclass enrollments

- Operation ID: `getMyEnrollments`
- Tags: `Masterclass Catalog & Enrollment`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/me`

#### GET — List authored masterclasses

- Operation ID: `getMyFormations`
- Tags: `Masterclass Authoring`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/catalog`

#### GET — Browse masterclass catalog

- Operation ID: `getPublishedCatalog`
- Tags: `Masterclass Catalog & Enrollment`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/catalog/{id}`

#### GET — Get published masterclass details

- Operation ID: `getPublishedFormationDetails`
- Tags: `Masterclass Catalog & Enrollment`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formateur-requests`

#### GET — Get formateur request history

- Operation ID: `getRequestHistory`
- Tags: `Instructor Accreditation`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formateur-requests/{id}`

#### GET — Get single formateur request

- Operation ID: `getRequestById`
- Tags: `Instructor Accreditation`
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

### `/api/v1/admin/users/{id}`

#### GET — getUserById

- Operation ID: `getUserById`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
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

### `/api/v1/admin/users/audit-logs`

#### GET — getAuditLogs

- Operation ID: `getAuditLogs`
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

### `/api/v1/admin/subscriptions/{id}`

#### GET — getSubscriptionById

- Operation ID: `getSubscriptionById`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
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

#### GET — List reports for moderation

- Operation ID: `list_5`
- Tags: `Content Moderation & Reports`
- Parameters:
  - `status` (`query`, optional)
  - `targetType` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/reports/{id}`

#### GET — Get report details

- Operation ID: `get_3`
- Tags: `Content Moderation & Reports`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/formations/{id}`

#### GET — getFormationById

- Operation ID: `getFormationById`
- Tags: `admin-formation-controller`
- Parameters:
  - `id` (`path`, required)
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

### `/api/v1/admin/formateur-requests/{id}`

#### GET — getRequestById_1

- Operation ID: `getRequestById_1`
- Tags: `admin-formateur-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}`

#### GET — Get feed post for administration

- Operation ID: `getById_1`
- Tags: `Community Feed Administration`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}`

#### DELETE — delete_2

- Operation ID: `delete_2`
- Tags: `Community Feed Administration`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/pending`

#### GET — listPending

- Operation ID: `listPending`
- Tags: `Community Feed Administration`
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

- Operation ID: `delete_3`
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

- Operation ID: `delete_4`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/media/{mediaId}`

#### DELETE — Delete post media attachment

- Operation ID: `removeMedia`
- Tags: `Community Feed`
- Parameters:
  - `id` (`path`, required)
  - `mediaId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files/{fileId}`

#### DELETE — Delete course attachment

- Operation ID: `deleteCourseFile`
- Tags: `Masterclass Authoring`
- Parameters:
  - `id` (`path`, required)
  - `fileId` (`path`, required)
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
{"type":"object","properties":{"type":{"type":"string","enum":["ACTUALITE","FORMATION","ANNONCE"]},"title":{"type":"string","minLength":1},"body":{"type":"string","minLength":1},"formationId":{"type":"string","maxLength":36,"minLength":0},"tags":{"type":"array","items":{"type":"string"}},"draft":{"type":"boolean"},"isDraft":{"type":"boolean"}},"required":["body","title","type"]}
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
{"type":"object","properties":{"id":{"type":"string"},"authorId":{"type":"string"},"authorName":{"type":"string"},"type":{"type":"string","enum":["ACTUALITE","FORMATION","ANNONCE"]},"title":{"type":"string"},"body":{"type":"string"},"status":{"type":"string","enum":["DRAFT","PENDING","PUBLISHED","HIDDEN","REJECTED","REMOVED"]},"formationId":{"type":"string"},"publishedAt":{"type":"string","format":"date-time"},"moderationNote":{"type":"string"},"media":{"type":"array","items":{"$ref":"#/components/schemas/FeedPostMediaResponseDTO"}},"tags":{"type":"array","items":{"type":"string"}},"likeCount":{"type":"integer","format":"int32"},"commentCount":{"type":"integer","format":"int32"},"bookmarkCount":{"type":"integer","format":"int32"},"shareCount":{"type":"integer","format":"int32"},"likedByCurrentUser":{"type":"boolean"},"bookmarkedByCurrentUser":{"type":"boolean"}}}
```

### `FeedPostCommentCreateDTO`

```json
{"type":"object","properties":{"content":{"type":"string","minLength":1}},"required":["content"]}
```

### `ApiResponseFeedPostCommentResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FeedPostCommentResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FeedPostCommentResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"postId":{"type":"string"},"parentId":{"type":"string"},"authorId":{"type":"string"},"authorName":{"type":"string"},"avatarUrl":{"type":"string"},"content":{"type":"string"},"likeCount":{"type":"integer","format":"int32"},"replyCount":{"type":"integer","format":"int32"},"likedByCurrentUser":{"type":"boolean"},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"}}}
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

### `GalleryImageUpdateDTO`

```json
{"type":"object","properties":{"title":{"type":"string"},"caption":{"type":"string"}}}
```

### `ApiResponseGalleryImageResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/GalleryImageResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `GalleryImageResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"imageUrl":{"type":"string"},"title":{"type":"string"},"caption":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
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

### `CertificationUpdateDTO`

```json
{"type":"object","properties":{"title":{"type":"string"},"issuer":{"type":"string"},"issuedAt":{"type":"string","format":"date"},"expiresAt":{"type":"string","format":"date"}}}
```

### `ApiResponseCertificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/CertificationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `CertificationResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"title":{"type":"string"},"issuer":{"type":"string"},"issuedAt":{"type":"string","format":"date"},"expiresAt":{"type":"string","format":"date"},"documentUrl":{"type":"string"},"verified":{"type":"boolean"}}}
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

### `TechniqueRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseTechniqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/TechniqueDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `TechniqueDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `JobSubCategoryRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"categoryId":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"active":{"type":"boolean","writeOnly":true},"status":{"type":"boolean","writeOnly":true},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseJobSubCategoryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/JobSubCategoryDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `JobSubCategoryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"categoryId":{"type":"string"}}}
```

### `RegionRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"parentId":{"type":"string"},"code":{"type":"string","maxLength":10,"minLength":0},"displayOrder":{"type":"integer","format":"int32"},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseRegionDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/RegionDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `RegionDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"code":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"children":{"type":"array","items":{"$ref":"#/components/schemas/RegionDTO"}}}}
```

### `MaterialRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"familyId":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"active":{"type":"boolean","writeOnly":true},"status":{"type":"boolean","writeOnly":true},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseMaterialDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/MaterialDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MaterialDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"familyId":{"type":"string"}}}
```

### `MaterialFamilyRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"active":{"type":"boolean","writeOnly":true},"status":{"type":"boolean","writeOnly":true},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseMaterialFamilyDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/MaterialFamilyDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MaterialFamilyDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"materials":{"type":"array","items":{"$ref":"#/components/schemas/MaterialDTO"}}}}
```

### `EpoqueRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"periodEra":{"type":"string","maxLength":100,"minLength":0},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseEpoqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/EpoqueDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `EpoqueDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"periodEra":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `JobCategoryRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"iconUrl":{"type":"string","maxLength":500,"minLength":0},"displayOrder":{"type":"integer","format":"int32"},"active":{"type":"boolean","writeOnly":true},"status":{"type":"boolean","writeOnly":true},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseJobCategoryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/JobCategoryDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `JobCategoryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"iconUrl":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"subCategories":{"type":"array","items":{"$ref":"#/components/schemas/JobSubCategoryDTO"}}}}
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
{"type":"object","properties":{"targetType":{"type":"string","enum":["USER","POST","COMMENT","REVIEW"]},"targetId":{"type":"string","maxLength":36,"minLength":0},"reason":{"type":"string","maxLength":100,"minLength":0},"details":{"type":"string","maxLength":5000,"minLength":0}},"required":["reason","targetId","targetType"]}
```

### `ApiResponseContentReportResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ContentReportResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ContentReportResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"reporterId":{"type":"string"},"targetType":{"type":"string","enum":["USER","POST","COMMENT","REVIEW"]},"targetId":{"type":"string"},"reason":{"type":"string"},"details":{"type":"string"},"status":{"type":"string","enum":["OPEN","DISMISSED","RESOLVED"]},"resolutionAction":{"type":"string","enum":["DISMISS","HIDE","REMOVE"]},"resolverId":{"type":"string"},"resolutionNote":{"type":"string"},"resolvedAt":{"type":"string","format":"date-time"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponseFeedShareResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FeedShareResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FeedShareResponseDTO`

```json
{"type":"object","properties":{"url":{"type":"string"},"title":{"type":"string"},"description":{"type":"string"}}}
```

### `ApiResponseFeedPostMediaResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FeedPostMediaResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseFeedPostLikeStatusDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FeedPostLikeStatusDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FeedPostLikeStatusDTO`

```json
{"type":"object","properties":{"likeCount":{"type":"integer","format":"int32"},"likedByCurrentUser":{"type":"boolean"}}}
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

### `ApiResponseClientFavoriteArtisanResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ClientFavoriteArtisanResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ClientFavoriteArtisanResponseDTO`

```json
{"type":"object","properties":{"favoriteId":{"type":"string"},"artisanId":{"type":"string"},"favoritedAt":{"type":"string","format":"date-time"}}}
```

### `VerifyEmailRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"code":{"type":"string","minLength":1,"pattern":"\\d{6}"}},"required":["code","email"]}
```

### `ResetPasswordRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"code":{"type":"string","minLength":1,"pattern":"\\d{6}"},"newPassword":{"type":"string","minLength":1}},"required":["code","email","newPassword"]}
```

### `ResendVerificationRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1}},"required":["email"]}
```

### `UserRegistrationDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"password":{"type":"string","minLength":1},"name":{"type":"string"},"firstName":{"type":"string"},"lastName":{"type":"string"},"accountType":{"type":"string","enum":["ADMIN","ARTISAN","CLIENT"]}},"required":["accountType","email","password"]}
```

### `ApiResponseProfileResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ProfileResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ProfileResponse`

```json
{"type":"object","properties":{"createdAt":{"type":"string","format":"date-time"},"accountStatus":{"type":"string","enum":["PENDING","ACTIVE","SUSPENDED","REJECTED"]},"emailVerifiedAt":{"type":"string","format":"date-time"},"emailVerified":{"type":"boolean"},"phone":{"type":"string"},"avatarUrl":{"type":"string"},"firstName":{"type":"string"},"lastName":{"type":"string"},"updatedAt":{"type":"string","format":"date-time"},"email":{"type":"string"},"name":{"type":"string"},"permissions":{"type":"array","items":{"type":"string"},"uniqueItems":true},"id":{"type":"string"}}}
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
{"type":"object","properties":{"email":{"type":"string"},"username":{"type":"string"},"password":{"type":"string","maxLength":128,"minLength":0},"loginIdentifier":{"type":"string"}},"required":["password"]}
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
{"type":"object","properties":{"oldPassword":{"type":"string","maxLength":128,"minLength":0},"newPassword":{"type":"string","minLength":1}},"required":["newPassword","oldPassword"]}
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

### `ApiResponseObject`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseFeedPostCommentResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFeedPostCommentResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseFeedPostCommentResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FeedPostCommentResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponsePaginatedResponseFeedPostResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFeedPostResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseFeedPostResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FeedPostResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
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

### `ApiResponsePaginatedResponseClientFavoriteArtisanItemDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseClientFavoriteArtisanItemDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ClientFavoriteArtisanItemDTO`

```json
{"type":"object","properties":{"favoritedAt":{"type":"string","format":"date-time"},"artisan":{"$ref":"#/components/schemas/ArtisanDirectoryCardDTO"}}}
```

### `PaginatedResponseClientFavoriteArtisanItemDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/ClientFavoriteArtisanItemDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseFavoriteStatusResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FavoriteStatusResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FavoriteStatusResponseDTO`

```json
{"type":"object","properties":{"favorited":{"type":"boolean"}}}
```

### `ApiResponseListTechniqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/TechniqueDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListRegionDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/RegionDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListMaterialFamilyDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/MaterialFamilyDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListEpoqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/EpoqueDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListJobCategoryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/JobCategoryDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePageArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PageArtisanReviewResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PageArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"totalPages":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"size":{"type":"integer","format":"int32"},"content":{"type":"array","items":{"$ref":"#/components/schemas/ArtisanReviewResponseDTO"}},"number":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"pageable":{"$ref":"#/components/schemas/PageableObject"},"numberOfElements":{"type":"integer","format":"int32"},"first":{"type":"boolean"},"last":{"type":"boolean"},"empty":{"type":"boolean"}}}
```

### `PageableObject`

```json
{"type":"object","properties":{"offset":{"type":"integer","format":"int64"},"paged":{"type":"boolean"},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"unpaged":{"type":"boolean"}}}
```

### `SortObject`

```json
{"type":"object","properties":{"empty":{"type":"boolean"},"sorted":{"type":"boolean"},"unsorted":{"type":"boolean"}}}
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

### `ApiResponsePaginatedResponseFormateurRequestResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFormateurRequestResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseFormateurRequestResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FormateurRequestResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
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
{"type":"object","properties":{"id":{"type":"string"},"email":{"type":"string"},"firstName":{"type":"string"},"lastName":{"type":"string"},"name":{"type":"string"},"phone":{"type":"string"},"avatarUrl":{"type":"string"},"status":{"type":"string","enum":["PENDING","ACTIVE","SUSPENDED","REJECTED"]},"emailVerified":{"type":"boolean"},"emailVerifiedAt":{"type":"string","format":"date-time"},"permissions":{"type":"array","items":{"type":"string"},"uniqueItems":true},"bannedUntil":{"type":"string","format":"date-time"},"banReason":{"type":"string"},"lastLoginAt":{"type":"string","format":"date-time"},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"},"validated":{"type":"boolean"},"premium":{"type":"boolean"},"teacher":{"type":"boolean"}}}
```

### `ApiResponseUserResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/UserResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseAuditLogDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseAuditLogDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `AuditLogDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"action":{"type":"string"},"details":{"type":"string"},"userEmail":{"type":"string"},"userId":{"type":"string"},"targetAccountId":{"type":"string"},"operation":{"type":"string"},"previousState":{"type":"string"},"newState":{"type":"string"},"reason":{"type":"string"},"paymentId":{"type":"string"},"subscriptionId":{"type":"string"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `PaginatedResponseAuditLogDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/AuditLogDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
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

