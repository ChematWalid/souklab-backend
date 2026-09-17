# Storage Controller Package (`com.project.souklab.filestorage.controller`)

HTTP controllers for direct file streaming and access-controlled resource downloads.

---

## Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/files/{key}` | Access policy dependent | Streams a stored object after `FileAccessService` authorization, with MIME detection and download rate limiting. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`FileServingController`](FileServingController.java) | Streams stored file content directly to HTTP clients with immutable cache headers and MIME detection. |
