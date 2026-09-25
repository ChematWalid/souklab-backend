# Subscription & Payment Controllers (`com.project.souklab.controller.subscription`)

REST controllers for subscription plans, Chargily Pay V2 (EDAHABIA / CIB) checkout, signature-verified webhooks, user billing account state, and administrative subscription management.

---

## Endpoints

### 1. Public Plans (`SubscriptionPlanController`)
| Method | Endpoint | Access | Summary | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/subscriptions/plans` | Public | List active plans | Retrieves available subscription tiers and pricing for clients and artisans. |
| `GET` | `/api/v1/subscriptions/plans/{id}` | Public | Get active plan | Retrieves single active subscription plan details by ID. |

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
| `GET` | `/api/v1/admin/subscriptions` | `permission:financial:admin` | List subscriptions | Administrative search, state inspection, and filtering. |
| `GET` | `/api/v1/admin/subscriptions/{id}` | `permission:financial:admin` | Get subscription | Retrieves full single subscription details by ID. |
| `POST` | `/api/v1/admin/subscriptions/grant` | `permission:financial:admin` | Grant subscription | Manual administrative subscription grant. |
| `POST` | `/api/v1/admin/subscriptions/{id}/revoke` | `permission:financial:admin` | Revoke subscription | Administrative subscription revocation. |
| `GET` | `/api/v1/admin/subscription-plans` | `permission:financial:admin` | List all plans | Administrative plan listing including inactive plans. |
| `GET` | `/api/v1/admin/subscription-plans/{id}` | `permission:financial:admin` | Get plan | Retrieves single subscription plan details (including inactive) by ID. |
| `POST` | `/api/v1/admin/subscription-plans` | `permission:financial:admin` | Create plan | Administrative plan creation and pricing setup. |
| `PUT` | `/api/v1/admin/subscription-plans/{id}` | `permission:financial:admin` | Update plan | Administrative plan modification. |
| `DELETE` | `/api/v1/admin/subscription-plans/{id}` | `permission:financial:admin` | Deactivate plan | Soft-deactivates or archives a plan. |
| `POST` | `/api/v1/admin/payments/{id}/refund` | `permission:financial:admin` | Issue refund | Administrative refund execution and financial auditing. |
