<!-- Generated: 2026-09-22 | Files scanned: 472 | Token estimate: ~750 -->

# External Dependencies & Infrastructure Codemap

## 1. Runtime Platform

- **Language & JDK**: Java 21 (Eclipse Temurin)
- **Framework**: Spring Boot 4.0.8 / Spring Framework 7.x
- **Embedded Web Server**: Apache Tomcat 11.0.26 (Servlet 6.1, Jakarta EE 11)
- **Serialization**: Jackson 3 (`tools.jackson.databind`, not Jackson 2 `com.fasterxml`)

## 2. Infrastructure Services (Docker Compose)

| Service | Container Image | Port(s) | Role in System |
|---|---|---|---|
| **MariaDB** | `mariadb:11.4` | `3306` (App), `3308` (Flyway test) | Primary relational datastore (UTF8MB4, InnoDB) |
| **RabbitMQ** | `rabbitmq:4.0-management` | `5672` (AMQP), `61613` (STOMP), `15672` (UI) | STOMP message relay for real-time chat & notifications |
| **MinIO** | `cgr.dev/chainguard/minio` | `9000` (S3 API), `9001` (Console) | S3-compatible object storage for avatars, gallery, course files |
| **ClamAV** | `clamav/clamav:1.4` | `3310` | Streaming antivirus inspection for all file uploads |
| **Redis** | `redis:7.4.1-alpine` | `6379` | Distributed token-bucket rate limiting (Bucket4j-redis) |
| **Elasticsearch**| `elasticsearch:8.15.3` | `9200` | Full-text directory search index (BM25, French/Arabic folding) |
| **App (SoukLab)**| `Dockerfile` (build) | `8080` | Spring Boot application container (Eclipse Temurin 21 JRE, non-root `souklab`) |

## 3. Third-Party Integrations & External APIs

| Provider | Integration Type | Authentication | Purpose |
|---|---|---|---|
| **Chargily Pay V2** | Hosted Checkout + Webhook | API Key + Signature Verification (`X-Signature`) | Payment processing for artisan subscription plans in DZD |
| **Google OAuth2** | OpenID Connect / OAuth2 | Client ID + Client Secret | Social registration and authentication for artisans & clients |
| **MailerSend** | REST API (`/v1/email`) | Bearer API Key | Transactional email delivery (PIN codes, password resets) |
| **Local SMTP** | SMTP protocol | Host/Port (`localhost:1025`) | Local development email trap (Mailpit/MailHog fallback) |

## 4. Key Library Ecosystem

| Library | Version | Purpose |
|---|---|---|
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | `0.11.5` | Cryptographic JWT signing (HS256) and claims extraction |
| `bucket4j-core` / `bucket4j-redis` | `8.10.1` | In-memory or Redis-backed sliding token-bucket rate limiting |
| `hibernate-search-mapper-orm` | `8.2.2.Final` | Automatic ORM-to-Elasticsearch index synchronization |
| `aws-sdk-s3` | `2.55.1` | AWS S3 SDK v2 used for MinIO / S3 object operations |
| `tika-core` | `4.0.0` | Content detection via magic-byte inspection (anti-spoofing) |
| `thumbnailator` | `0.4.21` | High-quality image downscaling and thumbnail generation |
| `pdfbox` | `3.0.8` | PDF document validation and metadata extraction |
| `caffeine` | — | High-performance in-memory caching for taxonomy trees |
| `springdoc-openapi-starter-webmvc-ui` | `3.1.1` | Code-first OpenAPI 3 spec generation at `/v3/api-docs` |
