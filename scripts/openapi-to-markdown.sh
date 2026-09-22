#!/usr/bin/env bash
set -euo pipefail

input_file="${1:-docs/generated/openapi.json}"
output_file="${2:-docs/API_OPENAPI.md}"

command -v jq >/dev/null 2>&1 || { echo "jq is required" >&2; exit 1; }
test -s "$input_file" || { echo "OpenAPI input is missing: $input_file" >&2; exit 1; }
jq -e '(.openapi | type == "string") and (.paths | type == "object")' "$input_file" >/dev/null

mkdir -p "$(dirname "$output_file")"
{
  echo "# Souklab OpenAPI contract"
  echo
  jq -r '"Generated from the running application on " + (now | strftime("%Y-%m-%dT%H:%M:%SZ")) + ". This Markdown view is a human-readable companion to the machine-readable `/v3/api-docs` document."' "$input_file"
  echo
  jq -r '"- OpenAPI version: `" + .openapi + "`\n- API title: `" + (.info.title // "") + "`\n- API version: `" + (.info.version // "") + "`\n- Paths: `" + ((.paths | length) | tostring) + "`\n- Schemas: `" + ((.components.schemas // {}) | length | tostring) + "`"' "$input_file"
  echo
  echo "## Security"
  echo
  jq -r 'if (.components.securitySchemes // {}) | length == 0 then "No security schemes were published." else (.components.securitySchemes | to_entries[] | "### `" + .key + "`\n\n```json\n" + (.value | tojson) + "\n```\n") end' "$input_file"
  echo
  echo "## Endpoints"
  echo
  jq -r '
    .paths | to_entries[] |
    "### `" + .key + "`\n\n" +
    (.value | to_entries[] | select(.key | IN("get","post","put","patch","delete","head","options","trace")) |
      "#### " + (.key | ascii_upcase) + " — " + (.value.summary // .value.operationId // "Operation") + "\n\n" +
      "- Operation ID: `" + (.value.operationId // "not specified") + "`\n" +
      "- Tags: `" + ((.value.tags // []) | join(", ")) + "`\n" +
      (if (.value.parameters // []) | length > 0 then "- Parameters:\n" + ((.value.parameters // []) | map("  - `" + .name + "` (`" + .in + "`, " + (if .required then "required" else "optional" end) + ")") | join("\n")) + "\n" else "" end) +
      (if .value.requestBody then "- Request body: `" + ((.value.requestBody.content // {}) | keys | join(", ")) + "`\n" else "" end) +
      "- Responses:\n" + ((.value.responses // {}) | to_entries | map("  - `" + .key + "` — " + (.value.description // "")) | join("\n")) + "\n")
  ' "$input_file"
  echo "## Schemas"
  echo
  jq -r '(.components.schemas // {}) | to_entries[] | "### `" + .key + "`\n\n```json\n" + (.value | tojson) + "\n```\n"' "$input_file"
} > "$output_file"

echo "OpenAPI Markdown written to $output_file"
