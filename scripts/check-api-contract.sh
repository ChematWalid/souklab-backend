#!/usr/bin/env bash
set -euo pipefail

controller_root="src/main/java/com/project/souklab/controller"
test -d "$controller_root"
while IFS= read -r file; do
  if ! rg -q '/api/v1|AVATAR_UPLOAD_URI' "$file"; then
    echo "REST controller has no /api/v1 mapping: $file" >&2
    exit 1
  fi
done < <(rg -l '@RestController' "$controller_root" --glob '*.java')
rg -q '^> \*\*Status: archival reference\.\*\*' docs/devdocs/POSTMAN_API_REFERENCE.md
test -f docs/current/frontend/API_HANDOFF.md
test -x scripts/export-openapi.sh
echo 'API contract checks passed'
