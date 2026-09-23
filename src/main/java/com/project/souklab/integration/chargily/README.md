# Chargily Pay V2 Integration (`com.project.souklab.integration.chargily`)

HTTP client adapters, request/response mappers, and payload deserializers communicating with the Chargily Pay V2 REST API.

---

## Classes & Responsibilities

| Class | Type | Responsibility |
| :--- | :--- | :--- |
| [`ChargilyClient`](ChargilyClient.java) | Service / Client | Executes outbound HTTPS requests to `https://pay.chargily.net/api/v2/checkouts` with bearer authorization. |
| [`ChargilyCheckoutClient`](ChargilyCheckoutClient.java) | Interface | Client abstraction allowing test stubbing and production HTTP switching. |
| [`ChargilyClientConfiguration`](ChargilyClientConfiguration.java) | Configuration | Configures HTTP timeout, headers, and client credentials from `app.chargily.*` properties. |
| [`ChargilyCheckoutRequest`](ChargilyCheckoutRequest.java) | Payload | Outbound JSON request schema for creating Chargily checkout sessions. |
| [`ChargilyCheckoutResponse`](ChargilyCheckoutResponse.java) | Payload | Deserialized JSON response from Chargily containing hosted payment URL and ID. |
| [`ChargilyWebhookPayload`](ChargilyWebhookPayload.java) | Payload | Inbound webhook envelope deserializer matching Chargily Pay V2 event schemas. |
| [`ChargilyRequestMapper`](ChargilyRequestMapper.java) & [`ChargilyResponseMapper`](ChargilyResponseMapper.java) | Mappers | Converts between domain entities and Chargily wire-format DTOs. |
| [`ChargilyErrorClassifier`](ChargilyErrorClassifier.java) | Exception Classifier | Categorizes HTTP status codes (4xx client vs 5xx provider) into structured domain errors. |
| [`ChargilyProviderException`](ChargilyProviderException.java) | Exception | Domain runtime exception raised on communication or provider errors. |
