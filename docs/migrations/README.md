# Schema migrations

`V1__phase7_social.sql` is the reviewed Phase 7 schema change for the external migration/deployment process. `V2__authorization_permissions.sql` is the breaking development reset that creates direct user permissions and removes role tables. This repository does not currently bundle Flyway or Liquibase, so scripts are reviewed deployment artifacts rather than automatically executed by Spring Boot. Apply them after the base identity, artisan, and formation tables are present and before starting the `prod` profile with Hibernate schema validation enabled.

The migration is forward-only for production. If a deployment requires rollback, use a reviewed compensating migration rather than editing an applied script.
