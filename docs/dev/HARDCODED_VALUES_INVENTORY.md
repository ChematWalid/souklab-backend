# Production Hard-Coded-Value Inventory

This inventory distinguishes intentional protocol/domain constants from values
that must be supplied by deployment configuration.

## Must remain configurable

- Database URL, credentials, dialect, schema mode, Flyway enablement, and pool limits.
- JWT secret and access/refresh lifetimes.
- Storage provider, endpoints, bucket, credentials, object limits, and scanning policy.
- Search URI, credentials, timeouts, schema strategy, and mass-indexing limits.
- RabbitMQ relay credentials, ports, and destinations.
- CORS origins, upload MIME allowlists, pagination limits, feed title/body/comment/tag limits, executor sizing, and cache limits.
- SMTP, MailerSend, OAuth, and payment credentials.

These values are represented by environment placeholders in
`src/main/resources/application.properties` and documented in `.env.example`.

## Intentional immutable constants

- JWT bearer scheme and standard HTTP status behavior.
- Permission keys and enum values, which are authorization protocol identifiers.
- Database entity/table column names used by JPA and migrations.
- Search field names and analyzer names, which must match the Elasticsearch mapping.
- MIME types, STOMP protocol tokens, and API route fragments when they define a stable application contract.
- Bounded algorithmic limits such as the directory bio snippet length and maximum badge count.

## Findings requiring follow-up

- `DataSeeder` still contains large reference-data literals. They are deterministic
  seed content, not deployment policy; moving them into Flyway reference-data
  migrations is recommended before operating multiple application replicas.
- Production rate limiting uses Redis-backed Bucket4j state. Local tests and non-production
  slices use an in-memory adapter; Redis is mandatory when the prod profile is active.
- Existing entity timestamps remain `LocalDateTime` for schema/API compatibility;
  all application-generated timestamps use the injected UTC `Clock`.
