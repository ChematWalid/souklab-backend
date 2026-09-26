package com.project.souklab.integration.payment;

/**
 * Result returned by a payment gateway provider after creating a checkout session.
 */
public record PaymentCheckoutResult(
        String checkoutId,
        String checkoutUrl
) {
}
