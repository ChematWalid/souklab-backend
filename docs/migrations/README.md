# Schema migrations

`V0__baseline_schema.sql` creates the pre-feature MariaDB schema required by the application. `V1__phase7_social.sql`, `V2__authorization_permissions.sql`, `V3__phase8_messaging.sql`, and `V4__production_query_indexes.sql` then apply the forward-only feature and query changes. Spring Boot's Flyway starter is enabled for production while local development keeps Flyway disabled by default.

Fresh production installation applies V0 through V4 before Hibernate starts. Existing deployments must reconcile their current schema with the Flyway history before enabling this chain; do not mark migrations as applied without reviewing the actual schema. Production uses Hibernate `validate` after migrations.

Elasticsearch requires a separate, explicit first-install bootstrap: set `SEARCH_SCHEMA_BOOTSTRAP_ENABLED=true`, `HIBERNATE_SEARCH_SCHEMA_MANAGEMENT=create-or-update`, and run the application once with the mandatory dependencies available. After indexes and aliases are created, restart with `SEARCH_SCHEMA_BOOTSTRAP_ENABLED=false` and `HIBERNATE_SEARCH_SCHEMA_MANAGEMENT=validate`.

The migration is forward-only for production. If a deployment requires rollback, use a reviewed compensating migration rather than editing an applied script.
