<!-- Generated: 2026-09-22 | Files scanned: 472 | Token estimate: ~450 -->

# Souklab Architecture Codemaps

Token-lean, high-density architecture documentation designed for both human engineers and AI agent context loading.

---

## Codemap Index

| Codemap | Focus Area | Description |
|---|---|---|
| [**`architecture.md`**](architecture.md) | High-Level System Architecture | Topology, runtime layers, cross-cutting filters, asynchronous execution, and data flow. |
| [**`backend.md`**](backend.md) | API Routes & Service Execution | Security filter chain, route-to-controller mapping, service delegation, and repository bindings. |
| [**`data.md`**](data.md) | Data Models & Migrations | 50 JPA entities grouped by domain, table constraints, relationships, and Flyway V0–V16 ledger. |
| [**`dependencies.md`**](dependencies.md) | Infrastructure & Integrations | External services (MariaDB, RabbitMQ, Redis, MinIO, ClamAV, Elasticsearch), 3rd-party APIs, and dependencies. |

---

## Usage Guidelines

- **For AI Agents**: Load these codemaps into context instead of crawling dozens of controllers or entity files. Each codemap is strictly under 1,000 tokens.
- **For Developers**: Use as a rapid reference for route contracts, layer separation, database tables, and external configuration requirements.
- **Updating**: Run `/update-codemaps` to regenerate when new modules, routes, or entities are added.
