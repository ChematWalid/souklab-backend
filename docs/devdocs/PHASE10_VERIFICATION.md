# Phase 10 verification evidence

## Latest local release-gate run

On 2026-09-21, `scripts/verify-local-integration.sh` completed against fresh
MariaDB 11.4, RabbitMQ 4.0/STOMP, Redis 7.4, MinIO, Elasticsearch 8.15, and
ClamAV services:

```text
Tests run: 1224, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The analytics RabbitMQ wire contract uses canonical `eventId`, `eventType`,
and ISO-8601 textual `eventTime` values. The producer regression test verifies
the exact outbox JSON shape; the consumer also accepts legacy Jackson
`LocalDateTime` arrays while old messages drain. The focused producer,
consumer, and taxonomy tests passed before the complete suite.

The dedicated local Chargily application scenario created a checkout through
the HTTP API, verified idempotent replay, processed a correctly signed
`checkout.paid` webhook, and observed `PAID`, `ACTIVE`, and `PROCESSED` state.
The same endpoint rejected an invalid signature with `403`; unknown checkout
events were safely ignored with `200`. No real credentials, card entry, or
money movement was used.

The repeatable form of this scenario is
`scripts/verify-chargily-local-e2e.sh`; it also supports separate prepared
test-user tokens for `checkout.failed` and `checkout.canceled` webhook paths.
Its report is sanitized TSV and never writes bearer tokens, signatures, or
response bodies.

Flyway validation passed through V13. The SMTP-isolated suite connected only
to the loopback sink at `127.0.0.1:1025`; verification containers, volumes,
and networks were removed by the harness cleanup trap. Live local checks also
confirmed that unauthenticated health/OpenAPI endpoints return `401`,
authenticated health returns `200`, the authenticated status sweep passed
profile/notification/subscription/payment/OpenAPI routes, the SockJS `/ws` and
`/ws/info` endpoints returned `200`, and authenticated OpenAPI JSON was
exported and converted to `docs/current/API_OPENAPI.md`. The credential-free
Chargily provider returned the expected success (`200`), validation (`422`),
rate-limit (`429` then `200`), provider (`503`), and malformed (`200` with an
invalid shape) fixtures. The real sandbox script was run without credentials
and correctly classified the attempt as `BLOCKED_CREDENTIALS`; no checkout or
money-moving action occurred.

Dependency-Check passed locally with the configured CVSS 7 threshold using the
cached NVD data after upgrading the transitive Elasticsearch REST client and
sniffer to `9.3.8` (`265` dependencies, `11` findings, `3` report-applicable
exact-SHA suppression entries, and no remaining CVSS >= 7 build-breaking
finding). A fresh NVD update still requires `NVD_API_KEY`; hosted CI, real
Chargily credentials/provider access, and production backup/restore evidence
remain unavailable in this checkout.

## Local Docker-backed verification

Run on 2026-09-21 from the reviewed working tree:

```text
./scripts/verify-local-integration.sh
```

The verification harness provisioned isolated MariaDB 11.4, RabbitMQ 4.0,
Redis 7.4, MinIO, Elasticsearch 8.15, and ClamAV containers. It then:

- validated and applied Flyway migrations V1 through V13 to a fresh schema;
- enabled the application analytics RabbitMQ connection and verified the
  durable exchange/queue, publisher confirms, consumer delivery, and
  idempotency marker persistence;
- verified enum-bound analytics bucket configuration, startup validation, and
  disabled-bucket request rejection;
- verified analytics activity, moderation, instructor, and formation-utilization
  KPI code paths compile and load through the application context;
- verified the moderation-resolution timestamp migration and fresh-schema
  `resolved_at` invariant;
- ran the MariaDB migration, RabbitMQ delivery, and Redis rate-limit integration tests;
- exercised MinIO-backed storage tests;
- ran the complete Maven test suite with JaCoCo reporting.

Result:

```text
Tests run: 1224, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The MariaDB health checks include a startup grace period and extended retry
window so first-run database initialization on clean Docker volumes is not
reported as a false readiness failure.

The verification containers and volumes were removed by the harness cleanup
trap. The opt-in MariaDB invariant and private operational endpoint-security
tests were also run explicitly with
`PHASE9_MARIADB_INTEGRATION=true` and `PHASE10_ENDPOINT_SECURITY=true`:

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

This covered the Phase 9 MariaDB invariant test and all three private
operational endpoint-security tests.

The repository also contains `scripts/verify-live-stomp.py`, a dependency-free
SockJS/STOMP client verifier for authenticated CONNECT, subscription, optional
SEND/MESSAGE delivery, and disconnect behavior. It was syntax-checked locally;
an active deployed endpoint and safe message fixture are still required for a
runtime delivery result.

## Dependency scan status

The local OWASP Dependency-Check 13.0.0 run used `-DfailBuildOnCVSS=7` and
produced `target/dependency-check-report.html` with `BUILD SUCCESS`. The
transitive `org.elasticsearch.client:elasticsearch-rest-client` and
`...:elasticsearch-rest-client-sniffer` artifacts are pinned to `9.3.8`; the
remaining exact-SHA exceptions are limited to server-only Elastic/Tika matches.
Spring
Boot is 4.0.8; Tomcat is 11.0.26; Netty is 4.2.18.Final; HttpClient is 5.6.4;
HttpCore is 5.4.3; PDFBox is 3.0.8; and Tika core is 4.0.0. The remaining
below-threshold CPE matches are documented in the exact-SHA-1 suppression file
at `config/dependency-check-suppressions.xml`; the suppressions cover only
Tika modules and Elasticsearch server advisories that do not affect the exact
client artifacts used by this application. No CVE is suppressed globally.

The subsequent regional/craft engagement query check used the same Docker
harness with `-Dtest=Phase10ActivityDimensionRepositoryTest` and completed with
`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`; both JPQL aggregations
executed against MariaDB and returned the expected empty result for an empty
profile dataset.

## Scope boundary

This is local acceptance evidence, not production-release approval. Hosted CI,
dependency scanning, production backup/restore evidence, immutable image
digests, deployed readiness, Prometheus scraping, and cross-instance rate-limit
checks still require the production environment and remain unchecked in
`RELEASE_CHECKLIST.md` until an operator records them.
