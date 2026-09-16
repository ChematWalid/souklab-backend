# Formation Controller Package (`com.project.souklab.controller.formation`)

REST controllers managing masterclass authoring, course material uploads, peer workshop discovery, enrollment reservations, and administrative curriculum moderation.

> **Access Boundary**: Artisan formation routes require `permission:artisan:formations`; administrative moderation routes require `permission:admin:formations`.

---

## Endpoints

### Artisan Masterclass Authoring (`ArtisanFormationController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/artisan/formations` | `permission:artisan:formations` | Creates a new formation draft (requires accredited instructor status `isTeacher = true`). |
| `GET` | `/api/v1/artisan/formations/me` | `permission:artisan:formations` | Retrieves paginated list of formations authored by the authenticated artisan. |
| `GET` | `/api/v1/artisan/formations/{id}` | `permission:artisan:formations` | Retrieves complete authored formation details including review history and course materials. |
| `PUT` | `/api/v1/artisan/formations/{id}` | `permission:artisan:formations` | Updates curriculum and schedule (core changes on approved/published reset to `PENDING_REVIEW`). |
| `POST` | `/api/v1/artisan/formations/{id}/thumbnail` | `permission:artisan:formations` | Uploads showcase thumbnail image (multipart, max 10MB, scanned when enabled). |
| `POST` | `/api/v1/artisan/formations/{id}/files` | `permission:artisan:formations` | Uploads course syllabus or resource document attachment (max 10 attachments, max 25MB, scanned when enabled). |
| `DELETE` | `/api/v1/artisan/formations/{id}/files/{fileId}` | `permission:artisan:formations` | Soft-deletes a course material attachment. |
| `POST` | `/api/v1/artisan/formations/{id}/submit` | `permission:artisan:formations` | Submits draft or rejected formation for administrative moderation (`PENDING_REVIEW`). |
| `DELETE` | `/api/v1/artisan/formations/{id}` | `permission:artisan:formations` | Soft-deletes an authored formation. |

### Peer Discovery, Enrollment & Downloads (`ArtisanFormationEnrollmentController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/artisan/formations/catalog` | `permission:artisan:formations` | Browses published peer masterclass catalog (paginated, sorted by `scheduledAt ASC`). |
| `GET` | `/api/v1/artisan/formations/catalog/{id}` | `permission:artisan:formations` | Retrieves detailed public view of a published masterclass, capacity, and syllabus files. |
| `POST` | `/api/v1/artisan/formations/{id}/enroll` | `permission:artisan:formations` | Enrolls caller in published workshop (blocks self-enrollment, enforces max capacity). |
| `POST` | `/api/v1/artisan/formations/{id}/cancel` | `permission:artisan:formations` | Cancels confirmed enrollment reservation (enforces configured cutoff deadline before start). |
| `GET` | `/api/v1/artisan/formations/my-enrollments` | `permission:artisan:formations` | Retrieves paginated enrollment history and registered workshops for authenticated artisan. |
| `GET` | `/api/v1/artisan/formations/{id}/files/{fileId}/download` | `permission:artisan:formations` | Streams protected course attachment (restricted to author and confirmed participants). |

### Administrative Moderation (`AdminFormationController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/formations/pending` | `permission:admin:formations` | Retrieves paginated queue of formations awaiting moderation review. |
| `POST` | `/api/v1/admin/formations/{id}/review` | `permission:admin:formations` | Submits review decision (`APPROVED` or `REJECTED`) with moderation comment. |
| `POST` | `/api/v1/admin/formations/{id}/publish` | `permission:admin:formations` | Publishes an approved formation to the public catalog. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`ArtisanFormationController`](ArtisanFormationController.java) | Handles authenticated artisan masterclass authoring, media uploads, review submissions, and deletion. |
| [`ArtisanFormationEnrollmentController`](ArtisanFormationEnrollmentController.java) | Manages peer catalog browsing, workshop reservations, cancellations, and protected file downloads. |
| [`AdminFormationController`](AdminFormationController.java) | Handles administrative moderation queues, review verdicts, and catalog publication. |
