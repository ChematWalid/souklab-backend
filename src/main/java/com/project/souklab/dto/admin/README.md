# Admin DTO Package (`com.project.souklab.dto.admin`)

Data representations for administrative monitoring, auditing, and platform management.

---

## Classes Reference

| Class | Type | Description |
| :--- | :--- | :--- |
| [`AuditLogDTO`](AuditLogDTO.java) | DTO Class | Serializes platform audit log entries including `action`, `performedBy`, `details`, and `createdAt` timestamp. |
| [`PermissionAssignmentRequestDTO`](PermissionAssignmentRequestDTO.java) | Request DTO | Carries the persisted value of one canonical grouped `Permission` enum for administrator assignment or revocation. |
