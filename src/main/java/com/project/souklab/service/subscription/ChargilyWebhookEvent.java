package com.project.souklab.service.subscription;

import java.util.Arrays;
import java.util.Optional;

/** Stable, grouped representation of supported Chargily webhook event types. */
public final class ChargilyWebhookEvent {
    private ChargilyWebhookEvent() { }

    public enum Checkout {
        PAID("checkout.paid"),
        FAILED("checkout.failed"),
        CANCELED("checkout.canceled");

        private final String value;

        Checkout(String value) { this.value = value; }

        public String value() { return value; }

        public static Optional<Checkout> fromValue(String value) {
            return Arrays.stream(values()).filter(event -> event.value.equals(value)).findFirst();
        }
    }
}
