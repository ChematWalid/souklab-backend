# Domain Events & Event-Driven Architecture (`com.project.souklab.event`)

In-process domain event infrastructure for the Souklab monolithic architecture, enabling loose coupling across domain aggregates (Open-Closed Principle).

---

## Architectural Principles

1. **Transactional Safety**: Event listeners use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` to process side effects (notifications, audits, analytics, webhooks) only after the primary domain mutation commits.
2. **Failure Isolation**: A failure in secondary pipelines (e.g. WebSocket broadcast, email transmission) does not roll back the user's primary business transaction.
3. **Extensibility**: Adding new reactions to business actions (e.g. awarding loyalty badges, triggering search indexing, sending push notifications) requires zero modifications to existing domain services.

---

## Component Reference

| Interface / Class / Record | Responsibility |
| :--- | :--- |
| [`DomainEvent`](DomainEvent.java) | Base marker interface with event UUID and timestamp. |
| [`DomainEventPublisher`](DomainEventPublisher.java) | Type-safe Spring `ApplicationEventPublisher` adapter with observability logging. |
| [`UserStatusChangedEvent`](user/UserStatusChangedEvent.java) | Account lifecycle transitions (`PENDING`, `ACTIVE`, `SUSPENDED`). |
| [`FormationEnrolledEvent`](formation/FormationEnrolledEvent.java) | Artisan masterclass enrollment completions. |
| [`FormationPublishedEvent`](formation/FormationPublishedEvent.java) | Accredited formation publication events. |
| [`FeedPostPublishedEvent`](feed/FeedPostPublishedEvent.java) | Moderated feed post community publication. |
| [`PaymentCompletedEvent`](subscription/PaymentCompletedEvent.java) | Successful subscription payment transactions. |
| [`AnalyticsDomainEventListener`](listener/AnalyticsDomainEventListener.java) | Sample decoupled listener executing after transaction commit. |
