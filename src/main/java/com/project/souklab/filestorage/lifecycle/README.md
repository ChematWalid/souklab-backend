# Storage Lifecycle (`com.project.souklab.filestorage.lifecycle`)

Coordinates external object cleanup with database transaction completion.

| Class | Responsibility |
| --- | --- |
| [`StorageObjectLifecycle`](StorageObjectLifecycle.java) | Deletes an opaque storage key after commit, or immediately when no transaction synchronization is active. Failures are logged and do not invalidate the committed database mutation. |
| [`StorageDeletionAfterCommit`](StorageDeletionAfterCommit.java) | Performs the named post-commit deletion callback without embedding an anonymous transaction synchronization implementation. |

The component depends only on the portable `StorageService` contract and is reusable by applications that need post-commit object cleanup.
