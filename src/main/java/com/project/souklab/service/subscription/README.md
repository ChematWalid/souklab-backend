# Subscription & Payment Services (`com.project.souklab.service.subscription`)

Transactional services managing subscription lifecycles, Chargily Pay V2 integration, webhook processing, and cryptographic security.

---

## Services Reference

| Service | Responsibility |
| :--- | :--- |
| [`SubscriptionCheckoutService`](SubscriptionCheckoutService.java) | Creates pending payment records and initiates hosted checkout sessions with Chargily. |
| [`ChargilyWebhookService`](ChargilyWebhookService.java) | Processes incoming webhooks, validates signatures, claims event IDs for idempotency, and updates payments/subscriptions. |
| [`WebhookSecurityService`](WebhookSecurityService.java) | Cryptographic HMAC-SHA256 signature validation against the configured Chargily webhook secret. |
| [`WebhookEventClaimService`](WebhookEventClaimService.java) | Database-backed event deduplication preventing double-crediting or duplicate processing. |
| [`SubscriptionLifecycleService`](SubscriptionLifecycleService.java) | Manages subscription activations, renewals, grace periods, and expiration transitions. |
| [`SubscriptionAccountService`](SubscriptionAccountService.java) | Evaluates user entitlements, active plans, and billing history. |
| [`SubscriptionPlanService`](SubscriptionPlanService.java) | Cached retrieval of publicly active subscription plans and single plan lookups. |
| [`SubscriptionPlanRules`](SubscriptionPlanRules.java) | Domain entitlement rules defining limits (portfolio size, formation count) per tier. |
| [`AdminSubscriptionService`](AdminSubscriptionService.java) | Administrative subscription interventions, single subscription detail retrieval, manual grants, and dispute resolutions. |
| [`AdminSubscriptionPlanService`](AdminSubscriptionPlanService.java) | Plan administration, single plan retrieval (active/inactive), price modifications, and archive workflows. |
| [`AdminRefundService`](AdminRefundService.java) | Financial refund execution and mandatory audit reason logging. |

---

## Supporting Types & Exceptions

| Class | Responsibility |
| :--- | :--- |
| [`ChargilyWebhookEvent`](ChargilyWebhookEvent.java) | Representation of a parsed Chargily Pay webhook callback event. |
| [`WebhookJsonValue`](WebhookJsonValue.java) | Typed wrapper for audit logging raw webhook payload JSON structures. |
| [`MalformedWebhookException`](MalformedWebhookException.java) | Thrown when an incoming webhook payload cannot be parsed or lacks required fields. |
| [`InvalidWebhookSignatureException`](InvalidWebhookSignatureException.java) | Thrown when HMAC-SHA256 signature verification fails for an incoming webhook. |
