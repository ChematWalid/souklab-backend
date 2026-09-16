# Schema migrations

`V1__phase7_social.sql` is the reviewed Phase 7 schema change for the external migration/deployment process. `V2__authorization_permissions.sql` is the breaking development reset that creates direct user permissions and removes role tables. The project bundles Flyway, but keeps it disabled for local development by default because these scripts are incremental and assume the base identity, artisan, and formation tables already exist. Production enables Flyway and must apply the reviewed history before starting with Hibernate schema validation enabled.

The migration is forward-only for production. If a deployment requires rollback, use a reviewed compensating migration rather than editing an applied script.
