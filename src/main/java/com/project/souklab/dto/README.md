# Data Transfer Objects (`com.project.souklab.dto`)

Contracts and schemas defining client-server communications across the application.

---

## Architectural Principles

- **No Entity Leaks**: JPA persistence entities (`User`, `Artisan`, `Notification`) are never exposed directly via REST responses to avoid mass assignment vulnerabilities and accidental data leakage.
- **Strict Typing**: All payload structures use typed Java records or Lombok-annotated classes with Jackson serialization hints.
- **Jakarta Validation**: Inbound DTOs enforce constraint annotations (`@NotBlank`, `@Email`, `@Size`, `@Pattern`).

---

## Subpackages

| Package | Purpose |
| :--- | :--- |
| [`admin`](admin/README.md) | Administrative audit log representations. |
| [`analytics`](analytics/README.md) | Query job requests, execution responses, rollups, and CSV artifact payloads. |
| [`artisan`](artisan/README.md) | Certification and gallery portfolio image outbound representations. |
| [`auth`](auth/README.md) | User registration, login credentials, password resets, and JWT responses. |
| [`catalog`](catalog/README.md) | Craft categories, raw materials, epochs, techniques, and administrative regions. |
| [`chat`](chat/README.md) | Conversation descriptors, message payloads, typing commands, and WebSocket events. |
| [`common`](common/README.md) | Standard API envelopes (`ApiResponse<T>`) and pagination wrappers (`PaginatedResponse<T>`). |
| [`directory`](directory/README.md) | Public directory search result cards, criteria filter payloads, and sort options. |
| [`feed`](feed/README.md) | Moderated feed post, media, and moderation payloads. |
| [`formateur`](formateur/README.md) | Accreditation applications, approval notes, rejections, and cooldown data. |
| [`formation`](formation/README.md) | Masterclass authoring, curriculum updates, enrollment, review, and file descriptors. |
| [`notification`](notification/README.md) | Notification item feed responses. |
| [`profile`](profile/README.md) | Artisan public profile views, client representations, and patch updates. |
| [`report`](report/README.md) | Report submission, resolution, and moderation responses. |
| [`review`](review/README.md) | Decimal formation review requests and responses. |
| [`subscription`](subscription/README.md) | Plan configurations, checkout requests/responses, payment receipts, and webhook logs. |
| [`user`](user/README.md) | User moderation requests (bans, timeouts) and avatar representations. |
