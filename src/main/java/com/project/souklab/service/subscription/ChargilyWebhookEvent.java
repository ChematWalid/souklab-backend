package com.project.souklab.service.subscription;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

/** Stable, grouped representation of supported Chargily webhook event types. */
public final class ChargilyWebhookEvent {
    private ChargilyWebhookEvent() { }

    public enum Checkout {
        PAID("checkout.paid"),
        FAILED("checkout.failed"),
        CANCELED("checkout.canceled"),
        UNKNOWN("unknown");

        private final String value;

        Checkout(String value) { this.value = value; }

        @JsonValue
        public String value() { return value; }

        public static Optional<Checkout> fromValue(String value) {
            return Arrays.stream(values()).filter(event -> event != UNKNOWN && event.value.equals(value)).findFirst();
        }

        public static Checkout fromValueOrUnknown(String value) {
            return fromValue(value).orElse(UNKNOWN);
        }
    }
}
