# Authorization matrix

Authorization is capability-based. The `Permission` enum and the `permissions`/`user_permissions` tables are the source of truth; account type is onboarding metadata and is not an authority.

| Permission | Protected capability | Enforced by |
| --- | --- | --- |
| `permission:admin:users` | User moderation, permission assignment, formateur governance | `UserManagementController`, `PermissionManagementController`, `AdminFormateurController` |
| `permission:admin:formations` | Formation moderation and protected formation-file administrator override | `AdminFormationController`, `FileAccessService` |
| `permission:admin:feed` | Feed moderation and administrator feed authoring | `AdminFeedController`, `FeedPostService` |
| `permission:admin:reports` | Report queue and report resolution | `ContentReportController`, `ContentReportService` |
| `permission:artisan:formations` | Formation authoring, enrollment, cancellation, and course downloads | `ArtisanFormationController`, `ArtisanFormationEnrollmentController` |
| `permission:artisan:content` | Gallery, certifications, formateur requests, and verified artisan feed content | artisan controllers and `FeedPostService` |
| `permission:artisan:reviews` | Formation review create/update/delete | `ArtisanReviewController` |
| `permission:profile:read` | Authenticated `/api/v1/auth/me` profile reads | `AuthController` and `AccessControlService` |
| `permission:profile:write` | Profile completion and `/me` patch operations | `AuthController` and `AccessControlService` |
| `permission:report:create` | Authenticated content-report submission | `ContentReportController` |
| `permission:file:read` | Protected-file policy for authenticated file access | `FileAccessService` and `CustomUserDetailsService` |

Public resources remain public at the HTTP layer: authentication bootstrap, catalog taxonomies, directory search, public feed reads, public artisan profiles, and public reviews. File URLs still require an authenticated request; `FileAccessService` allows public media with cacheable headers while certification and formation files additionally require `FILE_READ` plus ownership, enrollment, or the relevant administrator permission.

## Audit rules

- Add a new enum value, database seed value, and matrix row together.
- Use `@PreAuthorize("@accessControl...")` for endpoint capability gates.
- Keep ownership, account-status, workflow-state, and enrollment checks in the service layer.
- Do not introduce `ROLE_*`, role repositories, or string literals as authorization contracts.
