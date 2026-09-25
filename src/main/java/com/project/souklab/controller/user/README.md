# User & Avatar Controller Package (`com.project.souklab.controller.user`)

Handles administrative user moderation (approvals, bans, timeouts) and user avatar operations (upload, list, activate, delete).

---

## Endpoints

### User Moderation (`UserManagementController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/users` | `permission:admin:users` | Paginated search and filter across all users (`?search=&page=0&size=20`). |
| `GET` | `/api/v1/admin/users/{id}` | `permission:admin:users` | Retrieves single user profile details by ID. |
| `GET` | `/api/v1/admin/users/pending` | `permission:admin:users` | Lists users awaiting administrative validation. |
| `POST` | `/api/v1/admin/users/{id}/approve` | `permission:admin:users` | Approves pending user, activates account, and sends notification. |
| `POST` | `/api/v1/admin/users/approve-bulk` | `permission:admin:users` | Bulk approves a list of pending user IDs. |
| `POST` | `/api/v1/admin/users/{id}/ban` | `permission:admin:users` | Permanently bans user, revokes refresh tokens, dispatches notification. |
| `POST` | `/api/v1/admin/users/{id}/unban` | `permission:admin:users` | Unbans user, reactivates account status to `ACTIVE`. |
| `POST` | `/api/v1/admin/users/{id}/timeout` | `permission:admin:users` | Temporarily suspends user for specified duration in minutes. |
| `GET` | `/api/v1/admin/users/audit-logs` | `permission:admin:users` | Queries platform administrative audit logs. |

> [!IMPORTANT]
> **Administrative Self-Protection Guardrails**:
> - An administrator cannot ban their own account (`POST /api/v1/admin/users/{id}/ban` where `{id}` is caller ID returns HTTP 400 `BadRequestException`).
> - An administrator cannot timeout their own account (`POST /api/v1/admin/users/{id}/timeout` returns HTTP 400 `BadRequestException`).

### Permission Management (`PermissionManagementController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/users/{userId}/permissions` | `permission:admin:users` | Lists enabled permissions assigned to a user. |
| `POST` | `/api/v1/admin/users/{userId}/permissions` | `permission:admin:users` | Assigns an enabled permission to a user. |
| `DELETE` | `/api/v1/admin/users/{userId}/permissions` | `permission:admin:users` | Revokes an assigned permission from a user. |

> [!IMPORTANT]
> **Privilege Revocation Guardrails**:
> - An administrator cannot revoke their own `permission:admin:users` permission (`DELETE /api/v1/admin/users/{userId}/permissions` targeting caller returns HTTP 400 `BadRequestException`), preventing accidental lockout of the last administrator.

### Avatar Gallery (`AvatarController`)

Manages the authenticated caller's profile avatars under `/api/v1/users/me/avatars`.

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/users/me/avatars` | Authenticated | Upload new avatar | Multipart upload (`file`). Scanned with ClamAV, resized to 3 tiers, activates immediately. |
| `GET` | `/api/v1/users/me/avatars` | Authenticated | List avatar history | Paginated list of avatars uploaded by caller (`?page=0&size=20`). |
| `GET` | `/api/v1/users/me/avatars/{id}` | Authenticated | Get avatar | Retrieves a single avatar history record owned by caller by ID. |
| `PUT` | `/api/v1/users/me/avatars/{id}/activate` | Authenticated | Activate avatar | Sets a previously uploaded gallery avatar as the active profile avatar. |
| `DELETE` | `/api/v1/users/me/avatars/{id}` | Authenticated | Delete avatar | Soft-deletes avatar record and removes S3/MinIO files. |

#### Avatar Upload Specifications
- **Content-Type**: `multipart/form-data`
- **File Key**: `file`
- **Supported Formats**: JPEG, PNG, WebP (magic bytes verified)
- **Max File Size**: Configured per environment (default 5MB)
- **Security**: ClamAV antivirus scanned before persistence
- **Resolution Tiers Generated**:
  - `thumbnailUrl`: 150×150 px (for comments, chat messages, small headers)
  - `mediumUrl`: 400×400 px (for profile cards, directory previews)
  - `fullUrl`: High-resolution processed avatar (for profile headers)

#### Response Example (`AvatarResponseDTO`)
```json
{
  "success": true,
  "code": 201,
  "message": "Avatar uploaded successfully",
  "data": {
    "id": "avt-77bdcdf3-ba7b-4872-816a-31ba4c0ce53d",
    "thumbnailUrl": "https://storage.souklab.dz/avatars/user_thumb.webp",
    "mediumUrl": "https://storage.souklab.dz/avatars/user_med.webp",
    "fullUrl": "https://storage.souklab.dz/avatars/user_full.webp",
    "active": true,
    "uploadedAt": "2026-09-23T22:30:00"
  }
}
```

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`UserManagementController`](UserManagementController.java) | Administrative approval, ban, timeout, and audit log endpoints. |
| [`PermissionManagementController`](PermissionManagementController.java) | Administrator-only assignment and revocation of enabled user capabilities. |
| [`AvatarController`](AvatarController.java) | User avatar upload, gallery retrieval, activation, and deletion. |

