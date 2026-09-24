# Authentication Service Package (`com.project.souklab.service.auth`)

Core authentication workflows, credential hashing, registration state machines, and Spring Security identity adaptation.

---

## Key Workflows

### 1. Registration & Initial Capability Assignment
- New client registrations are saved with `AccountStatus.ACTIVE` and require email verification.
- New artisan registrations are saved with `AccountStatus.PENDING`, requiring administrative vetting and approval before activation.
- Password inputs across registration, login, and reset/change flows enforce bounds up to 128 characters to mitigate password hashing denial-of-service.

### 2. Email Verification & Anti-Enumeration Protection
- `verifyEmail` validates the 6-digit numeric OTP code.
- If the token is invalid, expired, or if no account exists for the provided email, a uniform `BadRequestException("Invalid or expired code.")` is returned to prevent account enumeration.

### 3. OAuth2 Registration & Account Provisioning
- In Google OAuth2 flow, `email_verified` claim is strictly verified: `Boolean.TRUE.equals(emailVerifiedObj)` rejects null, `false`, or `"false"` values.
- New users provisioned via OAuth receive verified email status (`emailVerified = true`, `emailVerifiedAt = LocalDateTime.now()`) with the requested account type intent (`ARTISAN` or `CLIENT`).

### 4. Spring Security UserDetailsService Adapter
- `CustomUserDetailsService` bridges SoukLab `User` entities into Spring Security `UserDetails` using enabled database permissions. Account type is onboarding metadata; legacy role authorities are not emitted or accepted.

### 5. Administrative Permission Management Guardrails
- `PermissionManagementService` allows authorized administrators (`permission:admin:users`) to inspect, grant, and revoke permissions.
- Prevents administrative self-lockout: an administrator cannot revoke `permission:admin:users` from their own account.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`AuthService`](AuthService.java) | Handles `registerUser`, `login`, `logout`, `refreshToken`, `verifyEmail`, `resendVerification`, `forgotPassword`, `resetPassword`, and `changePassword`. Enforces anti-enumeration and strict OAuth email verification. |
| [`PermissionManagementService`](PermissionManagementService.java) | Adds and removes enabled permissions from the canonical grouped `Permission` enum catalog after administrator authorization, enforcing self-revocation guardrails. |
| [`CustomUserDetailsService`](CustomUserDetailsService.java) | Implements Spring Security's `UserDetailsService`, loading users by email with enabled permission authorities. |
