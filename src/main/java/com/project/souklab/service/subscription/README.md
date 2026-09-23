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
| [`SubscriptionPlanService`](SubscriptionPlanService.java) | Cached retrieval of publicly active subscription plans. |
| [`SubscriptionPlanRules`](SubscriptionPlanRules.java) | Domain entitlement rules defining limits (portfolio size, formation count) per tier. |
| [`AdminSubscriptionService`](AdminSubscriptionService.java) | Administrative subscription interventions, manual grants, and dispute resolutions. |
| [`AdminSubscriptionPlanService`](AdminSubscriptionPlanService.java) | Plan administration, price modifications, and archive workflows. |
| [`AdminRefundService`](AdminRefundService.java) | Financial refund execution and mandatory audit reason logging. |
