# File Upload Pipeline (`com.project.souklab.filestorage.pipeline`)

Multi-stage reusable upload processing pipeline for the Souklab platform.

---

## Architecture & Lifecycle

The pipeline centralizes the complete multipart upload sequence:
1. Presence & non-empty assertion
2. Configured size bound verification
3. MIME type inspection & magic byte sniffing (via `FileValidator`)
4. ClamAV antivirus stream scanning (via `VirusScanService`)
5. Physical storage persistence (via `StorageService`)
6. Public/CDN URL resolution (via `FileUrlResolver`)
7. Automatic compensating delete if downstream operations fail

---

## Component Reference

| Class / Record | Type | Responsibility |
| :--- | :---: | :--- |
| [`FileUploadPipeline`](FileUploadPipeline.java) | `@Component` | Orchestrates the multi-stage upload lifecycle with rollback compensation. |
| [`FileUploadPolicy`](FileUploadPolicy.java) | Record | Encapsulates maximum byte limits and allowed MIME types per upload context. |
| [`StoredFileResult`](StoredFileResult.java) | Record | Returns storage key, public URL, sanitized filename, and detected MIME type. |
