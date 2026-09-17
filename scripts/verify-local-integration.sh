#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

# JaCoCo 0.8.12 supports the Java 17-21 class-file range; prefer the installed
# Java 21 runtime for reproducible coverage and to avoid instrumenting Java 26.
if [ -x /usr/lib/jvm/java-21-openjdk/bin/java ]; then
  export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

export DB_NAME="${DB_NAME:-souklab_test}"
export DB_USERNAME="${DB_USERNAME:-souklab_test}"
export DB_PASSWORD="${DB_PASSWORD:-souklab_test_password}"
export MARIADB_HOST_PORT="${MARIADB_HOST_PORT:-3306}"
if ss -ltn 2>/dev/null | awk '{print $4}' | grep -Eq '(^|:)3306$'; then
  export MARIADB_HOST_PORT="${MARIADB_FALLBACK_PORT:-3307}"
fi
# The application uses MySQL Connector/J; it is compatible with the MariaDB
# server supplied by this profile, but requires the jdbc:mysql URL scheme.
export DB_URL="${DB_URL:-jdbc:mysql://localhost:${MARIADB_HOST_PORT}/${DB_NAME}?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC}"
export JPA_DATABASE_PLATFORM="${JPA_DATABASE_PLATFORM:-org.hibernate.dialect.MariaDBDialect}"
export JPA_HIBERNATE_DDL_AUTO="${JPA_HIBERNATE_DDL_AUTO:-create-drop}"
export JPA_SHOW_SQL="${JPA_SHOW_SQL:-false}"
export JPA_FORMAT_SQL="${JPA_FORMAT_SQL:-false}"
export FLYWAY_ENABLED="${FLYWAY_ENABLED:-false}"
export PORT="${PORT:-8080}"
export APP_JWT_SECRET="${APP_JWT_SECRET:-local-test-secret-must-be-at-least-32-characters}"
export APP_JWT_ACCESS_EXP="${APP_JWT_ACCESS_EXP:-3600000}"
export APP_JWT_REFRESH_EXP="${APP_JWT_REFRESH_EXP:-86400000}"
export APP_STORAGE_UPLOADS="${APP_STORAGE_UPLOADS:-storage/uploads}"
export APP_STORAGE_THUMBNAILS="${APP_STORAGE_THUMBNAILS:-storage/thumbnails}"
export APP_STORAGE_INDEXES="${APP_STORAGE_INDEXES:-storage/indexes}"
export APP_ADMIN_DEFAULT_EMAIL="${APP_ADMIN_DEFAULT_EMAIL:-admin@souklab.test}"
export APP_ADMIN_DEFAULT_PASSWORD="${APP_ADMIN_DEFAULT_PASSWORD:-local-test-password}"
export APP_CORS_ALLOWED_ORIGINS="${APP_CORS_ALLOWED_ORIGINS:-http://localhost:3000}"
export RELAY_HOST="${RELAY_HOST:-localhost}"
export RELAY_PORT="${RELAY_PORT:-61613}"
export RELAY_CLIENT_LOGIN="${RELAY_CLIENT_LOGIN:-souklab_test}"
export RELAY_CLIENT_PASSCODE="${RELAY_CLIENT_PASSCODE:-souklab_test_password}"
export RELAY_SYSTEM_LOGIN="${RELAY_SYSTEM_LOGIN:-souklab_test}"
export RELAY_SYSTEM_PASSCODE="${RELAY_SYSTEM_PASSCODE:-souklab_test_password}"
export GOOGLE_OAUTH_CLIENT_ID="${GOOGLE_OAUTH_CLIENT_ID:-local-test-client}"
export GOOGLE_OAUTH_CLIENT_SECRET="${GOOGLE_OAUTH_CLIENT_SECRET:-local-test-secret}"
export GOOGLE_OAUTH_REDIRECT_URI="${GOOGLE_OAUTH_REDIRECT_URI:-http://localhost:8080/login/oauth2/code/google}"
# Keep the application default (in-memory) backend for the broad suite. The
# S3 verification class explicitly selects MinIO and must be able to test
# missing-property fail-fast behavior without inherited environment values.
export STORAGE_VIRUS_SCAN_ENABLED="true"
export STORAGE_VIRUS_SCAN_HOST="localhost"
export STORAGE_VIRUS_SCAN_PORT="3310"
export HIBERNATE_SEARCH_ENABLED="true"
export ELASTICSEARCH_URIS="http://localhost:9200"
export ELASTICSEARCH_VERSION="8.15"
export ELASTICSEARCH_VERSION_CHECK_ENABLED="false"
export HIBERNATE_SEARCH_SCHEMA_MANAGEMENT="create-or-update"
export SEARCH_SYNC_ON_STARTUP="true"

cleanup() {
  docker compose down --remove-orphans
}
trap cleanup EXIT

docker compose up -d mariadb rabbitmq minio elasticsearch clamav
docker compose ps

for service in mariadb rabbitmq minio elasticsearch clamav; do
  printf 'Waiting for %s...\n' "$service"
  deadline=$(( $(date +%s) + 300 ))
  until [ "$(docker compose ps --format '{{.Service}} {{.Health}}' | awk -v s="$service" '$1 == s {print $2}')" = "healthy" ]; do
    if [ "$(date +%s)" -ge "$deadline" ]; then
      docker compose ps
      docker compose logs --tail=80 "$service"
      exit 1
    fi
    sleep 3
  done
done

./mvnw clean test ${MAVEN_TEST_ARGS:-}
