# Formation Controller Package (`com.project.souklab.controller.formation`)

REST controllers managing masterclass authoring, course material uploads, peer workshop discovery, enrollment reservations, and administrative curriculum moderation.

> **Access Boundary**: Peer discovery, enrollment, and authoring routes are strictly reserved for artisans (`ROLE_ARTISAN`). `ROLE_CLIENT` callers are rejected with `403 Forbidden` across all routes.

---

## Endpoints

### Artisan Masterclass Authoring (`ArtisanFormationController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/artisan/formations` | `ROLE_ARTISAN` | Creates a new formation draft (requires accredited instructor status `isTeacher = true`). |
| `GET` | `/api/v1/artisan/formations/me` | `ROLE_ARTISAN` | Retrieves paginated list of formations authored by the authenticated artisan. |
| `GET` | `/api/v1/artisan/formations/{id}` | `ROLE_ARTISAN` | Retrieves complete authored formation details including review history and course materials. |
| `PUT` | `/api/v1/artisan/formations/{id}` | `ROLE_ARTISAN` | Updates curriculum and schedule (core changes on approved/published reset to `PENDING_REVIEW`). |
| `POST` | `/api/v1/artisan/formations/{id}/thumbnail` | `ROLE_ARTISAN` | Uploads showcase thumbnail image (multipart, max 10MB, scanned when enabled). |
| `POST` | `/api/v1/artisan/formations/{id}/files` | `ROLE_ARTISAN` | Uploads course syllabus or resource document attachment (max 10 attachments, max 25MB, scanned when enabled). |
| `DELETE` | `/api/v1/artisan/formations/{id}/files/{fileId}` | `ROLE_ARTISAN` | Soft-deletes a course material attachment. |
| `POST` | `/api/v1/artisan/formations/{id}/submit` | `ROLE_ARTISAN` | Submits draft or rejected formation for administrative moderation (`PENDING_REVIEW`). |
| `DELETE` | `/api/v1/artisan/formations/{id}` | `ROLE_ARTISAN` | Soft-deletes an authored formation. |

### Peer Discovery, Enrollment & Downloads (`ArtisanFormationEnrollmentController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/artisan/formations/catalog` | `ROLE_ARTISAN` | Browses published peer masterclass catalog (paginated, sorted by `scheduledAt ASC`). |
| `GET` | `/api/v1/artisan/formations/catalog/{id}` | `ROLE_ARTISAN` | Retrieves detailed public view of a published masterclass, capacity, and syllabus files. |
| `POST` | `/api/v1/artisan/formations/{id}/enroll` | `ROLE_ARTISAN` | Enrolls caller in published workshop (blocks self-enrollment, enforces max capacity). |
| `POST` | `/api/v1/artisan/formations/{id}/cancel` | `ROLE_ARTISAN` | Cancels confirmed enrollment reservation (enforces configured cutoff deadline before start). |
| `GET` | `/api/v1/artisan/formations/my-enrollments` | `ROLE_ARTISAN` | Retrieves paginated enrollment history and registered workshops for authenticated artisan. |
| `GET` | `/api/v1/artisan/formations/{id}/files/{fileId}/download` | `ROLE_ARTISAN` | Streams protected course attachment (restricted to author and confirmed participants). |

### Administrative Moderation (`AdminFormationController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/formations/pending` | `ROLE_ADMIN` | Retrieves paginated queue of formations awaiting moderation review. |
| `POST` | `/api/v1/admin/formations/{id}/review` | `ROLE_ADMIN` | Submits review decision (`APPROVED` or `REJECTED`) with moderation comment. |
| `POST` | `/api/v1/admin/formations/{id}/publish` | `ROLE_ADMIN` | Publishes an approved formation to the public catalog. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`ArtisanFormationController`](ArtisanFormationController.java) | Handles authenticated artisan masterclass authoring, media uploads, review submissions, and deletion. |
| [`ArtisanFormationEnrollmentController`](ArtisanFormationEnrollmentController.java) | Manages peer catalog browsing, workshop reservations, cancellations, and protected file downloads. |
| [`AdminFormationController`](AdminFormationController.java) | Handles administrative moderation queues, review verdicts, and catalog publication. |
