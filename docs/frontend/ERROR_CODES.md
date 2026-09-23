# SoukLab Frontend Error Handling & Code Reference

This document maps all backend `ApiErrorCode` constants to their HTTP status codes, common causes, and recommended frontend user experience (UX) handling.

---

## Standard Error Response Structure

All API errors return a standard JSON envelope with `success: false` and a typed `errorCode`:

```json
{
  "success": false,
  "code": 403,
  "errorCode": "FORBIDDEN",
  "message": "Please verify your email address to access artisan profiles.",
  "data": null,
  "timestamp": "2026-09-23T20:15:00.000Z"
}
```

### Validation Error Structure (`422 Unprocessable Content` / `400 Bad Request`)

When request field validation fails, field-level errors are included in the `errors` map:

```json
{
  "success": false,
  "code": 422,
  "errorCode": "BAD_REQUEST",
  "message": "Validation failed",
  "data": null,
  "errors": {
    "email": "Email is not valid",
    "password": "Password must be at least 8 characters with 1 number and 1 special char"
  }
}
```

---

## Error Codes Catalog & Recommended Frontend Actions

| `errorCode` | HTTP Status | Typical Scenario | Recommended Frontend Action |
| :--- | :---: | :--- | :--- |
| `UNAUTHORIZED` | `401` | Missing, malformed, or expired JWT access token. | Attempt silent refresh via `/api/v1/auth/refresh`. If refresh fails, clear storage and redirect to `/login`. |
| `AUTHENTICATION_FAILED` | `401` | Bad email/password on login, or invalid refresh token. | Display inline alert on login form: *"Invalid email or password"*. |
| `FORBIDDEN` | `403` | Accessing an endpoint without the required role or unverified email. | If message indicates unverified email, open OTP verification modal. Otherwise show "Access Denied" view. |
| `RESOURCE_NOT_FOUND` | `404` | Artisan, post, conversation, or formation ID not found. | Render a 404 Empty State / Not Found component with a back button. |
| `CONFLICT` | `409` | Email already registered, or duplicate unique field. | Highlight the conflicting form field with an error: *"An account with this email already exists"*. |
| `TOO_MANY_REQUESTS` | `429` | Rate limit exceeded (login attempts or file uploads). | Read the `Retry-After` header. Show a countdown banner: *"Too many attempts. Please wait X seconds before retrying."* |
| `AVATAR_LIMIT_EXCEEDED` | `400` | User reached maximum stored avatar gallery quota. | Show dialog prompting user to delete an older avatar before uploading. |
| `MAX_UPLOAD_SIZE_EXCEEDED` / `FILE_TOO_LARGE` | `413` | Uploaded image or attachment exceeds max allowed size (e.g. 5MB / 10MB). | Show toast error: *"File exceeds maximum allowed size (max 5MB)"*. Pre-validate file size on `<input type="file">`. |
| `UNSUPPORTED_FILE_TYPE` / `UNSUPPORTED_IMAGE_FORMAT` | `415` | File format not accepted (magic number validation failed). | Show toast error: *"Unsupported format. Please select JPEG, PNG, or WebP"*. |
| `VIRUS_DETECTED` | `422` | ClamAV antivirus scanner flagged malicious file stream. | Show destructive error modal: *"File upload rejected for security reasons. Please scan your file."* |
| `VIRUS_SCAN_UNAVAILABLE` | `503` | Antivirus daemon is temporarily unreachable. | Show banner: *"Upload temporarily unavailable. Please try again shortly."* |
| `STORAGE_ERROR` | `500` | S3 / MinIO storage failure during write. | Toast: *"Failed to save image. Please retry."* |
| `METHOD_NOT_ALLOWED` | `405` | Wrong HTTP method (e.g., POST instead of GET). | Internal developer error; log to telemetry. |
| `BAD_REQUEST` / `MALFORMED_REQUEST` | `400` | Invalid JSON syntax or unparseable payload. | Check console payload serialization. |
| `INTERNAL_SERVER_ERROR` | `500` | Unhandled server exception. | Display generic error toast: *"An unexpected error occurred. Please try again later."* |

---

## Directory & Profile Contact Privacy (Business Rule)

When querying `GET /api/v1/public/directory` or `GET /api/v1/artisan/{id}`:
- **`contactInfoLocked: true`**:
  - The artisan's name is masked (`"Artisan #XXXXX"`).
  - The phone number, email, address, and website fields are `null`.
  - **Frontend UI trigger**: Display a lock badge $\text{🔒}$ next to the contact details with a call-to-action button: *"Unlock contact details with SoukLab Pro/Premium"*. Clicking opens the Chargily subscription checkout drawer.
- **`contactInfoLocked: false`**:
  - Full artisan name and contact buttons (Call, WhatsApp, Email, Directions) are displayed normally.
