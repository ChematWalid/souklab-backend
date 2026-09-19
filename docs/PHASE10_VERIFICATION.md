# Phase 10 verification evidence

## Local Docker-backed verification

Run on 2026-09-19 from the reviewed working tree:

```text
./scripts/verify-local-integration.sh
```

The verification harness provisioned isolated MariaDB 11.4, RabbitMQ 4.0,
Redis 7.4, MinIO, Elasticsearch 8.15, and ClamAV containers. It then:

- validated and applied Flyway migrations V1 through V12 to a fresh schema;
- ran the MariaDB migration, RabbitMQ delivery, and Redis rate-limit integration tests;
- exercised MinIO-backed storage tests;
- ran the complete Maven test suite with JaCoCo reporting.

Result:

```text
Tests run: 1205, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS
```

The single skipped test is the existing opt-in Phase 9 migration test. The
verification containers and volumes were removed by the harness cleanup trap.

## Scope boundary

This is local acceptance evidence, not production-release approval. Hosted CI,
dependency scanning, production backup/restore evidence, immutable image
digests, deployed readiness, Prometheus scraping, and cross-instance rate-limit
checks still require the production environment and remain unchecked in
`RELEASE_CHECKLIST.md` until an operator records them.
