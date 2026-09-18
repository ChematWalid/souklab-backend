package com.project.souklab.integration.chargily;

public interface ChargilyCheckoutClient {
    ChargilyCheckoutResponse createCheckout(ChargilyCheckoutRequest request);
}
