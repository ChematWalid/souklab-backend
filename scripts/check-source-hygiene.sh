#!/usr/bin/env bash
set -euo pipefail

source_roots=(src/main/java src/test/java src/main/resources/db/migration scripts deploy)
if rg -n '[[:blank:]]+$' "${source_roots[@]}"; then
  echo 'trailing whitespace detected' >&2
  exit 1
fi
if rg -n $'\t' src/main/java src/test/java scripts; then
  echo 'tab indentation detected' >&2
  exit 1
fi
if rg -n --pcre2 '(?<![\w.])(?:java|org|jakarta|lombok|com)(?:\.[A-Za-z_][\w$]*){2,}' src/main/java src/test/java --glob '*.java' \
    | rg -v ':package |:import '; then
  echo 'inline fully qualified Java references detected; import types at the top of the file' >&2
  exit 1
fi
if rg -n --pcre2 'Permission\.(?:ADMIN_USERS|ADMIN_FORMATIONS|ADMIN_FEED|ADMIN_REPORTS|FINANCIAL_ADMIN|ARTISAN_FORMATIONS|ARTISAN_CONTENT|ARTISAN_REVIEWS|PROFILE_READ|PROFILE_WRITE|REPORT_CREATE|FILE_READ|MESSAGE_SEND|ANALYTICS_ADMIN)|Permission\.values\(|Permission\.valueOf\(' src/main/java src/test/java --glob '*.java'; then
  echo 'flat permission enum references detected; use grouped Permission enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsEvent\.(?:Authentication\.LOGIN_SUCCEEDED|Feed\.POST_PUBLISHED|Formation\.MODERATION_(?:APPROVED|REJECTED)|Payment\.STATE_TRANSITION|Source\.(?:PAYMENT_WEBHOOK|ACCOUNT_ACTION|ADMIN_ACTION|ADMIN_CORRECTION))|FinancialAuditOperation\.(?:MANUAL_GRANT|STATE_CORRECTION|REVOKE|CANCEL)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics taxonomy references detected; use grouped event and operation types' >&2
  exit 1
fi
if rg -n '\.authority\(\)' src/main/java src/test/java --glob '*.java'; then
  echo 'string authority adapter usage detected; use grouped Permission values and matches' >&2
  exit 1
fi

mapfile -t migrations < <(find src/main/resources/db/migration -maxdepth 1 -type f -name 'V*__*.sql' -printf '%f\n' | sort -V)
test "${#migrations[@]}" -gt 0 || { echo 'no Flyway migrations found' >&2; exit 1; }
versions="$(printf '%s\n' "${migrations[@]}" | sed -E 's/^V([0-9]+)__.*$/\1/' | sort -n)"
test "$(printf '%s\n' "$versions" | uniq -d)" = '' || {
  echo 'duplicate Flyway migration versions found' >&2
  exit 1
}
while read -r version; do
  test -n "$(find src/main/resources/db/migration -maxdepth 1 -type f -name "V${version}__*.sql" -size +0c -print -quit)" || {
    echo "empty Flyway migration V${version}" >&2
    exit 1
  }
done <<< "$versions"

test -f deploy/.env.production.example || {
  echo 'production environment template is missing' >&2
  exit 1
}
if git check-ignore -q deploy/.env.production.example; then
  echo 'production environment template must not be ignored' >&2
  exit 1
fi

if git ls-files | grep -E '(^|/)(\.env$|.*\.(key|pem|p12|jks)$)' >/dev/null; then
  echo 'tracked secret material detected' >&2
  exit 1
fi
echo 'source hygiene and migration checks passed'
