# Subscription & Payment Controllers (`com.project.souklab.controller.subscription`)

REST controllers for subscription plans, Chargily Pay V2 (EDAHABIA / CIB) checkout, signature-verified webhooks, user billing account state, and administrative subscription management.

---

## Endpoints

### 1. Public Plans (`SubscriptionPlanController`)
| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/subscriptions/plans` | Public | List active plans | Retrieves available subscription tiers and pricing for clients and artisans. |

### 2. Checkout & Renewals (`SubscriptionCheckoutController`)
| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/subscriptions/checkout` | Authenticated | Checkout subscription | Creates Chargily Pay V2 session returning checkout URL. Supports `Idempotency-Key`. |
| `POST` | `/api/v1/subscriptions/{id}/renew` | Authenticated | Renew subscription | Generates renewal checkout session. Supports `Idempotency-Key`. |

### 3. User Billing Account (`SubscriptionAccountController`)
| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/subscriptions/current` | Authenticated | Current subscription | Returns active subscription and tier entitlements. |
| `GET` | `/api/v1/subscriptions` | Authenticated | Subscription history | Lists past and current subscriptions. |
| `POST` | `/api/v1/subscriptions/{id}/cancel` | Authenticated | Cancel renewal | Cancels automatic renewal for an active subscription. |
| `GET` | `/api/v1/payments` | Authenticated | Payment history | Lists completed payment transactions and receipts. |
| `GET` | `/api/v1/payments/{id}` | Authenticated | Payment receipt | Retrieves transaction receipt by ID. |

### 4. Chargily Webhook (`ChargilyWebhookController`)
| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/integrations/chargily/webhook` | Public (HMAC-SHA256) | Chargily webhook | Processes asynchronous payment callbacks (`checkout.paid`, `checkout.failed`, etc.). |

### 5. Administration
| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET/POST` | `/api/v1/admin/subscriptions[/**]` | `admin:subscriptions` | Manage subscriptions | Administrative search, state correction, grants, and cancellations. |
| `GET/POST` | `/api/v1/admin/subscriptions/plans[/**]` | `admin:subscriptions` | Manage plans | Administrative plan creation, pricing changes, and archiving. |
| `POST` | `/api/v1/admin/payments/{id}/refund` | `admin:financials` | Issue refund | Administrative refund execution and financial auditing. |
