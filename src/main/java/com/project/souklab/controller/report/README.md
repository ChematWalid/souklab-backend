# Content Moderation & Reports Controller (`com.project.souklab.controller.report`)

REST controller for submitting user complaints/abuse reports against platform content and administrative resolution.

---

## Workflow Overview

1. **User Submission**: Authenticated users flag inappropriate content (`POST /api/v1/reports`).
   - Supported targets: `USER` (artisan or client profile), `POST` (community feed post), `REVIEW` (artisan review).
   - Reports enter the queue in `OPEN` status.
2. **Admin Review**: Moderators review open reports (`GET /api/v1/admin/reports`).
3. **Resolution**: Moderators take action (`POST /api/v1/admin/reports/{id}/resolve`):
   - `DISMISS`: Rejects the report as unfounded.
   - `HIDE`: Hides the flagged content from public visibility.
   - `REMOVE`: Soft-deletes or removes the reported content entirely.

---

## Endpoints

| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/reports` | Authenticated | Submit report | Creates a content report against a user, feed post, or review. |
| `GET` | `/api/v1/admin/reports` | Admin (`canModerateReports`) | List moderation queue | Retrieves paginated reports with optional `status` and `targetType` filters. |
| `POST` | `/api/v1/admin/reports/{id}/resolve` | Admin (`canModerateReports`) | Resolve report | Executes an administrative moderation action (`DISMISS`, `HIDE`, `REMOVE`) with notes. |

---

## Payloads & DTO Reference

### Submit Report (`POST /api/v1/reports`)

```json
{
  "targetType": "POST",
  "targetId": "c1f7a8b2-4d3e-4b7a-9a8c-123456789abc",
  "reason": "Spam or irrelevant advertising",
  "details": "This post is promoting non-handcrafted industrial goods."
}
```

### Report Response (`ContentReportResponseDTO`)

```json
{
  "success": true,
  "message": "Report submitted successfully.",
  "data": {
    "id": "rep-7f8e9d-1234",
    "reporterId": "u-456",
    "targetType": "POST",
    "targetId": "c1f7a8b2-4d3e-4b7a-9a8c-123456789abc",
    "reason": "Spam or irrelevant advertising",
    "details": "This post is promoting non-handcrafted industrial goods.",
    "status": "OPEN",
    "resolutionAction": null,
    "resolutionNote": null,
    "createdAt": "2026-09-23T20:15:00"
  }
}
```

### Resolve Report (`POST /api/v1/admin/reports/{id}/resolve`)

```json
{
  "action": "HIDE",
  "note": "Post violates commercial advertising policies. Hidden from public feed."
}
```

---

## TypeScript Contracts

```typescript
export type ReportTargetType = 'USER' | 'POST' | 'REVIEW';
export type ReportStatus = 'OPEN' | 'DISMISSED' | 'RESOLVED';
export type ReportResolutionAction = 'DISMISS' | 'HIDE' | 'REMOVE';

export interface ContentReportRequest {
  targetType: ReportTargetType;
  targetId: string;
  reason: string;
  details?: string;
}

export interface ReportResolutionRequest {
  action: ReportResolutionAction;
  note: string;
}

export interface ContentReportResponse {
  id: string;
  reporterId: string;
  targetType: ReportTargetType;
  targetId: string;
  reason: string;
  details?: string;
  status: ReportStatus;
  resolutionAction?: ReportResolutionAction | null;
  resolutionNote?: string | null;
  createdAt: string;
}
```
