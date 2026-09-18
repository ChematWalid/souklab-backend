# Production deployment

Copy `.env.production.example` to `.env.production`, fill every value from the secret store, and run `chmod 600 deploy/.env.production`. Never commit the resulting file.

The application runs Flyway on startup and validates the Hibernate schema. Redis is mandatory for production rate limiting and is the authoritative bucket store. Caddy is the only public entry point and handles TLS plus WebSocket upgrades.

```sh
chmod 600 deploy/.env.production
./scripts/validate-production-env.sh
docker compose --env-file deploy/.env.production -f deploy/compose.production.yml config
docker compose --profile bootstrap --env-file deploy/.env.production \
  -f deploy/compose.production.yml run --rm search-bootstrap
docker compose --env-file deploy/.env.production -f deploy/compose.production.yml up -d
docker compose --env-file deploy/.env.production -f deploy/compose.production.yml exec app \
  wget -qO- http://localhost:8080/actuator/health/readiness
```

Actuator routes are deliberately hidden by Caddy; readiness is checked from the
internal Compose network. Prometheus scrapes application and Caddy metrics on
the internal monitoring network. Grafana binds only to VPS loopback; access it
with `ssh -L 3000:127.0.0.1:3000 deploy@vps` and browse to `http://localhost:3000`.

The `search-bootstrap` profile is required only for a fresh Elasticsearch
volume. It runs with `create-or-update`; the long-running application remains
configured with `validate` and should be started only after this command exits
successfully.

For rollback, change `APP_IMAGE` to the prior immutable digest and recreate only the application. Do not reverse database migrations; ship a forward corrective migration.
