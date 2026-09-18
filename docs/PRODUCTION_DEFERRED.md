# Deferred production work

The release-readiness groundwork in this branch is committed, but the final production rollout is intentionally deferred. The items below require real infrastructure, credentials, or an operator decision and should be completed before announcing a production release.

## Deferred until the production deployment window

- Provision and harden the target VPS, DNS records, firewall rules, TLS domain, and backup storage.
- Fill `deploy/.env.production.example` into a permission-restricted production environment file or Docker secrets. Do not commit the populated file.
- Provision the external S3-compatible bucket and least-privilege credentials; verify object retention and replication or inventory behavior.
- Run the dependency vulnerability gate in hosted CI with the repository `NVD_API_KEY` secret and resolve every critical/high finding.
- Publish an immutable application image to the registry and deploy the pinned digest with the production Compose stack.
- Perform a fresh empty-directory deployment, including the one-time search schema bootstrap, then verify the normal application starts with strict schema validation.
- Run the production backup timer and confirm MariaDB and object-storage backups are encrypted, retained, monitored, and restorable.
- Perform and record the full restore drill: database restore, object validation, application startup, Flyway validation, authentication, file access, search, messaging, and readiness checks.
- Verify Caddy HTTP/TLS/WebSocket routing, Prometheus scraping, Grafana dashboards, alert delivery, certificate monitoring, and backup freshness alerts in the deployed environment.
- Exercise restart and failure scenarios for MariaDB, RabbitMQ, Elasticsearch, ClamAV, Redis, and the application. Confirm readiness fails closed for mandatory dependencies and Redis-backed limits remain shared across application processes.
- Complete production smoke tests for authentication, authorization, uploads/downloads, antivirus rejection, search, notifications, messaging, moderation, and graceful shutdown.
- Record the deployed image digest, Flyway version, restore duration, recovery-point age, and rollback decision in the release record.

## Current release boundary

This work does not claim zero-downtime deployment. Application rollback is restart-based and database migrations are never rolled back automatically; corrective migrations must be forward-only. OAuth, mail, payment, and optional search integrations remain configurable, while the mandatory infrastructure dependencies are intended to fail closed.

## Resume checklist

1. Review `docs/RELEASE_CHECKLIST.md` and `docs/DEPLOYMENT_RUNBOOK.md`.
2. Validate the real production environment without exposing secrets.
3. Complete the hosted CI dependency and integration gates.
4. Run the backup/restore drill before the first public deployment.
5. Deploy, verify readiness and critical flows, and record the release evidence.
