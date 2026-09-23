# Directory Controller Package (`com.project.souklab.controller.directory`)

REST controller for the artisan marketplace directory and faceted search engine. Backed by Elasticsearch / Hibernate Search with automatic relational database fallback.

---

## Endpoints

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/public/directory` | Authenticated | Search artisan directory | Multi-facet search across artisans with contact information privacy gating. |

---

## Authentication & Privacy Gating

> [!IMPORTANT]
> **Authentication is required** (`Authorization: Bearer <accessToken>`). Anonymous callers receive `403 Forbidden`.

The directory applies dynamic privacy masking to artisan identities:
- **Non-Premium Viewers** (Free Clients or unverified callers): The `artisanName` is masked to `"Artisan #" + id.substring(id.length() - 5).toUpperCase()` (e.g., `"Artisan #DC611"`).
- **Premium Viewers & Administrators**: The `artisanName` displays the artisan's full real name (e.g., `"Mouloud Mammeri"`).

---

## Query Parameters (`DirectorySearchFilterDTO`)

All parameters are optional HTTP query parameters passed via URL query string:

| Parameter | Type | Validation / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `keyword` | String | Max 120 chars | Full-text query matching artisan name, bio, craft, and city. |
| `categoryId` | String | Max 36 chars (UUID) | Filter by main craft job category ID. |
| `subCategoryId`| String | Max 36 chars (UUID) | Filter by specialized job subcategory ID. |
| `wilayaId` | String | Max 36 chars | Filter by Algerian Wilaya region ID. |
| `minRating` | Double | `0.0` to `5.0` | Minimum customer review rating threshold. |
| `verifiedOnly` | Boolean| Default: `false` | When `true`, filters to verified artisans only. |
| `materials` | List<String> | Array of UUIDs | Filter artisans utilizing specific raw materials. |
| `techniques` | List<String> | Array of UUIDs | Filter artisans mastering specific craftsmanship techniques. |
| `epoques` | List<String> | Array of UUIDs | Filter artisans inspired by specific historical epochs. |
| `page` | Integer| Min: `0`, Default: `0` | Zero-indexed page number. |
| `size` | Integer| `1` to `100`, Default: `20` | Items per page. |
| `sortBy` | String | `rating`, `createdAt`, `viewsCount` | Field to sort results by. |
| `sortDir` | String | `asc`, `desc` (Default: `desc`) | Sort direction. |

---

## Response Structure (`ApiResponse<PaginatedResponse<ArtisanDirectoryCardDTO>>`)

```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "e15eabe0-16cc-42e9-aa91-755c96edc611",
        "artisanName": "Artisan #DC611",
        "avatarUrl": "https://storage.souklab.dz/avatars/user-full.webp",
        "coverImageUrl": null,
        "bioSnippet": "Artisan potier traditionnel kabyle avec 15 ans d'expérience...",
        "city": "Tizi Ouzou",
        "wilayaName": "Tizi Ouzou",
        "wilayaCode": "15",
        "regionSlug": "tizi-ouzou",
        "categoryName": "Poterie & Céramique",
        "categorySlug": "poterie-ceramique",
        "subCategoryName": "Poterie traditionnelle",
        "subCategorySlug": "poterie-traditionnelle",
        "rating": 4.85,
        "reviewsCount": 24,
        "viewsCount": 350,
        "verified": true,
        "premium": true,
        "teacher": true,
        "primaryMaterials": ["Argile rouge", "Engobe"],
        "primaryTechniques": ["Modelage manuel"],
        "createdAt": "2026-08-15T08:30:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 48,
    "totalPages": 3,
    "last": false
  },
  "errors": null,
  "traceId": "0321f6ee-9119-4d0c-ade4-2fd7ebc6c4ff"
}
```

---

## TypeScript Interfaces

```typescript
export interface ArtisanDirectoryCard {
  id: string;
  artisanName: string; // "Artisan #XXXXX" for free users; real name for premium users
  avatarUrl: string | null;
  coverImageUrl: string | null;
  bioSnippet: string | null;
  city: string | null;
  wilayaName: string | null;
  wilayaCode: string | null;
  regionSlug: string | null;
  categoryName: string | null;
  categorySlug: string | null;
  subCategoryName: string | null;
  subCategorySlug: string | null;
  rating: number;
  reviewsCount: number;
  viewsCount: number;
  verified: boolean;
  premium: boolean;
  teacher: boolean;
  primaryMaterials: string[];
  primaryTechniques: string[];
  createdAt: string;
}

export interface PaginatedDirectoryResponse {
  content: ArtisanDirectoryCard[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
```
