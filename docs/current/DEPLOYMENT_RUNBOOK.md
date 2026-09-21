# Deployment and recovery runbook

## Deploy

1. Build and publish the application image, recording its digest.
2. Validate the production environment with `scripts/validate-production-env.sh` and `docker compose ... config`.
3. Run `BACKUP_DIR=/srv/souklab-backups BACKUP_PASSPHRASE_FILE=/root/.souklab-backup-pass scripts/backup-preflight.sh` and confirm the latest backup age is acceptable.
4. On a fresh Elasticsearch volume, run the one-time bootstrap:
   `docker compose --profile bootstrap --env-file deploy/.env.production -f deploy/compose.production.yml run --rm search-bootstrap`
5. Start the stack. The app waits for dependency health, runs Flyway, and exposes readiness.
6. Verify readiness from the internal network; Caddy intentionally returns 404 for public `/actuator/*` routes:
   `docker compose --env-file deploy/.env.production -f deploy/compose.production.yml exec app wget -qO- http://localhost:8080/actuator/health/readiness`
7. Verify Prometheus scraping, authentication, upload/download, search, messaging, and antivirus rejection.
8. Review the Grafana dashboard through an SSH tunnel (`ssh -L 3000:127.0.0.1:3000 deploy@vps`); Grafana is intentionally not public.

## Roll back

Set `APP_IMAGE` to the previous digest and recreate `app`. Never automatically roll back database migrations. Apply a forward migration if the schema needs correction.

## Backup and restore

Install `deploy/systemd/souklab-backup.service` and `deploy/systemd/souklab-backup.timer` on the VPS, create `/etc/souklab/backup.env` with the backup variables, and enable the timer. The script stores encrypted MariaDB dumps and encrypted object-storage inventories in daily, weekly, and monthly streams; set `BACKUP_FAILURE_WEBHOOK` for failure notification. It also publishes success/failure timestamps to the private Pushgateway on `127.0.0.1:9091`, which Prometheus alerts on when backups are stale or fail. Execute `scripts/restore-drill.sh` with a selected dump and inventory. It restores MariaDB into an isolated container and validates the encrypted object inventory; set `RESTORE_SMOKE_COMMAND` to start a temporary application against that database and verify Flyway, readiness, authentication, file access, search, messaging, and antivirus. Record restore duration and backup age in the release checklist.

Redis buckets are persisted through the production Redis AOF and named volume, so a normal container restart preserves the current limits. A lost volume, `FLUSHDB`, or unrecoverable Redis data loss resets buckets; during Redis unavailability protected requests fail closed and application readiness remains down until Redis responds again.
