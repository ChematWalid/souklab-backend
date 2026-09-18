#!/usr/bin/env bash
set -euo pipefail
umask 077

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="${project_dir}/deploy/compose.production.yml"
env_file="${PRODUCTION_ENV_FILE:-${project_dir}/deploy/.env.production}"

: "${BACKUP_DIR:?set BACKUP_DIR to an encrypted backup mount}"
: "${DB_NAME:?set DB_NAME}"
: "${DB_USERNAME:?set DB_USERNAME}"
: "${DB_PASSWORD:?set DB_PASSWORD}"
: "${BACKUP_PASSPHRASE_FILE:?set BACKUP_PASSPHRASE_FILE to a root-readable secret file}"

read_env_value() {
  sed -n "s/^$1=//p" "${env_file}" | tail -n 1
}
storage_bucket="${STORAGE_S3_BUCKET:-$(read_env_value STORAGE_S3_BUCKET)}"
storage_access_key="${STORAGE_S3_ACCESS_KEY:-$(read_env_value STORAGE_S3_ACCESS_KEY)}"
storage_secret_key="${STORAGE_S3_SECRET_KEY:-$(read_env_value STORAGE_S3_SECRET_KEY)}"
storage_region="${STORAGE_S3_REGION:-$(read_env_value STORAGE_S3_REGION)}"
storage_endpoint="${STORAGE_S3_ENDPOINT:-$(read_env_value STORAGE_S3_ENDPOINT)}"
: "${storage_access_key:?set STORAGE_S3_ACCESS_KEY in the environment or production env file}"
: "${storage_secret_key:?set STORAGE_S3_SECRET_KEY in the environment or production env file}"
: "${storage_region:?set STORAGE_S3_REGION in the environment or production env file}"
: "${storage_bucket:?set STORAGE_S3_BUCKET in the environment or production env file}"

timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
publish_backup_metric() {
  [ -n "${BACKUP_METRICS_PUSHGATEWAY:-}" ] || return 0
  curl --fail --silent --show-error --max-time 10 \
    --data-binary @- "${BACKUP_METRICS_PUSHGATEWAY}/metrics/job/souklab-backup" || true
}
mkdir -p "${BACKUP_DIR}/mariadb/daily" "${BACKUP_DIR}/mariadb/weekly" "${BACKUP_DIR}/mariadb/monthly"
mkdir -p "${BACKUP_DIR}/object-storage/daily" "${BACKUP_DIR}/object-storage/weekly" "${BACKUP_DIR}/object-storage/monthly"
tmp_dump="$(mktemp)"
tmp_inventory="$(mktemp)"
on_exit() {
  status=$?
  if [ "$status" -ne 0 ] && [ -n "${BACKUP_FAILURE_WEBHOOK:-}" ]; then
    curl --fail --silent --show-error -X POST -H 'Content-Type: application/json' \
      --data '{"text":"Souklab production backup failed"}' "$BACKUP_FAILURE_WEBHOOK" || true
  fi
  if [ "$status" -ne 0 ]; then
    publish_backup_metric <<EOF
# TYPE souklab_backup_last_failure_timestamp_seconds gauge
souklab_backup_last_failure_timestamp_seconds $(date +%s)
EOF
  fi
  rm -f "${tmp_dump}"
  rm -f "${tmp_inventory}"
  exit "$status"
}
trap on_exit EXIT
dump="${BACKUP_DIR}/mariadb/daily/${DB_NAME}-${timestamp}.sql.gz.gpg"
docker compose --env-file "${env_file}" -f "${compose_file}" exec -T \
  -e MYSQL_PWD="${DB_PASSWORD}" mariadb \
  mariadb-dump --single-transaction --routines --triggers -u"${DB_USERNAME}" "${DB_NAME}" \
  | gzip -9 > "${tmp_dump}"
gpg --batch --yes --symmetric --cipher-algo AES256 --passphrase-file "${BACKUP_PASSPHRASE_FILE}" \
  --output "${dump}" "${tmp_dump}"
object_inventory="${BACKUP_DIR}/object-storage/daily/${storage_bucket}-${timestamp}.json.gz.gpg"
aws_args=(s3api list-objects-v2 --bucket "${storage_bucket}" --output json)
if [ -n "${storage_endpoint}" ]; then
  aws_args+=(--endpoint-url "${storage_endpoint}")
fi
AWS_ACCESS_KEY_ID="${storage_access_key}" \
AWS_SECRET_ACCESS_KEY="${storage_secret_key}" \
AWS_DEFAULT_REGION="${storage_region}" \
aws "${aws_args[@]}" \
  | gzip -9 > "${tmp_inventory}"
gpg --batch --yes --symmetric --cipher-algo AES256 --passphrase-file "${BACKUP_PASSPHRASE_FILE}" \
  --output "${object_inventory}" "${tmp_inventory}"

if [ "$(date -u +%u)" = 7 ]; then
  cp --reflink=auto "$dump" "${BACKUP_DIR}/mariadb/weekly/"
  cp --reflink=auto "$object_inventory" "${BACKUP_DIR}/object-storage/weekly/"
fi
if [ "$(date -u +%d)" = 01 ]; then
  cp --reflink=auto "$dump" "${BACKUP_DIR}/mariadb/monthly/"
  cp --reflink=auto "$object_inventory" "${BACKUP_DIR}/object-storage/monthly/"
fi

find "${BACKUP_DIR}/mariadb/daily" -type f -mtime +31 -delete
find "${BACKUP_DIR}/mariadb/weekly" -type f -mtime +84 -delete
find "${BACKUP_DIR}/mariadb/monthly" -type f -mtime +370 -delete
find "${BACKUP_DIR}/object-storage/daily" -type f -mtime +31 -delete
find "${BACKUP_DIR}/object-storage/weekly" -type f -mtime +84 -delete
find "${BACKUP_DIR}/object-storage/monthly" -type f -mtime +370 -delete
publish_backup_metric <<EOF
# TYPE souklab_backup_last_success_timestamp_seconds gauge
souklab_backup_last_success_timestamp_seconds $(date +%s)
EOF
echo "backup complete: ${dump}"
