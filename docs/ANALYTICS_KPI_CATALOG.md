# Analytics KPI Catalog

All analytics calendar boundaries use `app.analytics.business-time-zone`; persisted timestamps remain UTC. Empty aggregates return `0`, never `null`. Soft-deleted records are excluded. Job results include the selected report type, requested range, bucket, and page metadata.

| KPI | Formula | Source | Status/time rule |
|---|---|---|---|
| `totalUsers` | count of users | `users` | excludes deleted rows |
| `newRegistrations` | users created in `[from, to)` | `users.created_at` | UTC persistence converted from business-time boundary |
| `verifiedRegistrations` | verified users created in `[from, to)` | `users.email_verified`, `created_at` | verified flag must be true |
| `activeUsers` | users with `ACTIVE` status | `users.status` | current snapshot |
| `pendingUsers` | users with `PENDING` status | `users.status` | current snapshot |
| `suspendedUsers` | users with `SUSPENDED` status | `users.status` | current snapshot |
| `activityEvents` | events in `[from, to)` | `activity_events.event_time` | raw event retention applies |
| `successfulLogins` | `LOGIN_SUCCEEDED` events in range | `activity_events.event_type` | explicit events only |
| `messagesSent` | `MESSAGE_SENT` events in range | `activity_events.event_type` | explicit events only |
| `profileViews` | `PROFILE_VIEW` events in range | `activity_events.event_type` | explicit events only; zero when no views exist |
| `reportResolutions` | `REPORT_RESOLVED` events in range | `activity_events.event_type` | explicit moderation events only; zero when no resolutions exist |
| `dau` / `wau` / `mau` | distinct non-null event actors on the end date / trailing 7 / trailing 30 business-calendar days | `activity_events.actor_id`, `event_time` | system events without an actor are excluded; zero when no actors exist |
| `engagementByAccountType` | distinct event actors joined to an artisan or client profile | `activity_events.actor_id` joined to `users.artisan` / `users.client` | only persisted profile relations are counted; an actor may be absent from both segments |
| `loginRetentionCohorts` | registration cohort actors with a recorded successful login in the D1, D7, or D30 calendar windows | `REGISTRATION_CREATED` and `LOGIN_SUCCEEDED` events | cohorts are only built from stored events; missing historical interaction data is not inferred; zero rates when a cohort is empty |
| `uniqueActors` | distinct non-null event actors inside each requested series bucket | `activity_events.actor_id`, `event_time` | respects the optional `eventType` filter |
| `publishedPosts` | `FEED_POST_PUBLISHED` events in range | `activity_events.event_type` | explicit events only |
| `feedPostsCreated` | feed posts created in range | `feed_posts.created_at` | deleted rows excluded |
| `formationsCreated` | formations created in range | `formations.created_at` | deleted rows excluded |
| `formationEnrollments` | enrollments created in range | `formation_enrollments.created_at` | deleted rows excluded |
| `reviewsSubmitted` | reviews created in range | `artisan_reviews.created_at` | deleted rows excluded |
| `reportsSubmitted` | reports created in range | `content_reports.created_at` | deleted rows excluded |
| `moderationActivity` | approval, suspension, timeout, reinstatement, formation moderation, and report-resolution event counts | `activity_events.event_type` | explicit events only; zero when unavailable |
| `formationCompletions` | enrollments with `ATTENDED` status in range | `formation_enrollments.status`, `created_at` | existing status is the supported completion signal |
| `enrollmentCancellationRate` | canceled enrollments / all enrollments in range | `formation_enrollments.status`, `created_at` | zero when no enrollments exist |
| `paymentsCreated` | payments created in range | `payments.created_at` | does not imply cash collected |
| `grossCollectedDzd` | sum of `amount` for `PAID` DZD payments whose origin is not manual | `payments.amount`, `status`, `currency`, `manual_grant` | excludes manual-origin and non-DZD records, including corrected manual payments |
| `providerFeesDzd` | sum of `fees` for `PAID` DZD payments whose origin is not manual | `payments.fees`, `status`, `currency`, `manual_grant` | missing fees are persisted as zero; manual-origin payments are excluded |
| `netCollectedDzd` | gross collected minus provider fees | derived | may be zero; manually granted is excluded |
| `paymentConversionRate` | paid payments / (paid + failed + canceled) | `payments.status` and `created_at` | zero when denominator is zero |
| `manualGrants` | count of payments whose origin is manual in range | `payments.manual_grant`, `created_at` | remains visible after a later payment-status correction; shown separately from cash revenue |
| `subscriptionsBySubscriberType` | subscription status counts split between artisan and client repositories | `artisan_subscriptions` / `client_subscriptions` | current records created in the requested range; no cross-type inference |
| `subscriptionLifecycleEvents` | counts of activation, expiry, cancellation, revocation, and renewal events | `activity_events.event_type` | explicit events only; zero when unavailable |
| `checkoutCreated` / `paymentStateTransitions` | explicit checkout and payment-transition event counts | `activity_events.event_type` | state transitions are not treated as paid revenue; cash uses payment status and manual-origin rules above |

Period comparison uses an immediately preceding window with the same number of calendar days. Manual-origin payment records are not treated as collected cash revenue, regardless of later status corrections; financial detail requires both `permission:analytics:admin` and `permission:financial:admin`.

## Report families

`OVERVIEW` and `GROWTH` return shared summary cards and period comparisons. `ENGAGEMENT` returns activity metrics and bucketed series. `MODERATION` returns moderation-source counts. `CONTENT_LEARNING` returns feed, formation, enrollment, and review counts. `SUBSCRIPTIONS_PAYMENTS` is permission-gated and returns payment/subscription metrics. `TIME_SERIES` returns the requested bucketed series. `CSV_EXPORT` uses the same authorization scope as the source job.

Family-specific tables are returned under `tables`; each table has its own
`PaginatedResponse` page and does not consume the `series` page contents.

`OPERATIONAL` additionally exposes live analytics job/outbox state, application
health, sanitized dependency component statuses, maintenance-job state, bounded
HTTP request outcome counters, and rate-limit rejection counters sourced from
Micrometer; health detail payloads and long-term operational history remain
private and/or in Prometheus.

## Activity event taxonomy

Persisted event types include `REGISTRATION_CREATED`, `LOGIN_SUCCEEDED`,
`USER_APPROVED`, `USER_SUSPENDED`, `USER_TIMED_OUT`, `USER_REINSTATED`,
`PROFILE_VIEW`, `MESSAGE_SENT`, `FORMATION_SUBMITTED`,
`FORMATION_MODERATION_APPROVED`, `FORMATION_MODERATION_REJECTED`,
`FORMATION_PUBLISHED`, `FORMATION_ENROLLMENT`, `REVIEW_SUBMITTED`,
`REPORT_SUBMITTED`, `REPORT_RESOLVED`, `FEED_POST_PUBLISHED`,
`CHECKOUT_CREATED`, `PAYMENT_STATE_TRANSITION`,
`SUBSCRIPTION_ACTIVATED`, `SUBSCRIPTION_EXPIRED`, `SUBSCRIPTION_CANCELED`,
`SUBSCRIPTION_REVOKED`, and `SUBSCRIPTION_RENEWAL`. Metadata excludes credentials, tokens, webhook
bodies, and raw message content.
