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
| `FORBIDDEN` | `403` | Caller lacks required capability or profile row (e.g. non-client accessing favorites). | Display contextual message or redirect to an upgrade / verification view. |
| `RESOURCE_NOT_FOUND` | `404` | Artisan, post, conversation, or formation ID not found in database. | Render a 404 Empty State / Not Found component with a back button. |
| `CONFLICT` | `409` | Duplicate unique record (email, duplicate favorite, duplicate catalog slug) or capacity limit reached. | Highlight the conflicting form field or display a capacity limit modal. |
| `BAD_REQUEST` | `400` | Generic malformed request or failed business precondition. | Display backend error message or highlight form validation issues. |
| `MALFORMED_REQUEST` | `400` | Unparseable JSON body, malformed syntax, or Jackson deserialization error. | Inspect client payload serialization; ensure valid JSON types. |
| `MISSING_PARAMETER` | `400` | Required query parameter or form field is absent. | Highlight the missing parameter/field in the UI before form submission. |
| `INVALID_PARAMETER` | `400` | Invalid query parameter value or unknown sort attribute (e.g. invalid sort property). | Reset query/sort controls to default values and notify user. |
| `METHOD_NOT_ALLOWED` | `405` | Wrong HTTP method dispatched to the endpoint (e.g., POST instead of GET). | Internal developer error; log to telemetry. |
| `UNSUPPORTED_MEDIA_TYPE` | `415` | Request `Content-Type` is not accepted by endpoint. | Verify `Content-Type: application/json` or multipart boundaries. |
| `TOO_MANY_REQUESTS` | `429` | Rate limit exceeded (login attempts, file uploads, or API requests). | Read the `Retry-After` header. Show a countdown banner: *"Too many attempts. Please wait X seconds before retrying."* |
| `AVATAR_LIMIT_EXCEEDED` | `400` | User reached maximum stored avatar gallery quota (max 10 avatars). | Show dialog prompting user to delete an older avatar before uploading a new one. |
| `MAX_UPLOAD_SIZE_EXCEEDED` | `413` | Uploaded multipart file exceeds server-configured max size limit. | Show toast: *"File exceeds maximum allowed size"*. Validate `file.size` before dispatching. |
| `FILE_TOO_LARGE` | `413` | Specific file stream exceeds max allowed size (e.g. 5MB for avatars, 15MB for certifications). | Show error banner with maximum size limit for that specific file category. |
| `FILE_NOT_FOUND` | `404` | File key does not exist in MinIO/S3 storage. | Display broken media placeholder or error message. |
| `INVALID_FILENAME` | `400` | Uploaded filename contains illegal path traversal characters or unsafe extensions. | Sanitize filename before sending; reject files with path separators. |
| `UNSUPPORTED_FILE_TYPE` | `415` | File MIME type is rejected by validation policy. | Display allowed file extensions (e.g. PDF, JPG, PNG). |
| `UNSUPPORTED_IMAGE_FORMAT` | `415` | Image magic bytes do not match JPEG, PNG, or WebP. | Restrict file picker to image formats and validate magic bytes on client if feasible. |
| `VIRUS_DETECTED` | `422` | ClamAV antivirus daemon flagged malicious file stream. | Show destructive error modal: *"File upload rejected for security reasons. Please scan your file."* |
| `VIRUS_SCAN_UNAVAILABLE` | `503` | Antivirus daemon is temporarily unreachable. | Show banner: *"Upload temporarily unavailable. Please try again shortly."* |
| `STORAGE_ERROR` | `500` | MinIO / S3 storage failure during read or write. | Toast: *"Failed to save or retrieve file. Please retry."* |
| `INTERNAL_SERVER_ERROR` | `500` | Unhandled server exception. Stack trace logged server-side with opaque safe error to client. | Display generic error toast: *"An unexpected error occurred. Please try again later."* |

---

## Directory & Profile Contact Privacy (Business Rule)

When querying `GET /api/v1/public/directory` or `GET /api/v1/artisan/{id}`:
- **`contactInfoLocked: true`**:
  - The artisan's name is masked (`"Artisan #XXXXX"`).
  - The phone number, email, address, and website fields are `null`.
  - **Frontend UI trigger**: Display a lock badge $\text{🔒}$ next to the contact details with a call-to-action button: *"Unlock contact details with SoukLab Pro/Premium"*. Clicking opens the Chargily subscription checkout drawer.
- **`contactInfoLocked: false`**:
  - Full artisan name and contact buttons (Call, WhatsApp, Email, Directions) are displayed normally.
