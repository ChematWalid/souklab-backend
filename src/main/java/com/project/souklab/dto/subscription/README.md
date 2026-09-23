# Subscription & Payment DTOs (`com.project.souklab.dto.subscription`)

Data Transfer Objects representing subscription plans, checkout requests, payment receipts, webhook audit logs, and administrative corrections.

---

## DTOs Reference

| Class | Type | Responsibility |
| :--- | :--- | :--- |
| [`SubscriptionCheckoutRequest`](SubscriptionCheckoutRequest.java) | Request | Client request to initiate checkout (target plan ID, payment mode). |
| [`SubscriptionCheckoutResponse`](SubscriptionCheckoutResponse.java) | Response | Contains generated Chargily hosted payment URL and checkout tracking ID. |
| [`SubscriptionPlanResponse`](SubscriptionPlanResponse.java) | Response | Public representation of plan tiers, pricing in DZD, and feature limits. |
| [`SubscriptionPlanRequest`](SubscriptionPlanRequest.java) | Request | Administrative request to configure or update a subscription plan. |
| [`SubscriptionResponse`](SubscriptionResponse.java) | Response | Details of an active user subscription (tier, period, auto-renewal flag). |
| [`PaymentResponse`](PaymentResponse.java) | Response | Transaction receipt details (amount, currency DZD, method CIB/EDAHABIA, status). |
| [`ManualSubscriptionGrantRequest`](ManualSubscriptionGrantRequest.java) | Request | Admin payload to grant a subscription plan directly without payment gateway. |
| [`FinancialReasonRequest`](FinancialReasonRequest.java) | Request | Mandatory auditable explanation required for refunds and corrections. |
| [`AdminWebhookLogResponse`](AdminWebhookLogResponse.java) | Response | Diagnostic view of incoming Chargily webhook payloads and processing results. |
