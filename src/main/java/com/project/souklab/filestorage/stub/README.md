# Storage Stubs Package (`com.project.souklab.filestorage.stub`)

In-memory storage mocks and test doubles for isolated unit testing.

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`InMemoryStorageService`](InMemoryStorageService.java) | Stores byte arrays in a concurrent hash map to allow fast, external-dependency-free integration tests. |
| [`StoredFile`](StoredFile.java) | Value record representing a stored byte payload, MIME type, and metadata held in the stub storage map. |
