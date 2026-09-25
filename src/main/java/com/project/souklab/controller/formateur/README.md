# Formateur Controller Package (`com.project.souklab.controller.formateur`)

Manages the dual-sided Formateur accreditation lifecycle: artisan applications and administrator moderation.

---

## Endpoints

### Artisan Endpoints (`ArtisanFormateurController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/artisan/formateur-request` | `permission:artisan:content` | Submits accreditation application with motivation. Enforces 14-day cooldown. |
| `GET` | `/api/v1/artisan/formateur-request` | `permission:artisan:content` | Gets the artisan's latest accreditation request. |
| `GET` | `/api/v1/artisan/formateur-requests` | `permission:artisan:content` | Gets paginated accreditation request history. |
| `GET` | `/api/v1/artisan/formateur-requests/{id}` | `permission:artisan:content` | Gets specific owned accreditation request details by ID. |

### Administrator Endpoints (`AdminFormateurController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/formateur-requests` | `permission:admin:users` | Paginated listing of pending accreditation requests. |
| `GET` | `/api/v1/admin/formateur-requests/{id}` | `permission:admin:users` | Retrieves single accreditation request details by ID. |
| `POST` | `/api/v1/admin/formateur-requests/{id}/approve` | `permission:admin:users` | Approves pending request, sets `isTeacher=true`, and dispatches notification. |
| `POST` | `/api/v1/admin/formateur-requests/{id}/reject` | `permission:admin:users` | Rejects request with admin note and configurable cooldown (default 14 days). |
| `POST` | `/api/v1/admin/artisans/{id}/formateur-grant` | `permission:admin:users` | Directly grants formateur status to an artisan without prior request. |
| `POST` | `/api/v1/admin/artisans/{id}/formateur-revoke` | `permission:admin:users` | Revokes formateur status from an artisan (`isTeacher=false`). |
| `POST` | `/api/v1/admin/formateur-requests/{artisanId}/lift-cooldown` | `permission:admin:users` | Overrides cooldown or reapply restrictions on an artisan request. |

Formateur requests remain immutable after submission; artisans can inspect the latest state and paginated history, but cannot edit or cancel submitted requests.

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`AdminFormateurController`](AdminFormateurController.java) | Administrator review and direct accreditation moderation. |
| [`ArtisanFormateurController`](ArtisanFormateurController.java) | Artisan application submission. |
