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
while IFS= read -r java_file; do
  duplicate_imports="$(sed -n 's/^import //p' "$java_file" | sort | uniq -d)"
  if [ -n "$duplicate_imports" ]; then
    echo "duplicate imports detected in $java_file:" >&2
    printf '%s\n' "$duplicate_imports" >&2
    exit 1
  fi
done < <(rg --files src/main/java src/test/java -g '*.java')
if rg -n --pcre2 'Permission\.(?:ADMIN_USERS|ADMIN_FORMATIONS|ADMIN_FEED|ADMIN_REPORTS|FINANCIAL_ADMIN|ARTISAN_FORMATIONS|ARTISAN_CONTENT|ARTISAN_REVIEWS|PROFILE_READ|PROFILE_WRITE|REPORT_CREATE|FILE_READ|MESSAGE_SEND|ANALYTICS_ADMIN)|Permission\.values\(|Permission\.valueOf\(' src/main/java src/test/java --glob '*.java'; then
  echo 'flat permission enum references detected; use grouped Permission enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsEvent\.(?:Authentication\.LOGIN_SUCCEEDED|Feed\.POST_PUBLISHED|Formation\.MODERATION_(?:APPROVED|REJECTED)|Payment\.STATE_TRANSITION|Source\.(?:PAYMENT_WEBHOOK|ACCOUNT_ACTION|ADMIN_ACTION|ADMIN_CORRECTION))|FinancialAuditOperation\.(?:MANUAL_GRANT|STATE_CORRECTION|REVOKE|CANCEL)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics taxonomy references detected; use grouped event and operation types' >&2
  exit 1
fi
if rg -n --pcre2 '"(?:PLAN_(?:CREATE|UPDATE|DEACTIVATE)|REFUND_REQUEST)"' src/main/java src/test/java --glob '*.java' \
    --glob '!FinancialAuditOperation.java'; then
  echo 'raw financial audit operation literals detected; use FinancialAuditOperation grouped enums' >&2
  exit 1
fi
if rg -n '\.authority\(\)' src/main/java src/test/java --glob '*.java'; then
  echo 'string authority adapter usage detected; use grouped Permission values and matches' >&2
  exit 1
fi
if rg -n '\.name\(\)' src/main/java src/test/java --glob '*.java'; then
  echo 'enum name string conversion detected; use typed enum values or an explicit boundary adapter' >&2
  exit 1
fi
if rg -n 'private String permissionKey' src/main/java/com/project/souklab/dto src/test/java --glob '*.java'; then
  echo 'permission request fields must use grouped Permission enums' >&2
  exit 1
fi
if rg -n 'Map<String, String> filters' src/main/java/com/project/souklab/dto --glob '*.java'; then
  echo 'analytics filter fields must use AnalyticsFilterKey enums' >&2
  exit 1
fi
if rg -n --pcre2 '"(?:current|previous|historical\.(?:registrations|feed_posts|formations|enrollments|reviews|reports|payments)|event\.)"' \
    src/main/java/com/project/souklab/analytics --glob '*.java' \
    --glob '!AnalyticsMetric.java'; then
  echo 'raw analytics contract keys detected; use grouped AnalyticsMetric enums' >&2
  exit 1
fi
if rg -n --pcre2 'Map\.of\("(?:accountType|subscriberType|reasonPresent|minutes|decision|postType|formationId|targetType|action|rating|status|previousStatus|source|paymentStatus|providerEvent|payment_id|subscription_id)"' \
    src/main/java/com/project/souklab/service src/main/java/com/project/souklab/analytics --glob '*.java'; then
  echo 'raw analytics metadata keys detected; use grouped AnalyticsMetadata enums' >&2
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
