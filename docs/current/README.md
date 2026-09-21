# Souklab documentation index

These documents describe the current layered modular monolith and its `/api/v1` contract. Historical phase notes are evidence, not API authority.

## Current authority

- [Frontend API handoff](frontend/API_HANDOFF.md) — TypeScript consumer guide.
- [API conventions](API_CONVENTIONS.md) — envelopes, statuses, pagination, and naming.
- [API specification](API_SPEC.md) — endpoint groups and authorization behavior.
- [Authorization matrix](AUTHORIZATION_MATRIX.md) — permission and ownership rules.
- [Architecture](ARCHITECTURE.md) — layer boundaries and package ownership.
- [Production configuration matrix](PRODUCTION_CONFIGURATION_MATRIX.md) — environment policy.
- [Deployment runbook](DEPLOYMENT_RUNBOOK.md) — operational procedures.
- [Database schema](DATABASE_SCHEMA.md) — entities and Flyway migrations.

## Generated contract

When the application is running, Springdoc exposes the code-first contract at `/v3/api-docs` (or the configured `OPENAPI_PATH`). Export it with:

```bash
./scripts/export-openapi.sh http://localhost:8080/v3/api-docs docs/generated/openapi.json
```

The generated artifact is intentionally not committed. CI should upload it as an artifact and validate it before publishing a frontend build.

Phase reports, old release plans, and the generated Postman reference remain for traceability and are explicitly archival where they can describe superseded behavior.
