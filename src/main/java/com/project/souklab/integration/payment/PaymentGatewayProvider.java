package com.project.souklab.integration.payment;

import com.project.souklab.model.PaymentProvider;

/**
 * Service Provider Interface (SPI) for payment gateways.
 * Decouples platform checkout orchestration from specific payment vendors
 * (Chargily Pay V2, Stripe, BaridiMob, or mock gateways for local testing).
 */
public interface PaymentGatewayProvider {

    /**
     * Identifies the provider enum mapped to this gateway implementation.
     */
    PaymentProvider getProvider();

    /**
     * Initiates a checkout session with the external payment vendor.
     *
     * @param command parameters needed to build the checkout session
     * @return checkout identifier and redirection URL
     */
    PaymentCheckoutResult createCheckout(PaymentCheckoutCommand command);
}
