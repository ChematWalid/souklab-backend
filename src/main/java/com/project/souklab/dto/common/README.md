# Common DTO Package (`com.project.souklab.dto.common`)

Standardized response envelope schemas used by all REST endpoints.

---

## Response Envelope Schema

### Standard Response (`ApiResponse<T>`)
```json
{
  "success": true,
  "code": 200,
  "message": "Operation successful",
  "data": { ... }
}
```

### Paginated Response (`ApiResponse<PaginatedResponse<T>>`)
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 42,
    "totalPages": 3,
    "last": false
  }
}
```

---

## Classes Reference

| Class | Description |
| :--- | :--- |
| [`ApiResponse`](ApiResponse.java) | Generic wrapper providing boolean `success`, integer `code`, optional `errorCode`, descriptive `message`, and typed `data`. Includes static builder factories (`success()`, `error()`). |
| [`PaginatedResponse`](PaginatedResponse.java) | Generic container wrapping Spring Data `Page<T>`, exposing zero-based `pageNumber`, `pageSize`, `totalElements`, `totalPages`, and boolean `last`. |
| [`PatchField`](PatchField.java) | Generic patch value | Distinguishes an omitted JSON field from an explicit `null` clear operation. |
| [`PatchFieldDeserializer`](PatchFieldDeserializer.java) | Jackson deserializer | Preserves `PatchField` omission/null semantics during profile updates. |
