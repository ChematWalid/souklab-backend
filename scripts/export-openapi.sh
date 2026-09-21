#!/usr/bin/env bash
set -euo pipefail

base_url="${1:-http://localhost:8080/v3/api-docs}"
output_file="${2:-docs/generated/openapi.json}"
mkdir -p "$(dirname "$output_file")"
curl --fail --silent --show-error --location "$base_url" --output "$output_file"
test -s "$output_file" || { echo "OpenAPI response is empty: $output_file" >&2; exit 1; }
if command -v jq >/dev/null 2>&1; then
  jq -e '(.openapi | type == "string") and (.paths | type == "object")' "$output_file" >/dev/null
else
  rg -q '"openapi"[[:space:]]*:' "$output_file"
  rg -q '"paths"[[:space:]]*:' "$output_file"
fi
echo "OpenAPI exported to $output_file"
