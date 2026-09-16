# SoukLab Architecture

This document describes the architecture implemented in the current source tree. Java classes, Spring configuration, and JPA mappings are the source of truth; roadmap items are not presented as implemented features.

## Runtime layers

```text
HTTP/STOMP clients
        |
Spring Security filters and method security
        |
REST controllers and STOMP handlers
        |
Transactional application services
        |
Spring Data JPA repositories ---- Hibernate Search / Elasticsearch
        |
MariaDB/MySQL                  S3-compatible object storage
```

Controllers translate transport contracts and delegate to services. Services enforce domain invariants, own transaction boundaries, publish notifications, and coordinate storage. Repositories expose persistence queries. DTOs keep transport contracts separate from JPA entities.

## Implemented packages

| Package | Responsibility |
| --- | --- |
| `config` | Application properties, security, CORS, WebSocket relay, caching, async execution, and seed data. |
| `controller` | REST endpoints for authentication, users, artisans, catalog, directory, formations, formateur governance, and notifications. |
| `dao` | 25 Spring Data JPA repositories for the current entities. |
| `dto` | Request and response contracts grouped by feature. |
| `exception` | Application exception hierarchy and global HTTP error mapping. |
| `filestorage` | Provider-neutral storage API, S3 adapter, in-memory stub, validation, ClamAV scanning, image variants, rate limiting, and post-commit cleanup. |
| `model` | 34 JPA entities and supporting enums. IDs are UUID strings supplied by `BaseEntity`. |
| `security` | JWT parsing, user principal construction, request rate limiting, and STOMP authentication. |
| `service` | Transactional business workflows, including application-specific `service.storage.FileAccessService`. |
| `util` / `validation` | Stateless helpers and custom Bean Validation constraints. |

Social feed, direct messaging, subscriptions, payments, and analytics are roadmap items and have no corresponding controller/service/entity implementation in this repository.

## Authentication and authorization

HTTP authentication uses a signed JWT access token in the `Authorization: Bearer` header. Refresh tokens are opaque UUID values persisted by `RefreshTokenService` and rotated/revoked during refresh and logout. User status, lockout state, and email verification are checked by the authentication service and JWT user-details loading path.

Method-level authorization uses database-backed permissions and centralized domain policies. `AccessControlService` centralizes coarse checks while domain services enforce ownership, account state, verification, enrollment, and moderation policies. WebSocket clients connect to `/ws` (SockJS enabled), send a bearer token in STOMP `CONNECT` headers, and use `/app`, `/topic`, `/queue`, and `/user` destinations. The broker is an externally configured STOMP relay; it is not an embedded RabbitMQ container managed by the application.

## Storage pipeline

`StorageService` is the portable provider-neutral contract. `S3StorageService` is selected for S3-compatible storage and `InMemoryStorageService` supports tests. Uploads pass through size/MIME/magic-byte validation, optional ClamAV scanning, and feature-specific image processing. `FileAccessService` is intentionally application-specific and checks ownership/enrollment before protected objects are served.

Soft-delete workflows schedule object deletion after a successful database commit through `StorageObjectLifecycle`. Failed external deletion is logged for operational follow-up.

## Persistence and concurrency

Entities inherit UUID and audit timestamps from `BaseEntity`; soft-delete is represented by nullable `deletedAt` where mapped. Mutating services use `@Transactional`. Formation enrollment and upload quota paths use pessimistic write locks where a read-then-write quota must be serialized. Production deployments should activate the `prod` profile, use `ddl-auto=validate`, and apply schema changes through an external migration process.

## Search

`Artisan` is indexed with Hibernate Search and queried through Elasticsearch. `DirectorySearchService` provides the indexed directory query path and a relational fallback for resilience. Search schema management is configurable; production profile validation prevents accidental index creation or mutation at startup.
