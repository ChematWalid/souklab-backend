# Storage Lifecycle (`com.project.souklab.filestorage.lifecycle`)

Coordinates external object cleanup with database transaction completion.

| Class | Responsibility |
| --- | --- |
| [`StorageObjectLifecycle`](StorageObjectLifecycle.java) | Deletes an opaque storage key after commit, or immediately when no transaction synchronization is active. Failures are logged and do not invalidate the committed database mutation. |

The component depends only on the portable `StorageService` contract and is reusable by applications that need post-commit object cleanup.
