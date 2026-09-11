package com.project.souklab.model;

public enum NotificationType {
    ACCOUNT_VALIDATED,
    /** Reserved for Phase 10 (Admin direct account rejection). */
    ACCOUNT_REJECTED,
    ACCOUNT_SUSPENDED,
    /** Reserved for Phase 6 (Formations approval). */
    FORMATION_APPROVED,
    /** Reserved for Phase 6 (Formations rejection). */
    FORMATION_REJECTED,
    /** Reserved for Phase 8 (Realtime Direct Messaging). */
    NEW_MESSAGE,
    /** Reserved for Phase 9 (Monetization & Subscriptions). */
    SUBSCRIPTION_RENEWED,
    /** Reserved for Phase 9 (Monetization & Subscriptions). */
    SUBSCRIPTION_EXPIRED,
    /** Reserved for Phase 9 (Chargily Pay V2 Payments). */
    PAYMENT_SUCCESS,
    /** Reserved for Phase 9 (Chargily Pay V2 Payments). */
    PAYMENT_FAILED,
    /** Reserved for Phase 7 (Social Feed & Moderation Reports). */
    NEW_REPORT,
    /** Reserved for Phase 7 (Social Feed & Reviews). */
    NEW_REVIEW,
    /** Reserved for Phase 6 (Formations announcement). */
    NEW_FORMATION,
    FORMATEUR_REQUEST_SUBMITTED,
    FORMATEUR_APPROVED,
    FORMATEUR_GRANTED,
    FORMATEUR_REJECTED,
    FORMATEUR_REVOKED,
    ACCOUNT_REINSTATED
}
