# Auth Controller Package (`com.project.souklab.controller.auth`)

Handles authentication, onboarding, credential recovery, and session token renewal.

---

## Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Registers a new artisan or client account. Artisans start in `PENDING` status. |
| `POST` | `/api/v1/auth/login` | Public | Authenticates credentials and returns access token + refresh token cookie/body. |
| `POST` | `/api/v1/auth/refresh` | Public | Exchanges a valid refresh token for a new access token. |
| `POST` | `/api/v1/auth/verify-email` | Public | Validates a 6-digit numeric verification code to activate client email accounts. |
| `POST` | `/api/v1/auth/resend-verification` | Public | Issues a fresh verification code and invalidates older active codes. |
| `POST` | `/api/v1/auth/forgot-password` | Public | Triggers password reset email with a single-use verification token. |
| `POST` | `/api/v1/auth/reset-password` | Public | Validates reset code and updates user password with confirmation matching. |
| `POST` | `/api/v1/auth/change-password` | Authenticated | Allows authenticated users to change password given their existing password. |
| `POST` | `/api/v1/auth/complete-profile` | Authenticated | Wizard endpoint to fill profile data (bio, address, crafts, company). |
| `GET` | `/api/v1/auth/oauth/google/artisan` | Public | Initiates Google OAuth2 login flow with artisan account-type intent. |
| `GET` | `/api/v1/auth/oauth/google/client` | Public | Initiates Google OAuth2 login flow with client account-type intent. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`AuthController`](AuthController.java) | Entrypoint controller for all authentication, password reset, and registration workflows. |
