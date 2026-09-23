# Authentication & Profile Controller Package (`com.project.souklab.controller.auth`)

HTTP adapter layer for user authentication, registration onboarding, token lifecycle, password management, and current authenticated user profile (`/me`).

---

## Endpoint Overview

All endpoints are mapped under the `/api/v1/auth` base path.

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register user | Creates a new Client or Artisan account. Artisans start in `PENDING` status. |
| `POST` | `/api/v1/auth/login` | Public | Login credentials | Authenticates with email & password, returning JWT access and refresh tokens. |
| `POST` | `/api/v1/auth/refresh` | Public | Rotate refresh token | Exchanges a valid refresh token for a fresh access/refresh token pair. |
| `POST` | `/api/v1/auth/logout` | Public/Auth | Logout session | Revokes refresh tokens and invalidates the active user session. |
| `POST` | `/api/v1/auth/verify-email` | Public | Verify email OTP | Validates a 6-digit numeric OTP code sent upon registration. |
| `POST` | `/api/v1/auth/resend-verification` | Public | Resend verification | Issues a fresh 6-digit verification code to an unverified email. |
| `POST` | `/api/v1/auth/forgot-password` | Public | Forgot password | Dispatches a 6-digit password reset code to the user's email. |
| `POST` | `/api/v1/auth/reset-password` | Public | Reset password | Resets password using the 6-digit OTP code received by email. |
| `POST` | `/api/v1/auth/change-password` | Authenticated | Change password | Updates password for the authenticated caller given current password. |
| `GET` | `/api/v1/auth/me` | Authenticated | Get current profile | Returns the caller's profile (`ArtisanResponseDTO` or `ClientProfileResponseDTO`). |
| `PATCH` | `/api/v1/auth/me` | Authenticated | Patch current profile | JSON Merge Patch for profile fields (bio, address, crafts, company, etc.). |
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

##### A. Updating Artisan Details
```json
{
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
  "city": "Oran",
  "companyName": "Nouvelle Entreprise SARL",
  "clientType": "ENTERPRISE"
}
```

#### Response (`200 OK`)
Returns the freshly updated `ApiResponse<ProfileResponse>`.

---

### 3. `POST /api/v1/auth/register` — User Registration

Registers a new user on the platform.

#### Request Body (`application/json`)
```json
{
  "email": "user@example.dz",
  "password": "StrongPassword123!",
  "confirmPassword": "StrongPassword123!",
  "firstName": "Fatima",
  "lastName": "Zahra",
  "phone": "+213550123456",
  "accountRole": "ARTISAN",
  "craftCategoryId": "cat-pottery",
  "subCategoryId": "sub-pottery-trad",
  "wilayaId": "reg-15"
}
```

#### Validation Notes
- `accountRole`: `"CLIENT"` or `"ARTISAN"`.
- `password`: Must be 8–72 characters, containing at least one uppercase letter, one lowercase letter, one digit, and one special character.
- `confirmPassword`: Must match `password` exactly.
- Artisans receive initial status `PENDING` awaiting admin validation. Clients receive `ACTIVE` (with `emailVerified: false`).

#### Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Registration successful. Welcome to Souklab!",
  "data": { ... ProfileResponse ... }
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
  "newPassword": "NewStrongPassword123!",
  "confirmPassword": "NewStrongPassword123!"
}
```

#### C. `POST /api/v1/auth/change-password` *(Authenticated)*
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewStrongPassword123!",
  "confirmPassword": "NewStrongPassword123!"
}
```

---

### 9. Google OAuth2 Social Login

- **Artisan intent**: `GET /api/v1/auth/oauth/google/artisan`
- **Client intent**: `GET /api/v1/auth/oauth/google/client`

Redirects browser to Google OAuth consent screen. On callback, sets secure cookies and redirects back to the frontend with auth tokens.

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
