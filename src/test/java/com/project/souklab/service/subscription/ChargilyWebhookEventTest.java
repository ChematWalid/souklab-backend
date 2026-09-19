package com.project.souklab.service.subscription;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChargilyWebhookEventTest {

    @Test
    void supportedValuesResolveToGroupedEnumMembers() {
        assertThat(ChargilyWebhookEvent.Checkout.fromValue("checkout.paid"))
                .contains(ChargilyWebhookEvent.Checkout.PAID);
        assertThat(ChargilyWebhookEvent.Checkout.PAID.value()).isEqualTo("checkout.paid");
    }

    @Test
    void unknownHistoricalValuesUseExplicitFallbackWithoutBeingAcceptedForProcessing() {
        assertThat(ChargilyWebhookEvent.Checkout.fromValue("checkout.unknown")).isEmpty();
        assertThat(ChargilyWebhookEvent.Checkout.fromValueOrUnknown("checkout.unknown"))
                .isEqualTo(ChargilyWebhookEvent.Checkout.UNKNOWN);
    }
}
