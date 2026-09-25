# Authorization matrix

Authorization is capability-based. The `Permission` enum and the `permissions`/`user_permissions` tables are the source of truth; account type is onboarding metadata and is not an authority.

| Permission | Protected capability | Enforced by |
| --- | --- | --- |
| `permission:admin:users` | User moderation, permission assignment, formateur governance | `UserManagementController`, `PermissionManagementController`, `AdminFormateurController` |
| `permission:admin:formations` | Formation moderation and protected formation-file administrator override | `AdminFormationController`, `FileAccessService` |
| `permission:admin:feed` | Feed moderation, comment moderation, and administrator feed authoring | `AdminFeedController`, `FeedPostService`, `FeedEngagementService` |
| `permission:admin:reports` | Report queue and report resolution | `ContentReportController`, `ContentReportService` |
| `permission:artisan:formations` | Formation authoring, enrollment, cancellation, and course downloads | `ArtisanFormationController`, `ArtisanFormationEnrollmentController` |
| `permission:artisan:content` | Gallery, certifications, formateur requests, and verified artisan feed content | artisan controllers and `FeedPostService` |
| `permission:artisan:reviews` | Formation review create/update/delete | `ArtisanReviewController` |
| `permission:profile:read` | Authenticated `/api/v1/auth/me` profile reads | `AuthController` and `AccessControlService` |
| `permission:profile:write` | Profile completion and `/me` patch operations | `AuthController` and `AccessControlService` |
| `permission:report:create` | Authenticated content-report submission, including posts and comments | `ContentReportController`, `ContentReportService` |
| `permission:file:read` | Protected-file policy for authenticated file access | `FileAccessService` and `CustomUserDetailsService` |
| `permission:message:send` | Send direct messages and upload message attachments (clients also require an active Premium subscription) | `ConversationService`, `ChatStompController`, `ConversationController` |
| `permission:financial:admin` | Subscription management, manual grants/revocations, refunds, and payment state corrections | `AdminSubscriptionController`, `AdminPaymentController`, `AccessControlService` |
| `permission:analytics:admin` | Analytics query jobs, rollups rebuild/backfill, metrics exports, and stats access | `AnalyticsJobController`, `AccessControlService` |
| `permission:admin:catalog` | Catalog taxonomy management (techniques, epoques, regions CRUD) | `AdminCatalogController`, `AccessControlService` |
| `permission:client:favorites` | Client favorite artisan management (add, list, check status, remove) | `ClientFavoriteArtisanController`, `AccessControlService` |

Public resources are strictly scoped to `HttpMethod.GET` at the HTTP security filter layer: catalog taxonomies (`/catalog/**`), public directory search (`/public/**`), public feed reads (`/feed`, `/feed/**`), public artisan profiles (`/artisan/{id}`), public subscription plans (`/subscriptions/plans`), and public reviews (`/artisans/*/reviews`, `/api/v1/artisan/reviews/**`). All write, modify, or delete operations on these paths require authentication and appropriate permissions. File URLs require an authenticated request; `FileAccessService` allows public media with cacheable headers while certification and formation files additionally require `FILE_READ` plus ownership, enrollment, or the relevant administrator permission.

## Administrative Self-Protection Guardrails

To prevent accidental permanent administrative lockout:
- An administrator cannot ban or timeout their own account (`UserManagementService`).
- An administrator cannot revoke their own `permission:admin:users` permission (`PermissionManagementService`).

## Audit rules

- Add a new enum value, database seed value, and matrix row together.
- Use `@PreAuthorize("@accessControl...")` for endpoint capability gates.
- Keep ownership, account-status, workflow-state, and enrollment checks in the service layer.
- Do not introduce `ROLE_*`, role repositories, or string literals as authorization contracts.
