# Storage Access Policy (`com.project.souklab.service.storage`)

Application-specific authorization for opaque file keys. This package is deliberately separate from `filestorage`, so the portable storage engine does not depend on users, permissions, formations, or enrollment rules.

| Class | Responsibility |
| --- | --- |
| [`FileAccessService`](FileAccessService.java) | Resolves public media and enforces administrator, owner, author, and confirmed-enrollee access for protected certification and formation objects. |

Storage providers retrieve bytes; this policy service decides whether the current principal may retrieve them.
