# Production Configuration Flow

The application uses external configuration as the runtime source of truth:

```text
deployment environment / local .env
        -> Spring property placeholders
        -> typed configuration beans
        -> configuration consumers
```

`src/main/resources/application.properties` contains no secret values and imports
the ignored local `.env` file only when present. Production must provide the same
variable names through its secret/configuration store and activate the `prod`
profile.

| Responsibility | Environment variables | Spring binding | Typed consumer | Production policy |
| --- | --- | --- | --- | --- |
| Database and schema | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JPA_DATABASE_PLATFORM`, `JPA_HIBERNATE_DDL_AUTO`, `FLYWAY_ENABLED` | `spring.datasource.*`, `spring.jpa.*`, `spring.flyway.enabled` | Spring Boot datasource/JPA/Flyway | MariaDB; Flyway enabled; Hibernate `validate`; UTC; no Open Session in View |
| JDBC pool | `DB_POOL_MAX_SIZE`, `DB_POOL_MIN_IDLE`, `DB_POOL_CONNECTION_TIMEOUT`, `DB_POOL_VALIDATION_TIMEOUT`, `DB_POOL_LEAK_DETECTION_THRESHOLD` | `spring.datasource.hikari.*` | HikariCP | Bounded pool; leak detection opt-in for incident diagnosis |
| Authentication | `APP_JWT_SECRET`, `APP_JWT_ACCESS_EXP`, `APP_JWT_REFRESH_EXP` | `app.jwt.*` | `AppProperties.Jwt`, `JwtUtils` | Secret required and at least 32 UTF-8 bytes; positive lifetimes |
| Account controls | `AUTH_*` | `app.auth.*` | `AppProperties.AuthConfig`, auth services | Positive lockout, OTP, and code policies |
| API rate limiting | `APP_RATE_LIMIT_*`, `RATE_LIMIT_*`, `REDIS_*` | `app.rate-limit.*` | `AppProperties.RateLimit`, `RateLimitEndpointProperties`, IP/user filters | Redis-backed Bucket4j state; shared limiter is mandatory in production; endpoint-specific authenticated-user overrides are optional and fall back to the global user policy |
| Analytics jobs and rollups | `ANALYTICS_DEFAULT_PAGE_SIZE`, `ANALYTICS_MAXIMUM_*`, `ANALYTICS_SUPPORTED_BUCKETS`, `ANALYTICS_*_RETENTION`, `ANALYTICS_JOB_*`, `ANALYTICS_EXPORT_*` | `app.analytics.*` | `AnalyticsProperties`, `AnalyticsJobProperties`, `AnalyticsExportProperties`, `AnalyticsRetentionProperties` | Enum-bound bucket allowlist; bounded ranges/pages/results; positive retention and cleanup policies; business timezone is explicit and persisted timestamps remain UTC |
| Search | `HIBERNATE_SEARCH_*`, `ELASTICSEARCH_*`, `SEARCH_*` | `app.search.*`, `spring.jpa.properties.hibernate.search.*` | `AppProperties.Search`, directory/search lifecycle services | Elasticsearch required in production; schema validation; bounded indexing |
| Application executors/cache | `ASYNC_*`, `APP_CACHE_*` | `app.async.*`, `app.cache.*` | `AppProperties.Async/Cache`, executor/cache config | Positive bounded pools, queues, cache sizes, and durations |
| Object storage | `STORAGE_*`, `APP_STORAGE_*` | `storage.*`, `app.storage.*` | `StorageProperties`, `AppProperties.Storage`, storage services | S3-compatible backend; bucket provisioned externally; credentials never logged |
| Antivirus | `STORAGE_VIRUS_SCAN_*` | `storage.virus-scan.*` | `StorageProperties.VirusScanProperties`, ClamAV services | Enabled and fail-closed in production |
| Avatar/content policy | `AVATAR_*`, `ARTISAN_*`, `FORMATION_*`, `FEED_*`, `NOTIFICATION_*` | `avatar.*`, `app.artisan.*`, `app.formation.*`, `app.feed.*`, `app.notification.*` | Typed policy classes and services | Positive limits and explicit MIME allowlists |
| SMTP and transactional email | `SMTP_*`, `APP_EMAIL_USE_SMTP`, `MAILERSEND_*` | `spring.mail.*`, `app.email.*`, `app.mailersend.*` | `EmailUtil` | Optional integration; enabled mode must have complete credentials |
| OAuth/payment | `GOOGLE_*`, `OAUTH_*`, `CHARGILY_*` | `spring.security.oauth2.*`, `app.oauth.*`, `app.chargily.*` | OAuth/payment services | Optional and disableable; secrets externalized |
| WebSocket relay | `RELAY_*`, `CHAT_*` | `app.relay.*`, `app.chat.*` | `AppProperties.Relay/Chat`, WebSocket config/services | RabbitMQ/STOMP required in production; explicit destinations and credentials |
| Admin bootstrap | `APP_ADMIN_*` | `app.admin.*` | `DataSeeder`, `ConfigurationPolicyValidator` | Bootstrap disabled in production |
| CORS and support | `APP_CORS_ALLOWED_ORIGINS`, `APP_SUPPORT_*` | `app.cors.*`, `app.support.*` | `AppProperties.Cors/SupportConfig`, security config | Explicit origins; wildcard rejected when credentials are enabled |
| Runtime logging | `APP_LOGGING_LEVEL_*` | `logging.level.*` | Spring Boot logging | Production should use `INFO` or stricter; request/debug logging disabled |

The variables used by local Docker Compose (`COMPOSE_PROJECT_NAME`, container credentials, port mappings) are infrastructure inputs configured via `.env` (for host-run app with backing containers) and `.env.docker` (for the fully containerized `app` service). Production uses `deploy/.env.production` strictly bound to the `prod` profile.
