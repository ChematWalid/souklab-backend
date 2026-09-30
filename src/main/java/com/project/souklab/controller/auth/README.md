# Authentication & Profile Controller Package (`com.project.souklab.controller.auth`)

HTTP adapter layer for user authentication, registration onboarding, token lifecycle, password management, and current authenticated user profile (`/me`).

## Controller Reference

- [`AuthController`](AuthController.java): Primary HTTP adapter implementing registration, authentication, token lifecycle, password management, and user profile endpoints under `/api/v1/auth`.

---

## Endpoint Overview

All endpoints are mapped under the `/api/v1/auth` base path.

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register user | Creates a new Client or Artisan account. Artisans start in `PENDING` status. |
| `POST` | `/api/v1/auth/login` | Public | Login credentials | Authenticates with email & password, returning JWT access and refresh tokens. |
| `POST` | `/api/v1/auth/refresh` | Public | Rotate refresh token | Exchanges a valid refresh token for a fresh access/refresh token pair. |
| `POST` | `/api/v1/auth/oauth/exchange` | Public | Exchange OAuth code | Consumes a one-time OAuth authorization code within 60s, returning JWT token pair. |
| `POST` | `/api/v1/auth/logout` | Public/Auth | Logout session | Revokes refresh tokens and invalidates the active user session. |
| `DELETE` | `/api/v1/auth/me` | Authenticated | Delete account | Permanently disables and anonymises account after password or OAuth confirmation. |
| `POST` | `/api/v1/auth/verify-email` | Public | Verify email OTP | Validates a 6-digit numeric OTP code sent upon registration. |
| `POST` | `/api/v1/auth/resend-verification` | Public | Resend verification | Issues a fresh 6-digit verification code to an unverified email. |
| `POST` | `/api/v1/auth/forgot-password` | Public | Forgot password | Dispatches a 6-digit password reset code to the user's email. |
| `POST` | `/api/v1/auth/reset-password` | Public | Reset password | Resets password using the 6-digit OTP code received by email. |
| `POST` | `/api/v1/auth/change-password` | Authenticated | Change password | Updates password for the authenticated caller given current password. |
| `GET` | `/api/v1/auth/me` | Authenticated | Get current profile | Returns the caller's profile (`ArtisanResponseDTO` or `ClientProfileResponseDTO`). |
| `PATCH` | `/api/v1/auth/me` | Authenticated | Patch current profile | JSON Merge Patch for user scalar fields (`firstName`, `lastName`, `phone`) and profile details. |
| `POST` | `/api/v1/auth/complete-profile` | Authenticated | Complete profile | Onboarding wizard to submit initial craft or enterprise details. |
| `GET` | `/api/v1/auth/oauth/google/artisan`| Public | Google OAuth (Artisan)| Sets artisan intent cookie and redirects to Google authorization. |
| `GET` | `/api/v1/auth/oauth/google/client` | Public | Google OAuth (Client) | Sets client intent cookie and redirects to Google authorization. |

---

## Detailed Endpoint Reference

### 1. `GET /api/v1/auth/me` — Current User Profile

Retrieves the identity, status, permissions, and profile details of the user associated with the provided Bearer token.

#### Headers
```http
Authorization: Bearer <accessToken>
```

#### Response Envelope (`200 OK`)
The `data` object is **polymorphic** and differs depending on the user's account type.

##### A. Artisan Response Example
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "id": "e15eabe0-16cc-42e9-aa91-755c96edc611",
    "email": "artisan@souklab.dz",
    "firstName": "Mouloud",
    "lastName": "Mammeri",
    "name": "Mouloud Mammeri",
    "phone": "+213555123456",
    "avatarUrl": "https://storage.souklab.dz/avatars/user-full.webp",
    "accountStatus": "ACTIVE",
    "permissions": ["artisan:content", "formation:enroll", "profile:read", "profile:write"],
    "emailVerified": true,
    "emailVerifiedAt": "2026-09-01T10:00:00",
    "createdAt": "2026-08-15T08:30:00",
    "updatedAt": "2026-09-20T14:22:10",
    "bio": "Artisan potier traditionnel kabyle avec 15 ans d'expérience.",
    "city": "Tizi Ouzou",
    "address": "Village Ath Yanni",
    "website": "https://poterie-kabyle.dz",
    "rating": 4.85,
    "reviewsCount": 24,
    "viewsCount": 350,
    "premium": true,
    "verified": true,
    "teacher": true,
    "region": {
      "id": "reg-15",
      "name": "Tizi Ouzou",
      "code": "15"
    },
    "craftCategory": {
      "id": "cat-pottery",
      "name": "Poterie & Céramique",
      "slug": "poterie-ceramique"
    },
    "subCategory": {
      "id": "sub-pottery-trad",
      "name": "Poterie traditionnelle",
      "slug": "poterie-traditionnelle"
    },
    "primaryMaterials": [
      { "id": "mat-argile", "name": "Argile rouge" },
      { "id": "mat-engobe", "name": "Engobe minéral" }
    ],
    "primaryTechniques": [
      { "id": "tech-modelage", "name": "Modelage manuel" }
    ],
    "epoques": [
      { "id": "ep-berbere", "name": "Traditionnel Berbère" }
    ],
    "certifications": [],
    "galleryImages": []
  },
  "errors": null,
  "traceId": "0321f6ee-9119-4d0c-ade4-2fd7ebc6c4ff"
}
```

##### B. Client Response Example
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "id": "a35df570-f6e5-4b87-acc6-16c1cd9f1aa9",
    "email": "client@souklab.dz",
    "firstName": "Karim",
    "lastName": "Brahimi",
    "name": "Karim Brahimi",
    "phone": "+213661987654",
    "avatarUrl": null,
    "accountStatus": "ACTIVE",
    "permissions": ["formation:enroll", "profile:read", "profile:write"],
    "emailVerified": true,
    "emailVerifiedAt": "2026-09-10T12:00:00",
    "createdAt": "2026-09-10T11:45:00",
    "updatedAt": "2026-09-15T09:12:00",
    "companyName": "Brahimi Imports",
    "clientType": "INDIVIDUAL",
    "premium": false,
    "city": "Algiers",
    "address": "12 Rue Didouche Mourad"
  },
  "errors": null,
  "traceId": "fa9201bc-4e20-4a8f-bc58-45e0d481f930"
}
```

---

### 2. `PATCH /api/v1/auth/me` — Partial Profile Update

Updates the authenticated user's profile using **JSON Merge Patch** semantics (`application/json`).

#### Merge Patch Rules
- **Field omitted** (`undefined`): The field is left completely unchanged in the database.
- **Field set to `null`**: The field is explicitly cleared/wiped in the database (where nullable).
- **Field set to value**: The field is updated to the new value.

#### Headers
```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

#### Request Payload Examples

##### A. Updating User Scalar & Artisan Details
```json
{
  "firstName": "Karim",
  "lastName": "Belkacem",
  "phone": "+213550123456",
  "bio": "Nouvelle biographie mise à jour.",
  "city": "Tizi Ouzou",
  "address": "Rue Principale 42",
  "website": "https://nouvelle-poterie.dz",
  "regionId": "reg-15",
  "subCategoryId": "sub-pottery-trad",
  "materialIds": ["mat-argile", "mat-engobe"],
  "techniqueIds": ["tech-modelage"],
  "epoqueIds": ["ep-berbere"]
}
```

##### B. Updating Client Details
```json
{
  "firstName": "Amina",
  "lastName": "Mansouri",
  "phone": "+213661987654",
  "city": "Oran",
  "companyName": "Nouvelle Entreprise SARL",
  "clientType": "ENTERPRISE"
}
```

#### Response (`200 OK`)
Returns the freshly updated `ApiResponse<ProfileResponse>`.

---

### 2b. `POST /api/v1/auth/oauth/exchange` — Exchange OAuth Authorization Code

Consumes a short-lived, single-use OAuth authorization code returned from the OAuth2 provider callback, returning a standard `JwtResponseDTO` token envelope. Codes expire after 60 seconds and cannot be reused.

#### Request Body (`application/json`)
```json
{
  "code": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

#### Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "OAuth authorization code exchanged successfully.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "type": "Bearer",
    "id": "e15eabe0-16cc-42e9-aa91-755c96edc611",
    "email": "artisan@souklab.dz",
    "accountType": "ARTISAN",
    "permissions": ["artisan:content", "formation:enroll", "profile:read", "profile:write"],
    "profileCompleted": true
  }
}
```

#### Errors
- `401 UNAUTHORIZED`: Code is missing, expired, or previously consumed.

---

### 2c. `DELETE /api/v1/auth/me` — Self-Service Account Deletion

Permanently disables and anonymises the authenticated user's account to fulfill GDPR "Right to Erasure" requirements. Revokes active refresh tokens, replaces direct PII (`email`, `firstName`, `lastName`, `phone`, `avatarUrl`) with anonymised values (`deleted+<id>@deleted.souklab.invalid`), sets account status to `DELETED`, and logs an immutable audit event (`AuditLogAction.User.DELETED`).

Requires either password verification or recent OAuth confirmation (within the last 10 minutes).

#### Headers
```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

#### Request Body (`application/json`)
```json
{
  "password": "CurrentPassword123!"
}
```
*Or for OAuth2-authenticated accounts:*
```json
{
  "oauthConfirmed": true
}
```

#### Response (`204 No Content`)
Empty body. Immediate revocation of all session and refresh tokens.

#### Errors
- `401 UNAUTHORIZED`: Caller is unauthenticated, password does not match, or OAuth confirmation is older than 10 minutes.

---

### 3. `POST /api/v1/auth/register` — User Registration

Registers a new user on the platform.

#### Request Body (`application/json`)
```json
{
  "email": "user@example.dz",
  "password": "StrongPassword123!",
  "firstName": "Fatima",
  "lastName": "Zahra",
  "name": "Fatima Zahra",
  "accountType": "ARTISAN"
}
```

#### Validation Notes
- `accountType`: Required. Either `"CLIENT"` or `"ARTISAN"`.
- `password`: Required. Must be between 8 and 128 characters.
- `email`: Required valid email address.
- Artisans receive initial status `PENDING` awaiting admin validation. Clients receive `ACTIVE` (with `emailVerified: false`).

#### Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Registration successful. Please verify your email.",
  "data": {
    "id": "e15eabe0-16cc-42e9-aa91-755c96edc611",
    "email": "user@example.dz",
    "name": "Fatima Zahra",
    "firstName": "Fatima",
    "lastName": "Zahra",
    "status": "PENDING",
    "emailVerified": false,
    "permissions": ["permission:artisan:content"]
  }
}
```

---

### 4. `POST /api/v1/auth/login` — Credentials Authentication

Authenticates an existing user and returns their JWT token pair.

#### Request Body (`application/json`)
```json
{
  "email": "user@example.dz",
  "password": "StrongPassword123!"
}
```
*Note: Clients may authenticate with either `email` or `username`. Password is capped at 128 characters.*

#### Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "e15eabe0-16cc-42e9-aa91-755c96edc611",
      "email": "user@example.dz",
      "firstName": "Fatima",
      "lastName": "Zahra",
      "name": "Fatima Zahra",
      "avatarUrl": null,
      "accountStatus": "ACTIVE",
      "permissions": ["artisan:content", "profile:read", "profile:write"]
    }
  }
}
```

#### Typed Error Codes (`403 Forbidden`)
If authentication credentials are valid but the account is not eligible to sign in, a `403 FORBIDDEN` is returned with a machine-readable `errorCode`:

| Error Code | HTTP Status | Trigger Condition |
| :--- | :--- | :--- |
| `ACCOUNT_LOCKED` | `403` | Too many failed login attempts; temporary lockout is active. |
| `ACCOUNT_SUSPENDED` | `403` | Administrator placed a permanent or temporary ban on the account. |
| `ACCOUNT_REJECTED` | `403` | Artisan account registration was formally rejected by an administrator. |
| `ACCOUNT_PENDING` | `403` | Artisan account is awaiting administrative identity & credential approval. |
| `EMAIL_NOT_VERIFIED` | `403` | User registered but has not yet completed 6-digit email OTP verification. |

```json
{
  "success": false,
  "code": 403,
  "errorCode": "ACCOUNT_SUSPENDED",
  "message": "Account is suspended: Policy violation"
}
```

---

### 5. `POST /api/v1/auth/refresh` — Token Rotation

Renews an expired access token using the active refresh token.

#### Request Body (`application/json`)
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Response (`200 OK`)
Returns a new `JwtResponseDTO` containing a fresh `accessToken` and rotated `refreshToken`.

---

### 6. `POST /api/v1/auth/logout` — Revoke Session

Invalidates the active refresh token and terminates the session.

#### Request Body (`application/json`, optional)
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Logout successful.",
  "data": null
}
```

---

### 7. Email Verification Flow

#### A. `POST /api/v1/auth/verify-email`
Validates the 6-digit OTP received via email upon registration.
```json
{
  "email": "user@example.dz",
  "code": "123456"
}
```
*Note: Returns HTTP 400 Bad Request (`"Invalid or expired code."`) uniformly if the code is invalid/expired or if the email is not registered, preventing user enumeration.*

#### B. `POST /api/v1/auth/resend-verification`
Requests a fresh 6-digit OTP code for an unverified account.
```json
{
  "email": "user@example.dz"
}
```

---

### 8. Password Recovery & Management

#### A. `POST /api/v1/auth/forgot-password`
Dispatches a 6-digit password reset code to the email address.
```json
{
  "email": "user@example.dz"
}
```

#### B. `POST /api/v1/auth/reset-password`
Resets the password using the received 6-digit code.
```json
{
  "email": "user@example.dz",
  "code": "654321",
  "newPassword": "NewStrongPassword123!"
}
```
*Validation: `email` (valid format), `code` (6-digit numeric pattern `^\d{6}$`), `newPassword` (8–128 characters).*

#### C. `POST /api/v1/auth/change-password` *(Authenticated)*
Changes the authenticated caller's password.
```json
{
  "oldPassword": "OldPassword123!",
  "newPassword": "NewStrongPassword123!"
}
```
*Validation: `oldPassword` (max 128 characters), `newPassword` (8–128 characters). Enforced by `@DifferentPasswords` so `newPassword` must differ from `oldPassword`.*

---

### 9. Google OAuth2 Social Login

- **Artisan intent**: `GET /api/v1/auth/oauth/google/artisan`
- **Client intent**: `GET /api/v1/auth/oauth/google/client`

Initiates Google OAuth2 login/signup flow with account-type intent:
1. Sets an HTTP-only intent cookie `souklab_oauth_intent` with value `ARTISAN` or `CLIENT`.
   - Security attributes: `Path=/`, `HttpOnly=true`, `SameSite=Lax`, `Secure` (when request is HTTPS), `Max-Age=300`.
   - Avoids unwanted session creation (`request.getSession(false)`) to maintain stateless architecture.
2. Redirects to `/oauth2/authorization/google`.
3. Upon successful Google authentication, the `OAuth2AuthenticationSuccessHandler` validates verified email (`email_verified == true`), creates/loads the user with the specified role, immediately clears the intent cookie, stores a one-time authorization code for 60 seconds, and returns HTTP 302 to the configured `GOOGLE_OAUTH_AUTHORIZED_REDIRECT_URI` with `?code=...`. The frontend exchanges it once through `POST /api/v1/auth/oauth/exchange`; raw JWT JSON is not returned from the browser callback path.

---

## TypeScript Interfaces for Frontend Developers

```typescript
export interface ApiResponse<T> {
  success: boolean;
  code: number;
  message: string;
  data: T;
  errors?: Record<string, string> | null;
  traceId?: string;
}

export interface RegisterRequest {
  email: string;
  password: string; // 8 - 128 characters
  firstName: string;
  lastName: string;
  name?: string;
  accountType: 'ARTISAN' | 'CLIENT';
}

export interface LoginRequest {
  email?: string;
  username?: string;
  password: string; // max 128 characters
}

export interface VerifyEmailRequest {
  email: string;
  code: string; // 6 digits
}

export interface ResendVerificationRequest {
  email: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  code: string; // 6 digits
  newPassword: string; // 8 - 128 characters
}

export interface ChangePasswordRequest {
  oldPassword: string; // max 128 characters
  newPassword: string; // 8 - 128 characters
}

export interface UserSummary {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  name: string;
  avatarUrl: string | null;
  accountStatus: 'ACTIVE' | 'PENDING' | 'SUSPENDED' | 'REJECTED';
  permissions: string[];
}

export interface JwtResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: UserSummary;
}

export interface BaseProfileResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  name: string;
  phone: string | null;
  avatarUrl: string | null;
  accountStatus: 'ACTIVE' | 'PENDING' | 'SUSPENDED' | 'REJECTED';
  permissions: string[];
  emailVerified: boolean;
  emailVerifiedAt: string | null;
  createdAt: string;
  updatedAt: string;
  premium: boolean;
  city?: string | null;
  address?: string | null;
}

export interface ArtisanProfileResponse extends BaseProfileResponse {
  bio: string | null;
  website: string | null;
  rating: number;
  reviewsCount: number;
  viewsCount: number;
  verified: boolean;
  teacher: boolean;
  region: { id: string; name: string; code: string } | null;
  craftCategory: { id: string; name: string; slug: string } | null;
  subCategory: { id: string; name: string; slug: string } | null;
  primaryMaterials: Array<{ id: string; name: string }>;
  primaryTechniques: Array<{ id: string; name: string }>;
  epoques: Array<{ id: string; name: string }>;
}

export interface ClientProfileResponse extends BaseProfileResponse {
  companyName: string | null;
  clientType: 'INDIVIDUAL' | 'ENTERPRISE';
}

export type ProfileResponse = ArtisanProfileResponse | ClientProfileResponse;

export interface UserPatchRequest {
  bio?: string | null;
  city?: string | null;
  address?: string | null;
  website?: string | null;
  regionId?: string | null;
  subCategoryId?: string | null;
  materialIds?: string[] | null;
  techniqueIds?: string[] | null;
  epoqueIds?: string[] | null;
  companyName?: string | null;
  clientType?: 'INDIVIDUAL' | 'ENTERPRISE' | null;
}
```

---

## Frontend Integration Tips

1. **Silent Token Refresh**:
   Intercept `401 Unauthorized` responses once per request queue. Call `POST /api/v1/auth/refresh` with the stored `refreshToken`. If successful, replay the queued requests with the new `accessToken`. If refresh returns 401/403, immediately clear user storage and redirect to `/login`.
2. **User Profile State**:
   On application bootstrap, call `GET /api/v1/auth/me`. Use `permissions.includes('artisan:content')` or the presence of `craftCategory` to determine whether the user is an Artisan or Client.
3. **Avatar Upload**:
   Avatar uploads are handled by the dedicated endpoint `POST /api/v1/users/me/avatars` with `multipart/form-data` (key: `file`). See the [`user`](../user/README.md) documentation for full details.
