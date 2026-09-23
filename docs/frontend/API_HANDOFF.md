# Souklab frontend API handoff

Status: current backend contract. The backend is the source of truth; generate the OpenAPI document from the running application rather than copying DTOs into the frontend repository.

## Environments

| Environment | API base URL | OpenAPI |
|---|---|---|
| Local | `http://localhost:8080/api/v1` | `http://localhost:8080/v3/api-docs` |
| Test/staging | supplied by deployment | same host + `/v3/api-docs` when enabled |
| Production | supplied by deployment | obtain the private CI artifact |

The path and port are configurable with `PORT`, `OPENAPI_PATH`, and `OPENAPI_SWAGGER_PATH`. Do not hard-code a production hostname.

## Authentication

Use `Authorization: Bearer <accessToken>` for protected REST calls. Login and refresh return access/refresh tokens and expiry metadata. Keep tokens in the application's approved secure mechanism and never log them.

- `POST /auth/register` starts onboarding.
- `POST /auth/login` creates an access/refresh-token pair.
- `POST /auth/refresh` renews the access session.
- `POST /auth/logout` invalidates the refresh session.
- `GET /auth/me` returns the current account.
- `PATCH /auth/me` applies the documented partial update semantics.

On `401`, attempt one refresh if available. If refresh fails, clear authentication state and redirect to login. A `403` is an authorization or ownership decision, not an expired token.

## Response and error contracts

Successful REST responses use:

```json
{"success":true,"code":200,"message":"Success","data":{}}
```

`code` is the HTTP status and is stamped from the actual response. Errors use the same envelope, with `success: false`, a stable `errorCode`, and `data: null`. Validation errors additionally include an `errors` object keyed by request field. Branch on `errorCode`, never on message text.

Common statuses are 400 (malformed request), 401 (unauthenticated), 403 (forbidden), 404 (missing resource), 409 (state conflict), 413 (upload too large), 415 (unsupported media type), 422 (validation/rejected content), 429 (rate limited), and 500/503 (server/dependency failure).

## Endpoint groups

| Group | Base paths | Notes |
|---|---|---|
| Auth/account | `/auth/**` | registration, login, refresh, verification, password, profile |
| Catalog/directory | `/catalog/**`, `/directory/**` | public reference data and search |
| Artisan/profile | `/artisan/**`, `/artisans/**` | profiles, certifications, galleries, reviews |
| Feed/moderation | `/feed/**`, `/admin/feed/**`, `/reports/**` | posts, media, reports |
| Formations | `/artisan/formations/**`, `/admin/formations/**` | authoring, files, enrollment, moderation |
| Messaging | `/conversations/**`, STOMP `/app/**` | conversations, messages, attachments |
| Notifications | `/notifications/**` | feed, unread count (raw integer in `data`), read state |
| Subscriptions/payments | `/subscriptions/**`, `/payments/**`, `/admin/**` | checkout, lifecycle, refunds |
| Administration | `/admin/**` | users, permissions, moderation, catalog taxonomy, analytics |
| Files | `/files/**`, avatar and multipart paths | uploads and protected downloads |

Use the generated OpenAPI artifact for exact path/method pairs, schemas, security requirements, and operation IDs. Do not infer routes from this summary.

## Pagination, files, and permissions

Use the advertised Spring pageable parameters (`page`, `size`, `sort`); the configured default is 20 and maximum is 100. Preserve server-provided page metadata. Multipart requests must use the browser-generated boundary; never manually set `Content-Type: multipart/form-data`. Downloads may be binary streams or redirects, so inspect the response content type and handle `Content-Disposition` safely.

Authorization is permission-based and database-backed. The canonical permission names and ownership rules are in [AUTHORIZATION_MATRIX.md](../AUTHORIZATION_MATRIX.md). UI checks are for usability only; the backend is authoritative.

## Realtime and reliability

WebSocket/STOMP authentication and destinations come from the backend WebSocket configuration and contract tests. Webhooks are server-to-server and are not called by the browser. Respect `Retry-After`; use bounded retries only for idempotent requests or operations with an idempotency key. Do not blindly retry login, checkout, refund, or message creation.

## TypeScript generation

Generated code is not committed in this repository:

```bash
npx @hey-api/openapi-ts -i ./openapi.json -o ./src/generated/api
# or
npx openapi-typescript ./openapi.json -o ./src/generated/openapi.d.ts
```

Pin the generator version in the frontend lockfile and review the OpenAPI diff whenever a controller, DTO, status, or enum code changes.

## Common mistakes

Do not send role strings instead of permissions, assume every operation returns 200, treat 403 as refresh failure, manually set a multipart boundary, expose refresh tokens in logs, or rely on historical Postman examples without checking the generated contract.
