# Souklab API — Complete Postman Reference Guide

This document provides the exhaustive specification for all requests, headers, request bodies, and responses configured in the **Souklab Postman Collection** (`.postman/souklab.postman_collection.json`).

## Table of Contents
1. [Auth — Client](#1-auth--client)
2. [Auth — Artisan](#2-auth--artisan)
3. [Auth — Admin](#3-auth--admin)
4. [Artisan — Profile View](#4-artisan--profile-view)
5. [Auth — Password Reset & Misc](#5-auth--password-reset--misc)
6. [Notifications](#6-notifications)
7. [Formateur — Artisan Actions](#7-formateur--artisan-actions)
8. [Formateur — Admin Actions](#8-formateur--admin-actions)
9. [Admin — User Moderation](#9-admin--user-moderation)
10. [File Storage](#10-file-storage)

---

## 1. Auth — Client

> Full lifecycle for CLIENT account: Registration, Email Verification, Login, Me inspection, Partial Profile Updates, Token Refresh, Password Mutation, and Logout.

### 1.1 Register (ROLE_CLIENT)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/register`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{clientEmail}}",
  "password": "Password123!",
  "firstName": "Yacine",
  "lastName": "Brahimi",
  "role": "CLIENT"
}
```

#### Response Examples
##### Success Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Registration successful. Welcome to Souklab!",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "user@souklab.dz",
    "firstName": "Yacine",
    "lastName": "Brahimi",
    "roles": [
      "ROLE_CLIENT"
    ],
    "accountStatus": "ACTIVE",
    "emailVerified": false,
    "createdAt": "2026-09-03T20:00:00"
  }
}
```

---

### 1.2 Verify Email (Client)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/verify-email`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{clientEmail}}",
  "code": "{{verificationCode}}"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Email verified successfully.",
  "data": null
}
```

---

### 1.3 Login (Client)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/login`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{clientEmail}}",
  "password": "Password123!"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7f708074-43f0-4cdc-a5f2-160ebd833908",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
      "email": "user@souklab.dz",
      "firstName": "Yacine",
      "lastName": "Brahimi",
      "name": "Yacine Brahimi",
      "phone": null,
      "avatarUrl": null,
      "accountStatus": "ACTIVE",
      "roles": [
        "ROLE_CLIENT"
      ],
      "emailVerified": true,
      "emailVerifiedAt": "2026-09-03T20:00:00",
      "createdAt": "2026-09-03T20:00:00",
      "updatedAt": "2026-09-03T20:00:00",
      "clientType": "INDIVIDUAL",
      "companyName": null,
      "bio": null,
      "address": null,
      "regionId": null,
      "city": null
    },
    "roles": [
      "ROLE_CLIENT"
    ]
  }
}
```

---

### 1.4 Get Current User / Me (Client)
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/auth/me`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{clientAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "user@souklab.dz",
    "firstName": "Yacine",
    "lastName": "Brahimi",
    "name": "Yacine Brahimi",
    "phone": "+213 555 12 34 56",
    "avatarUrl": null,
    "accountStatus": "ACTIVE",
    "roles": [
      "ROLE_CLIENT"
    ],
    "emailVerified": true,
    "emailVerifiedAt": "2026-09-03T20:05:00",
    "createdAt": "2026-09-03T20:00:00",
    "updatedAt": "2026-09-03T20:05:00"
  }
}
```

---

### 1.5 Update Profile / Patch Me (Client)
- **Method**: `PATCH`
- **Endpoint**: `{{baseUrl}}/auth/me`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{clientAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "city": "Oran"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Profile updated successfully.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "client@souklab.dz",
    "firstName": "Yacine",
    "lastName": "Brahimi",
    "city": "Oran",
    "accountStatus": "ACTIVE",
    "roles": [
      "ROLE_CLIENT"
    ],
    "emailVerified": true,
    "updatedAt": "2026-09-03T20:10:00"
  }
}
```

---

### 1.6 Refresh Token (Client)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/refresh`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "refreshToken": "{{clientRefreshToken}}"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Token refreshed successfully.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "e7c2f821-2e55-4d33-9118-8f553e1a8a22",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "roles": [
      "ROLE_CLIENT"
    ]
  }
}
```

---

### 1.7 Change Password (Client)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/change-password`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{clientAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "oldPassword": "Password123!",
  "newPassword": "NewPassword456!"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Action processed successfully.",
  "data": null
}
```

---

### 1.8 Logout (Client)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/logout`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{clientAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "refreshToken": "{{clientRefreshToken}}"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Action processed successfully.",
  "data": null
}
```

---

## 2. Auth — Artisan

> Full onboarding lifecycle for ARTISAN account: Registration, Verification, Login, Complete Profile with craft specialties, Partial Profile Updates, and Token Refresh.

### 2.1 Register (ROLE_ARTISAN)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/register`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisanEmail}}",
  "password": "Password123!",
  "firstName": "Karim",
  "lastName": "Ziani",
  "role": "ARTISAN"
}
```

#### Response Examples
##### Success Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Registration successful. Your artisan account has been created and is pending administrator verification.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "user@souklab.dz",
    "firstName": "Karim",
    "lastName": "Ziani",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "accountStatus": "PENDING",
    "emailVerified": false,
    "createdAt": "2026-09-03T20:00:00"
  }
}
```

---

### 2.2 Verify Email (Artisan)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/verify-email`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisanEmail}}",
  "code": "{{verificationCode}}"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Email verified successfully.",
  "data": null
}
```

---

### 2.3 Login (Artisan)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/login`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisanEmail}}",
  "password": "Password123!"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7f708074-43f0-4cdc-a5f2-160ebd833908",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
      "email": "user@souklab.dz",
      "firstName": "Karim",
      "lastName": "Ziani",
      "name": "Karim Ziani",
      "phone": null,
      "avatarUrl": null,
      "accountStatus": "PENDING",
      "roles": [
        "ROLE_ARTISAN"
      ],
      "emailVerified": true,
      "emailVerifiedAt": "2026-09-03T20:00:00",
      "createdAt": "2026-09-03T20:00:00",
      "updatedAt": "2026-09-03T20:00:00",
      "bio": null,
      "regionId": null,
      "city": null,
      "address": null,
      "website": null,
      "subCategoryId": null,
      "teacher": false,
      "verified": false,
      "premium": false,
      "rating": 0.0,
      "reviewsCount": 0
    },
    "roles": [
      "ROLE_ARTISAN"
    ]
  }
}
```

---

### 2.4 Complete Profile (Artisan)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/complete-profile`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "bio": "Master ceramist specializing in traditional Berber and Islamic pottery.",
  "city": "Tizi Ouzou",
  "address": "Route des Potiers, Atelier 4",
  "website": "https://ziani-art.dz",
  "isTeacher": false
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Profile completed successfully.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "artisan@souklab.dz",
    "bio": "Woodworking artisan specializing in cedar and walnut wood carvings.",
    "city": "Batna",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "accountStatus": "ACTIVE",
    "emailVerified": true
  }
}
```

---

### 2.5 Update Profile / Patch Me (Artisan)
- **Method**: `PATCH`
- **Endpoint**: `{{baseUrl}}/auth/me`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "city": "Constantine"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Profile updated successfully.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "artisan@souklab.dz",
    "firstName": "Karim",
    "lastName": "Ziani",
    "city": "Constantine",
    "accountStatus": "ACTIVE",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "emailVerified": true,
    "updatedAt": "2026-09-03T20:10:00"
  }
}
```

---

### 2.6 Refresh Token (Artisan)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/refresh`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "refreshToken": "{{artisanRefreshToken}}"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Token refreshed successfully.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "e7c2f821-2e55-4d33-9118-8f553e1a8a22",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "roles": [
      "ROLE_CLIENT"
    ]
  }
}
```

---

## 3. Auth — Admin

> Initial administrative bootstrap and user activation flows.

### 3.1 Login Admin (Config-Driven Credentials)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/login`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{adminEmail}}",
  "password": "{{adminPassword}}"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7f708074-43f0-4cdc-a5f2-160ebd833908",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "roles": [
      "ROLE_ADMIN"
    ]
  }
}
```

---

### 3.2 Approve Artisan 1 Account (as Admin)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/{{artisanId}}/approve`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "User approved successfully",
  "data": null
}
```

---

## 4. Artisan — Profile View

> Public and authenticated profile inspection for Artisans, with premium-gated contact privacy.

### 4.1 Get Artisan Profile — Non-Premium Client View (Locked)
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/artisan/{{targetArtisanId}}`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{clientAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "id": "7db2b93c-4999-4fd1-b840-1ed47a1bea5e",
    "name": "Artisan #BEA5E",
    "bio": "Pottery master specializing in Kabyle traditional ceramic jars.",
    "city": "Algiers",
    "regionId": "e1f1c7d2-45e6-48f1-83d2-38d1792e31e2",
    "subCategoryId": "88d3b844-32e1-4cfa-9302-39c819385d82",
    "rating": 4.8,
    "reviewsCount": 12,
    "teacher": false,
    "verified": true,
    "avatarUrl": null,
    "contactInfoLocked": true,
    "email": null,
    "phone": null,
    "website": null,
    "address": null,
    "createdAt": "2026-09-03T19:30:00"
  }
}
```

---

### 4.2 Get Artisan Profile — Self / Unlocked View
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/artisan/{{targetArtisanId}}`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "id": "7db2b93c-4999-4fd1-b840-1ed47a1bea5e",
    "name": "Karim Mansouri",
    "bio": "Pottery master specializing in Kabyle traditional ceramic jars.",
    "city": "Algiers",
    "regionId": "e1f1c7d2-45e6-48f1-83d2-38d1792e31e2",
    "subCategoryId": "88d3b844-32e1-4cfa-9302-39c819385d82",
    "rating": 4.8,
    "reviewsCount": 12,
    "teacher": false,
    "verified": true,
    "avatarUrl": "https://storage.souklab.dz/avatars/karim.jpg",
    "contactInfoLocked": false,
    "email": "karim.mansouri@souklab.dz",
    "phone": "+213 555 12 34 56",
    "website": "https://karim-pottery.dz",
    "address": "12 Rue Didouche Mourad",
    "createdAt": "2026-09-03T19:30:00"
  }
}
```

---

## 5. Auth — Password Reset & Misc

> Account recovery and credential lifecycle endpoints.

### 5.1 Forgot Password
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/forgot-password`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisanEmail}}"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Action processed successfully.",
  "data": null
}
```

---

### 5.2 Reset Password
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/reset-password`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisanEmail}}",
  "code": "{{verificationCode}}",
  "newPassword": "ResetPassword123!"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Action processed successfully.",
  "data": null
}
```

---

### 5.3 Resend Verification
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/resend-verification`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisanEmail}}"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Action processed successfully.",
  "data": null
}
```

---

## 6. Notifications

> In-app notification feeds, unread badge counts, read status updates, and soft deletions.
> **Note on Security & Ownership:** Ownership is query-scoped (`findByRecipientIdAndDeletedAtIsNull`). Attempting to read or delete a notification belonging to another user (or an already soft-deleted notification) returns `404 Not Found` with `RESOURCE_NOT_FOUND`, preventing resource enumeration.

### 6.1 Get Notifications (Paginated)
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/notifications?page=0&size=20`
- **Query Parameters**:
  - `page` (optional, default: `0`): Page index (0-based)
  - `size` (optional, default: `20`): Page size
  - *Default sort: `createdAt DESC` (newest first). Soft-deleted rows (`deletedAt IS NOT NULL`) are strictly excluded.*

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "18f3a09e-71c4-4b51-8e0f-48d8b67f1092",
        "message": "Your artisan profile has been vetted and approved by our team.",
        "type": "ACCOUNT_VALIDATED",
        "read": false,
        "targetId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
        "createdAt": "2026-09-03T20:15:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

### 6.2 Get Unread Count
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/notifications/unread-count`
- **Description**: Returns the count of unread, non-deleted notifications (`read = false AND deletedAt IS NULL`) as a raw integer in `data`.

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": 3
}
```

---

### 6.3 Mark One As Read
- **Method**: `PUT`
- **Endpoint**: `{{baseUrl}}/notifications/{{notificationId}}/read`
- **Description**: Marks a specific notification as read (`read = true`). Returns the updated notification DTO in `data`.

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "id": "18f3a09e-71c4-4b51-8e0f-48d8b67f1092",
    "message": "Your artisan profile has been vetted and approved by our team.",
    "type": "ACCOUNT_VALIDATED",
    "read": true,
    "targetId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "createdAt": "2026-09-03T20:15:00"
  }
}
```

##### Not Found Response (`404 Not Found`)
*Returned if the notification ID does not exist, belongs to another user, or is already soft-deleted.*
```json
{
  "success": false,
  "code": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Notification not found with id: 18f3a09e-71c4-4b51-8e0f-48d8b67f1092",
  "data": null
}
```

---

### 6.4 Mark All As Read
- **Method**: `PUT`
- **Endpoint**: `{{baseUrl}}/notifications/read-all`
- **Description**: Bulk updates all unread, non-deleted notifications for the authenticated user to `read = true`. Only touches rows where `read = false AND deletedAt IS NULL`.

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "All notifications marked as read",
  "data": null
}
```

---

### 6.5 Delete Notification
- **Method**: `DELETE`
- **Endpoint**: `{{baseUrl}}/notifications/{{notificationId}}`
- **Description**: Soft-deletes a notification (`deletedAt = now()`). Immediately decrements unread count if the notification was unread, and excludes it from future queries.

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Notification deleted successfully",
  "data": null
}
```

##### Not Found Response (`404 Not Found`)
*Returned if the notification ID does not exist, belongs to another user, or has already been soft-deleted.*
```json
{
  "success": false,
  "code": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Notification not found with id: 18f3a09e-71c4-4b51-8e0f-48d8b67f1092",
  "data": null
}
```

---

## 7. Formateur — Artisan Actions

> Artisan workflows for submitting Formateur accreditation applications and handling application state.

### 7.1 Submit Formateur Request (with motivation)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/artisan/formateur-request`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "motivation": "I have 10 years experience in traditional ceramics and masterclass pedagogy."
}
```

#### Response Examples
##### Success Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Formateur request submitted successfully.",
  "data": {
    "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
    "artisanId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "artisanName": "Karim Ziani",
    "artisanEmail": "artisan@souklab.dz",
    "status": "PENDING",
    "motivation": "15+ years experience teaching traditional pottery.",
    "adminNote": null,
    "canReapply": true,
    "cooldownUntil": null,
    "createdAt": "2026-09-03T20:22:00"
  }
}
```

---

### 7.2 Attempt Duplicate Request (Expected 409 Conflict)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/artisan/formateur-request`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisanAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "motivation": "Duplicate request while first is still pending."
}
```

#### Response Examples
##### Error Response (`409 Conflict`)
```json
{
  "success": false,
  "code": 409,
  "errorCode": "CONFLICT",
  "message": "You already have a pending Formateur request.",
  "data": null
}
```

---

## 8. Formateur — Admin Actions

> Administrative vetting workflows for Formateur requests, handling approvals, cooldowns, direct grants, revocations, and permanent blocks.

### 8.1 Get Pending Requests (Paginated)
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/admin/formateur-requests?page=0&size=20`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
        "artisanId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
        "artisanName": "Karim Ziani",
        "artisanEmail": "artisan@souklab.dz",
        "status": "PENDING",
        "motivation": "Master craftsman in traditional metalwork.",
        "adminNote": null,
        "canReapply": true,
        "cooldownUntil": null,
        "decidedByAdminId": null,
        "decidedByAdminEmail": null,
        "decidedAt": null,
        "createdAt": "2026-09-03T20:22:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

### 8.2 Approve Formateur Request
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/formateur-requests/{{formateurRequestId}}/approve`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "adminNote": "Impressive portfolio and master certification verified."
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Formateur request approved successfully.",
  "data": {
    "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
    "artisanId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "artisanName": "Karim Ziani",
    "artisanEmail": "artisan@souklab.dz",
    "status": "APPROVED",
    "motivation": "15+ years experience teaching traditional pottery.",
    "adminNote": "Impressive portfolio and master certification verified.",
    "canReapply": true,
    "cooldownUntil": null,
    "decidedByAdminId": "admin-user-id",
    "decidedByAdminEmail": "admin@souklab.dz",
    "decidedAt": "2026-09-03T20:25:00",
    "createdAt": "2026-09-03T20:22:00"
  }
}
```

---

### 8.3 Setup Artisan 2 (Register for Cooldown Test)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/register`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisan2Email}}",
  "password": "Password123!",
  "firstName": "Amina",
  "lastName": "Mansouri",
  "role": "ARTISAN"
}
```

#### Response Examples
##### Success Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Registration successful. Your artisan account has been created and is pending administrator verification.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "user@souklab.dz",
    "firstName": "Karim",
    "lastName": "Ziani",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "accountStatus": "PENDING",
    "emailVerified": false,
    "createdAt": "2026-09-03T20:00:00"
  }
}
```

---

### 8.4 Approve Artisan 2 (as Admin)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/{{artisan2Id}}/approve`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "User approved successfully",
  "data": null
}
```

---

### 8.5 Login Artisan 2
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/login`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisan2Email}}",
  "password": "Password123!"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7f708074-43f0-4cdc-a5f2-160ebd833908",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "roles": [
      "ROLE_ARTISAN"
    ]
  }
}
```

---

### 8.6 Complete Profile Artisan 2
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/complete-profile`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisan2AccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "bio": "Expert in leather goods and traditional embroidery.",
  "city": "Constantine",
  "address": "Casbah No. 12",
  "isTeacher": false
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Profile completed successfully.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "artisan@souklab.dz",
    "bio": "Woodworking artisan specializing in cedar and walnut wood carvings.",
    "city": "Batna",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "accountStatus": "ACTIVE",
    "emailVerified": true
  }
}
```

---

### 8.7 Submit Formateur Request (Artisan 2)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/artisan/formateur-request`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisan2AccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "motivation": "Leathercraft workshops and tooling masterclasses."
}
```

#### Response Examples
##### Success Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Formateur request submitted successfully.",
  "data": {
    "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
    "artisanId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "artisanName": "Karim Ziani",
    "artisanEmail": "artisan@souklab.dz",
    "status": "PENDING",
    "motivation": "15+ years experience teaching traditional pottery.",
    "adminNote": null,
    "canReapply": true,
    "cooldownUntil": null,
    "createdAt": "2026-09-03T20:22:00"
  }
}
```

---

### 8.8 Reject Request with Default 14-Day Cooldown
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/formateur-requests/{{formateurRequest2Id}}/reject`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "adminNote": "Please provide your master artisan certification before reapplying."
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Formateur request rejected successfully.",
  "data": {
    "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
    "artisanId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "artisanName": "Amina Mansouri",
    "artisanEmail": "amina@souklab.dz",
    "status": "REJECTED",
    "motivation": "Leathercraft workshops and tooling masterclasses.",
    "adminNote": "Please provide your master artisan certification before reapplying.",
    "canReapply": true,
    "cooldownUntil": "2026-09-17T20:25:00",
    "decidedByAdminId": "admin-user-id",
    "decidedByAdminEmail": "admin@souklab.dz",
    "decidedAt": "2026-09-03T20:25:00",
    "createdAt": "2026-09-03T20:22:00"
  }
}
```

---

### 8.9 Attempt Resubmit During Cooldown (Expected 403 Forbidden)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/artisan/formateur-request`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisan2AccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "motivation": "Attempting early resubmission during active cooldown."
}
```

#### Response Examples
##### Error Response (`403 Forbidden`)
```json
{
  "success": false,
  "code": 403,
  "errorCode": "FORBIDDEN",
  "message": "You cannot submit a request during the cooldown period. Cooldown expires on: 2026-09-17T20:25:00",
  "data": null
}
```

---

### 8.10 Lift Cooldown (as Admin)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/formateur-requests/{{artisan2Id}}/lift-cooldown`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "canReapply": true,
  "cooldownUntil": null
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Cooldown configuration updated successfully.",
  "data": {
    "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
    "artisanId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "artisanName": "Amina Mansouri",
    "artisanEmail": "amina@souklab.dz",
    "status": "REJECTED",
    "motivation": "Leathercraft workshops and tooling masterclasses.",
    "adminNote": "Please provide your master artisan certification before reapplying.",
    "canReapply": true,
    "cooldownUntil": null,
    "decidedByAdminId": "admin-user-id",
    "decidedByAdminEmail": "admin@souklab.dz",
    "decidedAt": "2026-09-03T20:25:00",
    "createdAt": "2026-09-03T20:22:00"
  }
}
```

---

### 8.11 Resubmit After Cooldown Lifted (Expected Success 201)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/artisan/formateur-request`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisan2AccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "motivation": "Reapplying after cooldown was officially waived by administrator."
}
```

#### Response Examples
##### Error Response (`403 Forbidden`)
```json
{
  "success": false,
  "code": 403,
  "errorCode": "FORBIDDEN",
  "message": "You cannot submit a request during the cooldown period.",
  "timestamp": "2026-09-03T20:25:00"
}
```

---

### 8.12 Setup Artisan 3 (Register for Direct Grant Test)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/register`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisan3Email}}",
  "password": "Password123!",
  "firstName": "Omar",
  "lastName": "Brahimi",
  "role": "ARTISAN"
}
```

#### Response Examples
##### Success Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Registration successful. Your artisan account has been created and is pending administrator verification.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "user@souklab.dz",
    "firstName": "Karim",
    "lastName": "Ziani",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "accountStatus": "PENDING",
    "emailVerified": false,
    "createdAt": "2026-09-03T20:00:00"
  }
}
```

---

### 8.13 Approve Artisan 3 (as Admin)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/{{artisan3Id}}/approve`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "User approved successfully",
  "data": null
}
```

---

### 8.14 Login Artisan 3
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/login`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisan3Email}}",
  "password": "Password123!"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7f708074-43f0-4cdc-a5f2-160ebd833908",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "roles": [
      "ROLE_ARTISAN"
    ]
  }
}
```

---

### 8.15 Complete Profile Artisan 3
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/complete-profile`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisan3AccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "bio": "Woodworking artisan specializing in cedar and walnut wood carvings.",
  "city": "Batna",
  "address": "Aures Workshop Hub",
  "isTeacher": false
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Profile completed successfully.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "artisan@souklab.dz",
    "bio": "Woodworking artisan specializing in cedar and walnut wood carvings.",
    "city": "Batna",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "accountStatus": "ACTIVE",
    "emailVerified": true
  }
}
```

---

### 8.16 Direct Grant Formateur Status (as Admin)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/artisans/{{artisan3Id}}/formateur-grant`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "adminNote": "Directly granted master instructor status by executive board recommendation."
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Formateur status granted successfully.",
  "data": {
    "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
    "status": "APPROVED",
    "adminNote": "Direct ministerial honor grant."
  }
}
```

---

### 8.17 Revoke Formateur Status (as Admin)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/artisans/{{artisan3Id}}/formateur-revoke`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "reason": "Administrative suspension due to workshop guideline breach."
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Formateur status revoked successfully.",
  "data": null
}
```

---

### 8.18 Setup Artisan 4 (Register for Permanent Block Test)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/register`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisan4Email}}",
  "password": "Password123!",
  "firstName": "Samia",
  "lastName": "Benali",
  "role": "ARTISAN"
}
```

#### Response Examples
##### Success Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Registration successful. Your artisan account has been created and is pending administrator verification.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "user@souklab.dz",
    "firstName": "Karim",
    "lastName": "Ziani",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "accountStatus": "PENDING",
    "emailVerified": false,
    "createdAt": "2026-09-03T20:00:00"
  }
}
```

---

### 8.19 Approve Artisan 4 (as Admin)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/{{artisan4Id}}/approve`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "User approved successfully",
  "data": null
}
```

---

### 8.20 Login Artisan 4
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/login`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "{{artisan4Email}}",
  "password": "Password123!"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "7f708074-43f0-4cdc-a5f2-160ebd833908",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "roles": [
      "ROLE_ARTISAN"
    ]
  }
}
```

---

### 8.21 Complete Profile Artisan 4
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/complete-profile`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisan4AccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "bio": "Traditional carpet weaving and natural wool dyeing.",
  "city": "Ghardaia",
  "address": "Ksar Artisans Center",
  "isTeacher": false
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Profile completed successfully.",
  "data": {
    "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "email": "artisan@souklab.dz",
    "bio": "Woodworking artisan specializing in cedar and walnut wood carvings.",
    "city": "Batna",
    "roles": [
      "ROLE_ARTISAN"
    ],
    "accountStatus": "ACTIVE",
    "emailVerified": true
  }
}
```

---

### 8.22 Submit Formateur Request (Artisan 4)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/artisan/formateur-request`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisan4AccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "motivation": "Carpentry masterclasses."
}
```

#### Response Examples
##### Success Response (`201 Created`)
```json
{
  "success": true,
  "code": 201,
  "message": "Formateur request submitted successfully.",
  "data": {
    "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
    "artisanId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "artisanName": "Karim Ziani",
    "artisanEmail": "artisan@souklab.dz",
    "status": "PENDING",
    "motivation": "15+ years experience teaching traditional pottery.",
    "adminNote": null,
    "canReapply": true,
    "cooldownUntil": null,
    "createdAt": "2026-09-03T20:22:00"
  }
}
```

---

### 8.23 Reject with canReapply=false (Permanent Block)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/formateur-requests/{{formateurRequest4Id}}/reject`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "adminNote": "Permanent disqualification due to fraudulent credential submission.",
  "canReapply": false
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Formateur request rejected successfully.",
  "data": {
    "id": "ca118845-92b6-4d57-ad67-a395975d9ced",
    "artisanId": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
    "artisanName": "Samia Benali",
    "artisanEmail": "samia@souklab.dz",
    "status": "REJECTED",
    "motivation": "Leathercraft workshops and tooling masterclasses.",
    "adminNote": "Permanent disqualification due to fraudulent credential submission.",
    "canReapply": false,
    "cooldownUntil": null,
    "decidedByAdminId": "admin-user-id",
    "decidedByAdminEmail": "admin@souklab.dz",
    "decidedAt": "2026-09-03T20:25:00",
    "createdAt": "2026-09-03T20:22:00"
  }
}
```

---

### 8.24 Attempt Resubmit After Permanent Block (Expected 403 Forbidden)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/artisan/formateur-request`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{artisan4AccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "motivation": "Attempting to resubmit after permanent disqualification."
}
```

#### Response Examples
##### Error Response (`403 Forbidden`)
```json
{
  "success": false,
  "code": 403,
  "errorCode": "FORBIDDEN",
  "message": "You are permanently blocked from submitting new Formateur requests.",
  "data": null
}
```

---

## 9. Admin — User Moderation & Discovery

> Comprehensive administrative operations for user discovery, pagination, filtering/search, registration approvals, permanent suspension (ban), temporary suspension (timeout with auto-expiry lapse), effective-status resolution, and manual reinstatement (unban) with dedicated notifications.

---

### 9.1 List All Users (Default Pagination & Effective Status)
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/admin/users`
- **Access**: `ROLE_ADMIN` only (`403 Forbidden` for other roles)

#### Query Parameters
| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `search` | String | *Optional* | Substring query matched against user email and name |
| `page` | Integer | `0` | Zero-indexed page number |
| `size` | Integer | `20` | Page size limit |
| `sort` | String | `createdAt,desc` | Sort field and direction |

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Request format |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Effective Status Resolution
If a user's database status is `SUSPENDED` but their `bannedUntil` timestamp is in the past (an expired timeout), the mapping layer automatically computes their effective status as `ACTIVE` and nulls out `bannedUntil` and `banReason` in the response DTO. This ensures administrators immediately see the user's live operational state without waiting for the user to trigger a login write.

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Operation completed successfully",
  "data": {
    "content": [
      {
        "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
        "email": "user@souklab.dz",
        "name": "Yacine Brahimi",
        "phone": "+213 555 12 34 56",
        "avatarUrl": null,
        "roles": [
          "ROLE_CLIENT"
        ],
        "primaryRole": "ROLE_CLIENT",
        "status": "ACTIVE",
        "emailVerified": true,
        "bannedUntil": null,
        "banReason": null,
        "lastLoginAt": "2026-09-11T16:00:00",
        "createdAt": "2026-09-03T20:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 25,
    "totalPages": 2,
    "last": false
  }
}
```

---

### 9.2 List Users (Custom Pagination & Sorting)
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/admin/users?page=0&size=5&sort=email,asc`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Operation completed successfully",
  "data": {
    "content": [
      {
        "id": "11aa22bb-33cc-44dd-55ee-66ff77aa88bb",
        "email": "a.artisan@souklab.dz",
        "name": "Ahmed Artisan",
        "status": "ACTIVE",
        "roles": ["ROLE_ARTISAN"]
      }
    ],
    "pageNumber": 0,
    "pageSize": 5,
    "totalElements": 25,
    "totalPages": 5,
    "last": false
  }
}
```

---

### 9.3 Search Users by Email Substring
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/admin/users?search={{userEmailPrefix}}`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Operation completed successfully",
  "data": {
    "content": [
      {
        "id": "62b9a719-a2e7-4356-8d06-df0eef43171a",
        "email": "b6.timeout.1789147285409@souklab.dz",
        "name": "Timeout User",
        "status": "ACTIVE",
        "bannedUntil": null,
        "banReason": null,
        "roles": ["ROLE_CLIENT"]
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

### 9.4 Search Users No Match (Expected Empty Page)
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/admin/users?search=NonExistentQueryZzz999X`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Operation completed successfully",
  "data": {
    "content": [],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 0,
    "totalPages": 0,
    "last": true
  }
}
```

---

### 9.5 List Pending Users
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/admin/users/pending?page=0&size=20`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Operation completed successfully",
  "data": {
    "content": [
      {
        "id": "324b7834-edea-4a16-a5d6-5dae62779ebb",
        "email": "b6.artisan.a.1789147283940@souklab.dz",
        "name": "Karim Ziani",
        "roles": [
          "ROLE_ARTISAN"
        ],
        "primaryRole": "ROLE_ARTISAN",
        "status": "PENDING",
        "emailVerified": true,
        "createdAt": "2026-09-11T17:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

### 9.6 Approve User Registration (Single)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/:id/approve`
- **Access**: `ROLE_ADMIN` only

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "User approved successfully",
  "data": null
}
```

> **Side Effects**: Sets `accountStatus = ACTIVE` and sends an `ACCOUNT_VALIDATED` notification to the user.

---

### 9.7 Approve Users Bulk
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/approve-bulk`
- **Access**: `ROLE_ADMIN` only

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
[
  "324b7834-edea-4a16-a5d6-5dae62779ebb",
  "a45235c6-cbc2-4061-a68b-95b09eef9863"
]
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "Users approved successfully",
  "data": null
}
```

---

### 9.8 Permanently Ban User
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/:id/ban`
- **Access**: `ROLE_ADMIN` only

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "reason": "Repeated violations of terms of service"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "User banned successfully",
  "data": null
}
```

> **Representation**: Sets `status = SUSPENDED`, `bannedUntil = null` (representing indefinite/permanent ban), `banReason = reason`, revokes active refresh tokens, and sends an `ACCOUNT_SUSPENDED` notification.

---

### 9.9 Timeout User (Temporary Suspension)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/:id/timeout`
- **Access**: `ROLE_ADMIN` only

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body (`application/json`)
```json
{
  "minutes": 60,
  "reason": "Temporary cooldown for administrative investigation"
}
```

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "User timed out successfully",
  "data": null
}
```

> **Representation**: Sets `status = SUSPENDED`, `bannedUntil = now + minutes`, `banReason = reason`, revokes active refresh tokens, and sends an `ACCOUNT_SUSPENDED` notification.

---

### 9.10 Login Lockout on Suspended User (Expected 403 Forbidden)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/auth/login`

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Header requirement |

#### Request Body (`application/json`)
```json
{
  "email": "suspended.user@souklab.dz",
  "password": "Password123!"
}
```

#### Response Examples
##### Error Response (`403 Forbidden`)
```json
{
  "success": false,
  "code": 403,
  "errorCode": "FORBIDDEN",
  "message": "Account suspended: Repeated violations of terms of service",
  "data": null
}
```

---

### 9.11 Unban User (Reinstatement)
- **Method**: `POST`
- **Endpoint**: `{{baseUrl}}/admin/users/:id/unban`
- **Access**: `ROLE_ADMIN` only

#### URL Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `id` | String | Unique user UUID to reinstate |

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer {{adminAccessToken}}` | Bearer authentication token |

#### Request Body
*None (No request payload)*

#### Response Examples
##### Success Response (`200 OK`)
```json
{
  "success": true,
  "code": 200,
  "message": "User unbanned successfully",
  "data": null
}
```

> **Side Effects**: Sets `status = ACTIVE`, clears `bannedUntil = null` and `banReason = null`, logs an `UNBAN_USER` audit record, and delivers an `ACCOUNT_REINSTATED` notification to the user.

##### Error Response: User Not Suspended (`409 Conflict`)
```json
{
  "success": false,
  "code": 409,
  "errorCode": "CONFLICT",
  "message": "User is not suspended. Current status: ACTIVE",
  "data": null
}
```

##### Error Response: Non-Existent User (`404 Not Found`)
```json
{
  "success": false,
  "code": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "User not found with id: 00000000-0000-0000-0000-000000000000",
  "data": null
}
```

##### Error Response: Non-Admin Access (`403 Forbidden`)
```json
{
  "success": false,
  "code": 403,
  "errorCode": "FORBIDDEN",
  "message": "You do not have permission to perform this action.",
  "data": null
}
```

---

### 9.12 Role Boundary Enforcement
All admin user moderation routes are guarded at the controller level with `@PreAuthorize("hasRole('ADMIN')")`. Any unauthenticated request receives `401 Unauthorized`, and any non-admin request (e.g., `ROLE_CLIENT` or `ROLE_ARTISAN`) receives `403 Forbidden`:

- `GET /api/v1/admin/users` ➔ `403 Forbidden`
- `GET /api/v1/admin/users?search=test` ➔ `403 Forbidden`
- `GET /api/v1/admin/users/pending` ➔ `403 Forbidden`
- `POST /api/v1/admin/users/:id/approve` ➔ `403 Forbidden`
- `POST /api/v1/admin/users/approve-bulk` ➔ `403 Forbidden`
- `POST /api/v1/admin/users/:id/ban` ➔ `403 Forbidden`
- `POST /api/v1/admin/users/:id/timeout` ➔ `403 Forbidden`
- `POST /api/v1/admin/users/:id/unban` ➔ `403 Forbidden`

---

## 10. File Storage

> File serving endpoints for retrieving stored assets by storage key with dedicated rate limiting and immutable caching headers.

### 10.1 Get File (Serve by Key)
- **Method**: `GET`
- **Endpoint**: `{{baseUrl}}/files/:key`

#### URL Parameters
| Parameter | Type | Description | Example |
| :--- | :--- | :--- | :--- |
| `key` | String | Unique storage identifier / key of the stored file (placeholder until upload orchestration in Phase D) | `sample-uuid.jpg` |

#### Headers
| Header | Value | Description |
| :--- | :--- | :--- |
| `Authorization` | `Bearer {{clientAccessToken}}` | Bearer authentication token (authenticated-only baseline) |

#### Response Examples
##### Success Response (`200 OK`)
- **Headers**:
  - `Content-Type`: `image/jpeg`
  - `Content-Length`: `1048576`
  - `Content-Disposition`: `inline; filename="avatar_original.jpg"; filename*=UTF-8''avatar_original.jpg`
  - `Cache-Control`: `private, max-age=31536000, immutable`
- **Body**:
```text
<binary file stream content>
```

##### Error: Unauthenticated (`401 Unauthorized`)
```json
{
  "success": false,
  "code": 401,
  "message": "Full authentication is required to access this resource",
  "data": null
}
```

##### Error: Not Found (`404 Not Found`)
```json
{
  "success": false,
  "code": 404,
  "errorCode": "FILE_NOT_FOUND",
  "message": "File not found for key: nonexistent-key.jpg",
  "data": null
}
```

##### Error: Rate Limited (`429 Too Many Requests`)
```json
{
  "success": false,
  "code": 429,
  "message": "Too many requests. Please try again later.",
  "data": null
}
```

---
