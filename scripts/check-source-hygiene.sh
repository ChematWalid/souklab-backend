#!/usr/bin/env bash
set -euo pipefail

source_roots=(src/main/java src/test/java src/main/resources/db/migration scripts deploy)
command -v rg >/dev/null 2>&1 || {
  echo 'source hygiene requires ripgrep (rg); install ripgrep or use the CI dependency check' >&2
  exit 127
}
bash -n scripts/full-verification.sh scripts/verify-chargily-callback.sh scripts/run-local-verification-app.sh
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
if rg -n --pcre2 'AnalyticsMetric\.Operational\.(?:ANALYTICS_JOBS_|MAINTENANCE_JOBS_|OUTBOX_|APPLICATION_HEALTH|HEALTH_COMPONENTS|REQUEST_COUNTERS|RATE_LIMIT_REJECTIONS|REQUEST_COUNTER_METRIC|RATE_LIMIT_REJECTION_METRIC)|AnalyticsMetric\.Retention\.(?:DAY_[0-9]+|Row\.DAY_)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics metric references detected; use grouped operational and retention enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetric\.Operational\.Metric\.(?:UPLOADS|VIRUS_SCANS|SEARCH_REQUESTS|WEBSOCKET_CONNECTIONS|RATE_LIMIT_REJECTIONS|HTTP_REQUESTS)|RateLimitScope\.Endpoint\.(?:CSV_EXPORTS|ANALYTICS|AUTHENTICATION|ADMINISTRATION|PUBLIC_API)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat operational metric or rate-limit scope references detected; use grouped enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsReportType\.(?:OVERVIEW|GROWTH|ENGAGEMENT|MODERATION|CONTENT_LEARNING|SUBSCRIPTIONS_PAYMENTS|OPERATIONAL|TIME_SERIES|CSV_EXPORT)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics report type references detected; use grouped report enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetric\.Series\.(?:START_DATE|END_DATE|ACTIVITY_EVENTS|UNIQUE_ACTORS|NEW_REGISTRATIONS)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics series references detected; use grouped series enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetric\.Result\.(?:REPORT_TYPE|FROM_DATE|TO_DATE|BUCKET|SUMMARY|TABLES|SERIES|CONTENT)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics result references detected; use grouped result enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetric\.Summary\.User\.(?:TOTAL|NEW_REGISTRATIONS|VERIFIED_REGISTRATIONS|ACTIVATION_RATE|VERIFIED|ARTISAN_PROFILES|CLIENT_PROFILES|ACTIVE_ARTISAN_PROFILES|ACTIVE_CLIENT_PROFILES|ACTIVE|PENDING|SUSPENDED|PENDING_APPROVALS|STATUSES)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics user summary references detected; use grouped summary enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetric\.Summary\.Engagement\.(?:ACTIVITY_EVENTS|SUCCESSFUL_LOGINS|PUBLISHED_POSTS|MESSAGES_SENT|PROFILE_VIEWS|REPORT_RESOLUTIONS|DAU|WAU|MAU|BY_ACCOUNT_TYPE|BY_REGION|BY_CRAFT_CATEGORY|LOGIN_RETENTION_COHORTS)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics engagement summary references detected; use grouped summary enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetric\.Summary\.(?:Content|Formation|Moderation|Report|Payment|Subscription|General)\.[A-Z][A-Z0-9_]+' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics summary references detected; use grouped summary enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetadata\.(?:Account\.SUBSCRIBER_TYPE|Moderation\.(?:REASON_PRESENT|MINUTES|DECISION)|Content\.(?:POST_TYPE|FORMATION_ID|TARGET_TYPE|ACTION|RATING)|Message\.CONVERSATION_ID|Subscription\.(?:PLAN_ID|PREVIOUS_STATUS|SOURCE)|Payment\.(?:STATUS|PROVIDER_EVENT|ID)|Provider\.SUBSCRIPTION_ID|Audit\.(?:JOB_ID|MAINTENANCE_JOB_ID|REPORT_TYPE|OPERATION|RANGE|FILTERS|PERMISSION_SCOPE|OUTCOME))' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics metadata references detected; use grouped metadata enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetric\.Payload\.(?:EVENT_ID|EVENT_TYPE|EVENT_TIME|ACTOR_ID|SUBJECT_ID|METADATA)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics payload references detected; use grouped payload enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsMetric\.Historical\.(?:REGISTRATIONS|FEED_POSTS|FORMATIONS|ENROLLMENTS|REVIEWS|REPORTS|PAYMENTS)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics historical references detected; use grouped historical enums' >&2
  exit 1
fi
if rg -n --pcre2 'AnalyticsEvent\.User\.TIMED_OUT' src/main/java src/test/java --glob '*.java'; then
  echo 'flat analytics timeout event references detected; use grouped event enums' >&2
  exit 1
fi
if rg -n --pcre2 'NotificationType\.(?:Subscription\.(?:RENEWAL_REMINDER|MANUALLY_GRANTED)|Refund\.REQUEST_UNAVAILABLE|Formateur\.REQUEST_SUBMITTED)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat notification references detected; use grouped notification enums' >&2
  exit 1
fi
if rg -n --pcre2 'DirectorySortOrder\.(?:RELEVANCE|RATING_DESC|REVIEWS_DESC|VIEWS_DESC|NEWEST)|DirectorySortOrder\.values\(' src/main/java src/test/java --glob '*.java'; then
  echo 'flat directory sort references detected; use grouped sort enums' >&2
  exit 1
fi
if rg -n --pcre2 'NotificationType\.(?:ACCOUNT_VALIDATED|ACCOUNT_REJECTED|ACCOUNT_SUSPENDED|ACCOUNT_REINSTATED|FORMATION_APPROVED|FORMATION_REJECTED|NEW_FORMATION|NEW_MESSAGE|SUBSCRIPTION_RENEWED|SUBSCRIPTION_EXPIRED|SUBSCRIPTION_RENEWAL_REMINDER|SUBSCRIPTION_MANUALLY_GRANTED|SUBSCRIPTION_REVOKED|PAYMENT_SUCCESS|PAYMENT_FAILED|CHECKOUT_CREATED|CHECKOUT_CANCELED|REFUND_REQUEST_UNAVAILABLE|NEW_REPORT|NEW_REVIEW|FORMATEUR_REQUEST_SUBMITTED|FORMATEUR_APPROVED|FORMATEUR_GRANTED|FORMATEUR_REJECTED|FORMATEUR_REVOKED)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat notification type references detected; use grouped notification enums' >&2
  exit 1
fi
if rg -n --pcre2 'AuditLogAction\.(?:EMAIL_VERIFIED|PASSWORD_RESET_COMPLETED|PASSWORD_CHANGED|ASSIGN_ROLE|ASSIGN_PERMISSION_BULK|PERMISSION_GRANTED|PERMISSION_REVOKED|APPROVE_USER|BAN_USER|TIMEOUT_USER|UNBAN_USER|APPROVE_ARTISAN|REJECT_ARTISAN|APPROVE_FORMATION|REJECT_FORMATION|RESOLVE_REPORT|DISMISS_REPORT|SUBSCRIPTION_GRANTED|SUBSCRIPTION_CANCELED|SUBSCRIPTION_REVOKED|SUBSCRIPTION_STATE_CORRECTED|PAYMENT_STATE_CORRECTED|REFUND_REQUEST_REJECTED|SUBSCRIPTION_PLAN_CREATED|SUBSCRIPTION_PLAN_UPDATED|SUBSCRIPTION_PLAN_DEACTIVATED|ANALYTICS_REBUILD|ANALYTICS_JOB_SUBMITTED|ANALYTICS_RESULT_READ|ANALYTICS_EXPORT)' src/main/java src/test/java --glob '*.java'; then
  echo 'flat audit action references detected; use grouped audit action enums' >&2
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
if rg -n --pcre2 'Map\.of\("(?:username|online|typing|reader|messageId|message)"' \
    src/main/java/com/project/souklab/service/chat src/main/java/com/project/souklab/controller/chat --glob '*.java'; then
  echo 'raw chat metadata keys detected; use grouped ChatMetadata enums' >&2
  exit 1
fi
if rg -n --pcre2 'ApiResponse\.error\("(?:FORBIDDEN|VIRUS_DETECTED|VIRUS_SCAN_UNAVAILABLE|FILE_TOO_LARGE|MALFORMED_REQUEST|RESOURCE_NOT_FOUND|METHOD_NOT_ALLOWED|AUTHENTICATION_FAILED|MISSING_PARAMETER|INVALID_PARAMETER|UNSUPPORTED_MEDIA_TYPE|MAX_UPLOAD_SIZE_EXCEEDED|INTERNAL_SERVER_ERROR)"' \
    src/main/java --glob '*.java'; then
  echo 'raw standardized API error codes detected; use ApiErrorCode enums' >&2
  exit 1
fi
if rg -n --pcre2 '"(?:authz_version|SOUKLAB_OAUTH_INTENT)"' \
    src/main/java src/test/java --glob '*.java' \
    --glob '!JwtClaim.java' --glob '!OAuthCookie.java'; then
  echo 'raw authentication protocol keys detected; use JwtClaim or OAuthCookie enums' >&2
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
python3 scripts/validate-audit-action-schema.py

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
