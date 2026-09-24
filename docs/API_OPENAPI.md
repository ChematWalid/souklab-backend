# Souklab OpenAPI contract

This is the authoritative, hand-maintained REST and WebSocket guide. The
machine-readable contract is available at `/v3/api-docs` (and
`/v3/api-docs.yaml` when enabled). Run
`python3 scripts/validate-api-docs.py http://localhost:8080/v3/api-docs`
against a running application before publishing changes. The validator checks
that all documented method/path pairs exist in the live contract and that no
stale operation remains.

## Quick start

Set `SOUKLAB_BASE_URL` to the API origin and send the JWT access token in the
`Authorization: Bearer <token>` header. JSON requests use
`Content-Type: application/json`; successful responses use the common envelope
`{ success, code, message, data, errors, traceId }`. `traceId` is safe to put
in support tickets. Do not log tokens, passwords, API keys, webhook bodies, or
personal data.

Most list endpoints accept Spring pagination parameters (`page`, `size`, and
`sort`) and return `data.content`, `data.pageNumber`, `data.pageSize`,
`data.totalElements`, `data.totalPages`, and `data.last`. Validation failures
normally return 400, unauthenticated calls 401, authorization failures 403,
missing resources 404, conflicts 409, and rate limits 429. Retry only safe or
idempotent operations; send an `Idempotency-Key` on checkout and other
retryable mutations where supported.

Uploads use `multipart/form-data` and are subject to the configured size,
extension, virus-scan, and upload-rate limits. Never construct a multipart
request with a manually fixed boundary.

Every operation below includes a curl and TypeScript example. Examples use
placeholders and are safe to copy into local development; they are not test
credentials.

## Chargily webhook setup

Provider callbacks cannot reach a localhost-only URL. For sandbox callback
testing, run ngrok against the local application and configure Chargily with:

`https://<ngrok-host>/api/v1/integrations/chargily/webhook`

Use `CHARGILY_MODE=test`,
`CHARGILY_BASE_URL=https://pay.chargily.net/test/api/v2`,
`CHARGILY_API_KEY`, `CHARGILY_SECRET_KEY`, and `CHARGILY_WEBHOOK_URL`. Keep
provider credentials in ephemeral environment variables only. The application
preserves the raw request body and reads the `signature` header. It accepts a
plain lowercase/uppercase hexadecimal HMAC-SHA256 digest and the equivalent
`sha256=<hex>` form. The digest is computed over the exact raw bytes using the
Chargily secret. Do not parse and re-serialize JSON before verification.

The safe external test boundary is checkout creation only: stop before card
entry, capture, refunds, or other irreversible provider actions. Local fake
provider tests cover success, validation, rate limit, provider failure,
malformed response, timeout/retry, and idempotent replay. Signed local webhook
cases cover `checkout.paid`, `checkout.failed`, `checkout.canceled`, duplicate,
stale, unknown checkout, malformed JSON, oversized body, missing/invalid
signature, unsupported event, and state-transition conflicts.

## WebSocket/STOMP

The SockJS endpoint is `/ws`; the native WebSocket transport is commonly
`/ws/websocket`, and `/ws/info` is the SockJS capability/handshake endpoint.
Send a STOMP `CONNECT` frame with `accept-version:1.2`, the host header, and
`Authorization: Bearer <access-token>`. Unauthenticated CONNECT attempts must
be rejected. Subscribe to `/user/queue/notifications` for notifications and
`/user/queue/chat-events` for conversation events. Client
commands are sent to `/app/v1/conversations/{conversationId}/messages.send`,
`.messages.edit`, `.messages.delete`, `.read`, `.typing.start`, and
`.typing.stop`. Use receipts/acknowledgements, handle `ERROR`, reconnect with
backoff, and re-subscribe only after a new `CONNECTED` frame.

Minimal TypeScript client shape:

```ts
const client = new Client({
  brokerURL: `${baseUrl.replace("http", "ws")}/ws/websocket`,
  connectHeaders: { Authorization: `Bearer ${accessToken}` },
  reconnectDelay: 2000,
});
client.onConnect = () => client.subscribe("/user/queue/notifications", onMessage);
client.onStompError = (frame) => console.error("STOMP error", frame.headers.message);
client.activate();
```

See [API conventions](API_CONVENTIONS.md), [authorization matrix](AUTHORIZATION_MATRIX.md),
and [frontend handoff](frontend/API_HANDOFF.md) for shared conventions and
permission boundaries.


Generated from the running application on 2026-09-23T01:47:58Z. This Markdown view is a human-readable companion to the machine-readable `/v3/api-docs` document.

- OpenAPI version: `3.1.0`
- API title: `Souklab API`
- API version: `1.0.0`
- Paths: `145`
- Schemas: `184`

## Security

### `bearerAuth`

```json
{"type":"http","description":"JWT access token issued by the authentication API.","scheme":"bearer","bearerFormat":"JWT"}
```


## Endpoints

### `/api/v1/client/favorites/artisans/{artisanId}`

#### POST — addFavoriteArtisan
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/client/favorites/artisans/{artisanId}`.

Authorization: Bearer access#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/client/favorites/artisans/${ARTISANID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/client/favorites/artisans/{artisanId}`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `addFavoriteArtisan`
- Tags: `Client Favorites`
- Parameters:
  - `artisanId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/client/favorites/artisans/{artisanId}`

#### DELETE — removeFavoriteArtisan
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/client/favorites/artisans/{artisanId}`.

Authorization: Bearer access#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/client/favorites/artisans/${ARTISANID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/client/favorites/artisans/{artisanId}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `removeFavoriteArtisan`
- Tags: `Client Favorites`
- Parameters:
  - `artisanId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/client/favorites/artisans`

#### GET — listFavoriteArtisans
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/client/favorites/artisans`.

Authorization: Bearer access#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/client/favorites/artisans" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/client/favorites/artisans`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `listFavoriteArtisans`
- Tags: `Client Favorites`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/client/favorites/artisans/{artisanId}/status`

#### GET — getFavoriteArtisanStatus
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/client/favorites/artisans/{artisanId}/status`.

Authorization: Bearer access#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/client/favorites/artisans/${ARTISANID}/status" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/client/favorites/artisans/{artisanId}/status`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getFavoriteArtisanStatus`
- Tags: `Client Favorites`
- Parameters:
  - `artisanId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars/{id}/activate`

#### PUT — activateAvatar
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/users/me/avatars/{id}/activate`.

Authorizat#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/users/me/avatars/${ID}/activate" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/users/me/avatars/{id}/activate`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ion: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `activateAvatar`
- Tags: `avatar-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/{id}/read`

#### PUT — markAsRead
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/notifications/{id}/read`.

Authorization: Bear#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/notifications/${ID}/read" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/notifications/{id}/read`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

er access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `markAsRead`
- Tags: `notification-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/read-all`

#### PUT — markAllAsRead
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/notific#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/notifications/read-all" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/notifications/read-all`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ations/read-all`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `markAllAsRead`
- Tags: `notification-controller`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### GET — get
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/feed/{id}`.

Authorization: Bearer a#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/feed/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/feed/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ccess token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `get`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### PUT — update
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/feed/{id}`.

Authorization: Bearer access token required; the live contrac#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/feed/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/feed/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

t and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `update`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}`

#### DELETE — remove
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/feed/{id}`.

Authorization: Beare#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/feed/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/feed/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

r access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `remove`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/reviews/{reviewId}`

#### PUT — update_1
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/artisan/reviews/{reviewId}`.

Authorization: Bearer access token required; the live con#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/reviews/${REVIEWID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/reviews/{reviewId}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

tract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `update_1`
- Tags: `artisan-review-controller`
- Parameters:
  - `reviewId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/reviews/{reviewId}`

#### DELETE — delete
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/artisan/reviews/{reviewId}`.

Authorization:#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/reviews/${REVIEWID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/reviews/{reviewId}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `delete`
- Tags: `artisan-review-controller`
- Parameters:
  - `reviewId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery/order`

#### PUT — reorderGallery
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/artisan/gallery/order`.

Authorization: Bearer#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/gallery/order" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/gallery/order`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `reorderGallery`
- Tags: `artisan-gallery-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### GET — getFormationDetails
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/formations/{id}`.

Authorization: Bearer access toke#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

n required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getFormationDetails`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### PUT — updateFormation
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/artisan/formations/{id}`.

Authorization: Bearer access token required; the live contract a#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

nd authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `updateFormation`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}`

#### DELETE — deleteFormation
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/artisan/formations/{id}`.

Authorization: Bearer a#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ccess token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteFormation`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans/{id}`

#### PUT — update_2
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/admin/subscription-plans/{id}`.

Authorization: Bearer access token required; the live con#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscription-plans/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscription-plans/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

tract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `update_2`
- Tags: `admin-subscription-plan-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans/{id}`

#### DELETE — deactivate
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/subscription-plans/{id}`.

Authorization: Bearer access token required; the live#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscription-plans/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscription-plans/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deactivate`
- Tags: `admin-subscription-plan-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/techniques/{id}`

#### PUT — updateTechnique
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/admin/catalog/techniques/{id}`.

Authorization: Bearer access token required; the live #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/techniques/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/techniques/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `updateTechnique`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/techniques/{id}`

#### DELETE — deleteTechnique
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/catalog/techniques/{id}`.

Authorization#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/techniques/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/techniques/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteTechnique`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/techniques/{id}`

#### PATCH — patchTechnique
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/techniques/{id}`.

Authorization: Bearer access token required; the #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/techniques/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/techniques/{id}`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchTechnique`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories/{id}`

#### PUT — updateSubCategory
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/admin/catalog/subcategories/{id}`.

Authorization: Bearer access token required; the live#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/subcategories/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/subcategories/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `updateSubCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories/{id}`

#### DELETE — deleteSubCategory
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/catalog/subcategories/{id}`.

Authorizatio#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/subcategories/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/subcategories/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

n: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteSubCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories/{id}`

#### PATCH — patchSubCategory
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/subcategories/{id}`.

Authorization: Bearer access token required; the#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/subcategories/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/subcategories/{id}`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchSubCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/regions/{id}`

#### PUT — updateRegion
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/admin/catalog/regions/{id}`.

Authorization: Bearer access token required; the live #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/regions/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/regions/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `updateRegion`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/regions/{id}`

#### DELETE — deleteRegion
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/catalog/regions/{id}`.

Authorization#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/regions/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/regions/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteRegion`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/regions/{id}`

#### PATCH — patchRegion
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/regions/{id}`.

Authorization: Bearer access token required; the #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/regions/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/regions/{id}`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchRegion`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials/{id}`

#### PUT — updateMaterial
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/admin/catalog/materials/{id}`.

Authorization: Bearer access token required; the live #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/materials/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/materials/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `updateMaterial`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials/{id}`

#### DELETE — deleteMaterial
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/catalog/materials/{id}`.

Authorization#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/materials/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/materials/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteMaterial`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials/{id}`

#### PATCH — patchMaterial
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/materials/{id}`.

Authorization: Bearer access token required; the #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/materials/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/materials/{id}`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchMaterial`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families/{id}`

#### PUT — updateMaterialFamily
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/admin/catalog/material-families/{id}`.

Authorization: Bearer access token required; the liv#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/material-families/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/material-families/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

e contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `updateMaterialFamily`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families/{id}`

#### DELETE — deleteMaterialFamily
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/catalog/material-families/{id}`.

Authorizati#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/material-families/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/material-families/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

on: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteMaterialFamily`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families/{id}`

#### PATCH — patchMaterialFamily
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/material-families/{id}`.

Authorization: Bearer access token required; th#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/material-families/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/material-families/{id}`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

e live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchMaterialFamily`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/epoques/{id}`

#### PUT — updateEpoque
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/admin/catalog/epoques/{id}`.

Authorization: Bearer access token required; the live #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/epoques/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/epoques/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `updateEpoque`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/epoques/{id}`

#### DELETE — deleteEpoque
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/catalog/epoques/{id}`.

Authorization#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/epoques/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/epoques/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteEpoque`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/epoques/{id}`

#### PATCH — patchEpoque
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/epoques/{id}`.

Authorization: Bearer access token required; the #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/epoques/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/epoques/{id}`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchEpoque`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories/{id}`

#### PUT — updateCategory
#### Purpose and authorization

Purpose: `PUT` performs the `PUT` operation for `/api/v1/admin/catalog/categories/{id}`.

Authorization: Bearer access token required; the live#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PUT "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/categories/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/categories/{id}`, { method: "PUT", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `updateCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories/{id}`

#### DELETE — deleteCategory
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/catalog/categories/{id}`.

Authorizatio#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/categories/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/categories/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

n: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories/{id}`

#### PATCH — patchCategory
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/categories/{id}`.

Authorization: Bearer access token required; the#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/categories/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/categories/{id}`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchCategory`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars`

#### GET — listAvatars
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/users/me/avatars`.

Authorization: Bearer access#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/users/me/avatars" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/users/me/avatars`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `listAvatars`
- Tags: `avatar-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars`

#### POST — uploadAvatar
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/users/me/avatars`.

Authorization: B#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/users/me/avatars" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/users/me/avatars`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

earer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `uploadAvatar`
- Tags: `avatar-controller`
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/{id}/renew`

#### POST — renew
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/subscriptions/{id}/renew`.

Authorization: Bearer access token required; the live contract and authorization matrix determine #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/subscriptions/${ID}/renew" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/subscriptions/{id}/renew`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `renew`
- Tags: `subscription-checkout-controller`
- Parameters:
  - `id` (`path`, required)
  - `Idempotency-Key` (`header`, optional)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/{id}/cancel`

#### POST — cancel
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/subscriptions/{id}/cancel`.

Authorization: Bear#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/subscriptions/${ID}/cancel" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/subscriptions/{id}/cancel`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

er access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `cancel`
- Tags: `subscription-account-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/checkout`

#### POST — checkout
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/subscriptions/checkout`.

Authorization: Bearer access token required; the live contract and authoriz#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/subscriptions/checkout" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/subscriptions/checkout`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ation matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `checkout`
- Tags: `subscription-checkout-controller`
- Parameters:
  - `Idempotency-Key` (`header`, optional)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/reports`

#### POST — create
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/reports`.

Authorization: Bearer ac#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/reports" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/reports`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

cess token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `create`
- Tags: `content-report-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/integrations/chargily/webhook`

#### POST — webhook
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/integrations/chargily/webhook`.

Authorization: Public or authentication-flow operation; #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/integrations/chargily/webhook"
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/integrations/chargily/webhook`, { method: "POST", headers: {} });
const payload = await response.json();
```

no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `webhook`
- Tags: `chargily-webhook-controller`
- Parameters:
  - `signature` (`header`, optional)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed`

#### GET — list
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/feed`.

Authorization: Bearer access token required; the live contract and #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/feed" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/feed`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `list`
- Tags: `feed-post-controller`
- Parameters:
  - `type` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed`

#### POST — create_1
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/feed`.

Authorization: Bearer ac#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/feed" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/feed`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

cess token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `create_1`
- Tags: `feed-post-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/media`

#### POST — addMedia
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/feed/{id}/media`.

Authorization: Bearer access token required; the live cont#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/feed/${ID}/media" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/feed/{id}/media`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `addMedia`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/conversations`

#### GET — list_1
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/conversations`.

Authorization: Bearer access tok#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

en required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `list_1`
- Tags: `conversation-controller`
- Parameters:
  - `archived` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/conversations`

#### POST — create_2
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/conversations`.

Authorization: Bea#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

rer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `create_2`
- Tags: `conversation-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/read`

#### POST — read
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/conversations/{id}/read`.

Authorization: Bearer access token required; t#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations/${ID}/read" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations/{id}/read`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

he live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `read`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/messages`

#### GET — messages
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/conversations/{id}/messages`.

Authorization: Bearer access token required; the live contract and authorizat#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations/${ID}/messages" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations/{id}/messages`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ion matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `messages`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
  - `cursor` (`query`, optional)
  - `size` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/messages`

#### POST — send
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/conversations/{id}/messages`.

Authorization: Bearer access token require#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations/${ID}/messages" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations/{id}/messages`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

d; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `send`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/attachments`

#### POST — uploadAttachment
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/conversations/{id}/attachments`.

Authorization: Bearer access token required; the live #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations/${ID}/attachments" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations/{id}/attachments`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `uploadAttachment`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/auth/verify-email`

#### POST — verifyEmail
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/verify-email`.

Authoriza#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/verify-email" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/verify-email`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

tion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `verifyEmail`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/reset-password`

#### POST — resetPassword
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/reset-password`.

Authoriza#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/reset-password" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/reset-password`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

tion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `resetPassword`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/resend-verification`

#### POST — resendVerification
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/resend-verification`.

Authoriza#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/resend-verification" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/resend-verification`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

tion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `resendVerification`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/register`

#### POST — register
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/register`.

Authorizat#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/register" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/register`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

ion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `register`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/refresh`

#### POST — refreshToken
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/refresh`.

Authorization: #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/refresh" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/refresh`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `refreshToken`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/logout`

#### POST — logout
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/logout`.

Authorizat#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/logout" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/logout`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

ion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `logout`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/login`

#### POST — login
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/login`.

Authorizat#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/login" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/login`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

ion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `login`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/forgot-password`

#### POST — forgotPassword
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/forgot-password`.

Authoriza#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/forgot-password" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/forgot-password`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

tion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `forgotPassword`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/complete-profile`

#### POST — completeProfile
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/complete-profile`.

Authoriza#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/complete-profile" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/complete-profile`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

tion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `completeProfile`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/change-password`

#### POST — changePassword
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/auth/change-password`.

Authoriza#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/change-password" --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/change-password`, { method: "POST", headers: {}, body: JSON.stringify({}) });
const payload = await response.json();
```

tion: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `changePassword`
- Tags: `auth-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery`

#### GET — getMyGallery
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/g#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/gallery" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/gallery`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

allery`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getMyGallery`
- Tags: `artisan-gallery-controller`
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery`

#### POST — uploadImage
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/gallery`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the req#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/gallery" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/gallery`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

uired role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `uploadImage`
- Tags: `artisan-gallery-controller`
- Parameters:
  - `title` (`query`, optional)
  - `caption` (`query`, optional)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations`

#### POST — createFormation
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/formations`.

Authorization: Bearer acc#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ess token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `createFormation`
- Tags: `artisan-formation-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/thumbnail`

#### POST — uploadThumbnail
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/formations/{id}/thumbnail`.

Authorization: Bearer access token required; the live c#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}/thumbnail" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}/thumbnail`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ontract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `uploadThumbnail`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/submit`

#### POST — submitForReview
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/formations/{id}/submit`.

Authorization: Beare#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}/submit" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}/submit`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

r access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `submitForReview`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files`

#### POST — uploadCourseFile
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/formations/{id}/files`.

Authorization: Bearer access token required; the live contra#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}/files" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}/files`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ct and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `uploadCourseFile`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/enroll`

#### POST — enroll
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/formations/{id}/enroll`.

Authorization: Bearer #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}/enroll" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}/enroll`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `enroll`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/cancel`

#### POST — cancel_1
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/formations/{id}/cancel`.

Authorization: Bearer ac#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}/cancel" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}/cancel`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

cess token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `cancel_1`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{formationId}/reviews`

#### POST — create_3
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/formations/{formationId}/reviews`.

Authorization: Bearer access token required;#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${FORMATIONID}/reviews" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{formationId}/reviews`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `create_3`
- Tags: `artisan-review-controller`
- Parameters:
  - `formationId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/formateur-request`

#### POST — submitRequest
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/formateur-request`.

Authorization: B#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formateur-request" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formateur-request`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

earer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `submitRequest`
- Tags: `artisan-formateur-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications`

#### GET — getMyCertifications
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/certifications#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/certifications" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/certifications`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getMyCertifications`
- Tags: `artisan-certification-controller`
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications`

#### POST — uploadCertification
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/artisan/certifications`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the respons#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/certifications" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/certifications`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

e codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `uploadCertification`
- Tags: `artisan-certification-controller`
- Parameters:
  - `title` (`query`, required)
  - `issuer` (`query`, required)
  - `issuedAt` (`query`, optional)
  - `expiresAt` (`query`, optional)
- Request body: `multipart/form-data`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{userId}/permissions`

#### GET — list_2
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/users/{userId}/permissions`.

Authorization: Bear#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/${USERID}/permissions" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/{userId}/permissions`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

er access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `list_2`
- Tags: `permission-management-controller`
- Parameters:
  - `userId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{userId}/permissions`

#### POST — grant
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/users/{userId}/permissions`.

Authorization: Bearer access token required; the li#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/${USERID}/permissions" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/{userId}/permissions`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ve contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `grant`
- Tags: `permission-management-controller`
- Parameters:
  - `userId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{userId}/permissions`

#### DELETE — revoke
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/users/{userId}/permissions`.

Authorization: Bearer access token required; the#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/${USERID}/permissions" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/{userId}/permissions`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `revoke`
- Tags: `permission-management-controller`
- Parameters:
  - `userId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{id}/unban`

#### POST — unbanUser
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/users/{id}/unban`.

Authorization: Beare#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/${ID}/unban" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/{id}/unban`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

r access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `unbanUser`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{id}/timeout`

#### POST — timeoutUser
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/users/{id}/timeout`.

Authorization: Bearer access token required; the live c#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/${ID}/timeout" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/{id}/timeout`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ontract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `timeoutUser`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{id}/ban`

#### POST — banUser
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/users/{id}/ban`.

Authorization: Bearer access token required; the live c#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/${ID}/ban" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/{id}/ban`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ontract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `banUser`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/users/{id}/approve`

#### POST — approveUser
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/users/{id}/approve`.

Authorization: Beare#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/${ID}/approve" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/{id}/approve`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

r access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `approveUser`
- Tags: `user-management-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/approve-bulk`

#### POST — approveUsersBulk
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/users/approve-bulk`.

Authorization: Bea#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/approve-bulk" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/approve-bulk`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

rer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `approveUsersBulk`
- Tags: `user-management-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/{id}/revoke`

#### POST — revoke_1
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/subscriptions/{id}/revoke`.

Authorization: Bearer access token required; the#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscriptions/${ID}/revoke" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscriptions/{id}/revoke`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `revoke_1`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/{id}/correct-state`

#### POST — correctSubscriptionState
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/subscriptions/{id}/correct-state`.

Authorization: Bearer access token required; the live con#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscriptions/${ID}/correct-state" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscriptions/{id}/correct-state`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

tract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `correctSubscriptionState`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/{id}/cancel`

#### POST — cancel_2
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/subscriptions/{id}/cancel`.

Authorization: Bearer access token required; the#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscriptions/${ID}/cancel" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscriptions/{id}/cancel`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `cancel_2`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/payments/{id}/correct-state`

#### POST — correctPaymentState
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/subscriptions/payments/{id}/correct-state`.

Authorization: Bearer access token required#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscriptions/payments/${ID}/correct-state" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscriptions/payments/{id}/correct-state`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `correctPaymentState`
- Tags: `admin-subscription-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/grant`

#### POST — grant_1
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/subscriptions/grant`.

Authorizati#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscriptions/grant" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscriptions/grant`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

on: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `grant_1`
- Tags: `admin-subscription-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans`

#### GET — list_3
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/subsc#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscription-plans" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscription-plans`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ription-plans`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `list_3`
- Tags: `admin-subscription-plan-controller`
- Responses:
  - `200` — OK

### `/api/v1/admin/subscription-plans`

#### POST — create_4
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/subscription-plans`.

Authorization: Bea#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscription-plans" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscription-plans`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

rer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `create_4`
- Tags: `admin-subscription-plan-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/reports/{id}/resolve`

#### POST — resolve
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/reports/{id}/resolve`.

Authorization: Bearer access token required; the#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/reports/${ID}/resolve" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/reports/{id}/resolve`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `resolve`
- Tags: `content-report-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/payments/{id}/refund`

#### POST — rejectRefund
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/payments/{id}/refund`.

Authorization: Bearer access token required; the li#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/payments/${ID}/refund" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/payments/{id}/refund`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ve contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `rejectRefund`
- Tags: `admin-refund-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/formations/{id}/review`

#### POST — reviewFormation
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/formations/{id}/review`.

Authorization: Bearer access token required; the live c#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/formations/${ID}/review" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/formations/{id}/review`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ontract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `reviewFormation`
- Tags: `admin-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/formations/{id}/publish`

#### POST — publishFormation
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/formations/{id}/publish`.

Authorization: Beare#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/formations/${ID}/publish" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/formations/{id}/publish`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

r access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `publishFormation`
- Tags: `admin-formation-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/formateur-requests/{id}/reject`

#### POST — rejectRequest
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/formateur-requests/{id}/reject`.

Authorization: Bearer access token required; #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/formateur-requests/${ID}/reject" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/formateur-requests/{id}/reject`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `rejectRequest`
- Tags: `admin-formateur-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/formateur-requests/{id}/approve`

#### POST — approveRequest
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/formateur-requests/{id}/approve`.

Authorization: Bearer access token required; #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/formateur-requests/${ID}/approve" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/formateur-requests/{id}/approve`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `approveRequest`
- Tags: `admin-formateur-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/formateur-requests/{artisanId}/lift-cooldown`

#### POST — liftCooldown
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/formateur-requests/{artisanId}/lift-cooldown`.

Authorization: Bearer access token re#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/formateur-requests/${ARTISANID}/lift-cooldown" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/formateur-requests/{artisanId}/lift-cooldown`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

quired; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `liftCooldown`
- Tags: `admin-formateur-controller`
- Parameters:
  - `artisanId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/remove`

#### POST — remove_1
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/feed/{id}/remove`.

Authorization:#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/feed/${ID}/remove" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/feed/{id}/remove`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `remove_1`
- Tags: `admin-feed-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/publish`

#### POST — publish
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/feed/{id}/publish`.

Authorization: Bearer access token required; th#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/feed/${ID}/publish" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/feed/{id}/publish`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

e live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `publish`
- Tags: `admin-feed-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/{id}/hide`

#### POST — hide
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/feed/{id}/hide`.

Authorization: Bearer access token required; th#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/feed/${ID}/hide" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/feed/{id}/hide`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

e live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `hide`
- Tags: `admin-feed-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/techniques`

#### POST — createTechnique
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/catalog/techniques`.

Authorization: #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/techniques" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/techniques`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `createTechnique`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/subcategories`

#### POST — createSubCategory
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/catalog/subcategories`.

Authorization:#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/subcategories" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/subcategories`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `createSubCategory`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/regions`

#### POST — createRegion
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/catalog/regions`.

Authorization: #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/regions" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/regions`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `createRegion`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials`

#### POST — createMaterial
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/catalog/materials`.

Authorization: #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/materials" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/materials`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `createMaterial`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families`

#### POST — createMaterialFamily
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/catalog/material-families`.

Authorization#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/material-families" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/material-families`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `createMaterialFamily`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/epoques`

#### POST — createEpoque
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/catalog/epoques`.

Authorization: #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/epoques" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/epoques`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `createEpoque`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories`

#### POST — createCategory
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/catalog/categories`.

Authorization:#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/categories" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/categories`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `createCategory`
- Tags: `admin-catalog-controller`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/artisans/{artisanId}/formateur-revoke`

#### POST — revokeDirectly
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/artisans/{artisanId}/formateur-revoke`.

Authorization: Bearer access token required; t#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/artisans/${ARTISANID}/formateur-revoke" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/artisans/{artisanId}/formateur-revoke`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

he live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `revokeDirectly`
- Tags: `admin-formateur-controller`
- Parameters:
  - `artisanId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/artisans/{artisanId}/formateur-grant`

#### POST — grantDirectly
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/artisans/{artisanId}/formateur-grant`.

Authorization: Bearer access token required; t#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/artisans/${ARTISANID}/formateur-grant" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/artisans/{artisanId}/formateur-grant`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

he live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `grantDirectly`
- Tags: `admin-formateur-controller`
- Parameters:
  - `artisanId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/rebuild`

#### POST — Queue a rollup rebuild (compatibility alias)
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/analytics/rollups/re#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/rollups/rebuild" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/rollups/rebuild`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

build`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `rebuild`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/rebuild`

#### POST — Queue a rollup rebuild (compatibility alias)
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/stats/rollups/rebuild`#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/rollups/rebuild" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/rollups/rebuild`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `rebuild_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/jobs/rebuild`

#### POST — Queue a rollup rebuild
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/analytics/rollups/jobs/re#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/rollups/jobs/rebuild" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/rollups/jobs/rebuild`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

build`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `queueRebuild`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/jobs/rebuild`

#### POST — Queue a rollup rebuild
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/stats/rollups/jobs/rebuild`#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/rollups/jobs/rebuild" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/rollups/jobs/rebuild`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `queueRebuild_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/jobs/backfill`

#### POST — Queue historical backfill
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/analytics/rollups/jobs/bac#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/rollups/jobs/backfill" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/rollups/jobs/backfill`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

kfill`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `queueBackfill`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/jobs/backfill`

#### POST — Queue historical backfill
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/stats/rollups/jobs/backfill`#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/rollups/jobs/backfill" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/rollups/jobs/backfill`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `queueBackfill_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/backfill`

#### POST — Queue historical backfill (compatibility alias)
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/stats/rollups/backfil#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/rollups/backfill" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/rollups/backfill`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

l`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `backfill`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/backfill`

#### POST — Queue historical backfill (compatibility alias)
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/analytics/rollups/backf#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/rollups/backfill" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/rollups/backfill`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

ill`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `backfill_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs`

#### POST — Submit an analytics job
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/stats/jobs`.

Autho#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/jobs" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/jobs`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

rization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `submit`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs`

#### POST — Submit an analytics job
#### Purpose and authorization

Purpose: `POST` performs the `POST` operation for `/api/v1/admin/analytics/jobs`.

Aut#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request POST "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/jobs" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/jobs`, { method: "POST", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

horization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `submit_1`
- Tags: `Admin analytics`
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{id}/archive`

#### PATCH — archive
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/conversations/{id}/archive`.

Authorization: Bearer access token required;#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations/${ID}/archive" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations/{id}/archive`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `archive`
- Tags: `conversation-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/conversations/{conversationId}/messages/{messageId}`

#### DELETE — delete_1
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/conversations/{conversationId}/messages/{messageId}`.

Authorization: Bearer access t#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations/${CONVERSATIONID}/messages/${MESSAGEID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations/{conversationId}/messages/{messageId}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

oken required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `delete_1`
- Tags: `conversation-controller`
- Parameters:
  - `conversationId` (`path`, required)
  - `messageId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/conversations/{conversationId}/messages/{messageId}`

#### PATCH — edit
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/conversations/{conversationId}/messages/{messageId}`.

Authorization: Bearer access token required; the live contract #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/conversations/${CONVERSATIONID}/messages/${MESSAGEID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/conversations/{conversationId}/messages/{messageId}`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `edit`
- Tags: `conversation-controller`
- Parameters:
  - `conversationId` (`path`, required)
  - `messageId` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/auth/me`

#### GET — getCurrentUser
#### Purpose and authorization

Purpose: Retrieves the profile of the currently authenticated user (`ProfileResponse`). Returns polymorphic responses: `ArtisanResponseDTO` (with craft categories, subcategory, materials, techniques, epochs, ratings, certifications, gallery) for artisans, or `ClientProfileResponseDTO` (with company name, client type) for clients.

Authorization: Requires authenticated session with bearer token (`Authorization: Bearer <token>`).

Failure cases: `401 Unauthorized` when the token is missing or expired; `403 Forbidden` if the account is deactivated or banned.

#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/me" \
  --header "Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}"
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/me`, {
  method: "GET",
  headers: {
    Authorization: `Bearer ${accessToken}`,
    Accept: "application/json"
  }
});
const payload = await response.json();
```

- Operation ID: `getCurrentUser`
- Tags: `Authentication & Profile`
- Responses:
  - `200` — Profile retrieved successfully
  - `401` — Unauthorized (missing or expired bearer token)

### `/api/v1/auth/me`

#### PATCH — patchCurrentUser
#### Purpose and authorization

Purpose: Partially updates the authenticated user's profile using JSON Merge Patch semantics (`application/json`). Omitted fields are preserved; fields set to `null` are cleared; fields set to values are updated. Supports artisan fields (`bio`, `city`, `address`, `website`, `regionId`, `subCategoryId`, `materialIds`, `techniqueIds`, `epoqueIds`) and client fields (`companyName`, `clientType`, `city`, `address`).

Authorization: Requires authenticated session with bearer token (`Authorization: Bearer <token>`).

Failure cases: `400 Bad Request` or `422 Unprocessable Content` on validation failure (e.g. invalid URL, invalid craft ID); `401 Unauthorized` if unauthenticated.

#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/me" \
  --header "Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}" \
  --header "Content-Type: application/json" \
  --data '{
    "bio": "Artisan potier traditionnel kabyle avec 15 ans d'\''expérience.",
    "city": "Tizi Ouzou",
    "address": "Village Ath Yanni",
    "website": "https://poterie-kabyle.dz"
  }'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/me`, {
  method: "PATCH",
  headers: {
    Authorization: `Bearer ${accessToken}`,
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    bio: "Artisan potier traditionnel kabyle avec 15 ans d'expérience.",
    city: "Tizi Ouzou",
    address: "Village Ath Yanni",
    website: "https://poterie-kabyle.dz"
  })
});
const payload = await response.json();
```

- Operation ID: `patchCurrentUser`
- Tags: `Authentication & Profile`
- Request body: `application/json`
- Responses:
  - `200` — Profile updated successfully
  - `400` — Malformed request
  - `401` — Unauthorized
  - `422` — Validation error

### `/api/v1/admin/catalog/subcategories/{id}/status`

#### PATCH — patchSubCategory_1
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/subcategories/{id}/status`.

Authorization: Bearer access token required#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/subcategories/${ID}/status" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/subcategories/{id}/status`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchSubCategory_1`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/materials/{id}/status`

#### PATCH — patchMaterial_1
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/materials/{id}/status`.

Authorization: Bearer access token required;#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/materials/${ID}/status" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/materials/{id}/status`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

 the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchMaterial_1`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/material-families/{id}/status`

#### PATCH — patchMaterialFamily_1
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/material-families/{id}/status`.

Authorization: Bearer access token require#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/material-families/${ID}/status" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/material-families/{id}/status`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

d; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchMaterialFamily_1`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/admin/catalog/categories/{id}/status`

#### PATCH — patchCategory_1
#### Purpose and authorization

Purpose: `PATCH` performs the `PATCH` operation for `/api/v1/admin/catalog/categories/{id}/status`.

Authorization: Bearer access token required#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request PATCH "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/catalog/categories/${ID}/status" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}' --header 'Content-Type: application/json' --data '{}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/catalog/categories/{id}/status`, { method: "PATCH", headers: { Authorization: `Bearer ${accessToken}` }, body: JSON.stringify({}) });
const payload = await response.json();
```

; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `patchCategory_1`
- Tags: `admin-catalog-controller`
- Parameters:
  - `id` (`path`, required)
- Request body: `application/json`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions`

#### GET — history
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/subscript#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/subscriptions" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/subscriptions`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ions`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `history`
- Tags: `subscription-account-controller`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/plans`

#### GET — listPlans
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/subscrip#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/subscriptions/plans" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/subscriptions/plans`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

tions/plans`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `listPlans`
- Tags: `subscription-plan-controller`
- Responses:
  - `200` — OK

### `/api/v1/subscriptions/current`

#### GET — current
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/subscript#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/subscriptions/current" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/subscriptions/current`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ions/current`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `current`
- Tags: `subscription-account-controller`
- Responses:
  - `200` — OK

### `/api/v1/public/directory`

#### GET — search
#### Purpose and authorization

Purpose: Multi-facet directory search and full-text querying across verified artisans. Non-premium viewers receive cards with anonymised artisan names (`Artisan #XXXXX`); premium viewers and administrators see real artisan names.

Authorization: Requires authenticated session with bearer token (`Authorization: Bearer <token>`). Anonymous requests receive `403 Forbidden`.

Failure cases: `403 Forbidden` if unauthenticated; `422 Unprocessable Content` if query filter fails validation (e.g. invalid rating > 5.0, negative page number, invalid keyword size).

#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/public/directory?keyword=poterie&page=0&size=20" \
  --header "Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}"
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/public/directory?keyword=poterie&page=0&size=20`, {
  method: "GET",
  headers: {
    Authorization: `Bearer ${accessToken}`,
    Accept: "application/json"
  }
});
const payload = await response.json();
```

- Operation ID: `search`
- Tags: `Artisan Directory`
- Parameters:
  - `keyword` (`query`, optional, max 120 chars)
  - `categoryId` (`query`, optional, UUID)
  - `subCategoryId` (`query`, optional, UUID)
  - `wilayaId` (`query`, optional)
  - `minRating` (`query`, optional, 0.0 - 5.0)
  - `materials` (`query`, optional, list of UUIDs)
  - `techniques` (`query`, optional, list of UUIDs)
  - `epoques` (`query`, optional, list of UUIDs)
  - `verifiedOnly` (`query`, optional, boolean)
  - `page` (`query`, optional, default: 0)
  - `size` (`query`, optional, default: 20, max: 100)
  - `sortBy` (`query`, optional, default: "createdAt")
  - `sortDir` (`query`, optional, default: "desc")
- Responses:
  - `200` — Paginated list of matching artisan cards
  - `403` — Forbidden (unauthenticated anonymous caller)
  - `422` — Validation failure on search parameters

### `/api/v1/payments`

#### GET — payments
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/payments`.#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/payments" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/payments`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```



Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `payments`
- Tags: `subscription-account-controller`
- Responses:
  - `200` — OK

### `/api/v1/payments/{id}`

#### GET — payment
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/payments/{id}`.

Authorization: Bearer access token#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/payments/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/payments/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `payment`
- Tags: `subscription-account-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications`

#### GET — getNotifications
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/notifications`.

Authorization: Bearer access token require#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/notifications" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/notifications`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

d; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getNotifications`
- Tags: `notification-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/unread-count`

#### GET — getUnreadCount
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/notifica#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/notifications/unread-count" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/notifications/unread-count`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

tions/unread-count`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getUnreadCount`
- Tags: `notification-controller`
- Responses:
  - `200` — OK

### `/api/v1/files/{key}`

#### GET — serveFile
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/files/{key}`.

Authorization: Bearer access to#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/files/${KEY}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/files/{key}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ken required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `serveFile`
- Tags: `file-serving-controller`
- Parameters:
  - `key` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/catalog/techniques`

#### GET — getTechniques
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/ca#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/catalog/techniques" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/catalog/techniques`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

talog/techniques`.

Authorization: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getTechniques`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/catalog/regions`

#### GET — getRegions
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/catalog/regions" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/catalog/regions`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

/catalog/regions`.

Authorization: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getRegions`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/catalog/materials`

#### GET — getMaterials
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/c#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/catalog/materials" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/catalog/materials`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

atalog/materials`.

Authorization: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getMaterials`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/catalog/epoques`

#### GET — getEpoques
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/catalog/epoques" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/catalog/epoques`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

/catalog/epoques`.

Authorization: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getEpoques`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/catalog/categories`

#### GET — getCategories
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/ca#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/catalog/categories" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/catalog/categories`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

talog/categories`.

Authorization: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getCategories`
- Tags: `catalog-controller`
- Responses:
  - `200` — OK

### `/api/v1/auth/oauth/google/client`

#### GET — initiateGoogleOAuthClient
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/auth/oauth/#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/oauth/google/client"
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/oauth/google/client`, { method: "GET", headers: {} });
const payload = await response.json();
```

google/client`.

Authorization: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `initiateGoogleOAuthClient`
- Tags: `auth-controller`
- Responses:
  - `200` — OK

### `/api/v1/auth/oauth/google/artisan`

#### GET — initiateGoogleOAuthArtisan
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/auth/oauth/g#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/auth/oauth/google/artisan"
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/auth/oauth/google/artisan`, { method: "GET", headers: {} });
const payload = await response.json();
```

oogle/artisan`.

Authorization: Public or authentication-flow operation; no bearer token is required unless the live contract declares security.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `initiateGoogleOAuthArtisan`
- Tags: `auth-controller`
- Responses:
  - `200` — OK

### `/api/v1/artisans/{artisanId}/reviews`

#### GET — list_4
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisans/{artisanId}/reviews`.

Authorization: Bearer access token required; the live #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisans/${ARTISANID}/reviews" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisans/{artisanId}/reviews`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `list_4`
- Tags: `artisan-review-controller`
- Parameters:
  - `artisanId` (`path`, required)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/{id}`

#### GET — getArtisanProfile
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/{id}`.

Authorization: Bearer access tok#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

en required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getArtisanProfile`
- Tags: `artisan-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files/{fileId}/download`

#### GET — downloadCourseFile
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/formations/{id}/files/{fileId}/download`.

Authorization: Bearer access token required; the li#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}/files/${FILEID}/download" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}/files/{fileId}/download`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ve contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `downloadCourseFile`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `id` (`path`, required)
  - `fileId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/my-enrollments`

#### GET — getMyEnrollments
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/formations/my-enrollments`.

Authorization: Bearer access token req#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/my-enrollments" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/my-enrollments`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

uired; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getMyEnrollments`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/me`

#### GET — getMyFormations
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/formations/me`.

Authorization: Bearer access token req#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/me" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/me`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

uired; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getMyFormations`
- Tags: `artisan-formation-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/catalog`

#### GET — getPublishedCatalog
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/formations/catalog`.

Authorization: Bearer access token required; the#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/catalog" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/catalog`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

 live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getPublishedCatalog`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/catalog/{id}`

#### GET — getPublishedFormationDetails
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/artisan/formations/catalog/{id}`.

Authorization: Bearer access token required; #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/catalog/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/catalog/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getPublishedFormationDetails`
- Tags: `artisan-formation-enrollment-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users`

#### GET — getAllUsers
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/users`.

Authorization: Bearer access token required; the live contract and authoriz#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ation matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getAllUsers`
- Tags: `user-management-controller`
- Parameters:
  - `search` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/pending`

#### GET — getPendingUsers
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/users/pending`.

Authorization: Bearer access token req#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/pending" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/pending`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

uired; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getPendingUsers`
- Tags: `user-management-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/users/audit-logs`

#### GET — getAuditLogs
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/users/audit-logs`.

Authorization: Bearer access tok#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/users/audit-logs" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/users/audit-logs`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

en required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getAuditLogs`
- Tags: `user-management-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions`

#### GET — subscriptions
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/subscriptions`.

Authorization: Bearer access token required; the live contract and a#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscriptions" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscriptions`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

uthorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `subscriptions`
- Tags: `admin-subscription-controller`
- Parameters:
  - `limit` (`query`, optional)
  - `query` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/webhooks`

#### GET — webhooks
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/subscriptions/webhooks`.

Authorization: Bearer access token required; the live #### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscriptions/webhooks" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscriptions/webhooks`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `webhooks`
- Tags: `admin-subscription-controller`
- Parameters:
  - `limit` (`query`, optional)
  - `query` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/admin/subscriptions/payments`

#### GET — payments_1
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/subscriptions/payments`.

Authorization: Bearer access token required; the live co#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/subscriptions/payments" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/subscriptions/payments`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ntract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `payments_1`
- Tags: `admin-subscription-controller`
- Parameters:
  - `limit` (`query`, optional)
  - `query` (`query`, optional)
- Responses:
  - `200` — OK

### `/api/v1/admin/reports`

#### GET — list_5
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/reports`.

Authorization: Bearer access token required; the live contract and authorization matrix determine the re#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/reports" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/reports`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

quired role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `list_5`
- Tags: `content-report-controller`
- Parameters:
  - `status` (`query`, optional)
  - `targetType` (`query`, optional)
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/formations/pending`

#### GET — getPendingFormations
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/formations/pending`.

Authorization: Bearer access token req#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/formations/pending" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/formations/pending`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

uired; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getPendingFormations`
- Tags: `admin-formation-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/formateur-requests`

#### GET — getPendingRequests
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/formateur-requests`.

Authorization: Bearer access token r#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/formateur-requests" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/formateur-requests`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

equired; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `getPendingRequests`
- Tags: `admin-formateur-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/feed/pending`

#### GET — listPending
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/feed/pending`.

Authorization: Bearer access t#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/feed/pending" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/feed/pending`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

oken required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `listPending`
- Tags: `admin-feed-controller`
- Parameters:
  - `pageable` (`query`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/rollups/jobs/{id}`

#### GET — Get maintenance job status
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/stats/rollups/jobs/{id}`.

Authorizatio#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/rollups/jobs/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/rollups/jobs/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

n: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `maintenanceStatus`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/rollups/jobs/{id}`

#### GET — Get maintenance job status
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/analytics/rollups/jobs/{id}`.

Authorizat#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/rollups/jobs/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/rollups/jobs/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ion: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `maintenanceStatus_1`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs/{id}/result`

#### GET — Fetch an analytics result
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/stats/jobs/{id}/result`.

Au#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/jobs/${ID}/result" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/jobs/{id}/result`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

thorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `result`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs/{id}/result`

#### GET — Fetch an analytics result
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/analytics/jobs/{id}/result`.

#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/jobs/${ID}/result" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/jobs/{id}/result`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `result_1`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs/{id}/download`

#### GET — Download an analytics result
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/analytics/jobs/{id}/download`.#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/jobs/${ID}/download" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/jobs/{id}/download`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```



Authorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `download`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs/{id}/download`

#### GET — Download an analytics result
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/stats/jobs/{id}/download`.

Auth#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/jobs/${ID}/download" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/jobs/{id}/download`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

orization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `download_1`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs/{id}`

#### GET — Get analytics job status
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/analytics/jobs/{id}`.

Autho#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/jobs/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/jobs/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

rization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `status`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/analytics/jobs/{id}`

#### DELETE — Delete an analytics job
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/analytics/jobs/{id}`.

A#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/analytics/jobs/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/analytics/jobs/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

uthorization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `delete_2`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs/{id}`

#### GET — Get analytics job status
#### Purpose and authorization

Purpose: `GET` performs the `GET` operation for `/api/v1/admin/stats/jobs/{id}`.

Authorizati#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request GET "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/jobs/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/jobs/{id}`, { method: "GET", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

on: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `status_1`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/admin/stats/jobs/{id}`

#### DELETE — Delete an analytics job
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/admin/stats/jobs/{id}`.

Autho#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/admin/stats/jobs/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/admin/stats/jobs/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

rization: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `delete_3`
- Tags: `Admin analytics`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/users/me/avatars/{id}`

#### DELETE — deleteAvatar
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/users/me/avatars/{id}`.

Authorizati#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/users/me/avatars/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/users/me/avatars/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

on: Bearer access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteAvatar`
- Tags: `avatar-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/notifications/{id}`

#### DELETE — deleteNotification
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/notifications/{id}`.

Authorization: Bearer acce#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/notifications/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/notifications/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ss token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteNotification`
- Tags: `notification-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/feed/{id}/media/{mediaId}`

#### DELETE — removeMedia
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/feed/{id}/media/{mediaId}`.

Authorization: Bearer access token require#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/feed/${ID}/media/${MEDIAID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/feed/{id}/media/{mediaId}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

d; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `removeMedia`
- Tags: `feed-post-controller`
- Parameters:
  - `id` (`path`, required)
  - `mediaId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/gallery/{id}`

#### DELETE — deleteImage
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/artisan/gallery/{id}`.

Authorization: Beare#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/gallery/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/gallery/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

r access token required; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteImage`
- Tags: `artisan-gallery-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/formations/{id}/files/{fileId}`

#### DELETE — deleteCourseFile
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/artisan/formations/{id}/files/{fileId}`.

Authorization: Bearer access token requir#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/formations/${ID}/files/${FILEID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/formations/{id}/files/{fileId}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ed; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteCourseFile`
- Tags: `artisan-formation-controller`
- Parameters:
  - `id` (`path`, required)
  - `fileId` (`path`, required)
- Responses:
  - `200` — OK

### `/api/v1/artisan/certifications/{id}`

#### DELETE — deleteCertification
#### Purpose and authorization

Purpose: `DELETE` performs the `DELETE` operation for `/api/v1/artisan/certifications/{id}`.

Authorization: Bearer access token requ#### HTTP example

The placeholders below are intentionally non-secret; replace path parameters and request fields with values from the schema.

```bash
curl --fail-with-body --request DELETE "${SOUKLAB_BASE_URL:-http://localhost:8080}/api/v1/artisan/certifications/${ID}" --header 'Authorization: Bearer ${SOUKLAB_ACCESS_TOKEN}'
```

```ts
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const accessToken = "<access-token>";
const response = await fetch(`${baseUrl}/api/v1/artisan/certifications/{id}`, { method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` } });
const payload = await response.json();
```

ired; the live contract and authorization matrix determine the required role/permission and ownership boundary.

Failure cases: use the response codes listed in the contract; common boundaries are 400 validation, 401 authentication, 403 authorization/ownership, 404 missing resource, 409 conflict, 413 upload size, 415 media type, 429 rate limit, and 5xx dependency failure.


- Operation ID: `deleteCertification`
- Tags: `artisan-certification-controller`
- Parameters:
  - `id` (`path`, required)
- Responses:
  - `200` — OK

## Schemas

### `ApiResponseAvatarResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AvatarResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `AvatarResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"urlOriginal":{"type":"string"},"urlMedium":{"type":"string"},"urlThumbnail":{"type":"string"},"originalFilename":{"type":"string"},"contentType":{"type":"string"},"fileSize":{"type":"integer","format":"int64"},"uploadedAt":{"type":"string","format":"date-time"},"active":{"type":"boolean"},"isActive":{"type":"boolean"}}}
```

### `ApiResponseNotificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/NotificationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `NotificationResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"message":{"type":"string"},"type":{"type":"string"},"targetId":{"type":"string"},"createdAt":{"type":"string","format":"date-time"},"read":{"type":"boolean"}}}
```

### `ApiResponseVoid`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FeedPostCreateDTO`

```json
{"type":"object","properties":{"type":{"type":"string","enum":["ACTUALITE","FORMATION","ANNONCE"]},"title":{"type":"string","maxLength":200,"minLength":0},"body":{"type":"string","maxLength":10000,"minLength":0},"formationId":{"type":"string","maxLength":36,"minLength":0}},"required":["body","title","type"]}
```

### `ApiResponseFeedPostResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FeedPostResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FeedPostMediaResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"url":{"type":"string"},"contentType":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `FeedPostResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"authorId":{"type":"string"},"authorName":{"type":"string"},"type":{"type":"string","enum":["ACTUALITE","FORMATION","ANNONCE"]},"title":{"type":"string"},"body":{"type":"string"},"status":{"type":"string","enum":["PENDING","PUBLISHED","HIDDEN","REMOVED"]},"formationId":{"type":"string"},"publishedAt":{"type":"string","format":"date-time"},"moderationNote":{"type":"string"},"media":{"type":"array","items":{"$ref":"#/components/schemas/FeedPostMediaResponseDTO"}}}}
```

### `ArtisanReviewRequestDTO`

```json
{"type":"object","properties":{"rating":{"type":"number","maximum":5.00,"minimum":0.00},"comment":{"type":"string","maxLength":5000,"minLength":0}},"required":["comment","rating"]}
```

### `ApiResponseArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ArtisanReviewResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"reviewerId":{"type":"string"},"reviewerName":{"type":"string"},"artisanId":{"type":"string"},"formationId":{"type":"string"},"rating":{"type":"number"},"comment":{"type":"string"},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"}}}
```

### `FormationUpdateDTO`

```json
{"type":"object","properties":{"title":{"type":"string","maxLength":255,"minLength":0},"description":{"type":"string","minLength":1},"location":{"type":"string"},"isOnline":{"type":"boolean"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32","minimum":1},"maxParticipants":{"type":"integer","format":"int32","minimum":1},"price":{"type":"integer","format":"int32","minimum":0},"currency":{"type":"string"},"online":{"type":"boolean"}},"required":["description","title"]}
```

### `ApiResponseFormationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormationAuthorDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"avatarUrl":{"type":"string"},"city":{"type":"string"},"teacher":{"type":"boolean"}}}
```

### `FormationFileResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"originalFilename":{"type":"string"},"contentType":{"type":"string"},"fileSize":{"type":"integer","format":"int64"},"downloadUrl":{"type":"string"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `FormationResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"author":{"$ref":"#/components/schemas/FormationAuthorDTO"},"title":{"type":"string"},"description":{"type":"string"},"thumbnailUrl":{"type":"string"},"location":{"type":"string"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32"},"maxParticipants":{"type":"integer","format":"int32"},"price":{"type":"integer","format":"int32"},"currency":{"type":"string"},"status":{"type":"string","enum":["DRAFT","PENDING_REVIEW","APPROVED","REJECTED","PUBLISHED","CANCELLED","COMPLETED"]},"activeEnrollmentsCount":{"type":"integer","format":"int64"},"files":{"type":"array","items":{"$ref":"#/components/schemas/FormationFileResponseDTO"}},"reviews":{"type":"array","items":{"$ref":"#/components/schemas/FormationReviewResponseDTO"}},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"},"online":{"type":"boolean"}}}
```

### `FormationReviewResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"adminId":{"type":"string"},"adminName":{"type":"string"},"decision":{"type":"string","enum":["APPROVED","REJECTED"]},"comment":{"type":"string"},"reviewedAt":{"type":"string","format":"date-time"}}}
```

### `SubscriptionPlanRequest`

```json
{"type":"object","properties":{"subscriberType":{"type":"string","enum":["ARTISAN","CLIENT"]},"name":{"type":"string","minLength":1},"description":{"type":"string"},"billingPeriod":{"type":"string","enum":["MONTHLY","YEARLY"]},"amount":{"type":"integer","format":"int64","exclusiveMinimum":0},"active":{"type":"boolean"},"entitlements":{"type":"object","additionalProperties":{"type":"string"}},"reason":{"type":"string","minLength":1}},"required":["billingPeriod","name","reason","subscriberType"]}
```

### `ApiResponseSubscriptionPlanResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/SubscriptionPlanResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `SubscriptionPlanResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"subscriberType":{"type":"string","enum":["ARTISAN","CLIENT"]},"name":{"type":"string"},"description":{"type":"string"},"billingPeriod":{"type":"string","enum":["MONTHLY","YEARLY"]},"amount":{"type":"integer","format":"int64"},"currency":{"type":"string"},"active":{"type":"boolean"},"entitlements":{"type":"object","additionalProperties":{"type":"string"}}}}
```

### `TechniqueRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseTechniqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/TechniqueDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `TechniqueDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `JobSubCategoryRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"categoryId":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"status":{"type":"boolean","writeOnly":true},"active":{"type":"boolean","writeOnly":true},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseJobSubCategoryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/JobSubCategoryDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `JobSubCategoryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"categoryId":{"type":"string"}}}
```

### `RegionRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"parentId":{"type":"string"},"code":{"type":"string","maxLength":10,"minLength":0},"displayOrder":{"type":"integer","format":"int32"},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseRegionDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/RegionDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `RegionDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"code":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"children":{"type":"array","items":{"$ref":"#/components/schemas/RegionDTO"}}}}
```

### `MaterialRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"familyId":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"status":{"type":"boolean","writeOnly":true},"active":{"type":"boolean","writeOnly":true},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseMaterialDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/MaterialDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MaterialDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"familyId":{"type":"string"}}}
```

### `MaterialFamilyRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"status":{"type":"boolean","writeOnly":true},"active":{"type":"boolean","writeOnly":true},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseMaterialFamilyDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/MaterialFamilyDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MaterialFamilyDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"materials":{"type":"array","items":{"$ref":"#/components/schemas/MaterialDTO"}}}}
```

### `EpoqueRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"periodEra":{"type":"string","maxLength":100,"minLength":0},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseEpoqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/EpoqueDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `EpoqueDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"periodEra":{"type":"string"},"description":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `JobCategoryRequest`

```json
{"type":"object","properties":{"name":{"type":"string","maxLength":100,"minLength":0},"slug":{"type":"string","maxLength":120,"minLength":0},"description":{"type":"string"},"iconUrl":{"type":"string","maxLength":500,"minLength":0},"displayOrder":{"type":"integer","format":"int32"},"status":{"type":"boolean","writeOnly":true},"active":{"type":"boolean","writeOnly":true},"isActive":{"type":"boolean"}},"required":["name"]}
```

### `ApiResponseJobCategoryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/JobCategoryDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `JobCategoryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"description":{"type":"string"},"iconUrl":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"},"subCategories":{"type":"array","items":{"$ref":"#/components/schemas/JobSubCategoryDTO"}}}}
```

### `SubscriptionCheckoutRequest`

```json
{"type":"object","properties":{"planId":{"type":"string","minLength":1},"successUrl":{"type":"string"},"failureUrl":{"type":"string"}},"required":["planId"]}
```

### `ApiResponseSubscriptionCheckoutResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/SubscriptionCheckoutResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `SubscriptionCheckoutResponse`

```json
{"type":"object","properties":{"paymentId":{"type":"string"},"subscriptionId":{"type":"string"},"providerCheckoutId":{"type":"string"},"checkoutUrl":{"type":"string"}}}
```

### `ContentReportRequestDTO`

```json
{"type":"object","properties":{"targetType":{"type":"string","enum":["USER","POST","REVIEW"]},"targetId":{"type":"string","maxLength":36,"minLength":0},"reason":{"type":"string","maxLength":100,"minLength":0},"details":{"type":"string","maxLength":5000,"minLength":0}},"required":["reason","targetId","targetType"]}
```

### `ApiResponseContentReportResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ContentReportResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ContentReportResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"reporterId":{"type":"string"},"targetType":{"type":"string","enum":["USER","POST","REVIEW"]},"targetId":{"type":"string"},"reason":{"type":"string"},"details":{"type":"string"},"status":{"type":"string","enum":["OPEN","DISMISSED","RESOLVED"]},"resolutionAction":{"type":"string","enum":["DISMISS","HIDE","REMOVE"]},"resolverId":{"type":"string"},"resolutionNote":{"type":"string"},"resolvedAt":{"type":"string","format":"date-time"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponseFeedPostMediaResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FeedPostMediaResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `CreateConversationRequest`

```json
{"type":"object","properties":{"recipientUserId":{"type":"string","minLength":1}},"required":["recipientUserId"]}
```

### `ApiResponseConversationResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ConversationResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ConversationResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"participantUserId":{"type":"string"},"participantName":{"type":"string"},"archived":{"type":"boolean"},"lastMessagePreview":{"type":"string"},"unreadCount":{"type":"integer","format":"int64"},"updatedAt":{"type":"string","format":"date-time"}}}
```

### `ReadReceiptRequest`

```json
{"type":"object","properties":{"messageId":{"type":"string"}}}
```

### `SendMessageRequest`

```json
{"type":"object","properties":{"idempotencyKey":{"type":"string","maxLength":128,"minLength":0},"content":{"type":"string","minLength":1},"attachmentKeys":{"type":"array","items":{"type":"string","minLength":1}}},"required":["content","idempotencyKey"]}
```

### `ApiResponseMessageResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/MessageResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MessageAttachmentResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"filename":{"type":"string"},"contentType":{"type":"string"},"size":{"type":"integer","format":"int64"},"downloadUrl":{"type":"string"}}}
```

### `MessageResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"conversationId":{"type":"string"},"authorId":{"type":"string"},"content":{"type":"string"},"deleted":{"type":"boolean"},"createdAt":{"type":"string","format":"date-time"},"editedAt":{"type":"string","format":"date-time"},"attachments":{"type":"array","items":{"$ref":"#/components/schemas/MessageAttachmentResponse"}}}}
```

### `ApiResponseAttachmentUploadResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AttachmentUploadResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `AttachmentUploadResponse`

```json
{"type":"object","properties":{"key":{"type":"string"},"filename":{"type":"string"},"contentType":{"type":"string"},"size":{"type":"integer","format":"int64"}}}
```

### `VerifyEmailRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"code":{"type":"string","minLength":1,"pattern":"\\d{6}"}},"required":["code","email"]}
```

### `ResetPasswordRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"code":{"type":"string","minLength":1,"pattern":"\\d{6}"},"newPassword":{"type":"string","maxLength":2147483647,"minLength":8}},"required":["code","email","newPassword"]}
```

### `ResendVerificationRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1}},"required":["email"]}
```

### `UserRegistrationDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1},"password":{"type":"string","maxLength":2147483647,"minLength":8},"name":{"type":"string"},"firstName":{"type":"string"},"lastName":{"type":"string"},"accountType":{"type":"string","enum":["ADMIN","ARTISAN","CLIENT"]}},"required":["accountType","email","password"]}
```

### `ApiResponseProfileResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ProfileResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ProfileResponse`

```json
{"type":"object","properties":{"name":{"type":"string"},"permissions":{"type":"array","items":{"type":"string"},"uniqueItems":true},"id":{"type":"string"},"email":{"type":"string"},"accountStatus":{"type":"string","enum":["PENDING","ACTIVE","SUSPENDED","REJECTED"]},"createdAt":{"type":"string","format":"date-time"},"phone":{"type":"string"},"avatarUrl":{"type":"string"},"emailVerified":{"type":"boolean"},"firstName":{"type":"string"},"lastName":{"type":"string"},"updatedAt":{"type":"string","format":"date-time"},"emailVerifiedAt":{"type":"string","format":"date-time"}}}
```

### `TokenRefreshRequestDTO`

```json
{"type":"object","properties":{"refreshToken":{"type":"string","minLength":1}},"required":["refreshToken"]}
```

### `ApiResponseJwtResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/JwtResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `JwtResponseDTO`

```json
{"type":"object","properties":{"accessToken":{"type":"string"},"refreshToken":{"type":"string"},"tokenType":{"type":"string","enum":["Bearer"]},"expiresIn":{"type":"integer","format":"int64"},"user":{"$ref":"#/components/schemas/ProfileResponse"},"permissions":{"type":"array","items":{"type":"string"}}}}
```

### `LoginDTO`

```json
{"type":"object","properties":{"email":{"type":"string"},"username":{"type":"string"},"password":{"type":"string","minLength":1},"loginIdentifier":{"type":"string"}},"required":["password"]}
```

### `ForgotPasswordRequestDTO`

```json
{"type":"object","properties":{"email":{"type":"string","format":"email","minLength":1}},"required":["email"]}
```

### `CompleteProfileRequestDTO`

```json
{"type":"object","properties":{"regionId":{"type":"string"},"region":{"type":"string"},"city":{"type":"string"},"bio":{"type":"string"},"address":{"type":"string"},"website":{"type":"string"},"subCategoryId":{"type":"string"},"materialIds":{"type":"array","items":{"type":"string"}},"epoqueIds":{"type":"array","items":{"type":"string"}},"techniqueIds":{"type":"array","items":{"type":"string"}},"clientType":{"type":"string","enum":["INDIVIDUAL","BUSINESS","ENTERPRISE"]},"companyName":{"type":"string"}}}
```

### `ChangePasswordRequestDTO`

```json
{"type":"object","properties":{"oldPassword":{"type":"string","minLength":1},"newPassword":{"type":"string","maxLength":2147483647,"minLength":8}},"required":["newPassword","oldPassword"]}
```

### `ApiResponseGalleryImageResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/GalleryImageResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `GalleryImageResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"imageUrl":{"type":"string"},"title":{"type":"string"},"caption":{"type":"string"},"displayOrder":{"type":"integer","format":"int32"}}}
```

### `FormationCreateDTO`

```json
{"type":"object","properties":{"title":{"type":"string","maxLength":255,"minLength":0},"description":{"type":"string","minLength":1},"location":{"type":"string"},"isOnline":{"type":"boolean"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32","minimum":1},"maxParticipants":{"type":"integer","format":"int32","minimum":1},"price":{"type":"integer","format":"int32","minimum":0},"currency":{"type":"string"},"online":{"type":"boolean"}},"required":["description","title"]}
```

### `ApiResponseFormationFileResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormationFileResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseFormationEnrollmentResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormationEnrollmentResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormationEnrollmentResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"formationId":{"type":"string"},"formationTitle":{"type":"string"},"artisanId":{"type":"string"},"artisanName":{"type":"string"},"status":{"type":"string","enum":["CONFIRMED","ATTENDED","CANCELLED"]},"enrolledAt":{"type":"string","format":"date-time"},"cancelledAt":{"type":"string","format":"date-time"}}}
```

### `FormateurRequestDTO`

```json
{"type":"object","properties":{"motivation":{"type":"string"}}}
```

### `ApiResponseFormateurRequestResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormateurRequestResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormateurRequestResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"artisanId":{"type":"string"},"artisanName":{"type":"string"},"artisanEmail":{"type":"string"},"status":{"type":"string","enum":["PENDING","APPROVED","REJECTED"]},"motivation":{"type":"string"},"adminNote":{"type":"string"},"canReapply":{"type":"boolean"},"cooldownUntil":{"type":"string","format":"date-time"},"decidedByAdminId":{"type":"string"},"decidedByAdminEmail":{"type":"string"},"decidedAt":{"type":"string","format":"date-time"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponseCertificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/CertificationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `CertificationResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"title":{"type":"string"},"issuer":{"type":"string"},"issuedAt":{"type":"string","format":"date"},"expiresAt":{"type":"string","format":"date"},"documentUrl":{"type":"string"},"verified":{"type":"boolean"}}}
```

### `PermissionAssignmentRequestDTO`

```json
{"type":"object","properties":{"permissionKey":{"type":"string"}},"required":["permissionKey"]}
```

### `ApiResponseSetPermission`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"type":"string"},"uniqueItems":true},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `TimeoutRequestDTO`

```json
{"type":"object","properties":{"minutes":{"type":"integer","format":"int32","exclusiveMinimum":0},"reason":{"type":"string"}}}
```

### `BanRequestDTO`

```json
{"type":"object","properties":{"reason":{"type":"string","minLength":1}},"required":["reason"]}
```

### `FinancialReasonRequest`

```json
{"type":"object","properties":{"reason":{"type":"string","minLength":1}},"required":["reason"]}
```

### `SubscriptionStateCorrectionRequest`

```json
{"type":"object","properties":{"status":{"type":"string","enum":["PENDING","ACTIVE","CANCELED","EXPIRED","REVOKED"]},"reason":{"type":"string","minLength":1}},"required":["reason","status"]}
```

### `PaymentStateCorrectionRequest`

```json
{"type":"object","properties":{"status":{"type":"string","enum":["CREATED","PENDING","PAID","FAILED","CANCELED","EXPIRED","MANUALLY_GRANTED"]},"reason":{"type":"string","minLength":1}},"required":["reason","status"]}
```

### `ManualSubscriptionGrantRequest`

```json
{"type":"object","properties":{"accountId":{"type":"string","minLength":1},"planId":{"type":"string","minLength":1},"reason":{"type":"string","minLength":1}},"required":["accountId","planId","reason"]}
```

### `ApiResponseSubscriptionResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/SubscriptionResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `SubscriptionResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"subscriberType":{"type":"string","enum":["ARTISAN","CLIENT"]},"status":{"type":"string","enum":["PENDING","ACTIVE","CANCELED","EXPIRED","REVOKED"]},"planName":{"type":"string"},"billingPeriod":{"type":"string","enum":["MONTHLY","YEARLY"]},"amount":{"type":"integer","format":"int64"},"currency":{"type":"string"},"startsAt":{"type":"string","format":"date-time"},"expiresAt":{"type":"string","format":"date-time"}}}
```

### `ReportResolutionRequestDTO`

```json
{"type":"object","properties":{"action":{"type":"string","enum":["DISMISS","HIDE","REMOVE"]},"note":{"type":"string","maxLength":2000,"minLength":0}},"required":["action","note"]}
```

### `FormationReviewRequestDTO`

```json
{"type":"object","properties":{"decision":{"type":"string","enum":["APPROVED","REJECTED"]},"comment":{"type":"string"}},"required":["decision"]}
```

### `FormateurRejectDTO`

```json
{"type":"object","properties":{"adminNote":{"type":"string","minLength":1},"cooldownUntil":{"type":"string","format":"date-time"},"canReapply":{"type":"boolean"}},"required":["adminNote"]}
```

### `FormateurApproveDTO`

```json
{"type":"object","properties":{"adminNote":{"type":"string","minLength":1}},"required":["adminNote"]}
```

### `FormateurCooldownOverrideDTO`

```json
{"type":"object","properties":{"canReapply":{"type":"boolean"},"cooldownUntil":{"type":"string","format":"date-time"}}}
```

### `FeedPostModerationDTO`

```json
{"type":"object","properties":{"note":{"type":"string","maxLength":2000,"minLength":0}},"required":["note"]}
```

### `FormateurRevokeDTO`

```json
{"type":"object","properties":{"reason":{"type":"string","minLength":1}},"required":["reason"]}
```

### `FormateurGrantDTO`

```json
{"type":"object","properties":{"adminNote":{"type":"string","minLength":1}},"required":["adminNote"]}
```

### `AnalyticsRebuildRequest`

```json
{"type":"object","properties":{"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"}},"required":["fromDate","toDate"]}
```

### `AnalyticsMaintenanceJobResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"operation":{"type":"string","enum":["REBUILD","BACKFILL"]},"status":{"type":"string","enum":["QUEUED","RUNNING","COMPLETED","FAILED","EXPIRED"]},"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"eventsRead":{"type":"integer","format":"int32"},"rollupsWritten":{"type":"integer","format":"int32"},"completedAt":{"type":"string","format":"date-time"},"expiresAt":{"type":"string","format":"date-time"},"failureMessage":{"type":"string"}}}
```

### `ApiResponseAnalyticsMaintenanceJobResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AnalyticsMaintenanceJobResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `AnalyticsJobRequest`

```json
{"type":"object","properties":{"reportType":{"type":"string"},"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"bucket":{"type":"string","enum":["DAY","WEEK","MONTH","QUARTER"]},"filters":{"type":"object","additionalProperties":{"type":"string"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"sortField":{"type":"string"},"sortDirection":{"type":"string","enum":["ASC","DESC"]},"outputFormat":{"type":"string","enum":["JSON","CSV"]}},"required":["bucket","fromDate","reportType","toDate"]}
```

### `AnalyticsJobResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"reportType":{"type":"string"},"status":{"type":"string","enum":["QUEUED","RUNNING","COMPLETED","FAILED","EXPIRED"]},"bucket":{"type":"string","enum":["DAY","WEEK","MONTH","QUARTER"]},"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"outputFormat":{"type":"string","enum":["JSON","CSV"]},"completedAt":{"type":"string","format":"date-time"},"expiresAt":{"type":"string","format":"date-time"},"failureMessage":{"type":"string"}}}
```

### `ApiResponseAnalyticsJobResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AnalyticsJobResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ArchiveConversationRequest`

```json
{"type":"object","properties":{"archived":{"type":"boolean"}}}
```

### `EditMessageRequest`

```json
{"type":"object","properties":{"content":{"type":"string","minLength":1}},"required":["content"]}
```

### `PatchFieldClientType`

```json
{"type":"object","properties":{"defined":{"type":"boolean"},"value":{"type":"string","enum":["INDIVIDUAL","BUSINESS","ENTERPRISE"]},"null":{"type":"boolean"}}}
```

### `PatchFieldListString`

```json
{"type":"object","properties":{"defined":{"type":"boolean"},"value":{"type":"array","items":{"type":"string"}},"null":{"type":"boolean"}}}
```

### `PatchFieldString`

```json
{"type":"object","properties":{"defined":{"type":"boolean"},"value":{"type":"string"},"null":{"type":"boolean"}}}
```

### `UserPatchDTO`

```json
{"type":"object","properties":{"bio":{"$ref":"#/components/schemas/PatchFieldString"},"city":{"$ref":"#/components/schemas/PatchFieldString"},"address":{"$ref":"#/components/schemas/PatchFieldString"},"website":{"$ref":"#/components/schemas/PatchFieldString"},"regionId":{"$ref":"#/components/schemas/PatchFieldString"},"region":{"$ref":"#/components/schemas/PatchFieldString"},"subCategoryId":{"$ref":"#/components/schemas/PatchFieldString"},"materialIds":{"$ref":"#/components/schemas/PatchFieldListString"},"techniqueIds":{"$ref":"#/components/schemas/PatchFieldListString"},"epoqueIds":{"$ref":"#/components/schemas/PatchFieldListString"},"companyName":{"$ref":"#/components/schemas/PatchFieldString"},"clientType":{"$ref":"#/components/schemas/PatchFieldClientType"},"empty":{"type":"boolean"}}}
```

### `Pageable`

```json
{"type":"object","properties":{"page":{"type":"integer","format":"int32","minimum":0},"size":{"type":"integer","format":"int32","minimum":1},"sort":{"type":"array","items":{"type":"string"}}}}
```

### `ApiResponsePaginatedResponseAvatarResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseAvatarResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseAvatarResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/AvatarResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseListSubscriptionResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/SubscriptionResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListSubscriptionPlanResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/SubscriptionPlanResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `DirectorySearchFilterDTO`

```json
{"type":"object","properties":{"keyword":{"type":"string","maxLength":120,"minLength":0},"regionSlug":{"type":"string","maxLength":120,"minLength":0},"wilayaCode":{"type":"string","maxLength":10,"minLength":0},"categorySlug":{"type":"string","maxLength":120,"minLength":0},"subCategorySlug":{"type":"string","maxLength":120,"minLength":0},"materials":{"type":"array","items":{"type":"string"}},"techniques":{"type":"array","items":{"type":"string"}},"epoques":{"type":"array","items":{"type":"string"}},"minRating":{"type":"number","format":"double","maximum":5.0,"minimum":0.0},"verifiedOnly":{"type":"boolean"},"premiumOnly":{"type":"boolean"},"teacherOnly":{"type":"boolean"},"sortBy":{"type":"string"},"page":{"type":"integer","format":"int32","minimum":0},"size":{"type":"integer","format":"int32","maximum":100,"minimum":1},"cleanKeyword":{"type":"string"},"q":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseArtisanDirectoryCardDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseArtisanDirectoryCardDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ArtisanDirectoryCardDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"artisanName":{"type":"string"},"avatarUrl":{"type":"string"},"coverImageUrl":{"type":"string"},"bioSnippet":{"type":"string"},"city":{"type":"string"},"wilayaName":{"type":"string"},"wilayaCode":{"type":"string"},"regionSlug":{"type":"string"},"categoryName":{"type":"string"},"categorySlug":{"type":"string"},"subCategoryName":{"type":"string"},"subCategorySlug":{"type":"string"},"rating":{"type":"number","format":"double"},"reviewsCount":{"type":"integer","format":"int32"},"viewsCount":{"type":"integer","format":"int32"},"verified":{"type":"boolean"},"premium":{"type":"boolean"},"teacher":{"type":"boolean"},"primaryMaterials":{"type":"array","items":{"type":"string"}},"primaryTechniques":{"type":"array","items":{"type":"string"}},"createdAt":{"type":"string","format":"date-time"}}}
```

### `PaginatedResponseArtisanDirectoryCardDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/ArtisanDirectoryCardDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseListPaymentResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/PaymentResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaymentResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"subscriptionId":{"type":"string"},"provider":{"type":"string","enum":["CHARGILY"]},"status":{"type":"string","enum":["CREATED","PENDING","PAID","FAILED","CANCELED","EXPIRED","MANUALLY_GRANTED"]},"amount":{"type":"integer","format":"int64"},"currency":{"type":"string"},"checkoutUrl":{"type":"string"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponsePaymentResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaymentResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseNotificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseNotificationResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseNotificationResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/NotificationResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseLong`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"integer","format":"int64"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `StreamingResponseBody`

```json
{}
```

### `ApiResponsePageFeedPostResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PageFeedPostResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PageFeedPostResponseDTO`

```json
{"type":"object","properties":{"totalPages":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"size":{"type":"integer","format":"int32"},"content":{"type":"array","items":{"$ref":"#/components/schemas/FeedPostResponseDTO"}},"number":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"pageable":{"$ref":"#/components/schemas/PageableObject"},"numberOfElements":{"type":"integer","format":"int32"},"first":{"type":"boolean"},"last":{"type":"boolean"},"empty":{"type":"boolean"}}}
```

### `PageableObject`

```json
{"type":"object","properties":{"offset":{"type":"integer","format":"int64"},"paged":{"type":"boolean"},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"unpaged":{"type":"boolean"}}}
```

### `SortObject`

```json
{"type":"object","properties":{"empty":{"type":"boolean"},"sorted":{"type":"boolean"},"unsorted":{"type":"boolean"}}}
```

### `ApiResponseListConversationResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/ConversationResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseMessagePageResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/MessagePageResponse"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `MessagePageResponse`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/MessageResponse"}},"nextCursor":{"type":"string"},"last":{"type":"boolean"}}}
```

### `ApiResponseListTechniqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/TechniqueDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListRegionDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/RegionDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListMaterialFamilyDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/MaterialFamilyDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListEpoqueDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/EpoqueDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponseListJobCategoryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/JobCategoryDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePageArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PageArtisanReviewResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PageArtisanReviewResponseDTO`

```json
{"type":"object","properties":{"totalPages":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"size":{"type":"integer","format":"int32"},"content":{"type":"array","items":{"$ref":"#/components/schemas/ArtisanReviewResponseDTO"}},"number":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"pageable":{"$ref":"#/components/schemas/PageableObject"},"numberOfElements":{"type":"integer","format":"int32"},"first":{"type":"boolean"},"last":{"type":"boolean"},"empty":{"type":"boolean"}}}
```

### `ApiResponseArtisanPublicViewDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ArtisanPublicViewDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ArtisanPublicViewDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"bio":{"type":"string"},"city":{"type":"string"},"regionId":{"type":"string"},"region":{"$ref":"#/components/schemas/RegionSummaryDTO"},"subCategoryId":{"type":"string"},"subCategory":{"$ref":"#/components/schemas/JobSubCategorySummaryDTO"},"materials":{"type":"array","items":{"$ref":"#/components/schemas/MaterialSummaryDTO"},"uniqueItems":true},"techniques":{"type":"array","items":{"$ref":"#/components/schemas/TechniqueSummaryDTO"},"uniqueItems":true},"epoques":{"type":"array","items":{"$ref":"#/components/schemas/EpoqueSummaryDTO"},"uniqueItems":true},"galleryImages":{"type":"array","items":{"$ref":"#/components/schemas/GalleryImageResponseDTO"}},"certifications":{"type":"array","items":{"$ref":"#/components/schemas/CertificationResponseDTO"}},"rating":{"type":"number","format":"double"},"reviewsCount":{"type":"integer","format":"int32"},"teacher":{"type":"boolean"},"verified":{"type":"boolean"},"avatarUrl":{"type":"string"},"createdAt":{"type":"string","format":"date-time"},"contactInfoLocked":{"type":"boolean"},"name":{"type":"string"},"phone":{"type":"string"},"email":{"type":"string"},"website":{"type":"string"},"address":{"type":"string"}}}
```

### `EpoqueSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"periodEra":{"type":"string"}}}
```

### `JobSubCategorySummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"categoryId":{"type":"string"},"categoryName":{"type":"string"}}}
```

### `MaterialSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"familyId":{"type":"string"},"familyName":{"type":"string"}}}
```

### `RegionSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"},"code":{"type":"string"}}}
```

### `TechniqueSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"name":{"type":"string"},"slug":{"type":"string"}}}
```

### `ApiResponseListGalleryImageResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/GalleryImageResponseDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseFormationEnrollmentDetailDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFormationEnrollmentDetailDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormationEnrollmentDetailDTO`

```json
{"type":"object","properties":{"enrollment":{"$ref":"#/components/schemas/FormationEnrollmentResponseDTO"},"formation":{"$ref":"#/components/schemas/FormationSummaryDTO"}}}
```

### `FormationSummaryDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"author":{"$ref":"#/components/schemas/FormationAuthorDTO"},"title":{"type":"string"},"thumbnailUrl":{"type":"string"},"location":{"type":"string"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32"},"maxParticipants":{"type":"integer","format":"int32"},"price":{"type":"integer","format":"int32"},"currency":{"type":"string"},"status":{"type":"string","enum":["DRAFT","PENDING_REVIEW","APPROVED","REJECTED","PUBLISHED","CANCELLED","COMPLETED"]},"activeEnrollmentsCount":{"type":"integer","format":"int64"},"createdAt":{"type":"string","format":"date-time"},"online":{"type":"boolean"}}}
```

### `PaginatedResponseFormationEnrollmentDetailDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FormationEnrollmentDetailDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponsePaginatedResponseFormationSummaryDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFormationSummaryDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseFormationSummaryDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FormationSummaryDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseFormationPublicViewDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FormationPublicViewDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FormationFileDescriptorDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"filename":{"type":"string"},"contentType":{"type":"string"},"fileSize":{"type":"integer","format":"int64"},"downloadUrl":{"type":"string"}}}
```

### `FormationPublicViewDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"author":{"$ref":"#/components/schemas/FormationAuthorDTO"},"title":{"type":"string"},"description":{"type":"string"},"thumbnailUrl":{"type":"string"},"location":{"type":"string"},"scheduledAt":{"type":"string","format":"date-time"},"durationHours":{"type":"integer","format":"int32"},"maxParticipants":{"type":"integer","format":"int32"},"price":{"type":"integer","format":"int32"},"currency":{"type":"string"},"status":{"type":"string","enum":["DRAFT","PENDING_REVIEW","APPROVED","REJECTED","PUBLISHED","CANCELLED","COMPLETED"]},"activeEnrollmentsCount":{"type":"integer","format":"int64"},"availableSeats":{"type":"integer","format":"int64"},"files":{"type":"array","items":{"$ref":"#/components/schemas/FormationFileDescriptorDTO"}},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"},"enrolled":{"type":"boolean"},"online":{"type":"boolean"},"isOnline":{"type":"boolean"},"isEnrolled":{"type":"boolean"}}}
```

### `ApiResponseListCertificationResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/CertificationResponseDTO"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePaginatedResponseUserResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseUserResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseUserResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/UserResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `UserResponseDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"email":{"type":"string"},"firstName":{"type":"string"},"lastName":{"type":"string"},"name":{"type":"string"},"phone":{"type":"string"},"avatarUrl":{"type":"string"},"status":{"type":"string","enum":["PENDING","ACTIVE","SUSPENDED","REJECTED"]},"emailVerified":{"type":"boolean"},"emailVerifiedAt":{"type":"string","format":"date-time"},"permissions":{"type":"array","items":{"type":"string"},"uniqueItems":true},"bannedUntil":{"type":"string","format":"date-time"},"banReason":{"type":"string"},"lastLoginAt":{"type":"string","format":"date-time"},"createdAt":{"type":"string","format":"date-time"},"updatedAt":{"type":"string","format":"date-time"},"teacher":{"type":"boolean"},"premium":{"type":"boolean"},"validated":{"type":"boolean"}}}
```

### `ApiResponsePaginatedResponseAuditLogDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseAuditLogDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `AuditLogDTO`

```json
{"type":"object","properties":{"id":{"type":"string"},"action":{"type":"string"},"details":{"type":"string"},"userEmail":{"type":"string"},"userId":{"type":"string"},"targetAccountId":{"type":"string"},"operation":{"type":"string"},"previousState":{"type":"string"},"newState":{"type":"string"},"reason":{"type":"string"},"paymentId":{"type":"string"},"subscriptionId":{"type":"string"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `PaginatedResponseAuditLogDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/AuditLogDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `AdminWebhookLogResponse`

```json
{"type":"object","properties":{"id":{"type":"string"},"providerEventId":{"type":"string"},"eventType":{"type":"string","enum":["checkout.paid","checkout.failed","checkout.canceled","unknown"]},"signatureValid":{"type":"boolean"},"status":{"type":"string","enum":["RECEIVED","PROCESSING","PROCESSED","IGNORED","FAILED"]},"providerCheckoutId":{"type":"string"},"failureReason":{"type":"string"},"createdAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponseListAdminWebhookLogResponse`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"type":"array","items":{"$ref":"#/components/schemas/AdminWebhookLogResponse"}},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ApiResponsePageContentReportResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PageContentReportResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PageContentReportResponseDTO`

```json
{"type":"object","properties":{"totalPages":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"size":{"type":"integer","format":"int32"},"content":{"type":"array","items":{"$ref":"#/components/schemas/ContentReportResponseDTO"}},"number":{"type":"integer","format":"int32"},"sort":{"$ref":"#/components/schemas/SortObject"},"pageable":{"$ref":"#/components/schemas/PageableObject"},"numberOfElements":{"type":"integer","format":"int32"},"first":{"type":"boolean"},"last":{"type":"boolean"},"empty":{"type":"boolean"}}}
```

### `ApiResponsePaginatedResponseFormateurRequestResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseFormateurRequestResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseFormateurRequestResponseDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/FormateurRequestResponseDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `AnalyticsResult`

```json
{"type":"object","properties":{"reportType":{"type":"string"},"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"bucket":{"type":"string","enum":["DAY","WEEK","MONTH","QUARTER"]},"summary":{"type":"object","additionalProperties":{}},"series":{"$ref":"#/components/schemas/PaginatedResponseMapKeyObject"},"tables":{"type":"object","additionalProperties":{"$ref":"#/components/schemas/PaginatedResponseMapCsvObject"}}}}
```

### `ApiResponseAnalyticsResult`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/AnalyticsResult"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseMapCsvObject`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"type":"object","additionalProperties":{}}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `PaginatedResponseMapKeyObject`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"type":"object","additionalProperties":{}}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ApiResponseClientFavoriteArtisanResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/ClientFavoriteArtisanResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `ClientFavoriteArtisanResponseDTO`

```json
{"type":"object","properties":{"favoriteId":{"type":"string"},"artisanId":{"type":"string"},"favoritedAt":{"type":"string","format":"date-time"}}}
```

### `ApiResponsePaginatedResponseClientFavoriteArtisanItemDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/PaginatedResponseClientFavoriteArtisanItemDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `PaginatedResponseClientFavoriteArtisanItemDTO`

```json
{"type":"object","properties":{"content":{"type":"array","items":{"$ref":"#/components/schemas/ClientFavoriteArtisanItemDTO"}},"pageNumber":{"type":"integer","format":"int32"},"pageSize":{"type":"integer","format":"int32"},"totalElements":{"type":"integer","format":"int64"},"totalPages":{"type":"integer","format":"int32"},"last":{"type":"boolean"}}}
```

### `ClientFavoriteArtisanItemDTO`

```json
{"type":"object","properties":{"favoritedAt":{"type":"string","format":"date-time"},"artisan":{"$ref":"#/components/schemas/ArtisanDirectoryCardDTO"}}}
```

### `ApiResponseFavoriteStatusResponseDTO`

```json
{"type":"object","properties":{"success":{"type":"boolean"},"code":{"type":"integer","format":"int32"},"errorCode":{"type":"string"},"message":{"type":"string"},"data":{"$ref":"#/components/schemas/FavoriteStatusResponseDTO"},"errors":{"type":"object","additionalProperties":{"type":"string"}},"traceId":{"type":"string"}}}
```

### `FavoriteStatusResponseDTO`

```json
{"type":"object","properties":{"favorited":{"type":"boolean"}}}
```

