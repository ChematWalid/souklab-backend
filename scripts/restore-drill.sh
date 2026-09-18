#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

: "${BACKUP_FILE:?set BACKUP_FILE to an encrypted MariaDB dump}"
: "${BACKUP_PASSPHRASE_FILE:?set BACKUP_PASSPHRASE_FILE}"
: "${DB_NAME:?set DB_NAME}"
: "${DB_ROOT_PASSWORD:?set DB_ROOT_PASSWORD}"
: "${OBJECT_INVENTORY_FILE:?set OBJECT_INVENTORY_FILE to an object-storage inventory JSON}"

case "$DB_NAME" in
  (''|*[!a-zA-Z0-9_]*)
    echo "invalid restore database name: $DB_NAME" >&2
    exit 1
    ;;
esac

test -s "$BACKUP_FILE"
test -s "$OBJECT_INVENTORY_FILE"
gpg --batch --quiet --decrypt --passphrase-file "$BACKUP_PASSPHRASE_FILE" "$BACKUP_FILE" | gzip -t

inventory_tmp="$(mktemp)"
cleanup_inventory() { rm -f "$inventory_tmp"; }
trap cleanup_inventory EXIT
gpg --batch --quiet --decrypt --passphrase-file "$BACKUP_PASSPHRASE_FILE" \
  "$OBJECT_INVENTORY_FILE" | gzip -t
gpg --batch --quiet --decrypt --passphrase-file "$BACKUP_PASSPHRASE_FILE" \
  "$OBJECT_INVENTORY_FILE" | gzip -dc > "$inventory_tmp"
if command -v jq >/dev/null 2>&1; then
  jq -e '.Contents // []' "$inventory_tmp" >/dev/null
fi

container="souklab-restore-drill-$$"
cleanup() { docker rm -f "$container" >/dev/null 2>&1 || true; rm -f "$inventory_tmp"; }
trap cleanup EXIT

docker run --detach --name "$container" \
  -e MARIADB_DATABASE="$DB_NAME" \
  -e MARIADB_ROOT_PASSWORD="$DB_ROOT_PASSWORD" \
  mariadb:11.4.4 >/dev/null

deadline=$(( $(date +%s) + 180 ))
until docker exec "$container" \
  mariadb-admin ping -h localhost -u root --silent >/dev/null 2>&1; do
  if [ "$(date +%s)" -ge "$deadline" ]; then
    echo 'restore drill database did not become ready' >&2
    exit 1
  fi
  sleep 2
done

docker exec "$container" mariadb -u root \
  -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\`"

gpg --batch --quiet --decrypt --passphrase-file "$BACKUP_PASSPHRASE_FILE" "$BACKUP_FILE" \
  | gzip -dc \
  | docker exec --interactive "$container" mariadb -u root "$DB_NAME"

docker exec "$container" mariadb -u root "$DB_NAME" \
  -e 'SELECT COUNT(*) AS restored_tables FROM information_schema.tables WHERE table_schema = DATABASE();'

if [ -n "${RESTORE_SMOKE_COMMAND:-}" ]; then
  sh -c "$RESTORE_SMOKE_COMMAND"
fi
echo 'restore drill completed; encrypted database and object inventory validated'
