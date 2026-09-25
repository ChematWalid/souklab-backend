# Schema migrations

`V0__baseline_schema.sql` creates the pre-feature MariaDB schema required by the application. Subsequent migrations (`V1` through `V17`) apply the forward-only social feed, authorization permissions, messaging, query indexing, subscriptions/payments, analytics pipelines, financial audits, admin catalog taxonomy management (`V15__admin_catalog_permission.sql`), client favorite artisans (`V16__client_favorites.sql`), and complete feed social engagement (`V17__feed_social_enhancements.sql`). Spring Boot's Flyway starter is enabled for production while local development keeps Flyway disabled by default.

Fresh production installation applies V0 through V17 before Hibernate starts. Existing deployments must reconcile their current schema with the Flyway history before enabling this chain; do not mark migrations as applied without reviewing the actual schema. Production uses Hibernate `validate` after migrations.

The dependency-backed verifier runs `scripts/verify-flyway.sh` against a fresh
isolated MariaDB schema before the application test suite. It applies and then
validates the complete migration chain using the Flyway version managed by the
build.

Elasticsearch requires a separate, explicit first-install bootstrap: set `SEARCH_SCHEMA_BOOTSTRAP_ENABLED=true`, `HIBERNATE_SEARCH_SCHEMA_MANAGEMENT=create-or-update`, and run the application once with the mandatory dependencies available. After indexes and aliases are created, restart with `SEARCH_SCHEMA_BOOTSTRAP_ENABLED=false` and `HIBERNATE_SEARCH_SCHEMA_MANAGEMENT=validate`.

The migration is forward-only for production. If a deployment requires rollback, use a reviewed compensating migration rather than editing an applied script.
