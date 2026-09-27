#!/usr/bin/env bash
set -euo pipefail

python3 - <<'EOF'
import os
import re
import sys

properties_path = "src/main/resources/application.properties"
env_targets = [
    ".env.example",
    ".env.docker.example",
    "deploy/.env.production.example"
]

if not os.path.isfile(properties_path):
    print(f"Error: {properties_path} not found", file=sys.stderr)
    sys.exit(1)

with open(properties_path, "r", encoding="utf-8") as f:
    properties_content = f.read()

required_vars = set(re.findall(r"\$\{([A-Za-z0-9_]+)\}", properties_content))

if not required_vars:
    print(f"Error: No required environment variables detected in {properties_path}", file=sys.stderr)
    sys.exit(1)

has_drift = False

for env_file in env_targets:
    if not os.path.isfile(env_file):
        print(f"Error: Target env file {env_file} does not exist", file=sys.stderr)
        has_drift = True
        continue

    defined_vars = set()
    with open(env_file, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            match = re.match(r"^([A-Za-z0-9_]+)=", line)
            if match:
                defined_vars.add(match.group(1))

    missing = sorted(list(required_vars - defined_vars))
    if missing:
        has_drift = True
        print(f"Drift detected in {env_file}! Missing {len(missing)} variables:", file=sys.stderr)
        for var in missing:
            print(f"  - {var}", file=sys.stderr)
    else:
        print(f"OK: {env_file} has all {len(required_vars)} required variables.")

if has_drift:
    sys.exit(1)

print("Environment drift check passed successfully.")
EOF
