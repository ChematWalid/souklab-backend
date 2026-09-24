# Authentication DTO Package (`com.project.souklab.dto.auth`)

Data transfer contracts for authentication, registration, password lifecycle, and JWT sessions.

---

## Classes Reference

| DTO Class | Direction | Description |
| :--- | :--- | :--- |
| [`UserRegistrationDTO`](UserRegistrationDTO.java) | Inbound | Registration payload with email, password (8–128 chars), firstName, lastName, and `accountType` (`CLIENT` or `ARTISAN`). |
| [`LoginDTO`](LoginDTO.java) | Inbound | Credentials payload (`email` or `username`, `password` max 128 chars) for JWT authentication. |
| [`JwtResponseDTO`](JwtResponseDTO.java) | Outbound | Authentication response returning access token, refresh token, expiration duration, and user summary. |
| [`TokenRefreshRequestDTO`](TokenRefreshRequestDTO.java) | Inbound | Payload containing refresh token for session renewal. |
| [`VerifyEmailRequestDTO`](VerifyEmailRequestDTO.java) | Inbound | Payload with email and 6-digit numeric verification code (`^\d{6}$`). |
| [`ResendVerificationRequestDTO`](ResendVerificationRequestDTO.java) | Inbound | Request to trigger fresh verification code dispatch. |
| [`ForgotPasswordRequestDTO`](ForgotPasswordRequestDTO.java) | Inbound | Request to trigger password reset instructions to user's registered email. |
| [`ResetPasswordRequestDTO`](ResetPasswordRequestDTO.java) | Inbound | Contains email, 6-digit verification code, and `newPassword` (8–128 chars). |
| [`ChangePasswordRequestDTO`](ChangePasswordRequestDTO.java) | Inbound | Contains `oldPassword` (max 128 chars) and `newPassword` (8–128 chars). Validated with `@DifferentPasswords`. |
| [`CompleteProfileRequestDTO`](CompleteProfileRequestDTO.java) | Inbound | Onboarding wizard payload containing address, bio, craft taxonomy IDs, and corporate details. |
| [`UserResponseDTO`](UserResponseDTO.java) | Outbound | Full user representation for administrative and profile management views. |
| [`UserSummaryDTO`](UserSummaryDTO.java) | Outbound | Lean representation of user identity (ID, email, name, avatar). |
| [`UpdateProfileRequestDTO`](UpdateProfileRequestDTO.java) | Inbound | Payload for common user identity updates (first name, last name, phone). |
