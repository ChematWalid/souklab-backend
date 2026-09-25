# Community Feed Controller Package (`com.project.souklab.controller.feed`)

REST controllers for community craft posts, updates, announcements, image attachments, and administrative moderation.

---

## Architecture & Moderation Workflow

1. **Draft or submission**: Authenticated artisans create a post (`POST /api/v1/feed`) with `isDraft=true` for `DRAFT`, otherwise it starts in `PENDING`.
2. **Media**: Post authors can attach images (`POST /api/v1/feed/{id}/media`, multipart `file`).
3. **Moderation Queue**: Administrators with feed moderation authority review pending posts (`GET /api/v1/admin/feed/pending`).
4. **Moderation**: Administrators publish, reject, hide, or remove posts. Rejected authors can revise and resubmit with `POST /api/v1/feed/{id}/submit`.
5. **Public Consumption**: Frontends query public posts (`GET /api/v1/feed`), which only returns `PUBLISHED` posts.

---

## Endpoints

### 1. Public Feed & Post Authoring (`FeedPostController`)

Base Path: `/api/v1/feed`

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/feed` | Public | List public feed posts | Retrieves a `PaginatedResponse` of published posts. Supports `type`, `authorId`, normalized `tag`, `q`, and `sort=latest|popular`. |
| `GET` | `/api/v1/feed/{id}` | Public/authenticated | Get feed post | Public callers receive published posts; authenticated owners/moderators may retrieve their managed states. |
| `GET` | `/api/v1/feed/me` | Authenticated | List own posts | Lists drafts, pending, rejected, published, hidden, and removed posts as allowed. |
| `GET` | `/api/v1/feed/following` | Authenticated client | Following feed | Lists posts from artisans in the client's favorites; non-clients receive an empty page. |
| `POST` | `/api/v1/feed` | Authenticated | Create feed post | Creates a draft or submits a post for moderation (`PENDING`). |
| `POST` | `/api/v1/feed/{id}/submit` | Authenticated owner | Resubmit post | Submits a `DRAFT` or `REJECTED` post for moderation. |
| `PUT` | `/api/v1/feed/{id}` | Authenticated | Update feed post | Updates content, normalized tags, or title of an owned post. Non-draft edits return to `PENDING`. |
| `DELETE` | `/api/v1/feed/{id}` | Authenticated | Delete feed post | Soft-deletes or removes an owned post. |
| `POST/DELETE` | `/api/v1/feed/{id}/likes` | Authenticated | Like/unlike post | One unique like per user, with conflict-safe insertion and atomic counters. |
| `GET` | `/api/v1/feed/{id}/likes` | Public | Read like status | Returns the current caller's like state and the post like count. |
| `POST/DELETE` | `/api/v1/feed/{id}/bookmarks` | Authenticated | Bookmark/unbookmark post | One unique bookmark per user. |
| `GET` | `/api/v1/feed/saved` | Authenticated | Saved posts | Lists the current user's bookmarked posts. |
| `GET/POST` | `/api/v1/feed/{id}/comments` | Public/authenticated | Comments | Lists root comments publicly or creates an authenticated root comment. |
| `GET` | `/api/v1/feed/comments/{commentId}` | Public | Get comment | Retrieves one visible comment or reply. |
| `GET/POST` | `/api/v1/feed/comments/{commentId}/replies` | Public/authenticated | Replies | Lists or creates one-level replies. |
| `PUT` | `/api/v1/feed/comments/{commentId}` | Authenticated | Update comment | Updates a comment or reply owned by the authenticated author. |
| `POST/DELETE` | `/api/v1/feed/comments/{commentId}/likes` | Authenticated | Like/unlike comment | One unique comment like per user. |
| `GET` | `/api/v1/feed/comments/{commentId}/likes` | Public | Read comment like status | Returns the current caller's comment-like state and count. |
| `DELETE` | `/api/v1/feed/comments/{commentId}` | Authenticated | Remove comment | Allows the comment author, post author, or feed moderator to soft-delete. |
| `POST` | `/api/v1/feed/{id}/share` | Public | Share post | Atomically increments the share count and returns a relative share path. |
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
| `POST` | `/api/v1/admin/feed/{id}/reject` | Admin / Moderator | Reject post | Rejects a post with a moderation note; author may revise and resubmit. |
| `POST` | `/api/v1/admin/feed/{id}/hide` | Admin / Moderator | Hide published post | Hides a published post from the public feed. |
| `POST` | `/api/v1/admin/feed/{id}/remove` | Admin / Moderator | Compatibility removal | Existing compatibility alias for administrative removal. |
| `DELETE` | `/api/v1/admin/feed/{id}` | Admin / Moderator | Delete post | Administratively deletes a post. |

---

## Payloads & DTO Reference

### Create Post (`POST /api/v1/feed`)

```json
{
  "type": "ACTUALITE",
  "title": "Nouveau tour de poterie disponible à l'atelier",
  "body": "Nous venons de recevoir un nouveau tour de poterie traditionnel...",
  "formationId": null,
  "isDraft": false,
  "tags": ["poterie", "atelier"]
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
    "tags": ["poterie", "atelier"],
    "likeCount": 0,
    "commentCount": 0,
    "bookmarkCount": 0,
    "shareCount": 0,
    "likedByCurrentUser": false,
    "bookmarkedByCurrentUser": false,
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
export type FeedPostStatus = 'DRAFT' | 'PENDING' | 'PUBLISHED' | 'HIDDEN' | 'REJECTED' | 'REMOVED';

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
  isDraft?: boolean;
  tags?: string[];
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
  tags: string[];
  likeCount: number;
  commentCount: number;
  bookmarkCount: number;
  shareCount: number;
  likedByCurrentUser: boolean;
  bookmarkedByCurrentUser: boolean;
}
```
