# Authentication Service Package (`com.project.souklab.service.auth`)

Core authentication workflows, credential hashing, registration state machines, and Spring Security identity adaptation.

---

## Key Workflows

### 1. Registration & Initial Capability Assignment
- New client registrations are saved with `AccountStatus.ACTIVE` and require email verification.
- New artisan registrations are saved with `AccountStatus.PENDING`, requiring administrative vetting and approval before activation.

### 2. Spring Security UserDetailsService Adapter
- `CustomUserDetailsService` bridges SoukLab `User` entities into Spring Security `UserDetails` using enabled database permissions. Legacy role authorities are not emitted or accepted.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`AuthService`](AuthService.java) | Handles `registerUser`, `login`, `logout`, `refreshToken`, `verifyEmail`, `resendVerification`, `forgotPassword`, `resetPassword`, and `changePassword`. Profile operations are owned by `ProfileService`. |
| [`PermissionManagementService`](PermissionManagementService.java) | Adds and removes enabled database-backed capabilities for users after administrator authorization. |
| [`CustomUserDetailsService`](CustomUserDetailsService.java) | Implements Spring Security's `UserDetailsService`, loading users by email with enabled permission authorities. |
