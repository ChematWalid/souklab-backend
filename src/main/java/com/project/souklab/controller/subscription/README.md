# Subscription & Payment Controllers (`com.project.souklab.controller.subscription`)

REST controllers for subscription plans, Chargily Pay V2 checkout, signature-verified webhooks, and administrative subscription management.

---

## Controllers Reference

| Controller | Base Route | Access Policy | Responsibility |
| :--- | :--- | :--- | :--- |
| [`SubscriptionPlanController`](SubscriptionPlanController.java) | `/api/v1/subscriptions/plans` | Public | Lists active subscription plans for artisans and clients. |
| [`SubscriptionCheckoutController`](SubscriptionCheckoutController.java) | `/api/v1/subscriptions/checkout` | Authenticated | Creates Chargily checkout session and returns hosted checkout URL. |
| [`SubscriptionAccountController`](SubscriptionAccountController.java) | `/api/v1/subscriptions/me` | Authenticated | Retrieves current user subscription state and payment history. |
| [`ChargilyWebhookController`](ChargilyWebhookController.java) | `/api/v1/integrations/chargily` | Public (HMAC Verified) | Receives Chargily Pay V2 webhook events (`checkout.paid`, `checkout.failed`, `checkout.canceled`). |
| [`AdminSubscriptionController`](AdminSubscriptionController.java) | `/api/v1/admin/subscriptions` | `permission:admin:subscriptions` | Administrative search, state correction, manual subscription grants, and cancellations. |
| [`AdminSubscriptionPlanController`](AdminSubscriptionPlanController.java) | `/api/v1/admin/subscriptions/plans` | `permission:admin:subscriptions` | Administrative creation, pricing updates, and archiving of plans. |
| [`AdminRefundController`](AdminRefundController.java) | `/api/v1/admin/subscriptions/refunds` | `permission:admin:financials` | Administrative refund execution and financial reason auditing. |
