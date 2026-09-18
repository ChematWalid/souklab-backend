#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"

: "${FLYWAY_MARIADB_HOST_PORT:?set FLYWAY_MARIADB_HOST_PORT}"
flyway_user="${FLYWAY_DB_USERNAME:-souklab_flyway}"
flyway_password="${FLYWAY_DB_PASSWORD:-souklab_flyway_password}"
flyway_database=souklab_flyway_verification

flyway_version="$(./mvnw -q help:evaluate -Dexpression=flyway.version -DforceStdout)"
flyway_url="jdbc:mariadb://127.0.0.1:${FLYWAY_MARIADB_HOST_PORT}/${flyway_database}?useSsl=false&serverTimezone=UTC"
flyway_args=(
  "-Dflyway.url=${flyway_url}"
  "-Dflyway.user=${flyway_user}"
  "-Dflyway.password=${flyway_password}"
  "-Dflyway.locations=filesystem:${project_dir}/src/main/resources/db/migration"
)

./mvnw --batch-mode --no-transfer-progress \
  "org.flywaydb:flyway-maven-plugin:${flyway_version}:migrate" "${flyway_args[@]}"
./mvnw --batch-mode --no-transfer-progress \
  "org.flywaydb:flyway-maven-plugin:${flyway_version}:validate" "${flyway_args[@]}"
echo "Flyway migration and validation passed: ${flyway_database}"
