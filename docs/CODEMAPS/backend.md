<!-- Generated: 2026-09-22 | Files scanned: 472 | Token estimate: ~950 -->

# Backend Route & Execution Codemap

## 1. Request Processing Pipeline

```
HTTP Request ──► CORS Filter ──► RateLimitFilter (IP: 5/min)
             ──► JwtAuthenticationFilter (HS256 Bearer)
             ──► UserRateLimitFilter (Auth User: 120/min)
             ──► Controller (@Valid DTO) ──► AccessControlService (@PreAuthorize)
             ──► Application Service (@Transactional) ──► JPA Repository / Elasticsearch
             ──► ResponseBodyAdvice (Stamps HTTP code into ApiResponse<T>)
```

## 2. API Route Map by Domain

### Auth & Onboarding (`/api/v1/auth`)
- `POST /register`: Register artisan/client (`accountType`: `ARTISAN` | `CLIENT`)
- `POST /verify-email`: 6-digit numeric PIN verification (max 5 attempts, 15m exp)
- `POST /resend-verification`: Generic anti-enumeration response
- `POST /login`: Credential validation -> returns JWT access + refresh tokens
- `POST /refresh`: Token rotation with reuse detection
- `POST /logout`: Invalidate refresh token (transactional)
- `POST /change-password`: Update password & invalidate active sessions
- `POST /forgot-password` / `POST /reset-password`: One-time password reset flow
- `GET /me`: Permission-aware user profile
- `PATCH /me`: JSON merge patch (omitted=untouched, null=cleared, rejects credential edits)

### Profiles, Catalog & Directory (`/api/v1/`)
- `GET /catalog/{regions|categories|materials|epoques|techniques}`: Public cached taxonomy trees
- `GET /public/directory`: Elasticsearch fuzzy search + faceted filters (`category`, `wilaya`, `material`, `epoque`, `sort`)
- `GET /artisan/{id}`: Public artisan profile (phone/email masked for non-premium clients)
- `POST|GET|DELETE /artisan/gallery`: Artisan portfolio images (max 20, 10MB each)
- `POST|GET|DELETE /artisan/certifications`: Professional documents (max 10, PDF/img up to 15MB)
- `GET|PATCH /client/profile` & `POST|DELETE|GET /client/favorites/{artisanId}`: Client favorites

### Formations & Formateur (`/api/v1/`)
- `POST /artisan/formations`: Create masterclass (`isTeacher=true` required, starts in `DRAFT`)
- `PUT|DELETE /artisan/formations/{id}`: Author edit/soft-delete (draft only)
- `POST /artisan/formations/{id}/thumbnail` & `POST|DELETE /artisan/formations/{id}/files`: Course materials (max 10 files, 25MB)
- `POST /artisan/formations/{id}/submit`: Submit to admin (`PENDING_REVIEW`)
- `GET /artisan/formations/catalog[/{id}]`: Published formation discovery
- `POST /artisan/formations/{id}/enroll`: Participant enrollment (capacity checked, self-enrollment blocked -> 400)
- `POST /artisan/formations/{id}/cancel`: Cancel reservation (enforces 24h deadline cutoff)
- `GET /artisan/formations/{id}/files/{fileId}/download`: Stream course file (owner/enrollee only -> 403)
- `POST /artisan/formateur-request`: Accreditation application (14-day cooldown on rejection)
- `GET|POST /admin/formateur-requests/**`: Admin approve, reject, grant, revoke, lift-cooldown

### Social Feed, Reviews & Reports (`/api/v1/`)
- `GET|POST /feed`: Browse and publish feed posts (verified artisans only, max 10 media)
- `PUT|DELETE /feed/{id}` & `POST|DELETE /feed/{id}/media`: Author mutations
- `POST /artisan/formations/{formationId}/reviews`: **Single review path** (requires `ATTENDED` enrollment, unique constraint)
- `GET /artisans/{artisanId}/reviews`: Public paginated reviews
- `POST /reports` & `GET|POST /admin/reports[/{id}/resolve]`: Content abuse moderation

### Real-Time Messaging (`/api/v1/conversations` & `/ws`)
- `POST|GET /conversations`: 1-on-1 conversations (self-conversation blocked -> 400)
- `GET|POST /conversations/{id}/messages`: Paginated cursor history & REST fallback
- `PATCH|DELETE /conversations/{id}/messages/{messageId}`: Message edit / soft-delete
- `POST /conversations/{id}/read`: Mark read receipt
- `STOMP /app/v1/conversations/{id}/messages.send`: Send message via RabbitMQ relay
- `STOMP /app/v1/conversations/{id}/typing.{start|stop}`: Ephemeral typing broadcast
- `User Subscriptions`: `/user/queue/chat`, `/user/queue/chat-events`, `/user/queue/notifications`

### Notifications & File Storage (`/api/v1/`)
- `GET /notifications`: Paginated feed (newest first)
- `GET /notifications/unread-count`: **Raw integer in `data`** (`{ "code": 200, "data": 5, "success": true }`)
- `PUT /notifications/{id}/read` & `PUT /notifications/read-all`: Mark read
- `DELETE /notifications/{id}`: Soft-delete (cross-user access returns 404 query-scoped)
- `GET /files/{key}`: Protected object streaming (size, MIME, Tika magic bytes, ClamAV)
- `POST|GET|DELETE /users/me/avatars`: Avatar lifecycle (max 10 avatars per user)

### Subscriptions, Payments & Admin (`/api/v1/`)
- `GET /subscriptions/plans`: Public subscription plans
- `POST /subscriptions/checkout`: Initiate Chargily Pay V2 hosted checkout
- `POST /integrations/chargily/webhook`: Signature-verified, idempotent webhook processing
- `GET /admin/users` & `POST /admin/users/{id}/{approve|ban|timeout|unban}`: User administration
- `GET|POST|DELETE /admin/users/{userId}/permissions`: Granular permission assignments
- `POST /admin/analytics/jobs` & `POST /admin/analytics/rollups/*`: Async reporting jobs & CSV exports
- `POST|PUT|PATCH|DELETE /admin/catalog/**`: Full taxonomy CRUD (`permission:admin:catalog`)

## 3. Core Component Delegation

| Controller | Application Service | Primary Repositories |
|---|---|---|
| `AuthController` | `AuthService`, `RefreshTokenService` | `UserRepository`, `RefreshTokenRepository` |
| `ArtisanController` | `ArtisanService`, `ProfileService` | `ArtisanRepository`, `UserProfileRepository` |
| `DirectoryController` | `DirectorySearchService` | `ArtisanRepository` (Lucene/ES + JPA Fallback) |
| `ArtisanFormationController` | `FormationService`, `FileAccessService` | `FormationRepository`, `FormationFileRepository` |
| `ConversationController` | `ConversationService`, `ChatDeliveryService` | `ConversationRepository`, `MessageRepository` |
| `NotificationController` | `NotificationService` | `NotificationRepository` |
| `SubscriptionCheckoutController`| `SubscriptionCheckoutService`, `ChargilyService` | `SubscriptionRepository`, `PaymentRepository` |
| `AnalyticsJobController` | `AnalyticsJobService`, `AnalyticsRollupService` | `AnalyticsJobRunRepository`, `AnalyticsRollupRepository` |
| `AdminCatalogController` | `AdminCatalogService` | `TechniqueRepository`, `EpoqueRepository`, `RegionRepository`, `JobCategoryRepository`, `JobSubCategoryRepository`, `MaterialFamilyRepository`, `MaterialRepository` |

