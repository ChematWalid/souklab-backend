# Souklab documentation

These are the current API, architecture, deployment, configuration, and
verification documents for Souklab. Historical phase notes, implementation
history, deferred work, and migration references are in [`dev/`](dev/README.md)
and are not the current production contract.

## Current authority

- **[Business features guide (EN)](BUSINESS_FEATURES.md)** / **[(FR)](BUSINESS_FEATURES_FR.md)** — plain-language description of every platform capability: what it does, who it serves, and why. No technical jargon. / Guide en langage clair des fonctionnalités.
- **[Technical feature reference (EN)](FEATURES.md)** / **[(FR)](FEATURES_FR.md)** — code-derived reference: all endpoints, permissions, enums, business rules, config, and verification ledger. / Référence technique dérivée du code source.
- [Frontend API handoff](frontend/API_HANDOFF.md)
- [API conventions](API_CONVENTIONS.md)
- [API specification](API_SPEC.md)
- [Authorization matrix](AUTHORIZATION_MATRIX.md)
- [Architecture](ARCHITECTURE.md)
- **[Architecture codemaps](CODEMAPS/README.md)** — token-lean architecture, routes, data model, and dependency maps.
- [Production configuration matrix](PRODUCTION_CONFIGURATION_MATRIX.md)
- [Deployment runbook](DEPLOYMENT_RUNBOOK.md)
- [Database schema](DATABASE_SCHEMA.md)
- [OpenAPI reference](API_OPENAPI.md)
- [Verification report](VERIFICATION_REPORT.md)
- [Verification coverage matrix](VERIFICATION_COVERAGE_MATRIX.md)
- [Chargily Test Mode procedure](CHARGILY_MANUAL_TEST.md)

## Generated contract

Springdoc exposes the code-first contract at `/v3/api-docs`. Export it with:

```bash
./scripts/export-openapi.sh http://localhost:8080/v3/api-docs docs/generated/openapi.json
./scripts/openapi-to-markdown.sh docs/generated/openapi.json docs/API_OPENAPI.md
```

The JSON export is intentionally not committed. The Markdown companion is the
reviewable API reference and should be regenerated whenever the live contract
changes.

## Verification entry points

- `scripts/full-verification.sh` runs the retained end-to-end workflow.
- `scripts/verify-local-integration.sh` runs the Docker-backed Maven gates.
- `scripts/verify-live-semantic.py` exercises live REST boundaries.
- `scripts/verify-live-stomp.py` and `scripts/verify-live-chat.py` exercise
  authenticated STOMP/WebSocket behavior.
- `scripts/verify-chargily-local-e2e.sh` covers local provider payment paths.
- `scripts/verify-chargily-callback.sh` reconciles hosted Test Mode callbacks.
