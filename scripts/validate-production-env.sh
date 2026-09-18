#!/usr/bin/env bash
set -euo pipefail

env_file="${1:-deploy/.env.production}"
test -f "$env_file" || { echo "missing production environment file: $env_file" >&2; exit 1; }

if [ "$(stat -c '%a' "$env_file")" != "600" ]; then
  echo "production environment file must have mode 600: $env_file" >&2
  exit 1
fi

required=(APP_IMAGE PUBLIC_DOMAIN GRAFANA_ADMIN_PASSWORD DB_NAME DB_USERNAME DB_PASSWORD DB_ROOT_PASSWORD
  RABBITMQ_DEFAULT_USER RABBITMQ_DEFAULT_PASS REDIS_PASSWORD APP_JWT_SECRET
  APP_RATE_LIMIT_BACKEND APP_RATE_LIMIT_KEY_PREFIX APP_ADMIN_DEFAULT_EMAIL APP_ADMIN_DEFAULT_PASSWORD
  STORAGE_PROVIDER STORAGE_S3_BUCKET STORAGE_S3_ACCESS_KEY STORAGE_S3_SECRET_KEY
  STORAGE_VIRUS_SCAN_ENABLED STORAGE_VIRUS_SCAN_FAIL_OPEN HIBERNATE_SEARCH_ENABLED
  HIBERNATE_SEARCH_SCHEMA_MANAGEMENT APP_ADMIN_BOOTSTRAP_ENABLED APP_CORS_ALLOWED_ORIGINS)

for name in "${required[@]}"; do
  value="$(sed -n "s/^${name}=//p" "$env_file" | tail -n 1)"
  test -n "$value" || { echo "missing required production setting: $name" >&2; exit 1; }
  case "$value" in
    *replace-with*|*your-org*|*example.com*)
      echo "placeholder remains in production setting: $name" >&2
      exit 1
      ;;
  esac
done

image="$(sed -n 's/^APP_IMAGE=//p' "$env_file" | tail -n 1)"
case "$image" in
  *@sha256:*) ;;
  *) echo 'APP_IMAGE must use an immutable sha256 digest' >&2; exit 1 ;;
esac

grep -q '^STORAGE_S3_AUTO_CREATE_BUCKET=false$' "$env_file" || {
  echo 'STORAGE_S3_AUTO_CREATE_BUCKET must be false in production' >&2; exit 1;
}
grep -q '^APP_ADMIN_BOOTSTRAP_ENABLED=false$' "$env_file" || {
  echo 'APP_ADMIN_BOOTSTRAP_ENABLED must be false in production' >&2; exit 1;
}
grep -q '^STORAGE_VIRUS_SCAN_FAIL_OPEN=false$' "$env_file" || {
  echo 'STORAGE_VIRUS_SCAN_FAIL_OPEN must be false in production' >&2; exit 1;
}
grep -q '^APP_RATE_LIMIT_BACKEND=redis$' "$env_file" || {
  echo 'APP_RATE_LIMIT_BACKEND must be redis in production' >&2; exit 1;
}

echo "production environment validated: $env_file"
