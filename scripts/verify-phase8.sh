#!/usr/bin/env bash
set -Eeuo pipefail

# Repeatable local Phase 8 verification. Credentials and endpoints remain
# environment-driven through application.properties and Compose interpolation.
compose_project="${COMPOSE_PROJECT_NAME:-souklab-phase8-test}"
export COMPOSE_PROJECT_NAME="$compose_project"
mariadb_port="${PHASE8_MARIADB_HOST_PORT:-${MARIADB_HOST_PORT:-3307}}"
test_db_name="${PHASE8_DB_NAME:-souklab_phase8_test}"
test_db_user="${PHASE8_DB_USERNAME:-souklab_phase8}"
test_db_password="${PHASE8_DB_PASSWORD:-souklab_phase8_password}"
test_db_root_password="${PHASE8_DB_ROOT_PASSWORD:-souklab_phase8_root_password}"
test_rabbit_user="${PHASE8_RABBITMQ_USER:-souklab_phase8}"
test_rabbit_password="${PHASE8_RABBITMQ_PASSWORD:-souklab_phase8_password}"
test_minio_user="${PHASE8_MINIO_ACCESS_KEY:-minioadmin}"
test_minio_password="${PHASE8_MINIO_SECRET_KEY:-minioadmin_secret}"

# Export the exact values used by Compose before it interpolates the file.
# This prevents a developer's normal .env (or a production-like shell) from
# silently replacing the isolated verification database credentials.
export MARIADB_HOST_PORT="$mariadb_port"
export DB_NAME="$test_db_name"
export DB_USERNAME="$test_db_user"
export DB_PASSWORD="$test_db_password"
export DB_ROOT_PASSWORD="$test_db_root_password"
export RABBITMQ_DEFAULT_USER="$test_rabbit_user"
export RABBITMQ_DEFAULT_PASS="$test_rabbit_password"
export MINIO_ROOT_USER="$test_minio_user"
export MINIO_ROOT_PASSWORD="$test_minio_password"
export RELAY_HOST="${PHASE8_RELAY_HOST:-localhost}"
export RELAY_PORT="${PHASE8_RELAY_PORT:-61613}"
export RELAY_CLIENT_LOGIN="$test_rabbit_user"
export RELAY_CLIENT_PASSCODE="$test_rabbit_password"
export RELAY_SYSTEM_LOGIN="$test_rabbit_user"
export RELAY_SYSTEM_PASSCODE="$test_rabbit_password"

cleanup() {
  docker compose --project-name "$compose_project" down --remove-orphans --volumes
}
trap cleanup EXIT

docker compose --project-name "$compose_project" up -d

services=(mariadb rabbitmq minio clamav elasticsearch)
for service in "${services[@]}"; do
  for attempt in $(seq 1 "${DEPENDENCY_WAIT_ATTEMPTS:-60}"); do
    status="$(docker compose --project-name "$compose_project" ps --format '{{.Service}} {{.Health}}' "$service" 2>/dev/null || true)"
    if [[ "$status" == *"healthy"* ]]; then
      break
    fi
    if [[ "$attempt" == "${DEPENDENCY_WAIT_ATTEMPTS:-60}" ]]; then
      echo "Dependency did not become healthy: $service" >&2
      docker compose --project-name "$compose_project" logs "$service" >&2 || true
      exit 1
    fi
    sleep "${DEPENDENCY_WAIT_SECONDS:-5}"
  done
done

export DB_URL="${PHASE8_DB_URL:-jdbc:mysql://localhost:${mariadb_port}/${test_db_name}?createDatabaseIfNotExist=true}"
export DB_USERNAME="$test_db_user"
export DB_PASSWORD="$test_db_password"
export JPA_DATABASE_PLATFORM="${PHASE8_JPA_DATABASE_PLATFORM:-org.hibernate.dialect.MariaDBDialect}"
export STORAGE_PROVIDER="${PHASE8_STORAGE_PROVIDER:-in-memory}"
export HIBERNATE_SEARCH_ENABLED="${PHASE8_SEARCH_ENABLED:-true}"
export ELASTICSEARCH_URIS="${PHASE8_ELASTICSEARCH_URIS:-http://localhost:9200}"
export ELASTICSEARCH_USERNAME="${ELASTICSEARCH_USERNAME:-}"
export ELASTICSEARCH_PASSWORD="${ELASTICSEARCH_PASSWORD:-}"
export DIRECTORY_DEFAULT_PAGE_INDEX="${PHASE8_DIRECTORY_DEFAULT_PAGE_INDEX:-${DIRECTORY_DEFAULT_PAGE_INDEX:-0}}"
export DIRECTORY_DEFAULT_PAGE_SIZE="${PHASE8_DIRECTORY_DEFAULT_PAGE_SIZE:-${DIRECTORY_DEFAULT_PAGE_SIZE:-20}}"
export DIRECTORY_MIN_PAGE_SIZE="${PHASE8_DIRECTORY_MIN_PAGE_SIZE:-${DIRECTORY_MIN_PAGE_SIZE:-1}}"
export DIRECTORY_MAX_PAGE_SIZE="${PHASE8_DIRECTORY_MAX_PAGE_SIZE:-${DIRECTORY_MAX_PAGE_SIZE:-100}}"

# The main suite must retain the storage module's in-memory default and must
# be able to exercise its missing-S3-configuration fail-fast tests. Provider
# credentials are intentionally not exported into this process.
unset STORAGE_S3_ENDPOINT STORAGE_S3_BUCKET STORAGE_S3_ACCESS_KEY STORAGE_S3_SECRET_KEY
export STORAGE_S3_ACCESS_KEY=""
export STORAGE_S3_SECRET_KEY=""

./mvnw clean test

# Provider-specific verification is deliberately separate from the application
# suite so tests that assert the in-memory provider remain deterministic.
STORAGE_PROVIDER=in-memory \
./mvnw -Djacoco.skip=true \
  -Dtest=S3StorageServiceVerificationTest,ClamdIntegrationVerificationTest test
