# Community Feed Controller Package (`com.project.souklab.controller.feed`)

REST controllers for community craft posts, updates, announcements, image attachments, and administrative moderation.

---

## Architecture & Moderation Workflow

1. **Submission**: Authenticated artisans/users create a post (`POST /api/v1/feed`). The post starts in `PENDING` status.
2. **Media**: Post authors can attach images (`POST /api/v1/feed/{id}/media`, multipart `file`).
3. **Moderation Queue**: Administrators with feed moderation authority review pending posts (`GET /api/v1/admin/feed/pending`).
4. **Publish / Hide**: Administrators publish (`POST /api/v1/admin/feed/{id}/publish`) or hide (`POST /api/v1/admin/feed/{id}/hide`) posts.
5. **Public Consumption**: Frontends query public posts (`GET /api/v1/feed`), which only returns `PUBLISHED` posts.

---

## Endpoints

### 1. Public Feed & Post Authoring (`FeedPostController`)

Base Path: `/api/v1/feed`

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/feed` | Public | List public feed posts | Retrieves paginated published feed posts. Supports filtering by `type` (`ACTUALITE`, `FORMATION`, `ANNONCE`). |
| `GET` | `/api/v1/feed/{id}` | Public | Get published feed post | Retrieves a single published feed post by ID. |
| `POST` | `/api/v1/feed` | Authenticated | Submit feed post | Creates a new post submitted for moderation (`PENDING` status). |
| `PUT` | `/api/v1/feed/{id}` | Authenticated | Update feed post | Updates content, craft tag, or title of an owned post. Resets status to `PENDING`. |
| `DELETE` | `/api/v1/feed/{id}` | Authenticated | Delete feed post | Soft-deletes or removes an owned post. |
| `POST` | `/api/v1/feed/{id}/media` | Authenticated | Upload post media | Uploads an image attachment (`multipart/form-data`, param `file`). |
| `DELETE` | `/api/v1/feed/{id}/media/{mediaId}` | Authenticated | Delete post media | Deletes a specific media attachment from an owned post. |

---

### 2. Administrator Moderation (`AdminFeedController`)

Base Path: `/api/v1/admin/feed`  
Access: Requires permission `@accessControl.canModerateFeed(authentication)`

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/feed/pending` | Admin / Moderator | List pending posts | Lists posts awaiting moderation review. |
| `POST` | `/api/v1/admin/feed/{id}/publish` | Admin / Moderator | Publish pending post | Approves and publishes post to public feed. |
| `POST` | `/api/v1/admin/feed/{id}/hide` | Admin / Moderator | Hide published post | Hides a published post from the public feed. |
| `DELETE` | `/api/v1/admin/feed/{id}` | Admin / Moderator | Delete post | Administratively deletes a post. |

---

## Payloads & DTO Reference

### Create Post (`POST /api/v1/feed`)

```json
{
  "type": "ACTUALITE",
  "title": "Nouveau tour de poterie disponible à l'atelier",
  "body": "Nous venons de recevoir un nouveau tour de poterie traditionnel...",
  "formationId": null
}
```

### Feed Post Response (`FeedPostResponseDTO`)

```json
{
  "success": true,
  "message": "Feed post submitted for moderation.",
  "data": {
    "id": "c1f7a8b2-4d3e-4b7a-9a8c-123456789abc",
    "authorId": "a9876543-210b-4b7a-8f9c-0123456789de",
    "authorName": "Amina Benali",
    "type": "ACTUALITE",
    "title": "Nouveau tour de poterie disponible à l'atelier",
    "body": "Nous venons de recevoir un nouveau tour de poterie traditionnel...",
    "status": "PENDING",
    "formationId": null,
    "publishedAt": null,
    "moderationNote": null,
    "media": [
      {
        "id": "med-123",
        "url": "https://cdn.souklab.dz/feed/media-123.jpg",
        "contentType": "image/jpeg",
        "displayOrder": 0
      }
    ]
  }
}
```

---

## TypeScript Contracts

```typescript
export type FeedPostType = 'ACTUALITE' | 'FORMATION' | 'ANNONCE';
export type FeedPostStatus = 'DRAFT' | 'PENDING' | 'PUBLISHED' | 'HIDDEN' | 'REJECTED';

export interface FeedPostMedia {
  id: string;
  url: string;
  contentType: string;
  displayOrder: number;
}

export interface FeedPostCreateRequest {
  type: FeedPostType;
  title: string;
  body: string;
  formationId?: string | null;
}

export interface FeedPostResponse {
  id: string;
  authorId: string;
  authorName: string;
  type: FeedPostType;
  title: string;
  body: string;
  status: FeedPostStatus;
  formationId?: string | null;
  publishedAt?: string | null;
  moderationNote?: string | null;
  media: FeedPostMedia[];
}
```
