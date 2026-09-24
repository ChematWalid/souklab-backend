# Client Favorite Controllers (`com.project.souklab.controller.favorite`)

REST controllers for authenticated client favorite operations on the Souklab marketplace platform.

---

## Endpoints

| Method | Endpoint | Access | HTTP Status | Summary | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/client/favorites/artisans/{artisanId}` | `permission:client:favorites` | `201 Created` | Favorite an artisan | Adds an artisan to the authenticated client favorites list. |
| `GET` | `/api/v1/client/favorites/artisans` | `permission:client:favorites` | `200 OK` | List favorite artisans | Returns a paginated list of visible favorite artisans for the client. |
| `GET` | `/api/v1/client/favorites/artisans/{artisanId}/status` | `permission:client:favorites` | `200 OK` | Check favorite status | Checks whether a specific artisan is bookmarked by the client. |
| `DELETE` | `/api/v1/client/favorites/artisans/{artisanId}` | `permission:client:favorites` | `200 OK` | Unfavorite an artisan | Removes an artisan from favorites (`data: null`). |

---

## Authorization & Security

All four endpoints enforce method security via:
```java
@PreAuthorize("@accessControl.canManageFavorites(authentication)")
```

### Access Control Rules
1. **Permission Check:** The caller must hold the granted authority `permission:client:favorites` (`Permission.Client.FAVORITES`).
2. **Client Profile Check:** In addition to holding the permission, the service layer requires the authenticated user to possess an active `Client` profile record in the database. Principals without a client profile (e.g. Administrators) receive `403 Forbidden` (`Only registered clients can manage favorites.`).
3. **Anonymous Requests:** Requests lacking bearer tokens receive `401 Unauthorized` with standard `ApiResponse` error envelopes.

---

## Response Envelope & DTO Shapes

All endpoints return standard Souklab `ApiResponse<T>` envelopes with integer `code` stamped from the HTTP status.

### 1. Add Favorite (`POST /{artisanId}`) -> `201 Created`
```json
{
  "success": true,
  "code": 201,
  "message": "Artisan added to favorites successfully",
  "data": {
    "favoriteId": "fav-uuid-123",
    "artisanId": "artisan-uuid-456",
    "favoritedAt": "2026-09-24T10:15:00"
  },
  "errors": null,
  "traceId": "trace-uuid"
}
```

### 2. List Favorites (`GET /`) -> `200 OK`
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "favoritedAt": "2026-09-24T10:15:00",
        "artisan": {
          "id": "artisan-uuid-456",
          "artisanName": "Karim Benali",
          "avatarUrl": "https://storage.souklab.dz/avatars/user.webp",
          "bioSnippet": "Artisan céramiste traditionnel...",
          "city": "Algiers",
          "rating": 4.9,
          "reviewsCount": 18,
          "verified": true,
          "premium": true
        }
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "errors": null,
  "traceId": "trace-uuid"
}
```

### 3. Check Status (`GET /{artisanId}/status`) -> `200 OK`
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "favorited": true
  },
  "errors": null,
  "traceId": "trace-uuid"
}
```

### 4. Remove Favorite (`DELETE /{artisanId}`) -> `200 OK`
Per API conventions, deletions return `200 OK` with `data: null`:
```json
{
  "success": true,
  "code": 200,
  "message": "Artisan removed from favorites successfully",
  "data": null,
  "errors": null,
  "traceId": "trace-uuid"
}
```

---

## Pagination and Sorting

- **Default Pageable:** `@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)`
- **Global Page Size Clamping:** Capped at `100` via Spring Data Web (`spring.data.web.pageable.max-page-size=100`). Requests specifying `?size=99999` are automatically clamped to `100`.
- **Sorting Fields:** Sorting operates on fields of the underlying `ClientFavoriteArtisan` entity (e.g. `createdAt`).
