# Schema migrations

`V1__phase7_social.sql` is the reviewed Phase 7 schema change for the external migration/deployment process. This repository does not currently bundle Flyway or Liquibase, so the script is not executed automatically by Spring Boot. Apply it after the existing identity, artisan, and formation tables are present and before starting the `prod` profile with Hibernate schema validation enabled.

The migration is forward-only for production. If a deployment requires rollback, use a reviewed compensating migration rather than editing an applied script.
