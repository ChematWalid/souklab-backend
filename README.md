# SoukLab Backend - Production Spring Boot Architecture

Enterprise-grade Spring Boot backend platform dedicated to the preservation, discovery, and commercialization of Algerian traditional crafts and craftsmanship heritage.

---

## Architecture Overview

SoukLab follows a decoupled layered architecture adhering to Domain-Driven Design (DDD) principles:

```mermaid
graph TD
    Client["Client / Frontend / Mobile"] -->|"REST / HTTPS"| Security["Spring Security & Rate Limit Filters"]
    Client -->|"STOMP / WSS"| WsBroker["Spring WebSocket Relay (RabbitMQ)"]
    Security --> ControllerLayer["Controller Layer (REST Adapters)"]
    ControllerLayer --> ServiceLayer["Service Layer (Transactional Business Logic)"]
    ServiceLayer --> DAOLayer["DAO Layer (Spring Data JPA Repositories)"]
    ServiceLayer --> StorageModule["File Storage Engine (S3 / MinIO / ClamAV)"]
    ServiceLayer --> NotificationEngine["Notification Engine (In-App & STOMP Push)"]
    DAOLayer --> Database[("MariaDB / MySQL 8.0")]
    StorageModule --> MinIO[("MinIO S3 Bucket")]
    StorageModule --> ClamAV["ClamAV Daemon (:3310)"]
```

---

## Core Modules & Capabilities

| Module | Core Responsibility | Key Technologies |
| :--- | :--- | :--- |
| **Authentication & RBAC** | Stateless JWT authentication, role-based authorization (`CLIENT`, `ARTISAN`, `ADMIN`), email verification codes, password reset lifecycle, OAuth2 Google login. | Spring Security 6, JJWT (HS256), BCrypt |
| **User & Profile Management** | Artisan public profiles, profile completion wizard, contact gating based on membership/roles, profile view metrics deduplication. | Spring Data JPA, Jakarta Validation |
| **Public Directory & Search** | Full-text scored search, faceted discovery (Wilayas, categories, materials, epoques, techniques), accent folding, edge n-grams, and JPA criteria fallback. | Hibernate Search 8.2.2.Final, Elasticsearch 8.x |
| **Catalog & Craft Taxonomy** | Hierarchical reference data (Wilayas/Communes, Categories/Subcategories, Material Families/Materials, Epochs, Craftsmanship Techniques). | Caffeine Cache, Spring Data JPA |
| **Formations & Peer Workshops** | Peer masterclass authoring (`isTeacher`), syllabus ClamAV scanning, administrative review lifecycle, capacity limits, cancellation deadlines, and client 403 boundary. | Spring Security, ClamAV, Spring Data JPA |
| **Formateur Accreditation** | Artisan teacher certification lifecycle (submission, admin review, cooldown enforcement, direct admin grants/revocations). | Multi-state state machine, Spring Events |
| **In-App Notifications** | User-scoped notification feeds, unread badge counters, instant WebSocket broadcast, query-scoped soft deletions. | Spring WebSocket, STOMP Relay, JPA Soft Delete |
| **File Storage & Avatars** | Multi-tier avatar processing (thumbnail, medium, full), magic number verification, ClamAV streaming antivirus, S3/MinIO bucket storage. | MinIO S3 SDK, Thumbnailator, Clamd instream |
| **Rate Limiting & Security** | Token-bucket sliding window rate limiting on authentication and avatar uploads, cache-backed with Caffeine. | Bucket4j, Caffeine Cache, OncePerRequestFilter |

---

## Technology Stack

- **Runtime & Language**: Java 17+, Spring Boot 4.0+
- **Data Persistence**: Spring Data JPA + Hibernate 6 + MariaDB / MySQL 8.0
- **Search Engine & Indexing**: Hibernate Search 8.2.2.Final (Elasticsearch 8.x backend)
- **Caching**: Caffeine Cache (catalog taxonomies, rate limiting buckets)
- **Connection Pool**: HikariCP (configured with leak detection and connection pooling)
- **Object Storage**: S3-compatible object store (MinIO for local development, AWS S3 / Cloudflare R2 for production)
- **Security & Antivirus**: Spring Security, JJWT, Bucket4j, ClamAV Daemon
- **Realtime Broker**: Spring WebSocket STOMP relay (RabbitMQ)
- **Build & Quality Tooling**: Maven Wrapper (`./mvnw`), Lombok, JaCoCo, Postman / Newman

---

## Repository Package Structure

```
src/main/java/com/project/souklab/
├── config/              # Infrastructure and cross-cutting framework beans
│   └── search/          # Hibernate Search Elasticsearch analysis config and startup index runner
├── controller/          # REST API entrypoints and HTTP adapters
│   ├── artisan/         # Artisan profile, certification, and gallery portfolio endpoints
│   ├── auth/            # Registration, login, verification, and password flows
│   ├── catalog/         # Reference craft taxonomies and administrative geography
│   ├── directory/       # Public artisan directory search and faceted filtering
│   ├── formateur/       # Formateur accreditation and moderation endpoints
│   ├── formation/       # Masterclass authoring, peer enrollment, and review endpoints
│   ├── notification/    # Notification feed and read-state management
│   └── user/            # User avatar upload/activation and admin moderation
├── dao/                 # Spring Data JPA repositories (25 repositories)
├── dto/                 # Data Transfer Objects (contracts for API requests/responses)
│   ├── admin/           # Administrative audit representations
│   ├── artisan/         # Certification and gallery image responses
│   ├── auth/            # Login, registration, token refresh, and password DTOs
│   ├── catalog/         # Reference taxonomy and geographic representations
│   ├── common/          # Standard response envelopes (ApiResponse, PaginatedResponse)
│   ├── directory/       # Directory search cards and criteria filter DTOs
│   ├── formateur/       # Formateur request and moderation DTOs
│   ├── formation/       # Masterclass authoring, review, enrollment, and file DTOs
│   ├── notification/    # Notification payload representations
│   ├── profile/         # Artisan and client profile representations
│   └── user/            # Moderation requests and avatar responses
├── exception/           # Exception hierarchy and GlobalExceptionHandler
├── filestorage/         # Dedicated object storage engine
│   ├── config/          # MinIO/S3 connection properties
│   ├── controller/      # Direct file streaming endpoints
│   ├── exception/       # File storage exception taxonomy
│   ├── image/           # Image resizing and thumbnailing service
│   ├── s3/              # S3/MinIO service implementation
│   ├── scan/            # ClamAV virus scanning integration
│   ├── security/        # File serving rate limit filter
│   ├── stub/            # In-memory test stubs
│   └── validation/      # Magic bytes and MIME validation
├── model/               # JPA entities and domain enums (34 models)
├── security/            # Security filters (JWT, rate limiting, upload boundaries)
├── service/             # Application business logic and transactional services
│   ├── artisan/         # Artisan profile and portfolio operations
│   ├── audit/           # Audit trail logging
│   ├── auth/            # User authentication and details management
│   ├── catalog/         # Cached taxonomy and geographic retrieval
│   ├── directory/       # Hibernate Search Elasticsearch discovery service
│   ├── formateur/       # Formateur accreditation workflows
│   ├── formation/       # Masterclass lifecycle, peer enrollment, and moderation
│   ├── notification/    # In-app notifications and WebSocket dispatch
│   ├── profile/         # Profile lifecycle, FK taxonomy resolution, and PATCH updates
│   ├── security/        # Token issuance and verification
│   └── user/            # User management and avatar processing
├── util/                # Stateless utility functions and mappers
└── validation/          # Custom Jakarta Bean Validation constraints
```

---

## Getting Started

### 1. Prerequisites
- JDK 17 or higher
- Docker & Docker Compose
- Maven 3.8+ (or use repository `./mvnw`)

### 2. Infrastructure Setup
Launch local infrastructure containers:
```bash
docker compose up -d mariadb minio rabbitmq clamav
```

Service endpoints:
- **MariaDB**: `localhost:3306` (Database: `souklab_db`)
- **MinIO S3**: `http://localhost:9000` (Console: `http://localhost:9001`)
- **RabbitMQ**: `localhost:5672` (Management: `http://localhost:15672`)
- **ClamAV**: `localhost:3310`

### 3. Build & Run
```bash
# Compile and test
./mvnw clean test

# Run development server
./mvnw spring-boot:run
```

The server listens on `http://localhost:8080/api/v1`.

---

## API Documentation & Testing Suite

- **API Specification**: See [`docs/API_SPEC.md`](docs/API_SPEC.md) for full endpoint references.
- **Postman API Reference**: Exhaustive contracts documented in [`docs/POSTMAN_API_REFERENCE.md`](docs/POSTMAN_API_REFERENCE.md).
- **Postman Test Suite**: Located in [`.postman/souklab.postman_collection.json`](.postman/souklab.postman_collection.json).
  To run the automated Newman verification:
  ```bash
  npx newman run .postman/souklab.postman_collection.json -e .postman/souklab.postman_environment.json
  ```

---

## Package Documentation Directory

Each individual package across the application contains its own dedicated `README.md` specifying internal classes, contracts, and architecture:

- [`com.project.souklab`](src/main/java/com/project/souklab/README.md) — Application root
- [`com.project.souklab.config`](src/main/java/com/project/souklab/config/README.md) — Framework configuration
  - [`config.search`](src/main/java/com/project/souklab/config/search/README.md) — Hibernate Search Elasticsearch configuration and startup runner
- [`com.project.souklab.controller`](src/main/java/com/project/souklab/controller/README.md) — Controller layer overview
  - [`controller.artisan`](src/main/java/com/project/souklab/controller/artisan/README.md) — Artisan profile, certification, and gallery portfolio endpoints
  - [`controller.auth`](src/main/java/com/project/souklab/controller/auth/README.md) — Authentication endpoints
  - [`controller.catalog`](src/main/java/com/project/souklab/controller/catalog/README.md) — Reference craft taxonomy endpoints
  - [`controller.directory`](src/main/java/com/project/souklab/controller/directory/README.md) — Public artisan directory search endpoints
  - [`controller.formateur`](src/main/java/com/project/souklab/controller/formateur/README.md) — Formateur accreditation endpoints
  - [`controller.formation`](src/main/java/com/project/souklab/controller/formation/README.md) — Formations authoring, peer enrollment, and review endpoints
  - [`controller.notification`](src/main/java/com/project/souklab/controller/notification/README.md) — Notification endpoints
  - [`controller.user`](src/main/java/com/project/souklab/controller/user/README.md) — User and avatar endpoints
- [`com.project.souklab.dao`](src/main/java/com/project/souklab/dao/README.md) — Persistence repositories (25 repositories)
- [`com.project.souklab.dto`](src/main/java/com/project/souklab/dto/README.md) — DTO taxonomy
  - [`dto.admin`](src/main/java/com/project/souklab/dto/admin/README.md) — Admin audit DTOs
  - [`dto.artisan`](src/main/java/com/project/souklab/dto/artisan/README.md) — Portfolio certification and gallery response DTOs
  - [`dto.auth`](src/main/java/com/project/souklab/dto/auth/README.md) — Authentication DTOs
  - [`dto.catalog`](src/main/java/com/project/souklab/dto/catalog/README.md) — Reference taxonomy DTOs
  - [`dto.common`](src/main/java/com/project/souklab/dto/common/README.md) — Response envelopes
  - [`dto.directory`](src/main/java/com/project/souklab/dto/directory/README.md) — Directory search cards and criteria filter DTOs
  - [`dto.formateur`](src/main/java/com/project/souklab/dto/formateur/README.md) — Formateur DTOs
  - [`dto.formation`](src/main/java/com/project/souklab/dto/formation/README.md) — Formation authoring, review, enrollment, and file DTOs
  - [`dto.notification`](src/main/java/com/project/souklab/dto/notification/README.md) — Notification DTOs
  - [`dto.profile`](src/main/java/com/project/souklab/dto/profile/README.md) — Profile representations
  - [`dto.user`](src/main/java/com/project/souklab/dto/user/README.md) — User and avatar DTOs
- [`com.project.souklab.exception`](src/main/java/com/project/souklab/exception/README.md) — Exception handling
- [`com.project.souklab.filestorage`](src/main/java/com/project/souklab/filestorage/README.md) — Storage engine
  - [`filestorage.config`](src/main/java/com/project/souklab/filestorage/config/README.md) — S3 configuration
  - [`filestorage.controller`](src/main/java/com/project/souklab/filestorage/controller/README.md) — File streaming controller
  - [`filestorage.exception`](src/main/java/com/project/souklab/filestorage/exception/README.md) — Storage exceptions
  - [`filestorage.image`](src/main/java/com/project/souklab/filestorage/image/README.md) — Image processing service
  - [`filestorage.s3`](src/main/java/com/project/souklab/filestorage/s3/README.md) — MinIO/S3 client
  - [`filestorage.scan`](src/main/java/com/project/souklab/filestorage/scan/README.md) — ClamAV scanner
  - [`filestorage.security`](src/main/java/com/project/souklab/filestorage/security/README.md) — Download rate limiting
  - [`filestorage.stub`](src/main/java/com/project/souklab/filestorage/stub/README.md) — In-memory test stubs
  - [`filestorage.validation`](src/main/java/com/project/souklab/filestorage/validation/README.md) — File validation
- [`com.project.souklab.service.storage`](src/main/java/com/project/souklab/service/storage/FileAccessService.java) — Application-specific storage access policy
- [`com.project.souklab.model`](src/main/java/com/project/souklab/model/README.md) — Domain entities and enums (34 models)
- [`com.project.souklab.security`](src/main/java/com/project/souklab/security/README.md) — Security filters and token parsing
- [`com.project.souklab.service`](src/main/java/com/project/souklab/service/README.md) — Service layer architecture
  - [`service.artisan`](src/main/java/com/project/souklab/service/artisan/README.md) — Artisan profile and portfolio services
  - [`service.audit`](src/main/java/com/project/souklab/service/audit/README.md) — Audit trail logging
  - [`service.auth`](src/main/java/com/project/souklab/service/auth/README.md) — Authentication workflows
  - [`service.catalog`](src/main/java/com/project/souklab/service/catalog/README.md) — Cached taxonomy retrieval service
  - [`service.directory`](src/main/java/com/project/souklab/service/directory/README.md) — Hibernate Search Elasticsearch discovery service
  - [`service.formateur`](src/main/java/com/project/souklab/service/formateur/README.md) — Formateur management
  - [`service.formation`](src/main/java/com/project/souklab/service/formation/README.md) — Masterclass lifecycle, peer enrollment, and moderation
  - [`service.notification`](src/main/java/com/project/souklab/service/notification/README.md) — Notification dispatcher
  - [`service.profile`](src/main/java/com/project/souklab/service/profile/README.md) — User profile management and taxonomy resolution
  - [`service.security`](src/main/java/com/project/souklab/service/security/README.md) — Token and verification services
  - [`service.user`](src/main/java/com/project/souklab/service/user/README.md) — User moderation and avatars
- [`com.project.souklab.util`](src/main/java/com/project/souklab/util/README.md) — Helper utilities
- [`com.project.souklab.validation`](src/main/java/com/project/souklab/validation/README.md) — Custom validator annotations
