#!/usr/bin/env bash
set -euo pipefail

# Start the app against the retained verification compose services. The
# project .env may target a separate developer database on port 3306; these
# explicit local-only defaults match the verification containers on port 3307.
export DB_NAME="${VERIFY_DB_NAME:-souklab_test}"
export DB_USERNAME="${VERIFY_DB_USERNAME:-souklab_test}"
export DB_PASSWORD="${VERIFY_DB_PASSWORD:-souklab_test_password}"
export MARIADB_HOST_PORT="${VERIFY_DB_PORT:-3307}"
export DB_URL="jdbc:mariadb://localhost:${MARIADB_HOST_PORT}/${DB_NAME}?createDatabaseIfNotExist=true&useSsl=false&serverTimezone=UTC"
export JPA_HIBERNATE_DDL_AUTO="${JPA_HIBERNATE_DDL_AUTO:-update}"

exec ./mvnw spring-boot:run "$@"
