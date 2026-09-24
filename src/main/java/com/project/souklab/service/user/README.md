# User & Avatar Service Package (`com.project.souklab.service.user`)

Handles administrative user discipline and full-lifecycle avatar processing.

---

## Key Capabilities

### 1. User Moderation
- **Approvals**: Activates pending accounts (`AccountStatus.ACTIVE`), flags artisans as verified, and emits `ACCOUNT_VALIDATED` notifications. Supports single and bulk approval operations.
- **Bans**: Suspends account indefinitely (100-year horizon), records administrative reason, and revokes all active refresh tokens.
- **Unbans**: Reactivates previously banned or suspended accounts back to `AccountStatus.ACTIVE`.
- **Timeouts**: Temporarily suspends account for specified duration in minutes and invalidates active tokens.
- **Self-Protection Guardrails**: Explicitly rejects attempts by an administrator to ban or timeout their own account (`BadRequestException`), preventing accidental platform lockout.

### 2. Avatar Processing Pipeline
- Resolves the current principal in the service layer, validates file headers, executes ClamAV virus scanning, generates 3 resolution tiers, uploads to MinIO/S3, and tracks gallery items in `UserAvatar`.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`UserManagementService`](UserManagementService.java) | Implements user search, pending list retrieval, single/bulk approvals, bans, unbans, timeouts, and self-lockout protection checks. |
| [`AvatarService`](AvatarService.java) | Owns authenticated-user resolution, avatar validation, configured gallery quota, primary activation, rollback, and file deletion. |
| [`CurrentUserProvider`](CurrentUserProvider.java) | Resolves the authenticated principal to a permission-loaded `User` entity for service operations. |
