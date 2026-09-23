<!-- Generated: 2026-09-22 | Files scanned: 472 | Token estimate: ~850 -->

# System Architecture Codemap

## 1. High-Level System Topology

```
[ Web / Mobile Clients ]
       │  (HTTPS REST)               │  (WSS / STOMP)
       ▼                             ▼
┌──────────────────────────────┬──────────────────────────────┐
│ Spring Security & Rate Limits│ STOMP WebSocket Relay        │
│ (JWT Auth, Bucket4j, CORS)   │ (RabbitMQ Broker :61613)     │
└──────────────┬───────────────┴──────────────┬───────────────┘
               ▼                              ▼
┌─────────────────────────────────────────────────────────────┐
│ Controller Layer (33 REST Controllers + STOMP Handlers)     │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ Application Service Layer (46 Transactional Services)       │
│ - Domain Invariants   - Event Outbox   - File Access Policy │
└──────┬───────────────┬──────────────┬───────────────┬───────┘
       ▼               ▼              ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌───────────┐ ┌───────────────┐
│ Spring Data │ │ Hibernate   │ │ Storage   │ │ External APIs │
│ JPA (46 Repo│ │ Search 8.2  │ │ Engine    │ │ - Chargily    │
│  MariaDB)   │ │ Elastic :920│ │ MinIO S3  │ │ - MailerSend  │
└─────────────┘ └─────────────┘ └─────┬─────┘ └───────────────┘
                                      ▼
                                ┌───────────┐
                                │ ClamAV    │
                                │ Scan :3310│
                                └───────────┘
```

## 2. Layer Responsibilities & Contracts

- **Transports (`controller/`, `filestorage/controller/`)**:
  HTTP/STOMP adapters. Validate inputs via Jakarta Validation (`@Valid`). Map HTTP codes to standard `ApiResponse<T>` envelope. Never own transactions or expose JPA entities.
- **Security (`security/`, `filestorage/security/`)**:
  - `JwtAuthenticationFilter`: Extracts and verifies HS256 Bearer JWT.
  - `RateLimitFilter`: IP-based token-bucket limiter on sensitive endpoints (`/auth/login`, `/verify-email`, `/forgot-password`).
  - `UserRateLimitFilter`: User-based token-bucket limiter on authenticated requests.
  - `AvatarUploadRateLimitFilter` / `FileRateLimitFilter`: Storage-specific rate limiters.
  - *All rate limiters emit `Retry-After: <seconds>` on HTTP 429.*
- **Services (`service/`, `filestorage/`)**:
  Own `@Transactional` boundaries. Enforce domain invariants (e.g. self-enrollment guard, single review per attended session, 14-day formateur cooldown). Handle post-commit storage cleanups.
- **Persistence (`dao/`, `model/`)**:
  Spring Data JPA repositories extending `JpaRepository` and `JpaSpecificationExecutor`. Soft deletes mapped via `deletedAt`. All entities extend `BaseEntity` (UUID primary key + audit timestamps).
- **Search (`config/search/`, `service/directory/`)**:
  Hibernate Search 8.2.2 synchronizes `Artisan` entity index into Elasticsearch 8.15.3. JPA criteria fallback on Elasticsearch unavailability.

## 3. Asynchronous & Messaging Pipelines

- **Thread Pools**:
  - `souklab-async-`: Core 4, Max 16, Queue 200 (General async events, notifications)
  - `souklab-workflow-`: Core 8, Max 32, Queue 500 (Batch analytics rollups, export jobs)
- **STOMP Destination Topology**:
  - App prefix: `/app` (Client sends commands to `/app/v1/conversations/{id}/*`)
  - User queues: `/user/queue/chat` (delivery + ACK), `/user/queue/chat-events` (mutations/presence), `/user/queue/notifications` (system push)
- **Analytics Outbox Pattern**:
  `analytics_outbox_events` guarantees at-least-once KPI delivery decoupled from user transaction.
