# Utility Package (`com.project.souklab.util`)

Stateless helpers, security context extractors, and response serializers.

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`SecurityUtils`](SecurityUtils.java) | Helper to retrieve current authenticated username (email) and user details from `SecurityContextHolder`. |
| [`ArtisanSecurityUtils`](ArtisanSecurityUtils.java) | Centralized resolver for the authenticated artisan principal from SecurityContext and repository lookup. |
| [`CodeGeneratorUtil`](CodeGeneratorUtil.java) | Cryptographically secure random numeric generator (`SecureRandom`) for 6-digit OTP verification codes. |
| [`EmailUtil`](EmailUtil.java) | Asynchronous email dispatch helper for verification codes, password resets, and accreditation notices. |
| [`FileStorageUtil`](FileStorageUtil.java) | File path utilities: sanitized filenames, extension extraction, and UUID-based object key formatting. |
| [`MapperUtil`](MapperUtil.java) | Model-to-DTO conversion helpers. |
| [`ServletResponseUtil`](ServletResponseUtil.java) | Directly writes standardized `ApiResponse` JSON error payloads into `HttpServletResponse` streams from servlet filters. |

