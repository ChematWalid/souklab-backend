# Directory Controller Package (`com.project.souklab.controller.directory`)

Public REST controller for the artisan marketplace directory and faceted search engine. Exposes full-text search, geographic filtering, and craft taxonomy discovery without requiring client authentication.

---

## Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/public/directory` | Public | Searches and filters public artisan directory profiles with multi-facet queries, keyword matching, and pagination. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`DirectoryController`](DirectoryController.java) | REST controller handling `/api/v1/public/directory` requests and delegating to `DirectorySearchService`. |
