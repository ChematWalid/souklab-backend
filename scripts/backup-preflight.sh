#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="${project_dir}/deploy/compose.production.yml"
env_file="${PRODUCTION_ENV_FILE:-${project_dir}/deploy/.env.production}"
: "${BACKUP_DIR:?set BACKUP_DIR to the encrypted backup mount}"
: "${BACKUP_PASSPHRASE_FILE:?set BACKUP_PASSPHRASE_FILE}"
max_age_hours="${BACKUP_MAX_AGE_HOURS:-26}"

test -r "$BACKUP_PASSPHRASE_FILE" || {
  echo "backup passphrase file is not readable: $BACKUP_PASSPHRASE_FILE" >&2
  exit 1
}
test "$(stat -c '%a' "$BACKUP_PASSPHRASE_FILE")" = 600 || {
  echo "backup passphrase file must have mode 600" >&2
  exit 1
}

"${project_dir}/scripts/validate-production-env.sh" "$env_file"
docker compose --env-file "$env_file" -f "$compose_file" config >/dev/null

latest_database="$(find "$BACKUP_DIR/mariadb" -type f -name '*.sql.gz.gpg' -printf '%T@ %p\n' 2>/dev/null \
  | sort -nr | awk 'NR == 1 {print $2}')"
latest_objects="$(find "$BACKUP_DIR/object-storage" -type f -name '*.json.gz.gpg' -printf '%T@ %p\n' 2>/dev/null \
  | sort -nr | awk 'NR == 1 {print $2}')"
test -n "$latest_database" || { echo 'no encrypted MariaDB backup found' >&2; exit 1; }
test -n "$latest_objects" || { echo 'no encrypted object-storage inventory found' >&2; exit 1; }

now="$(date +%s)"
for backup in "$latest_database" "$latest_objects"; do
  modified="$(stat -c '%Y' "$backup")"
  age_hours=$(( (now - modified) / 3600 ))
  test "$age_hours" -le "$max_age_hours" || {
    echo "backup is stale (${age_hours}h): $backup" >&2
    exit 1
  }
done

gpg --batch --quiet --decrypt --passphrase-file "$BACKUP_PASSPHRASE_FILE" "$latest_database" | gzip -t
gpg --batch --quiet --decrypt --passphrase-file "$BACKUP_PASSPHRASE_FILE" "$latest_objects" | gzip -t
echo "backup preflight passed: database=$latest_database objects=$latest_objects"
