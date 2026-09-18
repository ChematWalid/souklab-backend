# Backup timer installation

On the VPS, copy the service and timer units to `/etc/systemd/system/`, create
`/etc/souklab/backup.env` with mode `600`, and set at least:

```text
BACKUP_DIR=/srv/souklab-backups
BACKUP_PASSPHRASE_FILE=/root/.souklab-backup-pass
DB_NAME=souklab
DB_USERNAME=replace-with-db-user
DB_PASSWORD=replace-with-db-password
STORAGE_S3_BUCKET=replace-with-existing-bucket
STORAGE_S3_REGION=us-east-1
STORAGE_S3_ACCESS_KEY=replace-with-least-privilege-key
STORAGE_S3_SECRET_KEY=replace-with-least-privilege-secret
# Optional for an S3-compatible provider:
# STORAGE_S3_ENDPOINT=https://s3.example.com
PRODUCTION_ENV_FILE=/opt/souklab/deploy/.env.production
BACKUP_METRICS_PUSHGATEWAY=http://127.0.0.1:9091
```

Then enable the persistent daily schedule:

```sh
systemctl daemon-reload
systemctl enable --now souklab-backup.timer
systemctl start souklab-backup.service
systemctl status souklab-backup.timer souklab-backup.service
```

Keep the passphrase file and environment file outside the repository, both with
mode `600`. The service runs as root because it needs the Docker socket and the
root-readable passphrase file; backup artifacts are created with mode `600`.
Install `docker`, `gpg`, `aws` (AWS CLI), and `curl` on the VPS before enabling
the timer.
